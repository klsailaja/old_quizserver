package com.ab.tool;

import java.security.SecureRandom;

public class Utils {
	
	private static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" 
			+ "abcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom secureRnd = new SecureRandom();
	
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
}
