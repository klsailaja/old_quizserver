package com.ab.quiz.pojo;

public class WithdrawRequestInput {
	
	private WDUserInput withdrawUserInput;
	private WithdrawReqByPhone byPhoneDetails;
	private WithdrawReqByBank byBankDetails;
	
	public WithdrawReqByBank getByBankDetails() {
		return byBankDetails;
	}
	public void setByBankDetails(WithdrawReqByBank byBankDetails) {
		this.byBankDetails = byBankDetails;
	}
	public WDUserInput getWithdrawUserInput() {
		return withdrawUserInput;
	}
	public void setWithdrawUserInput(WDUserInput withdrawUserInput) {
		this.withdrawUserInput = withdrawUserInput;
	}
	public WithdrawReqByPhone getByPhoneDetails() {
		return byPhoneDetails;
	}
	public void setByPhoneDetails(WithdrawReqByPhone byPhoneDetails) {
		this.byPhoneDetails = byPhoneDetails;
	}
}
