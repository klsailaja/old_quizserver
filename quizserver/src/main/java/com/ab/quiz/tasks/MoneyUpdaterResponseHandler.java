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
import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.MoneyPayBackMode;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.MoneyCreditStatus;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.helper.CCUtils;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.pojo.CancelGameRefundStatus;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.GameSlotMoneyStatus;
import com.ab.quiz.pojo.MoneyUpdaterGameDetails;

public class MoneyUpdaterResponseHandler implements Runnable {

	private static final Logger logger = LogManager.getLogger(MoneyUpdaterResponseHandler.class);
	private static MoneyUpdaterResponseHandler instance = null;
	
	private HashMap<String,Integer> winMoneyCreditedSatus = new HashMap<>();
	// for cancelled games
	private HashMap<String,Integer> refundMoneyCreditedSatus = new HashMap<>();
	private Map<Long, List<MoneyUpdaterGameDetails>> slotGamesStartTimeVsPaymentGD = new HashMap<>();
	 
	
	private HashMap<Long,Integer> slotGamePlayedTimeVsRetryCount = new HashMap<>();
	private HashMap<Long,Integer> slotGamePlayedTimeVsOperationType = new HashMap<>();
	
	
	private MoneyUpdaterResponseHandler() {
	}
	
	public static MoneyUpdaterResponseHandler getInstance() {
		if (instance == null) {
			logger.info("This is in MoneyUpdaterResponse getInstance");
			instance = new MoneyUpdaterResponseHandler(); 
		}
		return instance;
	}
	
	public void addToPaymentInProgressShortGameDetails(long slotGameStartTime, 
			List<MoneyUpdaterGameDetails> paymentGD, int operationType) {
		
		slotGamePlayedTimeVsOperationType.put(slotGameStartTime, operationType);
		slotGamesStartTimeVsPaymentGD.put(slotGameStartTime, paymentGD);
	}
	
	@Override
	public void run() {
		try {
			String serverPrefixTrackKey = "server" + QuizConstants.MY_SERVER_ID;
			
			GetTask<GameSlotMoneyStatus[]> slotGamesStatusTask = Request.getGamesSlotMoneyStatus(serverPrefixTrackKey);
			GameSlotMoneyStatus[] thisServerStatus = (GameSlotMoneyStatus[]) slotGamesStatusTask.execute();
			
			String datePattern = "dd:MMM:yyyy-HH:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
			simpleDateFormat.applyPattern(datePattern);
			
			Map<String,Integer> winGamesprintInReadFormat = new HashMap<>();
			Map<String,Integer> refundGamesprintInReadFormat = new HashMap<>();
			HashMap<String,GameSlotMoneyStatus> serverKeyVsGameSlotMoneyStatus = new HashMap<>();
			
			for (int index = 0; index < thisServerStatus.length; index++) {
				GameSlotMoneyStatus gameStatusObj = thisServerStatus[index];
				serverKeyVsGameSlotMoneyStatus.put(gameStatusObj.getTrackKey(), gameStatusObj);
				String timeStr = thisServerStatus[index].getTrackKey();
				timeStr = timeStr.substring(timeStr.indexOf('-') + 1);
				long timeLong = Long.parseLong(timeStr);
				timeStr = simpleDateFormat.format(new Date(timeLong));
				if (gameStatusObj.getOperationType() == 1) {
					// Winners Money
					winGamesprintInReadFormat.put(timeStr, gameStatusObj.getMoneyCreditedStatus());
				} else if (gameStatusObj.getOperationType() == 2) {
					// Cancel games Refund Money
					refundGamesprintInReadFormat.put(timeStr, gameStatusObj.getMoneyCreditedStatus());
				}
			}
			logger.info("{} Money Credited Status for {} : {}", TAGS.WIN_MONEY, 
					serverPrefixTrackKey, winMoneyCreditedSatus);
			logger.info("{} Money Credited Status for {} : {}", TAGS.WIN_MONEY, 
					serverPrefixTrackKey, winGamesprintInReadFormat);
			logger.info("{} Money Refund Status for {} : {}", TAGS.REFUND_MONEY, 
					serverPrefixTrackKey, refundMoneyCreditedSatus);
			logger.info("{} Money Refund Status for {} : {}", TAGS.REFUND_MONEY, 
					serverPrefixTrackKey, refundGamesprintInReadFormat);
			
			List<Long> todelKeys = new ArrayList<>();
			Set<Map.Entry<Long, Integer>> slotVsOperationType = 
					slotGamePlayedTimeVsOperationType.entrySet(); 
			
			 
			for (Map.Entry<Long, Integer> eachEntry : slotVsOperationType) {
				
				long gameSlotTime = eachEntry.getKey();
				int operationType = eachEntry.getValue();
				
				String gameSlotKey = serverPrefixTrackKey + "-" + eachEntry.getKey();
				String tag = TAGS.WIN_MONEY;
				if (operationType == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
					tag = TAGS.REFUND_MONEY;
				}
				
				HashMap<String,Integer> currentMap = winMoneyCreditedSatus;
				if (operationType == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
					currentMap = refundMoneyCreditedSatus;
				}
				
				Integer status = currentMap.get(gameSlotKey);
				List<MoneyUpdaterGameDetails> gameSlotGameDetails = 
						slotGamesStartTimeVsPaymentGD.get(gameSlotTime);
				String printGameIdsStr = getPrintableGameIdsStr(gameSlotGameDetails);
				
				
				if (status == null) {
					todelKeys.add(gameSlotTime);
					currentMap.put(gameSlotKey, MoneyCreditStatus.ALL_FAIL.getId());
					recordMissing(gameSlotTime, 
							gameSlotGameDetails, MoneyCreditStatus.ALL_FAIL, operationType, 
							tag, printGameIdsStr);
					continue;
				} 
				else if (status == MoneyCreditStatus.IN_PROGRESS.getId()) {
					Integer retryCount = slotGamePlayedTimeVsRetryCount.get(gameSlotTime);
					if (retryCount == null) {
						retryCount = new Integer(0);
					}
					retryCount++;
					slotGamePlayedTimeVsRetryCount.put(gameSlotTime, retryCount);
					if (retryCount == 4) {
						logger.error(QuizConstants.ERROR_PREFIX_START);
						logger.info("{} Submitted slot payment record but retries timedout for game time {} and game ids {}",
								tag,
								new Date(gameSlotTime), printGameIdsStr);
						logger.error(QuizConstants.ERROR_PREFIX_END);
						todelKeys.add(gameSlotTime);
						currentMap.put(gameSlotKey, MoneyCreditStatus.ALL_FAIL.getId());
						missedPayment(gameSlotTime, gameSlotGameDetails, 
								MoneyCreditStatus.ALL_FAIL, operationType, tag);
					}
				} else if (status == MoneyCreditStatus.ALL_FAIL.getId()) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.info("{} Submitted slot payment record but all failed for game time {} and game ids {}",
							tag,
							new Date(gameSlotTime), printGameIdsStr);
					logger.error(QuizConstants.ERROR_PREFIX_END);
					todelKeys.add(gameSlotTime);
					currentMap.put(gameSlotKey, MoneyCreditStatus.ALL_FAIL.getId());
					missedPayment(gameSlotTime, gameSlotGameDetails, 
							MoneyCreditStatus.ALL_FAIL, operationType, tag);
				} else if (status == MoneyCreditStatus.ALL_SUCCESS.getId()) {
					todelKeys.add(gameSlotTime);
					currentMap.put(gameSlotKey, MoneyCreditStatus.ALL_SUCCESS.getId());
					
					List<Long> gameIds = new ArrayList<>();
					for (MoneyUpdaterGameDetails gd : gameSlotGameDetails) {
						if (gameIds.contains(gd.getGameServerId())) {
							gameIds.add(gd.getGameServerId());
						}
					}
					
					List<Integer> creditStatus = new ArrayList<>();
					for (int index = 0; index < gameIds.size(); index ++) {
						creditStatus.add(status);
					}
					if (gameIds.size() > 0) {
						GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
					}
					
				} else if (status == MoneyCreditStatus.PARTIAL_RESULTS.getId()) {
					todelKeys.add(gameSlotTime);
					currentMap.put(gameSlotKey, MoneyCreditStatus.ALL_FAIL.getId());
					
					List<Long> gameIds = new ArrayList<>();
					for (MoneyUpdaterGameDetails gd : gameSlotGameDetails) {
						if (!gameIds.contains(gd.getGameServerId())) {
							gameIds.add(gd.getGameServerId());
						}
					}
					
					List<Long> patialSuccessGameIds = new ArrayList<>();
					GameSlotMoneyStatus responseObj = serverKeyVsGameSlotMoneyStatus.get(gameSlotKey);
					
					int tktType = CustomerCareReqType.ADDED_MONEY_NOT_UPDATED.getId();
					if (operationType == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
						tktType = CustomerCareReqType.CANCELLED_GAME_MONEY_NOT_ADDED.getId();
					}
					
					List<CustomerTicket> ccTickets = new ArrayList<>();
					List<Integer> responseUniqueIds = responseObj.getUniqueIds();
					List<Integer> responseDBResults = responseObj.getDbResultsIds();
					for (int resultIndex = 0; resultIndex < responseDBResults.size(); resultIndex++) {
						if (responseDBResults.get(resultIndex) == 0) {
							for (MoneyUpdaterGameDetails matchInputObj : gameSlotGameDetails) {
								if (matchInputObj.getUniqueId() == responseUniqueIds.get(resultIndex)) {
									if (!patialSuccessGameIds.contains(matchInputObj.getGameServerId())) {
										patialSuccessGameIds.add(matchInputObj.getGameServerId());
									}
								}
								matchInputObj.setCreditResult(-1);
								
								HashMap<String,String> ccExtraDetailMap = new HashMap<>();
					            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(matchInputObj.getGameStartTime()).toString());
					            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_KEY, String.valueOf(matchInputObj.getGameClientId()));
					            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_SERVER_KEY, String.valueOf(matchInputObj.getGameServerId()));
					            ccExtraDetailMap.put(CCUtils.ISSUE_AMT_KEY, String.valueOf(matchInputObj.getAmount()));
					            
						        String ccExtraDetails = CCUtils.encodeCCExtraValues(ccExtraDetailMap);
						        ccTickets.add(CCUtils.createdCCTicket(tktType, matchInputObj.getUserId(), ccExtraDetails));
							}
						}
					}
					
					List<Integer> creditStatus = new ArrayList<>();
					for (int index = 0; index < gameIds.size(); index ++) {
						if (patialSuccessGameIds.contains(gameIds.get(index))) {
							creditStatus.add(MoneyCreditStatus.ALL_FAIL.getId());
						} else {
							creditStatus.add(MoneyCreditStatus.ALL_SUCCESS.getId());
						}
					}
					if (gameIds.size() > 0) {
						GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
					}
					
					if (ccTickets.size() > 0) {
						LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 15, TimeUnit.SECONDS);
					}
				}
				
				logger.info("{} Post Money Credited Status for {} : {}", TAGS.WIN_MONEY, 
						serverPrefixTrackKey, winMoneyCreditedSatus);
				logger.info("{} Post Money Credited Status for {} : {}", TAGS.WIN_MONEY, 
						serverPrefixTrackKey, winGamesprintInReadFormat);
				logger.info("{} Post Money Refund Status for {} : {}", TAGS.REFUND_MONEY, 
						serverPrefixTrackKey, refundMoneyCreditedSatus);
				logger.info("{} Post Money Refund Status for {} : {}", TAGS.REFUND_MONEY, 
						serverPrefixTrackKey, refundGamesprintInReadFormat);
				
				if (todelKeys.size() > 0) {
					// Delete the completed slot entries
					for (Long slotTime : todelKeys) {
						slotGamePlayedTimeVsRetryCount.remove(slotTime);
						slotGamePlayedTimeVsOperationType.remove(slotTime); 
					}
				}
				
				cleanupOldEntries();
			}
		}
		catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in run method while fetching the winners money update records from backend", 
					TAGS.WIN_MONEY);
			logger.error(ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
	
	private void recordMissing(long gameStartTime, List<MoneyUpdaterGameDetails> gameUpdateDetails,
			MoneyCreditStatus gameCompletedStatus, int operationType, 
			String tagName, String printGameInfo) {
		
		logger.error(QuizConstants.ERROR_PREFIX_START);
		logger.info("{} Staus missing for slotTime {} and game ids {}", tagName, 
				new Date(gameStartTime), printGameInfo);
		logger.error(QuizConstants.ERROR_PREFIX_END);
		missedPayment(gameStartTime, gameUpdateDetails, 
				MoneyCreditStatus.ALL_FAIL, operationType, tagName);
	}
	
	private void missedPayment(long slotTime, 
			List<MoneyUpdaterGameDetails> paymentGD, 
			MoneyCreditStatus gameCompletedStatus,
			int operationType, String tag) {
		
		logger.info("{} The below games payment started. Setting the state to {} {}", tag, 
				gameCompletedStatus.getId(), gameCompletedStatus.name());
		
		int tktType = CustomerCareReqType.WIN_MONEY_NOT_ADDED.getId();
		if (operationType == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tktType = CustomerCareReqType.CANCELLED_GAME_MONEY_NOT_ADDED.getId();
		}
		
		List<CustomerTicket> ccTickets = new ArrayList<>();
		List<Integer> creditStatus = new ArrayList<>();
		List<Long> gameIds = new ArrayList<>();
		
		try {
		
			for (MoneyUpdaterGameDetails gd : paymentGD) {
				
				if (!gameIds.contains(gd.getGameServerId())) {
					gameIds.add(gd.getGameServerId());
					creditStatus.add(gameCompletedStatus.getId());
				}
				
				HashMap<String,String> ccExtraDetailMap = new HashMap<>();
	            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(slotTime).toString());
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_KEY, String.valueOf(gd.getGameClientId()));
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_SERVER_KEY, String.valueOf(gd.getGameServerId()));
	            ccExtraDetailMap.put(CCUtils.ISSUE_AMT_KEY, String.valueOf(gd.getAmount()));
	            
		        String ccExtraDetails = CCUtils.encodeCCExtraValues(ccExtraDetailMap);
		        ccTickets.add(CCUtils.createdCCTicket(tktType, gd.getUserId(), ccExtraDetails)); 
			}
			
			LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 15, TimeUnit.SECONDS);
			
			if (gameIds.size() > 0) {
				GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
			}
		
		} catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception handling missed status case", tag);
			logger.error(ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
	
	private String getPrintableGameIdsStr(List<MoneyUpdaterGameDetails> pgds) {
		Map<Long, Integer> uniqueList = new HashMap<>();
		
		StringBuilder strBuilder = new StringBuilder();
		for (MoneyUpdaterGameDetails gd : pgds) {
			Integer mark = uniqueList.get(gd.getGameServerId());
			if (mark == null) {
				uniqueList.put(gd.getGameServerId(), 0);
			} else {
				continue;
			}
			strBuilder.append("Server Game Id:");
			strBuilder.append(gd.getGameServerId());
			strBuilder.append(" ");
			strBuilder.append("Client Game Id:");
			strBuilder.append(gd.getGameClientId());
			strBuilder.append(" ");
		}
		return strBuilder.toString();
	}
	
	public int getWinMoneyCreditedStatus(String trackKey) {
		Integer status = winMoneyCreditedSatus.get(trackKey);
		if (status == null) {
			String timeStr = trackKey;
			timeStr = timeStr.substring(timeStr.indexOf('-') + 1);
			long timeLong = Long.parseLong(timeStr);
			if (slotGamesStartTimeVsPaymentGD.get(timeLong) == null) {
				// If all the games in a slot time are free or cancelled then this case comes
				return MoneyCreditStatus.ALL_SUCCESS.getId();
			}
			return MoneyCreditStatus.IN_PROGRESS.getId();
		} else { 
			return status;
		}
	}
	
	public CancelGameRefundStatus getGameRefundStatus(String trackKey, long uid, 
			long gameSlotStartTime) {
		
		List<MoneyUpdaterGameDetails> list = slotGamesStartTimeVsPaymentGD.get(gameSlotStartTime);
		
		CancelGameRefundStatus refundStatus = new CancelGameRefundStatus();
		refundStatus.setUid(uid);
		if (list == null) {
			refundStatus.setStatus(MoneyCreditStatus.ALL_FAIL.getId());
			return refundStatus;
		} else {
			for (MoneyUpdaterGameDetails obj : list) {
				if (obj.getUserId() == uid) {
					refundStatus.setClientGameId(obj.getGameClientId());
					refundStatus.setStatus(obj.getCreditResult());
				}
			}
		}
		
		Integer status = refundMoneyCreditedSatus.get(trackKey);
		if (status == null) {
			refundStatus.setStatus(MoneyCreditStatus.IN_PROGRESS.getId());
		} else if (status != MoneyCreditStatus.PARTIAL_RESULTS.getId()) {
			refundStatus.setStatus(status);
		}
		
		return refundStatus;
	}
	
	private void cleanupOldEntries() {
		Set<Map.Entry<String, Integer>> slotVsWinMoneySet = winMoneyCreditedSatus.entrySet();
		List<String> toDelKeys = new ArrayList<>();
		for (Map.Entry<String, Integer> eachEntry : slotVsWinMoneySet) {
			String timeStr = eachEntry.getKey();
			timeStr = timeStr.substring(timeStr.lastIndexOf('-') + 1);
			long timeLong = Long.parseLong(timeStr);
			long currentTime = System.currentTimeMillis();
			if ((currentTime - timeLong) >= 10 * 60 * 1000) {
				toDelKeys.add(eachEntry.getKey());
			}
		}
		
		for (String delKey : toDelKeys) {
			winMoneyCreditedSatus.remove(delKey);
		}
		toDelKeys.clear();
		
		Set<Map.Entry<String, Integer>> slotVsCancelMoneySet = refundMoneyCreditedSatus.entrySet();
		for (Map.Entry<String, Integer> eachEntry : slotVsCancelMoneySet) {
			String timeStr = eachEntry.getKey();
			timeStr = timeStr.substring(timeStr.lastIndexOf('-') + 1);
			long timeLong = Long.parseLong(timeStr);
			long currentTime = System.currentTimeMillis();
			if ((currentTime - timeLong) >= 10 * 60 * 1000) {
				toDelKeys.add(eachEntry.getKey());
			}
		}
		for (String delKey : toDelKeys) {
			refundMoneyCreditedSatus.remove(delKey);
		}
		toDelKeys.clear();
		
		Set<Map.Entry<Long, List<MoneyUpdaterGameDetails>>> slotVsGD = 
				slotGamesStartTimeVsPaymentGD.entrySet();
		List<Long> toDelLongKeys = new ArrayList<>();
		for (Map.Entry<Long, List<MoneyUpdaterGameDetails>> eachEntry : slotVsGD) {
			long timeLong = eachEntry.getKey();
			long currentTime = System.currentTimeMillis();
			if ((currentTime - timeLong) >= 10 * 60 * 1000) {
				toDelLongKeys.add(eachEntry.getKey());
			}
		}
		for (Long delKey : toDelLongKeys) {
			slotGamesStartTimeVsPaymentGD.remove(delKey);
		}
		toDelLongKeys.clear();
	}
}
