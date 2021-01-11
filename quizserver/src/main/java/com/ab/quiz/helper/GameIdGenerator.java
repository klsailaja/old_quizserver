package com.ab.quiz.helper;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.LastGameIdDBHandler;

public class GameIdGenerator {
	
	private static final Logger logger = LogManager.getLogger(GameIdGenerator.class);
	private static GameIdGenerator instance = null;
	private long lastGameId;
	
	private GameIdGenerator() {
	}
	
	private void initialize() {
		logger.info("GameIdGenerator initialize is called ");
		try {
			long gameId = LastGameIdDBHandler.getInstance().getLastGameId();
			lastGameId = gameId;
		} catch (SQLException e) {
			logger.error("Exception while getting the last game id from DB:", e);
		}
		logger.info("GameIdGenerator initialize assigning {}", lastGameId);
	}
	
	public static GameIdGenerator getInstance() {
		if (instance == null) {
			logger.debug("In GameIdGenerator getInstance() method called");
			instance = new GameIdGenerator();
			instance.initialize();
		}
		return instance;
	}
	public long getNextGameId() {
		return ++lastGameId;
	}
}
