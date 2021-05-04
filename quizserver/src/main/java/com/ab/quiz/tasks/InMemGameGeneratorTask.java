package com.ab.quiz.tasks;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.helper.GamesGenerator;

public class InMemGameGeneratorTask implements Runnable {
	
	private GamesGenerator gamesGenerator;
	private static final Logger logger = LogManager.getLogger(InMemGameGeneratorTask.class);
	
	public InMemGameGeneratorTask(GamesGenerator gamesGenerator) {
		this.gamesGenerator = gamesGenerator;
	}
	
	public void run() {
		logger.info("This is in GamesGenerator InMemGameGeneratorTask");
		long startTime = System.currentTimeMillis();
		try {
			List<GameHandler> inMemGames = gamesGenerator.generateGameData(1);
			gamesGenerator.addNextGameSet(inMemGames);
			logger.info("Time to prepare the GamesGenerator in-mem games in secs {}", (System.currentTimeMillis() - startTime)/1000);
		} catch (SQLException e) {
			logger.error("SQLException in InMem Game Generator", e);
		}
	}
}
