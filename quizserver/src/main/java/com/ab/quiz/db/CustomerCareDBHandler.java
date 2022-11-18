package com.ab.quiz.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.CustomerCareReqState;
import com.ab.quiz.constants.PictureType;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.pojo.CCTicketsHolder;
import com.ab.quiz.pojo.CustomerTicket;


/*
CREATE TABLE CUSTOMERCAREREQS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		REFID VARCHAR(10) NOT NULL,
		USERID BIGINT NOT NULL,
		REQTYPE INT NOT NULL,
		OPENEDTIME BIGINT NOT NULL,
		STATUS INT NOT NULL,
		PROBLEMPICID BIGINT NOT NULL DEFAULT -1,
		RESOLVEDPICID BIGINT NOT NULL DEFAULT -1,
		CLOSEDTIME BIGINT NULL,
		EXTRADETAILS VARCHAR(300) NULL,
		CLOSECMTS VARCHAR(100) NULL, PRIMARY KEY (ID)) ENGINE = INNODB;
		
CREATE INDEX CUSTOMERCAREREQS_Inx1 ON CUSTOMERCAREREQS(USERID);		
DROP INDEX CUSTOMERCAREREQS_Inx1 ON CUSTOMERCAREREQS;		
CREATE INDEX CUSTOMERCAREREQS_Inx1 ON CUSTOMERCAREREQS(USERID);

*/



public class CustomerCareDBHandler {
	private static String TABLE_NAME = "CUSTOMERCAREREQS"; 
	
	private static String ID = "ID";
	private static String REFID = "REFID";
	private static String USER_PROFILE_ID = "USERID";
	private static String REQUEST_TYPE = "REQTYPE";
	private static String STATUS = "STATUS";
	private static String PROBLEM_PIC_ID = "PROBLEMPICID";
	private static String RESOLVE_PIC_ID = "RESOLVEDPICID";
	private static String REQUEST_OPENED_TIME = "OPENEDTIME";
	private static String REQUEST_CLOSED_TIME = "CLOSEDTIME";
	private static String EXTRADETAILS = "EXTRADETAILS";
	private static String CLOSECMTS = "CLOSECMTS";
	
	private static CustomerCareDBHandler instance = null;
	
	private static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
				+ "abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom secureRnd = new SecureRandom();
	private static final int REFERENCE_MAX_LEN = 10;
	private static final int MAX_ROWS = 5;
	private static final String MAX_WITHDRAW_REQ_ID = "SELECT MAX(ID) FROM " + TABLE_NAME;
	
	private static final String REMOVE_OLD_RECORDS = "DELETE FROM " + TABLE_NAME 
			+ " WHERE (" + REQUEST_CLOSED_TIME + " < ? AND ID <> 0 AND STATUS <>" + CustomerCareReqState.OPEN.getId() + ")";
	private static final String GET_PROBLEM_PIC_IDS = "SELECT " + PROBLEM_PIC_ID + " FROM " + TABLE_NAME + " WHERE (" + 
			REQUEST_CLOSED_TIME + " < ? AND STATUS <>" + CustomerCareReqState.OPEN.getId() + ")";
	private static final String GET_RESOLVED_PIC_IDS = "SELECT " + RESOLVE_PIC_ID + " FROM " + TABLE_NAME + " WHERE (" + 
			REQUEST_CLOSED_TIME + " < ? AND STATUS <>" + CustomerCareReqState.OPEN.getId() + ")";
	
	private static final String GET_TICKET_ENTRY_BY_REF_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " + REFID + " = ?";
	
	private static final String GET_TICKET_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE " 
			+ ID + " = ?";
	
	private static final String UPDATE_TICKET_BY_ID = "UPDATE " + TABLE_NAME + " SET " + PROBLEM_PIC_ID + "= ? WHERE " + ID + " = ?";

	
	private static final String CLOSE_TICKET_BY_REF_ID = "UPDATE " + TABLE_NAME + " SET "
			+ STATUS + " = ? ," + REQUEST_CLOSED_TIME + " = ? ,"
			+ RESOLVE_PIC_ID + " = ? ," + CLOSECMTS + " = ? "
			+ " WHERE (" + REFID + " = ? AND ID <> 0)";
	
	private static final String CREATE_TICKET_ENTRY = "INSERT INTO " + TABLE_NAME
			+ "(" + REFID + "," + USER_PROFILE_ID + "," 
			+ REQUEST_TYPE + "," + REQUEST_OPENED_TIME + "," + STATUS + ","  
			+ PROBLEM_PIC_ID + "," + RESOLVE_PIC_ID + "," + REQUEST_CLOSED_TIME + ","
			+ EXTRADETAILS + "," + CLOSECMTS + ") VALUES"
			+ "(?,?,?,?,?,?,?,?,?,?)";
	
	private static final String GET_TOTAL_COUNT = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USER_PROFILE_ID + " = ?";
	private static final String GET_TOTAL_COUNT_BY_STATUS = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE "
			+ USER_PROFILE_ID + " = ? AND " + STATUS + " = ?";
	private static final String GET_DATA_BY_USER_ID_STATUS = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USER_PROFILE_ID + " = ? AND " + STATUS + " = ? ORDER BY " + REQUEST_OPENED_TIME + " DESC LIMIT ?, " + MAX_ROWS;
	
	private static final String GET_DATA_BY_USER_ID = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + USER_PROFILE_ID + " = ? ORDER BY " + REQUEST_OPENED_TIME + " DESC LIMIT ?, " + MAX_ROWS;
	
	private static final String REMOVE_TICKET_BY_ID = "DELETE FROM " + TABLE_NAME 
			+ " WHERE " + ID + " = ? ";
	
	private static final Logger logger = LogManager.getLogger(CustomerCareDBHandler.class);
	
	private CustomerCareDBHandler() {
	}
	
	public static CustomerCareDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In getInstance() method instance created");
			instance = new CustomerCareDBHandler();
		}
		return instance;
	}
	
	public int deleteRecords(long timePeriod) throws SQLException {
		
		logger.info("In deleteRecords method");
		
		List<Long> oldProblemPicIds = getOldCCIds(timePeriod, 1);
		List<Long> oldResolvedPicIds = getOldCCIds(timePeriod, 2);
		oldProblemPicIds.addAll(oldResolvedPicIds);
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(REMOVE_OLD_RECORDS);
			
			ps.setLong(1, timePeriod);
			
			int result = ps.executeUpdate();
			logger.debug("In deleteRecords operation result : {}", result);
			
			for (long delPicId : oldProblemPicIds) {
				PictureDBHandler.getInstance().deletePictureEntry(delPicId);
			}
			return result;
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in deleteRecords ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	public boolean updateTicketById(long ticketId, long pictureId) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(UPDATE_TICKET_BY_ID);
		
		ps.setLong(1, ticketId);
		ps.setLong(2, pictureId);
		
		int operResult = 0;
		
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while updateTicketById for {} : {}", ticketId, pictureId);
			logger.error("SQLException in ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public boolean removeTicketById(long ticketId) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(REMOVE_TICKET_BY_ID);
		
		ps.setLong(1, ticketId);
		
		int operResult = 0;
		
		try {
			int resultCount = ps.executeUpdate();
			if (resultCount > 0) {
				operResult = 1;
			}
		}
		catch(SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Exception while removeTicketById for {}", ticketId);
			logger.error("SQLException in ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public long getMaxCCReqId() throws SQLException {
		
		logger.debug("In getMaxCCReqId() method");
		
		long maxId = -1;
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
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("SQL Exception in getMaxCCReqId()", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		logger.debug("Returning from getMaxCCReqId() {}", maxId);
		return maxId;
	}
	
	private List<Long> getOldCCIds(long timePeriod, int type) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		String sql = GET_PROBLEM_PIC_IDS;
		String columnName = PROBLEM_PIC_ID;
		if (type == 2) {
			sql = GET_RESOLVED_PIC_IDS;
			columnName = RESOLVE_PIC_ID;
		}
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ps.setLong(1, timePeriod);
		
		ResultSet rs = null;
		List<Long> oldWDReceiptIds = new ArrayList<>();
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					oldWDReceiptIds.add(rs.getLong(columnName));
				}
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("SQLException in getOldCCIds()", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		logger.info("Old CC Records to be deleted size is {}", oldWDReceiptIds.size());
		return oldWDReceiptIds;
	}
	
	public CustomerTicket getCustomerTicketById(long tktId) throws SQLException {
		
		logger.debug("In getCustomerTicketById() with {}", tktId);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_TICKET_BY_ID);
		ps.setLong(1, tktId);
		ResultSet rs = null;
		
		CustomerTicket ticket = new CustomerTicket();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					ticket.setId(rs.getLong(ID));
					ticket.setRefId(rs.getString(REFID));
					ticket.setUserId(rs.getLong(USER_PROFILE_ID));
					ticket.setStatus(rs.getInt(STATUS));
					ticket.setRequestType(rs.getInt(REQUEST_TYPE));
					ticket.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					ticket.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					ticket.setProblemPicId(rs.getLong(PROBLEM_PIC_ID));
					ticket.setResolvedPicId(rs.getLong(RESOLVE_PIC_ID));
					ticket.setExtraDetails(rs.getString(EXTRADETAILS));
					ticket.setClosedCmts(rs.getString(CLOSECMTS));
				}
				
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("SQLException in getCustomerTicketById()", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		return ticket;
	}
	
	public CustomerTicket getCustomerTicketByRefId(String refId) throws SQLException {
		
		refId = refId.trim();
		logger.debug("In getCustomerTicketByRefId() with {}", refId);
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_TICKET_ENTRY_BY_REF_ID);
		ps.setString(1, refId);
		ResultSet rs = null;
		
		CustomerTicket ticket = new CustomerTicket();
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					ticket.setId(rs.getLong(ID));
					ticket.setRefId(rs.getString(REFID));
					ticket.setUserId(rs.getLong(USER_PROFILE_ID));
					ticket.setStatus(rs.getInt(STATUS));
					ticket.setRequestType(rs.getInt(REQUEST_TYPE));
					ticket.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					ticket.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					ticket.setProblemPicId(rs.getLong(PROBLEM_PIC_ID));
					ticket.setResolvedPicId(rs.getLong(RESOLVE_PIC_ID));
					ticket.setExtraDetails(rs.getString(EXTRADETAILS));
					ticket.setClosedCmts(rs.getString(CLOSECMTS));
				}
				
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("SQLException in getCustomerTicketByRefId()", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
		return ticket;
	}
	
	public boolean updateCCRequestState(String ccRefId, CustomerCareReqState ticketState, String cmts, String resolvedFileName) 
			throws SQLException, NotAllowedException, FileNotFoundException, Exception {
		
		CustomerTicket ccRequest = getCustomerTicketByRefId(ccRefId);
		if (ccRequest.getId() == 0) {
			throw new NotAllowedException("No CustomerCare Request found");
		}
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement updateWDStatePS = null;
		int result1 = 0;
		
		try {
			long receiptId = -1;
			if (resolvedFileName != null) {
				BufferedImage bImage = ImageIO.read(new File(resolvedFileName));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(bImage, "jpg", bos );
			    byte [] data = bos.toByteArray();
			    
			    int picType = PictureType.TICKET_OPENED.getId();
			    
			    if ((CustomerCareReqState.CLOSED.getId() == ticketState.getId()) 
			    	|| (CustomerCareReqState.NOT_AN_ISSUE.getId() == ticketState.getId())
			    	|| (CustomerCareReqState.CANCELLED.getId() == ticketState.getId())) {
			    	
			    	picType = PictureType.TICKET_CLOSED.getId();
			    }
			    
				receiptId = PictureDBHandler.getInstance().createPictureDBEntry(String.valueOf(ccRequest.getId()), 
						picType, data, false, ccRequest.getProblemPicId());
				logger.info("The receipt file contents DB Entry id is {}", receiptId);
				if (receiptId == -1) {
					logger.error("Could not insert the resolved pic file contents to DB");
				}
			}
			
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			updateWDStatePS = dbConn.prepareStatement(CLOSE_TICKET_BY_REF_ID);
			
			updateWDStatePS.setInt(1, ticketState.getId());
			updateWDStatePS.setLong(2, System.currentTimeMillis());
			updateWDStatePS.setLong(3, receiptId);
			updateWDStatePS.setString(4, cmts);
			updateWDStatePS.setString(5, ccRefId);
			
			result1 = updateWDStatePS.executeUpdate();
			
			logger.debug("Changed the CC Ticket state result {}", (result1 > 0));
			
			return true;
			 
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error while executing closeCCRequest ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
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
	
	public long createCCTicket(CustomerTicket ticket) throws SQLException {
		
		long maxReqId = getMaxCCReqId() + 1;
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			ps = dbConn.prepareStatement(CREATE_TICKET_ENTRY, Statement.RETURN_GENERATED_KEYS);
			
			int idStrLen = String.valueOf(maxReqId).length();
			int remainingLen = REFERENCE_MAX_LEN - idStrLen;
			
			String refId = getReferenceNumber(remainingLen, maxReqId);
			ps.setString(1, refId);
			ps.setLong(2, ticket.getUserId());
			ps.setInt(3, ticket.getRequestType());
			ps.setLong(4, System.currentTimeMillis());
			ps.setInt(5, CustomerCareReqState.OPEN.getId());
			ps.setLong(6, -1);
			ps.setLong(7, -1);
			ps.setLong(8, -1);
			ps.setString(9, ticket.getExtraDetails());
			ps.setNull(10, Types.NULL);
			
			int result = ps.executeUpdate();
			logger.debug("In createCCTicket create op result : {}", result);
			
			if (result > 0) {
				ResultSet idRes = ps.getGeneratedKeys();
				if (idRes.next()) {
					long userProfileId = idRes.getLong(1);
					idRes.close();
					return userProfileId;
				}
			}
		} catch (SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error creating createCCTicket ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return -1;
	}
	
	private String getReferenceNumber(int maxLen, long maxId) throws SQLException {
		StringBuilder sb = new StringBuilder(maxLen); 
		for (int i = 0; i < maxLen; i++) 
			sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
		
		sb.append(maxId);
		return sb.toString();
	}
	
	public CCTicketsHolder getCCTickets(long userProfileId, int startRowNumber, int state) 
			throws SQLException, NotAllowedException {
		
		logger.info("In getCCTickets() with {} {} {}", userProfileId, state, startRowNumber);
		
		/*UserProfile userProfile = UserProfileDBHandler.getInstance().getProfileById(userProfileId);
		if (userProfile.getId() == 0) {
			throw new NotAllowedException("User not found with id " + userProfileId);
		}*/
		
		String totalSql = GET_TOTAL_COUNT;
		String sql = GET_DATA_BY_USER_ID;
		
		if (state != -1) {
			totalSql = GET_TOTAL_COUNT_BY_STATUS;
			sql = GET_DATA_BY_USER_ID_STATUS;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement totalPs = dbConn.prepareStatement(totalSql);
		PreparedStatement ps = dbConn.prepareStatement(sql);
		ResultSet totalRs = null;
		ResultSet rs = null;
		
		totalPs.setLong(1, userProfileId);
		ps.setLong(1, userProfileId);
		
		if (state != -1) {
			totalPs.setInt(2, state);
			
			ps.setInt(2, state);
			ps.setInt(3, startRowNumber);
		} else {
			ps.setInt(2, startRowNumber);
		}
		
		logger.info("Total SQL {}", totalSql);
		logger.info("SQL {}", sql);
		
		CCTicketsHolder holder = new CCTicketsHolder();
		List<CustomerTicket> dataList = new ArrayList<>();
		
		try {
			totalRs = totalPs.executeQuery();
			if (totalRs != null) {
				if (totalRs.next()) {
					
					int total = totalRs.getInt("COUNT(*)");
					holder.setTotal(total);
					
					int lowerRange = startRowNumber + 1;
					int higherRange = startRowNumber + MAX_ROWS;
					
					if (higherRange < total) {
						holder.setNextEnabled(true);
					} else {
						holder.setNextEnabled(false);
					}
					if ((lowerRange - MAX_ROWS) > 0) {
						holder.setPrevEnabled(true);
					} else {
						holder.setPrevEnabled(false);
					}
					
				}
			}
			
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					
					CustomerTicket dataItem = new CustomerTicket();
					
					dataItem.setsNo(++startRowNumber);
					
					dataItem.setId(rs.getLong(ID));
					dataItem.setRefId(rs.getString(REFID));
					dataItem.setUserId(rs.getLong(USER_PROFILE_ID));
					dataItem.setRequestType(rs.getInt(REQUEST_TYPE));
					dataItem.setStatus(rs.getInt(STATUS));
					
					dataItem.setOpenedTime(rs.getLong(REQUEST_OPENED_TIME));
					dataItem.setClosedTime(rs.getLong(REQUEST_CLOSED_TIME));
					dataItem.setProblemPicId(rs.getLong(PROBLEM_PIC_ID));
					dataItem.setResolvedPicId(rs.getLong(RESOLVE_PIC_ID));
					dataItem.setExtraDetails(rs.getString(EXTRADETAILS));
					dataItem.setClosedCmts(rs.getString(CLOSECMTS));
					
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
	
	public void createTicketsInBulk(List<CustomerTicket> list, int batchSize) throws SQLException {
		String tag = "CreateTickets:";
		logger.info("{} The Tickets size is: {}", tag, list.size());
		long maxReqId = getMaxCCReqId() + 1;
		
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		int totalFailureCount = 0;
		int totalSuccessCount = 0;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_TICKET_ENTRY);
			int index = 0;
			
			for (CustomerTicket ticket : list) {
				int idStrLen = String.valueOf(maxReqId).length();
				int remainingLen = REFERENCE_MAX_LEN - idStrLen;
				
				String refId = getReferenceNumber(remainingLen, maxReqId++);
				ps.setString(1, refId);
				ps.setLong(2, ticket.getUserId());
				ps.setInt(3, ticket.getRequestType());
				ps.setLong(4, System.currentTimeMillis());
				ps.setInt(5, CustomerCareReqState.OPEN.getId());
				ps.setLong(6, -1);
				ps.setLong(7, -1);
				ps.setLong(8, -1);
				ps.setString(9, ticket.getExtraDetails());
				ps.setNull(10, Types.NULL);
			
				ps.addBatch();
				index++;
				
				if (index % batchSize == 0) {
					int results[] = ps.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					for (int result : results) {
						if (result == 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = ps.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result == 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("End of createTicketsInBulk with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
		} catch(SQLException ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("Error in creating createTicketsInBulk ", ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
}
