package com.ab.quiz.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.PhonePaymentTypes;
import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.constants.WithdrawReqType;
import com.ab.quiz.db.ConnectionPool;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;
import com.ab.quiz.db.WithdrawReceiptDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.InMemUserMoneyManager;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawReqByBank;
import com.ab.quiz.pojo.WithdrawReqByPhone;
import com.ab.quiz.pojo.WithdrawRequestInput;
import com.ab.quiz.pojo.WithdrawRequestsHolder;
import com.ab.quiz.tasks.AddTransactionsTask;

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
	
	public UserMoney getUserMoney(long id) throws SQLException, NotAllowedException {
		//UserMoney userMoneyDb = UserMoneyDBHandler.getInstance().getUserMoneyByProfileId(userProfileId);
		UserMoney userMoneyDb = InMemUserMoneyManager.getInstance().getUserMoneyById(id);
		return userMoneyDb;
	}
	
	public boolean updateUserMoney (long userProfileId, int amt) throws SQLException {
		logger.info("This is in updateUserMoney {} : {}", userProfileId, amt);
		UserMoneyAccountType accountType = UserMoneyAccountType.LOADED_MONEY;
		UserMoneyOperType operType = UserMoneyOperType.ADD;
		long longAmt = amt;
		
		UserMoney userMoney = UserMoneyHandler.getInstance().getUserMoney(userProfileId);
		long userOB = userMoney.getLoadedAmount();
		long userCB = longAmt + userOB;
		long currentTime = System.currentTimeMillis();
		String comments = "Loaded Money";
		
		MyTransaction transaction = Utils.getTransactionPojo(userProfileId,currentTime, 
				amt, TransactionType.CREDITED.getId(), accountType.getId(), userOB, userCB,
				comments);
		return UserMoneyDBHandler.getInstance().updateUserMoney(accountType, operType, userProfileId, 
				longAmt, transaction);
	}

	public boolean transferMoney(long userProfileId, TransferRequest transferReq) 
			throws NotAllowedException, SQLException {
		
		UserMoneyAccountType accType = UserMoneyAccountType.findById(transferReq.getSourceAccType());
		if (accType == null) {
			throw new NotAllowedException("Specified account type not found");
		}
		UserMoney userMoney = getUserMoney(userProfileId);
		long accountMoney = userMoney.getLoadedAmount();
		if (accType == UserMoneyAccountType.WINNING_MONEY) {
			accountMoney = userMoney.getWinningAmount();
		}
		if (accType == UserMoneyAccountType.REFERAL_MONEY) {
			accountMoney = userMoney.getReferalAmount();
		}
		if (transferReq.getAmount() > accountMoney) {
			throw new NotAllowedException("Enough money not present in the account");
		}
		return UserMoneyDBHandler.getInstance().transferAmount(userProfileId, transferReq); 
				
	}
	
	// Withdraw support methods
	
	private boolean isUserEligibleForWD(WithdrawRequestInput wdInputObject)
		throws NotAllowedException, SQLException {
		
		WDUserInput wdUserInput = wdInputObject.getWithdrawUserInput();
		
		long userProfileId = wdUserInput.getUserProfileId();
		
		boolean hasInMemRecords = InMemUserMoneyManager.getInstance().hasInMemRecords(wdUserInput.getUserProfileId());
		if (hasInMemRecords) {
			InMemUserMoneyManager.getInstance().commitNow();
		}
		
		UserMoney userMoneyDb = UserMoneyDBHandler.getInstance().getUserMoneyById(userProfileId);
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
		
		logger.info("Withdraw Amount and Account Money {} and {}", wdUserInput.getAmount(), accountMoney);
		
		if (wdUserInput.getAmount() > accountMoney) {
			throw new NotAllowedException("Withdraw Amount is more than available amount");
		}
		
		if (wdUserInput.getRequestType() == WithdrawReqType.BY_PHONE.getId()) {
			WithdrawReqByPhone byPhone = wdInputObject.getByPhoneDetails();
			if ((byPhone.getPhNumber() == null) || (byPhone.getPhNumber().length() == 0)) {
				throw new NotAllowedException("Phone number is empty");
			}
			PhonePaymentTypes phonePayType = PhonePaymentTypes.findById(byPhone.getPaymentMethod());
			if (phonePayType == null) {
				throw new NotAllowedException("Invalid phone pay method");
			}
			if ((byPhone.getAccountHolderName() == null) || (byPhone.getAccountHolderName().length() == 0)) {
				throw new NotAllowedException("User Name is empty");
			}
		} else if (wdUserInput.getRequestType() == WithdrawReqType.BY_BANK.getId()) {
			
			WithdrawReqByBank byBank = wdInputObject.getByBankDetails();
			
			if ((byBank.getAccountNumber() == null) || (byBank.getAccountNumber().length() == 0)) {
				throw new NotAllowedException("Bank Account Number is empty");
			}
			if ((byBank.getIfscCode() == null) || (byBank.getIfscCode().length() == 0)) {
				throw new NotAllowedException("Bank Ifsc Number is empty");
			}
			if ((byBank.getBankName() == null) || (byBank.getBankName().length() == 0)) {
				throw new NotAllowedException("Bank Name is empty");
			}
			if ((byBank.getUserName() == null) || (byBank.getUserName().length() == 0)) {
				throw new NotAllowedException("User Name is empty");
			}
		}
		return true;
	}
	
	public String getUserAccWithDrawSql(int accType) {
		
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
	
	public boolean placeWithdrawMoneyRequest(WithdrawRequestInput wdInputObject)
		throws NotAllowedException, SQLException {
		
		boolean validOper = isUserEligibleForWD(wdInputObject);
		
		if (!validOper) {
			return false;
		}
		
		WDUserInput wdUserInput = wdInputObject.getWithdrawUserInput();
		WithdrawReqByPhone byPhoneReq = wdInputObject.getByPhoneDetails();
		WithdrawReqByBank byBankReq = wdInputObject.getByBankDetails();
		if (byPhoneReq != null) {
			byPhoneReq.setAccountHolderName(byPhoneReq.getAccountHolderName().trim());
			byPhoneReq.setPhNumber(byPhoneReq.getPhNumber().trim());
		}
		if (byBankReq != null) {
			byBankReq.setAccountNumber(byBankReq.getAccountNumber().trim());
			byBankReq.setIfscCode(byBankReq.getIfscCode().trim());
			byBankReq.setUserName(byBankReq.getUserName().trim());
			byBankReq.setBankName(byBankReq.getBankName().trim());
		}
		
		logger.info("WithdrawMoney is called with inputs {} and {} and {}", wdUserInput, byPhoneReq, byBankReq);
		
		WithdrawDBHandler wdDBHandler = WithdrawDBHandler.getInstance();
		boolean wdrecordsCreated = wdDBHandler.createWithDrawReq(wdUserInput, byPhoneReq, byBankReq);
		if (!wdrecordsCreated) {
			logger.debug("Withdraw DB Records not created in DB");
			throw new NotAllowedException("Could not insert beneficiary details to DB");
		}
		
		String wdSql = getUserAccWithDrawSql(wdUserInput.getFromAccType());
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		long userProfileId = wdUserInput.getUserProfileId();
		
		int userAccountType = wdUserInput.getFromAccType();
		UserMoneyAccountType accType = UserMoneyAccountType.findById(userAccountType);
		if (accType == null) {
			throw new NotAllowedException("Unknown Account Type: " + userAccountType);
		}
		
		List<UserMoneyAccountType> accTypes = new ArrayList<>();
		accTypes.add(accType);
				
		List<TransactionType> transactionTypes = new ArrayList<>();
		transactionTypes.add(TransactionType.DEBITED);
				
		List<String> comments = new ArrayList<>();
		comments.add("Withdraw Request Placed");
				
		List<MyTransaction> wdRelatedTransactions = UserMoneyDBHandler.getInstance(). 
			getTransactionObjects(wdUserInput.getUserProfileId(), wdUserInput.getAmount(), 
						accTypes, transactionTypes, comments);

		int transferRes = 0;
		
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
				transferRes = 1;
			}
			logger.debug("WithdrawMoney createResult {}", createResult);
			for (MyTransaction transaction : wdRelatedTransactions) {
				transaction.setOperResult(transferRes);
			}
			
			LazyScheduler.getInstance().submit(new AddTransactionsTask(wdRelatedTransactions));
		} catch(SQLException ex) {
			logger.error("Error while executing withdraw lock money statement", ex);
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
	
	public boolean cancelWithdrawRequest(long userProfileId, String withdrawRefId) 
			throws SQLException,NotAllowedException {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.cancelWithdrawRequest(userProfileId, withdrawRefId);
	}
	
	public boolean closeWithDrawRequest(String receiptFileName, String withdrawRefId, String wdClosedCmts) 
			throws SQLException, NotAllowedException, FileNotFoundException {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.closeWithDrawRequest(receiptFileName, withdrawRefId, wdClosedCmts);
	}
	
	public WithdrawRequestsHolder getWithdrawDataSet(long userProfileId, int startRowNumber, int state) 
			throws SQLException, NotAllowedException {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.getWithdrawRequests(userProfileId, startRowNumber, state);
	}
	
	public byte[] getReceiptContents(long id) throws NotAllowedException, SQLException {
		WithdrawReceiptDBHandler wdReceiptDBHander = WithdrawReceiptDBHandler.getInstance();
		return wdReceiptDBHander.getReceiptContents(id);
	}
	
	public static void main(String[] args) throws NotAllowedException, SQLException, FileNotFoundException {
		
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
		
		WithdrawRequestInput wdInputs = new WithdrawRequestInput();
		wdInputs.setWithdrawUserInput(wdInput);
		wdInputs.setByPhoneDetails(byPhone);
		
		UserMoneyHandler handler = UserMoneyHandler.getInstance();
		/*boolean result = handler.placeWithdrawMoneyRequest(wdInputs);
		System.out.println("result is :" + result);*/
		
		/*boolean result = handler.cancelWithdrawRequest(70, "htheFhBQy1");
		System.out.println("The WD Cancellation status is " + result);*/
		
		String filePath = "D:" + File.separator + "Projects" + File.separator + "Receipt.png";
		
		boolean result = handler.closeWithDrawRequest(filePath, "VrRzWzEjl3", "Paid in Phone");
		System.out.println("The WD Closure result is " + result);
		
		/*WithdrawRequestsHolder holder = handler.getWithdrawDataSet(70, 0, -1);
		System.out.println(holder);*/
	}
}
