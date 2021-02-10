package com.ab.quiz.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.constants.WithdrawReqType;
import com.ab.quiz.db.ConnectionPool;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawReqByPhone;
import com.ab.quiz.tasks.CreateTransactionTask;

public class UserMoneyHandler {
	private static final Logger logger = LogManager.getLogger(UserMoneyHandler.class);
	private static UserMoneyHandler instance = null;
	
	private UserMoneyHandler() {
	}
	
	public static UserMoneyHandler getInstance() {
		if (instance == null) {
			logger.debug("In UserMoneyHandler getInstance() method instance created");
			instance = new UserMoneyHandler();
		}
		return instance;
	}
	
	public UserMoney getUserMoney(long userProfileId) throws SQLException, NotAllowedException {
		UserMoney userMoneyDb = UserMoneyDBHandler.getInstance().getUserMoneyByProfileId(userProfileId);
		if (userMoneyDb.getId() == 0) {
			// The entry is not found in DB. So create one..
			UserMoney userMoney = new UserMoney();
			
			userMoney.setId(userProfileId);
			userMoney.setLoadedAmount(0);
			userMoney.setReferalAmount(0);
			userMoney.setWinningAmount(0);
			userMoney.setLoadedAmtLocked(0);
			userMoney.setReferalAmtLocked(0);
			userMoney.setWinningAmtLocked(0);
			
			UserMoneyDBHandler.getInstance().createUserMoney(userMoney);
			return userMoney;
		}
		return userMoneyDb;
	}
	
	public boolean updateUserMoney (long userProfileId, int amt) throws SQLException {
		
		UserMoneyAccountType accountType = UserMoneyAccountType.LOADED_MONEY;
		UserMoneyOperType operType = UserMoneyOperType.ADD;
		long longAmt = amt;
		
		UserMoney userMoney = UserMoneyHandler.getInstance().getUserMoney(userProfileId);
		long userOB = userMoney.getLoadedAmount();
		long userCB = longAmt + userOB;
		long currentTime = System.currentTimeMillis();
		String comments = "Loaded Money";
		
		MyTransaction transaction = Utils.getTransactionPojo(userProfileId,currentTime, 
				amt, TransactionType.CREDITED.getId(), TransactionType.LOADED.getId(), userOB, userCB,
				comments);
		return UserMoneyDBHandler.getInstance().updateUserMoney(accountType, operType, userProfileId, 
				longAmt, transaction);
	}

	public boolean transferMoney(long userProfileId, TransferRequest transferReq) 
			throws NotAllowedException, SQLException {
		
		UserMoneyAccountType accType = UserMoneyAccountType.findById(transferReq.getAccType());
		if (accType == null) {
			throw new NotAllowedException("Specified account type not found");
		}
		UserMoney userMoney = getUserMoney(userProfileId);
		long accountMoney = userMoney.getWinningAmount();
		if (accType == UserMoneyAccountType.REFERAL_MONEY) {
			accountMoney = userMoney.getReferalAmount();
		}
		if (transferReq.getAmount() > accountMoney) {
			throw new NotAllowedException("Enough money not present in the account");
		}
		return UserMoneyDBHandler.getInstance().transferAmount(userProfileId, transferReq.getAmount(), 
				transferReq.getAccType());
	}
	
	// Withdraw support methods
	
	private boolean isUserEligibleForWD(WDUserInput wdUserInput, WithdrawReqByPhone byPhoneReq)
		throws NotAllowedException, SQLException {
		
		long userProfileId = wdUserInput.getUserProfileId();
		UserMoney userMoneyDb = UserMoneyDBHandler.getInstance().getUserMoneyByProfileId(userProfileId);
		if (userMoneyDb.getId() == 0) {
			throw new NotAllowedException("Invalid user");
		}
		logger.info("The user money DB entry is {}", userMoneyDb);
		
		int accountType = wdUserInput.getFromAccType();
		UserMoneyAccountType userReqAccType = UserMoneyAccountType.findById(accountType);
		if (userReqAccType == null) {
			throw new NotAllowedException("Unknown Accont Type");
		}
		
		long accountMoney = 0;
		switch (userReqAccType) {
			case LOADED_MONEY: {
				accountMoney = userMoneyDb.getLoadedAmount();
				break;
			}
			case WINNING_MONEY: {
				accountMoney = userMoneyDb.getWinningAmount();
				break;
			}
			case REFERAL_MONEY: {
				accountMoney = userMoneyDb.getReferalAmount();
				break;
			}
		}
		
		logger.debug("Withdraw Amount and Account Money {} and {}", wdUserInput.getAmount(), accountMoney);
		
		if (wdUserInput.getAmount() > accountMoney) {
			throw new NotAllowedException("Withdraw Amount is more than available amount");
		}
		return true;
	}
	
	private String getUserAccWithDrawSql(int accType) {
		
		String wdSqlQry = UserMoneyDBHandler.WITHDRAW_LOADED_MONEY_BY_USER_ID; 
		UserMoneyAccountType userReqAccType = UserMoneyAccountType.findById(accType);
		
		switch (userReqAccType) {
			case WINNING_MONEY: {
				wdSqlQry = UserMoneyDBHandler.WITHDRAW_WINNING_MONEY_BY_USER_ID;
				break;
			}
			case REFERAL_MONEY: {
				wdSqlQry = UserMoneyDBHandler.WITHDRAW_REFERAL_MONEY_BY_USER_ID;
				break;
			}
			default:
				break;
		}
		return wdSqlQry;
	}
	
	public boolean withdrawMoney(WDUserInput wdUserInput, WithdrawReqByPhone byPhoneReq)
		throws NotAllowedException, SQLException {
		
		logger.info("WithdrawMoney is called with inputs {} and {}", wdUserInput, byPhoneReq);
		boolean validOper = isUserEligibleForWD(wdUserInput, byPhoneReq);
		if (!validOper) {
			return false;
		}
		WithdrawDBHandler wdDBHandler = WithdrawDBHandler.getInstance();
		boolean wdrecordsCreated = wdDBHandler.createWithDrawReq(wdUserInput, byPhoneReq);
		if (!wdrecordsCreated) {
			logger.debug("Withdraw DB Records not created in DB");
			return false;
		}
		
		String wdSql = getUserAccWithDrawSql(wdUserInput.getFromAccType());
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		long userProfileId = wdUserInput.getUserProfileId();
		
		UserMoneyDBHandler userMoneyHandler = UserMoneyDBHandler.getInstance();
		UserMoney userMoney = userMoneyHandler.getUserMoneyByProfileId(userProfileId);
		
		long userOB = -1;
		int userAccountType = wdUserInput.getFromAccType();
		UserMoneyAccountType accType = UserMoneyAccountType.findById(userAccountType);
		if (accType == null) {
			throw new NotAllowedException("Unknown Account Type: " + userAccountType);
		}
		
		if (accType == UserMoneyAccountType.LOADED_MONEY) {
			userOB = userMoney.getLoadedAmount();
		} else if (accType == UserMoneyAccountType.REFERAL_MONEY) {
			userOB = userMoney.getReferalAmount();
		} else if (accType == UserMoneyAccountType.WINNING_MONEY) {
			userOB = userMoney.getWinningAmount();
		}
		
		long timeEntry = System.currentTimeMillis();
		String comments = "Withdraw Request Placed";
		long userCB = userOB - wdUserInput.getAmount(); 
		
		
		MyTransaction transaction = Utils.getTransactionPojo(userProfileId, timeEntry, 
				wdUserInput.getAmount(), TransactionType.WITHDRAW_IN_PROGRESS.getId(), 
				accType.getId(), userOB, userCB, comments);
		
		int operResult = 0;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(wdSql);
			
			int withdrawAmt = wdUserInput.getAmount();
			
			ps.setLong(1, (withdrawAmt * -1));
			ps.setLong(2, withdrawAmt);
			ps.setLong(3, userProfileId);
			
			int createResult = ps.executeUpdate();
			if (createResult > 0) {
				operResult = 1;
			}
			logger.debug(" withdrawMoney createResult {}", createResult);
		} catch(SQLException ex) {
			logger.error("Error while executing with draw lock money statement", ex);
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
	
	public static void main(String[] args) throws NotAllowedException, SQLException {
		
		WDUserInput wdInput = new WDUserInput();
		
		wdInput.setUserProfileId(70);
		wdInput.setFromAccType(UserMoneyAccountType.LOADED_MONEY.getId());
		wdInput.setRequestType(WithdrawReqType.BY_PHONE.getId());
		wdInput.setAmount(100);
		wdInput.setOpenedTime(System.currentTimeMillis());
		
		WithdrawReqByPhone byPhone = new WithdrawReqByPhone();
		byPhone.setPhNumber("9566229372");
		byPhone.setPaymentMethod(1);
		byPhone.setAccountHolderName("Rajasekhar");
		
		UserMoneyHandler handler = UserMoneyHandler.getInstance();
		boolean result = handler.withdrawMoney(wdInput, byPhone);
		System.out.println("result is :" + result);
	}
}
