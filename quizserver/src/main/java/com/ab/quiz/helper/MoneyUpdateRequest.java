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
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MoneyUpdaterGameDetails;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;
import com.ab.quiz.tasks.CreateCustomerTickets;
import com.ab.quiz.tasks.MoneyUpdaterResponseHandler;

public class MoneyUpdateRequest implements Runnable {
	
	private int ccTktType;
	private UsersCompleteMoneyDetails usersCompleteDetailsObj;
	private List<MoneyUpdaterGameDetails> moneyUpdaterGameDetails;
	private long gameSlotTime;
	private static final Logger logger = LogManager.getLogger(MoneyUpdateRequest.class);
	
	public MoneyUpdateRequest(int ccTktType, long gameSlotTime, 
			UsersCompleteMoneyDetails usersCompleteDetailsObj, 
			List<MoneyUpdaterGameDetails> moneyUpdaterGameDetails) {
		this.ccTktType = ccTktType;
		this.gameSlotTime = gameSlotTime;
		this.usersCompleteDetailsObj = usersCompleteDetailsObj;
		this.moneyUpdaterGameDetails = moneyUpdaterGameDetails;
	}
	
	public void run() {
		try {
			MoneyUpdaterResponseHandler.getInstance().addToPaymentInProgressShortGameDetails(gameSlotTime, 
					moneyUpdaterGameDetails, ccTktType);
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
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in Money Updater Request", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			
			List<CustomerTicket> ccTickets = new ArrayList<>();
			for (MoneyUpdaterGameDetails gd : moneyUpdaterGameDetails) {
				HashMap<String,String> ccExtraDetailMap = new HashMap<>();
	            ccExtraDetailMap.put(CCUtils.ISSUE_DATE_KEY, new Date(gd.getGameStartTime()).toString());
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
