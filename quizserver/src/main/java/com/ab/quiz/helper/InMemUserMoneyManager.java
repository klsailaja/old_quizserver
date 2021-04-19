package com.ab.quiz.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.db.ConnectionPool;
import com.ab.quiz.db.MyTransactionDBHandler;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.UserMoney;

public class InMemUserMoneyManager implements Runnable {
	
	private Map<Long, UserMoney> preUserIdVsMoney = new HashMap<>();
	private Map<Long, UserMoney> postUserIdVsMoney = new HashMap<>();
	private Map<Long, List<MoneyTransaction>> userIdVsPendingTransactions = new HashMap<>();
	
	private static final Logger logger = LogManager.getLogger(InMemUserMoneyManager.class);
	
	private static InMemUserMoneyManager instance = null;
	private static int autoCommitInterval = 15; 
	
	private InMemUserMoneyManager() {
	}
	
	public static InMemUserMoneyManager getInstance() {
		if (instance == null) {
			logger.debug("In InMemUserMoneyManager getInstance() method called");
			instance = new InMemUserMoneyManager();
			
			LazyScheduler.getInstance().submitRepeatedTask(instance, autoCommitInterval * 1000, 
					autoCommitInterval * 1000, TimeUnit.MILLISECONDS);
		}
		return instance;
	}
	
	private void clearStates() {
		preUserIdVsMoney.clear();
		postUserIdVsMoney.clear();
		userIdVsPendingTransactions.clear();
	}
	
	private void updateUserMoneyRecords() throws SQLException {
		
		List<MoneyTransaction> fullPendingTransactions = new ArrayList<>();
		Collection<List<MoneyTransaction>> allUsersTransactions = userIdVsPendingTransactions.values();
		
		for (List<MoneyTransaction> userWiseTrans : allUsersTransactions) {
			fullPendingTransactions.addAll(userWiseTrans);
		}
		
		List<MoneyTransaction> loadedTransactions = new ArrayList<>();
		List<MoneyTransaction> winningTransactions = new ArrayList<>();
		List<MoneyTransaction> referralTransactions = new ArrayList<>();
		
		for (MoneyTransaction moneyTransaction : fullPendingTransactions) {
			
			UserMoneyAccountType userAccountType = moneyTransaction.getAccountType();
			switch(userAccountType) {
				case LOADED_MONEY: {
					loadedTransactions.add(moneyTransaction);
					break;
				}
				case WINNING_MONEY: {
					winningTransactions.add(moneyTransaction);
					break;
				}
				case REFERAL_MONEY: {
					referralTransactions.add(moneyTransaction);
					break;
				}
			}
		}
		try {
			if (loadedTransactions.size() > 0) {
				bulkUpdate(loadedTransactions, UserMoneyDBHandler.UPDATE_LOADED_AMOUNT_BY_USER_ID);	
			}
			if (winningTransactions.size() > 0) {
				bulkUpdate(winningTransactions, UserMoneyDBHandler.UPDATE_WINNING_AMOUNT_BY_USER_ID);
			}
			if (referralTransactions.size() > 0) {
				bulkUpdate(referralTransactions, UserMoneyDBHandler.UPDATE_REFERAL_AMOUNT_BY_USER_ID);
			}
		} catch(SQLException ex) {
			logger.error("Error processing buld update for user money objects", ex);
			throw ex;
		}
		clearStates();
	}
	
	private void bulkUpdate(List<MoneyTransaction> list, String sqlQry) throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(sqlQry);
			int index = 0;
			
			List<Integer> resultsList = new ArrayList<>();
			
			for (MoneyTransaction moneyTransaction : list) {
				++index;
				long amount = moneyTransaction.getAmount();
				if (moneyTransaction.getOperType() == UserMoneyOperType.SUBTRACT) {
					amount = -1 * amount;
				}
				ps.setLong(1,  amount);
				ps.setLong(2, moneyTransaction.getUserProfileId());
				ps.addBatch();
				
				if (index == 50) {
					int[] results = ps.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
					for (int result : results) {
						resultsList.add(result);
					}
					index = 0;
				}
			}
			if (index > 0) {
				int [] results = ps.executeBatch();
				dbConn.setAutoCommit(true);
				for (int result : results) {
					resultsList.add(result);
				}
			}
			logger.info("Total records size and results size {} : {}", list.size(), resultsList.size());
			
			int size = list.size();
			int operResult = 0;
			for (int counter = 0; counter < size; counter ++) {
				operResult = 0;
				MoneyTransaction transaction = list.get(counter);
				if (resultsList.get(counter) > 0) {
					operResult = 1;
				}
				transaction.getTransaction().setOperResult(operResult);
			}
			
			List<MyTransaction> transactionsList = new ArrayList<>();
			for (MoneyTransaction moneyTran : list) {
				transactionsList.add(moneyTran.getTransaction());
			}
			
			List<Integer> transactionsResults = MyTransactionDBHandler.getInstance().createTransactionsInBatch(transactionsList);
			logger.info("The transactionsResults size is {}", transactionsResults.size());
			
		} catch(SQLException ex) {
			logger.error("Error in bulk update", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public synchronized void update(List<MoneyTransaction> transactions, Map<Long, UserMoney> initialUserIdVsMoney) {
		
		if ((transactions != null) && (transactions.size() == 0)) {
			return;
		}
		
		List<Long> userIds = new ArrayList<>();
		
		for (MoneyTransaction transaction : transactions) {
			
			long userId = transaction.getUserProfileId();
			if (userIds.contains(userId)) {
				userIds.add(userId);
			}
			
			List<MoneyTransaction> userPendingTrans = userIdVsPendingTransactions.get(userId);
			if (userPendingTrans == null) {
				userPendingTrans = new ArrayList<>();
			}
			userPendingTrans.addAll(transactions);
			userIdVsPendingTransactions.put(userId, userPendingTrans);
		}
		
		for (Long userId : userIds) {
			UserMoney userMoney = initialUserIdVsMoney.get(userId);
			if (userMoney == null) {
				logger.info("***********************************************");
				logger.info("User Money Object not found for {}", userId);
				logger.info("***********************************************");
				continue;
			}
			
			List<MoneyTransaction> userPendingTrans = userIdVsPendingTransactions.get(userId);
			for (MoneyTransaction moneyTran : userPendingTrans) {
				UserMoneyAccountType userAccountType = moneyTran.getAccountType();
				long currentBalance = -1;
				switch(userAccountType) {
					case LOADED_MONEY: {
						currentBalance = userMoney.getLoadedAmount();
						break;
					}
					case WINNING_MONEY: {
						currentBalance = userMoney.getWinningAmount();
						break;
					}
					case REFERAL_MONEY: {
						currentBalance = userMoney.getReferalAmount();
						break;
					}
				}
				if (moneyTran.getOperType() == UserMoneyOperType.ADD) {
					currentBalance = currentBalance + moneyTran.getAmount();
				} else {
					currentBalance = currentBalance - moneyTran.getAmount();
				}
				
				switch(userAccountType) {
					case LOADED_MONEY: {
						userMoney.setLoadedAmount(currentBalance);
						break;
					}
					case WINNING_MONEY: {
						userMoney.setWinningAmount(currentBalance);
						break;
					}
					case REFERAL_MONEY: {
						currentBalance = userMoney.getReferalAmount();
						userMoney.setReferalAmount(currentBalance);
						break;
					}
				}
				
				postUserIdVsMoney.put(userId, userMoney);
			}
		}
	}
	
	public synchronized UserMoney getUserMoneyById(long userId) throws SQLException {
		UserMoney userMoney = postUserIdVsMoney.get(userId); 
		if (userMoney == null) {
			return UserMoneyDBHandler.getInstance().getUserMoneyByProfileId(userId);
		}
		return userMoney;
	}
	
	public synchronized void commitNow() throws SQLException {
		updateUserMoneyRecords();
	}
	
	public void run() {
		try {
			commitNow();
		} catch (Exception ex) {
			logger.error("Exception in periodic user money objects commit", ex);
		}
	}
}
