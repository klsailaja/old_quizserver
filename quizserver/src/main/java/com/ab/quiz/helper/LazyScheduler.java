package com.ab.quiz.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LazyScheduler {
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
	private static LazyScheduler instance = null;
	
	private LazyScheduler() {
	}
	
	public static LazyScheduler getInstance() {
		if (instance == null) {
			instance = new LazyScheduler();
		}
		return instance;
	}
	
	public void submit(Runnable run) {
		scheduler.schedule(run, 100, TimeUnit.MILLISECONDS);
	}
	
	public void submit(Runnable run, long delay, TimeUnit timeUnit) {
		scheduler.schedule(run, delay, timeUnit);
	}
	
	public void submitRepeatedTask(Runnable run, long initialDelay, long delay, TimeUnit unit) {
		scheduler.scheduleWithFixedDelay(run, initialDelay, delay, unit);
	}
	
	public void shutDown() {
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(60 * 1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
