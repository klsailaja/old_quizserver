package com.ab.quiz.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.InMemUserMoneyManager;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawReqByPhone;
import com.ab.quiz.tasks.AddTransactionsTask;

/*
CREATE TABLE USERMONEY (ID BIGINT UNSIGNED NOT NULL, 
		LOADEDAMOUNT BIGINT, 
		WINNINGAMOUNT BIGINT, 
		REFERALAMOUNT BIGINT, 
		LOADEDAMTLOCKED BIGINT,
		WINNINGAMTLOCKED BIGINT,
		REFERALAMTLOCKED BIGINT, PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class UserMoneyDBHandler {
	private static final Logger logger = LogManager.getLogger(UserMoneyDBHandler.class);
	// create a record
	// get the record
	// update 
	public static String ID = "id";
	public static String LOADED_AMOUNT = "LOADEDAMOUNT";
	public static String WINNING_AMOUNT = "WINNINGAMOUNT";
	public static String REFERAL_AMOUNT = "REFERALAMOUNT";
	public static String LOADED_AMOUNT_LOCKED = "LOADEDAMTLOCKED";
	public static String WINNING_AMOUNT_LOCKED = "WINNINGAMTLOCKED";
	public static String REFERAL_AMOUNT_LOCKED = "REFERALAMTLOCKED";
	
	
	private static UserMoneyDBHandler instance = null;
	
	private static final String CREATE_MONEY_ENTRY = "INSERT INTO USERMONEY " 
			+ "(" + ID + "," + LOADED_AMOUNT + "," + WINNING_AMOUNT + ","
			+ REFERAL_AMOUNT + "," + LOADED_AMOUNT_LOCKED + "," 
			+ WINNING_AMOUNT_LOCKED + "," + REFERAL_AMOUNT_LOCKED
			+ ") VALUES" + "(?,?,?,?,?,?,?)";
	
	public static final String GET_MONEY_ENTRY_BY_USER_ID = "SELECT * FROM USERMONEY WHERE " 
			+ ID + " = ?";
	
	public static final String UPDATE_LOADED_AMOUNT_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? WHERE " + ID + " = ? ";
	
	public static final String UPDATE_WINNING_AMOUNT_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ WINNING_AMOUNT + " = " + WINNING_AMOUNT + " + ? WHERE " + ID + " = ? ";
	
	public static final String UPDATE_REFERAL_AMOUNT_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ REFERAL_AMOUNT + " = " + REFERAL_AMOUNT + " + ? WHERE " + ID + " = ? ";
	
	private static final String TRANSFER_AMOUNT_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ WINNING_AMOUNT + " = " + WINNING_AMOUNT + " + ? , "
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? "
			+ "WHERE " + ID + " = ? ";
	
	private static final String TRANSFER_RF_AMOUNT_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ REFERAL_AMOUNT + " = " + REFERAL_AMOUNT + " + ? , "
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? "
			+ "WHERE " + ID + " = ? ";
	
	public static final String WITHDRAW_LOADED_MONEY_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ LOADED_AMOUNT + " = " + LOADED_AMOUNT + " + ? , "
			+ LOADED_AMOUNT_LOCKED + " = " + LOADED_AMOUNT_LOCKED + " + ? "
			+ "WHERE " + ID + " = ? ";
	
	public static final String WITHDRAW_WINNING_MONEY_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ WINNING_AMOUNT + " = " + WINNING_AMOUNT + " + ? , "
			+ WINNING_AMOUNT_LOCKED + " = " + WINNING_AMOUNT_LOCKED + " + ? "
			+ "WHERE " + ID + " = ? ";
	
	public static final String WITHDRAW_REFERAL_MONEY_BY_USER_ID = "UPDATE USERMONEY SET " 
			+ REFERAL_AMOUNT + " = " + REFERAL_AMOUNT + " + ? , "
			+ REFERAL_AMOUNT_LOCKED + " = " + REFERAL_AMOUNT_LOCKED + " + ? "
			+ "WHERE " + ID + " = ? ";
	
	public static final String GET_FIFTY_ENTRY_SET = "SELECT * FROM USERMONEY "    
			+ " WHERE " + ID + " IN (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static final String GET_TWENTY_ENTRY_SET = "SELECT * FROM USERMONEY "    
			+ " WHERE " + ID + " IN (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	
	
	private UserMoneyDBHandler() {
	}
	
	public static UserMoneyDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In UserMoneyDBHandler getInstance() method instance created");
			instance = new UserMoneyDBHandler();
		}
		return instance;
	}
	
	public void testCreateMoneyInBatch(List<UserMoney> userMoneyList, int batchSize) throws SQLException {
		
		System.out.println("In testCreateMoneyInBatch with size " +  userMoneyList.size() + " batch size " + batchSize);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		int totalFailureCount = 0;
		int totalSuccessCount = 0;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_MONEY_ENTRY);
			
			int index = 0;
			for (UserMoney userMoney : userMoneyList) {
				
				ps.setLong(1, userMoney.getId());
				ps.setLong(2, userMoney.getLoadedAmount());
				ps.setLong(3, userMoney.getWinningAmount());
				ps.setLong(4, userMoney.getReferalAmount());
				ps.setLong(5, userMoney.getLoadedAmtLocked());
				ps.setLong(6, userMoney.getWinningAmtLocked());
				ps.setLong(7, userMoney.getReferalAmtLocked());
			
				ps.addBatch();
				index++;
				
				if (index % batchSize == 0) {
					int results[] = ps.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
					for (int result : results) {
						if (result == 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = ps.executeBatch();
				dbConn.setAutoCommit(true);
				for (int result : results) {
					if (result == 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("End of testCreateMoneyInBatch with success row count {} : failure row count {}", totalSuccessCount, totalFailureCount);
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error in creating user money list in bulk mode", ex);
			logger.error("******************************");
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
	
	public UserMoney createUserMoney(UserMoney userMoney) throws SQLException {
		
		logger.debug("In createUserMoney with {}", userMoney.getId());
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_MONEY_ENTRY);
			
			ps.setLong(1, userMoney.getId());
			ps.setLong(2, userMoney.getLoadedAmount());
			ps.setLong(3, userMoney.getWinningAmount());
			ps.setLong(4, userMoney.getReferalAmount());
			ps.setLong(5, userMoney.getLoadedAmtLocked());
			ps.setLong(6, userMoney.getWinningAmtLocked());
			ps.setLong(7, userMoney.getReferalAmtLocked());
			int result = ps.executeUpdate();
			logger.info("createUserMoney with id {} result is {}", userMoney.getId(), (result > 0));
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error creating user money for id {} ", userMoney.getId());
			logger.error("The Exception is", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return getUserMoneyById(userMoney.getId());
	}
	
	public UserMoney getUserMoneyById(long id) throws SQLException {
		
		logger.debug("In getUserMoneyById() with {}", id);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_MONEY_ENTRY_BY_USER_ID);
		ps.setLong(1, id);
		
		ResultSet rs = null;
		
		UserMoney userMoney = new UserMoney();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					userMoney.setId(rs.getLong(ID));
					userMoney.setLoadedAmount(rs.getLong(LOADED_AMOUNT));
					userMoney.setWinningAmount(rs.getLong(WINNING_AMOUNT));
					userMoney.setReferalAmount(rs.getLong(REFERAL_AMOUNT));
					userMoney.setLoadedAmtLocked(rs.getLong(LOADED_AMOUNT_LOCKED));
					userMoney.setWinningAmtLocked(rs.getLong(WINNING_AMOUNT_LOCKED));
					userMoney.setReferalAmtLocked(rs.getLong(REFERAL_AMOUNT_LOCKED));
				}
			}
		} catch (SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while getting the UserMoney for id {}", id);
			logger.error("SQLException in getUserMoneyById()", ex);
			logger.error("******************************");
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
		return userMoney;
	}
	
	public boolean updateUserMoney(UserMoneyAccountType accountType, UserMoneyOperType operType, 
			long id, long amt, MyTransaction transaction)
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
		
		if (qry == null) {
			throw new RuntimeException("No query defined to handle the account type " + accountType);
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(qry);
		
		amt = (operType == UserMoneyOperType.SUBTRACT) ? -1 * amt : amt;  
		
		ps.setLong(1, amt);
		ps.setLong(2, id);
		int operResult = 0;
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while Updating UserMoney for id {}", id);
			logger.error("SQLException in ", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
			transaction.setOperResult(operResult);
			MyTransactionDBHandler.getInstance().createTransaction(transaction);
		}
		return true;
	}
	
	public List<MyTransaction> getTransactionObjects(long userId, int amount, List<UserMoneyAccountType> accountTypeList,
			List<TransactionType> transactionTypes, List<String> comments) {
		
		logger.info("This is in getTransactionObjects method");
		List<MyTransaction> transactionList = new ArrayList<>();

		try {
			UserMoney userMoney = InMemUserMoneyManager.getInstance().getUserMoneyById(userId);
			
			for (int index = 0; index < accountTypeList.size(); index ++) {
				
				UserMoneyAccountType accountType = accountTypeList.get(index); 
				
				long userOB = userMoney.getLoadedAmount();
				if (accountType.getId() == UserMoneyAccountType.WINNING_MONEY.getId()) {
					userOB = userMoney.getWinningAmount();
				} else if (accountType.getId() == UserMoneyAccountType.REFERAL_MONEY.getId()) {
					userOB = userMoney.getReferalAmount();
				}
				
				long userCB = userOB - amount;
				TransactionType transactionType = transactionTypes.get(index);
				if (transactionType.getId() == TransactionType.CREDITED.getId()) {
					userCB = userOB + amount;
				}
				String comment = comments.get(index);
				
				MyTransaction transaction = Utils.getTransactionPojo(userId, System.currentTimeMillis(), 
						amount, transactionType.getId(), accountType.getId(), userOB, userCB, comment);
				transaction.setIsWin(0);
				transactionList.add(transaction);
			}
		} catch(SQLException ex) {
			logger.error("Exception in getTransactionObjects", ex);
		}
		logger.info("The final size of Transactions created is {}", transactionList.size());
		return transactionList;
	}
	
	private String getAccountName(int accType) {
		switch (accType) {
			case 1 : {
				return "MAIN";
			}
			case 2 : {
				return "WINNING";
			}
			case 3 : {
				return "REFERRAL";
			}
		}
		return null;
	}
	
	
	public boolean transferAmount(long userProfileId, TransferRequest transferReq) throws SQLException {
		
		int amt = transferReq.getAmount();
		String sqlQry = transferReq.getSqlQry();
		
		logger.debug("transferAmount called with id {} amt {}", userProfileId, amt);
		
		boolean hasInMemRecords = InMemUserMoneyManager.getInstance().hasInMemRecords(userProfileId);
		if (hasInMemRecords) {
			InMemUserMoneyManager.getInstance().commitNow();
		}
		
		List<UserMoneyAccountType> accTypes = new ArrayList<>();
		accTypes.add(UserMoneyAccountType.findById(transferReq.getSourceAccType()));
		accTypes.add(UserMoneyAccountType.findById(transferReq.getDestAccType()));
		
		List<TransactionType> transactionTypes = new ArrayList<>();
		transactionTypes.add(TransactionType.DEBITED);
		transactionTypes.add(TransactionType.CREDITED);
		
		String srcAccountName = getAccountName(transferReq.getSourceAccType());
		String destAccountName = getAccountName(transferReq.getDestAccType());
		
		List<String> comments = new ArrayList<>();
		comments.add("Transferred to " + destAccountName + " Account");
		comments.add("Transferred from " + srcAccountName + " Account");
		
		List<MyTransaction> transferRelatedTransactions = getTransactionObjects(userProfileId, amt, 
				accTypes, transactionTypes, comments);
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		sqlQry = sqlQry + " WHERE " + ID + " = ? ";   
		
		PreparedStatement ps = dbConn.prepareStatement(sqlQry);
		
		logger.info("The qry is {}", sqlQry);
		
		ps.setLong(1, amt * -1);
		ps.setLong(2, amt);
		ps.setLong(3, userProfileId);
		
		try {
			int resultCount = ps.executeUpdate();
			logger.debug("The updated row count {}", resultCount);
			
			int transferRes = 0;
			if (resultCount > 0) {
				transferRes = 1;
			}
			for (MyTransaction transaction : transferRelatedTransactions) {
				transaction.setOperResult(transferRes);
			}
			LazyScheduler.getInstance().submit(new AddTransactionsTask(transferRelatedTransactions));
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
	
	public boolean addWithdrawMoneyReq(WDUserInput wdUserInput, WithdrawReqByPhone byPhoneReq) 
			throws SQLException {
		
		logger.info("addWithdrawMoneyReq called with id {} amt {}", wdUserInput.getUserProfileId(), wdUserInput.getAmount());
		WithdrawByPhoneReqDBHandler phoneDBHandler = WithdrawByPhoneReqDBHandler.getInstance();
		long phoneRecordId = phoneDBHandler.createReqByPhone(byPhoneReq);
		if (phoneRecordId == -1) {
			throw new SQLException("New Phone WithDraw Request not created for " + wdUserInput.getUserProfileId());
		}
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		String sqlQry = WITHDRAW_LOADED_MONEY_BY_USER_ID;
		
		int accountType = wdUserInput.getFromAccType();
		UserMoneyAccountType userReqAccType = UserMoneyAccountType.findById(accountType);
		if (userReqAccType == null) {
			throw new NotAllowedException("Unknown Accont Type");
		}
		
		switch (userReqAccType) {
			case LOADED_MONEY: {
				sqlQry = WITHDRAW_LOADED_MONEY_BY_USER_ID;
				break;
			}
			case WINNING_MONEY: {
				sqlQry = WITHDRAW_WINNING_MONEY_BY_USER_ID;
				break;
			}
			case REFERAL_MONEY: {
				sqlQry = WITHDRAW_REFERAL_MONEY_BY_USER_ID;
				break;
			}
		}
		
		PreparedStatement ps = dbConn.prepareStatement(sqlQry);
		
		logger.debug("The qry is {}", sqlQry);
		
		int amt = wdUserInput.getAmount();
		
		ps.setLong(1, amt * -1);
		ps.setLong(2, amt);
		ps.setLong(3, wdUserInput.getUserProfileId());
		
		try {
			int resultCount = ps.executeUpdate();
			logger.info("withdraw request for id {} and amt {} created status {}", 
					wdUserInput.getUserProfileId(), wdUserInput.getAmount(), (resultCount > 0));
		}
		catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while creating withdraw request for id {}", wdUserInput.getUserProfileId());
			logger.error("SQLException in addWithdrawMoneyReq()", ex);
			logger.error("******************************");

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

	
	/*
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
	} */
	
	public static void main(String[] args) throws SQLException {
		
		/*UserMoneyDBHandler userMoneyDBHandler = UserMoneyDBHandler.getInstance();
		UserMoney userMoney = userMoneyDBHandler.getUserMoneyById(70);*/
		
		
		/*UserMoney userMoney = new UserMoney();
		userMoney.setUserId(1);
		userMoney.setLoadedAmount(100);
		userMoney.setWinningAmount(100);
		userMoney.setReferalAmount(50);*/
		
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
		
		/*boolean result = userMoneyDBHandler.transferAmount(1, 30, 2);
		System.out.println("Load amt add result " + result);
		UserMoney dbUserObj = userMoneyDBHandler.getUserMoneyByProfileId(1);
		System.out.println(dbUserObj);*/
	}
}
