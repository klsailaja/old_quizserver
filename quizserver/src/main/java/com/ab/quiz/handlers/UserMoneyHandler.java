package com.ab.quiz.handlers;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.db.UserMoneyDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;

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
}
