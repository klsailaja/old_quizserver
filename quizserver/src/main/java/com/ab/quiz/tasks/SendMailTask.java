package com.ab.quiz.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.Mail;
import com.ab.quiz.services.MailService;
import com.ab.quiz.services.MailServiceImpl;

public class SendMailTask implements Runnable {
	private Mail mail;
	private static final Logger logger = LogManager.getLogger(SendMailTask.class);
	
	public SendMailTask(Mail mail) {
		this.mail = mail;
	}
	
	@Override
	public void run() {
		logger.info("This is in Mail Task {}", mail);
		MailService mailService = new MailServiceImpl();
		mailService.sendEmail(mail);
	}
}
