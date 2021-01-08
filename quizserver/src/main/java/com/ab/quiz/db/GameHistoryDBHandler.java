package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.GameResults;
import com.ab.quiz.pojo.GamePlayers;

/*
CREATE TABLE GameHistory(id bigint NOT NULL AUTO_INCREMENT, 
		gameId bigint NOT NULL,
		gamePlayedTime bigint NOT NULL,
		ticketRate int NOT NULL,
		playerCount int NOT NULL, 
		winnerList varchar(300), PRIMARY KEY (id));
		
CREATE TABLE PlayerHistory(id bigint NOT NULL AUTO_INCREMENT,
   		gameId bigint NOT NULL,
   		userId bigint NOT NULL, PRIMARY KEY (id)); 
*/

// Insert
// Get by gameId

public class GameHistoryDBHandler {
	private static String TABLE_NAME = "GameHistory";
	private static String PLAYER_HISTORY_TABLE_NAME = "PlayerHistory"; 
	
	@SuppressWarnings("unused")
	private static String ID = "id";
	private static String GAMEID = "gameId";
	private static String GAME_PLAYED_TIME = "gamePlayedTime";
	private static String TICKET_RATE = "ticketRate";
	private static String PLAYER_COUNT = "playerCount";
	private static String WINNERS_LIST = "winnerList";
	
	@SuppressWarnings("unused")
	private static String PLAYER_ID = "id";
	private static String PLAYER_GAMEID = "gameId";
	private static String PLAYER_USERID = "userId";
	
	private static final String CREATE_GAME_HISTORY = "INSERT INTO " + TABLE_NAME   
			+ "(" + GAMEID + "," + GAME_PLAYED_TIME + "," + TICKET_RATE + "," + PLAYER_COUNT + ","
			+ WINNERS_LIST + ") VALUES"
			+ "(?,?,?,?,?)";
	private static final String GET_GAME_HISTORY_ENTRY_BY_GAMEID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + GAMEID + "?";
	
	private static final String CREATE_PLAYER_HISTORY = "INSERT INTO " + PLAYER_HISTORY_TABLE_NAME   
			+ "(" + PLAYER_GAMEID + "," + PLAYER_USERID + ") VALUES"
			+ "(?,?)";
	
	private static final String GET_PLAYER_HISTORY_ENTRY_BY_USERID = "SELECT " + PLAYER_GAMEID + " FROM " + PLAYER_HISTORY_TABLE_NAME 
			+ " WHERE " + PLAYER_USERID + "?";
	
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
	
	public boolean createGameHistoryWithPlayers(GameResults gameResults, List<GamePlayers> playersList) 
			throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_GAME_HISTORY);
			
			ps.setLong(1, gameResults.getGameId());
			ps.setLong(2, gameResults.getGamePlayedTime());
			ps.setInt(3, gameResults.getTicketRate());
			ps.setInt(4, gameResults.getCurrentCount());
			ps.setString(5, gameResults.getWinnersList());
			
			int createResult = ps.executeUpdate();
			logger.debug("Created a game entry, The result is {}", createResult);
			if (playersList == null) {
				return (createResult > 0);
			}
			if (createResult > 0) {
				PreparedStatement psPlayer = dbConn.prepareStatement(CREATE_PLAYER_HISTORY);
				
				boolean finalResult = true;
				for (GamePlayers gamePlayer : playersList) {
					psPlayer.setLong(1, gamePlayer.getGameId());
					psPlayer.setLong(2, gamePlayer.getUserId());
					
					createResult = psPlayer.executeUpdate();
					logger.debug("Created a player entry, The result is {}", createResult);
					finalResult = finalResult & (createResult > 0);
				}
				
				if (psPlayer != null) {
					psPlayer.close();
				}
				return finalResult;
			}
		} catch(SQLException ex) {
			logger.error("Error creating game history and players entry", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return false;
	}
	
	public List<GameResults> getPlayedGames(long userId) throws SQLException {
		logger.debug("In getPlayedGames() with {}", userId);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_PLAYER_HISTORY_ENTRY_BY_USERID);
		ps.setLong(1, userId);
		
		PreparedStatement playerPs = dbConn.prepareStatement(GET_GAME_HISTORY_ENTRY_BY_GAMEID);
		
		List<GameResults> gameResultSet = new ArrayList<>();
		
		try {
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					long gameId = rs.getLong(PLAYER_GAMEID);
					
					playerPs.setLong(1, gameId);
					
					GameResults gameResult = new GameResults();
					
					gameResult.setGameId(rs.getLong(GAMEID));
					gameResult.setGamePlayedTime(rs.getLong(GAME_PLAYED_TIME));
					gameResult.setTicketRate(rs.getInt(TICKET_RATE));
					gameResult.setCurrentCount(rs.getInt(PLAYER_COUNT));
					gameResult.setWinnersList(rs.getString(WINNERS_LIST));
					
					gameResultSet.add(gameResult);
				}
				rs.close();
				if (playerPs != null) {
					playerPs.close();
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getQuestionSet()", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("History game result set size is {}" , gameResultSet.size());
		return gameResultSet;
	}
}
