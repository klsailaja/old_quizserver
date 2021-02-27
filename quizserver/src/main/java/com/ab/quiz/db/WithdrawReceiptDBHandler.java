package com.ab.quiz.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
CREATE TABLE WithdrawReceipt(id bigint NOT NULL AUTO_INCREMENT,
 		receipt MEDIUMBLOB NULL DEFAULT NULL, PRIMARY KEY (id));
*/

public class WithdrawReceiptDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawReceiptDBHandler.class);
	
	private static String TABLE_NAME = "WithdrawReceipt";
	
	private static String ID = "id";
	private static String RECEIPT_CONTENTS = "receipt";
	
	private static final String CREATE_WITHDRAW_ENTRY = "INSERT INTO " + TABLE_NAME  
			+ "(" + RECEIPT_CONTENTS + ") VALUES"
			+ "(?)";
	private static final String GET_RECEIPT_BY_ID = "SELECT " + RECEIPT_CONTENTS + " FROM " + 
			TABLE_NAME + " WHERE " + ID + " = ?";
	
	private static WithdrawReceiptDBHandler instance = null;
	// Create a new entry
	// get an entry with the given id
	
	private WithdrawReceiptDBHandler() {
	}
	
	public static WithdrawReceiptDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In WithdrawReceiptDBHandler getInstance() method instance created");
			instance = new WithdrawReceiptDBHandler();
		}
		return instance;
	}
	
	public long createWDReceipt(String filePath) throws SQLException, FileNotFoundException {
		
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_WITHDRAW_ENTRY, Statement.RETURN_GENERATED_KEYS);
			ps.setBinaryStream(1, fis, (int) file.length());
			int affectedRowCount = ps.executeUpdate();
			
			if (affectedRowCount > 0) {
				rs = ps.getGeneratedKeys();
				long receiptId = -1;
				if ((rs != null) && (rs.next())) {
					receiptId = rs.getLong(1);
				}
				if (receiptId == -1) {
					throw new SQLException("createWDReceipt new rec id is -1");
				}
				return receiptId;
			}
			return -1;
			
		} catch (SQLException ex) {
			logger.error("SQL Exception in createWDReceipt()", ex);
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
	}
	
	public byte[] getReceiptContents(long id) throws SQLException {
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(GET_RECEIPT_BY_ID);
			ps.setLong(1, id);
			
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					Blob blob = rs.getBlob(RECEIPT_CONTENTS);
					byte[] b = blob.getBytes(1, (int) blob.length());
					return b;
				}
			}
			return null;
		} catch(SQLException ex) {
			logger.error("SQL Exception in getReceiptContents()", ex);
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
	}
}
