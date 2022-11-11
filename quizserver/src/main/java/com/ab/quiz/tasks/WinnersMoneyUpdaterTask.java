package com.ab.quiz.tasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.WinMoneyCreditStatus;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.helper.CCUtils;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.PaymentGameDetails;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.SlotGamesWinMoneyStatus;

public class WinnersMoneyUpdaterTask implements Runnable {
	
	private static WinnersMoneyUpdaterTask instance = null;
	private static final Logger logger = LogManager.getLogger(WinnersMoneyUpdaterTask.class);
	
	private HashMap<String,Integer> moneyCreditedSatus = new HashMap<>();
	private HashMap<Long,Integer> slotGamePlayedTimeVsRetryCount = new HashMap<>();
	private Map<Long, List<Long>> slotGameStartTimeVsGameIds = new HashMap<>();
	private Map<Long, List<PaymentGameDetails>> slotGamesStartTimeVsPaymentGD = new HashMap<>();
	private String tag = "WinnerPayment";
	
	private WinnersMoneyUpdaterTask() {
	}
	
	public static WinnersMoneyUpdaterTask getInstance() {
		if (instance == null) {
			logger.info("This is in WinnersMoneyUpdaterTask getInstance");
			instance = new WinnersMoneyUpdaterTask(); 
		}
		return instance;
	}
	
	public void addToInProgressGameIds(Long slotGameStartTime, List<Long> gameIds) {
		slotGameStartTimeVsGameIds.put(slotGameStartTime, gameIds);
	}
	public void addToPaymentInProgressShortGameDetails(long slotGameStartTime, 
			List<PaymentGameDetails> paymentGD) {
		slotGamesStartTimeVsPaymentGD.put(slotGameStartTime, paymentGD);
	}
	
	
	@Override
	public void run() {
		try {
			String serverPrefixTrackKey = "server" + QuizConstants.MY_SERVER_ID;
			GetTask<SlotGamesWinMoneyStatus[]> slotGamesStatusTask = Request.getGamesSlotMoneyStatus(serverPrefixTrackKey);
			SlotGamesWinMoneyStatus[] thisServerStatus = (SlotGamesWinMoneyStatus[]) slotGamesStatusTask.execute();
			moneyCreditedSatus.clear();
			
			String datePattern = "dd:MMM:yyyy-HH:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
			simpleDateFormat.applyPattern(datePattern);
            
			
			Map<String,Integer> printInReadFormat = new HashMap<>();
			for (int index = 0; index < thisServerStatus.length; index++) {
				moneyCreditedSatus.put(thisServerStatus[index].getTrackKey(), thisServerStatus[index].getCreditedStatus());
				String timeStr = thisServerStatus[index].getTrackKey();
				timeStr = timeStr.substring(timeStr.indexOf('-') + 1);
				long timeLong = Long.parseLong(timeStr);
				timeStr = simpleDateFormat.format(new Date(timeLong));
				printInReadFormat.put(timeStr, thisServerStatus[index].getCreditedStatus());
			}
			logger.info("Money Credited Status for {} : {}", serverPrefixTrackKey, moneyCreditedSatus);
			logger.info("Money Credited Status for {} : {}", serverPrefixTrackKey, printInReadFormat);
			
			Set<Map.Entry<Long, List<Long>>> slotVsGameIdsSet = slotGameStartTimeVsGameIds.entrySet();
			List<Long> todelKeys = new ArrayList<>();
			for (Map.Entry<Long, List<Long>> eachEntry : slotVsGameIdsSet) {
				Integer status = moneyCreditedSatus.get(serverPrefixTrackKey + "-" + eachEntry.getKey());
				List<PaymentGameDetails> pgd = slotGamesStartTimeVsPaymentGD.get(eachEntry.getKey());
				StringBuilder strBuilder = new StringBuilder(serverPrefixTrackKey);
				strBuilder.append("-");
				strBuilder.append(eachEntry.getKey());
				if (status == null) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.info("Submitted slot payment record but did not get a reply for game time {} and game ids {}", new Date(eachEntry.getKey()), eachEntry.getValue());
					logger.error(QuizConstants.ERROR_PREFIX_END);
					todelKeys.add(eachEntry.getKey());
					moneyCreditedSatus.put(strBuilder.toString(), WinMoneyCreditStatus.ALL_FAIL.getId());
					missedWinnersPayment(eachEntry.getKey().toString(), pgd, WinMoneyCreditStatus.ALL_FAIL);
					continue;
				}
				if (status == WinMoneyCreditStatus.IN_PROGRESS.getId()) {
					Integer retryCount = slotGamePlayedTimeVsRetryCount.get(eachEntry.getKey());
					if (retryCount == null) {
						retryCount = new Integer(0);
					}
					retryCount++;
					slotGamePlayedTimeVsRetryCount.put(eachEntry.getKey(), retryCount);
					if (retryCount == 4) {
						logger.error(QuizConstants.ERROR_PREFIX_START);
						logger.info("Submitted slot payment record but retries timedout for game time {} and game ids {}", new Date(eachEntry.getKey()), eachEntry.getValue());
						logger.error(QuizConstants.ERROR_PREFIX_END);
						moneyCreditedSatus.put(strBuilder.toString(), WinMoneyCreditStatus.ALL_FAIL.getId());
						todelKeys.add(eachEntry.getKey());
						missedWinnersPayment(eachEntry.getKey().toString(), pgd, WinMoneyCreditStatus.ALL_FAIL);
					}
				} else if (status == WinMoneyCreditStatus.ALL_FAIL.getId()) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.info("Submitted slot payment record but all are failed for game time {} and game ids {}", new Date(eachEntry.getKey()), eachEntry.getValue());
					logger.error(QuizConstants.ERROR_PREFIX_END);
					moneyCreditedSatus.put(strBuilder.toString(), WinMoneyCreditStatus.ALL_FAIL.getId());	
					todelKeys.add(eachEntry.getKey());
					missedWinnersPayment(eachEntry.getKey().toString(), pgd, WinMoneyCreditStatus.ALL_FAIL);
				} else {
					moneyCreditedSatus.put(strBuilder.toString(), status);
					
					todelKeys.add(eachEntry.getKey());
					List<Long> gameIds = eachEntry.getValue();
					List<Integer> creditStatus = new ArrayList<>();
					for (int index = 0; index < gameIds.size(); index ++) {
						creditStatus.add(status);
					}
					if (gameIds.size() > 0) {
						GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
					}
				}
			}
			
			if (todelKeys.size() > 0) {
				// Delete the completed slot entries
				for (Long slotTime : todelKeys) {
					slotGamePlayedTimeVsRetryCount.remove(slotTime);
					slotGameStartTimeVsGameIds.remove(slotTime); 
					slotGamesStartTimeVsPaymentGD.remove(slotTime);
				}
			}
		} catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in run method while fetching the winners money update records from backend", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			Set<Long> slotVsGameIdsSet = slotGameStartTimeVsGameIds.keySet();
			for (Long eachEntry : slotVsGameIdsSet) {
				String newTrackKey = "server" + QuizConstants.MY_SERVER_ID + "-" + eachEntry; 
				moneyCreditedSatus.put(newTrackKey, WinMoneyCreditStatus.ALL_FAIL.getId());
				List<PaymentGameDetails> pgd = slotGamesStartTimeVsPaymentGD.get(eachEntry);
				missedWinnersPayment(String.valueOf(eachEntry), pgd, WinMoneyCreditStatus.ALL_FAIL);
			}
		}
		
	}
	
	public int getSlotGamesMoneyCreditedStatus(String trackKey) {
		Integer status = moneyCreditedSatus.get(trackKey);
		if (status == null) {
			String timeStr = trackKey;
			timeStr = timeStr.substring(timeStr.indexOf('-') + 1);
			long timeLong = Long.parseLong(timeStr);
			if (slotGameStartTimeVsGameIds.get(timeLong) == null) {
				// If all the games in a slot time are free or cancelled then this case comes
				return WinMoneyCreditStatus.ALL_SUCCESS.getId();
			}
			return WinMoneyCreditStatus.IN_PROGRESS.getId();
		} else { 
			return status;
		}
	}
	
	private void missedWinnersPayment(String slotTime, 
			List<PaymentGameDetails> paymentActionGD, WinMoneyCreditStatus gameCompletedStatus) {
		logger.info("{} The below games payment started. Setting the state to {} {}", tag, 
				gameCompletedStatus.getId(), gameCompletedStatus.name());
		
		List<CustomerTicket> ccTickets = new ArrayList<>();
		List<Integer> creditStatus = new ArrayList<>();
		List<Long> gameIds = new ArrayList<>();
		
		try {
		
			for (PaymentGameDetails completedGame : paymentActionGD) {
				
				gameIds.add(completedGame.getServerGameId());
				creditStatus.add(gameCompletedStatus.getId());
				
				HashMap<String,String> ccExtraDetailMap = new HashMap<>();
	            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(completedGame.getGameStartTime()).toString());
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_KEY, String.valueOf(completedGame.getClientGameId()));
	
	            String ccExtraDetails = CCUtils.encodeCCExtraValues(ccExtraDetailMap);
	            List<Long> enrolledUids = completedGame.getEnrolledUids(); 
	            for (Long uid : enrolledUids) {
	            	ccTickets.add(CCUtils.createdCCTicket(CustomerCareReqType.WIN_MONEY_NOT_ADDED.getId(), 
	            			uid, ccExtraDetails));
	            }
	
			}
			
			LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 15, TimeUnit.SECONDS);
			
			if (gameIds.size() > 0) {
				GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
			}
		
		} catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while fetching the winners money update records from backend", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
		
	}
}

