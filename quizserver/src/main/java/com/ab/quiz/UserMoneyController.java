package com.ab.quiz;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;

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
			logger.error("Exception in getUserMoney", ex);
			throw new InternalException("Server Error in getUserMoney");
		}
	}
	
	@RequestMapping(value = "/money/{userProfileId}/load/{amt}", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody boolean loadMoney(@PathVariable("userProfileId") long userProfileId, @PathVariable("amt") int amt,
			@RequestBody TransferRequest transferReq)
			throws InternalException {
		
		UsersCompleteMoneyDetails completeDetails = new UsersCompleteMoneyDetails();
		
		List<MoneyTransaction> loadMoneyTransactions = new ArrayList<>();
		
		MoneyTransaction loadTransaction = new MoneyTransaction();
		loadTransaction.setUserProfileId(userProfileId);
		loadTransaction.setAccountType(UserMoneyAccountType.LOADED_MONEY);
		loadTransaction.setOperType(UserMoneyOperType.ADD);
		loadTransaction.setAmount(amt);
		
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
			logger.error("Exception in loadMoney", ex);
			throw new InternalException("Server Error in loadMoney");
		}
	}
	
	@RequestMapping(value = "/money/update/{trackKey}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody int getGamesSlotMoneyStatus(@PathVariable("trackKey") String trackKey) throws InternalException {
		String newTrackKey = "server" + QuizConstants.MY_SERVER_ID + "-" + trackKey;
		try {
			GetTask<Integer> slotGamesStatusTask = Request.getGamesSlotMoneyStatus(newTrackKey);
			Object result = slotGamesStatusTask.execute();
			return (Integer) result;
		} catch (Exception ex) {
			logger.error("Exception in getGamesSlotMoneyStatus", ex);
			throw new InternalException("Server Error in getGamesSlotMoneyStatus");
		}
	}
}
