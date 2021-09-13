package com.ab.quiz;

import java.sql.SQLException;
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

import com.ab.quiz.db.MyTransactionDBHandler;
import com.ab.quiz.db.WithdrawDBHandler;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.UserMoneyHandler;
import com.ab.quiz.helper.Utils;
import com.ab.quiz.helper.WinMsgHandler;
import com.ab.quiz.pojo.WithdrawRequestInput;
import com.ab.quiz.pojo.WithdrawRequestsHolder;

@RestController
public class WithdrawOperationsController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(WithdrawOperationsController.class);
	
	@RequestMapping(value = "/wd/{userProfileId}/{pageNum}/{status}", method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody WithdrawRequestsHolder getWithdrawReqs(@PathVariable("userProfileId") long userProfileId,
			@PathVariable("pageNum") int pageNum, @PathVariable("status") int status) throws InternalException, NotAllowedException {
		logger.info("getWithdrawReqs is called with user id {} : pageNo {}", userProfileId, pageNum);
		try {
			UserMoneyHandler userMoneyHandler = UserMoneyHandler.getInstance();
			WithdrawRequestsHolder wdReqsHolder = userMoneyHandler.getWithdrawDataSet(userProfileId, pageNum, status); 
			logger.info("Withdraw Requests list size is {} for user profile id {}", wdReqsHolder.getList().size(), userProfileId);
			return wdReqsHolder;
		} catch (SQLException ex) {
			logger.error("Exception in getWithdrawReqs", ex);
			throw new InternalException("Server Error in getWithdrawReqs");
		}
	}
	
	@RequestMapping(value = "/wd", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Boolean createWithdrawReq(@RequestBody WithdrawRequestInput wdInput) 
			throws NotAllowedException, InternalException {
		
		logger.info("createWithdraw is called with {}", wdInput.getWithdrawUserInput().getUserProfileId());
		try {
			boolean createResult = UserMoneyHandler.getInstance().placeWithdrawMoneyRequest(wdInput);
			logger.info("createWithdraw request result is {}", createResult);
			return new Boolean(createResult);
		} catch (SQLException ex) {
			logger.error("Exception in createWithdraw", ex);
			throw new InternalException("Server Error in createWithdraw");
		}
	}
	
	@RequestMapping(value = "/wd/cancel/{profileId}/{wdrefid}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Boolean cancelWithdrawReq(@PathVariable("profileId") long userProfileId, 
			@PathVariable("wdrefid") String wdrefid) throws NotAllowedException, InternalException {
		
		logger.info("cancelWithdrawReq is called with userProfileId {} and wdRefId {}", userProfileId, wdrefid);
		try {
			boolean result = UserMoneyHandler.getInstance().cancelWithdrawRequest(userProfileId, wdrefid);
			logger.info("cancelWithdrawReq request result is {}", result);
			return new Boolean(result);
		} catch (SQLException ex) {
			logger.error("Exception in cancelWithdrawReq", ex);
			throw new InternalException("Server Error in cancelWithdrawReq");
		}
	}
	
	@RequestMapping(value = "/wd/receipt/{id}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody byte[] getReceiptContents(@PathVariable("id") long id) 
			throws NotAllowedException, InternalException {
		
		logger.info("getReceiptContents is called with id {} ", id);
		try {
			byte[] receiptBytes = UserMoneyHandler.getInstance().getReceiptContents(id);
			logger.info("getReceiptContents request result is {}", receiptBytes.length);
			return receiptBytes;
		} catch (SQLException ex) {
			logger.error("Exception in getReceiptContents", ex);
			throw new InternalException("Server Error in getReceiptContents");
		}
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
			
			List<Long> closedGroupMembersIds = new ArrayList<>();
			List<String> closedGroupMembersNames = new ArrayList<>();
			
			Utils.getClosedCircleUserIds(userId, maxCount, closedGroupMembersIds, closedGroupMembersNames);
			
			logger.info(closedGroupMembersIds);
			logger.info(closedGroupMembersNames);
			
			if (closedGroupMembersIds.size() > 0) {
				
				List<List<String>> totalUsersWinMsgs = new ArrayList<>();
				List<List<String>> totalUsersWithDrawMsgs = new ArrayList<>();
				
				int winMsgsMaxSize = 0;
				int wdMsgsMaxSize = 0;
				
				for (int userIndex = 0; userIndex < closedGroupMembersIds.size(); userIndex++) {
					 
					long closedGrpUserId = closedGroupMembersIds.get(userIndex);
					String closedGrpUserName = closedGroupMembersNames.get(userIndex);
					
					List<String> gameWinMsgs = MyTransactionDBHandler.
						getInstance().getRecentWinRecords(closedGrpUserId, true, closedGrpUserName);
					List<String> withDrawMsgs = WithdrawDBHandler.
						getInstance().getRecentWinRecords(closedGrpUserId, true, closedGrpUserName);
					
					totalUsersWinMsgs.add(gameWinMsgs);
					totalUsersWithDrawMsgs.add(withDrawMsgs);
					
					if (winMsgsMaxSize < gameWinMsgs.size()) {
						winMsgsMaxSize = gameWinMsgs.size();
					}
					
					if (wdMsgsMaxSize < withDrawMsgs.size()) {
						wdMsgsMaxSize = withDrawMsgs.size();
					}
				}
				
				List<String> closedGrpUsersMsgs = new ArrayList<>();
				
				for (int winMsgIndex = 0; winMsgIndex < winMsgsMaxSize; winMsgIndex ++) {
					for (int totalIndex = 0; totalIndex < totalUsersWinMsgs.size(); totalIndex ++) {
						List<String> gameWinMsgs = totalUsersWinMsgs.get(totalIndex);
						if (winMsgIndex < gameWinMsgs.size()) {
							closedGrpUsersMsgs.add(gameWinMsgs.get(winMsgIndex));
						}
					}
				}
				
				for (int wdMsgIndex = 0; wdMsgIndex < wdMsgsMaxSize; wdMsgIndex ++) {
					for (int totalIndex = 0; totalIndex < totalUsersWithDrawMsgs.size(); totalIndex ++) {
						List<String> gameWdMsgs = totalUsersWithDrawMsgs.get(totalIndex);
						if (wdMsgIndex < gameWdMsgs.size()) {
							closedGrpUsersMsgs.add(gameWdMsgs.get(wdMsgIndex));
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
		} catch (SQLException ex) {
			logger.error("Exception in getRecentWinWDMessages", ex);
			throw new InternalException("Server Error in getRecentWinWDMessages");
		}
	}
}
