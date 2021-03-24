package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.MyTransactionDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;

public class WinMsgHandler implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(WinMsgHandler.class);
	private static WinMsgHandler instance = null;
	
	private List<String> combinedMessages = new ArrayList<>();
	
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
		try {
			List<String> gameWinMsgs = MyTransactionDBHandler.getInstance().getRecentWinRecords(-1);
			List<String> withDrawMsgs = WithdrawDBHandler.getInstance().getRecentWinRecords(-1);
			List<String> remainingMsgs = gameWinMsgs;
			
			int size1 = gameWinMsgs.size();
			int size2 = withDrawMsgs.size();
			
			logger.debug("In the run method {} : {}", size1, size2);
			
			int smallSize = size2;
			if (size1 < smallSize) {
				smallSize = size1;
				remainingMsgs = withDrawMsgs;
			}
			
			for (int index = 0; index < smallSize; index ++) {
				combinedMessages.add(gameWinMsgs.get(index));
				combinedMessages.add(withDrawMsgs.get(index));
			}
			combinedMessages.addAll(remainingMsgs.subList(smallSize + 1, remainingMsgs.size()));
		} catch(Exception ex) {
			logger.error("Exception seen ", ex);
		}
	}
	
	public List<String> getCombinedMessages() {
		return combinedMessages;
	}
}