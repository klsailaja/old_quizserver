package com.ab.quiz.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.PictureType;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.Picture;

/* 
 CREATE TABLE PICTURES(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
		TYPE INT NOT NULL,
		PATH VARCHAR(100) NOT NULL, PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class PictureDBHandler {
	
	private static String OS_ROOT = "D:";
	
	private static String TABLE_NAME = "PICTURES";
	
	private static String ID = "ID";
	private static String PIC_TYPE = "TYPE";
	private static String PIC_PATH = "PATH";
	
	private static PictureDBHandler instance = null;
	
	private static final Logger logger = LogManager.getLogger(PictureDBHandler.class);
	
	private static final String PIC_HOME = OS_ROOT + File.separator + "QuizHome" + File.separator + "CustomerCare";
	
	
	private static final String CREATE_PIC_ENTRY = "INSERT INTO " + TABLE_NAME  
			+ "(" + PIC_TYPE + "," + PIC_PATH + ") VALUES"
			+ "(?,?)";
	
	private static final String GET_PICTURE_BY_ID = "SELECT *" + " FROM " + 
			TABLE_NAME + " WHERE " + ID + " = ?";
	
	private static final String UPDATE_PIC_ENTRY = "UPDATE " + TABLE_NAME + " SET " + PIC_TYPE + " = ?," + PIC_PATH + " = ? WHERE "
			+ ID + " = ?";
	
	private static final String REMOVE_PIC_ENTRY = "DELETE FROM " + TABLE_NAME 
			+ " WHERE " + ID + " = ? ";
	
	private PictureDBHandler() {
	}
	
	public static PictureDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new PictureDBHandler();
		}
		return instance;
	}
	
	// Create the file in the disk first
	// If success, insert into DB
	public long createPictureDBEntry(String recordKey, int pictureType, 
			byte[] fileContents, boolean markActive, long delPicId) throws SQLException {
		
		String[] fileNames = getPictureFileName(pictureType);
		String actualFileName = fileNames[1] + "_" + recordKey + ".jpg";
		String completeFielPath = PIC_HOME + File.separator + fileNames[0] + File.separator + actualFileName;
		
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
		
		if (markActive) {
			
			completeFielPath = PIC_HOME + File.separator + "CURRENT_TICKETS" + File.separator + actualFileName;
			try (FileOutputStream fos = new FileOutputStream(completeFielPath)) {
			      fos.write(fileContents);
			      fileCreationResult = true;
			} catch (FileNotFoundException e) {
				logger.error("FileNotFoundException while creating file: " + e);
			} catch (IOException e) {
				logger.error("IOException while creating file: " + e);
			}
		} else if (delPicId > -1) {
			completeFielPath = PIC_HOME + File.separator + "CURRENT_TICKETS" + File.separator + "ticket_opened" + "_" + recordKey + ".jpg";
			
			Path path = Paths.get(completeFielPath);
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				logger.error("IOException while deleting file: " + e);
	        }
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		cp = ConnectionPool.getInstance();
		dbConn = cp.getDBConnection();
		ps = dbConn.prepareStatement(CREATE_PIC_ENTRY, Statement.RETURN_GENERATED_KEYS);
		ps.setInt(1, pictureType);
		ps.setString(2, actualFileName);
		
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
		
		int pictureType = -1;
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
					pictureType = rs.getInt(PIC_TYPE);
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
	
	
	// Read
	public byte[] getPictureFileContents(long id) throws SQLException {
		
		Picture pictureObject = getPicture(id);
		if (pictureObject == null) {
			return null;
		}
		
		String[] fileNames = getPictureFileName(pictureObject.getPictureType());
		String completeFielPath = PIC_HOME + File.separator + fileNames[0] + File.separator + pictureObject.getFilePath();
		
		try {
			BufferedImage bImage = ImageIO.read(new File(completeFielPath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos );
		    byte [] data = bos.toByteArray();
		    if (data != null) {
		    	logger.info("The byte array length of the file is {} : {}", completeFielPath, data.length);
		    	return data;
		    }
		
		} catch (IOException e) {
			logger.error("Exception while reading the byte array for {}", completeFielPath);
			logger.error(e);
		}
	    return null;
	}
	
	// Update
	public boolean updatePictureEntry(long oldId, Picture newPicture) throws NotAllowedException, SQLException {
		
		Picture oldPicture = getPicture(oldId);
		
		if (oldPicture == null) {
			throw new NotAllowedException("No picture object found in DB for " + oldId);
		}
		
		String[] fileNames = getPictureFileName(oldPicture.getPictureType());
		String completeFielPath = PIC_HOME + File.pathSeparator + fileNames[0] + File.pathSeparator + fileNames[1];
		
		Path path = Paths.get(completeFielPath);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error("IOException while deleting file: " + e);
        }
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(UPDATE_PIC_ENTRY);
		
		ps.setInt(1, newPicture.getPictureType());
		ps.setString(2, newPicture.getFilePath());
		ps.setLong(3, oldId);
		
		int operResult = 0;
		
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while updatePictureEntry for {}", oldId);
			logger.error("SQLException in ", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return (operResult > 0);
	}
	
	// Delete
	public boolean deletePictureEntry(long picId) throws SQLException {
		Picture oldPicture = getPicture(picId);
		
		if (oldPicture == null) {
			throw new NotAllowedException("No picture object found in DB for " + picId);
		}
		
		String[] fileNames = getPictureFileName(oldPicture.getPictureType());
		String completeFielPath = PIC_HOME + File.separator + fileNames[0] + File.separator + oldPicture.getFilePath();;
		
		Path path = Paths.get(completeFielPath);
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error("IOException while deleting file: " + e);
        }
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(REMOVE_PIC_ENTRY);
		
		ps.setLong(1, picId);
		
		int operResult = 0;
		
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Exception while deletePictureEntry for {}", picId);
			logger.error("SQLException in ", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return (operResult > 0);
	}
	
	private String[] getPictureFileName(int pictureType) {
		PictureType picType = PictureType.findById(pictureType);
		String dirName = "CUSTOMER_TICKETS";
		String fileName = "ticket_opened";
		switch (picType) {
			case TICKET_OPENED: {
				dirName = "CUSTOMER_TICKETS";
				fileName = "ticket_opened";
				break;
			}
			case TICKET_CLOSED: {
				dirName = "CUSTOMER_TICKETS";
				fileName = "ticket_closed";
				break;
			}
			
			case WDRECEIPT: {
				dirName = "WD_RECEIPTS";
				fileName = "wd_receipt";
				break;
			}
			case AADHAR_FRONT: {
				dirName = "KYC_DOCS";
				fileName = "aadhar_front";
				break;
			}
			case AADHAR_BACK: {
				dirName = "KYC_DOCS";
				fileName = "aadhar_back";
				break;
			}
			case PAN: {
				dirName = "KYC_DOCS";
				fileName = "pan";
				break;
			}
			default: {
				dirName = "KYC_DOCS";
				fileName = "pan";
				break;
			}
		}
		String[] fileProps = new String[2];
		fileProps[0] = dirName;
		fileProps[1] = fileName;
		return fileProps;
	}
}
