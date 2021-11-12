package com.ab.quiz;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.ConnectionPool;
import com.ab.quiz.helper.GamesGenerator;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.tasks.DeleteOldRecords;
import com.ab.quiz.tasks.TestUsersTask;

@SpringBootApplication
public class TeluguMovieQuizApplication implements ApplicationRunner {
	
	private static final Logger logger = LogManager.getLogger(TeluguMovieQuizApplication.class);
	
	public static void main(String[] args) {
		
		for (String arg:args) {
            if (arg.indexOf("serverid") > -1) {
            	arg = arg.trim();
            	int pos = arg.lastIndexOf("=");
            	int idInt = Integer.parseInt(arg.substring(pos + 1));
            	QuizConstants.MY_SERVER_ID = idInt;
            	System.out.println("My id is :" + QuizConstants.MY_SERVER_ID);
            } else if (arg.indexOf("db") > -1) {
            	arg = arg.trim();
            	int pos = arg.lastIndexOf("=");
            	String dbName = arg.substring(pos + 1);
            	ConnectionPool.JDBC_DB_URL = ConnectionPool.JDBC_DB_URL.replace("${server_quiz_db}", dbName);
            	System.out.println("JDBC Url :" + ConnectionPool.JDBC_DB_URL);
            } else if (arg.indexOf("uid1") > -1) {
            	arg = arg.trim();
            	int pos = arg.lastIndexOf("=");
            	long uid1 = Long.parseLong(arg.substring(pos + 1));
            	TestUsersTask.startUIDValue1 = uid1;
            } else if (arg.indexOf("uid2") > -1) {
            	arg = arg.trim();
            	int pos = arg.lastIndexOf("=");
            	long uid2 = Long.parseLong(arg.substring(pos + 1));
            	TestUsersTask.startUIDValue2 = uid2;
            }
		}
		SpringApplication.run(TeluguMovieQuizApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			logger.debug("Starting the TeluguMovieQuizApplication application");
			
			GamesGenerator gameGenerator1 = new GamesGenerator(1);
			gameGenerator1.initialize();
			
			GamesGenerator gameGenerator2 = new GamesGenerator(2);
			gameGenerator2.initialize();
			
			boolean first = false;
			if (gameGenerator1.getFirstGameTime() < gameGenerator2.getFirstGameTime()) {
				first = true;
			}
			
			for (int index = 1; index <= QuizConstants.MAX_LIVE_SLOTS; index++) {
				if (first) {
					gameGenerator1.buildInitialGameSet();
					gameGenerator2.buildInitialGameSet();
				} else {
					gameGenerator2.buildInitialGameSet();
					gameGenerator1.buildInitialGameSet();
				}
			}
			
			int inMemSlots = 2 * QuizConstants.MAX_LIVE_SLOTS;
			for (int index = 1; index <= inMemSlots; index++) {
				if (first) {
					gameGenerator1.buildNextGameSet();
					gameGenerator2.buildNextGameSet();
				} else {
					gameGenerator2.buildNextGameSet();
					gameGenerator1.buildNextGameSet();
				}
			}
			
			gameGenerator1.setupGames();
			gameGenerator2.setupGames();
			
			//WinMsgHandler.getInstance();
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, 1);
			calendar.set(Calendar.HOUR, 3);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.AM_PM, Calendar.AM);
			
			long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();
			//initialDelay = 0;
			
			LazyScheduler.getInstance().submitRepeatedTask(new DeleteOldRecords(), initialDelay, 
					24 * 60 * 1000, TimeUnit.MILLISECONDS);
			
			if (QuizConstants.TESTMODE == 1) {
				TestUsersTask task = new TestUsersTask();
				task.setUp();
			}
			
			logger.info("Server started successfully...");
			
		} catch(SQLException ex) {
			logger.error("SQLException in TeluguMovieQuizApplication", ex);
		}
	}
}
