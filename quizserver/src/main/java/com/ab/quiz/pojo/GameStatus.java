package com.ab.quiz.pojo;

import java.util.Map;

public class GameStatus {
	long gameId;
	int currentCount;
	int gameStatus;
	int viewId;
	Map <Long, Boolean> userAccountRevertStatus;
	
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
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public int getGameStatus() {
		return gameStatus;
	}
	public void setGameStatus(int gameStatus) {
		this.gameStatus = gameStatus;
	}
	public Map<Long, Boolean> getUserAccountRevertStatus() {
		return userAccountRevertStatus;
	}
	public void setUserAccountRevertStatus(Map<Long, Boolean> userAccountRevertStatus) {
		this.userAccountRevertStatus = userAccountRevertStatus;
	}
}
