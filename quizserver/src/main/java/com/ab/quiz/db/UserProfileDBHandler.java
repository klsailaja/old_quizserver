package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.UserProfile;
import com.ab.quiz.pojo.UserReferal;

/*
CREATE TABLE UserProfile(id bigint NOT NULL AUTO_INCREMENT, 
		name varchar(20) NOT NULL,
		passwd varchar(70) NOT NULL, 
		mailId varchar(70) NOT NULL, 
		myreferalId varchar(10), 
		referredId varchar(10), 
		createdDate bigint, 
		lastLoggedDate bigint, PRIMARY KEY (id));
*/

public class UserProfileDBHandler {
	
	private static final Logger logger = LogManager.getLogger(UserProfileDBHandler.class);

	private static String ID = "id";
	private static String PASSWD = "passwd";
	private static String NAME = "name";
	private static String MAIL_ID = "mailId";
	private static String MYREFERAL_ID = "myreferalId";
	private static String REFERED_ID = "referredId";
	private static String CREATEDDATE = "createdDate";
	private static String LASTLOGGEDDATE = "lastLoggedDate";
	
	private static UserProfileDBHandler instance = null;
	
	private static final String GET_USER_PROFILE_BY_MAIL_ID = "SELECT * FROM UserProfile WHERE " 
			+ MAIL_ID + " = ?";
	private static final String GET_USER_PROFILE_BY_ID = "SELECT * FROM UserProfile WHERE " 
			+ ID + " = ?";
	private static final String GET_USER_PROFILE_BY_REFERAL_CODE = "SELECT * FROM UserProfile WHERE " 
			+ MYREFERAL_ID + " = ?";
	private static final String GET_MY_REFERALS = "SELECT * FROM UserProfile WHERE " 
			+ REFERED_ID + " = ? ORDER BY " + ID + " LIMIT ?, 10";
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM UserProfile WHERE "
			+ REFERED_ID + " = ?";
	  
	
	private static final String CREATE_USER_PROFILE = "INSERT INTO UserProfile " 
			+ "(" + NAME + "," + MAIL_ID + "," + PASSWD + "," + MYREFERAL_ID + "," + REFERED_ID + ","
			+ CREATEDDATE + "," + LASTLOGGEDDATE + ") VALUES"   
			+ "(?,?,?,?,?,?,?)";
	private static final String MAX_USER_PROFILE_ID = "SELECT MAX(ID) FROM UserProfile";
	private static final String UPDATE_NAME_BY_ID = "UPDATE UserProfile SET " + NAME + "= ? WHERE " + ID + " = ?";
	private static final String UPDATE_TIME_BY_ID = "UPDATE UserProfile SET " + LASTLOGGEDDATE + "= ? WHERE " + ID + " = ?";
			
		
	private UserProfileDBHandler() {
	}
	
	public static UserProfileDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new UserProfileDBHandler();
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
	
	public UserProfile createUserProfile(UserProfile userProfile) throws SQLException {
		
		logger.debug("In createUserProfile for {}", userProfile.getEmailAddress());
		long maxUseId = getMaxUserId() + 1;
		int idStrLen = String.valueOf(maxUseId).length();
		int remainingLen = 8 - idStrLen;
		String userName = userProfile.getName();
		if (userName.length() >= remainingLen) {
			userName = userName.substring(0, remainingLen);
		}
		String userIdStr = userName + String.valueOf(maxUseId); 
		userProfile.setMyReferalId(userIdStr);
		logger.debug("Max referal id formed is {}", userIdStr);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet idRes = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_USER_PROFILE, Statement.RETURN_GENERATED_KEYS);
		
			ps.setString(1, userProfile.getName());
			ps.setString(2, userProfile.getEmailAddress());
			ps.setString(3, userProfile.getPasswordHash());
			ps.setString(4, userProfile.getMyReferalId());
			ps.setString(5, userProfile.getBossReferredId());
			ps.setLong(6,  userProfile.getCreatedDate());
			ps.setLong(7, userProfile.getLastLoggedDate());
		
			int createResult = ps.executeUpdate();
			logger.debug(" createResult {}", createResult);
			idRes = ps.getGeneratedKeys();
			if (idRes.next()) {
		        long userProfileId = idRes.getLong(1);
		        userProfile.setId(userProfileId);
		    }
		} catch(SQLException ex) {
			logger.error("Error creating user profile", ex);
			throw ex;
		} finally {
			if (idRes != null) {
				idRes.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return userProfile;
	}
	
	public UserProfile getProfile(String sql, String strVal, long longValue) throws SQLException {
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(sql);
		if (strVal != null) {
			ps.setString(1, strVal);
		}
		if (longValue != -1) {
			ps.setLong(1, longValue);
		}
		
		UserProfile userProfile = new UserProfile();
		
		try {
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					userProfile.setId(rs.getLong(ID));
					userProfile.setEmailAddress(rs.getString(MAIL_ID));
					userProfile.setPasswordHash(rs.getString(PASSWD));
					userProfile.setName(rs.getString(NAME));
					userProfile.setMyReferalId(rs.getString(MYREFERAL_ID));
					userProfile.setBossReferredId(rs.getString(REFERED_ID));
					userProfile.setCreatedDate(rs.getLong(CREATEDDATE));
					userProfile.setLastLoggedTime(rs.getLong(LASTLOGGEDDATE));
				}
				rs.close();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("The userProfile is {}", userProfile);
		return userProfile;
	}
	
	public UserProfile getProfileByBossRefaralCode(String bossReferalCode) throws SQLException {
		
		logger.debug("In bossReferalCode {}", bossReferalCode);
		bossReferalCode = bossReferalCode.trim();
		String sql = GET_USER_PROFILE_BY_REFERAL_CODE;
		
		try {
			UserProfile userProfile = getProfile(sql, bossReferalCode, -1);
			return userProfile;
		} catch (SQLException ex) {
			logger.error("SQL Exception in getProfileByReralCode", ex);
			throw ex;
		}
	}
	
	public ReferalDetails getUserReferals(String myReferalCode, int startRowNumber) throws SQLException {
		
		String sql = GET_MY_REFERALS;
		String totalSql = GET_TOTAL_COUNT;
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement totalPs = dbConn.prepareStatement(totalSql);
		PreparedStatement ps = dbConn.prepareStatement(sql);
		
		totalPs.setString(1, myReferalCode);
		
		ps.setString(1, myReferalCode);
		ps.setInt(2, startRowNumber);
		
		ReferalDetails referalDetails = new ReferalDetails();
		List<UserReferal> myReferals = new ArrayList<>();
		
		try {
			ResultSet totalRs = totalPs.executeQuery();
			if (totalRs != null) {
				if (totalRs.next()) {
					
					int total = totalRs.getInt("COUNT(*)");
					
					int lowerRange = startRowNumber + 1;
					int higherRange = startRowNumber + 10;
					
					if (higherRange < total) {
						referalDetails.setNextEnabled(true);
					} else {
						referalDetails.setNextEnabled(false);
					}
					if ((lowerRange - 10) > 0) {
						referalDetails.setPrevEnabled(true);
					} else {
						referalDetails.setPrevEnabled(false);
					}
					
				}
				totalRs.close();
			}
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					UserReferal userReferal = new UserReferal();
					
					userReferal.setSno(++startRowNumber); 
					userReferal.setUserName(rs.getString(NAME));
					userReferal.setLastLoggedDate(rs.getLong(LASTLOGGEDDATE));
					
					
					myReferals.add(userReferal);
				}
				rs.close();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		referalDetails.setReferalList(myReferals);
		return referalDetails;
	}
	
	public UserProfile getProfileById(long profileId) throws SQLException {
		
		logger.debug("In getProfileById {}", profileId);
		String sql = GET_USER_PROFILE_BY_ID;
		
		try {
			UserProfile userProfile = getProfile(sql, null, profileId);
			return userProfile;
		} catch (SQLException ex) {
			logger.error("SQL Exception in getProfileById", ex);
			throw ex;
		}
	}
	
	public UserProfile getProfileByMailid(String mailId) throws SQLException {
		String sql = GET_USER_PROFILE_BY_MAIL_ID;
		
		try {
			UserProfile userProfile = getProfile(sql, mailId, -1);
			return userProfile;
		} catch (SQLException ex) {
			logger.error("SQL Exception in getProfileByPhNumber", ex);
			throw ex;
		}
	}
	
	public boolean updateUserProfileName(String name, long id) throws SQLException {
		logger.debug("This is in updateUserProfileName {} {}", name, id);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(UPDATE_NAME_BY_ID);
		
		ps.setString(1, name);
		ps.setLong(2,  id);
		
		try {
			int resultCount = ps.executeUpdate();
			logger.debug("The updated row count {}", resultCount);
		}
		catch(SQLException ex) {
			logger.error("Error updating in updateUserProfileName", ex);
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
	
	public boolean updateUserProfileLoggedTime(long id) throws SQLException {
		logger.debug("This is in updateUserProfileLoggedTime {}", id);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(UPDATE_TIME_BY_ID);
		ps.setLong(1, Calendar.getInstance().getTime().getTime());
		ps.setLong(2, id);
		try {
			int resultCount = ps.executeUpdate();
			logger.debug("The updated row count {}", resultCount);
		}
		catch(SQLException ex) {
			logger.error("Error updating in updateUserProfileLoggedTime", ex);
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
	
	public static void main(String[] args) throws SQLException {
		
		UserProfileDBHandler dbHandler = UserProfileDBHandler.getInstance();
		
		// System Users from 1 - 20
		for (int index = 1; index <= 20; index ++) {
			UserProfile userProfile = new UserProfile();
			userProfile.setEmailAddress("systemuser" + index + "@gmail.com");
			userProfile.setName("Systemuser" + index);
			userProfile.setPasswordHash("5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5");
			userProfile.setBossReferredId("Rajasekh10");
			userProfile.setCreatedDate(1609861020944L);
			userProfile.setLastLoggedTime(1609861020944L);
			
			dbHandler.createUserProfile(userProfile);
		}
		
		UserProfile userProfile = new UserProfile();
		userProfile.setEmailAddress("ggraj.pec@gmail.com");
		userProfile.setName("Rajasekhar");
		userProfile.setPasswordHash("5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5");
		userProfile.setBossReferredId("NoOne");
		userProfile.setCreatedDate(1609861020944L);
		userProfile.setLastLoggedTime(1609861020944L);
		dbHandler.createUserProfile(userProfile);
		
		for (int index = 1; index <= 50; index ++) {
			userProfile = new UserProfile();
			userProfile.setEmailAddress("testuser" + index + ".com");
			userProfile.setName("Testuser" + index);
			userProfile.setPasswordHash("5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5");
			userProfile.setBossReferredId("Rajase21");
			userProfile.setCreatedDate(1609861020944L);
			userProfile.setLastLoggedTime(1609861020944L);
			
			dbHandler.createUserProfile(userProfile);
		}
	}
}
