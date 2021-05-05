package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.UserMoney;

public class PaymentProcessor {
	
	private List<PlayerSummary> summaryList;
	private GameDetails gameDetails;
	private List<Long> winUsersIds = new ArrayList<>();
	
	
	private final Logger logger = LogManager.getLogger(PaymentProcessor.class);
	
	public PaymentProcessor(List<PlayerSummary> summaryList, GameDetails gameDetails) {
		this.summaryList = summaryList;
		this.gameDetails = gameDetails;
	}
	
	public List<Long> getWinnerUserIdSet() {
		for (PlayerSummary ps : summaryList) {
			if (ps.getAmountWon() == 0) {
				continue;
			}
			winUsersIds.add(ps.getUserProfileId());
		}
		return winUsersIds;
	}
	
	public void processPayments(Map<Long, UserMoney> userIdVsUserMoney, 
			Map<Long, Long> userIdVsBossId) {
		
		logger.info("*******************************************************");
		logger.info("Batching payments for GameId#:" + gameDetails.getGameId());
		for (PlayerSummary ps : summaryList) {
			logger.info(ps);
		}
		for (PlayerSummary ps : summaryList) {
			if (ps.getAmountWon() == 0) {
				continue;
			}
			
			List<MoneyTransaction> winnersMoneyTransactions = new ArrayList<>();
			
			String userName = ps.getUserName();
			int amountWon = ps.getAmountWon();
			int profit = amountWon - gameDetails.getTicketRate();
			long userProfileId = ps.getUserProfileId();
			
			try {
				logger.info("Paying amount {} to id {} and userName {}", amountWon, userProfileId, userName);
				
				UserMoney userMoney = userIdVsUserMoney.get(userProfileId);
				if (userMoney == null) {
					logger.info("*****************************************");
					logger.info("User Money not found for userId : {}", userProfileId);
					logger.info("*****************************************");
					continue;
				}
				
				long userOB = userMoney.getWinningAmount();
				long gameStartTime = gameDetails.getStartTime();
				String comments = "Winning Money for GameId#:" + gameDetails.getGameId();
				long userCB = userOB + amountWon; 
				
				
				MyTransaction transaction = Utils.getTransactionPojo(userProfileId, gameStartTime, 
						amountWon, TransactionType.CREDITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments);
				transaction.setIsWin(1);
				
				MoneyTransaction moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.WINNING_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.ADD);
				moneyTransaction.setUserProfileId(userProfileId);
				moneyTransaction.setAmount(amountWon);
				moneyTransaction.setTransaction(transaction);
				
				winnersMoneyTransactions.add(moneyTransaction);
				
				Map<Long, UserMoney> winUserMap = new HashMap<>();
				winUserMap.put(userProfileId, userMoney);
				
				InMemUserMoneyManager.getInstance().update(winnersMoneyTransactions, winUserMap);
				winnersMoneyTransactions.clear();
				
				Long bossProfileId = userIdVsBossId.get(userProfileId);
				if (bossProfileId == null) {
					logger.info("Boss Referal code is null. Returning for userProfileId : {}", userProfileId);
					continue;
				}
				
				long bossUserProfileId = bossProfileId;
				int bossShare = Utils.getBossMoney(profit);
				if (bossShare == 0) {
					continue;
				}
				
				UserMoney bossUserMoney = userIdVsUserMoney.get(bossUserProfileId);
				
				if (bossUserMoney == null) {
					logger.info("*****************************************");
					logger.info("User Money not found for userId : {}", userProfileId);
					logger.info("*****************************************");
					continue;
				}
				
				userMoney = InMemUserMoneyManager.getInstance().getUserMoneyById(userProfileId);
				
				userOB = userMoney.getWinningAmount();
				long bossOB = bossUserMoney.getReferalAmount();
				
				gameStartTime = gameDetails.getStartTime();
				
				comments = "Paying Your Referrer share for GameId# " + gameDetails.getGameId();
				String bossCmts = "For Referring " + userName 
					+ ". Winning money share for GameId#:" + gameDetails.getGameId();
				userCB = userOB - bossShare;
				long bossCB = bossOB + bossShare;
				
				transaction = Utils.getTransactionPojo(userProfileId, gameDetails.getStartTime(), 
						bossShare, TransactionType.DEBITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments);
				
				MyTransaction transaction1 = Utils.getTransactionPojo(bossUserProfileId, gameDetails.getStartTime(), 
								bossShare, TransactionType.CREDITED.getId(), 
								UserMoneyAccountType.REFERAL_MONEY.getId(), bossOB, bossCB, bossCmts);
				transaction1.setIsWin(1);
				
				moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.WINNING_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.SUBTRACT);
				moneyTransaction.setUserProfileId(userProfileId);
				moneyTransaction.setAmount(bossShare);
				moneyTransaction.setTransaction(transaction);
				
				winnersMoneyTransactions.add(moneyTransaction);
				
				moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.REFERAL_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.ADD);
				moneyTransaction.setUserProfileId(bossUserProfileId);
				moneyTransaction.setAmount(bossShare);
				moneyTransaction.setTransaction(transaction1);
				
				winnersMoneyTransactions.add(moneyTransaction);
				
				winUserMap.put(userProfileId, userMoney);
				winUserMap.put(bossUserProfileId, bossUserMoney);
				
				InMemUserMoneyManager.getInstance().update(winnersMoneyTransactions, winUserMap);
				
			} catch (Exception ex) {
				logger.error("For user profile id " + userProfileId);
				logger.error("Exception while processing the payment", ex);
				continue;
			}
		}
	}
}
