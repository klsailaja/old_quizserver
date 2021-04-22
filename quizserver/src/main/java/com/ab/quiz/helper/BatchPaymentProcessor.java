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
import com.ab.quiz.pojo.UserMoney;

public class BatchPaymentProcessor implements Runnable {
	
	// This map maintains the UserId Vs Boss Id
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	private Map<Long, UserMoney> userIdVsUserMoney = new HashMap<>();
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
		
		
		List<Long> loadUserIds = new ArrayList<>();
		List<Long> loadBossIds = new ArrayList<>();
		
		for (Map.Entry<Long, Long> entries : userIdVsBossId.entrySet()) {
			Long id = entries.getKey();
			Long bossId = entries.getValue();
			if (!loadUserIds.contains(id)) {
				loadUserIds.add(id);
			}
			if (bossId == 0) {
				continue;
			}
			if (!loadBossIds.contains(bossId)) {
				loadBossIds.add(bossId);
			}
		}
		
		logger.info("This is in fetchBossUserMoneyObjects keys size {} : boss size {}", loadUserIds.size(), loadBossIds.size());
		
		List<Long> tobeLoadedUserMoneyIds = new ArrayList<>();
		tobeLoadedUserMoneyIds.addAll(loadUserIds);
		
		for (Long bossIdInt : loadBossIds) {
			if (!tobeLoadedUserMoneyIds.contains(bossIdInt)) {
				tobeLoadedUserMoneyIds.add(bossIdInt);
			}
		}
		
		logger.info("Before size {}", userIdVsUserMoney.size());
		int size = tobeLoadedUserMoneyIds.size();
		if (size <= 0) {
			return;
		}
		
		logger.info("UserMoney objects missing for {}", tobeLoadedUserMoneyIds.size());
		
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
				logger.info("Greater than 50");
			} else if (remainingSize >= 20) {
				String sql = UserMoneyDBHandler.GET_TWENTY_ENTRY_SET;
				ps = dbConn.prepareStatement(sql);
				for (int counter = 1; counter <= 20; counter ++) {
					ps.setLong(counter, tobeLoadedUserMoneyIds.get(index++));
				}
				logger.info("Greater than 20");
			} else {
				String sql = UserMoneyDBHandler.GET_MONEY_ENTRY_BY_USER_ID;
				ps = dbConn.prepareStatement(sql);
				ps.setLong(1, tobeLoadedUserMoneyIds.get(index++));
			}
			
			try {
				rs = ps.executeQuery();
				if (rs != null) {
					while (rs.next()) {
						
						UserMoney userMoney = new UserMoney();
						
						userMoney.setId(rs.getLong(UserMoneyDBHandler.ID));
						userMoney.setLoadedAmount(rs.getLong(UserMoneyDBHandler.LOADED_AMOUNT));
						userMoney.setWinningAmount(rs.getLong(UserMoneyDBHandler.WINNING_AMOUNT));
						userMoney.setReferalAmount(rs.getLong(UserMoneyDBHandler.REFERAL_AMOUNT));
						userMoney.setLoadedAmtLocked(rs.getLong(UserMoneyDBHandler.LOADED_AMOUNT_LOCKED));
						userMoney.setWinningAmtLocked(rs.getLong(UserMoneyDBHandler.WINNING_AMOUNT_LOCKED));
						userMoney.setReferalAmtLocked(rs.getLong(UserMoneyDBHandler.REFERAL_AMOUNT_LOCKED));
				
						userIdVsUserMoney.put(userMoney.getId(), userMoney);
					}
				}
			} catch (SQLException ex) {
				logger.error("SQLException executing prepared statement", ex);
				throw ex;
			} finally {
				if (rs != null) {
					rs.close();
				}
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
			long startTime = System.currentTimeMillis();
			// Just Committing the in-mem transactions
			InMemUserMoneyManager.getInstance().commitNow();
			
			fetchBossUserMoneyObjects();
			
			for (PaymentProcessor processor : paymentProcessors) {
				
				processor.processPayments(userIdVsUserMoney, userIdVsBossId);
				
				Map<Long, UserMoney> inMemMap = InMemUserMoneyManager.getInstance().getInMemUserMoneyObjects();
				
				// The In mem uncommitted objects are used for further transactions 
				for (Map.Entry<Long, UserMoney> entry : inMemMap.entrySet()) {
					Long id = entry.getKey();
					UserMoney userCashObj = entry.getValue();
					if (userIdVsUserMoney.get(id) != null) {
						userIdVsUserMoney.put(id, userCashObj);
					}
				}
			}
			InMemUserMoneyManager.getInstance().commitNow();
			logger.info("Total Time in Run {}", (System.currentTimeMillis() - startTime));
			
		} catch(Exception ex) {
			logger.error("Exception in bulk processing", ex);
		}
	}
}
