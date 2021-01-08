package com.ab.quiz.pojo;

public class WithdrawReqByPhone {
	
	private long id;
	private long date;
	private String phNumber;
	private int paymentType; // Phone Pe, Paytm etc...
	private String userName;
	private long userProfielId;
	private int amt;
	
	public int getAmt() {
		return amt;
	}
	public void setAmt(int amt) {
		this.amt = amt;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	
	public long getUserProfielId() {
		return userProfielId;
	}
	public void setUserProfileId(long userProfielId) {
		this.userProfielId = userProfielId;
	}
	
	public String getPhNumber() {
		return phNumber;
	}
	public void setPhNumber(String phNumber) {
		this.phNumber = phNumber;
	}
	public int getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(int paymentType) {
		this.paymentType = paymentType;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
