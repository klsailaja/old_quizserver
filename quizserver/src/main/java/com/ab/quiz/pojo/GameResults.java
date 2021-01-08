package com.ab.quiz.pojo;

public class GameResults {
	
	private long id;
	private long gameId;
	private long gamePlayedTime;
	private int ticketRate; 
	private int currentCount;
	private String WinnersList;
	
	//userName:rank:correctCount:totalTime:amountWon;
	
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
	public int getTicketRate() {
		return ticketRate;
	}
	public void setTicketRate(int ticketRate) {
		this.ticketRate = ticketRate;
	}
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public String getWinnersList() {
		return WinnersList;
	}
	public void setWinnersList(String winnersList) {
		WinnersList = winnersList;
	}
}
