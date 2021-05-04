package com.ab.quiz.questions;

import java.util.Calendar;
import java.util.Date;

class Test {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		startTime = startTime + 5 * 60 * 1000;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);

		int minute = calendar.get(Calendar.MINUTE);
		System.out.println("minute " + minute);
		minute = minute / 5;
	
		System.out.println("minute " + minute);
		if ((minute % 2) == 0) { minute++; }
		
		 
		minute = minute * 5;
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		startTime = calendar.getTimeInMillis();
		
		System.out.println(new Date(startTime));
		
		long milliseconds = 3500100;
		 
        // formula for conversion for
        // milliseconds to minutes.
        long minutes = (milliseconds / 1000) / 60;
 
        // formula for conversion for
        // milliseconds to seconds
        long seconds = (milliseconds / 1000) % 60;
        
        long millis = milliseconds - (60 * 1000 * minutes) - (seconds * 1000);
        
 
        // Print the output
        System.out.println(milliseconds + " Milliseconds = "
                           + minutes + " minutes and "
                           + seconds + " seconds."
                           + millis + " millis.");

	}
}
