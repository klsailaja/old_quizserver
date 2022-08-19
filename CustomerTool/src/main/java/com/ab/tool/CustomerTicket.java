package com.ab.tool;

public class CustomerTicket {
	private long id;
	private String refId;
	private long userId;
	private int requestType;
	private int status;
	private long openedTime;
	private long closedTime;
	private long problemPicId;
	private long resolvedPicId;
	private String extraDetails;
	private String closedCmts;
	private int sNo;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getRequestType() {
		return requestType;
	}
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getOpenedTime() {
		return openedTime;
	}
	public void setOpenedTime(long openedTime) {
		this.openedTime = openedTime;
	}
	public long getClosedTime() {
		return closedTime;
	}
	public void setClosedTime(long closedTime) {
		this.closedTime = closedTime;
	}
	public long getProblemPicId() {
		return problemPicId;
	}
	public void setProblemPicId(long problemPicId) {
		this.problemPicId = problemPicId;
	}
	public long getResolvedPicId() {
		return resolvedPicId;
	}
	public void setResolvedPicId(long resolvedPicId) {
		this.resolvedPicId = resolvedPicId;
	}
	public String getExtraDetails() {
		return extraDetails;
	}
	public void setExtraDetails(String extraDetails) {
		this.extraDetails = extraDetails;
	}
	public String getClosedCmts() {
		return closedCmts;
	}
	public void setClosedCmts(String closedCmts) {
		this.closedCmts = closedCmts;
	}
	
	public int getsNo() {
		return sNo;
	}
	public void setsNo(int sNo) {
		this.sNo = sNo;
	}
}
