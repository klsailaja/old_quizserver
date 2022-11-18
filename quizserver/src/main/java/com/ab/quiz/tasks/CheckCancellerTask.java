package com.ab.quiz.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.handlers.GameManager;

public class CheckCancellerTask implements Runnable {
	
	private int mode;
	private static final Logger logger = LogManager.getLogger(CheckCancellerTask.class);
	private String tag = TAGS.CANCEL_GAMES;
	
	public CheckCancellerTask(int mode) {
		this.mode = mode;
	}
	
	public void run() {
		logger.info("{} This is in GameCheckCancellerTask {}", tag, mode);
		try {
			GameManager.getInstance().cancelGames(mode);
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in GameCancellerTask", tag, ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
}
