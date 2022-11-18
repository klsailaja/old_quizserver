package com.ab.quiz.tasks;

import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;

public class DeleteOldRecords implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(DeleteOldRecords.class);
	
	public DeleteOldRecords() {
	}

	@Override
	public void run() {
		try {
			logger.info("{} This is in DeleteOldRecords", TAGS.DELETE_OLD_RECORDS);
			
			int oldWDReceipts = QuizConstants.CLOSED_WD_RECEIPTS_DURATION * -1;
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, oldWDReceipts);
			long time = calendar.getTimeInMillis();
			
			int delCt = WithdrawDBHandler.getInstance().deleteRecords(time);
			logger.info("{} Deleted WithdrawClosed Records size {}", TAGS.DELETE_OLD_RECORDS, delCt);
			
			int historyGamesDaysDuration = QuizConstants.GAMES_HISTORY_DURATION * -1;
			calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, historyGamesDaysDuration);
			time = calendar.getTimeInMillis();
			
			List<Long> delGameIds = GameHistoryDBHandler.getInstance().deleteRecords(time);
			logger.info("{} Deleted Old Game Records size {}", TAGS.DELETE_OLD_RECORDS, delGameIds.size());
			
			GameHistoryDBHandler.getInstance().bulkDeletePlayerDetails(delGameIds, 70);
		}
		catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception while deleting the old records", TAGS.DELETE_OLD_RECORDS);
			logger.error(ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
	
	public static void main(String[] args) {
		DeleteOldRecords test = new DeleteOldRecords();
		test.run();
	}
}
