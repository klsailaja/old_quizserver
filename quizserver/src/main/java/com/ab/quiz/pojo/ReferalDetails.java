package com.ab.quiz.pojo;

import java.util.List;

public class ReferalDetails {
	private boolean isPrevEnabled;
	private boolean isNextEnabled;
	private List<UserReferal> referalList;
	
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
	public List<UserReferal> getReferalList() {
		return referalList;
	}
	public void setReferalList(List<UserReferal> referalList) {
		this.referalList = referalList;
	}
}
