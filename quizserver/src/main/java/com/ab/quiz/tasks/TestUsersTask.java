package com.ab.quiz.tasks;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GameOperation;
import com.ab.quiz.pojo.PlayerAnswer;

public class TestUsersTask implements Runnable {
	
	private ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
	private int mode;
	private long testingUserIdMode1 = 1001;
	private long testingUserIdMode2 = 2001;
	private static final Logger logger = LogManager.getLogger(TestUsersTask.class);
	
	public TestUsersTask() {
		((ScheduledThreadPoolExecutor)scheduler).setRemoveOnCancelPolicy(true);
	}
	
	private ScheduledFuture<?> submitRepeatedTask(Runnable run, long initialDelay, long delay, TimeUnit unit) {
		return scheduler.scheduleAtFixedRate(run, initialDelay, delay, unit);
	}
	
	public void setUp() {
		long mode1StartTime = getStartTime(1);
		long mode2StartTime = getStartTime(2);
		
		long startTaskTime = mode1StartTime;
		mode = 1;
		if (mode2StartTime < startTaskTime) {
			startTaskTime = mode2StartTime;
			mode = 2;
		}
		
		long initailDelay = startTaskTime - System.currentTimeMillis() + 1 * 1000;
		
		submitRepeatedTask(this, 0, 5 * 60 * 1000, TimeUnit.MILLISECONDS);
	}
	
	private long getStartTime(int mode) {
		long currentTime = System.currentTimeMillis();
		if (mode == 1) {
			currentTime = currentTime + QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS;
		} else {
			currentTime = currentTime + 5 * 60 * 1000;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		
		int minute = calendar.get(Calendar.MINUTE);
		if (mode == 1) {
			minute = (minute/QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MINS) * QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MINS;
		} else {
			minute = minute/5;
			if ((minute % 2) == 0) {
				minute++;
			}
			minute = minute * 5;
		}
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		currentTime = calendar.getTimeInMillis();
		return currentTime;
	}
	
	
	
	public void run() {
		try {
			logger.info("Adding simulated user start for {} games", mode);
			List<GameDetails> games = GameManager.getInstance().getFutureGames(mode);
			int index = 0;
			for (GameDetails gameDetails : games) {
				addTestUsersToGame(gameDetails);
				++index;
				if ((index % QuizConstants.GAMES_RATES_IN_ONE_SLOT_MIXED.length) == 0) {
					testingUserIdMode1 = 1001;
					testingUserIdMode2 = 2001;
				}
			}
			logger.info("Adding simulated user completed for {} games", mode);
			if (mode == 1) {
				mode = 2;
			} else {
				mode = 1;
			}
		} catch(Exception ex) {
			logger.error("Exception in test users task", ex);
		}
	}
	
	private void addTestUsersToGame(GameDetails gameDetails) {
		if (gameDetails.getTicketRate() == 0) {
			return;
		}
		if (gameDetails.getCurrentCount() == 10) {
			return;
		}
		
		//int min = 3;
		//int max = QuizConstants.MAX_PLAYERS_PER_GAME;
		//int randomPlayerCount = min + (int) (Math.random() * (max - min));
		int randomPlayerCount = 8;
		
		for (int index = 1; index <= randomPlayerCount; index ++) {
			
			try {
				
				long predefinedUserProfileId = -1;
				if (gameDetails.getGameType() == 1) {
					predefinedUserProfileId = testingUserIdMode1++; 
				} else {
					predefinedUserProfileId = testingUserIdMode2++;
				}
					
				GameOperation gameOperation = new GameOperation();
				gameOperation.setUserProfileId(predefinedUserProfileId);
				gameOperation.setUserAccountType(UserMoneyAccountType.LOADED_MONEY.getId());
				GameManager.getInstance().joinGame(gameDetails.getGameId(), gameOperation);
				//Thread.sleep(300);
				
				for (int qIndex = 1; qIndex <= 10; qIndex ++) {
					PlayerAnswer playerAns = getRandomPlayerAnswer();
					playerAns.setQuestionNo(qIndex);
					playerAns.setUserProfileId(predefinedUserProfileId);
					GameManager.getInstance().submitAnswer(gameDetails.getGameId(), playerAns);
				}
			} catch(Exception ex) {
				logger.error("Exception in addTestUsersToGame", ex);
			}
		}
	}
	
	private PlayerAnswer getRandomPlayerAnswer() {
		int userAnswerMin = 1;
		int userAnswerMax = 5;
		int userAnswerFinal = userAnswerMin + (int) (Math.random() * (userAnswerMax - userAnswerMin));
		
		int timeMin = 1;
		int timeMax = 29;
		int timeFinal = timeMin + (int) (Math.random() * (timeMax - timeMin));
		int timeFinalMillis = timeFinal * 1000; 
		
		PlayerAnswer answer = new PlayerAnswer();
		answer.setUserAnswer(userAnswerFinal);
		answer.setTimeDiff(timeFinalMillis);
		
		return answer;
	}
}
