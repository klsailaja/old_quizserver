package com.ab.quiz.tasks;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.pojo.SlotGamesWinMoneyStatus;

public class WinnersMoneyUpdaterTask implements Runnable {
	
	private static WinnersMoneyUpdaterTask instance = null;
	private static final Logger logger = LogManager.getLogger(WinnersMoneyUpdaterTask.class);
	
	private HashMap<String,Integer> moneyCreditedSatus = new HashMap<>();
	
	private WinnersMoneyUpdaterTask() {
	}
	
	public static WinnersMoneyUpdaterTask getInstance() {
		if (instance == null) {
			logger.info("This is in WinnersMoneyUpdaterTask getInstance");
			instance = new WinnersMoneyUpdaterTask(); 
		}
		return instance;
	}
	
	@Override
	public void run() {
		try {
			String serverPrefixTrackKey = "server" + QuizConstants.MY_SERVER_ID;
			GetTask<SlotGamesWinMoneyStatus[]> slotGamesStatusTask = Request.getGamesSlotMoneyStatus(serverPrefixTrackKey);
			SlotGamesWinMoneyStatus[] thisServerStatus = (SlotGamesWinMoneyStatus[]) slotGamesStatusTask.execute();
			moneyCreditedSatus.clear();
			for (int index = 0; index < thisServerStatus.length; index++) {
				moneyCreditedSatus.put(thisServerStatus[index].getTrackKey(), thisServerStatus[index].getCreditedStatus());
			}
			logger.info("Money Credited Sattus for {} : {}", serverPrefixTrackKey, moneyCreditedSatus);
		}
		catch(Exception ex) {
			logger.error("Exception while fetching the winners money update records from backend", ex);
		}
	}
	
	public int getSlotGamesMoneyCreditedStatus(String trackKey) {
		Integer status = moneyCreditedSatus.get(trackKey);
		if (status == null) {
			return 0;
		}
		return status;
	}
}
