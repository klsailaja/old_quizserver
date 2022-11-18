package com.ab.quiz;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.PictureType;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.CustomerCareDBHandler;
import com.ab.quiz.db.PictureDBHandler;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.CCHandler;
import com.ab.quiz.pojo.CCTicketsHolder;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.Document;

@RestController
public class CustomerCareController extends BaseController {
	private static final Logger logger = LogManager.getLogger(CustomerCareController.class);
	
	@RequestMapping(value = "/cc/{userProfileId}/{pageNum}/{status}", method = RequestMethod.GET,
			produces = "application/json")
	public @ResponseBody CCTicketsHolder getCCReqs(@PathVariable("userProfileId") long userProfileId,
			@PathVariable("pageNum") int pageNum, @PathVariable("status") int status) throws InternalException, NotAllowedException {
		logger.info("{} getCCReqs is called with user id {} : pageNo {}", TAGS.FETCH_CUSTOMER_TKTS, 
				userProfileId, pageNum);
		try {
			CCHandler handler = CCHandler.getInstance();
			CCTicketsHolder ccReqsHolder = handler.getWithdrawDataSet(userProfileId, pageNum, status); 
			logger.info("{} CC Requests list size is {} for user profile id {}", TAGS.FETCH_CUSTOMER_TKTS,
					ccReqsHolder.getList().size(), userProfileId);
			return ccReqsHolder;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getCCReqs", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getCCReqs");
		}
	}
	
	@RequestMapping (value = "/ccticket", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Long createCustomerTicket(@RequestBody CustomerTicket customerTicket) 
			throws NotAllowedException, InternalException {
		try {
			CCHandler handler = CCHandler.getInstance();
			long ccTkt = handler.placeCustomerCareTicket(customerTicket);
			logger.info("{} createCustomerTicket request result is {}", TAGS.CREATE_CUSTOMER_TKT, ccTkt);
			return ccTkt;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in createCustomerTicket", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in createCustomerTicket");
		}
	}
	
	@RequestMapping(value = "/ccimg", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public @ResponseBody Boolean createCCImg(@RequestParam("user-file") MultipartFile file) 
			throws NotAllowedException, InternalException {
		logger.info("{} Image Upload in Create Customer Ticket", TAGS.CREATE_CUSTOMER_TKT);
		String fileName = file.getOriginalFilename();
        //logger.info("File name: " + fileName);
        Document problemPic = null;
        
        CustomerTicket dbCCTkts = null;
        int pos = -1;
        String ccTktIdStr = null;
        long ccTktId = -1;
        try {
	        if (fileName != null) { 
	        	pos = fileName.indexOf("_");
	        	if (pos > -1) {
	        		ccTktIdStr = fileName.substring(pos + 1);
	        		ccTktId = Long.parseLong(ccTktIdStr);
	        		dbCCTkts = CCHandler.getInstance().getCustomerCareTicket(ccTktId);
	        		if (dbCCTkts == null) {
	        			ccTktIdStr = null;
	        			ccTktId = -1;
	        			return false;
	        		}
	        	}
	        }
        } catch(SQLException ex) {
        	logger.error(QuizConstants.ERROR_PREFIX_START);
        	logger.error("SQLException while getting the Customer Ticket with id {}", ccTktId);
        	logger.error(QuizConstants.ERROR_PREFIX_END);
        	return false;
        }

        boolean isImgCreationError = false;
        if (file != null) {
			if (!file.isEmpty()) {
				byte[] bytes = null;
				try {
					bytes = file.getBytes();
					problemPic = new Document();
					problemPic.setDocContents(bytes);
					problemPic.setDocType(PictureType.TICKET_OPENED.getId());
					
				} catch (Exception e) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.error("error processing uploaded file", e);
					logger.error(QuizConstants.ERROR_PREFIX_END);
					isImgCreationError = true;
				}
			}
		}
        
    	long problemPicId = -1;
		if (problemPic.getDocContents() != null) {
			try {
				problemPicId = PictureDBHandler.getInstance().createPictureDBEntry(ccTktIdStr, 
						PictureType.TICKET_OPENED.getId(), problemPic.getDocContents(), true, -1);
				if (problemPicId == -1) {
					isImgCreationError = true;
				}
			} catch (Exception e) {
				isImgCreationError = true;
				logger.error(QuizConstants.ERROR_PREFIX_START);
				logger.error("Exception while creating the picture for customer id : {}", ccTktIdStr);
				logger.error(QuizConstants.ERROR_PREFIX_END);
			}
		}
		if (isImgCreationError) {
			if (ccTktId > -1) {
				try {
					CustomerCareDBHandler.getInstance().removeTicketById(ccTktId);
				} catch (Exception e) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.error("Exception while deleting the customer ticket with id : {}", ccTktIdStr);
					logger.error(QuizConstants.ERROR_PREFIX_END);
				}
			}
			return false;
		} else {
			if ((ccTktId > -1) && (problemPicId > -1)) {
				try {
					CustomerCareDBHandler.getInstance().updateTicketById(ccTktId, problemPicId);
					return true;
				} catch (Exception e) {
					logger.error(QuizConstants.ERROR_PREFIX_START);
					logger.error("Exception while updateTicketById with id : {} and {}", ccTktIdStr, problemPicId);
					logger.error(QuizConstants.ERROR_PREFIX_END);
				}
			}
			
		}
		return false;
	}
	
	
	@RequestMapping(value = "/cc/cancel/{profileId}/{ccrefid}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Boolean cancelCCReq(@PathVariable("profileId") long userProfileId, 
			@PathVariable("ccrefid") String ccrefid) throws NotAllowedException, InternalException {
		
		logger.info("cancelCCReq is called with userProfileId {} and ccRefId {}", userProfileId, ccrefid);
		try {
			boolean result = CCHandler.getInstance().cancelCustomerCareTicket(userProfileId, ccrefid);
			//logger.info("cancelCCReq request result is {}", result);
			return new Boolean(result);
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in cancelCCReq", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in cancelCCReq");
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in cancelCCReq", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in cancelCCReq");
		}
	}
	
	@RequestMapping(value = "/cc/receipt/{id}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody byte[] getReceiptContents(@PathVariable("id") long id) 
			throws NotAllowedException, InternalException {
		
		logger.info("{} getReceiptContents is called with id {} ", TAGS.CUSTOMER_TKT_RECEIPT, id);
		try {
			byte[] receiptBytes = PictureDBHandler.getInstance().getPictureFileContents(id);
			//logger.info("CC getReceiptContents request result is {}", receiptBytes.length);
			return receiptBytes;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception in getReceiptContents", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw new InternalException("Server Error in getReceiptContents");
		}
	}
}
