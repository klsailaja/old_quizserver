package com.ab.quiz.pojo;

public class CancelGameRefundStatus {
	private long uid;
	private int clientGameId;
	private int status;
	
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	public int getClientGameId() {
		return clientGameId;
	}
	public void setClientGameId(int clientGameId) {
		this.clientGameId = clientGameId;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
