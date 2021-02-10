package com.ab.quiz.pojo;

public class WithdrawRequest {
	private long id;
	private String refId;
	private long userProfileId; // from user
	private int fromAccType; // from user
	private int reqStatus;
	private int requestType; // from user
	private int accountDetailsId;
	private int amount; // from user
	private int openedTime; // from user
	private int closedTime;
	private long receiptId;
	private String closedComents;
	
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
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public int getFromAccType() {
		return fromAccType;
	}
	public void setFromAccType(int fromAccType) {
		this.fromAccType = fromAccType;
	}
	public int getReqStatus() {
		return reqStatus;
	}
	public void setReqStatus(int reqStatus) {
		this.reqStatus = reqStatus;
	}
	public int getRequestType() {
		return requestType;
	}
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	public int getAccountDetailsId() {
		return accountDetailsId;
	}
	public void setAccountDetailsId(int accountDetailsId) {
		this.accountDetailsId = accountDetailsId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getOpenedTime() {
		return openedTime;
	}
	public void setOpenedTime(int openedTime) {
		this.openedTime = openedTime;
	}
	public int getClosedTime() {
		return closedTime;
	}
	public void setClosedTime(int closedTime) {
		this.closedTime = closedTime;
	}
	public long getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(long receiptId) {
		this.receiptId = receiptId;
	}
	public String getClosedComents() {
		return closedComents;
	}
	public void setClosedComents(String closedComents) {
		this.closedComents = closedComents;
	}
}
