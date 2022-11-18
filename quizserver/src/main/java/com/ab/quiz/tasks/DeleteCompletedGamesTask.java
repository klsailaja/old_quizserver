package com.ab.quiz.tasks;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.handlers.GameManager;

public class DeleteCompletedGamesTask implements Runnable {
	
	private List<Long> completedGameIds;
	private static final Logger logger = LogManager.getLogger(DeleteCompletedGamesTask.class);
	
	public DeleteCompletedGamesTask(List<Long> completedGameIds) {
		this.completedGameIds = completedGameIds;
	}
	
	public void run() {
		logger.info("{} In DeleteCompletedGamesTask", TAGS.DELETE_COMPLETED_GAMES);
		GameManager.getInstance().deleteCompletedGames(completedGameIds, TAGS.DELETE_COMPLETED_GAMES);
	}
}
