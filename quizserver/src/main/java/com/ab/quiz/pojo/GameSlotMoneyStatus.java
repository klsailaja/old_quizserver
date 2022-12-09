package com.ab.quiz.pojo;

import java.util.List;

public class GameSlotMoneyStatus {
	private String trackKey;
	private String serverId;
	private long slotGameStartTime;
	
	private int operationType;
	private int moneyCreditedStatus;
	private List<Integer> uniqueIds;
	private List<Integer> dbResultsIds;
	
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public int getOperationType() {
		return operationType;
	}
	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}
	
	
	public String getTrackKey() {
		return trackKey;
	}
	public void setTrackKey(String trackKey) {
		this.trackKey = trackKey;
	}
	public long getSlotGameStartTime() {
		return slotGameStartTime;
	}
	public void setSlotGameStartTime(long slotGameStartTime) {
		this.slotGameStartTime = slotGameStartTime;
	}
	public int getMoneyCreditedStatus() {
		return moneyCreditedStatus;
	}
	public void setMoneyCreditedStatus(int moneyCreditedStatus) {
		this.moneyCreditedStatus = moneyCreditedStatus;
	}
	
	public void setUniqueIds(List<Integer> uniqueIds) {
		this.uniqueIds = uniqueIds;
	}
	public List<Integer> getUniqueIds() {
		return uniqueIds;
	}
	
	public void setDbResultsIds(List<Integer> dbResultsIds) {
		this.dbResultsIds = dbResultsIds;
	}
	public List<Integer> getDbResultsIds() {
		return dbResultsIds;
	}
}
