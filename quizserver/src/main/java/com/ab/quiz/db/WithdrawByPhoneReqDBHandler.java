package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.WithdrawReqByPhone;

/*
CREATE TABLE WithdrawByPhoneReq(id bigint NOT NULL AUTO_INCREMENT, 
		phoneNumber varchar(15) NOT NULL,
		paymentMethod int, 
		userName varchar(20), PRIMARY KEY (id));
*/

public class WithdrawByPhoneReqDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawByPhoneReqDBHandler.class);
	
	private static String TABLE_NAME = "WithdrawByPhoneReq";
	private static String ID = "id";
	private static String PHONE_NUMBER = "phoneNumber";
	private static String PH_PAYMENT_METHOD = "paymentMethod";
	private static String USERNAME = "userName";
	
	private static final String CREATE_WITHDRAW_BY_PHONE = "INSERT INTO " + TABLE_NAME   
			+ "(" + PHONE_NUMBER + "," + PH_PAYMENT_METHOD + "," + USERNAME    
			+ ") VALUES" + "(?,?,?)";
	
	private static WithdrawByPhoneReqDBHandler instance = null;
	
	private WithdrawByPhoneReqDBHandler() {
	}
	
	public static WithdrawByPhoneReqDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawByPhoneReqDBHandler();
		}
		return instance;
	}
	
	/*
	 * This creates a request in db. Returns the newly addded record id if success
	 * if fail, throws an exception
	 */
	
	public long createReqByPhone(WithdrawReqByPhone byPhReq) throws SQLException {
		
		logger.debug("In createReqByPhone for {}", byPhReq.getPhNumber());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_BY_PHONE, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, byPhReq.getPhNumber());
			ps.setInt(2, byPhReq.getPaymentMethod());
			ps.setString(3, byPhReq.getAccountHolderName());
			
			int createResult = ps.executeUpdate();
			logger.debug("In createReqByPhone create op result : {}", createResult);
			
			if (createResult > 0) {
				rs = ps.getGeneratedKeys();
				long withdrawReqId = -1;
				if ((rs != null) && (rs.next())) {
					withdrawReqId = rs.getLong(1);
				}
				if (withdrawReqId == -1) {
					throw new SQLException("createReqByPhone new rec id is -1");
				}
				return withdrawReqId;
			}
			return -1;
		}
		catch(SQLException ex) {
			logger.error("Error creating withdraw req by phone ", ex);
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
	}
}
