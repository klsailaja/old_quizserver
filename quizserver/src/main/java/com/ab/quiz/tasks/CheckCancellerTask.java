package com.ab.quiz.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.handlers.GameManager;

public class CheckCancellerTask implements Runnable {
	
	private int mode;
	private static final Logger logger = LogManager.getLogger(CheckCancellerTask.class);
	
	public CheckCancellerTask(int mode) {
		this.mode = mode;
	}
	
	public void run() {
		logger.info("This is in GameCheckCancellerTask {}", mode);
		try {
			GameManager.getInstance().getAllGamesStatus(mode);
		} catch (Exception ex) {
			logger.error("Exception in GameCancellerTask", ex);
		}
	}
}
