package com.ab.quiz.pojo;

public class KYCEntry {
	private long userId;
	private long afpId;
	private long abpId;
	private long ppId;
	private long lastUpdatedTime;
	private String status;
	
	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}
	public void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getAfpId() {
		return afpId;
	}
	public void setAfpId(long afpId) {
		this.afpId = afpId;
	}
	public long getAbpId() {
		return abpId;
	}
	public void setAbpId(long abpId) {
		this.abpId = abpId;
	}
	public long getPpId() {
		return ppId;
	}
	public void setPpId(long ppId) {
		this.ppId = ppId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
