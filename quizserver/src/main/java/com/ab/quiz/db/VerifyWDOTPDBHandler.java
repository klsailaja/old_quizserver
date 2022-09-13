package com.ab.quiz.db;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.pojo.OTPDetails;

/*
 * CREATE TABLE VERIFYWDOTP(MAILID VARCHAR(320) NOT NULL, 
 * OTPCODE VARCHAR(80) NULL, 
 * CREATEDDATE BIGINT, PRIMARY KEY (MAILID)) ENGINE = INNODB;
 */

public class VerifyWDOTPDBHandler {
	
	private static final Logger logger = LogManager.getLogger(VerifyWDOTPDBHandler.class);
	
	private static String TABLE_NAME = "VERIFYWDOTP";
	private static String MAILID = "MAILID";
	private static String OTP = "OTPCODE";
	private static String CREATEDDATE = "CREATEDDATE";
	
	private static int DELETE_USELESS_ENTRIES_DURATION_IN_HRS = 3;
	private static final long DELETE_USELESS_ENTRIES_DURATION_IN_MILLIS = DELETE_USELESS_ENTRIES_DURATION_IN_HRS * 60 * 1000;
	
	private static VerifyWDOTPDBHandler instance = null;
	
	private static final String CREATE_OTP_PROFILE = "INSERT INTO " + TABLE_NAME   
			+ "(" + MAILID + "," + OTP + "," + CREATEDDATE + ") VALUES"   
			+ "(?,?,?)";
	private static final String REMOVE_OTP_PRIFILE = "DELETE FROM " + TABLE_NAME 
			+ " WHERE " + MAILID + " = ? ";
	
	private static final String SELECT_OTP_PRIFILE = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + MAILID + " = ? ";
	
	private static final String UPDATE_OTP = "UPDATE " + TABLE_NAME + " SET " + OTP + " = ? WHERE " + MAILID + " = ? ";
	
	private static final String DELETE_USELESS_ENTRIES = "DELETE FROM " + TABLE_NAME + " WHERE (?" 
			+ "- CREATEDDATE) <= " + DELETE_USELESS_ENTRIES_DURATION_IN_MILLIS;
	
	private static final String SOURCE = "0123456789"; //ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz 
	
	private static final SecureRandom secureRnd = new SecureRandom();
	
			
	private VerifyWDOTPDBHandler() {
	}
	
	public static VerifyWDOTPDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In VerifyWDOTPDBHandler getInstance() method instance created");
			instance = new VerifyWDOTPDBHandler();
		}
		return instance;
	}
	
	public boolean createWDOTPForVerify(OTPDetails otpDetails) throws SQLException {
		
		logger.debug("In createWDOTPForVerify with {}", otpDetails.getMailId());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_OTP_PROFILE);
			
			ps.setString(1, otpDetails.getMailId());
			ps.setString(2, otpDetails.getOtp_hash());
			ps.setLong(3, System.currentTimeMillis());
			
			int result = ps.executeUpdate();
			logger.info("createWDOTPForVerify with mailid {} result is {}", otpDetails.getMailId(), (result > 0));
			return (result > 0);
			
		} catch(SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in createWDOTPForVerify for mailid {} ", otpDetails.getMailId());
			logger.error("The Exception is", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public OTPDetails getWDOTPDetailsByMailId(String mailId) throws SQLException {
		
		mailId = mailId.trim();
		logger.info("In getWDOTPDetailsByMailId with {}", mailId);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(SELECT_OTP_PRIFILE);
		ps.setString(1, mailId);
		
		ResultSet rs = null;
		
		OTPDetails otpDetails = new OTPDetails();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					otpDetails.setMailId(mailId);
					otpDetails.setOtp_hash(rs.getString(OTP));
				}
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while getting the getWDOTPDetailsByMailId for mailId {}", mailId);
			logger.error("SQLException in getWDOTPDetailsByMailId()", ex);
			logger.error("******************************");
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
		return otpDetails;
	}
	
	public int deleteWDOTPRecord(String mailId) throws SQLException {
		
		logger.info("In deleteWDOTPRecord method: " + mailId);
		mailId = mailId.trim();
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(REMOVE_OTP_PRIFILE);
			
			ps.setString(1, mailId);
			
			int result = ps.executeUpdate();
			logger.debug("In deleteWDOTPRecord operation result : {}", result);
			return result;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in deleteWDOTPRecord ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public int deleteUselessEntries() throws SQLException {
		
		logger.info("In deleteUselessEntries in WD OTP Table method: ");
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(DELETE_USELESS_ENTRIES);
			
			ps.setLong(1, System.currentTimeMillis());
			
			int result = ps.executeUpdate();
			logger.debug("In deleteUselessEntries WD operation result : {}", result);
			return result;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in deleteUselessEntries WD ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public int updateRecordWithWDOTP(String mailId, String otpHash) throws SQLException {
		
		mailId = mailId.trim();
		otpHash = otpHash.trim();
		logger.info("In updateRecordWithWDOTP method" + mailId);
		
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(UPDATE_OTP);
			
			ps.setString(1, otpHash);
			ps.setString(2, mailId);
			
			int result = ps.executeUpdate();
			logger.debug("In updateRecordWithWDOTP operation result : {}", result);
			return result;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in updateRecordWithWDOTP ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public String getRandomPasswd(int maxLen) {
		StringBuilder sb = new StringBuilder(maxLen); 
		for (int i = 0; i < maxLen; i++) 
			sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
		return sb.toString();
	}
	
	public static String getPasswordHash(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        if (md == null) {
            return null;
        }
        md.update(password.getBytes());
        byte [] byteData = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte byteDatum : byteData) {
            sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}

