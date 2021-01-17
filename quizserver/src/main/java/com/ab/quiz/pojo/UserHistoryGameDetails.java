package com.ab.quiz.pojo;

import java.util.List;

public class UserHistoryGameDetails {
	
	private int total;
	private boolean isPrevEnabled;
	private boolean isNextEnabled;
	private List<GameResults> historyGames;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public boolean isPrevEnabled() {
		return isPrevEnabled;
	}
	public void setPrevEnabled(boolean isPrevEnabled) {
		this.isPrevEnabled = isPrevEnabled;
	}
	public boolean isNextEnabled() {
		return isNextEnabled;
	}
	public void setNextEnabled(boolean isNextEnabled) {
		this.isNextEnabled = isNextEnabled;
	}
	public List<GameResults> getHistoryGames() {
		return historyGames;
	}
	public void setHistoryGames(List<GameResults> historyGames) {
		this.historyGames = historyGames;
	}
}
