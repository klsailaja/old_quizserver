package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.KYCEntry;

/*
 * CREATE TABLE KYCTABLE(USERID BIGINT UNSIGNED NOT NULL,
  		AFP BIGINT NOT NULL DEFAULT -1,
  		ABP BIGINT NOT NULL DEFAULT -1,
  		PP BIGINT NOT NULL DEFAULT -1,
  		UPDATEDTIME BIGINT NOT NULL,
		STATUS VARCHAR(100) NOT NULL, PRIMARY KEY (USERID)) ENGINE = INNODB;
	
 */

public class KYCDBHandler {
	private static String TABLE_NAME = "KYCTABLE";
	
	private static String USER_PROFILE_ID = "USERID";
	private static String AADHAR_FRONT_PAGE = "AFP";
	private static String AADHAR_BACK_PAGE = "ABP";
	private static String PAN_PAGE = "PP";
	private static String UPDATED_TIME = "UPDATEDTIME";
	private static String STATUS = "STATUS";
	
	private static final Logger logger = LogManager.getLogger(KYCDBHandler.class);
	private static KYCDBHandler instance = null;
	
	private static final String CREATE_KYC_ENTRY = "INSERT INTO " + TABLE_NAME
			+ "(" + USER_PROFILE_ID + "," + AADHAR_FRONT_PAGE + "," 
			+ AADHAR_BACK_PAGE + "," + PAN_PAGE + ","    
			+ UPDATED_TIME + ","
			+ STATUS + ") VALUES"
			+ "(?,?,?,?,?,?)";
	
	private static final String GET_KYC_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " 
			+ USER_PROFILE_ID + " = ?";
	
	private static final String UPDATE_KYC_1_BY_ID = "UPDATE " + TABLE_NAME + " SET " + AADHAR_FRONT_PAGE + "= ?,SET "
			+ UPDATED_TIME + "= ?" + " WHERE " + USER_PROFILE_ID + " = ?"; 
	private static final String UPDATE_KYC_2_BY_ID = "UPDATE " + TABLE_NAME + " SET " + AADHAR_BACK_PAGE + "= ?,SET " 
			+ UPDATED_TIME + "= ?" + " WHERE " + USER_PROFILE_ID + " = ?";
	private static final String UPDATE_KYC_3_BY_ID = "UPDATE " + TABLE_NAME + " SET " + PAN_PAGE + "= ?,SET " 
			+ UPDATED_TIME + "= ?" + " WHERE " + USER_PROFILE_ID + " = ?";

	
	private KYCDBHandler() {
	}
	
	public static KYCDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new KYCDBHandler();
		}
		return instance;
	}
	
	
	public boolean createKYCEntry(KYCEntry kycEntry) throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_KYC_ENTRY);
			
			ps.setLong(1, kycEntry.getUserId());
			ps.setLong(2, kycEntry.getAfpId());
			ps.setLong(3, kycEntry.getAbpId());
			ps.setLong(4, kycEntry.getPpId());
			ps.setString(5, kycEntry.getStatus());
			
			int result = ps.executeUpdate();
			logger.info("In createKYCEntry create op result : {}", result);
			
			if (result > 0) {
				return true;
			}
			return false;
		} catch (SQLException ex) {
			logger.error("Error creating createKYCEntry ", ex);
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
	
	public boolean deleteAllTraces(long afpId, long abpId, long ppId) throws SQLException {
		boolean delResult = true;
		if (afpId > -1) {
			boolean result = PictureDBHandler.getInstance().deletePictureEntry(afpId);
			delResult = delResult & result;
		}
		if (abpId > -1) {
			boolean result = PictureDBHandler.getInstance().deletePictureEntry(abpId);
			delResult = delResult & result;
		}
		if (ppId > -1) {
			boolean result = PictureDBHandler.getInstance().deletePictureEntry(ppId);
			delResult = delResult & result;
		}
		return delResult;
	}
	
	public KYCEntry getKYCEntryById(long userId) throws SQLException {
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_KYC_BY_ID);
		ps.setLong(1, userId);
		ResultSet rs = null;
		
		KYCEntry kycEntry = new KYCEntry();
		kycEntry.setAfpId(-1);
		kycEntry.setAbpId(-1);
		kycEntry.setPpId(-1);

		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					kycEntry.setUserId(rs.getLong(USER_PROFILE_ID));
					kycEntry.setAfpId(rs.getLong(AADHAR_FRONT_PAGE));
					kycEntry.setAbpId(rs.getLong(AADHAR_BACK_PAGE));
					kycEntry.setPpId(rs.getLong(PAN_PAGE));
					kycEntry.setStatus(rs.getString(STATUS));
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getKYCEntryById()", ex);
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
		return kycEntry;
	}
	
	private String getUpdateQryType(int qryType) {
		if (qryType == 1) {
			return UPDATE_KYC_1_BY_ID;
		}
		if (qryType == 2) {
			return UPDATE_KYC_2_BY_ID;
		}
		if (qryType == 3) {
			return UPDATE_KYC_3_BY_ID;
		}
		return null;
	}
	
	public boolean updateKYCEntry(long userId, long pictureId, int qryType) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(getUpdateQryType(qryType));
		
		ps.setLong(1, pictureId);
		ps.setLong(2, userId);
		
		int operResult = 0;
		
		try {
			int resultCount = ps.executeUpdate();
			logger.info("In updateKYCEntry {}", resultCount);
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while updateKYCEntry for {} : {}", userId, pictureId);
			logger.error("SQLException in ", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return (operResult > 0);
	}
}
