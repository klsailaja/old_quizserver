package com.ab.quiz;

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
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.LoginData;
import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserProfile;

@RestController
public class UserProfileController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(UserProfileController.class);
	
	
	@RequestMapping(value = "/loggedin/count", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody long getLoggedInUserCount() throws InternalException {
		
		GetTask<Long> loggedInCountTask = Request.getLoggedInUsersCtTask();
		long count = -1;
		try {
			count = (long) loggedInCountTask.execute();
			return count;
		} catch (Exception ex) {
			logger.error("Exception while getting the logged in count from core server", ex);
			throw new InternalException("Server Error while getting the logged-in users count");
		}
	}
	
	
	// Tested.
	@RequestMapping(value = "/user/{email}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserProfile getUserProfile(@PathVariable("email") String email) 
			throws InternalException {
		
		GetTask<UserProfile> getUPByMailTask = Request.getUserProfileByMailIdTask(email);
		UserProfile userProfile = new UserProfile();
		try {
			userProfile = (UserProfile) getUPByMailTask.execute();
			return userProfile;
		} catch (Exception ex) {
			logger.error("Exception in getUserProfileByMailId", ex);
			throw new InternalException("Server Error in getUserProfileByMailId");
		}
		
	}
	
	@RequestMapping(value="/user/logout/{id}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String logout(@PathVariable("id") long id) throws InternalException {
		
		logger.info("logout called with {} ", id);
		GetTask<String> logoutTask = Request.getLogoutTask(id);
		String result = String.valueOf(false);
		try {
			result = (String)logoutTask.execute();
			return result;
		} catch (Exception ex) {
			logger.error("Exception in logout", ex);
			throw new InternalException("Server Error in logout");
		}
	}


	// Tested.
	@RequestMapping(value = "/forgot", method = RequestMethod.POST, produces = "application/json") 
	public @ResponseBody UserProfile forgotPassword(@RequestBody LoginData loginData) 
			throws NotAllowedException, InternalException {
		
		logger.info("forgotPassword is called with {}", loginData.getMailAddress());
		UserProfile userProfile = new UserProfile();
		PostTask<LoginData, UserProfile> forgotPasswdTask = Request.forgotPasswordTask();
		forgotPasswdTask.setPostObject(loginData);
		
		try {
			userProfile = (UserProfile) forgotPasswdTask.execute();
			return userProfile;
		} catch (Exception ex) {
			logger.error("Exception in forgotPassword", ex);
			throw new InternalException("Server Error in forgotPassword");
		}
	}
	
	// Tested.
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile updateUserProfile(@RequestBody UserProfile userProfile)
			throws NotAllowedException, InternalException {
		
		logger.info("updateUserProfile is called with {}", userProfile.getEmailAddress().trim());
		UserProfile newUserProfile = new UserProfile();
		PostTask<UserProfile, UserProfile> updateProfileTask = Request.updateProfileTask();
		updateProfileTask.setPostObject(userProfile);
		
		try {
			newUserProfile = (UserProfile) updateProfileTask.execute();
			return newUserProfile;
		} catch (Exception ex) {
			logger.error("Exception in updateUserProfile", ex);
			throw new InternalException("Server Error in updateUserProfile");
		}
	}
	
	@RequestMapping(value = "/user/mreferal/{myreferalcode}/{pageNum}", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody ReferalDetails getUserReferals(@PathVariable("myreferalcode") String referalCode,
			@PathVariable("pageNum") int pageNum) throws InternalException {
		
		logger.info("getUserReferals is called with code {} : pageNo {}", referalCode, pageNum);
		GetTask<ReferalDetails> getReferalsTask = Request.userReferralTask(referalCode, pageNum);
		
		try {
			ReferalDetails referalDetails = (ReferalDetails) getReferalsTask.execute();
			logger.info("Referals list size is {} for referal code {}", referalDetails.getReferalList().size(), referalCode);
			return referalDetails;
		} catch (Exception ex) {
			logger.error("Exception in getUserReferals", ex);
			throw new InternalException("Server Error in getUserReferals");
		}
		
	}
	
	
	@RequestMapping(value = "/user/transaction/{userProfileId}/{pageNum}/{accType}", method = RequestMethod.GET,
			produces = "application/json") 
	public @ResponseBody TransactionsHolder getTransactions(@PathVariable("userProfileId") long userProfileId,
			@PathVariable("pageNum") int pageNum, @PathVariable("accType") int accType) throws InternalException, NotAllowedException {
		
		logger.info("getTransactions is called with user id {} : pageNo {}", userProfileId, pageNum);
		GetTask<TransactionsHolder> getTransactionsTask = Request.getTransactionsTask(userProfileId, pageNum, accType);
		
		try {
			TransactionsHolder transactionsDetails = (TransactionsHolder) getTransactionsTask.execute();
			logger.info("Transactions list size is {} for user profile id {}", transactionsDetails.getTransactionsList().size(), userProfileId);
			return transactionsDetails;
		} catch (Exception ex) {
			logger.error("Exception in getTransactionsList", ex);
			throw new InternalException("Server Error in getTransactionsList");
		}
	}
	
	@RequestMapping(value = "/user/time/{clientTime}", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody String checkTimes(@PathVariable("clientTime") long clientTime) throws InternalException, NotAllowedException {
		long currentTime = System.currentTimeMillis();
		logger.info("Server Time Value is {} : and Client Time Value is {}", currentTime, clientTime);
		long diff = currentTime - clientTime;
		if (diff < -1) {
			diff = diff * -1;
		}
		logger.info("Time difference is {}", diff);
		if (diff <= 15000) {
			return "true";
		}
		return "false";
	}
}
