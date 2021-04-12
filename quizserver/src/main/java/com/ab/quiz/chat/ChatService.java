package com.ab.quiz.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.helper.LazyScheduler;
import com.ab.quiz.pojo.Chat;

public class ChatService implements Runnable {
	
	private static ChatService instance;
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private ArrayList<Chat> list = new ArrayList<>();
	private static final Logger logger = LogManager.getLogger(ChatService.class);
	private ScheduledFuture<?> chatSchedulerTask;
	
	private ChatService() {
	}
	
	public static ChatService getInstance() {
		if (instance == null) {
			instance = new ChatService();
			long delay = QuizConstants.DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS;
			instance.chatSchedulerTask = LazyScheduler.getInstance().submitRepeatedTask(instance, delay, 
					delay, TimeUnit.MILLISECONDS);
		}
		return instance;
	}
	
	public boolean post(Chat chatMsg) {
		lock.readLock().lock();
		boolean result = list.add(chatMsg);
		lock.readLock().unlock();
		return result;
	}
	
	public List<Chat> getMessages(long start, long end) {
		List<Chat> chatList = new ArrayList<>();
		lock.readLock().lock();
		//long currentTime = System.currentTimeMillis();
		
		
		Predicate<Chat> betweenStartEnd = msg -> (msg.getSentTimeStamp() >= start && msg.getSentTimeStamp() <= end);  
				//&& (msg.getGameStartTime() != -1 && currentTime < msg.getGameStartTime()));
		
		chatList = list.stream().filter(betweenStartEnd).collect(Collectors.toList());
		lock.readLock().unlock();
		logger.info("Hasini .....In get messages :{} and {} ", chatList.size(), list.size());
		return chatList;
	}
	
	public void run() {
		try {
			long currentTime = System.currentTimeMillis();
			logger.info("Repeated Task in Chat service at {}", new Date(currentTime));
			lock.writeLock().lock();
			Predicate<Chat> byMsgAge = msg -> (currentTime - msg.getSentTimeStamp()) >= QuizConstants.DELETE_OLD_MSGS_TIME_PERIOD_IN_MILLIS; 
			boolean anyMsgsRemoved = list.removeIf(byMsgAge);
			lock.writeLock().unlock();
			logger.info("Chat messages removed if any {}", anyMsgsRemoved);
		} catch(Exception ex) {
			logger.error("Exception in chat service repeated task", ex);
		}
	}
	
	public void shutDown() {
		if (instance.chatSchedulerTask != null) {
			instance.chatSchedulerTask.cancel(true);
		}
	}
}
