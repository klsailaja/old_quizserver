package com.ab.quiz.pojo;

import java.util.List;

public class WithdrawRequestsHolder {
	private int total;
	private boolean isPrevEnabled;
	private boolean isNextEnabled;
	private List<WithdrawRequest> list;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public boolean isPrevEnabled() {
		return isPrevEnabled;
	}
	public void setPrevEnabled(boolean isPrevEnabled) {
		this.isPrevEnabled = isPrevEnabled;
	}
	public boolean isNextEnabled() {
		return isNextEnabled;
	}
	public void setNextEnabled(boolean isNextEnabled) {
		this.isNextEnabled = isNextEnabled;
	}
	public List<WithdrawRequest> getList() {
		return list;
	}
	public void setList(List<WithdrawRequest> list) {
		this.list = list;
	}
	
	@Override
	public String toString() {
		return "WithdrawRequestsHolder [total=" + total + ", isPrevEnabled=" + isPrevEnabled + ", isNextEnabled="
				+ isNextEnabled + ", list=" + list + "]";
	}
}
