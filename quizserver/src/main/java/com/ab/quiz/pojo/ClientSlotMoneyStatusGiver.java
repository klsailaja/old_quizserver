package com.ab.quiz.pojo;

import java.util.List;

public class ClientSlotMoneyStatusGiver {
	// to be filled by the request objects
	private int serverId;
	private int requestId;
	private long gameSlotTime;
	private int operationType;
	
	// to be filled after the response is received
	private int moneyOverallStatus;
	private long processedTime;
	// Just one field in the below object
	private List<MoneyUpdaterGameDetails> slotMoneyGD;
	
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
	public long getGameSlotTime() {
		return gameSlotTime;
	}
	public void setGameSlotTime(long gameSlotTime) {
		this.gameSlotTime = gameSlotTime;
	}
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
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
	public List<MoneyUpdaterGameDetails> getSlotMoneyGD() {
		return slotMoneyGD;
	}
	public void setSlotMoneyGD(List<MoneyUpdaterGameDetails> slotMoneyGD) {
		this.slotMoneyGD = slotMoneyGD;
	}
}
