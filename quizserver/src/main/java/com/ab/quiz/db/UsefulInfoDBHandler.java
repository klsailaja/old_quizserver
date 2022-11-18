package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;

public class UsefulInfoDBHandler {
	
	private static final Logger logger = LogManager.getLogger(UsefulInfoDBHandler.class);
	
	private static String TABLE_NAME = "USEFULINFO";
	private static String ID = "ID";
	private static String STATEMENT = "NSTATEMENT";
	
	private static final String SELECT_VALUE = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + ID + " = ? ";
	
	private static UsefulInfoDBHandler instance = null;
	
	public static UsefulInfoDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In UsefulInfoDBHandler getInstance() method instance created");
			instance = new UsefulInfoDBHandler();
		}
		return instance;
	}
	
	public String getPicQuestionPrefix() throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(SELECT_VALUE);
		ps.setLong(1, 1);
		
		ResultSet rs = null;
		String prefix = null;
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					prefix = rs.getString(STATEMENT);
				}
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while getting the getPicQuestionPrefix");
			logger.error("SQLException in getPicQuestionPrefix", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		return prefix;
	}
}
