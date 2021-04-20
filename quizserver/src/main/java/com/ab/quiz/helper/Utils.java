package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.List;

import com.ab.quiz.pojo.MyTransaction;
import com.ab.quiz.pojo.PrizeDetail;

public class Utils {
	
	private static double[] twoWinners = {70.0, 30.0};
	private static double[] threeWinners = {60.0, 25.0, 15.0};
	private static double[] fourWinners = {50.0, 25.0, 15.0, 10.0};
	private static double[] fiveWinners = {45.0, 18.0, 15.0, 12.0, 10.0};
	
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
				addedBrainsShare = 10.0;
				break;
			}
			case 5: {
				winners = threeWinners;
				winnerCount = 3;
				addedBrainsShare = 15.0;
				break;
			}
			case 6: {
				winners = fourWinners;
				winnerCount = 4;
				addedBrainsShare = 15.0;
				break;
			}
			case 7:
			case 8:
			case 9:
			case 10: {
				winners = fiveWinners;
				winnerCount = 5;
				addedBrainsShare = 20.0;
				break;
			}
		}
		
		double totalAmount = ticketRate * playerCount;
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
			int amount, int transactionType, int accountType, long ob, long cb, String comments) {
		
		MyTransaction transaction = new MyTransaction();
		transaction.setUserId(userId);
		transaction.setDate(date);
		transaction.setAmount(amount);
		transaction.setAccountType(accountType);
		transaction.setTransactionType(transactionType);
		transaction.setOpeningBalance(ob);
		transaction.setClosingBalance(cb);
		transaction.setComment(comments);
		
		return transaction;
	}
	
	public static int getBossMoney(int profit) {
		return (profit/50);
	}
}
