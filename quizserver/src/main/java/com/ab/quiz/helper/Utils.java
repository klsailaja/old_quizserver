package com.ab.quiz.helper;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PrizeDetail;

public class Utils {
	
	private static double[] twoWinners = {60.0, 40.0};
	private static double[] threeWinners = {50.0, 30.0, 20.0};
	private static double[] fourWinners = {40.0, 30.0, 20.0, 10.0};
	private static double[] fiveWinners = {30.0, 25.0, 20.0, 15.0, 10.0};
	
	private static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
			+ "abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom secureRnd = new SecureRandom();
	
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
	private static final String timePattern = "dd/MMM/yyyy/HH/mm";
	
	private static final String GAME_START_TIME_KEY = "GAME_START_TIME";
	private static final String GAME_SERVER_ID = "GAME_SERVER_ID";
	private static final String GAME_CLIENT_ID = "GAME_CLIENT_ID";
	private static final String TKT_RATE = "TKT_RATE";
	private static final String AMOUNT = "AMOUNT";
	
	
	public static String getTransactionObjComments(GameDetails gameDetails) {
        simpleDateFormat.applyPattern(timePattern);
        
        StringBuffer strBuffer = new StringBuffer("GameId#:");
        strBuffer.append(gameDetails.getTempGameId());
        strBuffer.append(" on ");
        strBuffer.append(simpleDateFormat.format(new Date(gameDetails.getStartTime())));
        return strBuffer.toString();
	}
	
	public static List<PrizeDetail> getPrizeDetails(int ticketRate, int playerCount) {
		
		double addedBrainsShare = 10.0;
		int winnerCount = 2;
		double[] winners = null;
		
		switch (playerCount) {
			case 3:
			case 4:
			{
				winners = twoWinners;
				winnerCount = 2;
				addedBrainsShare = 4.0;
				break;
			}
			case 5: {
				winners = threeWinners;
				winnerCount = 3;
				addedBrainsShare = 5.0;
				break;
			}
			case 6: {
				winners = fourWinners;
				winnerCount = 4;
				addedBrainsShare = 10.0;
				break;
			}
			case 7:
			case 8:
			case 9:
			case 10: {
				winners = fiveWinners;
				winnerCount = 5;
				addedBrainsShare = 12.0;
				break;
			}
		}
		
		double totalAmount = ticketRate * playerCount;
		if (totalAmount <= 200.00) {
			addedBrainsShare = 10.0;
		} else if ((totalAmount > 200.00) && (totalAmount <= 500.00)) {
			addedBrainsShare = 10.0;
		} else {
			addedBrainsShare = 15.0;
		}
		double totalPrizeMoney = totalAmount - (totalAmount * addedBrainsShare)/100;
		totalPrizeMoney = totalPrizeMoney - winnerCount * ticketRate;
		
		List<PrizeDetail> prizeDetails = new ArrayList<>();
		
		for (int index = 1; index <= winnerCount; index ++) {
			
			PrizeDetail pd = new PrizeDetail();
			pd.setRank(index);
			
			double percentage = winners[index - 1];
			double winMoney = (totalPrizeMoney * percentage)/100;
			int winMoneyInt = (int) Math.round(winMoney);
			int prizeMoney = ticketRate + winMoneyInt; 
			pd.setPrizeMoney(prizeMoney);
			
			prizeDetails.add(pd);
		}
		return prizeDetails;
	}
	
	public static MyTransaction getTransactionPojo(long userId, long date,  
			int amount, int transactionType, int accountType, long ob, long cb, String comments, String transactionId) {
		
		MyTransaction transaction = new MyTransaction();
		transaction.setUserId(userId);
		transaction.setDate(date);
		transaction.setAmount(amount);
		transaction.setAccountType(accountType);
		transaction.setTransactionType(transactionType);
		transaction.setOpeningBalance(ob);
		transaction.setClosingBalance(cb);
		transaction.setComment(comments);
		transaction.setIsWin(0);
		if (transactionId == null) {
			StringBuilder sb = new StringBuilder(5); 
			for (int i = 0; i < 5; i++) { 
				sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
			}
			transactionId = sb.toString(); 
		}
		transaction.setTransactionId(transactionId);
		return transaction;
	}
	
	public static int getBossMoney(int profit) {
		return (profit/25);
	}
	
	public static String fillExtraDetailsForTransactions(long gameStartTime, long serverId,
			int clientId, int tktRate, int amount) {
		
		HashMap<String,String> extraValues = new HashMap<>();
		
		simpleDateFormat.applyPattern(timePattern);
		String dateStr = simpleDateFormat.format(new Date(gameStartTime));
		
		extraValues.put(GAME_START_TIME_KEY, dateStr);
		extraValues.put(GAME_SERVER_ID, String.valueOf(serverId));
		extraValues.put(GAME_CLIENT_ID, String.valueOf(clientId));
		extraValues.put(TKT_RATE, String.valueOf(tktRate));
		extraValues.put(AMOUNT, String.valueOf(amount));
		
		return encodeCCExtraValues(extraValues);
	}
	
	public static String encodeCCExtraValues(HashMap<String,String> extraValues) {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> mapEntry : extraValues.entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(value);
            stringBuilder.append(";");
        }
        return stringBuilder.toString();
    }

    public static HashMap<String,String> decodeCCExtraValues(String extraValues) {
        extraValues = extraValues.trim();
        StringTokenizer firstTokenizer = new StringTokenizer(extraValues, ";");
        HashMap<String, String> extraValuesMap = new HashMap<>();
        while (firstTokenizer.hasMoreTokens()) {
            StringTokenizer secondTokenizer = new StringTokenizer(firstTokenizer.nextToken(), "=");
            String key = secondTokenizer.nextToken();
            String value = secondTokenizer.nextToken();
            extraValuesMap.put(key, value);
       }
       return extraValuesMap;
    }
	
	public static void main(String[] args) {
		int[] rates = {20, 50, 50, 75, 75, 50, 50, 75, 100, 125};
		int[] players = {3,4,5,6,7,8,9,10};
		int totalOurShare = 0;
		int avgShare = 0;
		for (int i = 0; i < rates.length; i++) {
			avgShare = 0;
			for (int j = 0; j < players.length; j++) {
				System.out.println("For Rate : " + rates[i] + " Player Count :" + players[j]);
				int total = rates[i] * players[j];
				System.out.println("Total budget :" + total);
				List<PrizeDetail> details = Utils.getPrizeDetails(rates[i], players[j]);
				System.out.println("Total Winners :" + details.size());
				System.out.println(details);
				int playerMoney = 0;
				for (PrizeDetail pd : details) {
					playerMoney = playerMoney + pd.getPrizeMoney();
				}
				int ourSharePerGame = (total - playerMoney);
				avgShare = avgShare + ourSharePerGame;
				System.out.println("Our share :" + (total - playerMoney));
				System.out.println();
			}
			avgShare = avgShare / players.length;
			totalOurShare = totalOurShare + avgShare;
		}
		System.out.println("Total share is " + (totalOurShare));
		long totalAmt = (totalOurShare * 2 * 288);
		System.out.println("totalAmt :" + totalAmt);
		
		long totalLanguages = 5;
		long totalModes = 4;
		long totalTimeCovers = 1;
		long totalServers = 20;
		long finalTotalPerDay = totalAmt * totalModes * totalTimeCovers * totalServers * totalLanguages;    
		System.out.println("totalAmt finally:" + finalTotalPerDay);
		
		
		/*System.out.println(getBossMoney(55));
		System.out.println(getBossMoney(102));
		//
		int[] rates = {10,20,50,75,100,50,100,150,200,250};
		int[] players = {3,4,5,6,7,8,9,10};
		int totalOurShare = 0;
		int avgShare = 0;
		
		for (int rate : rates) {
			for (int ct : players) {
				List<PrizeDetail> prizes = Utils.getPrizeDetails(rate, ct);
				System.out.println("For " + rate + ":" + ct);
				int playersPrizeMoney = 0;
				for (PrizeDetail pd : prizes) {
					System.out.println(pd);
					playersPrizeMoney = playersPrizeMoney + pd.getPrizeMoney();
				}
				int ourSharePerGame = ((rate * ct) - playersPrizeMoney);
				avgShare = avgShare + ourSharePerGame; 
				System.out.println("Our Share is " + ((rate * ct) - playersPrizeMoney));
			}
			avgShare = avgShare / players.length;
			totalOurShare = totalOurShare + avgShare; 
		}
		System.out.println("Total share is " + (totalOurShare));
		long totalAmt = (totalOurShare * 2 * 144);
		System.out.println("totalAmt :" + totalAmt);*/
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("E");
		String dayOfWeek = dateFormat.format(c.getTime()).toLowerCase();
		System.out.println(dayOfWeek);
	}
}
