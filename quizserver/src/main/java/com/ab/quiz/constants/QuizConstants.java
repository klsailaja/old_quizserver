package com.ab.quiz.constants;

public class QuizConstants {
	
	public static final int SPECIAL_CODE_MAX_COUNT = 100;
	public static int MY_SERVER_ID = 1;
	
	public static final int MAX_LIVE_SLOTS = 4;
	
	//public static final int[] GAMES_RATES_IN_ONE_SLOT_MIXED = {0, 10, 10, 20, 20, 50, 50, 100, 100, 150, 150, 200, 200};
	public static final int[] GAMES_RATES_IN_ONE_SLOT_MIXED = {0, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100};
	//public static final int[] GAMES_RATES_IN_ONE_SLOT_MIXED = {0, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100, 10, 10, 20, 20, 50, 50, 75, 75, 100, 100};
	
	public static final int MAX_TOTAL_GAMES_MIXED = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_MIXED.length;
	
	//public static final int[] GAMES_RATES_IN_ONE_SLOT_SPECIAL = {0, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
	public static final int[] GAMES_RATES_IN_ONE_SLOT_SPECIAL = {0, 50, 100, 150, 200, 250, 50, 100, 150, 200, 250};
	//public static final int[] GAMES_RATES_IN_ONE_SLOT_SPECIAL = {50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250,50, 100, 150, 200, 250, 50, 100, 150, 200, 250};
	
	
	public static final int MAX_TOTAL_GAMES_SPECIAL = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_SPECIAL.length;
	
	//public static final long QUESTION_COUNT = 20;
	
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MINS = 10; // In minutes
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MILLIS = TIME_GAP_BETWEEN_SLOTS_IN_MINS * 60 * 1000;
	public static final int START_PAYMENTS_BEFORE_COMPLETION_TIME_OFFSET = 25 * 1000;
	 
	
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_SECS = 15; // In secs
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_MILLIS = GAME_BEFORE_LOCK_PERIOD_IN_SECS * 1000;
	public static final long GAP_BETWEEN_QUESTIONS = 60 * 1000;
	
	public static final int MAX_PLAYERS_PER_GAME = 10;
	
	public static int TESTMODE = 1;
	
	// Chat Related settings
	public static int DELETE_OLD_MSGS_TIME_PERIOD = 30;
	public static long DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS = DELETE_OLD_MSGS_TIME_PERIOD * 60 * 1000;
	
	public static int LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL = 30;
	public static long LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL_IN_MILLIS = LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL * 60 * 1000; 
	
}
