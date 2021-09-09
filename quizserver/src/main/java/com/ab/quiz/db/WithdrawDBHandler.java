package com.ab.quiz.db;

import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.ReceiptType;
import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.WithdrawReqState;
import com.ab.quiz.constants.WithdrawReqType;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.UserMoneyHandler;
import com.ab.quiz.helper.InMemUserMoneyManager;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.UserProfile;
import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawReqByBank;
import com.ab.quiz.pojo.WithdrawReqByPhone;
import com.ab.quiz.pojo.WithdrawRequest;
import com.ab.quiz.pojo.WithdrawRequestsHolder;
import com.ab.quiz.tasks.AddTransactionsTask;

/*
 *
 CREATE TABLE WITHDRAWREQUESTS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		REFID VARCHAR(10) NOT NULL,
		USERID BIGINT NOT NULL,
		STATUS INT NOT NULL,
		REQTYPE INT NOT NULL,
		ACCOUNTDETAILSID BIGINT NOT NULL,
		AMOUNT INT NOT NULL,
		OPENEDTIME BIGINT NOT NULL,
		CLOSEDTIME BIGINT NULL,
		RECEIPTID BIGINT NULL,
		CLOSECMTS VARCHAR(100), PRIMARY KEY (ID)) ENGINE = INNODB;
		
CREATE INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS(USERID);		
DROP INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS;		
CREATE INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS(USERID);

*/

public class WithdrawDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawDBHandler.class);
	
	private static String TABLE_NAME = "WITHDRAWREQUESTS"; 
	
	private static String ID = "ID";
	private static String REFID = "REFID";
	private static String USER_PROFILE_ID = "USERID";
	private static String STATUS = "STATUS";
	private static String REQUEST_TYPE = "REQTYPE";
	private static String ACDETAILS_ID = "ACCOUNTDETAILSID";
	private static String AMOUNT = "AMOUNT";
	private static String REQUEST_OPENED_TIME = "OPENEDTIME";
	private static String REQUEST_CLOSED_TIME = "CLOSEDTIME";
	private static String TRANSACTION_RECEIPT_ID = "RECEIPTID";
	private static String CLOSED_CMTS = "CLOSECMTS";
	
	private static WithdrawDBHandler instance = null;
	
	private static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
				+ "abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom secureRnd = new SecureRandom();
	private static final int REFERENCE_MAX_LEN = 10;
	private static final int MAX_ROWS = 5;
	
	private static final String GET_DATA_BY_USER_ID_STATUS = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USER_PROFILE_ID + " = ? AND " + STATUS + " = ? ORDER BY " + REQUEST_OPENED_TIME + " DESC LIMIT ?, " + MAX_ROWS;
	
	private static final String GET_DATA_BY_USER_ID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USER_PROFILE_ID + " = ? ORDER BY " + REQUEST_OPENED_TIME + " DESC LIMIT ?, " + MAX_ROWS;
	
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USER_PROFILE_ID + " = ?";
	private static final String GET_TOTAL_COUNT_BY_STATUS = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USER_PROFILE_ID + " = ? AND " + STATUS + " = ?";


	private static final String CREATE_WITHDRAW_ENTRY = "INSERT INTO " + TABLE_NAME
			+ "(" + REFID + "," + USER_PROFILE_ID + "," 
			+ STATUS + "," + REQUEST_TYPE + "," + ACDETAILS_ID + ","  
			+ AMOUNT + "," + REQUEST_OPENED_TIME + "," + REQUEST_CLOSED_TIME + ","
			+ TRANSACTION_RECEIPT_ID + "," + CLOSED_CMTS + ") VALUES"
			+ "(?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String MAX_WITHDRAW_REQ_ID = "SELECT MAX(ID) FROM " + TABLE_NAME;
	private static final String GET_WITHDRAW_ENTRY_BY_REF_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " + REFID + " = ?";
	private static final String UPDATE_WITHDRAW_ENTRY_BY_REF_ID = "UPDATE " + TABLE_NAME + " SET "
			+ STATUS + " = ? " + " WHERE (" + REFID + " = ? AND ID <> 0)";
	private static final String CLOSE_WITHDRAW_ENTRY_BY_REF_ID = "UPDATE " + TABLE_NAME + " SET "
			+ STATUS + " = ? ," + REQUEST_CLOSED_TIME + " = ? ,"
			+ TRANSACTION_RECEIPT_ID + " = ? ," + CLOSED_CMTS + " = ? "
			+ " WHERE (" + REFID + " = ? AND ID <> 0)";
	
	private static final String LATEST_WD_RECORDS = "SELECT " + USER_PROFILE_ID + "," + AMOUNT + "," 
			+ REQUEST_CLOSED_TIME + " FROM " + TABLE_NAME 
			+ " WHERE " + STATUS + " =? ORDER BY " + ID + " DESC LIMIT 0,120";
	private static final String LATEST_BOSS_WD_RECORDS = "SELECT " + USER_PROFILE_ID + "," + AMOUNT + "," 
			+ REQUEST_CLOSED_TIME + " FROM " + TABLE_NAME 
			+ " WHERE " + USER_PROFILE_ID + " = ? AND " + STATUS + " =? ORDER BY " + ID + " DESC LIMIT 0,10";
	
	private static final String REMOVE_OLD_RECORDS = "DELETE FROM " + TABLE_NAME 
			+ " WHERE (" + REQUEST_CLOSED_TIME + " < ? AND ID <> 0 AND STATUS =" + WithdrawReqState.CLOSED.getId() + ")";
	private static final String GET_WITHDRAW_TYPE_RECEIPT_IDS = "SELECT " + TRANSACTION_RECEIPT_ID + " FROM " + TABLE_NAME + " WHERE " + 
			REQUEST_CLOSED_TIME + " < ?";
	
	private WithdrawDBHandler() {
	}
	
	public static WithdrawDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new WithdrawDBHandler();
		}
		return instance;
	}
	
	public int deleteRecords(long timePeriod) throws SQLException {
		logger.info("In deleteRecords method");
		
		List<Long> oldWDReceiptIds = getOldWithdrawReceiptIds(timePeriod);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(REMOVE_OLD_RECORDS);
			
			ps.setLong(1, timePeriod);
			
			int result = ps.executeUpdate();
			logger.debug("In deleteRecords create op result : {}", result);
			
			WithdrawReceiptDBHandler.getInstance().bulkDeleteReceiptRecords(oldWDReceiptIds, 50);
			
			return result;
		} catch (SQLException ex) {
			logger.error("Error in deleteRecords ", ex);
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
	
	public long getMaxWithdrawReqId() throws SQLException {
		
		logger.debug("In getMaxWithdrawReqId() method");
		
		long maxId = -1;
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet idRs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(MAX_WITHDRAW_REQ_ID);
			
			idRs = ps.executeQuery();
			if (idRs != null) {
				if (idRs.next()) {
					maxId = idRs.getLong("MAX(ID)");
				}
			}
		} catch (SQLException ex) {
			logger.error("SQL Exception in getMaxWithdrawReqId()", ex);
			throw ex;
		} finally {
			if (idRs != null) {
				idRs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("Returning from getMaxWithdrawReqId() {}", maxId);
		return maxId;
	}
	
	private List<Long> getOldWithdrawReceiptIds(long timePeriod) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(GET_WITHDRAW_TYPE_RECEIPT_IDS);
		ps.setLong(1, timePeriod);
		
		ResultSet rs = null;
		List<Long> oldWDReceiptIds = new ArrayList<>();
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					oldWDReceiptIds.add(rs.getLong(TRANSACTION_RECEIPT_ID));
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getOldWithdrawReceiptIds()", ex);
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.info("Old Withdraw Receipt Records to be deleted size is {}", oldWDReceiptIds.size());
		return oldWDReceiptIds;
	}
	
	public WithdrawRequest getWithdrawReqByRefId(String refId) throws SQLException {
		
		refId = refId.trim();
		logger.debug("In getWithdrawReqByRefId() with {}", refId);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_WITHDRAW_ENTRY_BY_REF_ID);
		ps.setString(1, refId);
		ResultSet rs = null;
		
		WithdrawRequest wdRequest = new WithdrawRequest();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					wdRequest.setId(rs.getLong(ID));
					wdRequest.setRefId(rs.getString(REFID));
					wdRequest.setUserProfileId(rs.getLong(USER_PROFILE_ID));
					wdRequest.setReqStatus(rs.getInt(STATUS));
					wdRequest.setRequestType(rs.getInt(REQUEST_TYPE));
					wdRequest.setAccountDetailsId(rs.getInt(ACDETAILS_ID));
					wdRequest.setAmount(rs.getInt(AMOUNT));
					wdRequest.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					wdRequest.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					wdRequest.setReceiptId(rs.getLong(TRANSACTION_RECEIPT_ID));
					wdRequest.setClosedComents(rs.getString(CLOSED_CMTS));
				}
				
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getWithdrawReqByRefId()", ex);
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return wdRequest;
	}
	
	
	public boolean closeWithDrawRequest(String receiptFileName, String withdrawRefId, String wdClosedCmts) 
			throws SQLException, NotAllowedException, FileNotFoundException {
		// all validations
		// Insert the receipt file
		// Make the withdraw request as closed..
		WithdrawRequest wdRequest = getWithdrawReqByRefId(withdrawRefId);
		if (wdRequest.getId() == 0) {
			throw new NotAllowedException("No Withdraw Request found");
		}
		if (wdRequest.getReqStatus() != WithdrawReqState.OPEN.getId()) {
			throw new NotAllowedException("This type of Request cannot be closed");
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement updateWDStatePS = null;
		PreparedStatement revertMoneyPS = null;
		int result1 = 0;
		int result2 = 0;
		
		try {
			
			long receiptId = WithdrawReceiptDBHandler.getInstance().createWDReceipt(ReceiptType.WITHDRAW.getId(), receiptFileName);
			logger.info("The receipt file contents DB Entry id is {}", receiptId);
			if (receiptId == -1) {
				logger.error("Could not insert the receipt file contents to DB");
			}
			
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			updateWDStatePS = dbConn.prepareStatement(CLOSE_WITHDRAW_ENTRY_BY_REF_ID);
			
			updateWDStatePS.setInt(1, WithdrawReqState.CLOSED.getId());
			updateWDStatePS.setLong(2, System.currentTimeMillis());
			updateWDStatePS.setLong(3, receiptId);
			updateWDStatePS.setString(4, wdClosedCmts);
			updateWDStatePS.setString(5, withdrawRefId);
			
			result1 = updateWDStatePS.executeUpdate();
			
			logger.debug("Changed the withdraw req state result {}", (result1 > 0));
			
			UserMoneyDBHandler userMoneyHandler = UserMoneyDBHandler.getInstance();
			long userProfileId = wdRequest.getUserProfileId(); 
			UserMoney userMoney = userMoneyHandler.getUserMoneyById(userProfileId);
			
			long userOB = userMoney.getAmount();
			long time = System.currentTimeMillis();
			String comments = "Withdraw Request Processed. Receipt Attached for " + wdRequest.getRefId();
			long userCB = userOB;
			
			MyTransaction transaction = Utils.getTransactionPojo(userProfileId, time, 
					wdRequest.getAmount(), TransactionType.CLOSED.getId(), 
					wdRequest.getFromAccType(), userOB, userCB, comments);
			
			String revertMoneySql = UserMoneyHandler.getInstance().getUserAccWithDrawSql(wdRequest.getFromAccType());
			
			revertMoneyPS = dbConn.prepareStatement(revertMoneySql);
			
			revertMoneyPS.setLong(1, 0);
			revertMoneyPS.setLong(2, -1 * wdRequest.getAmount());
			revertMoneyPS.setLong(3, userProfileId);
			
			result2 = revertMoneyPS.executeUpdate();
			
			int operRes = 0;
			
			if (result2 > 0) {
				operRes = 1;
			}
			transaction.setOperResult(operRes);
			MyTransactionDBHandler.getInstance().createTransaction(transaction);
			return true;
			 
		} catch (SQLException ex) {
			logger.error("Error while executing cancelWithdrawRequest ", ex);
			throw ex;
		} finally {
			if (updateWDStatePS != null) {
				updateWDStatePS.close();
			}
			if (revertMoneyPS != null) {
				revertMoneyPS.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public boolean cancelWithdrawRequest(long userProfileId, String withdrawRefId) 
			throws SQLException,NotAllowedException {
		
		WithdrawRequest wdRequest = getWithdrawReqByRefId(withdrawRefId);
		if (wdRequest.getId() == 0) {
			throw new NotAllowedException("No Withdraw Request found");
		}
		if (wdRequest.getReqStatus() != WithdrawReqState.OPEN.getId()) {
			throw new NotAllowedException("This type of Request cannot be canceled");
		}
		if (wdRequest.getUserProfileId() != userProfileId) {
			throw new NotAllowedException("This Request not owned by you");
		}
		
		boolean hasInMemRecords = InMemUserMoneyManager.getInstance().hasInMemRecords(userProfileId);
		if (hasInMemRecords) {
			InMemUserMoneyManager.getInstance().commitNow();
		}
		
		List<UserMoneyAccountType> accTypes = new ArrayList<>();
		accTypes.add(UserMoneyAccountType.findById(wdRequest.getFromAccType()));
		
		List<TransactionType> transactionTypes = new ArrayList<>();
		transactionTypes.add(TransactionType.CREDITED);
		
		List<String> comments = new ArrayList<>();
		comments.add("Withdraw Request Cancelled");
		
		List<MyTransaction> wdRelatedTransactions = UserMoneyDBHandler.getInstance(). 
				getTransactionObjects(wdRequest.getUserProfileId(), wdRequest.getAmount(), 
				accTypes, transactionTypes, comments);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement updateWDStatePS = null;
		PreparedStatement revertMoneyPS = null;
		int result1 = 0;
		int result2 = 0;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			updateWDStatePS = dbConn.prepareStatement(UPDATE_WITHDRAW_ENTRY_BY_REF_ID);
			
			updateWDStatePS.setInt(1, WithdrawReqState.CANCELLED.getId());
			updateWDStatePS.setString(2, withdrawRefId);
			
			result1 = updateWDStatePS.executeUpdate();
			
			logger.debug("Changed the withdraw req state result : {}", (result1 > 0));
			
			String revertMoneySql = UserMoneyHandler.getInstance().getUserAccWithDrawSql(wdRequest.getFromAccType());
			
			revertMoneyPS = dbConn.prepareStatement(revertMoneySql);
			
			revertMoneyPS.setLong(1, wdRequest.getAmount());
			revertMoneyPS.setLong(2, -1 * wdRequest.getAmount());
			revertMoneyPS.setLong(3, userProfileId);
			
			result2 = revertMoneyPS.executeUpdate();
			
			int transferRes = 0;
			if (result2 > 0) {
				transferRes = 1;
			}
			
			for (MyTransaction transaction : wdRelatedTransactions) {
				transaction.setOperResult(transferRes);
			}
			LazyScheduler.getInstance().submit(new AddTransactionsTask(wdRelatedTransactions));
			return true;
			 
		} catch (SQLException ex) {
			logger.error("Error while executing cancelWithdrawRequest ", ex);
			throw ex;
		} finally {
			if (updateWDStatePS != null) {
				updateWDStatePS.close();
			}
			if (revertMoneyPS != null) {
				revertMoneyPS.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public boolean createWithDrawReq(WDUserInput wdUserInput, 
			WithdrawReqByPhone phoneReq, WithdrawReqByBank bankReq) throws SQLException {
		
		long wdDetailsId = -1;
		
		if (wdUserInput.getRequestType() == WithdrawReqType.BY_PHONE.getId()) {
			WithdrawByPhoneReqDBHandler handler = WithdrawByPhoneReqDBHandler.getInstance(); 
			wdDetailsId = handler.createReqByPhone(phoneReq);
		} else if (wdUserInput.getRequestType() == WithdrawReqType.BY_BANK.getId()) {
			WithdrawByBankDBHandler handler = WithdrawByBankDBHandler.getInstance();
			wdDetailsId = handler.createReqByBank(bankReq);
		}
		
		if (wdDetailsId < 0) {
			return false;
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_ENTRY);
			
			long maxReqId = getMaxWithdrawReqId() + 1;
			int idStrLen = String.valueOf(maxReqId).length();
			int remainingLen = REFERENCE_MAX_LEN - idStrLen;
			
			String refId = getReferenceNumber(remainingLen, maxReqId);
			ps.setString(1, refId);
			ps.setLong(2, wdUserInput.getUserProfileId());
			ps.setInt(3, WithdrawReqState.OPEN.getId());
			ps.setInt(4, wdUserInput.getRequestType());
			ps.setLong(5, wdDetailsId);
			ps.setInt(6, wdUserInput.getAmount());
			
			long currentTime = System.currentTimeMillis();
			ps.setLong(7, currentTime);
			ps.setNull(8, Types.NULL);
			ps.setNull(9, Types.NULL);
			ps.setNull(10, Types.NULL);
			
			int result = ps.executeUpdate();
			logger.debug("In createWithDrawReq create op result : {}", result);
			
			return (result >= 1);
		} catch (SQLException ex) {
			logger.error("Error creating createWithDrawReq ", ex);
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
	
	public WithdrawRequestsHolder getWithdrawRequests(long userProfileId, int startRowNumber, int state) 
			throws SQLException, NotAllowedException {
		
		logger.debug("In getWithdrawRequests() with {} {} {}", userProfileId, state, startRowNumber);
		
		UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userProfileId);
		if (userProfile.getId() == 0) {
			throw new NotAllowedException("User not found with id " + userProfileId);
		}
		
		String totalSql = GET_TOTAL_COUNT;
		String sql = GET_DATA_BY_USER_ID;
		
		if (state != -1) {
			totalSql = GET_TOTAL_COUNT_BY_STATUS;
			sql = GET_DATA_BY_USER_ID_STATUS;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement totalPs = dbConn.prepareStatement(totalSql);
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ResultSet totalRs = null;
		ResultSet rs = null;
		
		totalPs.setLong(1, userProfileId);
		ps.setLong(1, userProfileId);
		
		if (state != -1) {
			totalPs.setInt(2, state);
			
			ps.setInt(2, state);
			ps.setInt(3, startRowNumber);
		} else {
			ps.setInt(2, startRowNumber);
		}
		
		WithdrawRequestsHolder holder = new WithdrawRequestsHolder();
		List<WithdrawRequest> dataList = new ArrayList<>();
		
		try {
			totalRs = totalPs.executeQuery();
			if (totalRs != null) {
				if (totalRs.next()) {
					
					int total = totalRs.getInt("COUNT(*)");
					holder.setTotal(total);
					
					int lowerRange = startRowNumber + 1;
					int higherRange = startRowNumber + MAX_ROWS;
					
					if (higherRange < total) {
						holder.setNextEnabled(true);
					} else {
						holder.setNextEnabled(false);
					}
					if ((lowerRange - MAX_ROWS) > 0) {
						holder.setPrevEnabled(true);
					} else {
						holder.setPrevEnabled(false);
					}
					
				}
			}
			
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					
					WithdrawRequest dataItem = new WithdrawRequest();
					
					dataItem.setsNo(++startRowNumber);
					
					dataItem.setId(rs.getLong(ID));
					dataItem.setRefId(rs.getString(REFID));
					dataItem.setUserProfileId(rs.getLong(USER_PROFILE_ID));
					dataItem.setReqStatus(rs.getInt(STATUS));
					dataItem.setRequestType(rs.getInt(REQUEST_TYPE));
					dataItem.setAccountDetailsId(rs.getLong(ACDETAILS_ID));
					dataItem.setAmount(rs.getInt(AMOUNT));
					dataItem.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					dataItem.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					dataItem.setReceiptId(rs.getLong(TRANSACTION_RECEIPT_ID));
					dataItem.setClosedComents(rs.getString(CLOSED_CMTS));
					
					WithdrawReqType wdReqType = WithdrawReqType.BY_PHONE;
					
					if (wdReqType.getId() == dataItem.getRequestType()) {
						WithdrawByPhoneReqDBHandler dbHandler = WithdrawByPhoneReqDBHandler.getInstance();
					
						WithdrawReqByPhone wdByPhone = dbHandler.getWithdrawReqByPhoneById(dataItem.getAccountDetailsId());
						dataItem.setByPhone(wdByPhone);
					} else if (dataItem.getRequestType() == WithdrawReqType.BY_BANK.getId()) {
						WithdrawByBankDBHandler dbHandler = WithdrawByBankDBHandler.getInstance();
						
						WithdrawReqByBank wdByBank = dbHandler.getWithdrawReqByBankById(dataItem.getAccountDetailsId());
						dataItem.setByBank(wdByBank);
					}
					dataList.add(dataItem);
				}
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if (totalRs != null) {
				totalRs.close();
			}
			if (rs != null) {
				rs.close();
			}
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
		holder.setList(dataList);
		
		return holder;
	}
	
	private String getReferenceNumber(int maxLen, long maxId) throws SQLException {
		StringBuilder sb = new StringBuilder(maxLen); 
		for (int i = 0; i < maxLen; i++) 
			sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
		
		sb.append(maxId);
		return sb.toString();
	}
	
	public List<String> getRecentWinRecords(long userProfileId, boolean isBoss, String bossUserName) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		ResultSet rs = null;
		
		String sql = LATEST_WD_RECORDS;
		if (isBoss) {
			sql = LATEST_BOSS_WD_RECORDS;
		}
		PreparedStatement ps = dbConn.prepareStatement(sql);
		
		if (isBoss) {
			ps.setLong(1, userProfileId);
			ps.setInt(2, WithdrawReqState.CLOSED.getId());
		} else {
			ps.setInt(1, WithdrawReqState.CLOSED.getId());
		}
		
		List<String> winMessages = new ArrayList<>();
		String msg1 = "$NAME WITHDREW Rs.$AMT RECENTLY";
		String msg2 = "Your Referrer $NAME WITHDREW Rs.$AMT RECENTLY";
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					long userId = rs.getLong(USER_PROFILE_ID);
					int amt = rs.getInt(AMOUNT);
					//long dateTime = rs.getLong(DATE);
					
					String userName = null;
					String str = msg1;
					if (isBoss) {
						userName = bossUserName;
						str = msg2;
					} else {
						UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userId);
						userName = userProfile.getName();
					}
					
					str = str.replace("$NAME", userName);
					str = str.replace("$AMT", String.valueOf(amt));
					
					winMessages.add(str);
				}
			}
		} catch(SQLException ex) {
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return winMessages;
	}
	
	public static void main(String[] args) throws SQLException {
		WithdrawDBHandler instance = WithdrawDBHandler.getInstance();
		List<String> msgs = instance.getRecentWinRecords(24, true, "Raj24");
		for (String str : msgs) {
			System.out.println(str);
		}
	}
}
