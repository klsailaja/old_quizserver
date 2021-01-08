package com.ab.quiz.helper;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.db.UserProfileDBHandler;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.UserProfile;

public class PaymentProcessor {
	private List<PlayerSummary> summaryList;
	private GameDetails gameDetails;
	
	private final Logger logger = LogManager.getLogger(PaymentProcessor.class);
	
	public PaymentProcessor(List<PlayerSummary> summaryList, GameDetails gameDetails) {
		this.summaryList = summaryList;
		this.gameDetails = gameDetails;
	}
	
	public void processPayments() {
		logger.info("*******************************************************");
		logger.info("Processing payments for GameId#:" + gameDetails.getGameId());
		for (PlayerSummary ps : summaryList) {
			logger.info(ps);
		}
		for (PlayerSummary ps : summaryList) {
			if (ps.getAmountWon() == 0) {
				continue;
			}
			
			UserMoneyDBHandler userMoneyHandler = UserMoneyDBHandler.getInstance();
			
			String userName = ps.getUserName();
			int amountWon = ps.getAmountWon();
			int profit = amountWon - gameDetails.getTicketRate();
			long userProfileId = ps.getUserProfileId();
			
			try {
				logger.info("Paying amount {} to id {} and userName {}", amountWon, userProfileId, userName);
				
				UserMoney userMoney = userMoneyHandler.getUserMoneyByProfileId(userProfileId);
				
				long userOB = userMoney.getWinningAmount();
				long gameStartTime = gameDetails.getStartTime();
				String comments = "Winning Money for GameId#:" + gameDetails.getGameId();
				long userCB = userOB + amountWon; 
				
				
				MyTransaction transaction = Utils.getTransactionPojo(userProfileId, gameStartTime, 
						amountWon, TransactionType.CREDITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments);
						
				
				boolean res = userMoneyHandler.updateUserMoney(UserMoneyAccountType.WINNING_MONEY, 
						UserMoneyOperType.ADD, userProfileId, amountWon, transaction);
				
				logger.info("Updating WINNING MONEY status for id {}", res);
				
				UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userProfileId);
				String bossCode = userProfile.getBossReferredId();
				if ((bossCode == null) || (bossCode.equalsIgnoreCase("null"))) {
					logger.info("Boss Referal code is null. Returning for userProfileId : {}", userProfileId);
					continue;
				}
				
				UserProfile myBossUserProfile = UserProfileDBHandler.getInstance().getProfileByBossRefaralCode(bossCode);
				if ((myBossUserProfile == null) || (myBossUserProfile.getId() == 0)) {
					logger.info("Boss User Profile not found with code {}", bossCode);
					continue;
				}
				
				long bossUserProfileId = myBossUserProfile.getId();
				int bossShare = Utils.getBossMoney(profit);
				
				userMoney = userMoneyHandler.getUserMoneyByProfileId(userProfileId);
				UserMoney bossUserMoney = userMoneyHandler.getUserMoneyByProfileId(bossUserProfileId);
				
				userOB = userMoney.getWinningAmount();
				long bossOB = bossUserMoney.getReferalAmount();
				logger.info("********************************* {}", bossOB);
				
				gameStartTime = gameDetails.getStartTime();
				
				comments = "Paying Your Referrer share for GameId# " + gameDetails.getGameId();
				String bossCmts = "For Referring " + userProfile.getName() 
					+ ". Winning money share for GameId#:" + gameDetails.getGameId();
				userCB = userOB - bossShare;
				long bossCB = bossOB + bossShare;
				
				transaction = Utils.getTransactionPojo(userProfileId, gameDetails.getStartTime(), 
						bossShare, TransactionType.DEBITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments);
				
				MyTransaction transaction1 = Utils.getTransactionPojo(bossUserProfileId, gameDetails.getStartTime(), 
								bossShare, TransactionType.CREDITED.getId(), 
								UserMoneyAccountType.REFERAL_MONEY.getId(), bossOB, bossCB, bossCmts);
				
				res = userMoneyHandler.payToBoss(userProfileId, bossUserProfileId, bossShare, transaction, transaction1);
				
				logger.info("Boss payment overall result is {}", res);
				
			} catch (SQLException ex) {
				logger.error("For user profile id " + userProfileId);
				logger.error("SQL Exception while processing the payment", ex);
				continue;
			}
		}
	}
}
