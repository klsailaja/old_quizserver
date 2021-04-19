package com.ab.quiz.handlers;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.MyTransactionDBHandler;
import com.ab.quiz.db.UserProfileDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.LoginData;
import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserProfile;

public class UserProfileHandler {
	
	private static final Logger logger = LogManager.getLogger(UserProfileHandler.class);
	private static UserProfileHandler instance = null;
	
	private static final String EMPTY_MESSAGE = "Value is empty for : %s";
    private static final String MAX_LENGTH = "Value exceeds %d for : %s";
	
	private UserProfileHandler() {
	}
	
	public static UserProfileHandler getInstance() {
		if (instance == null) {
			logger.debug("In UserProfileHandler getInstance() method instance created");
			instance = new UserProfileHandler();
		}
		return instance;
	}
	
	public UserProfile getUserProfileByMailId(String mailId) throws SQLException {
		return UserProfileDBHandler.getInstance().getProfileByMailid(mailId.trim());
	}
	
	public boolean updateUserProfileDetails(UserProfile userProfile, boolean fromForgotPasswd) 
			throws SQLException, NotAllowedException {
		return UserProfileDBHandler.getInstance().updateUserProfileDetails(userProfile, fromForgotPasswd);
	}
	
	public UserProfile login(LoginData loginData) throws SQLException,NotAllowedException {
		String mailId = loginData.getMailAddress().trim();
		String passwdHash = loginData.getPassword().trim();
		logger.info("Login method called with {} {}", mailId, passwdHash);
		UserProfile userProfile = getUserProfileByMailId(mailId);
		logger.info("userProfile is {}", userProfile);
		if (userProfile.getId() == 0) {
			throw new NotAllowedException("User does not exist. Please Register first");
		}
		if (passwdHash.equals(userProfile.getPasswordHash())) {
			logger.info("Authentication is success for {}", mailId);
			//UpdateUserLastLoggedTime run = new UpdateUserLastLoggedTime(userProfile.getId());
			//LazyScheduler.getInstance().submit(run);
			return userProfile;
		}
		logger.info("Authentication is failure for {}", mailId);
		throw new NotAllowedException("Password is Wrong. Use Forgot Password Option if required");
	}
	
	public UserProfile createUserProfile(UserProfile userProfile) throws NotAllowedException, SQLException {
		// Validate the fields
		// Get with mail and check if not exists
		// Check if referal id is correct
		// Set the other fields
		
		String str = userProfile.getEmailAddress().trim();
		userProfile.setEmailAddress(str);
		str = userProfile.getPasswordHash().trim();
		userProfile.setPasswordHash(str);
		str = userProfile.getName().trim();
		userProfile.setName(str);
		str = userProfile.getBossReferredId();
		if (str != null) {
			str = str.trim();
			userProfile.setBossReferredId(str);
		}
		
		
		UserProfileDBHandler dbInstance = UserProfileDBHandler.getInstance();
		String eMail = userProfile.getEmailAddress();
		
		UserProfile checkByMailId = dbInstance.getProfileByMailid(eMail);
		if (checkByMailId.getId() > 0) {
			// Already entry exists
			logger.info("Entry with {} found in db with the id {}", eMail, checkByMailId.getId());
			throw new NotAllowedException("Already mail id is registered. Use forgot password if required");
		}
		
		String bossReferalId = userProfile.getBossReferredId();
		if ((bossReferalId != null) && (bossReferalId.length() > 0)) {
			checkByMailId = dbInstance.getProfileByBossRefaralCode(bossReferalId);
			if (checkByMailId.getId() == 0) {
				throw new NotAllowedException("Invalid Referral code. No User exists with this code");
			}
			userProfile.setBossId(checkByMailId.getId());
			userProfile.setBossName(checkByMailId.getName());
		} else {
			userProfile.setBossId(0);
			userProfile.setBossName("");
		}
		long currentTime = System.currentTimeMillis();
		userProfile.setCreatedDate(currentTime);
		userProfile.setLastLoggedTime(currentTime);
		
		return UserProfileDBHandler.getInstance().createUserProfile(userProfile);
	}
	
	public ReferalDetails getUserReferals(String referalCode, int startRowNo) throws SQLException {
		UserProfileDBHandler dbInstance = UserProfileDBHandler.getInstance();
		return dbInstance.getUserReferals(referalCode, startRowNo);
	}
	
	public TransactionsHolder getTransactionsList(long userProfileId, int startRowNo, int accountType)
		throws NotAllowedException, SQLException {
		MyTransactionDBHandler transDbHandler = MyTransactionDBHandler.getInstance();
		return transDbHandler.getTransactions(userProfileId, startRowNo, accountType);
	}
			
	
	public static String fullValidate(String str, String componentName, boolean canBeEmpty, int maxLen) {
        if (str.length() == 0) {
            if (!canBeEmpty) {
                return String.format(EMPTY_MESSAGE, componentName);
            } else {
                return null;
            }
        }
        if ((maxLen != -1) && (str.length() > maxLen)) {
            return String.format(MAX_LENGTH, maxLen, componentName);
        }
        return null;
    }
	
	public static void main(String[] args) throws NotAllowedException, SQLException {
		
		UserProfileHandler userProfileHandler = UserProfileHandler.getInstance();
		
		UserProfile userProfile = new UserProfile();
		userProfile.setEmailAddress("ggraj.pec@gmail.com");
		
		userProfileHandler.updateUserProfileDetails(userProfile, true);
	}
}
