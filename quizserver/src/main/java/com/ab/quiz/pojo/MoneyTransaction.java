package com.ab.quiz.pojo;

import com.ab.quiz.constants.UserMoneyAccountType;
import com.ab.quiz.constants.UserMoneyOperType;

public class MoneyTransaction {
	
	private UserMoneyAccountType accountType;
	private UserMoneyOperType operType;
	private long userProfileId;
	private long amount;
	private MyTransaction transaction;
	private String trackStatusKey;
	
	public MoneyTransaction() {
		
	}
	
	public MoneyTransaction(UserMoneyAccountType accountType, UserMoneyOperType operType, long userProfileId,
			long amount, MyTransaction transaction, String trackStatusKey) {
		super();
		this.accountType = accountType;
		this.operType = operType;
		this.userProfileId = userProfileId;
		this.amount = amount;
		this.transaction = transaction;
		this.trackStatusKey = trackStatusKey;
	}
	
	public UserMoneyAccountType getAccountType() {
		return accountType;
	}
	public void setAccountType(UserMoneyAccountType accountType) {
		this.accountType = accountType;
	}
	public UserMoneyOperType getOperType() {
		return operType;
	}
	public void setOperType(UserMoneyOperType operType) {
		this.operType = operType;
	}
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public MyTransaction getTransaction() {
		return transaction;
	}
	public void setTransaction(MyTransaction transaction) {
		this.transaction = transaction;
	}
	public String getTrackStatusKey() {
		return trackStatusKey;
	}
	public void setTrackStatusKey(String trackStatusKey) {
		this.trackStatusKey = trackStatusKey;
	}
}
