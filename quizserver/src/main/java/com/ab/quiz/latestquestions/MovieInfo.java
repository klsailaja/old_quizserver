package com.ab.quiz.latestquestions;

import java.util.ArrayList;
import java.util.List;

public class MovieInfo {
	private List<Category> categories = new ArrayList<>();
	private int id;
	
	public MovieInfo() {
	}
	
	public void addCategory(Category newCategory) {
		categories.add(newCategory);
	}
	
	public List<Category> getCategoryList(String categoryName) {
		List<Category> categoryList = new ArrayList<>();
		for (Category category : categories) {
			if (categoryName.equals(category.getCategoryName())) {
				categoryList.add(category);
			}
		}
		return categoryList;
	}
	
	public List<Category> getAllCategories() {
		return categories;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
}
