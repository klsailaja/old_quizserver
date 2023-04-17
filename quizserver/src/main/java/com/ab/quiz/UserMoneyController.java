package com.ab.quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ab.quiz.common.GetTask;
import com.ab.quiz.common.PostTask;
import com.ab.quiz.common.Request;
import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.MoneyPayBackMode;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.TransactionType;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.pojo.LoadMoney;
import com.ab.quiz.pojo.MoneyStatusInput;
import com.ab.quiz.pojo.MoneyStatusOutput;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;
import com.ab.quiz.tasks.MoneyUpdaterResponseHandler;

@RestController
public class UserMoneyController extends BaseController {
	private static final Logger logger = LogManager.getLogger(UserMoneyController.class);
	// get money
	// add money 
	// Transfer
	
	@RequestMapping(value = "/money/{userProfileId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserMoney getUserMoney(@PathVariable("userProfileId") long userProfileId) 
			throws InternalException, NotAllowedException {
		
		GetTask<UserMoney> getUserMoneyTask = Request.getMoneyTask(userProfileId);
		try {
			UserMoney userMoney = (UserMoney) getUserMoneyTask.execute();
			return userMoney;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getUserMoney", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getUserMoney");
		}
	}
	
	@RequestMapping(value = "/fullmoney/{userProfileId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserMoney getFullUserMoney(@PathVariable("userProfileId") long userProfileId) 
			throws InternalException, NotAllowedException {
		
		GetTask<UserMoney> getUserMoneyTask = Request.getFullMoneyTask(userProfileId);
		try {
			UserMoney userMoney = (UserMoney) getUserMoneyTask.execute();
			return userMoney;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getFullMoneyTask", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getFullMoneyTask");
		}
	}
	
	@RequestMapping(value = "/money/load", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody boolean addMoney(@RequestBody LoadMoney loadObject)
			throws InternalException {
		
		logger.info("This is in addMoney for uid : {} and amount : {}", loadObject.getUid(), loadObject.getAmount());
		
		UsersCompleteMoneyDetails completeDetails = new UsersCompleteMoneyDetails();
		String logTag = TAGS.UPDATE_USER + " AddMoneyFromUser : sid : " 
				+ QuizConstants.MY_SERVER_ID + " : uid :" + loadObject.getUid();
		completeDetails.setLogTag(logTag);
		
		List<MoneyTransaction> loadMoneyTransactions = new ArrayList<>();
		
		MoneyTransaction loadTransaction = new MoneyTransaction();
		loadTransaction.setUserProfileId(loadObject.getUid());
		loadTransaction.setAccountType(UserMoneyAccountType.LOADED_MONEY);
		loadTransaction.setOperType(UserMoneyOperType.ADD);
		
		
		String transactionDesc = null;
		int spentNumber = -1;
		
		if (QuizConstants.getMoneyMode()) {
			transactionDesc = "Added Money : Rs." + loadObject.getAmount();
			loadTransaction.setAmount(loadObject.getAmount());
			spentNumber = loadObject.getAmount();
		} else {
			transactionDesc = "Bought " + loadObject.getCoinCount() + " Coins for : Rs." + loadObject.getAmount();
			spentNumber = loadObject.getCoinCount();
		}
		MyTransaction transaction = Utils.getTransactionPojo(loadObject.getUid(), 
				System.currentTimeMillis(), (int)spentNumber, TransactionType.CREDITED.getId(), 
				UserMoneyAccountType.LOADED_MONEY.getId(), -1, -1, transactionDesc, null);
		loadTransaction.setTransaction(transaction);
		
		loadMoneyTransactions.add(loadTransaction);
		
		completeDetails.setUsersMoneyTransactionList(loadMoneyTransactions);
		
		try {
			PostTask<UsersCompleteMoneyDetails, Integer[]> updateMoneyTask = Request.updateMoney();
			updateMoneyTask.setPostObject(completeDetails);
			Object result = updateMoneyTask.execute();
			List<Integer> resultList = Arrays.asList((Integer[]) result);
			boolean clientResult = false;
			if ((resultList != null) && (resultList.size() > 0)) {
				if (resultList.get(0) > 0) {
					return true;
				}
			}
			return clientResult;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in addMoney", logTag);
			logger.error("Exception is: ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error while adding money");
		}
	}
	
	@RequestMapping(value = "/money/update", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody MoneyStatusOutput getGamesSlotMoneyStatus(@RequestBody MoneyStatusInput statusInput) 
			throws InternalException {
		String tag = TAGS.WIN_MONEY;
		if (statusInput.getOperType() == MoneyPayBackMode.REFUND_CANCEL_GAMES.getId()) {
			tag = TAGS.REFUND_MONEY;
		}
		logger.info("{} Slot Money Status Query received for uid : {} game start time : {}", tag, 
				statusInput.getUid(),  
				new Date(statusInput.getGameSlotTime()));
		try {
			MoneyStatusOutput output = MoneyUpdaterResponseHandler.getInstance().getStatus(statusInput);
			logger.info("{} status : {} and msg : {}", tag, output.getStatus(), output.getMessage());
			return output;
		} catch(Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in getGamesSlotMoneyStatus", tag);
			logger.error("Exception is: ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getGamesSlotMoneyStatus");
		}
	}
}
