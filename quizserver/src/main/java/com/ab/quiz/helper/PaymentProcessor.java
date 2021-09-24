package com.ab.quiz.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	
	public void processPayments(Map<Long, UserMoney> userIdVsUserMoney, Map<Long, Long> userIdVsBossId, 
			Map<Long, Integer> userIdVsReferalAmount, Map<Long, Integer> userIdVsWinningAmount) {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		String timePattern = "dd:MMM:yy:HH:mm";
		logger.info("*******************************************************");
		logger.info("Batching payments for GameId#:" + gameDetails.getGameId() + ": client GameId#" + gameDetails.getTempGameId());
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
			
			if (profit < 0) {
				logger.info("****************** profit is negative: " + (profit));
				profit = 0;
			}
			
			long userProfileId = ps.getUserProfileId();
			userIdVsWinningAmount.put(userProfileId, profit);
			
			try {
				logger.info("Paying amount {} to id {} and userName {}", amountWon, userProfileId, userName);
				
				UserMoney userMoney = userIdVsUserMoney.get(userProfileId);
				if (userMoney == null) {
					logger.info("*****************************************");
					logger.info("User Money not found for userId : {}", userProfileId);
					logger.info("*****************************************");
					continue;
				}
				
				long userOB = userMoney.getAmount();
				long gameStartTime = gameDetails.getStartTime();
				String comments = "Winning Money for GameId#:" + gameDetails.getGameId();
				long userCB = userOB + amountWon; 
				
				
				MyTransaction transaction = Utils.getTransactionPojo(userProfileId, gameStartTime, 
						amountWon, TransactionType.CREDITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments, null);
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
					logger.info("Boss User Money not found for userId : {} with boss id : {}", userProfileId, bossUserProfileId);
					logger.info("*****************************************");
					continue;
				}
				
				userMoney = InMemUserMoneyManager.getInstance().getUserMoneyById(userProfileId);
				
				userOB = userMoney.getAmount();
				long bossOB = bossUserMoney.getAmount();
				
				gameStartTime = gameDetails.getStartTime();
				
		        simpleDateFormat.applyPattern(timePattern);
		        String timeStr = simpleDateFormat.format(new Date(gameDetails.getStartTime()));
		        
				comments = "Referrer share for GameId#" + gameDetails.getTempGameId() + " on " + timeStr; 
				String bossCmts = "For Referring, share from " + userName 
					+ " for GameId#" + gameDetails.getGameId() + " on " + timeStr;
				userCB = userOB - bossShare;
				long bossCB = bossOB + bossShare;
				
				transaction = Utils.getTransactionPojo(userProfileId, gameDetails.getStartTime(), 
						bossShare, TransactionType.DEBITED.getId(), 
						UserMoneyAccountType.WINNING_MONEY.getId(), userOB, userCB, comments, null);
				
				MyTransaction transaction1 = Utils.getTransactionPojo(bossUserProfileId, gameDetails.getStartTime(), 
								bossShare, TransactionType.CREDITED.getId(), 
								UserMoneyAccountType.REFERAL_MONEY.getId(), bossOB, bossCB, bossCmts, null);
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
				
				userIdVsWinningAmount.put(userProfileId, (profit - bossShare));
				Integer totalBossShare = userIdVsReferalAmount.get(bossProfileId);
				if (totalBossShare == null) {
					totalBossShare = new Integer(bossShare);
				} else {
					totalBossShare = totalBossShare + bossShare;
				}
				userIdVsReferalAmount.put(bossProfileId, totalBossShare);
				logger.info("userIdVsReferalAmount size {}", userIdVsReferalAmount);
				
			} catch (Exception ex) {
				logger.error("For user profile id " + userProfileId);
				logger.error("Exception while processing the payment", ex);
				continue;
			}
		}
	}
}
