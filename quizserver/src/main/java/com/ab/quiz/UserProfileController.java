package com.ab.quiz;

import java.util.ArrayList;
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
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.WithdrawDBHandler;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.helper.WinMsgHandler;
import com.ab.quiz.pojo.LoginData;
import com.ab.quiz.pojo.ReferalDetails;
import com.ab.quiz.pojo.TransactionsHolder;
import com.ab.quiz.pojo.UserNetwork;
import com.ab.quiz.pojo.UserProfile;

@RestController
public class UserProfileController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(UserProfileController.class);
	
	
	@RequestMapping(value = "/loggedin/count", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody long getLoggedInUserCount() throws InternalException {
		logger.debug("{} getLoggedInUserCount", TAGS.LOGGED_IN_USER_COUNT);
		GetTask<Long> loggedInCountTask = Request.getLoggedInUsersCtTask();
		long count = -1;
		try {
			count = (long) loggedInCountTask.execute();
			logger.debug("{} getLoggedInUserCount value : {}", TAGS.LOGGED_IN_USER_COUNT, count);
			return count;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception while getting the logged in count from core server", TAGS.LOGGED_IN_USER_COUNT, ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		
		logger.debug("logout called with {} ", id);
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
		
		logger.info("{} forgotPassword is called with {}", TAGS.FORGOT_PASSWD, loginData.getMailAddress());
		UserProfile userProfile = new UserProfile();
		PostTask<LoginData, UserProfile> forgotPasswdTask = Request.forgotPasswordTask();
		forgotPasswdTask.setPostObject(loginData);
		
		try {
			userProfile = (UserProfile) forgotPasswdTask.execute();
			return userProfile;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in forgotPassword", TAGS.FORGOT_PASSWD);
			logger.error("Exception is: ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in forgotPassword");
		}
	}
	
	// Tested.
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody UserProfile updateUserProfile(@RequestBody UserProfile userProfile)
			throws NotAllowedException, InternalException {
		
		logger.info("{} updateUserProfile is called with {}", TAGS.UPDATE_USER, 
				userProfile.getEmailAddress().trim());
		UserProfile newUserProfile = new UserProfile();
		PostTask<UserProfile, UserProfile> updateProfileTask = Request.updateProfileTask();
		updateProfileTask.setPostObject(userProfile);
		
		try {
			newUserProfile = (UserProfile) updateProfileTask.execute();
			return newUserProfile;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in updateUserProfile", TAGS.UPDATE_USER);
			logger.error("Exception is: ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in updateUserProfile");
		}
	}
	
	@RequestMapping(value = "/user/mreferal/{myreferalcode}/{pageNum}", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody ReferalDetails getUserReferals(@PathVariable("myreferalcode") String referalCode,
			@PathVariable("pageNum") int pageNum) throws InternalException {
		
		logger.debug("getUserReferals is called with code {} : pageNo {}", referalCode, pageNum);
		GetTask<ReferalDetails> getReferalsTask = Request.userReferralTask(referalCode, pageNum);
		
		try {
			ReferalDetails referalDetails = (ReferalDetails) getReferalsTask.execute();
			logger.debug("Referals list size is {} for referal code {}", referalDetails.getReferalList().size(), referalCode);
			return referalDetails;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getUserReferals", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getUserReferals");
		}
		
	}
	
	
	@RequestMapping(value = "/user/transaction/{userProfileId}/{pageNum}/{accType}", method = RequestMethod.GET,
			produces = "application/json") 
	public @ResponseBody TransactionsHolder getTransactions(@PathVariable("userProfileId") long userProfileId,
			@PathVariable("pageNum") int pageNum, @PathVariable("accType") int accType) throws InternalException, NotAllowedException {
		
		logger.debug("getTransactions is called with user id {} : pageNo {}", userProfileId, pageNum);
		GetTask<TransactionsHolder> getTransactionsTask = Request.getTransactionsTask(userProfileId, pageNum, accType);
		
		try {
			TransactionsHolder transactionsDetails = (TransactionsHolder) getTransactionsTask.execute();
			logger.debug("Transactions list size is {} for user profile id {}", transactionsDetails.getTransactionsList().size(), userProfileId);
			return transactionsDetails;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getTransactionsList", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		if (diff <= QuizConstants.SERVER_CLIENT_TIME_DIFF_IN_SECS) {
			return "true";
		}
		return "false";
	}
	
	@RequestMapping(value = "/wd/messages/{userId}/{maxCount}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<String> getRecentWinWDMessages(@PathVariable long userId, @PathVariable int maxCount) 
			throws NotAllowedException, InternalException {
	
		logger.info("In getRecentWinWDMessages with userId {} and {}", userId, maxCount);
		try {
			List<String> combinedMsgs = WinMsgHandler.getInstance().getCombinedMessages();
			if (userId == -1) {
				return combinedMsgs;
			}
			
			GetTask<UserNetwork> closedFrdsTask = Request.getUserClosedNetworkTask(userId, maxCount);
			UserNetwork frdsDetails = (UserNetwork)closedFrdsTask.execute();
			
			List<Long> closedGroupMembersIds = frdsDetails.getClosedUserIdSet();
			List<String> closedGroupMembersNames = frdsDetails.getClosedUserNameList();
			logger.info("The closed friends for uid : {} ids size is {}", userId, closedGroupMembersIds.size());
			logger.info(closedGroupMembersIds);
			logger.info(closedGroupMembersNames);
			
			if (closedGroupMembersIds.size() > 0) {
				
				GetTask<String[]> userFrdsWinMsgTask = Request.getUserFrdsWinMsgsTask(userId, maxCount);
				String[] frdsWinMsgs = (String[])userFrdsWinMsgTask.execute();
				
				List<List<String>> totalUsersWinMsgs = new ArrayList<>();
				
				List<String> eachUserWinMsgs = new ArrayList<>();
				for (int index = 0; index < frdsWinMsgs.length; index ++) {
					if (frdsWinMsgs[index].length() > 1) {
						eachUserWinMsgs.add(frdsWinMsgs[index]);
					} else {
						totalUsersWinMsgs.add(eachUserWinMsgs);
						eachUserWinMsgs = new ArrayList<>();
					}
				}
				frdsWinMsgs = null;
				
				List<List<String>> totalUsersWithDrawMsgs = new ArrayList<>();
				
				for (int userIndex = 0; userIndex < closedGroupMembersIds.size(); userIndex++) {
					 
					long closedGrpUserId = closedGroupMembersIds.get(userIndex);
					String closedGrpUserName = closedGroupMembersNames.get(userIndex);
					
					List<String> withDrawMsgs = WithdrawDBHandler.
						getInstance().getRecentWinRecords(closedGrpUserId, true, closedGrpUserName);
					
					totalUsersWithDrawMsgs.add(withDrawMsgs);
				}
				
				List<String> closedGrpUsersMsgs = new ArrayList<>();
				
				int frdsSize = closedGroupMembersIds.size();
				int maxMsgIndex = 10;
				
				for (int msgIndex = 0; msgIndex < maxMsgIndex; msgIndex ++) {
					for (int index = 0; index < frdsSize; index ++) {
						if (index < totalUsersWinMsgs.size()) {
							List<String> winList = totalUsersWinMsgs.get(index);
							if (msgIndex < winList.size()) {
								closedGrpUsersMsgs.add(winList.get(msgIndex));
							}
						}
						if (index < totalUsersWithDrawMsgs.size()) {
							List<String> wdList = totalUsersWithDrawMsgs.get(index);
							if (msgIndex < wdList.size()) {
								closedGrpUsersMsgs.add(wdList.get(msgIndex));
							}
						}
					}
				}
				
				closedGrpUsersMsgs.addAll(closedGrpUsersMsgs);
				closedGrpUsersMsgs.addAll(closedGrpUsersMsgs);
				
				int totalClosedGrpMsgCount = 240 - closedGrpUsersMsgs.size();
				for (int totalIndex = 0; totalIndex < totalClosedGrpMsgCount; totalIndex ++) {
					if (totalIndex < combinedMsgs.size()) {
						closedGrpUsersMsgs.add(combinedMsgs.get(totalIndex));
					}
				}
				logger.info("closedGrpUsersMsgs size {}", closedGrpUsersMsgs.size());
				return closedGrpUsersMsgs;
			}
			logger.info("combinedMsgs {}", combinedMsgs.size());
			return combinedMsgs;
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getRecentWinWDMessages", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getRecentWinWDMessages");
		}
	}
}
