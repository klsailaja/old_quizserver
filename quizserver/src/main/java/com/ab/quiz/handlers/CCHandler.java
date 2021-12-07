package com.ab.quiz.handlers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.CustomerCareReqState;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.ReceiptType;
import com.ab.quiz.db.CustomerCareDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.CCTicketsHolder;
import com.ab.quiz.pojo.CustomerTicket;
import com.ab.quiz.pojo.Document;

public class CCHandler {
	
	private static final Logger logger = LogManager.getLogger(CCHandler.class);
	
	private static CCHandler instance = null;
	
	private CCHandler() {
	}
	
	public static CCHandler getInstance() {
		if (instance == null) {
			logger.debug("In CCHandler getInstance() method instance created");
			instance = new CCHandler();
		}
		return instance;
	}
	
	public CCTicketsHolder getWithdrawDataSet(long userProfileId, int startRowNumber, int state) 
			throws SQLException, NotAllowedException {
		
		CustomerCareDBHandler ccDBHandler = CustomerCareDBHandler.getInstance();
		return ccDBHandler.getCCTickets(userProfileId, startRowNumber, state);
	}
	
	public boolean placeCustomerCareTicket(CustomerTicket ccTicket, Document document)
			throws NotAllowedException, SQLException {
		
		CustomerCareDBHandler ccDBHandler = CustomerCareDBHandler.getInstance();
		return ccDBHandler.createCCTicket(ccTicket, document);
	}
	
	public boolean cancelCustomerCareTicket(long userProfileId, String withdrawRefId) 
			throws SQLException, NotAllowedException, FileNotFoundException, Exception {
		CustomerCareDBHandler ccDBHandler = CustomerCareDBHandler.getInstance();
		return ccDBHandler.updateCCRequestState(withdrawRefId, CustomerCareReqState.CANCELLED, "Ticket Cancelled", null);
	}
	
	public boolean closeCustomerCareTicket(long userProfileId, String withdrawRefId, String comments, String remedyPicFilePath) 
			throws SQLException, NotAllowedException, FileNotFoundException, Exception {
		
		CustomerCareDBHandler ccDBHandler = CustomerCareDBHandler.getInstance();
		return ccDBHandler.updateCCRequestState(withdrawRefId, CustomerCareReqState.CLOSED, comments, remedyPicFilePath);
	}
	
	public boolean rejectCustomerCareTicket(long userProfileId, String withdrawRefId, String comments, String remedyPicFilePath) 
			throws SQLException, NotAllowedException, FileNotFoundException, Exception {
		
		CustomerCareDBHandler ccDBHandler = CustomerCareDBHandler.getInstance();
		return ccDBHandler.updateCCRequestState(withdrawRefId, CustomerCareReqState.NOT_AN_ISSUE, comments, remedyPicFilePath);
	}
	
	public static void main(String[] args) throws Exception {
		CCHandler ccHandler = CCHandler.getInstance();
		
		CustomerTicket cTicket = new CustomerTicket();
		cTicket.setRequestType(CustomerCareReqType.ADDED_MONEY_NOT_UPDATED.getId());
		cTicket.setUserId(23);
		cTicket.setOpenedTime(System.currentTimeMillis());
		cTicket.setStatus(CustomerCareReqState.OPEN.getId());
		cTicket.setExtraDetails("ExtraDetails");
		
		Document document = new Document();
		document.setDocType(ReceiptType.TICKET.getId());
		
		String completeFielPath = "D:" + File.separator + "Projects" + File.separator + "Receipt.png";
		
		BufferedImage bImage = ImageIO.read(new File(completeFielPath));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bImage, "jpg", bos );
	    byte [] data = bos.toByteArray();

		document.setDocContents(data);
		
		//ccHandler.placeCustomerCareTicket(cTicket, document);
		ccHandler.closeCustomerCareTicket(23, "EpC0kkYNS1", "Closed Comments", completeFielPath);
	}
}
