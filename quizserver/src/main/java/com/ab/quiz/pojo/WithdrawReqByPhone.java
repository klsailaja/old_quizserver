package com.ab.quiz.pojo;

public class WithdrawReqByPhone {
	private long id;
	private String phNumber;
	private int paymentMethod;
	private String accountHolderName;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getPhNumber() {
		return phNumber;
	}
	public void setPhNumber(String phNumber) {
		this.phNumber = phNumber;
	}
	public int getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(int paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	
	@Override
	public String toString() {
		return "WithdrawReqByPhone [id=" + id + ", phNumber=" + phNumber + ", paymentMethod=" + paymentMethod
				+ ", accountHolderName=" + accountHolderName + "]";
	}
}
