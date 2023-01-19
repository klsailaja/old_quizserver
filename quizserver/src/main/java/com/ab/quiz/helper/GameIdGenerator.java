package com.ab.quiz.helper;

import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.LastGameIdDBHandler;

public class GameIdGenerator {
	
	private static final Logger logger = LogManager.getLogger(GameIdGenerator.class);
	private static GameIdGenerator instance = null;
	private long lastGameId;
	private int tempGameId;
	private int moneyUpdateRequestId = 0;
	
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
	
	public int getTempGameId() {
		if (tempGameId >= 10000) {
			tempGameId = 0;
		}
		++tempGameId;
		return tempGameId;
	}
	
	public int getMoneyUpdateReuestId() {
		if (moneyUpdateRequestId >= 100) {
			moneyUpdateRequestId = 0;
		}
		++moneyUpdateRequestId;
		return moneyUpdateRequestId;
	}
}
