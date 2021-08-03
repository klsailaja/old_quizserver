package com.ab.quiz;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.UserMoneyHandler;
import com.ab.quiz.helper.InMemUserMoneyManager;
import com.ab.quiz.pojo.TransferRequest;
import com.ab.quiz.pojo.UserMoney;

@RestController
public class UserMoneyController extends BaseController {
	private static final Logger logger = LogManager.getLogger(UserMoneyController.class);
	// get money
	// add money 
	// Transfer
	
	@RequestMapping(value = "/money/{userProfileId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserMoney getUserMoney(@PathVariable("userProfileId") long userProfileId) 
			throws InternalException, NotAllowedException {
		
		try {
			//return UserMoneyHandler.getInstance().getUserMoney(userProfileId);
			return InMemUserMoneyManager.getInstance().getUserMoneyById(userProfileId);
		} catch (SQLException ex) {
			logger.error("Exception in getUserMoney", ex);
			throw new InternalException("Server Error in getUserMoney");
		}
	}
	
	@RequestMapping(value = "/money/{userProfileId}/load/{amt}", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody boolean loadMoney(@PathVariable("userProfileId") long userProfileId, @PathVariable("amt") int amt,
			@RequestBody TransferRequest transferReq)
			throws InternalException {
		try {
			return UserMoneyHandler.getInstance().updateUserMoney(userProfileId, amt);
		} catch (SQLException ex) {
			logger.error("Exception in loadMoney", ex);
			throw new InternalException("Server Error in loadMoney");
		}
	}
	
	@RequestMapping(value = "/money/{userProfileId}/transfer", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody boolean transferMoney(@PathVariable("userProfileId") long userProfileId, 
			@RequestBody TransferRequest transferReq) throws NotAllowedException, InternalException {
		try {
			return UserMoneyHandler.getInstance().transferMoney(userProfileId, transferReq);
		} catch (SQLException ex) {
			logger.error("Exception in transferMoney", ex);
			throw new InternalException("Server Error in transferMoney");
		}
	}
}
