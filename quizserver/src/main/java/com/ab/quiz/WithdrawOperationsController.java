package com.ab.quiz;

import java.sql.SQLException;
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
	
	@RequestMapping(value = "/wd/messages/{userId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<String> getRecentWinWDMessages(@PathVariable long userId) 
			throws NotAllowedException, InternalException {
		logger.info("In getRecentWinWDMessages with userId {}", userId);
		try {
			List<String> combinedMsgs = WinMsgHandler.getInstance().getCombinedMessages();
			if (userId != -1) {
				List<String> gameWinMsgs = MyTransactionDBHandler.getInstance().getRecentWinRecords(userId);
				List<String> withDrawMsgs = WithdrawDBHandler.getInstance().getRecentWinRecords(userId);
				for (int index = withDrawMsgs.size() - 1; index >= 0; index--) {
					combinedMsgs.add(0, withDrawMsgs.get(index));
				}
				for (int index = gameWinMsgs.size() - 1; index >= 0; index--) {
					combinedMsgs.add(0, gameWinMsgs.get(index));
				}
			}
			logger.info("combinedMsgs {}", combinedMsgs.size());
			return combinedMsgs;
		} catch (SQLException ex) {
			logger.error("Exception in getRecentWinWDMessages", ex);
			throw new InternalException("Server Error in getRecentWinWDMessages");
		}
	}
}
