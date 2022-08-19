package com.ab.tool;

import java.util.List;

public class KYCEntriesHolder {
	private int total;
	private boolean isPrevEnabled;
	private boolean isNextEnabled;
	private List<KYCEntry> list;
	
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
	public List<KYCEntry> getList() {
		return list;
	}
	public void setList(List<KYCEntry> list) {
		this.list = list;
	}
}
