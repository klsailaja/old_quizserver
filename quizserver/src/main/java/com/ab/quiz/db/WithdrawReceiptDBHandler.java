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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
CREATE TABLE WITHDRAWRECEIPT(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
		TYPE INT SIGNED NOT NULL,
 		RECEIPT MEDIUMBLOB NULL DEFAULT NULL, PRIMARY KEY (ID));
*/

public class WithdrawReceiptDBHandler {
	
	private static final Logger logger = LogManager.getLogger(WithdrawReceiptDBHandler.class);
	
	private static String TABLE_NAME = "WITHDRAWRECEIPT";
	
	private static String ID = "ID";
	private static String RECEIPT_TYPE = "TYPE";
	private static String RECEIPT_CONTENTS = "RECEIPT";
	
	private static final String CREATE_WITHDRAW_ENTRY = "INSERT INTO " + TABLE_NAME  
			+ "(" + RECEIPT_TYPE + "," + RECEIPT_CONTENTS + ") VALUES"
			+ "(?,?)";
	private static final String GET_RECEIPT_BY_ID = "SELECT " + RECEIPT_CONTENTS + " FROM " + 
			TABLE_NAME + " WHERE " + ID + " = ?";
	
	private static final String REMOVE_OLD_RECORDS_RECEIPTS = "DELETE FROM " + TABLE_NAME 
			+ " WHERE " + ID + " = ? ";
	
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
	
	public long createWDReceipt(int receiptType, String filePath) throws SQLException, FileNotFoundException {
		
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
			ps.setInt(1, receiptType);
			ps.setBinaryStream(2, fis, (int) file.length());
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
	
	public void bulkDeleteReceiptRecords(List<Long> receiptIds, int batchSize) throws SQLException {
		if (receiptIds.size() == 0) {
			return;
		}
		
		long startTime = System.currentTimeMillis();
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement psPlayer = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			logger.info("In bulkDeletePlayerDetails ");
			
			dbConn.setAutoCommit(false);
			
			psPlayer = dbConn.prepareStatement(REMOVE_OLD_RECORDS_RECEIPTS);
			
			int totalFailureCount = 0;
			int totalSuccessCount = 0;
			
			int index = 0;
			for (Long id : receiptIds) {
				psPlayer.setLong(1, id);
				psPlayer.addBatch();
				index++;
				
				if (index == batchSize) {
					index = 0;
					int results[] = psPlayer.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					for (int result : results) {
						if (result >= 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = psPlayer.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result >= 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("Bulk deleted Receipt records with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
			logger.info("Time taken to process this query in Millis : {}", (System.currentTimeMillis() - startTime));
		} catch(SQLException ex) {
			logger.error("******************************");
			logger.error("Error deleting Receipt records entries in bulk mode", ex);
			logger.error("******************************");
			throw ex;
		} finally {
			if (psPlayer != null) {
				psPlayer.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
}
