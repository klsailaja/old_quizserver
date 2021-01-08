package com.ab.quiz.pojo;

public class WithdrawHistory {
	private long id;
	private String refId;
	private int status;
	private int reqType;
	private long accountDetailsRecord;
	private int amount;
	private String accDetails;
	private String closingComments;
	
	
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getReqType() {
		return reqType;
	}
	public void setReqType(int reqType) {
		this.reqType = reqType;
	}
	public String getAccDetails() {
		return accDetails;
	}
	public void setAccDetails(String accDetails) {
		this.accDetails = accDetails;
	}
	public String getClosingComments() {
		return closingComments;
	}
	public void setClosingComments(String closingComments) {
		this.closingComments = closingComments;
	}
	public long getAccountDetailsRecord() {
		return accountDetailsRecord;
	}
	public void setAccountDetailsRecord(long accountDetailsRecord) {
		this.accountDetailsRecord = accountDetailsRecord;
	}
}
