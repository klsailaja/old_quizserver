package com.ab.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


/*
 *
 CREATE TABLE WITHDRAWREQUESTS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		REFID VARCHAR(10) NOT NULL,
		USERID BIGINT NOT NULL,
		STATUS INT NOT NULL,
		REQTYPE INT NOT NULL,
		ACCOUNTDETAILSID BIGINT NOT NULL,
		AMOUNT INT NOT NULL,
		OPENEDTIME BIGINT NOT NULL,
		CLOSEDTIME BIGINT NULL,
		RECEIPTID BIGINT NULL,
		CLOSECMTS VARCHAR(100), PRIMARY KEY (ID)) ENGINE = INNODB;
		
CREATE INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS(USERID);		
DROP INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS;		
CREATE INDEX WITHDRAWREQUESTS_Inx1 ON WITHDRAWREQUESTS(USERID);

*/

public class WithdrawDBHandler {
	
	private static String TABLE_NAME = "WITHDRAWREQUESTS"; 
	
	private static String ID = "ID";
	private static String REFID = "REFID";
	private static String USER_PROFILE_ID = "USERID";
	private static String STATUS = "STATUS";
	private static String REQUEST_TYPE = "REQTYPE";
	private static String ACDETAILS_ID = "ACCOUNTDETAILSID";
	private static String AMOUNT = "AMOUNT";
	private static String REQUEST_OPENED_TIME = "OPENEDTIME";
	private static String REQUEST_CLOSED_TIME = "CLOSEDTIME";
	private static String TRANSACTION_RECEIPT_ID = "RECEIPTID";
	private static String CLOSED_CMTS = "CLOSECMTS";
	
	private static WithdrawDBHandler instance = null;
	
	//private static int MAX_ROWS = 5;
	
	private static final int IMP_DATA = 1;
	private static final int ALL_DATA = 2;
	private static final int ONE_DATA = 3;
	
	private static final String GET_DATA_BY_STATUS = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + STATUS + " = ? ORDER BY " + REQUEST_OPENED_TIME + " DESC LIMIT ?, ?" ;
	private static final String GET_DATA_BY_ID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + ID + " = ? ";
	private static final String GET_IMP_DATA = "SELECT * FROM " + TABLE_NAME
			+ " WHERE (" + STATUS + " = ? ) AND (? -" + REQUEST_OPENED_TIME + ") >= " 
			+ Constants.WD_REQ_MAX_OPEN_DAYS_IN_MILLIS + " ORDER BY " + ID + " DESC LIMIT ?, ?";
	
	private static final String GET_DATA_BY_STATUS_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME 
			+ " WHERE " + STATUS + " = ? ORDER BY " + REQUEST_OPENED_TIME;
	private static final String GET_IMP_DATA_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME 
			+ " WHERE (" + STATUS + " = ? ) AND (? -" + REQUEST_OPENED_TIME + ") >= " 
			+ Constants.WD_REQ_MAX_OPEN_DAYS_IN_MILLIS;

	private static final String GET_WITHDRAW_ENTRY_BY_REF_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " + REFID + " = ?";
	private static final String CLOSE_WITHDRAW_ENTRY_BY_REF_ID = "UPDATE " + TABLE_NAME + " SET "
			+ STATUS + " = ? ," + REQUEST_CLOSED_TIME + " = ? ,"
			+ TRANSACTION_RECEIPT_ID + " = ? ," + CLOSED_CMTS + " = ? "
			+ " WHERE (" + REFID + " = ? AND ID <> 0)";
	
	
	
	private WithdrawDBHandler() {
	}
	
	public static WithdrawDBHandler getInstance() {
		if (instance == null) {
			System.out.println("In getInstance() method instance created");
			instance = new WithdrawDBHandler();
		}
		return instance;
	}
	
	public WithdrawRequest getWithdrawReqByRefId(String refId) throws SQLException {
		
		refId = refId.trim();
		System.out.println("In getWithdrawReqByRefId() with " + refId);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_WITHDRAW_ENTRY_BY_REF_ID);
		ps.setString(1, refId);
		ResultSet rs = null;
		
		WithdrawRequest wdRequest = new WithdrawRequest();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					wdRequest.setId(rs.getLong(ID));
					wdRequest.setRefId(rs.getString(REFID));
					wdRequest.setUserProfileId(rs.getLong(USER_PROFILE_ID));
					wdRequest.setReqStatus(rs.getInt(STATUS));
					wdRequest.setRequestType(rs.getInt(REQUEST_TYPE));
					wdRequest.setAccountDetailsId(rs.getInt(ACDETAILS_ID));
					wdRequest.setAmount(rs.getInt(AMOUNT));
					wdRequest.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					wdRequest.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					wdRequest.setReceiptId(rs.getLong(TRANSACTION_RECEIPT_ID));
					wdRequest.setClosedComents(rs.getString(CLOSED_CMTS));
				}
				
			}
		} catch (SQLException ex) {
			System.out.println("SQLException in getWithdrawReqByRefId()");
			ex.printStackTrace();
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
		return wdRequest;
	}
	
	
	public boolean closeWithDrawRequest(String receiptFileName, String withdrawRefId, String wdClosedCmts) 
			throws SQLException, NotAllowedException, FileNotFoundException, Exception {
		// all validations
		// Insert the receipt file
		// Make the withdraw request as closed..
		WithdrawRequest wdRequest = getWithdrawReqByRefId(withdrawRefId);
		if (wdRequest.getId() == 0) {
			throw new NotAllowedException("No Withdraw Request found");
		}
		if (wdRequest.getReqStatus() != WithdrawReqState.OPEN.getId()) {
			throw new NotAllowedException("This type of Request cannot be closed");
		}
		
		long time = System.currentTimeMillis();
		String comments = "Withdraw Request Processed. Receipt Attached for " + wdRequest.getRefId();
		
		MyTransaction transaction = Utils.getTransactionPojo(wdRequest.getUserProfileId(), time, 
				wdRequest.getAmount(), TransactionType.CLOSED.getId(), 
				wdRequest.getFromAccType(), -1, -1, comments, null);
		
		WithdrawMoney wdMoneyDetails = new WithdrawMoney();
		
		wdMoneyDetails.setUid(wdRequest.getUserProfileId());
		wdMoneyDetails.setWdAmt(wdRequest.getAmount());
		wdMoneyDetails.setWdType(WithdrawReqState.CLOSED.getId());
		wdMoneyDetails.setTransaction(transaction);
		
		PostTask<WithdrawMoney, Boolean> wdTask = Request.performWitdrawTask();
		wdTask.setPostObject(wdMoneyDetails);
		boolean wdOperationResult = (boolean) wdTask.execute();
		
		if (!wdOperationResult) {
			return false;
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement updateWDStatePS = null;
		int result1 = 0;
		
		try {
			BufferedImage bImage = ImageIO.read(new File(receiptFileName));
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ImageIO.write(bImage, "jpg", bos );
		    byte [] data = bos.toByteArray();
			long receiptId = PictureDBHandler.getInstance().createPictureDBEntry(String.valueOf(wdRequest.getId()), 
					PictureType.WDRECEIPT.getId(), data, false, -1);
			System.out.println("The receipt file contents DB Entry id is " + receiptId);
			if (receiptId == -1) {
				System.out.println("Could not insert the receipt file contents to DB");
			}
			
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			updateWDStatePS = dbConn.prepareStatement(CLOSE_WITHDRAW_ENTRY_BY_REF_ID);
			
			updateWDStatePS.setInt(1, WithdrawReqState.CLOSED.getId());
			updateWDStatePS.setLong(2, System.currentTimeMillis());
			updateWDStatePS.setLong(3, receiptId);
			updateWDStatePS.setString(4, wdClosedCmts);
			updateWDStatePS.setString(5, withdrawRefId);
			
			result1 = updateWDStatePS.executeUpdate();
			
			System.out.println("Changed the withdraw req state result: " + (result1 > 0));
			
			return true;
			 
		} catch (SQLException ex) {
			System.out.println("Error while executing closeWithDrawRequest ");
			ex.printStackTrace();
			throw ex;
		} finally {
			if (updateWDStatePS != null) {
				updateWDStatePS.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	
	public WithdrawRequestsHolder getWithdrawRequests(int queryType, int startRowNumber, 
			int state, long wdId, int pageLength) 
			throws SQLException, NotAllowedException {
		
		System.out.println("In getWithdrawRequests() with : " + state + ":" + startRowNumber);
		System.out.println("queryType :" + queryType);
		
		String totalSql = GET_IMP_DATA_COUNT;
		String sql = GET_IMP_DATA;
		
		if (queryType == ALL_DATA) {
			totalSql = GET_DATA_BY_STATUS_COUNT;
			sql = GET_DATA_BY_STATUS;
			System.out.println("totalSql : " + totalSql);
			System.out.println("sql : " + sql);
		} else if (queryType == ONE_DATA) {
			totalSql = null;
			sql = GET_DATA_BY_ID;
		} else if (queryType == IMP_DATA) {
			System.out.println("IMP Data");
			totalSql = GET_IMP_DATA_COUNT;
			sql = GET_IMP_DATA;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement totalPs = null;
		if (totalSql != null) {
			totalPs = dbConn.prepareStatement(totalSql); 
		}
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ResultSet totalRs = null;
		ResultSet rs = null;
		
		if (queryType == ALL_DATA) {
			totalPs.setInt(1, state);
			
			ps.setInt(1, state);
			ps.setInt(2, startRowNumber);
			ps.setInt(3, pageLength);
		} else if (queryType == ONE_DATA) {
			ps.setLong(1, wdId);
		} else if (queryType == IMP_DATA) {
			totalPs.setInt(1, state);
			totalPs.setLong(2, System.currentTimeMillis());
			
			ps.setInt(1, state);
			ps.setLong(2, System.currentTimeMillis());
			ps.setInt(3, startRowNumber);
			ps.setInt(4, pageLength);
		}
		
		System.out.println("Raj totalSql : " + totalSql);
		System.out.println("Raj sql : " + sql);
		
		WithdrawRequestsHolder holder = new WithdrawRequestsHolder();
		List<WithdrawRequest> dataList = new ArrayList<>();
		
		try {
			int total = 0;
			if (totalPs != null) {
				totalRs = totalPs.executeQuery();
			} else {
				total = 1;
				holder.setTotal(total);
			}
			if (totalRs != null) {
				if (totalRs.next()) {
					total = totalRs.getInt("COUNT(*)");
					holder.setTotal(total);
				}
			}
			int lowerRange = startRowNumber + 1;
			int higherRange = startRowNumber + pageLength;
			
			if (higherRange < total) {
				holder.setNextEnabled(true);
			} else {
				holder.setNextEnabled(false);
			}
			if ((lowerRange - pageLength) > 0) {
				holder.setPrevEnabled(true);
			} else {
				holder.setPrevEnabled(false);
			}
	
			
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					
					WithdrawRequest dataItem = new WithdrawRequest();
					
					dataItem.setsNo(++startRowNumber);
					
					dataItem.setId(rs.getLong(ID));
					dataItem.setRefId(rs.getString(REFID));
					dataItem.setUserProfileId(rs.getLong(USER_PROFILE_ID));
					dataItem.setReqStatus(rs.getInt(STATUS));
					dataItem.setRequestType(rs.getInt(REQUEST_TYPE));
					dataItem.setAccountDetailsId(rs.getLong(ACDETAILS_ID));
					dataItem.setAmount(rs.getInt(AMOUNT));
					dataItem.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					dataItem.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					dataItem.setReceiptId(rs.getLong(TRANSACTION_RECEIPT_ID));
					dataItem.setClosedComents(rs.getString(CLOSED_CMTS));
					
					WithdrawReqType wdReqType = WithdrawReqType.BY_PHONE;
					
					if (wdReqType.getId() == dataItem.getRequestType()) {
						WithdrawByPhoneReqDBHandler dbHandler = WithdrawByPhoneReqDBHandler.getInstance();
					
						WithdrawReqByPhone wdByPhone = dbHandler.getWithdrawReqByPhoneById(dataItem.getAccountDetailsId());
						dataItem.setByPhone(wdByPhone);
					} else if (dataItem.getRequestType() == WithdrawReqType.BY_BANK.getId()) {
						WithdrawByBankDBHandler dbHandler = WithdrawByBankDBHandler.getInstance();
						
						WithdrawReqByBank wdByBank = dbHandler.getWithdrawReqByBankById(dataItem.getAccountDetailsId());
						dataItem.setByBank(wdByBank);
					}
					dataList.add(dataItem);
				}
			}
		} catch (SQLException ex) {
			throw ex;
		} finally {
			if (totalRs != null) {
				totalRs.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (totalPs != null) {
				totalPs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		holder.setList(dataList);
		
		return holder;
	}
}
