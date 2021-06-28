package com.ab.quiz.latestquestions;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	public static List<String> getMergedList(int index, List<Category> mutipleEntries) {
		
		List<String> mergedList = new ArrayList<>();
		for (Category category : mutipleEntries) {
			mergedList.add(category.getValue(index));
		}
		return mergedList;
	}
	
	public static List<String> getAllCategoriesMerged(MovieInfo movieInfo) {
		List<String> mergedResults = new ArrayList<>();
		List<Category> allCategories = movieInfo.getAllCategories();
		
		for (Category category : allCategories) {
			mergedResults.addAll(category.getCategoryValues());
		}
		return mergedResults;
	}
}
