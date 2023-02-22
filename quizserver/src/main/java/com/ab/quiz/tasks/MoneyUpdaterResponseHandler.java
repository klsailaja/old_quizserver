package com.ab.quiz.tasks;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.MoneyCreditStatus;
import com.ab.quiz.constants.MoneyPayBackMode;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.helper.CCUtils;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.pojo.ClientSlotMoneyStatusGiver;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.GameSlotMoneyStatus;
import com.ab.quiz.pojo.MoneyStatusInput;
import com.ab.quiz.pojo.MoneyStatusOutput;
import com.ab.quiz.pojo.MoneyUpdaterGameDetails;

public class MoneyUpdaterResponseHandler implements Runnable {

	private static final Logger logger = LogManager.getLogger(MoneyUpdaterResponseHandler.class);
	private static MoneyUpdaterResponseHandler instance = null;
	
	private HashMap<Integer,Integer> requestIdVsRetryCount = new HashMap<>();
	
	private List<ClientSlotMoneyStatusGiver> clientServers = new ArrayList<>();
	private List<ClientSlotMoneyStatusGiver> freeGameServers = new ArrayList<>();
	
	private String WIN_MONEY_SUCCESS_MSG = "Win Money Amount Rs.%d for GameId: %s Credited successfuly .";
	private String WIN_MONEY_FAIL_MSG = "Win Money Amount Rs.%d for GameId: %s NOT Credited successfully. \n" 
					+ "Customer Ticket Created for this issue. Please verify in Customer Tickets view";
	private String WIN_MONEY_FREE_GAME_MSG = "No Win Money for free game. Win Money Credit result will be shown for paid games. \n"
					+ "If not credited for some reason, Customer Ticket is created automatically and \n"
					+ " resolved within 3-5 days";
	
	private String REFUND_SUCCESS_MSG = "Game with GameId: %s Cancelled as min 3 users not joined. \n" 
			+ "Ticket Money Amount Rs.%d Credited successfuly.";
	private String REFUND_FAIL_MSG = "Game with GameId: %s Cancelled as min 3 users not joined. \n" 
			+ "Ticket Money Amount Rs.%d NOT credited successfully. \n"
			+ "Customer Ticket Created for this issue. Please verify in Customer Tickets view";
	
	private MoneyUpdaterResponseHandler() {
	}
	
	public static MoneyUpdaterResponseHandler getInstance() {
		if (instance == null) {
			logger.info("This is in MoneyUpdaterResponse getInstance");
			instance = new MoneyUpdaterResponseHandler(); 
		}
		return instance;
	}
	
	public void addtoMoneyUpdateQueue(ClientSlotMoneyStatusGiver statusServer, ClientSlotMoneyStatusGiver freeGameServer) {
		statusServer.setMoneyOverallStatus(MoneyCreditStatus.IN_PROGRESS.getId());
		clientServers.add(statusServer);
		
		if (freeGameServer != null) {
			freeGameServer.setMoneyOverallStatus(MoneyCreditStatus.IN_PROGRESS.getId());
			freeGameServers.add(freeGameServer);
		}
	}
	
	@Override
	public void run() {
		try {
			GetTask<GameSlotMoneyStatus[]> slotGamesStatusTask = Request.getGamesSlotMoneyStatus(QuizConstants.MY_SERVER_ID);
			GameSlotMoneyStatus[] thisServerStatus = (GameSlotMoneyStatus[]) slotGamesStatusTask.execute();
			
			for (ClientSlotMoneyStatusGiver statusGiver : clientServers) {
				if (statusGiver.getProcessedTime() > 0) {
					continue;
				}
				
				boolean isRecordFound = false;
				for (int index = 0; index < thisServerStatus.length; index ++) {
					GameSlotMoneyStatus statusObj = thisServerStatus[index]; 
					if (statusObj.getRequestId() == statusGiver.getRequestId()) {
						isRecordFound = true;
						int requestObjectStatus = statusObj.getMoneyCreditedStatus();
						statusGiver.setMoneyOverallStatus(requestObjectStatus);
						
						if (requestObjectStatus == MoneyCreditStatus.IN_PROGRESS.getId()) {
							handleInProgressState(statusGiver, statusObj);
						} else if (requestObjectStatus == MoneyCreditStatus.ALL_FAIL.getId()) {
							handleAllFailState(statusGiver, statusObj);
						} else if (requestObjectStatus == MoneyCreditStatus.ALL_SUCCESS.getId()) {
							handleAllSuccessState(statusGiver, statusObj);
						} else if (requestObjectStatus == MoneyCreditStatus.PARTIAL_RESULTS.getId()) {
							handlePartialCase(statusGiver, statusObj);
						}
					}
				}
				if (!isRecordFound) {
					statusGiver.setMoneyOverallStatus(MoneyCreditStatus.IN_PROGRESS.getId());
					handleInProgressState(statusGiver, null);
				}
			}
			
			printStatus();
			cleanupOldEntries();
		}
		catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Payment Exception in run method while fetching the money update records from backend", 
					TAGS.WIN_MONEY);
			logger.error(ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
	
	private void printStatus() {
		StringBuilder winMoneyDetais = new StringBuilder();
		StringBuilder refundMoneyDetais = new StringBuilder();
		String datePattern = "dd:MMM:yyyy-HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern(datePattern);
		
		for (ClientSlotMoneyStatusGiver statusGiver : clientServers) {
			StringBuilder strBuillder = winMoneyDetais;
			if (statusGiver.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
				strBuillder = refundMoneyDetais;
			}
			strBuillder.append("Request Id:");
			strBuillder.append(statusGiver.getRequestId());
			strBuillder.append(" Game Slot Time:");
			strBuillder.append(simpleDateFormat.format(new Date(statusGiver.getGameSlotTime())));
			strBuillder.append(" Money Credited Status:");
			strBuillder.append(statusGiver.getMoneyOverallStatus());
			strBuillder.append(" Status Elapsed Time:");
			strBuillder.append(simpleDateFormat.format(new Date(statusGiver.getProcessedTime() + (10 * 60 * 1000))));
		}
		logger.info("{} Payment prints {}", TAGS.WIN_MONEY, winMoneyDetais.toString());
		logger.info("{} Payment prints {}", TAGS.REFUND_MONEY, refundMoneyDetais.toString());
	}
	
	private void handlePartialCase(ClientSlotMoneyStatusGiver statusServer, GameSlotMoneyStatus statusObject) 
			throws SQLException {
		
		String tag = TAGS.WIN_MONEY;
		if (statusServer.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tag = TAGS.REFUND_MONEY;
		}
		statusServer.setProcessedTime(System.currentTimeMillis());
		
		List<Long> gameIds = new ArrayList<>();
		for (MoneyUpdaterGameDetails gd : statusServer.getSlotMoneyGD()) {
			if (!gameIds.contains(gd.getGameServerId())) {
				gameIds.add(gd.getGameServerId());
			}
		}
		
		List<Long> patialSuccessGameIds = new ArrayList<>();
		
		int tktType = CustomerCareReqType.ADDED_MONEY_NOT_UPDATED.getId();
		if (statusServer.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tktType = CustomerCareReqType.CANCELLED_GAME_MONEY_NOT_ADDED.getId();
		}
		
		List<CustomerTicket> ccTickets = new ArrayList<>();
		List<Integer> responseUniqueIds = statusObject.getUniqueIds();
		List<Integer> responseDBResults = statusObject.getDbResultsIds();
		for (int resultIndex = 0; resultIndex < responseDBResults.size(); resultIndex++) {
			if (responseDBResults.get(resultIndex) == 0) {
				for (MoneyUpdaterGameDetails matchInputObj : statusServer.getSlotMoneyGD()) {
					if (matchInputObj.getUniqueId() == responseUniqueIds.get(resultIndex)) {
						if (!patialSuccessGameIds.contains(matchInputObj.getGameServerId())) {
							patialSuccessGameIds.add(matchInputObj.getGameServerId());
						}
					}
					matchInputObj.setCreditResult(MoneyCreditStatus.ALL_FAIL.getId());
					
					HashMap<String,String> ccExtraDetailMap = new HashMap<>();
		            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(statusServer.getGameSlotTime()).toString());
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
			LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 5, TimeUnit.SECONDS);
		}
		
	}
	
	private void handleAllSuccessState(ClientSlotMoneyStatusGiver statusServer, GameSlotMoneyStatus statusObject) 
			throws SQLException {
		
		String tag = TAGS.WIN_MONEY;
		if (statusServer.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tag = TAGS.REFUND_MONEY;
		}
		statusServer.setProcessedTime(System.currentTimeMillis());
		
		String printGameIdsStr = getPrintableGameIdsStr(statusServer.getSlotMoneyGD());
		logger.info("{} Payment All success for game time {} and game ids {}",
				tag,
				new Date(statusServer.getGameSlotTime()), printGameIdsStr);
		List<Long> gameIds = new ArrayList<>();
		for (MoneyUpdaterGameDetails gd : statusServer.getSlotMoneyGD()) {
			if (gameIds.contains(gd.getGameServerId())) {
				gameIds.add(gd.getGameServerId());
			}
		}
		
		List<Integer> creditStatus = new ArrayList<>();
		for (int index = 0; index < gameIds.size(); index ++) {
			creditStatus.add(statusServer.getMoneyOverallStatus());
		}
		
		if (gameIds.size() > 0) {
			GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
		}
	}
	
	private void handleAllFailState(ClientSlotMoneyStatusGiver statusServer, GameSlotMoneyStatus statusObject) {
		String tag = TAGS.WIN_MONEY;
		if (statusServer.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tag = TAGS.REFUND_MONEY;
		}
		String printGameIdsStr = getPrintableGameIdsStr(statusServer.getSlotMoneyGD());
		logger.error(QuizConstants.ERROR_PREFIX_START);
		logger.info("{} Payment All failed for game time {} and game ids {}",
				tag,
				new Date(statusServer.getGameSlotTime()), printGameIdsStr);
		logger.error(QuizConstants.ERROR_PREFIX_END);
		statusServer.setProcessedTime(System.currentTimeMillis());
		missedPayment(statusServer.getGameSlotTime(), statusServer.getSlotMoneyGD(), 
				MoneyCreditStatus.ALL_FAIL, statusServer.getOperationType(), tag);

	}
	
	private void handleInProgressState(ClientSlotMoneyStatusGiver statusServer, GameSlotMoneyStatus statusObject) {
		int requestId = statusServer.getRequestId();
		Integer retryCount = requestIdVsRetryCount.get(requestId);
		if (retryCount == null) {
			retryCount = new Integer(0);
		}
		retryCount++;
		requestIdVsRetryCount.put(requestId, retryCount);
		String tag = TAGS.WIN_MONEY;
		if (statusServer.getOperationType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tag = TAGS.REFUND_MONEY;
		}
		if (retryCount == 5) {
			String printGameIdsStr = getPrintableGameIdsStr(statusServer.getSlotMoneyGD());
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.info("{} Payment Timeout for retries for game time {} and game ids {}",
					tag,
					new Date(statusServer.getGameSlotTime()), printGameIdsStr);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			
			statusServer.setProcessedTime(System.currentTimeMillis());
			missedPayment(statusServer.getGameSlotTime(), statusServer.getSlotMoneyGD(), 
					MoneyCreditStatus.ALL_FAIL, statusServer.getOperationType(), tag);
		}
		
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
			
			LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 5, TimeUnit.SECONDS);
			
			if (gameIds.size() > 0) {
				GameHistoryDBHandler.getInstance().bulkUpdateStatus(gameIds, creditStatus, 5);
			}
		
		} catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception handling timeout status case", tag);
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
	
	public MoneyStatusOutput getStatus(MoneyStatusInput input) {
		
		logger.info("In get Status");
		MoneyStatusOutput output = new MoneyStatusOutput();
		output.setMessage(null);
		output.setStatus(-1);
		
		if (QuizConstants.getBackServerStatus()) {
			output.setStatus(MoneyCreditStatus.ALL_FAIL.getId());
			if (input.getOperType() == MoneyPayBackMode.WIN_MONEY.getId()) {
				output.setMessage(WIN_MONEY_FAIL_MSG);
			} else {
				output.setMessage(REFUND_FAIL_MSG);
			}
			logger.info("In back server status down");
			return output;
		}
		
		if (input.getOperType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(input.getGameSlotTime());
			
			int minute = calendar.get(Calendar.MINUTE);
			int gameMode = -1;  
			if (minute % 2 == 0) {
				gameMode = 1;
			} else {
				gameMode = 2;
			}
			try {
				GameManager.getInstance().processCancelGamesRefund(gameMode);
			} catch (Exception ex) {
				logger.error(QuizConstants.ERROR_PREFIX_START);
				logger.error("{} Payment Exception in run method while fetching the money update records from backend", 
						TAGS.WIN_MONEY);
				logger.error(ex);
				logger.error(QuizConstants.ERROR_PREFIX_END);
				return output;
			}
		}
		
		if (input.getOperType() == MoneyPayBackMode.WIN_MONEY.getId()) {
			// Display a message for free game in win money category
			for (ClientSlotMoneyStatusGiver freeGameHandler : freeGameServers) {
				if (freeGameHandler.getOperationType() == input.getOperType()) {
					long timeDiff = freeGameHandler.getGameSlotTime() - input.getGameSlotTime();
					if (timeDiff < 0) {
						timeDiff = -1 * timeDiff;
					}
					if (timeDiff <= (10 * 1000)) {
						List<MoneyUpdaterGameDetails> gdDetails = freeGameHandler.getSlotMoneyGD();
						for (MoneyUpdaterGameDetails gdObject : gdDetails) {
							if (gdObject.getUserId() == input.getUid()) {
								output.setStatus(MoneyCreditStatus.ALL_SUCCESS.getId());
								output.setMessage(WIN_MONEY_FREE_GAME_MSG);
								
								output.setGamePlayedTime(freeGameHandler.getGameSlotTime());
								
								output.setServerGameId(gdObject.getGameServerId());
								output.setClientGameId(gdObject.getGameClientId());
								output.setAmount(gdObject.getAmount());
								
								logger.info("In win money free game");
								return output;
							}
						}
					}
				}
			}
		}
		
		
		for (ClientSlotMoneyStatusGiver statusServer : clientServers) {
			logger.info("In real status");
			if (statusServer.getOperationType() == input.getOperType()) {
				logger.info("In timeDiff : {} and {}" + statusServer.getGameSlotTime(), input.getGameSlotTime());
				logger.info("In timeDiff : {} and {}" + new Date(statusServer.getGameSlotTime()), 
						new Date(input.getGameSlotTime()));
				long timeDiff = statusServer.getGameSlotTime() - input.getGameSlotTime();
				if (timeDiff < 0) {
					timeDiff = -1 * timeDiff;
				}
				logger.info("In timeDiff :" + timeDiff);
				if (timeDiff <= (10 * 1000)) {
					logger.info("In timeDiff < 10 sec:" + timeDiff);
					for (MoneyUpdaterGameDetails gd : statusServer.getSlotMoneyGD()) {
						if (gd.getUserId() == input.getUid()) {
							logger.info("Found matching entry:" + input.getUid());
							int status = statusServer.getMoneyOverallStatus();
							if (status == MoneyCreditStatus.PARTIAL_RESULTS.getId()) {
								status = gd.getCreditResult();
							}
							output.setStatus(status);
							if (status == MoneyCreditStatus.IN_PROGRESS.getId()) {
								output.setMessage(null);
							} else if (status == MoneyCreditStatus.ALL_SUCCESS.getId()) {
								if (input.getOperType() == MoneyPayBackMode.WIN_MONEY.getId()) {
									output.setMessage(String.format(WIN_MONEY_SUCCESS_MSG, gd.getAmount(), String.valueOf(gd.getGameClientId())));
								} else if (input.getOperType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
									output.setMessage(String.format(REFUND_SUCCESS_MSG, String.valueOf(gd.getGameClientId()),gd.getAmount()));
								}
							} else if (status == MoneyCreditStatus.ALL_FAIL.getId()) {
								if (input.getOperType() == MoneyPayBackMode.WIN_MONEY.getId()) {
									output.setMessage(String.format(WIN_MONEY_FAIL_MSG, gd.getAmount(), String.valueOf(gd.getGameClientId())));
								} else if (input.getOperType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
									output.setMessage(String.format(REFUND_FAIL_MSG, String.valueOf(gd.getGameClientId()),gd.getAmount()));
								}
							}
							output.setGamePlayedTime(statusServer.getGameSlotTime());
							
							output.setServerGameId(gd.getGameServerId());
							output.setClientGameId(gd.getGameClientId());
							output.setAmount(gd.getAmount());
							return output;
						}
					}
					
				}
			}
		}
		return output;
	}
	
	private void cleanupOldEntries() {
		logger.info("{} Size of the ClientServer insatnces before cleanup {}", TAGS.WIN_MONEY, clientServers.size());
		Iterator<ClientSlotMoneyStatusGiver> iterator = clientServers.iterator();
		while (iterator.hasNext()) {
			ClientSlotMoneyStatusGiver instance = iterator.next();
			long processedTime = instance.getProcessedTime();
			if (processedTime > 0) {
				long diff = System.currentTimeMillis() - instance.getProcessedTime();
				if (diff >= (10 * 60 * 1000)) {
					iterator.remove();
				}
			}
		}
		logger.info("{} Size of the ClientServer insatnces after cleanup {}", TAGS.WIN_MONEY, clientServers.size());
		logger.info("{} Size of the Free ClientServer insatnces before cleanup {}", TAGS.WIN_MONEY, freeGameServers.size());
		iterator = freeGameServers.iterator();
		while (iterator.hasNext()) {
			ClientSlotMoneyStatusGiver instance = iterator.next();
			long processedTime = instance.getProcessedTime();
			if (processedTime > 0) {
				long diff = System.currentTimeMillis() - instance.getProcessedTime();
				if (diff >= (10 * 60 * 1000)) {
					iterator.remove();
				}
			}
		}
		logger.info("{} Size of the Free ClientServer insatnces after cleanup {}", TAGS.WIN_MONEY, freeGameServers.size());
	}
}
