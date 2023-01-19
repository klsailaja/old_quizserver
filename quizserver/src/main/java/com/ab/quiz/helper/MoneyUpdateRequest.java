package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.pojo.ClientSlotMoneyStatusGiver;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MoneyUpdaterGameDetails;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;
import com.ab.quiz.tasks.CreateCustomerTickets;
import com.ab.quiz.tasks.MoneyUpdaterResponseHandler;

public class MoneyUpdateRequest implements Runnable {
	
	private int ccTktType;
	private UsersCompleteMoneyDetails usersCompleteDetailsObj;
	private ClientSlotMoneyStatusGiver clientMoneyStatusGiver;
	private ClientSlotMoneyStatusGiver freeGameMoneyStatusGiver;
	private static final Logger logger = LogManager.getLogger(MoneyUpdateRequest.class);
	
	public ClientSlotMoneyStatusGiver getClientMoneyStatusGiver() {
		return clientMoneyStatusGiver;
	}

	public void setClientMoneyStatusGiver(ClientSlotMoneyStatusGiver clientMoneyStatusGiver) {
		this.clientMoneyStatusGiver = clientMoneyStatusGiver;
		clientMoneyStatusGiver.setServerId(usersCompleteDetailsObj.getServerId());
		clientMoneyStatusGiver.setRequestId(usersCompleteDetailsObj.getRequestId());
		clientMoneyStatusGiver.setOperationType(usersCompleteDetailsObj.getOperationType());
	}

	public ClientSlotMoneyStatusGiver getFreeGameMoneyStatusGiver() {
		return freeGameMoneyStatusGiver;
	}

	public void setFreeGameMoneyStatusGiver(ClientSlotMoneyStatusGiver freeGameMoneyStatusGiver) {
		this.freeGameMoneyStatusGiver = freeGameMoneyStatusGiver;
		if (freeGameMoneyStatusGiver != null) {
			freeGameMoneyStatusGiver.setOperationType(usersCompleteDetailsObj.getOperationType());
			freeGameMoneyStatusGiver.setProcessedTime(System.currentTimeMillis());
		}
	}

	public MoneyUpdateRequest(int ccTktType,  
			UsersCompleteMoneyDetails usersCompleteDetailsObj) {
		this.ccTktType = ccTktType;
		this.usersCompleteDetailsObj = usersCompleteDetailsObj;
	}
	
	public void run() {
		try {
			MoneyUpdaterResponseHandler.getInstance().addtoMoneyUpdateQueue(clientMoneyStatusGiver, freeGameMoneyStatusGiver);
			int seqId = 1;
			List<MoneyTransaction> moneyTransactionsList = 
					usersCompleteDetailsObj.getUsersMoneyTransactionList();
			for (MoneyTransaction mt : moneyTransactionsList) {
				mt.setUniqueId(seqId++);
			}
			PostTask<UsersCompleteMoneyDetails, Integer[]> joinTask = Request.updateMoney();
			joinTask.setPostObject(usersCompleteDetailsObj);
			joinTask.execute();
			
		} catch (Exception ex) {
			QuizConstants.setBackServerStatus(true);
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in Money Updater Request", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			
			List<CustomerTicket> ccTickets = new ArrayList<>();
			for (MoneyUpdaterGameDetails gd : clientMoneyStatusGiver.getSlotMoneyGD()) {
				HashMap<String,String> ccExtraDetailMap = new HashMap<>();
	            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(clientMoneyStatusGiver.getGameSlotTime()).toString());
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_KEY, String.valueOf(gd.getGameClientId()));
	            ccExtraDetailMap.put(CCUtils.ISSUE_GAMEID_SERVER_KEY, String.valueOf(gd.getGameServerId()));
	            ccExtraDetailMap.put(CCUtils.ISSUE_AMT_KEY, String.valueOf(gd.getAmount()));
	
	            String ccExtraDetails = CCUtils.encodeCCExtraValues(ccExtraDetailMap);
	            ccTickets.add(CCUtils.createdCCTicket(ccTktType, 
	            			gd.getUserId(), ccExtraDetails));
			}
			
			if (ccTickets.size() > 0) {
				LazyScheduler.getInstance().submit(new CreateCustomerTickets(ccTickets), 15, TimeUnit.SECONDS);
			}
		}
	}
}
