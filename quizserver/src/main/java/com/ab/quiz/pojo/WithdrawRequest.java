package com.ab.quiz.pojo;

public class WithdrawRequest {
	private long id;
	private String refId;
	private long userProfileId; // from user
	private int fromAccType; // from user
	private int reqStatus;
	private int requestType; // from user
	private long accountDetailsId;
	private int amount; // from user
	private long openedTime; // from user
	private long closedTime;
	private long receiptId;
	private String closedComents;
	private int sNo;
	private WithdrawReqByPhone byPhone;
	private WithdrawReqByBank byBank;
	
	public void setByBank(WithdrawReqByBank byBank) {
		this.byBank = byBank;
	}
	public WithdrawReqByBank getByBank() {
		return byBank;
	}
	
	public WithdrawReqByPhone getByPhone() {
		return byPhone;
	}
	public void setByPhone(WithdrawReqByPhone byPhone) {
		this.byPhone = byPhone;
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
	public long getAccountDetailsId() {
		return accountDetailsId;
	}
	public void setAccountDetailsId(long accountDetailsId) {
		this.accountDetailsId = accountDetailsId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
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
	
	public int getsNo() {
		return sNo;
	}
	public void setsNo(int sNo) {
		this.sNo = sNo;
	}
	@Override
	public String toString() {
		return "WithdrawRequest [id=" + id + ", refId=" + refId + ", userProfileId=" + userProfileId + ", fromAccType="
				+ fromAccType + ", reqStatus=" + reqStatus + ", requestType=" + requestType + ", accountDetailsId="
				+ accountDetailsId + ", amount=" + amount + ", openedTime=" + openedTime + ", closedTime=" + closedTime
				+ ", receiptId=" + receiptId + ", closedComents=" + closedComents + ", sNo=" + sNo + ", byPhone="
				+ byPhone + ", receiptContents=" + "]";
	}
}
