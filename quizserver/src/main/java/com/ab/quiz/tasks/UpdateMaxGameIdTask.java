package com.ab.quiz.tasks;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.LastGameIdDBHandler;

public class UpdateMaxGameIdTask implements Runnable {
	
	private long maxGameId;
	private static final Logger logger = LogManager.getLogger(UpdateMaxGameIdTask.class);
	
	public UpdateMaxGameIdTask(long maxGameId) {
		this.maxGameId = maxGameId;
	}
	
	public void run() {
		try {
			logger.debug("Max game id passed is {}", maxGameId);
			if (maxGameId == -1) {
				logger.debug("Max game id is -1. Returning");
				return;
			}
			boolean result = LastGameIdDBHandler.getInstance().updateLastGameId(maxGameId);
			logger.debug("The result after updating the max game id is {}", result);		
		} catch (SQLException e) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while updating max game id " + maxGameId, e);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		} 
	}
}
