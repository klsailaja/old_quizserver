package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.WithdrawReqState;
import com.ab.quiz.constants.WithdrawReqType;
import com.ab.quiz.pojo.WithdrawHistory;
import com.ab.quiz.pojo.WithdrawReqByPhone;

/*
CREATE TABLE WithdrawReqPhone(id bigint NOT NULL AUTO_INCREMENT, 
		phoneNumber varchar(15) NOT NULL,
		paymentType int, 
		userName varchar(10),
		date bigint NOT NULL, 
		userProfielId bigint, PRIMARY KEY (id)); 
*/

public class WithdrawReqPhoneDBHandler {
	// Create.. If successful create one entry into withdraw history db too
	
	private static final Logger logger = LogManager.getLogger(WithdrawReqPhoneDBHandler.class);

	private static String TABLE_NAME = "WithdrawReqPhone";
	
	private static String ID = "id";
	private static String DATE = "date";
	private static String PHONE_NUMBER = "phoneNumber";
	private static String PAYMENT_TYPE = "paymentType";
	private static String USERNAME = "userName";
	private static String USERPROFILEID = "userProfielId";
	
	private static final String CREATE_WITHDRAW_BY_PHONE = "INSERT INTO " + TABLE_NAME   
			+ "(" + PHONE_NUMBER + "," + PAYMENT_TYPE + "," + USERNAME + "," + USERPROFILEID + ","  
			+ DATE + ") VALUES" + "(?,?,?,?,?)";
	
	private static WithdrawReqPhoneDBHandler instance = null;
	
	private WithdrawReqPhoneDBHandler() {
	}
	
	public static WithdrawReqPhoneDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawReqPhoneDBHandler();
		}
		return instance;
	}
	
	public boolean createWithdrawReqByPhone(WithdrawReqByPhone byPhone) throws SQLException {
		
		logger.debug("In createWithdrawReqByPhone for {}", byPhone.getUserProfielId());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet idRes = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_BY_PHONE, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1,	byPhone.getPhNumber());
			ps.setInt(2, byPhone.getPaymentType());
			ps.setString(3, byPhone.getUserName());
			ps.setLong(4, byPhone.getUserProfielId());
			ps.setLong(5, byPhone.getDate());
			
			int createResult = ps.executeUpdate();
			logger.debug(" createResult {}", createResult);
			if (createResult > 0) {
				idRes = ps.getGeneratedKeys();
				long withdrawReqId = -1;
				if ((idRes != null) && (idRes.next())) {
					withdrawReqId = idRes.getLong(1);
				}
				if (withdrawReqId == -1) {
					logger.debug("Some problem ... Returning here *******************");
				}
				
				WithdrawHistory withDrawHistory = new WithdrawHistory();
				withDrawHistory.setAmount(byPhone.getAmt());
				withDrawHistory.setAccountDetailsRecord(withdrawReqId);
				withDrawHistory.setReqType(WithdrawReqType.BY_PHONE.getId());
				withDrawHistory.setStatus(WithdrawReqState.OPEN.getId());
				
				return WithdrawHistoryDBHandler.getInstance().createWithdrawHistoryEntry(withDrawHistory);
			}
		} catch(SQLException ex) {
			logger.error("Error creating withdraw req by phone ", ex);
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
}
