package com.ab.quiz.tasks;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.db.CustomerCareDBHandler;
import com.ab.quiz.pojo.CustomerTicket;

public class CreateCustomerTickets implements Runnable {
	private static final Logger logger = LogManager.getLogger(CreateCustomerTickets.class);
	private List<CustomerTicket> tkts;
	
	public CreateCustomerTickets(List<CustomerTicket> tkts) {
		this.tkts = tkts;
	}
	
	public void run() {
		String tag = TAGS.CREATE_CUSTOMER_TKT;
		logger.info("{} This is in CreateCustomerTickets", tag);
		try {
			CustomerCareDBHandler.getInstance().createTicketsInBulk(tkts, 20);
		} catch (Exception ex) {
			logger.error(QuizConstants.ERROR_PREFIX_START);
			logger.error("{} Exception in CreateCustomerTickets", tag, ex);
			logger.error(QuizConstants.ERROR_PREFIX_END);
		}
	}
}
