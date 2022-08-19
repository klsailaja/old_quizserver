package com.ab.tool;

public class Request {
	public static String baseUri = "http://192.168.1.5:8888";
	
	public static final int WD_OPERATION = 700;
	
	public static PostTask<WithdrawMoney, Boolean> performWitdrawTask() {
		String uri = baseUri + "/money/wd";
		return new PostTask<>(uri, WD_OPERATION, null, Boolean.class);
	}
}
