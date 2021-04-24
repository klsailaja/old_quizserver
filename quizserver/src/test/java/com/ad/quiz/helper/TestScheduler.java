package com.ad.quiz.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestScheduler {
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(250);
	private static TestScheduler instance = null;
	
	private TestScheduler() {
	}
	
	public static TestScheduler getInstance() {
		if (instance == null) {
			instance = new TestScheduler();
		}
		return instance;
	}
	
	public void submit(Runnable run) {
		scheduler.schedule(run, 0, TimeUnit.MILLISECONDS);
	}
}
