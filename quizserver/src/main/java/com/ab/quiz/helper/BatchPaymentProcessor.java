package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;
import com.ab.quiz.tasks.CreateCustomerTickets;
import com.ab.quiz.tasks.WinnersMoneyUpdaterTask;

public class BatchPaymentProcessor {
	
	// This map maintains the UserId Vs Boss Id
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	private List<PaymentProcessor> paymentProcessors = new ArrayList<>();
	private long gameSlotsStartTime;
	private List<GameHandler> completedGameHandlers;
	
	private static final Logger logger = LogManager.getLogger(BatchPaymentProcessor.class);
	
	public BatchPaymentProcessor(List<GameHandler> completedGameHandlers) {
		this.completedGameHandlers = completedGameHandlers;
		for (GameHandler gh : completedGameHandlers) {
			if (gameSlotsStartTime == 0) {
				gameSlotsStartTime = gh.getGameDetails().getStartTime();
			}
		}
	}
	
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
			String logTag = TAGS.UPDATE_USER + " WinnersMoney : sid : " 
					+ QuizConstants.MY_SERVER_ID + " : SlotTime :" + new Date(gameSlotsStartTime).toString();
			winnerMoneyDetails.setLogTag(logTag);
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
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in bulk processing", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			
			List<CustomerTicket> ccTickets = new ArrayList<>(); 
			for (GameHandler cancelGH : completedGameHandlers) {
				HashMap<String,String> ccExtraDetailMap = new HashMap<>();
	            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(cancelGH.getGameDetails().getStartTime()).toString());
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_KEY, String.valueOf(cancelGH.getGameDetails().getTempGameId()));
	
	            String ccExtraDetails = CCUtils.encodeCCExtraValues(ccExtraDetailMap);
	            List<Long> enrolledUids = cancelGH.getEnrolledUserIds(); 
	            for (Long uid : enrolledUids) {
	            	ccTickets.add(CCUtils.createdCCTicket(CustomerCareReqType.WIN_MONEY_NOT_ADDED.getId(), 
	            			uid, ccExtraDetails));
	            }
			}
			
			if (ccTickets.size() > 0) {
				LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 15, TimeUnit.SECONDS);
			}

		}
	}

	public void setCompletedGameIds(List<Long> completedGameIds) {
		WinnersMoneyUpdaterTask.getInstance().addToInProgressGameIds(gameSlotsStartTime, completedGameIds);
	}

	public void setPaymentGD(List<PaymentGameDetails> paymentGameDetails) {
		WinnersMoneyUpdaterTask.getInstance().addToPaymentInProgressShortGameDetails(gameSlotsStartTime, paymentGameDetails);
	}
}
