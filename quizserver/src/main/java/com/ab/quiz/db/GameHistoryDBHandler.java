package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.GamePlayers;
import com.ab.quiz.pojo.GameResults;
import com.ab.quiz.pojo.UserHistoryGameDetails;

/*
CREATE TABLE GAMEHISTORY(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		GAMEID BIGINT NOT NULL,
		GAMEPLAYEDTIME BIGINT NOT NULL,
		TICKETRATE INT NOT NULL,
		CELEBRITYNAME VARCHAR(50) NULL,
		WINNERLIST VARCHAR(500), PRIMARY KEY (ID)) ENGINE = INNODB;
CREATE TABLE PLAYERHISTORY(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
   		GAMEID BIGINT NOT NULL,
   		USERID BIGINT NOT NULL, PRIMARY KEY (ID)) ENGINE = INNODB;
   		    		
CREATE INDEX GAMEHISTORY_Inx ON GAMEHISTORY(GAMEID);		
DROP INDEX GAMEHISTORY_Inx ON GAMEHISTORY;		
CREATE INDEX GAMEHISTORY_Inx ON GAMEHISTORY(GAMEID);
CREATE INDEX PLAYERHISTORY_Inx ON PLAYERHISTORY(USERID);		
DROP INDEX PLAYERHISTORY_Inx ON PLAYERHISTORY;		
CREATE INDEX PLAYERHISTORY_Inx ON PLAYERHISTORY(USERID);
*/

// Insert
// Get by gameId

public class GameHistoryDBHandler {
	private static String TABLE_NAME = "GAMEHISTORY";
	private static String PLAYER_HISTORY_TABLE_NAME = "PLAYERHISTORY"; 
	
	//private static String ID = "ID";
	private static String GAMEID = "GAMEID";
	private static String GAME_PLAYED_TIME = "GAMEPLAYEDTIME";
	private static String TICKET_RATE = "TICKETRATE";
	private static String CELEBRITY_NAME = "CELEBRITYNAME";
	private static String WINNERS_LIST = "WINNERLIST";
	
	private static String PLAYER_ID = "ID";
	private static String PLAYER_GAMEID = "GAMEID";
	private static String PLAYER_USERID = "USERID";
	//private static String ISWINNER = "ISWINNER";
	
	private static final String CREATE_GAME_HISTORY = "INSERT INTO " + TABLE_NAME   
			+ "(" + GAMEID + "," + GAME_PLAYED_TIME + "," + TICKET_RATE + "," + CELEBRITY_NAME + ","
			+ WINNERS_LIST + ") VALUES"
			+ "(?,?,?,?,?)";
	private static final String GET_GAME_HISTORY_ENTRY_BY_GAMEID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + GAMEID + " = ?";
	
	private static final String CREATE_PLAYER_HISTORY = "INSERT INTO " + PLAYER_HISTORY_TABLE_NAME   
			+ "(" + PLAYER_GAMEID + "," + PLAYER_USERID + ") VALUES"
			+ "(?,?)";
	
	private static final String GET_GAMEID_SET_BY_USERID = "SELECT " + PLAYER_GAMEID + " FROM " + PLAYER_HISTORY_TABLE_NAME 
			+ " WHERE " + PLAYER_USERID + " = ? " + "ORDER BY " + PLAYER_ID + " DESC LIMIT ?,10";
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM " +  PLAYER_HISTORY_TABLE_NAME + " WHERE "
			+ PLAYER_USERID + " = ? ";
	private static final String REMOVE_OLD_RECORDS = "DELETE FROM " + TABLE_NAME 
			+ " WHERE (" + GAME_PLAYED_TIME + " < ? AND ID <> 0)";
	private static final String SELECT_OLD_RECORDS_GAMEID = "SELECT " + GAMEID + " FROM " + TABLE_NAME + " WHERE " + GAME_PLAYED_TIME + " < ?";
	private static final String REMOVE_OLD_RECORDS_PLAYERS = "DELETE FROM " + PLAYER_HISTORY_TABLE_NAME 
			+ " WHERE " + PLAYER_GAMEID + " = ? ";
	
	private static final Logger logger = LogManager.getLogger(GameHistoryDBHandler.class);
	private static GameHistoryDBHandler instance = null;
	
	private GameHistoryDBHandler() {
	}
	
	public static GameHistoryDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In GameHistoryDBHandler getInstance() method instance created");
			instance = new GameHistoryDBHandler();
		}
		return instance;
	}
	
	public List<Long> deleteRecords(long timePeriod) throws SQLException {
		logger.info("In deleteRecords method");
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Long> gameIds = new ArrayList<Long>();
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(SELECT_OLD_RECORDS_GAMEID);
			
			ps.setLong(1, timePeriod);
			
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					gameIds.add(rs.getLong(GAMEID));
				}
			}
			
			PreparedStatement psDel = dbConn.prepareStatement(REMOVE_OLD_RECORDS);
			psDel.setLong(1, timePeriod);
			int result = psDel.executeUpdate();
			logger.info("In deleteRecords number of games deleted count : {}", result);
			if (psDel != null) {
				psDel.close();
			}
			
		} catch (SQLException ex) {
			logger.error("******************************");
			logger.error("SQLException in deleteRecords()", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return gameIds;
	}
	
	public void bulkDeletePlayerDetails(List<Long> gameIds, int batchSize) throws SQLException {
		if (gameIds.size() == 0) {
			return;
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement psPlayer = null;
		
		long startTime = System.currentTimeMillis();
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			logger.info("In bulkDeletePlayerDetails ");
			
			dbConn.setAutoCommit(false);
			
			psPlayer = dbConn.prepareStatement(REMOVE_OLD_RECORDS_PLAYERS);
			
			int totalFailureCount = 0;
			int totalSuccessCount = 0;
			
			int index = 0;
			for (Long gameId : gameIds) {
				psPlayer.setLong(1, gameId);
				psPlayer.addBatch();
				index++;
				
				if (index == batchSize) {
					index = 0;
					int results[] = psPlayer.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					for (int result : results) {
						if (result >= 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = psPlayer.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result >= 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("Bulk deleted Game Player records with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
			logger.info("Time taken to process this query in Millis : {}", (System.currentTimeMillis() - startTime));
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error deleting game players entries in bulk mode", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (psPlayer != null) {
				psPlayer.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public void bulkInsertGamePlayers(List<GamePlayers> playersList, int batchSize) throws SQLException {
		
		logger.info("In bulkInsertGamePlayers {}", playersList.size());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement psPlayer = null;
		
		long startTime = System.currentTimeMillis();
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			logger.info("In bulkInsertGamePlayers " + dbConn.hashCode());
			
			dbConn.setAutoCommit(false);
			
			psPlayer = dbConn.prepareStatement(CREATE_PLAYER_HISTORY);
			
			int totalFailureCount = 0;
			int totalSuccessCount = 0;
			
			int index = 0;
			for (GamePlayers gamePlayer : playersList) {
				psPlayer.setLong(1, gamePlayer.getGameId());
				psPlayer.setLong(2, gamePlayer.getUserId());
				psPlayer.addBatch();
				index++;
				
				if (index == batchSize) {
					index = 0;
					int results[] = psPlayer.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					for (int result : results) {
						if (result == 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = psPlayer.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result == 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("Bulk inserted Game Player records with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
			logger.info("Time taken to process this query in Millis : {}", (System.currentTimeMillis() - startTime));
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error creating game players entries in bulk mode", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (psPlayer != null) {
				psPlayer.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public void bulkInsertGameResults(List<GameResults> gameResultsList, int batchSize1) 
			throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			//dbConn = cp.getDBConnection();
			dbConn = cp.getConnectionNotFromPool();
			logger.info("In BulkInsertGameResults " + dbConn.hashCode());
			
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_GAME_HISTORY);
			
			int totalFailureCount = 0;
			int totalSuccessCount = 0;
			
			int index = 0;
			for (GameResults gameResults : gameResultsList) {
				ps.setLong(1, gameResults.getGameId());
				ps.setLong(2, gameResults.getGamePlayedTime());
				ps.setInt(3, gameResults.getTktRate());
				ps.setString(4, gameResults.getCelebrityName());
				ps.setString(5, gameResults.getWinnersList());
				
				ps.addBatch();
				index++;
				
				if (index == batchSize1) {
					int results[] = ps.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					index = 0;
					for (int result : results) {
						if (result == 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			
			if (index > 0) {
				int results[] = ps.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result == 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			
			logger.info("Bulk inserted Game Records with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
			
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error creating game history entries in bulk mode", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				logger.info("In BulkInsertGameResults close" + dbConn.hashCode());
				dbConn.close();
			}
		}
	}
	
	public List<Long> getUserPlayedGameIds(long userId, int startRowNo) throws SQLException {
		
		logger.debug("getUserPlayedGameIds is called with userId {} : startRowNo {}",
				userId, startRowNo);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_GAMEID_SET_BY_USERID);
		ps.setLong(1, userId);
		ps.setInt(2, startRowNo);
		
		List<Long> gameIds = new ArrayList<Long>();
		ResultSet rs = null;
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					gameIds.add(rs.getLong(PLAYER_GAMEID));
				}
			}
		} catch (SQLException ex) {
			logger.error("******************************");
			logger.error("SQLException in getUserPlayedGameIds()", ex);
			logger.error("******************************");
			throw ex;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.info("#Games played by the user {} are {}", userId, gameIds.size());
		return gameIds;
	}
	
	public UserHistoryGameDetails getUserPlayedGameDetails(long userId, int startRowNo)
			throws SQLException {
		
		logger.debug("getUserPlayedGameDetails is called with userId {} : startRowNo {}",
				userId, startRowNo);
		
		UserHistoryGameDetails historyGameDetails = new UserHistoryGameDetails();
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		ResultSet rs = null;
		List<GameResults> gameResultSet = new ArrayList<>();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_GAME_HISTORY_ENTRY_BY_GAMEID);
		PreparedStatement totalPs = dbConn.prepareStatement(GET_TOTAL_COUNT);
		ResultSet totalRs = null;
		
		try {
			totalPs.setLong(1, userId);
			
			totalRs = totalPs.executeQuery();
			if (totalRs != null) {
				if (totalRs.next()) {
					
					int total = totalRs.getInt("COUNT(*)");
					historyGameDetails.setTotal(total);
					
					int lowerRange = startRowNo + 1;
					int higherRange = startRowNo + 10;
					
					if (higherRange < total) {
						historyGameDetails.setNextEnabled(true);
					} else {
						historyGameDetails.setNextEnabled(false);
					}
					if ((lowerRange - 10) > 0) {
						historyGameDetails.setPrevEnabled(true);
					} else {
						historyGameDetails.setPrevEnabled(false);
					}
				}
			}
			
			List<Long> gameIds = getUserPlayedGameIds(userId, startRowNo);
			
			for (int index = 0; index < gameIds.size(); index++) {
				ps.setLong(1, gameIds.get(index));
				rs = ps.executeQuery();
				if (rs.next()) {
					
					GameResults gameResult = new GameResults();
					
					gameResult.setsNo(++startRowNo);
					gameResult.setGameId(rs.getLong(GAMEID));
					gameResult.setGamePlayedTime(rs.getLong(GAME_PLAYED_TIME));
					gameResult.setTktRate(rs.getInt(TICKET_RATE));
					gameResult.setCelebrityName(rs.getString(CELEBRITY_NAME));
					gameResult.setWinnersList(rs.getString(WINNERS_LIST));
					
					gameResultSet.add(gameResult);
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getting history game details", ex);
		} finally {
			if (totalRs != null) {
				totalRs.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (totalPs != null) {
				totalPs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
		historyGameDetails.setHistoryGames(gameResultSet);
		logger.info("In Game History results. Returned size is {}", gameResultSet.size());
		return historyGameDetails;
	}
	
	/*
	public boolean createGameHistoryWithPlayers(GameResults gameResults, List<GamePlayers> playersList) 
			throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		PreparedStatement psPlayer = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_GAME_HISTORY);
			
			ps.setLong(1, gameResults.getGameId());
			ps.setLong(2, gameResults.getGamePlayedTime());
			ps.setInt(3, gameResults.getTktRate());
			ps.setString(4, gameResults.getCelebrityName());
			ps.setString(5, gameResults.getWinnersList());
			
			int createResult = ps.executeUpdate();
			logger.debug("Created a game entry, The result is {}", createResult);
			
			if (createResult > 0) {
				psPlayer = dbConn.prepareStatement(CREATE_PLAYER_HISTORY);
				
				boolean finalResult = true;
				for (GamePlayers gamePlayer : playersList) {
					psPlayer.setLong(1, gamePlayer.getGameId());
					psPlayer.setLong(2, gamePlayer.getUserId());
					psPlayer.addBatch();
				}
				int[] inserted = psPlayer.executeBatch();
				for (int result : inserted) {
					if (result <= 0) {
						finalResult = false;
						logger.error("Could not insert the record for {}", result);
					}
				}
				return finalResult;
			}
		} catch(SQLException ex) {
			logger.error("Error creating game history and players entry", ex);
			throw ex;
		} finally {
			if (psPlayer != null) {
				psPlayer.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return false;
	} */

	
	public static void main(String[] args) throws SQLException {
		
		GameHistoryDBHandler instance = GameHistoryDBHandler.getInstance();
		
		long total = 300000;
		List<GameResults> gameResultsList = new ArrayList<>();
		
		for (int i = 1; i <= total; i++) {
			GameResults gr = new GameResults();
			
			gr.setGameId(i);
			gr.setGamePlayedTime(System.currentTimeMillis());
			gr.setCelebrityName("TestCeleb");
			gr.setTktRate(100);
			gr.setWinnersList("PlayersResults");
			
			gameResultsList.add(gr);
		}
		System.out.println("GameResults size " + gameResultsList.size());
		
		List<GamePlayers> gamePlayersList = new ArrayList<>();
		for (int i = 1; i <= total; i++) {
			GamePlayers gp1 = new GamePlayers();
			gp1.setGameId(i);
			gp1.setUserId(10);
			
			gamePlayersList.add(gp1);
			
			GamePlayers gp2 = new GamePlayers();
			gp2.setGameId(i);
			gp2.setUserId(20);
			
			gamePlayersList.add(gp2);
			
			GamePlayers gp3 = new GamePlayers();
			gp3.setGameId(i);
			gp3.setUserId(30);
			
			gamePlayersList.add(gp3);
		}
		System.out.println("GamePlayers size " + gamePlayersList.size());
		instance.bulkInsertGameResults(gameResultsList, 400);
		instance.bulkInsertGamePlayers(gamePlayersList, 500);
	}
}
