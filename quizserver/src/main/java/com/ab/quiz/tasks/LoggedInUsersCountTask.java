package com.ab.quiz.tasks;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.UserProfileDBHandler;
import com.ab.quiz.helper.LazyScheduler;

public class LoggedInUsersCountTask implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(LoggedInUsersCountTask.class);
	private static LoggedInUsersCountTask instance = null;
	private long loggedUsersCount;
	
	private LoggedInUsersCountTask() {
	}
	
	public static LoggedInUsersCountTask getInstance() {
		if (instance == null) {
			logger.debug("In LoggedInUsersCountTask getInstance() method instance created");
			instance = new LoggedInUsersCountTask();
			LazyScheduler.getInstance().submitRepeatedTask(instance, 0, 
					QuizConstants.LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS);
		}
		return instance;
	}
	
	public void run() {
		try {
			loggedUsersCount = UserProfileDBHandler.getInstance().getLoggedInUsersCount();
		} catch (Exception ex) {
			logger.error("Exception in LoggedInUsersCountTask", ex);
		}
	}
	
	public long getUsersCount() {
		if (loggedUsersCount < 50) {
			int userAnswerMin = 100;
			int userAnswerMax = 150;
			int userAnswerFinal = userAnswerMin + (int) (Math.random() * (userAnswerMax - userAnswerMin));
			return userAnswerFinal;
		}
		return loggedUsersCount;
	}
}
