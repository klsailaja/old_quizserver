package com.ab.quiz.pojo;

public class WithdrawReqByBankAcc {
	private long id;
	private String bankAccountNumber;
	private String bankIfscCode;
	private String bankName;
	private String bankBranchName;
	private String bankAccountHolderName;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}
	public String getBankIfscCode() {
		return bankIfscCode;
	}
	public void setBankIfscCode(String bankIfscCode) {
		this.bankIfscCode = bankIfscCode;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankBranchName() {
		return bankBranchName;
	}
	public void setBankBranchName(String bankBranchName) {
		this.bankBranchName = bankBranchName;
	}
	public String getBankAccountHolderName() {
		return bankAccountHolderName;
	}
	public void setBankAccountHolderName(String bankAccountHolderName) {
		this.bankAccountHolderName = bankAccountHolderName;
	}
}
