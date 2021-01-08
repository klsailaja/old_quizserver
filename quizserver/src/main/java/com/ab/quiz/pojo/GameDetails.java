package com.ab.quiz.pojo;

import java.util.List;

public class GameDetails {
	
	private long gameId; 
	private int ticketRate; 
	private int currentCount;
	private int maxCapacity = 10;
	private long startTime;
	private int gameType;
	private int status;
	private List<Question> gameQuestions;
	private Question flipQuestion;
	
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
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
	public int getMaxCapacity() {
		return maxCapacity;
	}
	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public int getGameType() {
		return gameType;
	}
	public void setGameType(int gameType) {
		this.gameType = gameType;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public List<Question> getGameQuestions() {
		return gameQuestions;
	}
	public void setGameQuestions(List<Question> gameQuestions) {
		this.gameQuestions = gameQuestions;
	}
	public Question getFlipQuestion() {
		return flipQuestion;
	}
	public void setFlipQuestion(Question question) {
		this.flipQuestion = question;
	}
	
	
	public String toString() {
		return getGameId() + "";
	}
}
