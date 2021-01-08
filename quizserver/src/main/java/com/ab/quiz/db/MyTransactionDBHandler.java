package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserProfile;

/*
CREATE TABLE Transactions(id bigint NOT NULL AUTO_INCREMENT,
 		userId bigint NOT NULL,
		date bigint NOT NULL,
		amount int NOT NULL,
		accountType int NOT NULL,
		transactionType int NOT NULL,
		result int NOT NULL,
		openingBalance bigint NOT NULL,
		closingBalance bigint NOT NULL,
		comments varchar(100) NULL, PRIMARY KEY (id)); 
*/

public class MyTransactionDBHandler {
	// Create an entry
	// get list by userId and account type with limit parameter
	private static String TABLE_NAME = "Transactions";
	
	private static String ID = "id";
	private static String USERID = "userId";
	private static String DATE = "date";
	private static String AMOUNT = "amount";
	private static String ACCOUNT_TYPE = "accountType";
	private static String TRANSACTION_TYPE = "transactionType";
	private static String RESULT = "result";
	private static String OB = "openingBalance";
	private static String CB = "closingBalance";
	private static String COMMENTS = "comments";
	
	private static final String CREATE_TRANSACTION_ENTRY = "INSERT INTO " + TABLE_NAME   
			+ "(" + USERID + "," + DATE + "," + AMOUNT + "," + ACCOUNT_TYPE + ","
			+ TRANSACTION_TYPE + "," + RESULT + "," + OB + "," + CB + "," 
			+ COMMENTS + ") VALUES"
			+ "(?,?,?,?,?,?,?,?,?)";
	private static final String GET_TRANSACTIONS_BY_USER_ID_ACC_TYPE = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USERID + " = ? AND " + ACCOUNT_TYPE + " = ? ORDER BY " + DATE + " LIMIT ?, 5";
	
	private static final String GET_TRANSACTIONS_BY_USER_ID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USERID + " = ? ORDER BY " + DATE + " LIMIT ?, 5";
	
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USERID + " = ?";
	private static final String GET_TOTAL_COUNT_BY_ACCTYPE = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USERID + " = ? AND " + ACCOUNT_TYPE + " = ?";
	
	private static final Logger logger = LogManager.getLogger(MyTransactionDBHandler.class);
	private static MyTransactionDBHandler instance = null;
	
	private MyTransactionDBHandler() {
	}
	
	public static MyTransactionDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In MyTransactionDBHandler getInstance() method instance created");
			instance = new MyTransactionDBHandler();
		}
		return instance;
	}
	
	public boolean createTransaction(MyTransaction myTransaction) throws SQLException {
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_TRANSACTION_ENTRY);
			
			ps.setLong(1, myTransaction.getUserId());
			ps.setLong(2, myTransaction.getDate());
			ps.setInt(3, myTransaction.getAmount());
			ps.setInt(4, myTransaction.getAccountType());
			ps.setInt(5, myTransaction.getTransactionType());
			ps.setInt(6, myTransaction.getOperResult());
			ps.setLong(7, myTransaction.getOpeningBalance());
			ps.setLong(8, myTransaction.getClosingBalance());
			ps.setString(9, myTransaction.getComment());
			
			int createResult = ps.executeUpdate();
			logger.debug(" createResult {}", createResult);
			return (createResult > 0);
		} catch(SQLException ex) {
			logger.error("Error creating transaction entry", ex);
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
	
	public TransactionsHolder getTransactions(long userProfileId, int startRowNumber, int accType) 
			throws SQLException, NotAllowedException {
		
		logger.debug("In getTransactions() with {} {} {}", userProfileId, accType, startRowNumber);
		
		UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userProfileId);
		if (userProfile.getId() == 0) {
			throw new NotAllowedException("User not found with id " + userProfileId);
		}
		
		String totalSql = GET_TOTAL_COUNT;
		String sql = GET_TRANSACTIONS_BY_USER_ID;
		
		if (accType != -1) {
			totalSql = GET_TOTAL_COUNT_BY_ACCTYPE;
			sql = GET_TRANSACTIONS_BY_USER_ID_ACC_TYPE;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println(totalSql);
		System.out.println(sql);
		
		
		PreparedStatement totalPs = dbConn.prepareStatement(totalSql);
		PreparedStatement ps = dbConn.prepareStatement(sql);
		
		totalPs.setLong(1, userProfileId);
		ps.setLong(1, userProfileId);
		
		if (accType != -1) {
			totalPs.setInt(2, accType);
			
			ps.setInt(2, accType);
			ps.setInt(3, startRowNumber);
		} else {
			ps.setInt(2, startRowNumber);
		}
		
		TransactionsHolder transactionsDetails = new TransactionsHolder();
		List<MyTransaction> myTransactions = new ArrayList<>();
		
		try {
			ResultSet totalRs = totalPs.executeQuery();
			if (totalRs != null) {
				if (totalRs.next()) {
					
					int total = totalRs.getInt("COUNT(*)");
					transactionsDetails.setTotal(total);
					
					int lowerRange = startRowNumber + 1;
					int higherRange = startRowNumber + 4;
					
					if (higherRange < total) {
						transactionsDetails.setNextEnabled(true);
					} else {
						transactionsDetails.setNextEnabled(false);
					}
					if ((lowerRange - 5) > 0) {
						transactionsDetails.setPrevEnabled(true);
					} else {
						transactionsDetails.setPrevEnabled(false);
					}
					
				}
				totalRs.close();
			}
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					
					MyTransaction userTrans = new MyTransaction();
					
					userTrans.setsNo(++startRowNumber);
					
					userTrans.setId(rs.getLong(ID));
					userTrans.setUserId(rs.getLong(USERID));
					userTrans.setDate(rs.getLong(DATE));
					userTrans.setAmount(rs.getInt(AMOUNT));
					userTrans.setAccountType(rs.getInt(ACCOUNT_TYPE));
					userTrans.setTransactionType(rs.getInt(TRANSACTION_TYPE));
					userTrans.setOperResult(rs.getInt(RESULT));
					userTrans.setOpeningBalance(rs.getLong(OB));
					userTrans.setClosingBalance(rs.getLong(CB));
					userTrans.setComment(rs.getString(COMMENTS));
					
					myTransactions.add(userTrans);
				}
				transactionsDetails.setTransactionsList(myTransactions);
				rs.close();
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if (totalPs != null) {
				totalPs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		transactionsDetails.setTransactionsList(myTransactions);
		return transactionsDetails;
	}
}
