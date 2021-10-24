package com.ab.quiz.handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.CelebritySpecialHandler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.CelebrityFullDetails;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GameOperation;
import com.ab.quiz.pojo.GameStatus;
import com.ab.quiz.pojo.GameStatusHolder;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PlayerAnswer;
import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.PrizeDetail;
import com.ab.quiz.pojo.UpcomingCelebrity;
import com.ab.quiz.pojo.UserHistoryGameDetails;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;

public class GameManager {

	private static final Logger logger = LogManager.getLogger(GameManager.class);
	private static GameManager instance = null;
	private TreeMap<Long, GameHandler> gameIdToGameHandler = new TreeMap<Long, GameHandler>();
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private GameManager() {
	}
	
	public static GameManager getInstance() {
		if (instance == null) {
			logger.debug("In UserMoneyDBHandler getInstance() method instance created");
			instance = new GameManager();
		}
		return instance;
	}
	
	public void addNewGames(List<GameHandler> gameHandlers) {
		lock.writeLock().lock();
		for (GameHandler gameHandler : gameHandlers) {
			long gameId = gameHandler.getGameDetails().getGameId();
			Long keyLong = new Long(gameId);
			gameIdToGameHandler.put(keyLong, gameHandler);
		}
		lock.writeLock().unlock();
		logger.info("New games are added. The size is {}", gameIdToGameHandler.size());
	}
	
	public void deleteCompletedGames(List<Long> completedGameIds) {
		
		lock.writeLock().lock();
		
		for (Long id : completedGameIds) {
			gameIdToGameHandler.remove(id);
		}
		
		lock.writeLock().unlock();
		
		logger.info("Completed games are deleted. Now the size is {}", gameIdToGameHandler.size());
	}
	
	public List<GameDetails> getFutureGames(int gametype) {
		// Iterate through the Tree Map and return the list
		
		logger.debug("In getFutureGames() with {}", gametype);
		
		List<GameDetails> list = new ArrayList<>();
		
		lock.readLock().lock();
		
		Set<Map.Entry<Long, GameHandler>> setValues = gameIdToGameHandler.entrySet();
		long currentTime = System.currentTimeMillis();
		
		for (Map.Entry<Long, GameHandler> eachEntry : setValues) {
			GameHandler gameHandler = eachEntry.getValue();
			if (gametype != gameHandler.getGameDetails().getGameType()) {
				continue;
			}
			long startTime = gameHandler.getGameDetails().getStartTime();
			if ((currentTime < startTime) && ((startTime - currentTime) >= QuizConstants.GAME_BEFORE_LOCK_PERIOD_IN_MILLIS)) {
				logger.debug("Adding Game Id : " + gameHandler.getGameDetails().getGameId());
				list.add(gameHandler.getGameDetails());
			}
		}
		lock.readLock().unlock();
		logger.info("In getFutureGames() Returned size is " + list.size());
		return list;
	}
	
	public UserHistoryGameDetails getHistoryGames(long userProfileId, 
			int startRowNo) throws SQLException {
		
		return GameHistoryDBHandler.getInstance().getUserPlayedGameDetails(userProfileId, 
				startRowNo);
	}
	
	public List<GameDetails> getEnrolledGames(int gametype, long userProfileId) {
		
		List<GameDetails> list = new ArrayList<>();
		
		lock.readLock().lock();
		
		Set<Map.Entry<Long, GameHandler>> setValues = gameIdToGameHandler.entrySet();
		long currentTime = System.currentTimeMillis();
		
		for (Map.Entry<Long, GameHandler> eachEntry : setValues) {
			GameHandler gameHandler = eachEntry.getValue();
			if (gametype != gameHandler.getGameDetails().getGameType()) {
				continue;
			}
			long startTime = gameHandler.getGameDetails().getStartTime();
			if (currentTime > startTime) {
				if ((currentTime - startTime) >= QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS) {
					continue;
				}
			}
			if (gameHandler.isUserEnrolled(userProfileId)) {
				list.add(gameHandler.getGameDetails());
			}
		}
		
		lock.readLock().unlock();
		return list;
	}
	
	public GameDetails getGameDetailsById(long gameId) throws NotAllowedException {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		return gameHandler.getGameDetails();
	}
	
	public GameStatus getGameStatus(long gameId) throws NotAllowedException, SQLException, Exception {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		
		GameStatus gameStatus = new GameStatus();
		gameStatus.setGameId(gameId);
		gameStatus.setCurrentCount(gameHandler.getEnrolledUserCount());
		gameStatus.setGameStatus(1); // Game is alive
		
		long currentTime = System.currentTimeMillis();
		long startTime = gameHandler.getGameDetails().getStartTime();
		
		if (currentTime < startTime) {
			if ((startTime - currentTime) <= QuizConstants.GAME_BEFORE_LOCK_PERIOD_IN_MILLIS) {
				gameStatus.setGameStatus(2); // Game is alive and lock period is reached..
				if (gameHandler.getEnrolledUserCount() < 3) {
					gameStatus.setGameStatus(-1);
					Map<Long, Boolean> revertedStatus = gameHandler.cancelGame();
					gameStatus.setUserAccountRevertStatus(revertedStatus);
				}
			}
		}
		return gameStatus;
	}
	
	private GameStatusHolder getGamesStatus(int gameType, long userProfileId) throws SQLException, Exception {
		// Discard completed state games
		// Include Locked, In-Progress, Future
		HashMap <Long, GameStatus> gameIdToGameStatus = new HashMap<>();
		
		lock.readLock().lock();
		
		Set<Map.Entry<Long, GameHandler>> setValues = gameIdToGameHandler.entrySet();
		long currentTime = System.currentTimeMillis();
		List<GameHandler> cancelledGames = new ArrayList<>();
		
		for (Map.Entry<Long, GameHandler> eachEntry : setValues) {
			GameHandler gameHandler = eachEntry.getValue();
			if (gameType != -1) { // Ignore Game Type. Get all games status here.
				if (gameHandler.getGameDetails().getGameType() != gameType) {
					continue;
				}
			} else {
				int statusSizes = gameIdToGameStatus.size();
				if (statusSizes == 2 * QuizConstants.GAMES_RATES_IN_ONE_SLOT_MIXED.length) {
					// All games status is not necessary. First slot in Mixed and Celebrities are enough.
					break;
				}
			}
			if (userProfileId != -1) {
				if (!gameHandler.isUserEnrolled(userProfileId)) {
					continue;
				}
			}
			
			long startTime = gameHandler.getGameDetails().getStartTime();
			if (currentTime > startTime) {
				continue;
			}
			long diff = startTime - currentTime;
			if (diff <= QuizConstants.GAME_BEFORE_LOCK_PERIOD_IN_MILLIS) {
				// Locked state
				if (gameHandler.getEnrolledUserCount() < 3) {
					cancelledGames.add(gameHandler);
					continue;
				} 
			}
			GameStatus gameStatus = new GameStatus();
			gameStatus.setGameId(gameHandler.getGameDetails().getGameId());
			gameStatus.setViewId(gameHandler.getGameDetails().getTempGameId());
			gameStatus.setCurrentCount(gameHandler.getEnrolledUserCount());
			gameStatus.setGameStatus(1);  // Still active
			
			gameIdToGameStatus.put(gameStatus.getGameId(), gameStatus);
		}
		
		for (GameHandler cancelGameHandler : cancelledGames) {
			Map<Long, Boolean> revertedStatus = cancelGameHandler.getRevertedStatus();
			if (revertedStatus == null) {
				revertedStatus = cancelGameHandler.cancelGame();
				cancelGameHandler.getGameDetails().setStatus(-1);
			}
			GameStatus gameStatus = new GameStatus();
			gameStatus.setGameId(cancelGameHandler.getGameDetails().getGameId());
			gameStatus.setViewId(cancelGameHandler.getGameDetails().getTempGameId());
			gameStatus.setCurrentCount(cancelGameHandler.getEnrolledUserCount());
			gameStatus.setGameStatus(-1);	// Cancelled
			gameStatus.setUserAccountRevertStatus(revertedStatus);
			
			gameIdToGameStatus.put(gameStatus.getGameId(), gameStatus);
		}
		lock.readLock().unlock();
		
		GameStatusHolder holder = new GameStatusHolder();
		holder.setVal(gameIdToGameStatus);
		return holder;
	}
	
	public GameStatusHolder getUserEnrolledGamesStatus(int gameType,long userProfileId) throws SQLException, Exception {
		return getGamesStatus(gameType,userProfileId);
	}
	
	public GameStatusHolder getAllGamesStatus(int gameType) throws SQLException, Exception {
		return getGamesStatus(gameType, -1);
	}
	
	public boolean joinGame(long gameId, GameOperation gameOper) 
			throws NotAllowedException, SQLException {
		
		// Step 1 : Get the GameHandller see if exists
		// Step 2: See if any other game he is enrolled for the same time
		// Step 3 : Check if game is locked
		// Step 4 check the max allowed count
		// Step 5 : See if the account has enough money to play
		
		// Step 1
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game does not exist " + gameId);
		}

		// Already enrolled.
		boolean isAlreadyEnrolled = gameHandler.isUserEnrolled(gameOper.getUserProfileId());
		if (isAlreadyEnrolled) {
			return true;
		}
		
		int gameType = gameHandler.getGameDetails().getGameType();
		int otherGameType = -1;
		if (gameType == 1) {
			otherGameType = 2;
		} else {
			otherGameType = 1;
		}
		
		// Step 2
		long currentGameStartTime = gameHandler.getGameDetails().getStartTime();
		List<GameDetails> enrolledGames = getEnrolledGames(gameType, gameOper.getUserProfileId());
		List<GameDetails> otherTypeEnrolledGames = getEnrolledGames(otherGameType, gameOper.getUserProfileId());
		enrolledGames.addAll(otherTypeEnrolledGames);
		if (enrolledGames.size() > 0) {
			HashMap<Long, Integer> startTimeToGameId = new HashMap<>();
			for (GameDetails gameDetails : enrolledGames) {
				startTimeToGameId.put(gameDetails.getStartTime(), gameDetails.getTempGameId());
			}
			Integer enrolledGameId = startTimeToGameId.get(currentGameStartTime);
			if (enrolledGameId != null) {
				logger.info("Trying to join the game " + gameId + " Temp Id is " + gameHandler.getGameDetails().getTempGameId());	
				throw new NotAllowedException("Not Allowed as Already enrolled for game starting at same time with GameId#: " + enrolledGameId);
			}
			
			// Check -5 and +5 minutes games too.
			long fiveMinsBackTime = currentGameStartTime - (5 * 60 * 1000);
			enrolledGameId = startTimeToGameId.get(fiveMinsBackTime);
			if (enrolledGameId != null) {
				throw new NotAllowedException("Not Allowed as Already enrolled for game with GameId#: " + enrolledGameId);
			}
			
			long fiveMinsAfterTime = currentGameStartTime + (5 * 60 * 1000);
			enrolledGameId = startTimeToGameId.get(fiveMinsAfterTime);
			if (enrolledGameId != null) {
				throw new NotAllowedException("Not Allowed as Already enrolled for game with GameId#: " + enrolledGameId);
			}
		}
		
		// Step 3
		long currentTime = System.currentTimeMillis();
		if (currentTime > currentGameStartTime) {
			throw new NotAllowedException("Game already started. Please try next game");
		}
		if (currentTime < currentGameStartTime) {
			if ((currentGameStartTime - currentTime) <= QuizConstants.GAME_BEFORE_LOCK_PERIOD_IN_MILLIS) {
				throw new NotAllowedException("Game about to start. Please try next game");
			}
		}
		
		// Step 4
		int currentCount = gameHandler.getEnrolledUserCount();
		if ((currentCount + 1) > gameHandler.getGameDetails().getMaxCapacity()) {
			throw new NotAllowedException("Max count reached. Please try next game");
		}
		
		long userBossId = gameOper.getUserBossId();
		String userName = gameOper.getUserName();
		
		if (gameHandler.getGameDetails().getTicketRate() == 0) {
			boolean res = gameHandler.join(gameOper.getUserProfileId(), gameOper.getUserAccountType(), 0, userName);
			return res;
		}
		
		
		try {
			
			logger.info("This is in game manager join");
			UsersCompleteMoneyDetails completeDetails = new UsersCompleteMoneyDetails();
			completeDetails.setCheckMoney(true);
			
			List<MoneyTransaction> joinMoneyTransactions = new ArrayList<>();
			
			MoneyTransaction joinTransaction = new MoneyTransaction();
			joinTransaction.setUserProfileId(gameOper.getUserProfileId());
			joinTransaction.setAccountType(UserMoneyAccountType.LOADED_MONEY);
			joinTransaction.setOperType(UserMoneyOperType.SUBTRACT);
			joinTransaction.setAmount(gameHandler.getGameDetails().getTicketRate());
			
			long tktRate = gameHandler.getGameDetails().getTicketRate();
			
			MyTransaction transaction = Utils.getTransactionPojo(gameOper.getUserProfileId(), 
					currentGameStartTime, (int)tktRate, TransactionType.DEBITED.getId(), 
					UserMoneyAccountType.LOADED_MONEY.getId(), -1, -1, "Played game#:" + gameId, null);
			
			joinTransaction.setTransaction(transaction);
			
			joinMoneyTransactions.add(joinTransaction);
			
			completeDetails.setUsersMoneyTransactionList(joinMoneyTransactions);
			
			PostTask<UsersCompleteMoneyDetails, Boolean> joinTask = Request.updateMoney();
			joinTask.setPostObject(completeDetails);
			joinTask.execute();
			
		} catch (Exception e) {
			logger.error("Error while fetching User Money Details" , e);
			String errMessage = "Check beckend connectvity";
            boolean isAPIException = false;
			if (e instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) e;
                errMessage = clientExp.getResponseBodyAsString();
                isAPIException = true;
            } else if (e instanceof HttpServerErrorException) {
                HttpServerErrorException serverExp = (HttpServerErrorException) e;
                errMessage = serverExp.getResponseBodyAsString();
                isAPIException = true;
            }
			logger.error("Error message is {} : apiexception is {}", errMessage, isAPIException);
			throw new NotAllowedException(errMessage);
		}
		
		boolean res = gameHandler.join(gameOper.getUserProfileId(), gameOper.getUserAccountType(), userBossId, userName);
		return res;
	}

	public boolean unjoin(long gameId, GameOperation gameOper) throws NotAllowedException, SQLException {
		// Step 1 : Check if game exists
		// Step 2 : Check if enrolled
		// Step 3 : Check if locking period is over
		
		// Step 1
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game does not exist " + gameId);
		}
		
		// Step 2
		if (!gameHandler.isUserEnrolled(gameOper.getUserProfileId())) {
			throw new NotAllowedException("User never registered for this game" + gameId);
		}
		
		// Step 3
		long currentTime = System.currentTimeMillis();
		long currentGameStartTime = gameHandler.getGameDetails().getStartTime();
		if (currentTime > currentGameStartTime) {
			throw new NotAllowedException("Game already started. Cannot leave now");
		}
		if (currentTime < currentGameStartTime) {
			if ((currentGameStartTime - currentTime) <= QuizConstants.GAME_BEFORE_LOCK_PERIOD_IN_MILLIS) {
				throw new NotAllowedException("Game about to start. Cannot leave now");
			}
		}
		
		UserMoneyAccountType accType = UserMoneyAccountType.LOADED_MONEY;
		
		try {
			long tktRate = gameHandler.getGameDetails().getTicketRate();
			if (tktRate == 0) {
				return gameHandler.withdraw(gameOper.getUserProfileId());
			}
			
			UsersCompleteMoneyDetails completeDetails = new UsersCompleteMoneyDetails();
			List<MoneyTransaction> unjoinMoneyTransactions = new ArrayList<>();
			
			MoneyTransaction unjoinTransaction = new MoneyTransaction();
			unjoinTransaction.setUserProfileId(gameOper.getUserProfileId());
			unjoinTransaction.setAccountType(UserMoneyAccountType.LOADED_MONEY);
			unjoinTransaction.setOperType(UserMoneyOperType.ADD);
			unjoinTransaction.setAmount(tktRate);
			
			MyTransaction transaction = Utils.getTransactionPojo(gameOper.getUserProfileId(), 
					currentGameStartTime, (int)tktRate, TransactionType.CREDITED.getId(), 
					accType.getId(), -1, -1, "Refund for left game# " + gameId, null);
			
			unjoinTransaction.setTransaction(transaction);
			
			unjoinMoneyTransactions.add(unjoinTransaction);
			
			completeDetails.setUsersMoneyTransactionList(unjoinMoneyTransactions);
			
			PostTask<UsersCompleteMoneyDetails, Boolean> updateMoneyTask = Request.updateMoney();
			updateMoneyTask.setPostObject(completeDetails);
			updateMoneyTask.execute();
			
			
		} catch (Exception e) {
			logger.error("Error while fetching User Money Details" , e);
			String errMessage = "Check beckend connectvity";
            boolean isAPIException = false;
			if (e instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) e;
                errMessage = clientExp.getResponseBodyAsString();
                isAPIException = true;
            } else if (e instanceof HttpServerErrorException) {
                HttpServerErrorException serverExp = (HttpServerErrorException) e;
                errMessage = serverExp.getResponseBodyAsString();
                isAPIException = true;
            }
			logger.error("Error message is {} : apiexception is {}", errMessage, isAPIException);
			throw new NotAllowedException(errMessage);
		}
		return gameHandler.withdraw(gameOper.getUserProfileId());
	}
	
	public List<GameHandler> getCompletedGameHandlers(int mode) {
		// Tested
		List<GameHandler> list = new ArrayList<>();
		
		lock.readLock().lock();
		Set<Map.Entry<Long, GameHandler>> setValues = gameIdToGameHandler.entrySet();
		long currentTime = System.currentTimeMillis();
		
		for (Map.Entry<Long, GameHandler> eachEntry : setValues) {
			GameHandler gameHandler = eachEntry.getValue();
			if (gameHandler.getGameDetails().getGameType() != mode) {
				continue;
			}
			long startTime = gameHandler.getGameDetails().getStartTime();
			long diff = currentTime - startTime;
			if (diff >= (QuizConstants.TIME_GAP_BETWEEN_SLOTS_IN_MILLIS - QuizConstants.START_PAYMENTS_BEFORE_COMPLETION_TIME_OFFSET)) {
				// Completed
				list.add(gameHandler);
			}
		}
		
		lock.readLock().unlock();
		return list;
	}

	public boolean submitAnswer(long gameId, PlayerAnswer answer) throws NotAllowedException {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		return gameHandler.submitAnswer(answer);
	}
	
	public List<PrizeDetail> getGamePrizeDetails(long gameId) throws NotAllowedException {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		return gameHandler.getPrizeDetails();
	}
	
	public List<PlayerSummary> getLeaderBoard(long gameId, int qNo) throws NotAllowedException {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		return gameHandler.getLeaderBoardPositions(qNo);
	}
	
	public boolean getEnrolledStatus(long gameId, long userProfileId) throws NotAllowedException {
		GameHandler gameHandler = gameIdToGameHandler.get(gameId);
		if (gameHandler == null) {
			throw new NotAllowedException("Game not found with id " + gameId);
		}
		return gameHandler.isUserEnrolled(userProfileId);
	}

	public CelebrityFullDetails getCelebrityFullDetails() {
		
		CelebrityFullDetails fullDetails = new CelebrityFullDetails();
		
		int maxSize = QuizConstants.GAMES_RATES_IN_ONE_SLOT_SPECIAL.length;
		List<UpcomingCelebrity> upcomingCelebrities = CelebritySpecialHandler.getInstance().getUpcomingCelebrityDetails(maxSize);
		
		List<String> masterList = new ArrayList<>();
		
		for (UpcomingCelebrity uc : upcomingCelebrities) {
			for (String celebName : uc.getCelebrityNames()) {
				if (!masterList.contains(celebName)) {
					masterList.add(celebName);
				}
			}
		}
		
		List<String> sortedNames = masterList.stream().sorted().collect(Collectors.toList());
		
		fullDetails.setMasterNames(sortedNames);
		fullDetails.setNamesList(upcomingCelebrities);
		logger.debug("In getCelebrityFullDetails() with master list size {} and celebrities list size {}", 
				sortedNames.size(), upcomingCelebrities.size());
		return fullDetails;
	}
}
