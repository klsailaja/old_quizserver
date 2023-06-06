package com.ab.quiz.pojo;

public class UserMoneyOpersStatusGiver {
	// to be filled by the request objects
	private int serverId;
	private int requestId;
	private int userPerformedMoneyOperation;
	
	// to be filled after the response is received
	private int moneyOverallStatus;
	private long processedTime;
	
	public int getServerId() {
		return serverId;
	}
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	public int getUserPerformedMoneyOperation() {
		return userPerformedMoneyOperation;
	}
	public void setUserPerformedMoneyOperation(int userPerformedMoneyOperation) {
		this.userPerformedMoneyOperation = userPerformedMoneyOperation;
	}
	public int getMoneyOverallStatus() {
		return moneyOverallStatus;
	}
	public void setMoneyOverallStatus(int moneyOverallStatus) {
		this.moneyOverallStatus = moneyOverallStatus;
	}
	public long getProcessedTime() {
		return processedTime;
	}
	public void setProcessedTime(long processedTime) {
		this.processedTime = processedTime;
	}
}
