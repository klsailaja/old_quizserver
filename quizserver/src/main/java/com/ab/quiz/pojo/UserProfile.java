package com.ab.quiz.pojo;

public class UserProfile {
	
	private long id;
	private String emailAddress;
	private String password_hash;
	private String name;
	private String myReferalId;
	private String bossReferredId;
	private long createdDate;
	private long lastLoggedDate;
	private String bossName;
	private long bossId;
	private int isLoggedIn;
	private int forgotPasswdUsed;
	
	public int getIsLoggedIn() {
		return isLoggedIn;
	}
	public void setIsLoggedIn(int isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	public int getForgotPasswdUsed() {
		return forgotPasswdUsed;
	}
	public void setForgotPasswdUsed(int forgotPasswdUsed) {
		this.forgotPasswdUsed = forgotPasswdUsed;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getPasswordHash() {
		return password_hash;
	}
	public void setPasswordHash(String password_hash) { 
		this.password_hash = password_hash;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMyReferalId() {
		return myReferalId;
	}
	public void setMyReferalId(String referalId) {
		this.myReferalId = referalId;
	}
	public String getBossReferredId() {
		return bossReferredId;
	}
	public void setBossReferredId(String referredUserId) {
		this.bossReferredId = referredUserId;
	}
	public long getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}
	public long getLastLoggedDate() {
		return lastLoggedDate;
	}
	public void setLastLoggedTime(long lastLoggedDate) {
		this.lastLoggedDate = lastLoggedDate;
	}
	
	public String getBossName() {
		return bossName;
	}
	public void setBossName(String bossName) {
		this.bossName = bossName;
	}
	
	public long getBossId() {
		return bossId;
	}
	public void setBossId(long bossId) {
		this.bossId = bossId;
	}
	@Override
	public String toString() {
		return "UserProfile [id=" + id + ", emailAddress=" + emailAddress + ", name=" + name + ", referalId=" + myReferalId
				+ ", referredId=" + bossReferredId + ", createdDate=" + createdDate + ", lastLoggedDate="
				+ lastLoggedDate + "]";
	}
}
