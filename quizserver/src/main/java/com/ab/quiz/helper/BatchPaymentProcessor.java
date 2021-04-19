package com.ab.quiz.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.ConnectionPool;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.UserMoney;

public class BatchPaymentProcessor implements Runnable {
	
	// This map maintains the UserId Vs Boss Id
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	private Map<Long, UserMoney> userIdVsUserMoney = new HashMap<>();
	private List<MoneyTransaction> userMoneyTransactions = new ArrayList<>();
	private List<PaymentProcessor> paymentProcessors = new ArrayList<>();
	
	private static final Logger logger = LogManager.getLogger(BatchPaymentProcessor.class);
	
	public void addUserBossIds(Map<Long,Long> gameUserBossIds) {
		userIdVsBossId.putAll(gameUserBossIds);
	}
	
	public void addUserMoneyEntries(Map<Long, UserMoney> gameUserMoneyEntries) {
		userIdVsUserMoney.putAll(gameUserMoneyEntries);
	}
	
	public void addPaymentProcessor(PaymentProcessor processor) {
		paymentProcessors.add(processor);
	}
	
	public void fetchBossUserMoneyObjects() throws SQLException {
		logger.info("This is in fetchBossUserMoneyObjects ");
		List<Long> tobeLoadedUserMoneyIds = new ArrayList<>();
		for (Long bossId : userIdVsBossId.values()) {
			if (bossId == 0) {
				// No boss..
				continue;
			}
			if (!tobeLoadedUserMoneyIds.contains(bossId)) {
				tobeLoadedUserMoneyIds.add(bossId);
			}
		}
		logger.info("UserMoney objects missing for {}", tobeLoadedUserMoneyIds.size());
		logger.info("Before size {}", userIdVsUserMoney.size());
		int size = tobeLoadedUserMoneyIds.size();
		if (size <= 0) {
			return;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		int index = 0;
		while (index < size) {
			int remainingSize = size - index;
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			if (remainingSize >= 50) {
				String sql = UserMoneyDBHandler.GET_FIFTY_ENTRY_SET;
				ps = dbConn.prepareStatement(sql);
				for (int counter = 1; counter <= 50; counter ++) {
					ps.setLong(counter, tobeLoadedUserMoneyIds.get(index++));
				}
			} else if (remainingSize >= 20) {
				String sql = UserMoneyDBHandler.GET_TWENTY_ENTRY_SET;
				ps = dbConn.prepareStatement(sql);
				for (int counter = 1; counter <= 20; counter ++) {
					ps.setLong(counter, tobeLoadedUserMoneyIds.get(index++));
				}
			} else {
				String sql = UserMoneyDBHandler.GET_MONEY_ENTRY_BY_USER_ID;
				ps = dbConn.prepareStatement(sql);
				ps.setLong(1, tobeLoadedUserMoneyIds.get(index++));
			}
			
			try {
				rs = ps.executeQuery();
				if (rs != null) {
					while (rs.next()) {
						
						UserMoney bossUserMoney = new UserMoney();
						
						bossUserMoney.setId(rs.getLong(UserMoneyDBHandler.ID));
						bossUserMoney.setUserId(rs.getLong(UserMoneyDBHandler.USER_ID));
						bossUserMoney.setLoadedAmount(rs.getLong(UserMoneyDBHandler.LOADED_AMOUNT));
						bossUserMoney.setWinningAmount(rs.getLong(UserMoneyDBHandler.WINNING_AMOUNT));
						bossUserMoney.setReferalAmount(rs.getLong(UserMoneyDBHandler.REFERAL_AMOUNT));
						bossUserMoney.setLoadedAmtLocked(rs.getLong(UserMoneyDBHandler.LOADED_AMOUNT_LOCKED));
						bossUserMoney.setWinningAmtLocked(rs.getLong(UserMoneyDBHandler.WINNING_AMOUNT_LOCKED));
						bossUserMoney.setReferalAmtLocked(rs.getLong(UserMoneyDBHandler.REFERAL_AMOUNT_LOCKED));
						
						userIdVsUserMoney.put(bossUserMoney.getId(), bossUserMoney);
					}
					rs.close();
				}
			} catch (SQLException ex) {
				logger.error("SQLException executing prepared statement", ex);
				throw ex;
			} finally {
				if (ps != null) {
					ps.close();
				}
			}
		}
		if (dbConn != null) {
			dbConn.close();
		}
		logger.info("After size {}", userIdVsUserMoney.size());
	}
	
	public void run() {
		try {
			
			logger.info("Start Time ");
			long startTime = System.currentTimeMillis();
			fetchBossUserMoneyObjects();
			for (PaymentProcessor processor : paymentProcessors) {
				processor.processPayments(userIdVsUserMoney, userIdVsBossId, userMoneyTransactions);
			}
			logger.info("Total Time in Run {}", (System.currentTimeMillis() - startTime));
			/*
			updateMoneyRecords(); 
			List<MyTransaction> transactionsList = new ArrayList<>();
			for (MoneyTransaction moneyTran : userMoneyTransactions) {
				transactionsList.add(moneyTran.getTransaction());
			}
			List<Integer> transactionsResults = MyTransactionDBHandler.getInstance().createTransactionsInBatch(transactionsList);
			logger.info("The transactionsResults size is {}", transactionsResults.size());*/
			
			InMemUserMoneyManager.getInstance().update(userMoneyTransactions, userIdVsUserMoney);
			InMemUserMoneyManager.getInstance().commitNow();
		} catch(Exception ex) {
			logger.error("Exception in bulk processing", ex);
		}
	}

	/*
	public void updateMoneyRecords() throws SQLException {
		
		logger.info("moneyRecords.size() :" + userMoneyTransactions.size());
		
		List<MoneyTransaction> winTransactions = new ArrayList<>();
		List<MoneyTransaction> refTransactions = new ArrayList<>();
		
		for (MoneyTransaction moneyTransaction : userMoneyTransactions) {
			if (moneyTransaction.getAccountType() == UserMoneyAccountType.WINNING_MONEY) {
				winTransactions.add(moneyTransaction);
			} else {
				refTransactions.add(moneyTransaction);
			}
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		
		PreparedStatement psWin = null;
		PreparedStatement psRef = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			
			dbConn.setAutoCommit(false);
			
			psWin = dbConn.prepareStatement(UserMoneyDBHandler.UPDATE_WINNING_AMOUNT_BY_USER_ID);
			int index = 0;
			
			List<Integer> winResults = new ArrayList<>();
			
			for (MoneyTransaction moneyTransaction : winTransactions) {
				++index;
				int amount = moneyTransaction.getAmount();
				if (moneyTransaction.getOperType() == UserMoneyOperType.SUBTRACT) {
					amount = -1 * amount;
				}
				psWin.setInt(1,  amount);
				psWin.setLong(2, moneyTransaction.getUserProfileId());
				psWin.addBatch();
				
				if (index == 50) {
					int[] results = psWin.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
					for (int result : results) {
						winResults.add(result);
					}
					index = 0;
				}
			}
			if (index > 0) {
				int [] results = psWin.executeBatch();
				dbConn.setAutoCommit(true);
				for (int result : results) {
					winResults.add(result);
				}
			}
			logger.info("Total records size and results size {} : {}", winTransactions.size(), winResults.size());
			
			int size = winTransactions.size();
			int operResult = 0;
			for (int counter = 0; counter < size; counter ++) {
				MoneyTransaction winTransaction = winTransactions.get(counter);
				operResult = 0;
				if (winResults.get(counter) > 0) {
					operResult = 1;
				}
				winTransaction.getTransaction().setOperResult(operResult);
			}
			
			// Referral account related queries..
			psRef = dbConn.prepareStatement(UserMoneyDBHandler.UPDATE_REFERAL_AMOUNT_BY_USER_ID);
			dbConn.setAutoCommit(false);
			
			index = 0;
			
			List<Integer> referOperResults = new ArrayList<>();
			
			for (MoneyTransaction moneyTransaction : refTransactions) {
				++index;
				int amount = moneyTransaction.getAmount();
				if (moneyTransaction.getOperType() == UserMoneyOperType.SUBTRACT) {
					amount = -1 * amount;
				}
				
				psRef.setInt(1, amount);
				psRef.setLong(2, moneyTransaction.getUserProfileId());
				
				psRef.addBatch();
				
				if (index == 50) {
					int[] results = psRef.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
					for (int result : results) {
						referOperResults.add(result);
					}
					index = 0;
				}
			}
			if (index > 0) {
				int [] results = psRef.executeBatch();
				dbConn.setAutoCommit(true);
				for (int result : results) {
					referOperResults.add(result);
				}
			}
			logger.info("Total ref records size and results size {} : {}", refTransactions.size(), referOperResults.size());
			
			size = refTransactions.size();
			for (int counter = 0; counter < size; counter ++) {
				operResult = 0;
				MoneyTransaction refTransaction = refTransactions.get(counter);
				if (referOperResults.get(counter) > 0) {
					operResult = 1;
				}
				refTransaction.getTransaction().setOperResult(operResult);
			}
		} catch(SQLException ex) {
			logger.error("Error in UpdateMoneyRecords", ex);
			throw ex;
		} finally {
			if (psWin != null) {
				psWin.close();
			}
			if (psRef != null) {
				psRef.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
	}*/
}
