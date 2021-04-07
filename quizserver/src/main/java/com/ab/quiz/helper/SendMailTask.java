package com.ab.quiz.helper;

import com.ab.quiz.pojo.Mail;
import com.ab.quiz.services.MailService;
import com.ab.quiz.services.MailServiceImpl;

public class SendMailTask implements Runnable {
	private Mail mail;
	
	public SendMailTask(Mail mail) {
		this.mail = mail;
	}
	
	@Override
	public void run() {
		MailService mailService = new MailServiceImpl();
		mailService.sendEmail(mail);
	}

}
