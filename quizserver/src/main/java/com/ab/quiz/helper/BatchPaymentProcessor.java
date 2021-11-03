package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;

public class BatchPaymentProcessor {
	
	// This map maintains the UserId Vs Boss Id
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	private List<PaymentProcessor> paymentProcessors = new ArrayList<>();
	private long gameSlotsStartTime;
	
	private static final Logger logger = LogManager.getLogger(BatchPaymentProcessor.class);
	
	public void addUserBossIds(Map<Long,Long> gameUserBossIds) {
		userIdVsBossId.putAll(gameUserBossIds);
	}

	public void addPaymentProcessor(PaymentProcessor processor) {
		paymentProcessors.add(processor);
	}
	
	public void setGameSlotsStartTime(long slotStartTime) {
		this.gameSlotsStartTime = slotStartTime;
	}
	public long getGameSlotsStartTime() {
		return this.gameSlotsStartTime;
	}
	
	public void run() {
		try {
			long startTime = System.currentTimeMillis();
			
			UsersCompleteMoneyDetails winnerMoneyDetails = new UsersCompleteMoneyDetails();
			List<MoneyTransaction> winUsersTransactions = new ArrayList<>();

			for (PaymentProcessor processor : paymentProcessors) {
				processor.processPayments(userIdVsBossId, winUsersTransactions);
			}
			
			winnerMoneyDetails.setUsersMoneyTransactionList(winUsersTransactions);
			String trackKey = "server" + QuizConstants.MY_SERVER_ID + "-" + String.valueOf(gameSlotsStartTime);
			winnerMoneyDetails.setTrackStatusKey(trackKey);
			
			PostTask<UsersCompleteMoneyDetails, Integer[]> joinTask = Request.updateMoney();
			joinTask.setPostObject(winnerMoneyDetails);
			joinTask.execute();
			
			logger.info("Total Time in Run {}", (System.currentTimeMillis() - startTime));
		} catch (Exception ex) {
			logger.error("Exception in bulk processing", ex);
		}
	}
}
