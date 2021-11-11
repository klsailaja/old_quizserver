package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.WithdrawDBHandler;

public class WinMsgHandler implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(WinMsgHandler.class);
	private static WinMsgHandler instance = null;
	
	private List<String> combinedMessages = new ArrayList<>();
	private List<String> gameWinMsgs = new ArrayList<>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	private WinMsgHandler() {
	}
	
	public static WinMsgHandler getInstance() {
		if (instance == null) {
			logger.debug("In WinMsgHandler getInstance() method instance created");
			instance = new WinMsgHandler();
			instance.initialize();
		}
		return instance;
	}
	
	private void initialize() {
		LazyScheduler.getInstance().submitRepeatedTask(this, 0, 
				30, TimeUnit.MINUTES);
	}

	@Override
	public void run() {
		
		lock.writeLock().lock();
		try {
			combinedMessages.clear();
			
			List<String> withDrawMsgs = WithdrawDBHandler.getInstance().getRecentWinRecords(-1, false, null);
			int wdMsgsSize = withDrawMsgs.size();
			if (wdMsgsSize > 0) {
				if (QuizConstants.MY_SERVER_ID == 1) {
					String[] postWDMsgs = new String[wdMsgsSize];
					int index = 0;
					for (String str : withDrawMsgs) {
						postWDMsgs[index++] = str; 
					}
					
					PostTask<String[], String> postWDMsgsTask = Request.setGenericWithdrawMsgsTask();
					postWDMsgsTask.setPostObject(postWDMsgs);
					String postWdResult = (String) postWDMsgsTask.execute();
					logger.info("The result of setting of generic withdraw messages to core server is:" + postWdResult);
				}
			}
			
			GetTask<String[]> genericWinMsgTask = Request.getGenericWinMessagesTask();
			String[] localWinMsgs = (String[]) genericWinMsgTask.execute();
			List<String> localGenericWinMessages = Arrays.asList(localWinMsgs);
			if (localGenericWinMessages.size() > 0) {
				gameWinMsgs.clear();
				gameWinMsgs.addAll(localGenericWinMessages);
			}
			
			List<String> remainingMsgs = gameWinMsgs;
			
			int size1 = gameWinMsgs.size();
			int size2 = withDrawMsgs.size();
			
			logger.info("In the run method {} : {}", size1, size2);
			
			int smallSize = size2;
			if (size1 < smallSize) {
				smallSize = size1;
				remainingMsgs = withDrawMsgs;
			}
			
			for (int index = 0; index < smallSize; index ++) {
				combinedMessages.add(gameWinMsgs.get(index));
				combinedMessages.add(withDrawMsgs.get(index));
			}
			combinedMessages.addAll(remainingMsgs.subList(smallSize, remainingMsgs.size()));
		} catch(Exception ex) {
			logger.error("Exception seen ", ex);
		}
		lock.writeLock().unlock();
	}
	
	public void setWithdrawMessages(List<String> wdMsgs) {
		gameWinMsgs.clear();
		gameWinMsgs.addAll(wdMsgs);
	}
	
	
	public List<String> getCombinedMessages() {
		lock.readLock().lock();
		List<String> retValue = combinedMessages;
		lock.readLock().unlock();
		return retValue;
	}
}
