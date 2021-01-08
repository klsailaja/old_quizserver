package com.ab.quiz.tasks;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.MyTransactionDBHandler;
import com.ab.quiz.pojo.MyTransaction;

public class CreateTransactionTask implements Runnable {
	
	private MyTransaction transaction;
	
	private final Logger logger = LogManager.getLogger(CreateTransactionTask.class);
	
	public CreateTransactionTask(MyTransaction transaction) {
		this.transaction = transaction;
	}
	
	public void run() {
		try {
			logger.info("Creating a transaction");
			if (transaction.getOperResult() == 0) {
				String cmts = transaction.getComment();
				cmts = cmts + ". This operation is failed due to some error. Will be corrected in 1 or 2 days";
				transaction.setComment(cmts);
			}
			boolean result = MyTransactionDBHandler.getInstance().createTransaction(transaction);
			logger.info("Transaction creation status :" + result);
		} catch(SQLException ex) {
			logger.error("SQL Exception while creating a transaction", ex);
		}
	}
}
