package com.ab.quiz.helper;

import java.util.ArrayList;
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
	
	public void processPayments(Map<Long, Long> userIdVsBossId, 
			List<MoneyTransaction> winUsersTransactions) {
		
		logger.info("*******************************************************");
		logger.info("Batching payments for GameId#:" + gameDetails.getGameId() + ": client GameId#" + gameDetails.getTempGameId());
		logger.info("Game Ticket Rate {}", gameDetails.getTicketRate());
		for (PlayerSummary ps : summaryList) {
			logger.info(ps);
		}
		for (PlayerSummary ps : summaryList) {
			if (ps.getAmountWon() == 0) {
				continue;
			}
			
			String userName = ps.getUserName();
			int amountWon = ps.getAmountWon();
			int profit = amountWon - gameDetails.getTicketRate();
			
			
			if (profit < 0) {
				logger.info("****************** profit is negative: " + (profit));
				profit = 0;
			}
			
			logger.info("Profit is {}", profit);
			
			long userProfileId = ps.getUserProfileId();
			
			try {
				logger.info("Paying amount {} to id {} and userName {}", amountWon, userProfileId, userName);
				
				
				long gameStartTime = gameDetails.getStartTime();
				String transactionDesc = "Winning Money for " + Utils.getTransactionObjComments(gameDetails);
				
				
				MyTransaction transaction = Utils.getTransactionPojo(userProfileId, gameStartTime, 
						amountWon, TransactionType.CREDITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), -1, -1, transactionDesc, null);
				transaction.setIsWin(1);
				
				MoneyTransaction moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.WINNING_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.ADD);
				moneyTransaction.setUserProfileId(userProfileId);
				moneyTransaction.setAmount(amountWon);
				moneyTransaction.setTransaction(transaction);
				
				winUsersTransactions.add(moneyTransaction);
				
				Long bossProfileId = userIdVsBossId.get(userProfileId);
				if ((bossProfileId == 0) || (bossProfileId == null)) {
					logger.info("Boss Referal code is null. Returning for userProfileId : {}", userProfileId);
					continue;
				}
				
				long bossUserProfileId = bossProfileId;
				int bossShare = Utils.getBossMoney(profit);
				
				logger.info("Paying boss share of {} to boss id {}",bossShare, bossUserProfileId);
				
				if (bossShare == 0) {
					continue;
				}
				
				transactionDesc = "Referrer share for " + Utils.getTransactionObjComments(gameDetails);
				String bossCmts = "For Referring, share from " + userName 
					+ " for " + Utils.getTransactionObjComments(gameDetails);
				
				transaction = Utils.getTransactionPojo(userProfileId, gameDetails.getStartTime(), 
						bossShare, TransactionType.DEBITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), -1, -1, transactionDesc, null);
				
				MyTransaction transaction1 = Utils.getTransactionPojo(bossUserProfileId, gameDetails.getStartTime(), 
								bossShare, TransactionType.CREDITED.getId(), 
								UserMoneyAccountType.REFERAL_MONEY.getId(), -1, -1, bossCmts, null);
				transaction1.setIsWin(1);
				
				moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.WINNING_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.SUBTRACT);
				moneyTransaction.setUserProfileId(userProfileId);
				moneyTransaction.setAmount(bossShare);
				moneyTransaction.setTransaction(transaction);
				
				winUsersTransactions.add(moneyTransaction);
				
				moneyTransaction = new MoneyTransaction();
				moneyTransaction.setAccountType(UserMoneyAccountType.REFERAL_MONEY);
				moneyTransaction.setOperType(UserMoneyOperType.ADD);
				moneyTransaction.setUserProfileId(bossUserProfileId);
				moneyTransaction.setAmount(bossShare);
				moneyTransaction.setTransaction(transaction1);
				
				winUsersTransactions.add(moneyTransaction);
				
			} catch (Exception ex) {
				logger.error("For user profile id " + userProfileId);
				logger.error("Exception while processing the payment", ex);
				continue;
			}
		}
	}
}
