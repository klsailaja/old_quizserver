package com.ab.quiz.pojo;

public class MoneyUpdaterGameDetails {
	private long gameServerId;
	private int gameClientId;
	private long userId;
	private int amount;
	private int uniqueId;
	private int creditResult;
	
	public long getGameServerId() {
		return gameServerId;
	}
	public void setGameServerId(long gameServerId) {
		this.gameServerId = gameServerId;
	}
	public int getCreditResult() {
		return creditResult;
	}
	public void setCreditResult(int creditResult) {
		this.creditResult = creditResult;
	}
	public int getGameClientId() {
		return gameClientId;
	}
	public void setGameClientId(int gameClientId) {
		this.gameClientId = gameClientId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}
}
