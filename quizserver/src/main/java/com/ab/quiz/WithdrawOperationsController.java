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

import com.ab.quiz.db.PictureDBHandler;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.WDHandler;
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
			WDHandler wdHandler = WDHandler.getInstance();
			WithdrawRequestsHolder wdReqsHolder = wdHandler.getWithdrawDataSet(userProfileId, pageNum, status); 
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
			WDHandler wdHandler = WDHandler.getInstance();
			boolean createResult = wdHandler.placeWithdrawMoneyRequest(wdInput);
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
			boolean result = WDHandler.getInstance().cancelWithdrawRequest(userProfileId, wdrefid);
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
		
		logger.info("wd getReceiptContents is called with id {} ", id);
		try {
			byte[] receiptBytes = PictureDBHandler.getInstance().getPictureFileContents(id);
			logger.info("wd getReceiptContents request result is {}", receiptBytes.length);
			return receiptBytes;
		} catch (SQLException ex) {
			logger.error("Exception in getReceiptContents", ex);
			throw new InternalException("Server Error in getReceiptContents");
		}
	}
}
