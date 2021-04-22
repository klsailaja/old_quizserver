package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.WithdrawReqByBank;

/*
CREATE TABLE WITHDRAWBYBANKREQ(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		ACCOUNTNUMBER VARCHAR(30) NOT NULL,
		IFSCCODE VARCHAR(10) NOT NULL,
		BANKNAME VARCHAR(20) NOT NULL,
		USERNAME VARCHAR(20), PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class WithdrawByBankDBHandler {
	private static final Logger logger = LogManager.getLogger(WithdrawByBankDBHandler.class);
	
	private static String TABLE_NAME = "WITHDRAWBYBANKREQ";
	
	private static String ID = "ID";
	private static String ACCOUNT_NUMBER = "ACCOUNTNUMBER";
	private static String IFSC_CODE = "IFSCCODE";
	private static String BANK_NAME = "BANKNAME";
	private static String USERNAME = "USERNAME";
	
	private static final String CREATE_WITHDRAW_BY_BANK = "INSERT INTO " + TABLE_NAME   
			+ "(" + ACCOUNT_NUMBER + "," + IFSC_CODE + "," + BANK_NAME + "," + USERNAME    
			+ ") VALUES" + "(?,?,?,?)";
	private static final String GET_WITHDRAW_BY_BANK_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = ?";
	
	private static WithdrawByBankDBHandler instance = null;
	
	private WithdrawByBankDBHandler() {
	}
	
	public static WithdrawByBankDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawByBankDBHandler();
		}
		return instance;
	}
	
	public WithdrawReqByBank getWithdrawReqByBankById(long id) throws SQLException {
		
		logger.debug("getWithdrawReqByBankById called with {}", id);
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(GET_WITHDRAW_BY_BANK_BY_ID);
			
			ps.setLong(1, id);
			
			rs = ps.executeQuery();
			
			if (rs != null) {
				if (rs.next()) {
					
					WithdrawReqByBank byBankEntry = new WithdrawReqByBank();
					
					byBankEntry.setId(id);
					byBankEntry.setAccountNumber(rs.getString(ACCOUNT_NUMBER));
					byBankEntry.setIfscCode(rs.getString(IFSC_CODE));
					byBankEntry.setBankName(rs.getString(BANK_NAME));
					byBankEntry.setUserName(rs.getString(USERNAME));
					
					return byBankEntry;
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException while getting the bank withdraw request details by id ", ex);
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
	
	public long createReqByBank(WithdrawReqByBank byBankReq) throws SQLException {
		
		logger.info("In createReqByBank for {}", byBankReq.getUserName());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_BY_BANK, Statement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, byBankReq.getAccountNumber());
			ps.setString(2, byBankReq.getIfscCode());
			ps.setString(3, byBankReq.getBankName());
			ps.setString(4, byBankReq.getUserName());
			
			int createResult = ps.executeUpdate();
			logger.info("In createReqByBank create op result : {}", createResult);
			long withdrawReqId = -1;
			if (createResult > 0) {
				rs = ps.getGeneratedKeys();
				if ((rs != null) && (rs.next())) {
					withdrawReqId = rs.getLong(1);
				}
			}
			if (withdrawReqId == -1) {
				throw new SQLException("createReqByBank new rec id is -1");
			}
			return withdrawReqId;
		}
		catch(SQLException ex) {
			logger.error("Error creating withdraw req by bank ", ex);
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
