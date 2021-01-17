package com.ab.quiz.pojo;

public class GameResults {
	
	private int sNo;
	private long gameId;
	private long gamePlayedTime;
	private String celebrityName;
	private int tktRate;
	private String WinnersList;
	
	//userName:rank:correctCount:totalTime:amountWon;
	
	public int getsNo() {
		return sNo;
	}
	public void setsNo(int sNo) {
		this.sNo = sNo;
	}
	
	public int getTktRate() {
		return tktRate;
	}
	public void setTktRate(int tktRate) {
		this.tktRate = tktRate;
	}
	
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	public long getGamePlayedTime() {
		return gamePlayedTime;
	}
	public void setGamePlayedTime(long gamePlayedTime) {
		this.gamePlayedTime = gamePlayedTime;
	}
	public String getCelebrityName() {
		return celebrityName;
	}
	public void setCelebrityName(String celebrityName) {
		this.celebrityName = celebrityName;
	}
	public String getWinnersList() {
		return WinnersList;
	}
	public void setWinnersList(String winnersList) {
		WinnersList = winnersList;
	}
}
