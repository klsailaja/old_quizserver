package com.ab.quiz.pojo;

public class WithdrawReqByBankAcc {
	
	private long id;
	private long date;
	private String accNumber;
	private String ifscCode;
	private String bankName;
	private String userName;
	private long userProfielId;
	
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
	public String getAccNumber() {
		return accNumber;
	}
	public void setAccNumber(String accNumber) {
		this.accNumber = accNumber;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public long getUserProfielId() {
		return userProfielId;
	}
	public void setUserProfielId(long userProfielId) {
		this.userProfielId = userProfielId;
	}
}
