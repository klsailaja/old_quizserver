package com.ab.quiz.handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.db.UserProfileDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.InMemUserMoneyManager;
import com.ab.quiz.helper.LeaderBoard;
import com.ab.quiz.helper.PaymentProcessor;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GamePlayers;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PlayerAnswer;
import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.PrizeDetail;
import com.ab.quiz.pojo.Question;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.UserProfile;

public class GameHandler {
	
	private GameDetails gameDetails;
	
	// duplicate data need for faster access.
	private TreeMap<Integer,Question> questions = new TreeMap<>();
	
	private Map<Long, PlayerSummary> userProfileIdVsSummary = new HashMap<>();
	private Map<Long, List<PlayerAnswer>> userProfileIdVsAnswers = new HashMap<>();
	
	// This map maintains the question number vs all game players summary as of that question 
	private Map<Integer, List<PlayerSummary>> questionNoVsSummary = new HashMap<>();
	
	// This map maintains the UserId Vs Boss Id
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	//private Map<Long, String> userIdVsName = new HashMap<>();
	private Map<Long, Boolean> userCreditedStatus;
	
	
	private static final Logger logger = LogManager.getLogger(GameHandler.class);
	private Object lock = new Object();
	private List<PrizeDetail> gamePrizeDetails;
	
	public GameHandler(GameDetails gameDetails) {
		this.gameDetails = gameDetails;
		List<Question> quizQuestions = gameDetails.getGameQuestions();  
		for (int index = 0; index < quizQuestions.size(); index ++) {
			questions.put(index + 1, quizQuestions.get(index));
		}
	}
	
	public GameDetails getGameDetails() {
		gameDetails.setCurrentCount(getEnrolledUserCount());
		return gameDetails;
	}
	
	public int getAccountTypeUsed(long userProfileId) {
		PlayerSummary playerSummary = userProfileIdVsSummary.get(userProfileId);
		if (playerSummary == null) {
			return -1;
		}
		return playerSummary.getAccountUsed();
	}
	
	public boolean join(long userProfileId, int accountUsed) throws SQLException {
		// Create a Player object and assign the id. Add to Map userProfileIdVsSummary
		// Get the UserProfile object and assign the name.
		// If successful return true.
		
		PlayerSummary player = new PlayerSummary();
		
		player.setUserProfileId(userProfileId);
		UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userProfileId);
		player.setUserName(userProfile.getName());
		player.setAccountUsed(accountUsed);
		
		synchronized (lock) {
			userProfileIdVsSummary.put(userProfileId, player);
		}
		
		userProfileIdVsAnswers.put(userProfileId, new ArrayList<PlayerAnswer>());
		userIdVsBossId.put(userProfileId, userProfile.getBossId());
		//userIdVsName.put(userProfileId, userProfile.getName());
		
		return true;
	}
	
	public boolean withdraw(long userProfileId) throws NotAllowedException {
		logger.debug("In withdraw :" + userProfileIdVsSummary.keySet());
		userIdVsBossId.remove(userProfileId);
		userProfileIdVsAnswers.remove(userProfileId);
		//userIdVsName.remove(userProfileId);
		
		// Remove the Player object from userProfileIdVsSummary.
		
		PlayerSummary player = null;
		synchronized (lock) {
			player = userProfileIdVsSummary.remove(userProfileId);
		}
		if (player == null) { // This user never joined
			logger.debug("False");
			return false;
		}
		logger.debug("True");
		return true;
	}
	
	public boolean isUserEnrolled(long userProfileId) {
		return userProfileIdVsSummary.containsKey(userProfileId);
	}
	
	public int getEnrolledUserCount() {
		synchronized (lock) {
			return userProfileIdVsSummary.size();
		}
	}
	
	private long getAccountBalance(UserMoney userMoney, int accountType) {
		if (accountType == UserMoneyAccountType.LOADED_MONEY.getId()) {
			return userMoney.getAmount();
		} 
		return -1;
	}
	
	public Map<Long, Boolean> getRevertedStatus() {
		return userCreditedStatus;
	}
	
	public Map<Long, Boolean> cancelGame() throws SQLException {
		userCreditedStatus = new HashMap<>();
		
		Set<Map.Entry<Long, PlayerSummary>> setValues = userProfileIdVsSummary.entrySet();
		long userProfileId = 0;
		int accountUsed = 0;
		List<MoneyTransaction> cancelTransList = new ArrayList<>();
		for (Map.Entry<Long, PlayerSummary> eachEntry : setValues) {
			PlayerSummary playerSummary = eachEntry.getValue();
			userProfileId = playerSummary.getUserProfileId();
			accountUsed = playerSummary.getAccountUsed();
			UserMoneyAccountType userAccType = UserMoneyAccountType.findById(accountUsed);
			long amt = getGameDetails().getTicketRate();
			
			//UserMoney userMoney = UserMoneyHandler.getInstance().getUserMoney(userProfileId);
			UserMoney userMoney = InMemUserMoneyManager.getInstance().getUserMoneyById(userProfileId);
			long userOB = getAccountBalance(userMoney, accountUsed);
			if (userOB == -1) {
				logger.info("******************************");
				logger.info("Invalid user account type for {} with profileId {}", userAccType, userProfileId);
				logger.info("******************************");
				continue;
			}
			long userCB = userOB + amt;
			String comments = "Refund for Cancelled game#:" + gameDetails.getGameId();
			
			MyTransaction transaction = Utils.getTransactionPojo(userProfileId, gameDetails.getStartTime(), 
					gameDetails.getTicketRate(), TransactionType.CREDITED.getId(), accountUsed, userOB, userCB, comments, null); 
			
			/*boolean res = UserMoneyDBHandler.getInstance().updateUserMoney(userAccType, 
					UserMoneyOperType.ADD, userProfileId, amt, transaction);*/
			MoneyTransaction cancelGameTransaction = new MoneyTransaction(userAccType, UserMoneyOperType.ADD, 
					userProfileId, amt, transaction);
			
			cancelTransList.add(cancelGameTransaction);
			
			
			userCreditedStatus.put(userProfileId, true);
		}
		InMemUserMoneyManager.getInstance().update(cancelTransList, null);
		
		userProfileIdVsSummary.clear();
		return userCreditedStatus;
	}
	
	public List<PlayerSummary> getLeaderBoardPositions(int qNo) {
		return getLeaderBoardPositions(qNo, false);
	}
	
	public List<PlayerSummary> getLeaderBoardPositions(int qNo, boolean print) {
		if (print) {
			logger.info("**************************************************************");
			logger.info("Server GameId#: {} and Client GameId#: {}" + gameDetails.getGameId(), gameDetails.getTempGameId());
			logger.info("In getLeaderBoardPositions " + qNo);
		}
		
		List<PlayerSummary> finalPlayerSummary = questionNoVsSummary.get(qNo);
		
		if (finalPlayerSummary == null) {
			Set<Map.Entry<Long, List<PlayerAnswer>>> setValues = userProfileIdVsAnswers.entrySet();
			for (Map.Entry<Long, List<PlayerAnswer>> entry : setValues) {
				Long userProfileId = entry.getKey();
				List<PlayerAnswer> userAnswers = entry.getValue();
				int correctCount = 0;
				long totalCorrectQuestionAnsweredTime = 0;
				
				for (PlayerAnswer playerAns : userAnswers) {
					if (playerAns.getQuestionNo() <= qNo) {
						Question question = questions.get(playerAns.getQuestionNo());
						if (playerAns.isFlipUsed()) {
							question = gameDetails.getFlipQuestion();
						}
						if (question.getCorrectOption() == playerAns.getUserAnswer()) {
							correctCount++;
							totalCorrectQuestionAnsweredTime = totalCorrectQuestionAnsweredTime + playerAns.getTimeDiff();  
						}
					}
				}
				PlayerSummary summary = userProfileIdVsSummary.get(userProfileId);
				summary.setCorrectCount(correctCount);
				summary.setTotalTime(totalCorrectQuestionAnsweredTime);
				summary.setAmountWon(0);
				userProfileIdVsSummary.put(userProfileId, summary);
			}
			
			List<PlayerSummary> positions = new ArrayList<>(userProfileIdVsSummary.values());
			
			LeaderBoard leaderBoard = new LeaderBoard(positions, getPrizeDetails());
			finalPlayerSummary = leaderBoard.fillRankAndWinningMoney();
			
			questionNoVsSummary.put(qNo, finalPlayerSummary);
			/*for (PlayerSummary ps : finalPlayerSummary) {
				logger.info(ps);
			}*/
			logger.info("**************************************************************");
		}
		return finalPlayerSummary;
	}
	
	
	
	public List<GamePlayers> getPlayerDetails() {
		
		List<GamePlayers> playersList = new ArrayList<>();
		
		Set<Map.Entry<Long, PlayerSummary>> setValues = userProfileIdVsSummary.entrySet();
		
		for (Map.Entry<Long, PlayerSummary> entry : setValues) {
			
			GamePlayers player = new GamePlayers();
			player.setGameId(gameDetails.getGameId());
			player.setUserId(entry.getKey());
			playersList.add(player);
			
		}
		return playersList;
	}
	
	public boolean isGameCancelled() {
		if ((userProfileIdVsSummary.size() < 3) || (gameDetails.getTicketRate() == 0)) {
			return true;
		}
		return false;
	}
	
	public PaymentProcessor getPaymentHandler() {
		
		if (isGameCancelled()) {
			logger.info("Skipping as Game cancelled. GameId# {}", gameDetails.getGameId());
			return null;
		}
		
		List<PlayerSummary> payments = getLeaderBoardPositions(10);  
		PaymentProcessor pp = new PaymentProcessor(payments, gameDetails);
		return pp;
	}
	
	public List<PrizeDetail> getPrizeDetails() {
		logger.info("getPrizeDetails is called for {}", gameDetails.getGameId());
		if (gamePrizeDetails == null) {
			gamePrizeDetails = Utils.getPrizeDetails(gameDetails.getTicketRate(), gameDetails.getCurrentCount()); 
		}
		logger.info("getPrizeDetails returned with size {}", gamePrizeDetails.size());
		return gamePrizeDetails;
	}

	public boolean submitAnswer(PlayerAnswer answer) {
		List<PlayerAnswer> answersList = userProfileIdVsAnswers.get(answer.getUserProfileId());
		if (answersList != null) {
			answersList.add(answer);
			return true;
		}
		return false;
	}
	
	public String toString() {
		return gameDetails.toString();
	}
	
	public Map<Long, Long> getUserIdToBossIdDetails() {
		return userIdVsBossId;
	}
}
