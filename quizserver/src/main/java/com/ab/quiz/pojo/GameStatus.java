package com.ab.quiz.pojo;

import java.util.ArrayList;
import java.util.List;

public class GameStatus {
	long gameId;
	int gameStatus;
	int viewId;
	List<String> enrolledPlayerNames = new ArrayList<>();

	public int getViewId() {
		return viewId;
	}
	public void setViewId(int viewId) {
		this.viewId = viewId;
	}
 
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	public int getGameStatus() {
		return gameStatus;
	}
	public void setGameStatus(int gameStatus) {
		this.gameStatus = gameStatus;
	}
	public List<String> getEnrolledPlayerNames() {
		return enrolledPlayerNames;
	}
	public void setEnrolledPlayerNames(List<String> enrolledPlayerNames) {
		this.enrolledPlayerNames = enrolledPlayerNames;
	}
}
