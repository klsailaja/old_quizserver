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

import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.KYCHandler;
import com.ab.quiz.pojo.KYCEntry;

@RestController
public class KYCController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(KYCController.class);

	@RequestMapping (value = "/kyc", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Long createKYCEntry(@RequestBody KYCEntry kycEntry) 
			throws NotAllowedException, InternalException {
		logger.info("This is in createKYCEntry {}", kycEntry);
		try {
			KYCHandler kycHandler = KYCHandler.getInstance(); 
			long ccTkt = kycHandler.placeKYCEntry(kycEntry);
			logger.info("createKYCEntry result is {}", ccTkt);
			return ccTkt;
		} catch (SQLException ex) {
			logger.error("Exception in createKYCEntry", ex);
			throw new InternalException("Server Error in createKYCEntry");
		}
	}
	
	@RequestMapping(value = "/kycimg", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public @ResponseBody Boolean createKYCImg(@RequestParam("user-file") MultipartFile file) 
			throws NotAllowedException, InternalException {
		try {
			KYCHandler kycHandler = KYCHandler.getInstance(); 
			boolean imgCreateResult = kycHandler.createKYCImg(file);
			logger.info("createKYCImg result is {}", imgCreateResult);
			return imgCreateResult;
		} catch (SQLException ex) {
			logger.error("Exception in createKYCImg", ex);
			throw new InternalException("Server Error in createKYCImg");
		}
	}
	
	@RequestMapping(value = "/kyc/{uid}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody KYCEntry getKYCEntry(@PathVariable("uid") long userProfileId) 
			throws InternalException, NotAllowedException {
		
		try {
			KYCHandler kycHandler = KYCHandler.getInstance();
			return kycHandler.getKYCEntry(userProfileId);
		} catch (Exception ex) {
			logger.error("Exception in getKYCEntry", ex);
			throw new InternalException("Server Error in getKYCEntry");
		}
	}
}
