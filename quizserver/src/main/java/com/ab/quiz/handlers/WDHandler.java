package com.ab.quiz.handlers;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.constants.PhonePaymentTypes;
import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.WithdrawReqState;
import com.ab.quiz.constants.WithdrawReqType;
import com.ab.quiz.db.WithdrawDBHandler;
import com.ab.quiz.db.WithdrawReceiptDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.KYCEntry;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.WDUserInput;
import com.ab.quiz.pojo.WithdrawMoney;
import com.ab.quiz.pojo.WithdrawReqByBank;
import com.ab.quiz.pojo.WithdrawReqByPhone;
import com.ab.quiz.pojo.WithdrawRequestInput;
import com.ab.quiz.pojo.WithdrawRequestsHolder;

public class WDHandler {
	private static final Logger logger = LogManager.getLogger(WDHandler.class);
	
	private static WDHandler instance = null;
	
	private WDHandler() {
	}
	
	public static WDHandler getInstance() {
		if (instance == null) {
			logger.debug("In WDHandler getInstance() method instance created");
			instance = new WDHandler();
		}
		return instance;
	}
	
	public WithdrawRequestsHolder getWithdrawDataSet(long userProfileId, int startRowNumber, int state) 
			throws SQLException, NotAllowedException {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.getWithdrawRequests(userProfileId, startRowNumber, state);
	}
	
	private boolean isUserEligibleForWD(WithdrawRequestInput wdInputObject)
			throws NotAllowedException, SQLException {
	
		WDUserInput wdUserInput = wdInputObject.getWithdrawUserInput();
	
		long userProfileId = wdUserInput.getUserProfileId();
		UserMoney userMoneyDb = null;
	
		try {
			GetTask<UserMoney> getUserMoneyTask = Request.getFullMoneyTask(userProfileId);
			userMoneyDb = (UserMoney)getUserMoneyTask.execute();
		} catch (Exception ex) {
			logger.error("Exception while getting the User Money for uid " + userProfileId, ex);
			throw new NotAllowedException("Backend issue while getting User Money Object");
		}
		
		if (userMoneyDb.getId() == 0) {
			throw new NotAllowedException("Invalid user");
		}
		logger.info("The user money DB entry is {}", userMoneyDb);
	
		long accountMoney = userMoneyDb.getAmount();
	
		logger.info("Withdraw Amount and Account Money {} and {}", wdUserInput.getAmount(), accountMoney);
	
		if (wdUserInput.getAmount() > accountMoney) {
			throw new NotAllowedException("Withdraw Amount is more than available amount");
		}
		if (userMoneyDb.getWinAmount() > 50000) {
			KYCEntry kycDocStatus = KYCHandler.getInstance().getKYCEntry(userProfileId);
			if (!kycDocStatus.getStatus().equalsIgnoreCase("approved")) {
				throw new NotAllowedException("Please complete the KYC Process to proceed");
			}
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
		
		MyTransaction transaction = Utils.getTransactionPojo(wdUserInput.getUserProfileId(), System.currentTimeMillis(), 
				wdUserInput.getAmount(), TransactionType.DEBITED.getId(), 
				UserMoneyAccountType.LOADED_MONEY.getId(), -1, -1, "Withdraw Request Placed", null);
		WithdrawMoney wdMoneyDetails = new WithdrawMoney();
		
		wdMoneyDetails.setUid(wdUserInput.getUserProfileId());
		wdMoneyDetails.setWdAmt(wdUserInput.getAmount());
		wdMoneyDetails.setWdType(WithdrawReqState.OPEN.getId());
		wdMoneyDetails.setTransaction(transaction);
		
		PostTask<WithdrawMoney, Boolean> wdTask = Request.performWitdrawTask();
		wdTask.setPostObject(wdMoneyDetails);
		
		try {
			boolean wdOperationResult = (boolean) wdTask.execute();
		
			if (!wdOperationResult) {
				return false;
			}
		} catch(Exception ex) {
			logger.error(ex);
			String errMessage = "Please retry after some time. Backend issue";
			if (ex instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) ex;
                errMessage = clientExp.getResponseBodyAsString();
            }
			throw new NotAllowedException(errMessage);
		}
		return true;
	}
	
	public boolean cancelWithdrawRequest(long userProfileId, String withdrawRefId) 
			throws SQLException,NotAllowedException {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.cancelWithdrawRequest(userProfileId, withdrawRefId);
	}
	
	public boolean closeWithDrawRequest(String receiptFileName, String withdrawRefId, String wdClosedCmts) 
			throws SQLException, NotAllowedException, FileNotFoundException,Exception {
		
		WithdrawDBHandler withdrawDbHandler = WithdrawDBHandler.getInstance();
		return withdrawDbHandler.closeWithDrawRequest(receiptFileName, withdrawRefId, wdClosedCmts);
	}
	
	public byte[] getReceiptContents(long id) throws NotAllowedException, SQLException {
		WithdrawReceiptDBHandler wdReceiptDBHander = WithdrawReceiptDBHandler.getInstance();
		return wdReceiptDBHander.getReceiptContents(id);
	} 
}
