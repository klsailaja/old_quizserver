package com.ab.quiz.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.PictureType;
import com.ab.quiz.pojo.Picture;

/* 
CREATE TABLE QUESTIONPICS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
		PATH VARCHAR(100) NOT NULL, PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class QuestionPicsDBHandler {
	
	private static String OS_ROOT = "D:";
	
	private static String TABLE_NAME = "QUESTIONPICS";
	
	private static String ID = "ID";
	private static String PIC_PATH = "PATH";
	
	private static QuestionPicsDBHandler instance = null;
	
	private static final Logger logger = LogManager.getLogger(QuestionPicsDBHandler.class);
	
	private static final String PIC_HOME = OS_ROOT + File.separator + "QuizHome" + File.separator + "QuestionPics";
	
	private static final String MAX_WITHDRAW_REQ_ID = "SELECT MAX(ID) FROM " + TABLE_NAME;
	
	private static final String CREATE_PIC_ENTRY = "INSERT INTO " + TABLE_NAME  
			+ "(" + PIC_PATH + ") VALUES"
			+ "(?)";
	
	private static final String GET_PICTURE_BY_ID = "SELECT *" + " FROM " + 
			TABLE_NAME + " WHERE " + ID + " = ?";
	
	private QuestionPicsDBHandler() {
	}
	
	public static QuestionPicsDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new QuestionPicsDBHandler();
		}
		return instance;
	}
	
	public long getMaxWithdrawReqId() throws SQLException {
		
		logger.debug("In getMaxWithdrawReqId() method");
		
		long maxId = 0;
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet idRs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(MAX_WITHDRAW_REQ_ID);
			
			idRs = ps.executeQuery();
			if (idRs != null) {
				if (idRs.next()) {
					maxId = idRs.getLong("MAX(ID)");
				}
			}
		} catch (SQLException ex) {
			logger.error("SQL Exception in getMaxWithdrawReqId()", ex);
			throw ex;
		} finally {
			if (idRs != null) {
				idRs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		logger.debug("Returning from getMaxWithdrawReqId() {}", maxId);
		return maxId;
	}
	
	public long createPictureDBEntry(String fileNamePrefix, byte[] fileContents) throws SQLException {
		
		long maxId = getMaxWithdrawReqId();
		maxId++;
		
		String actualFileName = fileNamePrefix + "_" + maxId + ".jpg";
		String completeFielPath = PIC_HOME + File.separator + actualFileName;
		
		boolean fileCreationResult = false;
		try (FileOutputStream fos = new FileOutputStream(completeFielPath)) {
		      fos.write(fileContents);
		      fileCreationResult = true;
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException while creating file: " + e);
		} catch (IOException e) {
			logger.error("IOException while creating file: " + e);
		}
		
		if (!fileCreationResult) {
			return -1;
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		cp = ConnectionPool.getInstance();
		dbConn = cp.getDBConnection();
		ps = dbConn.prepareStatement(CREATE_PIC_ENTRY, Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, actualFileName);
		
		int affectedRowCount = ps.executeUpdate();
		long picEntryId = -1;
		
		if (affectedRowCount > 0) {
			rs = ps.getGeneratedKeys();
			
			if ((rs != null) && (rs.next())) {
				picEntryId = rs.getLong(1);
			}
		}
		if (rs != null) {
			rs.close();
		}
		
		if (ps != null) {
			ps.close();
		}
		if (dbConn != null) {
			dbConn.close();
		}
		return picEntryId;
	}
	
	public Picture getPicture(long id) throws SQLException {
		
		int pictureType = PictureType.MOVIE_QUESTION.getId();
		String fileName = null;
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(GET_PICTURE_BY_ID);
			ps.setLong(1, id);
			
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					fileName = rs.getString(PIC_PATH);
				}
			}
		} catch(SQLException ex) {
			logger.error("SQL Exception in getPicture() for {}", id, ex);
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		
		if (pictureType == -1) {
			return null;
		}
		
		Picture pictureObject = new Picture();
		pictureObject.setId(id);
		pictureObject.setPictureType(pictureType);
		pictureObject.setFilePath(fileName);
		
		return pictureObject;
	}
	
	public byte[] getPictureFileContents(long id) throws SQLException {
		
		Picture pictureObject = getPicture(id);
		if (pictureObject == null) {
			logger.info("********************* Movie pic id object not found {}", id);
			return null;
		}
		
		String completeFielPath = PIC_HOME + File.separator + pictureObject.getFilePath();
		
		try {
			BufferedImage bImage = ImageIO.read(new File(completeFielPath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos );
		    byte [] data = bos.toByteArray();
		    if (data != null) {
		    	//logger.info("The byte array length of the file is {} : {}", completeFielPath, data.length);
		    	return data;
		    }
		
		} catch (IOException e) {
			logger.error("Exception while reading the byte array for {}", completeFielPath);
			logger.error(e);
		}
		logger.info("********************* Movie pic id retunring null here for id {}", id);
	    return null;
	}
}
