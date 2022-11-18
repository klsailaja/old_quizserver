package com.ab.quiz;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ab.quiz.handlers.ChatServiceHandler;
import com.ab.quiz.pojo.Chat;

@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {
	private static final Logger logger = LogManager.getLogger(ChatController.class);
	
	@RequestMapping(value = "/{start}/{end}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<Chat> getChatMessages(@PathVariable("start") long startTime, @PathVariable("end") long endTime) {
		logger.debug("Call to getChatMessages()");
		List<Chat> msgsList = ChatServiceHandler.getInstance().getMessages(startTime, endTime);
		//logger.info("Call to getChatMessages() returned with {}", msgsList.size());
		return msgsList;
	}
	
	@RequestMapping(value="/new", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Boolean post(@RequestBody Chat chatMsg) {
		//logger.info("chat post called with {}", chatMsg.getSenderName());
		Boolean result = ChatServiceHandler.getInstance().postMessage(chatMsg);
		//logger.info("chat post returned with {}", result);
		return result;
	}
	
	@RequestMapping(value = "/count/{start}/{end}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Integer getChatMessagesCount(@PathVariable("start") long startTime, @PathVariable("end") long endTime) {
		List<Chat> msgsList = ChatServiceHandler.getInstance().getMessages(startTime, endTime);
		return msgsList.size();
	}
}
