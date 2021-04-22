package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
CREATE TABLE LASTGAMEDETAILS (ID INT UNSIGNED NOT NULL, 
		GAMELASTID BIGINT NOT NULL , PRIMARY KEY (ID)) ENGINE = INNODB;
INSERT INTO LASTGAMEDETAILS (ID,GAMELASTID) VALUES (1, 0); 		
*/

public class LastGameIdDBHandler {
	private static final Logger logger = LogManager.getLogger(LastGameIdDBHandler.class);
	
	private static String TABLE_NAME = "LASTGAMEDETAILS";
	
	private static String ID = "ID";
	private static String LAST_ID = "GAMELASTID";
	
	private static final String GET_GAME_LAST_ID_BY_ID = "SELECT " + LAST_ID 
			+ " FROM " + TABLE_NAME + " WHERE "+ ID + " = 1";
	private static final String UPDATE_GAME_ID = "UPDATE " + TABLE_NAME + " SET " 
			+ LAST_ID + "= ? WHERE "+ ID + " = 1";
	
	private static LastGameIdDBHandler instance = null;
	
	private LastGameIdDBHandler() {
	}
	
	public static LastGameIdDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In GameLastIdDBHandler getInstance() method instance created");
			instance = new LastGameIdDBHandler();
		}
		return instance;
	}
	
	public long getLastGameId() throws SQLException {
		logger.info("getLastGameId called");
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(GET_GAME_LAST_ID_BY_ID);
		
		long lastGameId = 0;
		ResultSet rs = null;
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					lastGameId = rs.getLong(LAST_ID);
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getLastGameId()", ex);
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
		logger.info("The lastGameId is {}", lastGameId);
		return lastGameId;
	}
	
	public boolean updateLastGameId(long gameId) throws SQLException {
		
		logger.info("This is in updateLastGameId {}", gameId);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(UPDATE_GAME_ID);
		ps.setLong(1, gameId);
		
		try {
			int resultCount = ps.executeUpdate();
			logger.info("The updated row count is {}", (resultCount > 0));
		}
		catch(SQLException ex) {
			logger.error("Error updating in updateLastGameId", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return true;
	}
}
