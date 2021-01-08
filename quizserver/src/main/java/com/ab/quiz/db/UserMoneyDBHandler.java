package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.tasks.CreateTransactionTask;

/*
CREATE TABLE UserMoney(id bigint NOT NULL AUTO_INCREMENT, 
		userProfileId bigint NOT NULL, 
		loadedAmount bigint, 
		winningAmount bigint, 
		referalAmount bigint, PRIMARY KEY (id));
*/

public class UserMoneyDBHandler {
	private static final Logger logger = LogManager.getLogger(UserMoneyDBHandler.class);
	// create a record
	// get the record
	// update 
	private static String ID = "id";
	private static String USER_ID = "userProfileId";
	private static String LOADED_AMOUNT = "loadedAmount";
	private static String WINNING_AMOUNT = "winningAmount";
	private static String REFERAL_AMOUNT = "referalAmount";
	
	private static UserMoneyDBHandler instance = null;
	
	private static final String CREATE_MONEY_ENTRY = "INSERT INTO UserMoney " 
			+ "(" + USER_ID + "," + LOADED_AMOUNT + "," + WINNING_AMOUNT + "," + REFERAL_AMOUNT + ") VALUES"
			+ "(?,?,?,?)";
	private static final String GET_MONEY_ENTRY_BY_USER_ID = "SELECT * FROM UserMoney WHERE " 
			+ USER_ID + " = ?";
	private static final String UPDATE_LOADED_AMOUNT_BY_USER_ID = "UPDATE UserMoney SET " 
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? WHERE (" + USER_ID + " = ? AND ID <> 0)";
	private static final String UPDATE_WINNING_AMOUNT_BY_USER_ID = "UPDATE UserMoney SET " 
			+ WINNING_AMOUNT + " = " + WINNING_AMOUNT + " + ? WHERE (" + USER_ID + " = ? AND ID <> 0)";
	private static final String UPDATE_REFERAL_AMOUNT_BY_USER_ID = "UPDATE UserMoney SET " 
			+ REFERAL_AMOUNT + " = " + REFERAL_AMOUNT + " + ? WHERE (" + USER_ID + " = ? AND ID <> 0)";
	private static final String TRANSFER_AMOUNT_BY_USER_ID = "UPDATE UserMoney SET " 
			+ WINNING_AMOUNT + " = " + WINNING_AMOUNT + " + ? , "
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? "
			+ "WHERE (" + USER_ID + " = ? AND ID <> 0)";
	private static final String TRANSFER_RF_AMOUNT_BY_USER_ID = "UPDATE UserMoney SET " 
			+ REFERAL_AMOUNT + " = " + REFERAL_AMOUNT + " + ? , "
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? "
			+ "WHERE (" + USER_ID + " = ? AND ID <> 0)";
	
	
	private UserMoneyDBHandler() {
	}
	
	public static UserMoneyDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In UserMoneyDBHandler getInstance() method instance created");
			instance = new UserMoneyDBHandler();
		}
		return instance;
	}
	
	public UserMoney createUserMoney(UserMoney userMoney) throws SQLException {
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_MONEY_ENTRY);
			
			ps.setLong(1, userMoney.getUserProfileId());
			ps.setLong(2, userMoney.getLoadedAmount());
			ps.setLong(3, userMoney.getWinningAmount());
			ps.setLong(4, userMoney.getReferalAmount());
		
			int createResult = ps.executeUpdate();
			logger.debug(" createResult {}", createResult);
		} catch(SQLException ex) {
			logger.error("Error creating user money", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return getUserMoneyByProfileId(userMoney.getUserProfileId());
	}
	
	public boolean payToBoss(long userProfileId, long bossUserProfileId, int amt, 
			MyTransaction transaction1, MyTransaction transaction2) throws SQLException {
		// - amt from winning and to referal category
		logger.debug("In payBoss with {} {} {}", userProfileId, bossUserProfileId, amt);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		
		int firstQryRes = 0;
		int secondQryRes = 0;
		
		try {
			dbConn.setAutoCommit(false);
			
			ps1 = dbConn.prepareStatement(UPDATE_WINNING_AMOUNT_BY_USER_ID);
			ps1.setInt(1, amt * -1);
			ps1.setLong(2, userProfileId);
		
			int resultCount1 = ps1.executeUpdate();
		
			ps2 = dbConn.prepareStatement(UPDATE_REFERAL_AMOUNT_BY_USER_ID);
			ps2.setInt(1, amt);
			ps2.setLong(2, bossUserProfileId);
		
			int resultCount2 = ps2.executeUpdate();
		
			dbConn.commit();
		
			dbConn.setAutoCommit(true);
			
			if (resultCount1 > 0) {
				firstQryRes = 1;
			}
			
			if (resultCount2 > 0) {
				secondQryRes = 1;
			}
			
			if ((resultCount1 > 0) && (resultCount2 > 0)) {
				return true;
			}
		} catch (SQLException ex) {
			logger.error("Error creating user money", ex);
		} finally {
			if (ps1 != null) {
				ps1.close();
			}
			if (ps2 != null) {
				ps2.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
			
			transaction1.setOperResult(firstQryRes);
			CreateTransactionTask cTask = new CreateTransactionTask(transaction1);
			LazyScheduler.getInstance().submit(cTask);
			
			transaction2.setOperResult(secondQryRes);
			cTask = new CreateTransactionTask(transaction2);
			LazyScheduler.getInstance().submit(cTask);
		}
		return false;
	}
	
	public UserMoney getUserMoneyByProfileId(long userProfileId) throws SQLException {
		
		logger.debug("In getUserMoneyByProfileId() with {}", userProfileId);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_MONEY_ENTRY_BY_USER_ID);
		ps.setLong(1, userProfileId);
		
		UserMoney userMoney = new UserMoney();
		
		try {
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					userMoney.setId(rs.getLong(ID));
					userMoney.setUserId(rs.getLong(USER_ID));
					userMoney.setLoadedAmount(rs.getLong(LOADED_AMOUNT));
					userMoney.setWinningAmount(rs.getLong(WINNING_AMOUNT));
					userMoney.setReferalAmount(rs.getLong(REFERAL_AMOUNT));
				}
				rs.close();
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getUserMoneyByProfileId()", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return userMoney;
	}
	
	public boolean updateUserMoney(UserMoneyAccountType accountType, UserMoneyOperType operType, 
			long userProfileId, long amt, MyTransaction transaction)
			throws SQLException {
		
		String qry = null;
		switch (accountType) {
			case LOADED_MONEY: {
				qry = UPDATE_LOADED_AMOUNT_BY_USER_ID;
				break;
			}
			case WINNING_MONEY: {
				qry = UPDATE_WINNING_AMOUNT_BY_USER_ID;
				break;
			}
			case REFERAL_MONEY: {
				qry = UPDATE_REFERAL_AMOUNT_BY_USER_ID;
				break;
			}
		}
		
		logger.debug("The query to be executed {}", qry);
		if (qry == null) {
			throw new RuntimeException("No query defined to handle the account type " + accountType);
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(qry);
		
		amt = (operType == UserMoneyOperType.SUBTRACT) ? -1 * amt : amt;  
		
		ps.setLong(1, amt);
		ps.setLong(2, userProfileId);
		int operResult = 0;
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
			logger.debug("The updated row count {}", resultCount);
		}
		catch(SQLException ex) {
			logger.error("Error updating in updateUserMoney", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
			
			transaction.setOperResult(operResult);
			CreateTransactionTask cTask = new CreateTransactionTask(transaction);
			LazyScheduler.getInstance().submit(cTask);
		}
		return true;
	}
	
	public boolean transferAmount(long userProfileId, long amt, int sourceAccType) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		String sqlQry = TRANSFER_AMOUNT_BY_USER_ID;
		UserMoneyAccountType srcAccType = UserMoneyAccountType.findById(sourceAccType);
		
		if (srcAccType != null) {
			sqlQry = TRANSFER_RF_AMOUNT_BY_USER_ID;
		}
		PreparedStatement ps = dbConn.prepareStatement(sqlQry);
		
		logger.debug("The qry is {}", sqlQry);
		
		ps.setLong(1, amt * -1);
		ps.setLong(2, amt);
		ps.setLong(3, userProfileId);
		
		try {
			int resultCount = ps.executeUpdate();
			logger.debug("The updated row count {}", resultCount);
		}
		catch(SQLException ex) {
			logger.error("Error updating in transferAmount", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws SQLException {
		
		UserMoneyDBHandler userMoneyDBHandler = UserMoneyDBHandler.getInstance();
		
		UserMoney userMoney = new UserMoney();
		userMoney.setUserId(1);
		userMoney.setLoadedAmount(100);
		userMoney.setWinningAmount(100);
		userMoney.setReferalAmount(50);
		
		//UserMoney dbUserObj = userMoneyDBHandler.createUserMoney(userMoney);
		//System.out.println(dbUserObj);
		
		/*boolean result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.LOADED_MONEY, 
				UserMoneyOperType.ADD, 1, 10);
		System.out.println("Load amt add result " + result);
		UserMoney dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
		result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.LOADED_MONEY, 
				UserMoneyOperType.SUBTRACT, 1, 20);
		System.out.println("Load amt add result " + result);
		dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
		
		result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.WINNING_MONEY, 
				UserMoneyOperType.ADD, 1, 100);
		System.out.println("Load amt add result " + result);
		dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
		result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.WINNING_MONEY, 
				UserMoneyOperType.SUBTRACT, 1, 50);
		System.out.println("Load amt add result " + result);
		dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
		
		result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.REFERAL_MONEY, 
				UserMoneyOperType.ADD, 1, 200);
		System.out.println("Load amt add result " + result);
		dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
		result = userMoneyDBHandler.updateUserMoney(UserMoneyAccountType.REFERAL_MONEY, 
				UserMoneyOperType.SUBTRACT, 1, 50);
		System.out.println("Load amt add result " + result);
		dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);*/
		
		boolean result = userMoneyDBHandler.transferAmount(1, 30, 2);
		System.out.println("Load amt add result " + result);
		UserMoney dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);
	}
}
