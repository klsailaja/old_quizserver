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
import com.ab.quiz.handlers.UserProfileHandler;
import com.ab.quiz.pojo.LoginData;
import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserProfile;

@RestController
public class UserProfileController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(UserProfileController.class);
	
	@RequestMapping(value = "/user/{email}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody UserProfile getUserProfile(@PathVariable("email") String email) 
			throws InternalException {
		
		try {
			return UserProfileHandler.getInstance().getUserProfileByMailId(email);
		} catch (SQLException ex) {
			logger.error("Exception in getUserProfileByMailId", ex);
			throw new InternalException("Server Error in getUserProfileByMailId");
		}
	}
	
	@RequestMapping(value="/user/login", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile login(@RequestBody LoginData loginData) throws InternalException {
		logger.info("login called with {} : {}", loginData.getMailAddress(), loginData.getPassword());
		try {
			UserProfile loginResult = UserProfileHandler.getInstance().login(loginData); 
			logger.info("login returned with {} : {} : {}", loginData.getMailAddress(), loginData.getPassword(), loginResult);
			return loginResult;
		} catch(SQLException ex) {
			logger.error("Exception in login", ex);
			throw new InternalException("Server Error in login");
		}
	}
	
	@RequestMapping(value = "/user", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile createUserProfile(@RequestBody UserProfile userProfile) 
			throws NotAllowedException, InternalException {
		
		logger.info("createUserProfile is called with {}", userProfile.getEmailAddress());
		
		try {
			UserProfile dbUserProfile = UserProfileHandler.getInstance().createUserProfile(userProfile); 
			logger.info("createUserProfile returning with {} and {}", dbUserProfile.getEmailAddress(), dbUserProfile.getId());
			return dbUserProfile;
			
		} catch (SQLException ex) {
			logger.error("Exception in createUserProfile", ex);
			throw new InternalException("Server Error in createUserProfile");
		}
	}
	
	@RequestMapping(value = "/user/mreferal/{myreferalcode}/{pageNum}", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody ReferalDetails getUserReferals(@PathVariable("myreferalcode") String referalCode,
			@PathVariable("pageNum") int pageNum) throws InternalException {
		logger.info("getUserReferals is called with code {} : pageNo {}", referalCode, pageNum);
		try {
			UserProfileHandler profileHandler = UserProfileHandler.getInstance(); 
			ReferalDetails referalDetails = profileHandler.getUserReferals(referalCode, pageNum);
			logger.info("Referals list size is {} for referal code {}", referalDetails.getReferalList().size(), referalCode);
			return referalDetails;
		} catch (SQLException ex) {
			logger.error("Exception in getUserReferals", ex);
			throw new InternalException("Server Error in getUserReferals");
		}
		
	}
	
	
	@RequestMapping(value = "/user/transaction/{userProfileId}/{pageNum}/{accType}", method = RequestMethod.GET,
			produces = "application/json") 
	public @ResponseBody TransactionsHolder getTransactions(@PathVariable("userProfileId") long userProfileId,
			@PathVariable("pageNum") int pageNum, @PathVariable("accType") int accType) throws InternalException, NotAllowedException {
		logger.info("getTransactions is called with user id {} : pageNo {}", userProfileId, pageNum);
		try {
			UserProfileHandler profileHandler = UserProfileHandler.getInstance();
			TransactionsHolder transactionsDetails = profileHandler.getTransactionsList(userProfileId, pageNum, accType); 
			logger.info("Transactions list size is {} for user profile id {}", transactionsDetails.getTransactionsList().size(), userProfileId);
			return transactionsDetails;
		} catch (SQLException ex) {
			logger.error("Exception in getTransactionsList", ex);
			throw new InternalException("Server Error in getTransactionsList");
		}
	}
}
