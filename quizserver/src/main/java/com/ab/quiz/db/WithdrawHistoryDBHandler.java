package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.WithdrawHistory;

/*
CREATE TABLE WithdrawHistory(id bigint NOT NULL AUTO_INCREMENT, 
		refId varchar(20) NOT NULL,
		status int NOT NULL,
		reqType int NOT NULL,
		acDetailsRecord bigint NOT NULL,
		amount int NOT NULL,
		accDetails varchar(50),
		closeCmts varchar(100), PRIMARY KEY (id); 
*/
public class WithdrawHistoryDBHandler {
	private static final Logger logger = LogManager.getLogger(WithdrawHistoryDBHandler.class);
	
	private static String TABLE_NAME = "WithdrawHistory";

	private static String ID = "id";
	private static String REFID = "refId";
	private static String STATUS = "status";
	private static String REQTYPE = "reqType";
	private static String ACDETAILS_ID = "acDetailsRecord";
	private static String AMOUNT = "amount";
	private static String ACCDETAILS_STR = "accDetails";
	private static String CLOSECMTS_STR = "closeCmts";
	
	private static WithdrawHistoryDBHandler instance = null;
	
	private static final String CREATE_WITHDRAW_ENTRY = "INSERT INTO " + TABLE_NAME  
			+ "(" + REFID + "," + STATUS + "," + REQTYPE + "," + ACDETAILS_ID + ","
			+ AMOUNT + "," + ACCDETAILS_STR + CLOSECMTS_STR + ") VALUES"   
			+ "(?,?,?,?,?,?,?)";
	private static final String MAX_USER_PROFILE_ID = "SELECT MAX(ID) FROM " + TABLE_NAME;
	
	private WithdrawHistoryDBHandler() {
	}
	
	public static WithdrawHistoryDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawHistoryDBHandler();
		}
		return instance;
	}
	
	public long getMaxUserId() throws SQLException {
		
		logger.debug("In getMaxUserId() method");
		long maxUserId = -1;
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(MAX_USER_PROFILE_ID);
			
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					maxUserId = rs.getLong("MAX(ID)");
				}
				rs.close();
			}
		} catch (SQLException ex) {
			logger.error("SQL Exception in getMaxUserId()", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("Returning from getMaxUserId() {}", maxUserId);
		return maxUserId;
	}
	
	public boolean createWithdrawHistoryEntry(WithdrawHistory wdHistory) throws SQLException {
		
		logger.debug("In createWithdrawHistoryEntry for {}", wdHistory.getAccDetails());
		long maxUseId = getMaxUserId() + 1;
		String userIdStr = "Ref" + String.valueOf(maxUseId); 
		logger.debug("Max userid formed is {}", userIdStr);
		wdHistory.setRefId(userIdStr);
	
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_ENTRY);
		
			ps.setString(1, wdHistory.getRefId());
			ps.setInt(2, wdHistory.getStatus());
			ps.setInt(3, wdHistory.getReqType());
			ps.setLong(4, wdHistory.getAccountDetailsRecord());
			ps.setInt(5,  wdHistory.getAmount());
			ps.setString(6, wdHistory.getAccDetails());
			ps.setString(7, wdHistory.getClosingComments());
		
			int createResult = ps.executeUpdate();
			logger.debug(" createResult {}", createResult);
		} catch(SQLException ex) {
			logger.error("Error creating withdraw history entry", ex);
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
