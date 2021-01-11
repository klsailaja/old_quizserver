package com.ab.quiz.helper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.db.QuestionDBHandler;
import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.PlayerAnswer;
import com.ab.quiz.pojo.Question;
import com.ab.quiz.tasks.DeleteCompletedGamesTask;
import com.ab.quiz.tasks.HistoryGameSaveTask;
import com.ab.quiz.tasks.UpdateMaxGameIdTask;

public class GamesGenerator implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(GamesGenerator.class);
	
	private List<GameHandler> initialGameSet = new ArrayList<>(QuizConstants.MAX_TOTAL_GAMES);
	private List<GameHandler> nextGameSet = new ArrayList<>(QuizConstants.MAX_TOTAL_GAMES);
	
	private long lastGameId;
	private long lastProcessedTime;
	private long firstGameTime;
	
	private int mode = 1; // 1 - public, 2 - celebraties
	
	public GamesGenerator(int mode) {
		this.mode = mode;
	}
	
	public void initialize() throws SQLException {
		logger.debug("This is in GamesGenerator with mode {}", mode);
		lastProcessedTime = System.currentTimeMillis();
		if (mode == 1) {
			lastProcessedTime = lastProcessedTime + QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS;
		} else {
			lastProcessedTime = lastProcessedTime + 5 * 60 * 1000;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastProcessedTime);
		
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
		lastProcessedTime = calendar.getTimeInMillis();
		firstGameTime = lastProcessedTime; 
	}
	
	public void buildInitialGameSet() {
		logger.info("Creating the initial set for mode {}", mode);
		try {
			List<GameHandler> set = generateGameData(1);
			initialGameSet.addAll(set);
			logger.info("Done with the initial set for mode {} size {}", 
					mode, initialGameSet.size());
			for (GameHandler gh : set) {
				logger.info(gh.toString());
			}
		}
		catch(SQLException ex) {
			logger.error("SQL Exception in GamesGenerator buildInitialGameSet", ex);
		}
	}
	
	public void buildNextGameSet() {
		logger.info("Creating the next game set for mode {}", mode);
		try {
			List<GameHandler> set = generateGameData(1);
			nextGameSet.addAll(set);
			logger.info("Done with the next set for mode {} size {}", 
					mode, nextGameSet.size());
			for (GameHandler gh : set) {
				logger.info(gh.toString());
			}
		}
		catch(SQLException ex) {
			logger.error("SQL Exception in GamesGenerator nextGameSet", ex);
		}
	}
	
	public long getFirstGameTime() {
		return firstGameTime;
	}
	
	public void setupGames() {
		
		GameManager.getInstance().addNewGames(initialGameSet);
		
		long repeatedTaskInterval = QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS 
				+ 1 * 1000;
		long initailDelay = firstGameTime - System.currentTimeMillis() + repeatedTaskInterval; 
		
		LazyScheduler.getInstance().submitRepeatedTask(this, initailDelay, 
					repeatedTaskInterval, TimeUnit.MILLISECONDS);
		
	}

	public void run() {
		logger.info("Running Repeated Task for mode {}", mode);
		try {
			if (initialGameSet.size() > 0) { 
				initialGameSet.clear();
			}
			
			List<GameHandler> newGames = new ArrayList<>();
			for (int index = 1; index <= QuizConstants.GAMES_RATES_IN_ONE_SLOT.length; index ++) {
				newGames.add(nextGameSet.remove(0));
			}
			
			GameManager.getInstance().addNewGames(newGames);
			
			List<GameHandler> completedGames = GameManager.getInstance().getCompletedGameHandlers(mode);
			int completedGameCount = completedGames.size();
			logger.info("Completed games count is {}", completedGameCount);
			
			long maxId = -1;
			List<Long> completedGameIds = new ArrayList<>();
			
			long paymentTimeTaken = System.currentTimeMillis();
			for (GameHandler completedGame : completedGames) {
				logger.info("Making payments for Game# {}", completedGame.getGameDetails().getGameId());
				
				completedGame.processPayments();
				
				Long gameId = completedGame.getGameDetails().getGameId(); 
				if (gameId > maxId) {
					maxId = gameId;
				}
				completedGameIds.add(gameId);
			}
			logger.debug("completed game list {}", completedGameIds);
			logger.info("Time taken for processing payments {}" , (System.currentTimeMillis() - paymentTimeTaken)/1000);
			
			LazyScheduler.getInstance().submit(new UpdateMaxGameIdTask(maxId));
			LazyScheduler.getInstance().submit(new HistoryGameSaveTask(completedGames));
			LazyScheduler.getInstance().submit(new DeleteCompletedGamesTask(completedGameIds), 2, TimeUnit.MINUTES);
			
			List<GameHandler> inMemGames = null;
			try {
				inMemGames = generateGameData(1);
				nextGameSet.addAll(inMemGames);
			}
			catch(SQLException ex) {
				logger.error("SQL Exception in GamesGenerator Task ", ex);
			}
			logger.debug("In Memory game set size {}", nextGameSet.size());
		}
		catch (Exception ex) {
			logger.error("Exception in the periodic task execution", ex);
		}
	}
	
	private List<GameHandler> generateGameData(int slotCount) throws SQLException {
		List<GameHandler> gameHandlerList = new ArrayList<>();
		
		for (int i = 1; i <= slotCount; i ++) {
			for (int j = 0; j < QuizConstants.GAMES_RATES_IN_ONE_SLOT.length; j ++) {
				GameDetails gameDetails = new GameDetails();
				
				lastGameId = GameIdGenerator.getInstance().getNextGameId();
				logger.info("lastGameId is {}",lastGameId);
				gameDetails.setGameId(lastGameId);
				gameDetails.setTicketRate(QuizConstants.GAMES_RATES_IN_ONE_SLOT[j]);
				gameDetails.setStartTime(lastProcessedTime);
				gameDetails.setGameType(mode);
				
				int publicView = -1;
				if (mode == 2) {
					publicView = 104;
				}
				
				List<Question> quizQuestions = QuestionDBHandler.getInstance().getRandomQues(publicView);
				Question flipQuestion = quizQuestions.remove(10);
				gameDetails.setFlipQuestion(flipQuestion);
				
				long gap = 0; 
				for (Question ques : quizQuestions) {
					ques.setQuestionStartTime(lastProcessedTime + gap);
					gap = gap + QuizConstants.GAP_BETWEEN_QUESTIONS;
				}
				gameDetails.setGameQuestions(quizQuestions);
				
				GameHandler gameHandler = new GameHandler(gameDetails);
				
				handleFreeGame(gameHandler);
				
				gameHandlerList.add(gameHandler);
			}
			lastProcessedTime = lastProcessedTime + QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS;
		}
		return gameHandlerList;
	}
	
	private void handleFreeGame(GameHandler gameHandlerInstance) {
		
		if (QuizConstants.TESTMODE == 0) {
			if (gameHandlerInstance.getGameDetails().getTicketRate() != 0) {
				return;
			}
		} else {
			if (gameHandlerInstance.getGameDetails().getTicketRate() == 0) {
				return;
			}
		}
		
		int min = 3;
		int max = QuizConstants.MAX_PLAYERS_PER_GAME;
		int randomPlayerCount = min + (int) (Math.random() * (max - min));
		
		int userIdOffset = 0;
		if (mode == 2) {
			userIdOffset = 10;
		}
		
		if (QuizConstants.TESTMODE == 1) {
			
			int tktRate = gameHandlerInstance.getGameDetails().getTicketRate();
			
			switch (tktRate) {
				case 10: {
					userIdOffset = 21;
					randomPlayerCount = 10;
					break;
				}
				case 20: {
					userIdOffset = 31;
					randomPlayerCount = 10;
					break;
				}
				case 50: {
					userIdOffset = 41;
					randomPlayerCount = 10;
					break;
				}
				case 75: {
					userIdOffset = 51;
					randomPlayerCount = 10;
					break;
				}
				case 100: {
					userIdOffset = 61;
					randomPlayerCount = 9;
					break;
				}
			}
		}
		
		
		for (int index = 1; index <= randomPlayerCount; index ++) {
			
			try {
				long predefinedUserProfileId = index + userIdOffset;
				
				boolean res = gameHandlerInstance.join(predefinedUserProfileId, UserMoneyAccountType.LOADED_MONEY.getId());
				
				logger.info("System User with profileid {} added status {}", predefinedUserProfileId, res);
				
				for (int qIndex = 1; qIndex <= 10; qIndex ++) {
					PlayerAnswer playerAns = getRandomPlayerAnswer();
					playerAns.setQuestionNo(qIndex);
					playerAns.setUserProfileId(predefinedUserProfileId);
					gameHandlerInstance.submitAnswer(playerAns);
				}
			} catch(SQLException ex) {
				logger.error("SQL Exception in handle free game", ex);
			}
		}
	}
	
	private PlayerAnswer getRandomPlayerAnswer() {
		int userAnswerMin = 1;
		int userAnswerMax = 5;
		int userAnswerFinal = userAnswerMin + (int) (Math.random() * (userAnswerMax - userAnswerMin));
		
		int timeMin = 2;
		int timeMax = 31;
		int timeFinal = timeMin + (int) (Math.random() * (timeMax - timeMin));
		int timeFinalMillis = timeFinal * 1000; 
		
		PlayerAnswer answer = new PlayerAnswer();
		answer.setUserAnswer(userAnswerFinal);
		answer.setTimeDiff(timeFinalMillis);
		
		return answer;
	}
}
