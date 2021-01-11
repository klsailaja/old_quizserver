package com.ab.quiz.constants;

public class QuizConstants {
	
	public static final int MAX_LIVE_SLOTS = 5;
	
	public static final int[] GAMES_RATES_IN_ONE_SLOT = {0, 10 , 20, 50, 75, 100};
	
	public static final int MAX_TOTAL_GAMES = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT.length;
	
	public static final long QUESTION_COUNT = 20;
	
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MINS = 10; // In minutes
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MILLIS = TIME_GAP_BETWEEN_SLOTS_IN_MINS * 60 * 1000;
	
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_SECS = 10; // In secs
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_MILLIS = GAME_BEFORE_LOCK_PERIOD_IN_SECS * 1000;
	
	public static final long GAP_BETWEEN_QUESTIONS = 30 * 1000;
	
	public static final int MAX_PLAYERS_PER_GAME = 10;
	
	public static int TESTMODE = 0;
}
