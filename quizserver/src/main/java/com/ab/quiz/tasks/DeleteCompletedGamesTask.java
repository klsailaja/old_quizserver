package com.ab.quiz.tasks;

import java.util.List;

import com.ab.quiz.handlers.GameManager;

public class DeleteCompletedGamesTask implements Runnable {
	
	private List<Long> completedGameIds;
	
	public DeleteCompletedGamesTask(List<Long> completedGameIds) {
		this.completedGameIds = completedGameIds;
	}
	
	public void run() {
		GameManager.getInstance().deleteCompletedGames(completedGameIds);
	}
}
