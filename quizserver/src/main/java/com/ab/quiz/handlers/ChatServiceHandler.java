package com.ab.quiz.handlers;

import java.util.List;

import com.ab.quiz.chat.ChatService;
import com.ab.quiz.pojo.Chat;

public class ChatServiceHandler {
	
	ChatService chatService = ChatService.getInstance();
	private static ChatServiceHandler instance;
	
	private ChatServiceHandler() {
	}
	
	public static ChatServiceHandler getInstance() {
		if (instance == null) {
			instance = new ChatServiceHandler();
		}
		return instance;
	}
	
	public boolean postMessage(Chat chatMsg) {
		chatMsg.setSentTimeStamp(System.currentTimeMillis());
		return chatService.post(chatMsg);
	}
	
	public List<Chat> getMessages(long start, long end) {
		return chatService.getMessages(start, end);
	}
}
