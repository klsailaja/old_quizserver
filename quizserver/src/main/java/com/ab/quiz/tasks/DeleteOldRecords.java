package com.ab.quiz.tasks;

import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;

public class DeleteOldRecords implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(DeleteOldRecords.class);
	
	public DeleteOldRecords() {
	}

	@Override
	public void run() {
		try {
			int lastFewDays = -30;
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, lastFewDays);
			long time = calendar.getTimeInMillis();
			
			logger.info("Time is {}", time);
			int delCt = WithdrawDBHandler.getInstance().deleteRecords(time);
			logger.info("Deleted WithdrawClosed Records size {}", delCt);
			System.out.println(delCt);
			List<Long> delGameIds = GameHistoryDBHandler.getInstance().deleteRecords(time);
			logger.info("Deleted Old Game Records size {}", delGameIds.size());
			System.out.println(delGameIds.size());
			GameHistoryDBHandler.getInstance().bulkDeletePlayerDetails(delGameIds, 70);
		}
		catch(Exception ex) {
			logger.error("Exception while deleting the old records", ex);
		}
	}
	
	public static void main(String[] args) {
		DeleteOldRecords test = new DeleteOldRecords();
		test.run();
	}
}
