package com.ab.quiz.pojo;

public class UserMoney {
	private long id;
	private long userProfileId;
	private long loadedAmount;
	private long winningAmount;
	private long referalAmount;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public long getLoadedAmount() {
		return loadedAmount;
	}
	public void setLoadedAmount(long loadedAmount) {
		this.loadedAmount = loadedAmount;
	}
	public long getWinningAmount() {
		return winningAmount;
	}
	public void setWinningAmount(long winningAmount) {
		this.winningAmount = winningAmount;
	}
	public long getReferalAmount() {
		return referalAmount;
	}
	public void setReferalAmount(long referalAmount) {
		this.referalAmount = referalAmount;
	}
	
	@Override
	public String toString() {
		return "UserMoney [id=" + id + ", userProfileId=" + userProfileId + ", loadedAmount=" + loadedAmount
				+ ", winningAmount=" + winningAmount + ", referalAmount=" + referalAmount + "]";
	}

}
