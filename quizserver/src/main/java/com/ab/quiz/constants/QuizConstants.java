package com.ab.quiz.constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.ab.quiz.exceptions.NotAllowedException;

public class QuizConstants {
	
	public static final String ERROR_PREFIX_START = "ERROR_START:***********************";
	
	public static final String ERROR_PREFIX_END = "ERROR_END:***********************";
	
	public static final int MAX_LIVE_SLOTS = 4;
	
	public static final int[] GAMES_RATES_IN_ONE_SLOT_MIXED = {0, 20, 20, 50, 50, 50, 50, 75, 75, 75, 75};
	
	public static final int MAX_TOTAL_GAMES_MIXED = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_MIXED.length;
	
	// There is a copy of this var in core server too. Please update there too.
	public static final int[] GAMES_RATES_IN_ONE_SLOT_SPECIAL = {0, 50, 50, 75, 100, 125, 50, 50, 75, 100, 125};
	
	public static final int MAX_TOTAL_GAMES_SPECIAL = MAX_LIVE_SLOTS * GAMES_RATES_IN_ONE_SLOT_SPECIAL.length;
	
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MINS = 10; // In minutes
	
	public static final int TIME_GAP_BETWEEN_SLOTS_IN_MILLIS = TIME_GAP_BETWEEN_SLOTS_IN_MINS * 60 * 1000;
	
	public static final int START_PAYMENTS_BEFORE_COMPLETION_TIME_OFFSET = 25 * 1000;
	 
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_SECS = 15; // In secs
	
	public static final long GAME_BEFORE_LOCK_PERIOD_IN_MILLIS = GAME_BEFORE_LOCK_PERIOD_IN_SECS * 1000;
	
	public static final long GAP_BETWEEN_QUESTIONS = 60 * 1000;
	
	public static final int MAX_PLAYERS_PER_GAME = 10;
	
	public static final int MAX_QUESTIONS_PER_GAME = 10;
	
	private static final String OS_ROOT = "D:";
	
	private static final String QUIZ_SERVER_PROP_FILE = OS_ROOT + File.separator + "QuizHome" 
			+ File.separator + "Conf" + File.separator + "QuizServer.prop";
	
	public static int MY_SERVER_ID;
	
	public static int CLOSED_WD_RECEIPTS_DURATION;
	
	public static int GAMES_HISTORY_DURATION;
	
	public static String FROM_MAIL_ID;
	
	public static String VERIFY_MAIL_ID_SUBJECT;
	
	public static String VERIFY_MAIL_ID_SENDER_NAME;
	
	public static String VERIFY_MAIL_ID_BODY;
	
	public static String VERIFY_MAIL_CONTENTS;

	public static int SERVER_CLIENT_TIME_DIFF;
	
	public static int SERVER_CLIENT_TIME_DIFF_IN_SECS;
	
	public static int TESTMODE;
	
	// Chat Related settings
	public static int DELETE_OLD_MSGS_TIME_PERIOD;
	
	public static long DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS;
	
	public static int LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL;
	
	public static long LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL_IN_MILLIS;
	

	public static int MIXED_MODE_QUESTION_DIFFICULTY_LEVEL = 2; // 2 - means easy
	
	public static int CELEBRITY_MODE_QUESTION_DIFFICULTY_LEVEL = 1; // 2 - means easy 1 - difficult and old movie
	
	public static int APP_SERVER_MODE = 2; // 1 - 8 Text and 2 Pics, 2 - 5 Text and 5 Pics, 3- 10 text and 0 pics
	
	public static int MIX_MODE_TEXT_QUESTIONS_COUNT = -1;
	
	public static int MIX_MODE_PICS_QUESTIONS_COUNT = -1;
	
	public static int CEEBRITY_MODE_TEXT_QUESTIONS_COUNT = -1;
	
	public static int CEEBRITY_MODE_PICS_QUESTIONS_COUNT = -1;
	
	private static boolean isMoneyMode;
	
	public static int COIN_BUY_RATE = -1;
	public static int COIN_SELL_RATE = -1;
	
	public static String DATABASE_NAME = null;
	public static long TEST_MIXTYPE_START_UID = -1;
	public static long TEST_SPECIAL_START_UID = -1;
	
	public static void initialize() {
		
		try {
			FileInputStream reader = new FileInputStream(QUIZ_SERVER_PROP_FILE);
			Properties props = new Properties();
			props.load(reader);
			
			String value = props.getProperty("MY_SERVER_ID", "1");
			MY_SERVER_ID = Integer.parseInt(value);
			
			value = props.getProperty("DB_NAME", "");
			DATABASE_NAME = value;
			
			value = props.getProperty("TEST_MIXTYPE_START_UID", "-1");
			TEST_MIXTYPE_START_UID = Long.parseLong(value);
			
			value = props.getProperty("TEST_SPECIAL_START_UID", "-1");
			TEST_SPECIAL_START_UID = Long.parseLong(value);
			
			value = props.getProperty("CLOSED_WD_RECEIPTS_DURATION", "10");
			CLOSED_WD_RECEIPTS_DURATION = Integer.parseInt(value);
			
			value = props.getProperty("GAMES_HISTORY_DURATION", "10");
			GAMES_HISTORY_DURATION = Integer.parseInt(value);
			
			value = props.getProperty("SERVER_CLIENT_TIME_DIFF", "15");
			SERVER_CLIENT_TIME_DIFF = Integer.parseInt(value);
			SERVER_CLIENT_TIME_DIFF_IN_SECS = SERVER_CLIENT_TIME_DIFF * 1000;
			
			value = props.getProperty("TESTMODE", "1");
			TESTMODE = Integer.parseInt(value);
			
			value = props.getProperty("DELETE_OLD_MSGS_TIME_PERIOD", "15");
			DELETE_OLD_MSGS_TIME_PERIOD = Integer.parseInt(value);
			DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS = DELETE_OLD_MSGS_TIME_PERIOD * 60 * 1000;
			
			value = props.getProperty("LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL", "30");
			LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL = Integer.parseInt(value);
			LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL_IN_MILLIS = LOGGED_IN_USERS_COUNT_UPDATE_TIME_INTERVAL * 60 * 1000;
			
			FROM_MAIL_ID = props.getProperty("FROM_MAIL_ID", "satyahasini25@gmail.com");
			VERIFY_MAIL_ID_SUBJECT = props.getProperty("VERIFY_MAIL_ID_SUBJECT", "4-digit Verification Code");
			VERIFY_MAIL_ID_SENDER_NAME = props.getProperty("VERIFY_MAIL_ID_SENDER_NAME", "TeluguQuiz");
			VERIFY_MAIL_ID_BODY = props.getProperty("VERIFY_MAIL_ID_BODY", "Your 4-digit verification code : %s");
			VERIFY_MAIL_CONTENTS = "<html><p>Your 4-digit verification code : <b>%s</b>" 
					+ "<br><br>Thanks<br>"
					+ VERIFY_MAIL_ID_SENDER_NAME
					+ "</p></html>";
			
			value = props.getProperty("APP_SERVER_MODE", "2");
			APP_SERVER_MODE = Integer.parseInt(value);
			
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean backend_server_down = false;
	
	public static void setBackServerStatus(boolean status) {
		backend_server_down = status;
	}
	public static boolean getBackServerStatus() {
		return backend_server_down;
	}
	
	public static void setMoneyMode(boolean moneyMode) {
		isMoneyMode = moneyMode;
	}
	public static boolean getMoneyMode() {
		return isMoneyMode;
	}
	
	public static void setCoreServerConfig(Properties coreServerProps) {
		String TESTMODE_KEY = "TESTMODE";
		String MONEY_MODE_KEY = "MONEY_MODE";
		String COIN_BUY_RATE_KEY = "COIN_BUY_RATE";
		String COIN_SELL_RATE_KEY = "COIN_SELL_RATE";
		
		String value = coreServerProps.getProperty(MONEY_MODE_KEY);
		if (value == null) {
			throw new NotAllowedException("MONEY_MODE value missing from Core Server");
		}
		isMoneyMode = value.equals("1");
		
		value = coreServerProps.getProperty(TESTMODE_KEY);
		if (value == null) {
			throw new NotAllowedException("TESTMODE value missing from Core Server");
		}
		TESTMODE = Integer.parseInt(value);
		
		value = coreServerProps.getProperty(COIN_BUY_RATE_KEY);
		if (value == null) {
			throw new NotAllowedException("COIN_BUY_RATE value missing from Core Server");
		}
		COIN_BUY_RATE = Integer.parseInt(value);
		
		value = coreServerProps.getProperty(COIN_SELL_RATE_KEY);
		if (value == null) {
			throw new NotAllowedException("COIN_SELL_RATE value missing from Core Server");
		}
		COIN_SELL_RATE = Integer.parseInt(value);
	}
}
