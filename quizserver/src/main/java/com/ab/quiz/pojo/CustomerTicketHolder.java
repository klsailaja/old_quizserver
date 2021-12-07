package com.ab.quiz.pojo;

public class CustomerTicketHolder {
	private CustomerTicket customerTicket;
	private Document problemPic;
	
	public CustomerTicket getCustomerTicket() {
		return customerTicket;
	}
	public void setCustomerTicket(CustomerTicket customerTicket) {
		this.customerTicket = customerTicket;
	}
	public Document getProblemPic() {
		return problemPic;
	}
	public void setProblemPic(Document problemPic) {
		this.problemPic = problemPic;
	}
}
