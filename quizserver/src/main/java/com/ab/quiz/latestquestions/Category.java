package com.ab.quiz.latestquestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Category {
	private String categoryName;
	private List<String> categoryFileds = new ArrayList<>();
	private List<String> questionType1AnswersList = new ArrayList<>();
	private List<String> questionType2AnswersList = new ArrayList<>();
	private Map<String,List<String>> categoryNameVsAnswersList = new HashMap<>();
	
	public Category(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public void addAnswersToMap(String dependencyCategoryName, List<String> answers) {
		categoryNameVsAnswersList.put(dependencyCategoryName, answers);
	}
	
	public Map<String,List<String>> getMapAnswers() {
		return categoryNameVsAnswersList;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void addValue(String fieldValue) {
		categoryFileds.add(fieldValue);
	}
	
	public String getValue(int index) {
		return categoryFileds.get(index);
	}
	
	public int getCategoryFieldsSize() {
		return categoryFileds.size();
	}
	
	public List<String> getCategoryValues() {
		return categoryFileds;
	}
	
	public void addAnswers(int questionType, List<String> answers) {
		List<String> handle = questionType1AnswersList;
		if (questionType == 2) {
			handle = questionType2AnswersList;
		}
		handle.addAll(answers);
	}
	
	public void clearAnswers(int questionType) {
		List<String> handle = questionType1AnswersList;
		if (questionType == 2) {
			handle = questionType2AnswersList;
		}
		handle.clear();
	}
	
	public List<String> getType1Answers() {
		return questionType1AnswersList;
	}
	
	public List<String> getType2Answers() {
		return questionType2AnswersList;
	}
	
	public String toString() {
		return categoryName + " : \n" 
				+ categoryFileds + "\n"  
				+ categoryNameVsAnswersList; 
	}
}
