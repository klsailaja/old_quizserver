package com.ab.quiz.constants;

public class QuizConstants {
	
	public static final String ERROR_PREFIX_START = "ERROR_START:***********************";
	
	public static final String ERROR_PREFIX_END = "ERROR_END:***********************";
	
	public static final String FROM_MAIL_ID = "satyahasini25@gmail.com";
	
	public static final String VERIFY_MAIL_ID_SUBJECT = "4-digit Money Withdrawal Code";
	
	public static final String VERIFY_MAIL_ID_SENDER_NAME = "TeluguQuiz";
	
	public static final String VERIFY_MAIL_ID_BODY = "Your 4-digit verification code : %s";
	
	public static final String VERIFY_MAIL_CONTENTS = "<html><p>Your 4-digit withdrawal code : <b>%s</b>" 
				+ "<br><br>Thanks<br>"
				+ VERIFY_MAIL_ID_SENDER_NAME
				+ "</p></html>";

	
	public static final int SPECIAL_CODE_MAX_COUNT = 100;
	public static int MY_SERVER_ID = 1;
	
	public static final int MAX_LIVE_SLOTS = 4;
	
	public static final int[] GAMES_RATES_IN_ONE_SLOT_MIXED = {0, 20, 20, 50, 50, 50, 50, 75, 75, 75, 75};
	
	public static final int MAX_TOTAL_GAMES_MIXED = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_MIXED.length;
	
	public static final int[] GAMES_RATES_IN_ONE_SLOT_SPECIAL = {0, 50, 75, 100, 125, 150, 50, 75, 100, 125, 150};
	
	
	public static final int MAX_TOTAL_GAMES_SPECIAL = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_SPECIAL.length;
	
	//public static final long QUESTION_COUNT = 20;
	
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MINS = 10; // In minutes
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MILLIS = TIME_GAP_BETWEEN_SLOTS_IN_MINS * 60 * 1000;
	public static final int START_PAYMENTS_BEFORE_COMPLETION_TIME_OFFSET = 25 * 1000;
	 
	
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_SECS = 15; // In secs
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_MILLIS = GAME_BEFORE_LOCK_PERIOD_IN_SECS * 1000;
	public static final long GAP_BETWEEN_QUESTIONS = 60 * 1000;
	
	public static final int MAX_PLAYERS_PER_GAME = 10;
	public static final int MAX_QUESTIONS_PER_GAME = 10;
	
	public static int TESTMODE = 1;
	
	// Chat Related settings
	public static int DELETE_OLD_MSGS_TIME_PERIOD = 30;
	public static long DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS = DELETE_OLD_MSGS_TIME_PERIOD * 60 * 1000;
	
	public static int LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL = 30;
	public static long LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL_IN_MILLIS = LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL * 60 * 1000;
	

	public static int MIXED_MODE_QUESTION_DIFFICULTY_LEVEL = 2; // 2 - means easy
	public static int CELEBRITY_MODE_QUESTION_DIFFICULTY_LEVEL = 1; // 2 - means easy 1 - difficult and old movie
	public static int APP_SERVER_MODE = 2; // 1 - 8 Text and 2 Pics, 2 - 5 Text and 5 Pics, 3- 10 text and 0 pics
	
	public static int MIX_MODE_TEXT_QUESTIONS_COUNT = -1;
	public static int MIX_MODE_PICS_QUESTIONS_COUNT = -1;
	
	public static int CEEBRITY_MODE_TEXT_QUESTIONS_COUNT = -1;
	public static int CEEBRITY_MODE_PICS_QUESTIONS_COUNT = -1;
	
	public static void initialize() {
		if (APP_SERVER_MODE == 1) {
			MIX_MODE_TEXT_QUESTIONS_COUNT = 8;
			CEEBRITY_MODE_TEXT_QUESTIONS_COUNT = 8;
		} else if (APP_SERVER_MODE == 2) {
			MIX_MODE_TEXT_QUESTIONS_COUNT = 4;
			CEEBRITY_MODE_TEXT_QUESTIONS_COUNT = 4;
		} else if (APP_SERVER_MODE == 3) {
			MIX_MODE_TEXT_QUESTIONS_COUNT = 10;
			CEEBRITY_MODE_TEXT_QUESTIONS_COUNT = 10;
		}
		MIX_MODE_PICS_QUESTIONS_COUNT = MAX_QUESTIONS_PER_GAME - MIX_MODE_TEXT_QUESTIONS_COUNT;
		CEEBRITY_MODE_PICS_QUESTIONS_COUNT = MAX_QUESTIONS_PER_GAME - CEEBRITY_MODE_TEXT_QUESTIONS_COUNT;
	}
}
