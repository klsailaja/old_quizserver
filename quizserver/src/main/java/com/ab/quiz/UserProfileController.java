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

import com.ab.quiz.db.UserProfileDBHandler;
import com.ab.quiz.db.VerifyUserProfile;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.UserProfileHandler;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.helper.SendMailTask;
import com.ab.quiz.pojo.LoginData;
import com.ab.quiz.pojo.Mail;
import com.ab.quiz.pojo.OTPDetails;
import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserProfile;

@RestController
public class UserProfileController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(UserProfileController.class);
	
	// Tested.
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
	
	// Tested.
	@RequestMapping(value="/user/login", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile login(@RequestBody LoginData loginData) 
			throws NotAllowedException,InternalException {
		logger.info("login called with {} ", loginData.getMailAddress());
		try {
			UserProfile loginResult = UserProfileHandler.getInstance().login(loginData); 
			logger.info("login returned with {} : {} : {}", loginData.getMailAddress(), loginData.getPassword(), loginResult);
			return loginResult;
		} catch(SQLException ex) {
			logger.error("Exception in login", ex);
			throw new InternalException("Server Error in login");
		}
	}
	
	@RequestMapping(value="/user/logout/{id}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String logout(@PathVariable("id") long id) throws InternalException {
		logger.info("logout called with {} ", id);
		try {
			boolean result = UserProfileDBHandler.getInstance().updateLoggedInState(id, 0);
			return String.valueOf(result);
		} catch(SQLException ex) {
			logger.error("Exception in logout", ex);
			throw new InternalException("Server Error in logout");
		}
	}

	// Tested.
	@RequestMapping(value = "/user", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile createUserProfile(@RequestBody UserProfile userProfile) 
			throws NotAllowedException, InternalException {
		
		String userMailId = userProfile.getEmailAddress().trim();
		logger.info("createUserProfile is called with {}", userMailId);
		
		try {
			UserProfile dbUserProfile = UserProfileHandler.getInstance().createUserProfile(userProfile); 
			logger.info("createUserProfile returning with {} and {}", dbUserProfile.getEmailAddress(), dbUserProfile.getId());
			return dbUserProfile;
			
		} catch (SQLException ex) {
			logger.error("Exception in createUserProfile", ex);
			throw new InternalException("Server Error in createUserProfile");
		}
	}

	// Tested.
	@RequestMapping(value = "/forgot", method = RequestMethod.POST, produces = "application/json") 
	public @ResponseBody UserProfile forgotPassword(@RequestBody LoginData loginData) 
			throws NotAllowedException, InternalException {
		
		logger.info("forgotPassword is called with {}", loginData.getMailAddress());
		try {
			UserProfile userProfile = new UserProfile();
			userProfile.setEmailAddress(loginData.getMailAddress());
			
			boolean updateResult = UserProfileHandler.getInstance().updateUserProfileDetails(userProfile, true);
			
			if (!updateResult) {
				String errMsg = "Could not update profile contents for : " + userProfile.getEmailAddress().trim() + " in forgot passwd";
				throw new InternalException(errMsg);
			}
			
			UserProfile dbProfile = UserProfileHandler.getInstance().getUserProfileByMailId(userProfile.getEmailAddress());
			logger.info("updateUserProfile returning with {} and {}", dbProfile.getEmailAddress(), dbProfile.getId());
			return dbProfile;
		} catch (SQLException ex) {
			logger.error("Exception in forgotPassword", ex);
			throw new InternalException("Server Error in forgotPassword");
		}
	}
	
	// Tested.
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile updateUserProfile(@RequestBody UserProfile userProfile)
			throws NotAllowedException, InternalException {
		
		logger.info("updateUserProfile is called with {}", userProfile.getEmailAddress().trim());
		try {
			boolean updateResult = UserProfileHandler.getInstance().updateUserProfileDetails(userProfile, false);
			if (!updateResult) {
				throw new InternalException("Could not update profile contents for " + userProfile.getEmailAddress() + " during update");
			}
			
			UserProfile dbProfile = UserProfileHandler.getInstance().getUserProfileByMailId(userProfile.getEmailAddress());
			logger.info("updateUserProfile returning with {} and {}", dbProfile.getEmailAddress(), dbProfile.getId());
			return dbProfile;
		} catch (SQLException ex) {
			logger.error("Exception in updateUserProfile", ex);
			throw new InternalException("Server Error in updateUserProfile");
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
	
	@RequestMapping(value = "/user/verify", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String verifyOTP(@RequestBody OTPDetails otpDetails)
			throws NotAllowedException, InternalException {
		
		String eMail = otpDetails.getMailId().trim();
		String passwdHash = otpDetails.getOtp_hash().trim();
		
		try {
			OTPDetails dbEntry = VerifyUserProfile.getInstance().getOTPDetailsByMailId(eMail);
			if (dbEntry.getMailId() != null) {
				if (dbEntry.getMailId().equals(eMail)) {
					if (dbEntry.getOtp_hash().equals(passwdHash)) {
						return String.valueOf(true);
					}
				}
			}
			return String.valueOf(false);
		} catch (SQLException ex) {
			logger.error("Error while OTP Verify process", ex);
			throw new InternalException("Server Error while OTP Verify process");
		}
		
	}
	
	@RequestMapping(value = "/user/sendcode", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String verifyUserIdGenerateOTP(@RequestBody String eMail) throws NotAllowedException,
		InternalException {
		
		eMail = eMail.trim();
		eMail = eMail.replace("\"", "");
		logger.info("This is in verifyUserIdGenerateOTP {}", eMail);
		try {
			UserProfile userProfile = UserProfileHandler.getInstance().getUserProfileByMailId(eMail);
			if (userProfile.getId() > 0) {
				throw new NotAllowedException(eMail + " already registered. Use forgot password if required");
			}
			
			 
			OTPDetails otpDBEntry = VerifyUserProfile.getInstance().getOTPDetailsByMailId(eMail);
			boolean exists = ((otpDBEntry.getMailId() != null) && otpDBEntry.getMailId().equals(eMail)); 
			
			String passwd = UserProfileDBHandler.getInstance().getRandomPasswd(4);
			String passwdHash = UserProfileDBHandler.getPasswordHash(passwd);
			
			OTPDetails otpDetails = new OTPDetails();
			otpDetails.setMailId(eMail);
			otpDetails.setOtp_hash(passwdHash);
			
			boolean dbAddOrUpdate = false;
			if (!exists) {
				dbAddOrUpdate = VerifyUserProfile.getInstance().createUserProfileForVerify(otpDetails);
			} else {
				int updateRowCount = VerifyUserProfile.getInstance().updateRecordWithOTP(eMail, passwdHash);
				dbAddOrUpdate = (updateRowCount > 0);
			}
			if (dbAddOrUpdate) {
				
				Mail mail = new Mail();
		        
				mail.setMailFrom("ggraj.pec@gmail.com");
				mail.setMailTo(eMail);
				mail.setMailSubject("4-digit Verification Code");
	        
				mail.setMailContent("Your 4-digit verification code :" + passwd 
	        		+ "\n\nThanks\nTeluguQuiz");
				
				LazyScheduler.getInstance().submit(new SendMailTask(mail));
			}
			
			return String.valueOf(true);
		} catch(SQLException ex) {
			logger.error("Error in verifyUserIdGenerateOTP for {}", eMail);
			logger.error(ex);
			throw new InternalException("Server Error in verifyUserIdGenerateOTP");
		}
	}
}
