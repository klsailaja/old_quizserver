package com.ab.quiz.pojo;

import java.util.Date;

public class GameResults {
	
	private long id;
	private long gameId;
	private long gamePlayedTime;
	private String celabrityName;
	private int tktRate;
	private String WinnersList;
	
	//userName:rank:correctCount:totalTime:amountWon;
	
	public int getTktRate() {
		return tktRate;
	}
	public void setTktRate(int tktRate) {
		this.tktRate = tktRate;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public String getCelabrityName() {
		return celabrityName;
	}
	public void setCelabrityName(String celabrityName) {
		this.celabrityName = celabrityName;
	}
	public String getWinnersList() {
		return WinnersList;
	}
	public void setWinnersList(String winnersList) {
		WinnersList = winnersList;
	}
	
	public String getGameTime() {
		return new Date(gamePlayedTime).toString();
	}
	@Override
	public String toString() {
		return "GameResults [id=" + id + ", gameId=" + gameId + ", gamePlayedTime=" + gamePlayedTime + ", celabrityName="
				+ celabrityName + ", WinnersList=" + WinnersList + "]";
	}
	
	
}
