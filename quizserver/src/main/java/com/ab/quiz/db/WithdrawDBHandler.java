package com.ab.quiz.db;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawReqByPhone;

/*
CREATE TABLE WithdrawRequests(id bigint NOT NULL AUTO_INCREMENT, 
		refId varchar(10) NOT NULL,
		userProfileId bigint NOT NULL,
		fromAccType int NOT NULL,
		status int NOT NULL,
		reqType int NOT NULL,
		accountDetailsId bigint NOT NULL,
		amount int NOT NULL,
		openedTime bigint NOT NULL,
		closedTime bigint NULL,
		receiptId bigint NULL,
		closeCmts varchar(100), PRIMARY KEY (id); 
*/

public class WithdrawDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawDBHandler.class);
	
	private static String TABLE_NAME = "WithdrawRequests"; 
	
	private static String ID = "id";
	private static String REFID = "refId";
	private static String USER_PROFILE_ID = "userProfileId";
	private static String FROM_APP_BANK_ACC_NAME = "fromAccType";
	private static String STATUS = "status";
	private static String REQUEST_TYPE = "reqType";
	private static String ACDETAILS_ID = "accountDetailsId";
	private static String AMOUNT = "amount";
	private static String REQUEST_OPENED_TIME = "openedTime";
	private static String REQUEST_CLOSED_TIME = "closedTime";
	private static String TRANSACTION_RECEIPT_ID = "receiptId";
	private static String CLOSED_CMTS = "closeCmts";
	
	private static WithdrawDBHandler instance = null;
	
	private static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
				+ "abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom secureRnd = new SecureRandom();
	private static final int REFERENCE_MAX_LEN = 10; 


	private static final String CREATE_WITHDRAW_ENTRY = "INSERT INTO " + TABLE_NAME
			+ "(" + REFID + "," + USER_PROFILE_ID + "," + FROM_APP_BANK_ACC_NAME + ","
			+ STATUS + "," + REQUEST_TYPE + "," + ACDETAILS_ID + ","  
			+ AMOUNT + "," + REQUEST_OPENED_TIME + "," + REQUEST_CLOSED_TIME + ","
			+ TRANSACTION_RECEIPT_ID + "," + CLOSED_CMTS + ") VALUES"
			+ "(?,?,?,?,?,?,?,?,?,?,?)";
	private static final String MAX_WITHDRAW_REQ_ID = "SELECT MAX(ID) FROM " + TABLE_NAME;
	
	private WithdrawDBHandler() {
	}
	
	public static WithdrawDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawDBHandler();
		}
		return instance;
	}
	
	public long getMaxWithdrawReqId() throws SQLException {
		
		logger.debug("In getMaxWithdrawReqId() method");
		
		long maxId = -1;
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet idRs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(MAX_WITHDRAW_REQ_ID);
			
			idRs = ps.executeQuery();
			if (idRs != null) {
				if (idRs.next()) {
					maxId = idRs.getLong("MAX(ID)");
				}
			}
		} catch (SQLException ex) {
			logger.error("SQL Exception in getMaxWithdrawReqId()", ex);
			throw ex;
		} finally {
			if (idRs != null) {
				idRs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("Returning from getMaxWithdrawReqId() {}", maxId);
		return maxId;
	}
	
	public boolean createWithDrawReq(WDUserInput wdUserInput, 
			WithdrawReqByPhone phoneReq) throws SQLException {
		
		WithdrawByPhoneReqDBHandler handler = WithdrawByPhoneReqDBHandler.getInstance(); 
		long phoneReqId = handler.createReqByPhone(phoneReq);
		
		if (phoneReqId < 0) {
			return false;
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_ENTRY);
			
			long maxReqId = getMaxWithdrawReqId() + 1;
			int idStrLen = String.valueOf(maxReqId).length();
			int remainingLen = REFERENCE_MAX_LEN - idStrLen;
			
			String refId = getReferenceNumber(remainingLen, maxReqId);
			ps.setString(1, refId);
			ps.setLong(2, wdUserInput.getUserProfileId());
			ps.setInt(3, wdUserInput.getFromAccType());
			ps.setInt(4, 1);
			ps.setInt(5, 1);
			ps.setLong(6, phoneReqId);
			ps.setInt(7, wdUserInput.getAmount());
			
			long currentTime = System.currentTimeMillis();
			ps.setLong(8, currentTime);
			ps.setNull(9, Types.NULL);
			ps.setNull(10, Types.NULL);
			ps.setNull(11, Types.NULL);
			
			int result = ps.executeUpdate();
			logger.debug("In createWithDrawReq create op result : {}", result);
			return (result >= 1);
		} catch (SQLException ex) {
			logger.error("Error creating createWithDrawReq ", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	private String getReferenceNumber(int maxLen, long maxId) throws SQLException {
		StringBuilder sb = new StringBuilder(maxLen); 
		for (int i = 0; i < maxLen; i++) 
			sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
		
		sb.append(maxId);
		return sb.toString();
	}
}
