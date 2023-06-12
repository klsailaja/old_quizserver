package com.ab.quiz.pojo;

public class WDUserInput {
	private long userProfileId; 
	private int requestType; 
	private int amount;
	private int coinCount;
	private boolean moneyMode;
	private long openedTime;
	
	public int getCoinCount() {
		return coinCount;
	}
	public void setCoinCount(int coinCount) {
		this.coinCount = coinCount;
	}
	public boolean isMoneyMode() {
		return moneyMode;
	}
	public void setMoneyMode(boolean moneyMode) {
		this.moneyMode = moneyMode;
	}
	
	
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public int getRequestType() {
		return requestType;
	}
	public void setRequestType(int requestType) {
		this.requestType = requestType;
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
}
