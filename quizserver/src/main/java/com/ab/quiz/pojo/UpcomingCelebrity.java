package com.ab.quiz.pojo;

import java.util.List;

public class UpcomingCelebrity {
	private String hourVal;
	private List<String> celebrityNames;
	
	public String getHourVal() {
		return hourVal;
	}
	public void setHourVal(String hourVal) {
		this.hourVal = hourVal;
	}
	public List<String> getCelebrityNames() {
		return celebrityNames;
	}
	public void setCelebrityNames(List<String> celebrityNames) {
		this.celebrityNames = celebrityNames;
	}
	
	@Override
	public String toString() {
		return "UpcomingCelebrity [hourVal=" + hourVal + ", celebrityNames=" + celebrityNames + "]";
	}
}
