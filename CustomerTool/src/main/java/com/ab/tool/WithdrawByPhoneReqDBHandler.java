package com.ab.tool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
CREATE TABLE WITHDRAWBYPHONEREQ(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		PHONENUMBER VARCHAR(15) NOT NULL,
		PAYMENTMETHOD INT, 
		USERNAME VARCHAR(20), PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class WithdrawByPhoneReqDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawByPhoneReqDBHandler.class);
	
	private static String TABLE_NAME = "WITHDRAWBYPHONEREQ";
	
	private static String ID = "ID";
	private static String PHONE_NUMBER = "PHONENUMBER";
	private static String PH_PAYMENT_METHOD = "PAYMENTMETHOD";
	private static String USERNAME = "USERNAME";
	
	private static final String CREATE_WITHDRAW_BY_PHONE = "INSERT INTO " + TABLE_NAME   
			+ "(" + PHONE_NUMBER + "," + PH_PAYMENT_METHOD + "," + USERNAME    
			+ ") VALUES" + "(?,?,?)";
	private static final String GET_WITHDRAW_BY_PHONE_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = ?";
	
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
	
	public WithdrawReqByPhone getWithdrawReqByPhoneById(long id) throws SQLException {
		
		logger.debug("getWithdrawReqByPhoneById called with {}", id);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(GET_WITHDRAW_BY_PHONE_BY_ID);
			
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					
					WithdrawReqByPhone byPhoneEntry = new WithdrawReqByPhone();
					
					byPhoneEntry.setId(id);
					byPhoneEntry.setPhNumber(rs.getString(PHONE_NUMBER));
					byPhoneEntry.setPaymentMethod(rs.getInt(PH_PAYMENT_METHOD));
					byPhoneEntry.setAccountHolderName(rs.getString(USERNAME));
					
					return byPhoneEntry;
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException while getting the phone withdraw request details by id ", ex);
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
		return null;
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
			long withdrawReqId = -1;
			
			if (createResult > 0) {
				rs = ps.getGeneratedKeys();
				if ((rs != null) && (rs.next())) {
					withdrawReqId = rs.getLong(1);
				}
			}
			if (withdrawReqId == -1) {
				throw new SQLException("createReqByPhone new rec id is -1");
			}
			return withdrawReqId;
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
