package com.ab.quiz.handlers;

import java.sql.SQLException;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.ab.quiz.constants.PictureType;
import com.ab.quiz.db.KYCDBHandler;
import com.ab.quiz.db.PictureDBHandler;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.Document;
import com.ab.quiz.pojo.KYCEntry;

public class KYCHandler {
	
	private static final Logger logger = LogManager.getLogger(KYCHandler.class);
	
	private static KYCHandler instance = null;
	
	private KYCHandler() {
	}
	
	public static KYCHandler getInstance() {
		if (instance == null) {
			logger.debug("In KYCHandler getInstance() method instance created");
			instance = new KYCHandler();
		}
		return instance;
	}
	
	public long placeKYCEntry(KYCEntry kycEntry)
			throws NotAllowedException, SQLException {
		KYCEntry dbEntry = KYCDBHandler.getInstance().getKYCEntryById(kycEntry.getUserId());
		if (dbEntry.getUserId() == 0) {
			kycEntry.setUserId(kycEntry.getUserId());
			kycEntry.setStatus("Not Submitted");
			KYCDBHandler.getInstance().createKYCEntry(kycEntry);
			return kycEntry.getUserId();
		}
		KYCDBHandler.getInstance().deleteAllTraces(kycEntry.getAfpId(), kycEntry.getAbpId(), kycEntry.getPpId());
		return kycEntry.getUserId();
	}

	public boolean createKYCImg(MultipartFile file) throws NotAllowedException, SQLException {
		String fileName = file.getOriginalFilename();
        logger.info("File name: " + fileName);
        StringTokenizer strTokenizer = new StringTokenizer(fileName, "_"); 
        Document problemPic = null;
        
        KYCEntry dbKycEntry = null;
        String userIdStr = null;
        long userId = -1;
        String picTypeStr = null;
        int picTypeInt = -1;
        try {
	        if (fileName != null) { 
        		userIdStr = strTokenizer.nextToken().trim();
        		userId = Long.parseLong(userIdStr);
        		dbKycEntry = KYCDBHandler.getInstance().getKYCEntryById(userId);
        		if (dbKycEntry.getUserId() == 0) {
        			throw new NotAllowedException("KYC Object not found for user id" + userId);
        		}
        		
        		strTokenizer.nextToken().trim();
        		picTypeStr = strTokenizer.nextToken().trim();
        		picTypeInt = Integer.parseInt(picTypeStr);
	        	if (picTypeInt == -1) {
	        		throw new NotAllowedException("Illegal Picture Type " + picTypeInt);
	        	}
	        }
        } catch(SQLException ex) {
        	logger.error("SQLException while getting the KYC Object with id {}", userIdStr);
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
					problemPic.setDocType(getPictureTypeEnumVal(picTypeInt).getId());
					
				} catch (Exception e) {
					logger.error("error processing uploaded file", e);
					isImgCreationError = true;
				}
			}
		}
        
    	long kycDocId = -1;
		if (problemPic.getDocContents() != null) {
			try {
				kycDocId = PictureDBHandler.getInstance().createPictureDBEntry(userIdStr, 
						getPictureTypeEnumVal(picTypeInt).getId(), problemPic.getDocContents(), false, -1);
				logger.info("kycDocId is ***********" + kycDocId);
				if (kycDocId == -1) {
					isImgCreationError = true;
				}
			} catch (Exception e) {
				isImgCreationError = true;
				logger.error("SQLException while creating the kyc picture for customer id : {}", userIdStr);
			}
			if (isImgCreationError) {
				logger.info("kycDocId is ***********" + isImgCreationError);
				return false;
			}
			if ((userId > 0) && (picTypeInt > 0)) {
				return KYCDBHandler.getInstance().updateKYCEntry(userId, kycDocId, picTypeInt);
			}
		}
		return false;
	}
	
	public KYCEntry getKYCEntry(long uid) throws SQLException {
		return KYCDBHandler.getInstance().getKYCEntryById(uid);
	}
	
	private PictureType getPictureTypeEnumVal(int picType) {
		if (picType == 1) {
			return PictureType.AADHAR_FRONT;
		}
		if (picType == 2) {
			return PictureType.AADHAR_BACK;
		}
		return PictureType.PAN;
	}
}
