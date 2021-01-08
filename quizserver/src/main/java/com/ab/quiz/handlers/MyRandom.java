package com.ab.quiz.handlers;

import java.util.ArrayList;
import java.util.List;

public class MyRandom {
	
	private long leftLimit = 1L;
	private long maxLimit;
	private List<Long> generatedList = new ArrayList<>();
	
	public MyRandom(long max) {
		this.maxLimit = max;
	}
	
	public Long[] generateList(int size) {
	    while (generatedList.size() < size) {
	    	long generatedLong = leftLimit + (long) (Math.random() * (maxLimit - leftLimit));
	        if (!generatedList.contains(generatedLong)) {
	            generatedList.add(generatedLong);
	        }
		 }
		 Long [] list = new Long[11];
		 list = generatedList.toArray(list);
		 return list;
	}
}
