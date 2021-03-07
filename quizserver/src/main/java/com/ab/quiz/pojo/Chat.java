package com.ab.quiz.pojo;

public class Chat {
	private long senderUserId;
	private String message;
	private String senderName;
	private long sentTimeStamp;
	private long gameStartTime;
	
	
	public long getSenderUserId() {
		return senderUserId;
	}
	public void setSenderUserId(long senderUserId) {
		this.senderUserId = senderUserId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public long getSentTimeStamp() {
		return sentTimeStamp;
	}
	public void setSentTimeStamp(long timeStamp) {
		this.sentTimeStamp = timeStamp;
	}
	public long getGameStartTime() {
		return gameStartTime;
	}
	public void setGameStartTime(long timeStamp) {
		this.gameStartTime = timeStamp;
	}
}
