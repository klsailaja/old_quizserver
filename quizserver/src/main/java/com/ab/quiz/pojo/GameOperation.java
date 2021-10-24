package com.ab.quiz.pojo;

public class GameOperation {
	private long userProfileId;
	private int userAccountType;
	private String userName;
	private long userBossId;
	
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public int getUserAccountType() {
		return userAccountType;
	}
	public void setUserAccountType(int userAccountType) {
		this.userAccountType = userAccountType;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public long getUserBossId() {
		return userBossId;
	}
	public void setUserBossId(long userBossId) {
		this.userBossId = userBossId;
	}
}
