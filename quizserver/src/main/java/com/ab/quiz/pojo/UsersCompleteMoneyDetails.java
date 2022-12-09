package com.ab.quiz.pojo;

import java.util.List;

public class UsersCompleteMoneyDetails {
	private String trackStatusKey;
	private boolean checkMoney;
	private List<MoneyTransaction> usersMoneyTransactionList;
	private int kycDocsStatus;  // 1 means approved
	private String logTag;
	private int operationType;
	
	public boolean isCheckMoney() {
		return checkMoney;
	}
	public void setCheckMoney(boolean checkMoney) {
		this.checkMoney = checkMoney;
	}
	
	public List<MoneyTransaction> getUsersMoneyTransactionList() {
		return usersMoneyTransactionList;
	}
	public void setUsersMoneyTransactionList(List<MoneyTransaction> usersMoneyTransactionList) {
		this.usersMoneyTransactionList = usersMoneyTransactionList;
	}
	public int getkycDocsStatus() {
		return kycDocsStatus;
	}
	public void setkycDocsStatus(int kycDocsStatus) {
		this.kycDocsStatus = kycDocsStatus;
	}
	public String getTrackStatusKey() {
		return trackStatusKey;
	}
	public void setTrackStatusKey(String trackStatusKey) {
		this.trackStatusKey = trackStatusKey;
	}
	
	public String getLogTag() {
		return logTag;
	}
	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}
	
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}
}
