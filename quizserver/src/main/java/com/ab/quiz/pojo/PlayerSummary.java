package com.ab.quiz.pojo;

public class PlayerSummary {
	private long userProfileId;
	private String userName;
	private int rank = 0;
	private long totalTime;
	private int correctCount;
	private int accountUsed;
	private int amountWon;
	
	public int getCorrectCount() {
		return correctCount;
	}
	
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	public int getAccountUsed() {
		return accountUsed;
	}
	public void setAccountUsed(int accountUsed) {
		this.accountUsed = accountUsed;
	}

	public int getAmountWon() {
		return amountWon;
	}

	public void setAmountWon(int amountWon) {
		this.amountWon = amountWon;
	}

	public void setCorrectCount(int correctCount) {
		this.correctCount = correctCount;
	}

	@Override
	public String toString() {
		return "PlayerSummary [userProfileId=" + userProfileId + ", userName=" + userName + ", rank=" + rank
				+ ", totalTime=" + totalTime + ", correctCount=" + correctCount + ", amountWon=" + amountWon + "]";
	}
	
}
