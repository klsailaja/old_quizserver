package com.ab.quiz.tasks;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.UserProfileDBHandler;

public class UpdateUserLastLoggedTime implements Runnable {
	
	private long userProfileId;
	private final Logger logger = LogManager.getLogger(UpdateUserLastLoggedTime.class);
	
	public UpdateUserLastLoggedTime(long id) {
		this.userProfileId = id;
	}
	
	@Override
	public void run() {
		logger.info("Updating the last logged in time for {}", userProfileId);
		try {
			boolean result = UserProfileDBHandler.getInstance().updateUserProfileLoggedTime(userProfileId);
			logger.info("Result is {}", result);
		} catch(SQLException ex) {
			logger.error("Exception while executing updateUserProfileLoggedTime", ex);
		}
		
	}
}
