package com.ab.quiz.pojo;

public class WDUserInput {
	private long userProfileId; 
	private int requestType; 
	private int amount; 
	private long openedTime;
	
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
