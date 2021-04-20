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
CREATE TABLE GameHistory(id bigint NOT NULL AUTO_INCREMENT, 
		gameId bigint NOT NULL,
		gamePlayedTime bigint NOT NULL,
		ticketRate int NOT NULL,
		celebrityName varchar(50) NULL,
		winnerList varchar(500), PRIMARY KEY (id));
		
CREATE TABLE PlayerHistory(id bigint NOT NULL AUTO_INCREMENT,
   		gameId bigint NOT NULL,
   		userId bigint NOT NULL, PRIMARY KEY (id)); 
*/

// Insert
// Get by gameId

public class GameHistoryDBHandler {
	private static String TABLE_NAME = "GameHistory";
	private static String PLAYER_HISTORY_TABLE_NAME = "PlayerHistory"; 
	
	//private static String ID = "id";
	private static String GAMEID = "gameId";
	private static String GAME_PLAYED_TIME = "gamePlayedTime";
	private static String TICKET_RATE = "ticketRate";
	private static String CELEBRITY_NAME = "celebrityName";
	private static String WINNERS_LIST = "winnerList";
	
	private static String PLAYER_ID = "id";
	private static String PLAYER_GAMEID = "gameId";
	private static String PLAYER_USERID = "userId";
	
	private static final String CREATE_GAME_HISTORY = "INSERT INTO " + TABLE_NAME   
			+ "(" + GAMEID + "," + GAME_PLAYED_TIME + "," + TICKET_RATE + "," + CELEBRITY_NAME + ","
			+ WINNERS_LIST + ") VALUES"
			+ "(?,?,?,?,?)";
	private static final String GET_GAME_HISTORY_ENTRY_BY_GAMEID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + GAMEID + " = ? ";
	
	private static final String CREATE_PLAYER_HISTORY = "INSERT INTO " + PLAYER_HISTORY_TABLE_NAME   
			+ "(" + PLAYER_GAMEID + "," + PLAYER_USERID + ") VALUES"
			+ "(?,?)";
	
	private static final String GET_GAMEID_SET_BY_USERID = "SELECT " + PLAYER_GAMEID + " FROM " + PLAYER_HISTORY_TABLE_NAME 
			+ " WHERE " + PLAYER_USERID + " = ? " + "ORDER BY " + PLAYER_ID + " LIMIT ?,10";
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM " +  PLAYER_HISTORY_TABLE_NAME + " WHERE "
			+ PLAYER_USERID + " = ? ";
			
	
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
	
	public void bulkInsertGameResults(List<GameResults> gameResultsList, List<GamePlayers> playersList) 
			throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		PreparedStatement psPlayer = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_GAME_HISTORY);
			
			for (GameResults gameResults : gameResultsList) {
				ps.setLong(1, gameResults.getGameId());
				ps.setLong(2, gameResults.getGamePlayedTime());
				ps.setInt(3, gameResults.getTktRate());
				ps.setString(4, gameResults.getCelebrityName());
				ps.setString(5, gameResults.getWinnersList());
				
				ps.addBatch();
			}
			
			ps.executeBatch();
			dbConn.setAutoCommit(true);
			
			dbConn.setAutoCommit(false);
			
			psPlayer = dbConn.prepareStatement(CREATE_PLAYER_HISTORY);
			
			int index = 0;
			for (GamePlayers gamePlayer : playersList) {
				psPlayer.setLong(1, gamePlayer.getGameId());
				psPlayer.setLong(2, gamePlayer.getUserId());
				psPlayer.addBatch();
				index++;
				
				if (index == 51) {
					ps.executeBatch();
					
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
					index = 0;
				}
			}
			if (index > 0) {
				ps.executeBatch();
				dbConn.setAutoCommit(true);
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
			logger.error("SQLException in getUserPlayedGameIds()", ex);
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
		
		try {
			totalPs.setLong(1, userId);
			
			ResultSet totalRs = totalPs.executeQuery();
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
				totalRs.close();
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
				if (rs != null) {
					rs.close();
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getting history game details", ex);
		} finally {
			if (totalPs != null) {
				totalPs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception ex) {
			}
		}
		historyGameDetails.setHistoryGames(gameResultSet);
		return historyGameDetails;
	}
	
	public static void main(String[] args) throws SQLException {
		
		GameHistoryDBHandler instance = GameHistoryDBHandler.getInstance();
		UserHistoryGameDetails results = instance.getUserPlayedGameDetails(48, 0);
		for (GameResults res : results.getHistoryGames()) {
			System.out.println(res);
		}
	}
}
