package com.ab.quiz.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.pojo.GameHistoryTempPOJO;
import com.ab.quiz.pojo.SlotGamesWinMoneyStatus;

public class WinnersMoneyUpdaterTask implements Runnable {
	
	private static WinnersMoneyUpdaterTask instance = null;
	private static final Logger logger = LogManager.getLogger(WinnersMoneyUpdaterTask.class);
	
	private HashMap<String,Integer> moneyCreditedSatus = new HashMap<>();
	private HashMap<Long,Integer> slotGamePlayedTimeVsRetryCount = new HashMap<>();
	
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
			
			Map<Date,Integer> printInReadFormat = new HashMap<>();
			for (int index = 0; index < thisServerStatus.length; index++) {
				moneyCreditedSatus.put(thisServerStatus[index].getTrackKey(), thisServerStatus[index].getCreditedStatus());
				String timeStr = thisServerStatus[index].getTrackKey();
				timeStr = timeStr.substring(timeStr.indexOf('-') + 1);
				long timeLong = Long.parseLong(timeStr);
				printInReadFormat.put(new Date(timeLong), thisServerStatus[index].getCreditedStatus());
			}
			logger.info("Money Credited Status for {} : {}", serverPrefixTrackKey, moneyCreditedSatus);
			logger.info("Money Credited Status for {} : {}", serverPrefixTrackKey, printInReadFormat);
			
			List<GameHistoryTempPOJO> openRecords = GameHistoryDBHandler.getInstance().getOpenStatusRecords();
			List<Long> ids = new ArrayList<>();
			List<Integer> creditedStatus = new ArrayList<>();
			
			for (GameHistoryTempPOJO pojo : openRecords) {
				Integer status = moneyCreditedSatus.get(serverPrefixTrackKey + "-" + pojo.getGamePlayedTime());
				if (status == null) {
					logger.info("Ignoring open record with game time {} and game id {}", new Date(pojo.getGamePlayedTime()), pojo.getGameId());
					continue;
				}
				if (status == 1) {
					ids.add(pojo.getGameId());
					creditedStatus.add(1);
					slotGamePlayedTimeVsRetryCount.remove(pojo.getGamePlayedTime());
				} else {
					Integer retryCount = slotGamePlayedTimeVsRetryCount.get(pojo.getGamePlayedTime());
					if (retryCount == null) {
						retryCount = new Integer(0);
					}
					retryCount++;
					slotGamePlayedTimeVsRetryCount.put(pojo.getGamePlayedTime(), retryCount);
					if (retryCount == 6) {
						ids.add(pojo.getGameId());
						creditedStatus.add(2);
						slotGamePlayedTimeVsRetryCount.remove(pojo.getGamePlayedTime());
					}
				}
			}
			if (ids.size() > 0) {
				GameHistoryDBHandler.getInstance().bulkUpdateStatus(ids, creditedStatus, 5);
			}
			
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
