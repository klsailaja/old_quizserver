package com.ab.quiz.latestquestions;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;

public class QuestionsGenerator {
	
	private static List<MovieInfo> moviesDataBase = new ArrayList<>();
	private static List<String> celebrityNames = new ArrayList<>();
	private static List<String> questionsList = new ArrayList<>();
	private static Map<String,List<String>> extraOptionsMap = new HashMap<>();
	
	private static HashMap<String,String> map = new HashMap<>();
	private static List<String> finalQuestions = new ArrayList<>();
	private static List<String> perMovieCelebrityNames = new ArrayList<>();
	private static TreeSet<String> allArtistNames = new TreeSet<String>();
	
	
	private static List<String> getStrTokens(String str) {
		List<String> tokenList = new ArrayList<>();
		
		StringTokenizer strTokenizer = new StringTokenizer(str, ",");
		while (strTokenizer.hasMoreTokens()) {
			tokenList.add(strTokenizer.nextToken().trim());
		}
		return tokenList;
	}
	
	private static void fillVarsValsMap(String prefix, List<String> values) {
		
		for (int index = 0; index < values.size(); index ++) {
			String key = "%" + prefix + (index + 1) + "%";
			map.put(key, values.get(index));
		}
	}
	
	
	private static void formExtraOptions(String fileName) throws Exception {
		
		Path extraOptionsFileNamePath = Paths.get(fileName);
		List<String> extraOptionsList = Files.readAllLines(extraOptionsFileNamePath, StandardCharsets.UTF_8);
		
		for (String extraOption : extraOptionsList) {
			extraOption = extraOption.trim();
			if (extraOption.length() == 0) {
				continue;
			}
			StringTokenizer strTokenizer = new StringTokenizer(extraOption, "=");
			String key = strTokenizer.nextToken();
			String value = strTokenizer.nextToken();
			
			List<String> valuesList = getStrTokens(value); 
			extraOptionsMap.put(key, valuesList);
		}
	}
	
	
	private static void formQuestionList(String fileName) throws Exception {
		
		Path filePath = Paths.get(fileName);
		List<String> list = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		for (String line : list) {
			line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	
        	if (line.startsWith("//")) {
        		continue;
        	}
        	questionsList.add(line);
		}
	}
	
	private static void formCelebrityList(String fileName) throws Exception {
		Path filePath = Paths.get(fileName);
		List<String> list = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		for (String line : list) {
			line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	
        	if (line.startsWith("//")) {
        		continue;
        	}
        	StringTokenizer strTokenizer = new StringTokenizer(line, ",");
        	while (strTokenizer.hasMoreTokens()) {
        		celebrityNames.add(strTokenizer.nextToken().trim());
        	}
		}
	}
	
	private static Category buildCategory(StringTokenizer strTonenizer, int tokenCount, String categoryName) {
		Category category = new Category(categoryName);
		while (strTonenizer.hasMoreTokens()) {
			String str = strTonenizer.nextToken();
			str = str.trim();
			category.addValue(str);
		}
		return category;
	}
	
	private static void fillupMoviesDB(String fileName) throws Exception {
		
		Path filePath = Paths.get(fileName);
		List<String> list = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		int uniqueId = 0;
		
		for (String line : list) {
        	line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	
        	if (line.startsWith("//")) {
        		continue;
        	}

        	MovieInfo movieInfo = new MovieInfo();
        	movieInfo.setId(++uniqueId);
        	
        	StringTokenizer strTokenizer1 = new StringTokenizer(line, ":");
        	while (strTokenizer1.hasMoreTokens()) {
	    		String eachCategoryStr = strTokenizer1.nextToken();
	    		eachCategoryStr = eachCategoryStr.trim();
	    		StringTokenizer strTokenizer2 = new StringTokenizer(eachCategoryStr, "-");
	    		String categoryStr = strTokenizer2.nextToken();
	    		categoryStr = categoryStr.trim();
	    		Category category = null;
	    		
	    		switch(categoryStr) {
	    			case "a": {
	    				// Movies Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "b": {
	    				// Hero Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "c": {
	    				// Hero Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "d": {
	    				// Hero and Heroine together Category
	    				category = new Category("d");
	    				break;
	    			}
	    			case "e": {
	    				// Director Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "f": {
	    				// Music director Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "g": {
	    				// Villain Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "h": {
	    				// Relation Category
	    				category = buildCategory(strTokenizer2, 3, categoryStr);
	    				break;
	    			}
	    			case "j": {
	    				// Person Name Category
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "k": {
	    				// Profession Category
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "l": {
	    				// 2 Artists together Category
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "m": {
	    				// Song Lyrics
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "n": {
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "o": {
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "p": {
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "q": {
	    				category = buildCategory(strTokenizer2, 2, categoryStr);
	    				break;
	    			}
	    			case "r": {
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "s": {
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    		}
	    		movieInfo.addCategory(category);
	    		
        	}
        	moviesDataBase.add(movieInfo);
		}
		//System.out.println("Moviz size :" + moviesDataBase.size());
	}
	
	private static void fillupAnswers() {
		
		for (MovieInfo movieInfo : moviesDataBase) {
			map.clear();
			List<Category> categoryList = movieInfo.getAllCategories();
			
			for (Category category : categoryList) {
				switch (category.getCategoryName()) {
					case "a": {
						break;
					}
					case "b": {
						fillupBCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "c": {
						fillupCCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "e": {
						fillupECategory(movieInfo.getId(), categoryList);
						break;
					}
					case "f": {
						fillupFCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "g": {
						fillupGCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "h": {
						fillHCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "j": {
						fillJCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "k": {
						fillKCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "l": {
						fillLCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "m": {
						fillupMCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "n": {
						fillupNCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "o": {
						fillupOCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "p": {
						fillPCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "q": {
						fillupQCategory(movieInfo.getId(), categoryList);
						break;
					}
					case "r": {
						break;
					}
					case "s": {
						fillupSCategory(movieInfo.getId(), categoryList);
						break;
					}
				}
			}
		}
	}
	
	private static void  fillupSCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "s";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(0));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
				
				if (currentMovieObj.getValue(0).equals(miMovieObject.getValue(0))) {
					continue;
				}
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					
					String personName = miCurrentCategory.getValue(0);
					String pani = miCurrentCategory.getValue(1);
					
					
					if (personName.equals(currentValueObj.getValue(0))) {
						if (pani.equals(currentValueObj.getValue(1))) {
							continue;
						}
					}
					
					if (!wrongAnswers1.contains(personName)) {
						wrongAnswers1.add(personName);
						wrongAnswers2.add(miMovieObject.getValue(0));
					}
				}
			}
			
			String gender = "b";
			if (currentValueObj.getCategoryFieldsSize() == 3) {
				gender = "c";
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(gender, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillPCategory(int currentId, List<Category> allCategoryList) {
		
		String currentCategoryName = "p";
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(1));
			wrongAnswers1.addAll(answers1List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				for (Category miCurrentCategory : miCurrentNamesList) {
					String personName = miCurrentCategory.getValue(0);
					if (personName.equals(currentValueObj.getValue(0))) { 
						continue;
					}
					if (!wrongAnswers1.contains(miCurrentCategory.getValue(1))) {
						wrongAnswers1.add(miCurrentCategory.getValue(1));
					}
				}
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(currentCategoryName, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			currentValueObj.addAnswers(1, answers1List);
		}
	}

	private static void fillupQCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "q";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(1));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
				
				if (currentMovieObj.getValue(0).equals(miMovieObject.getValue(0))) {
					continue;
				}
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					
					String personName = miCurrentCategory.getValue(1);
					
					if (personName.equals(currentValueObj.getValue(1))) { 
						continue;
					}
					
					if (!wrongAnswers1.contains(personName)) {
						wrongAnswers1.add(personName);
					}
					if (!wrongAnswers2.contains(miMovieObject.getValue(0))) {
						wrongAnswers2.add(miMovieObject.getValue(0));
					}
				}
			}
			String gender = "b";
			if (currentValueObj.getCategoryFieldsSize() == 3) {
				gender = "c";
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(gender, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillupOCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "o";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(1));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					
					String personName = miCurrentCategory.getValue(1);
					
					if (personName.equals(currentValueObj.getValue(1))) { 
						continue;
					}
					
					if (!wrongAnswers1.contains(personName)) {
						wrongAnswers1.add(personName);
					}
					
					if (!wrongAnswers2.contains(miMovieObject.getValue(0))) {
						wrongAnswers2.add(miMovieObject.getValue(0));	
					}
				}
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(currentCategoryName, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillKCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "k";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(1));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					String artistName = miCurrentCategory.getValue(0);
					String professionName = miCurrentCategory.getValue(1);
					
					if ((artistName.equals(currentValueObj.getValue(0))) && 
					   (professionName.equals(currentValueObj.getValue(1)))) {
						continue;
					}
					if (artistName.equals(currentValueObj.getValue(0))) {
						if (!wrongAnswers1.contains(professionName)) {
							wrongAnswers1.add(professionName);
						}
						wrongAnswers2.add(miMovieObject.getValue(0));
					}
				}
			}
			List<String> finalWongAnswer1 = fillupWrongAnswers(currentCategoryName, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillupNCategory(int currentId, List<Category> allCategoryList) {
		
		String currentCategoryName = "n";
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = getStrTokens(currentValueObj.getValue(1));
			currentValueObj.addAnswers(1, answers1List);
		}
	}
	
	
	private static void fillupMCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "m";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		List<Category> currentHeroList = getCategoryList("b", allCategoryList);
		
		List<String> mergedHeroList = Utils.getMergedList(0, currentHeroList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (currentId == movieInfo.getId()) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miHeroList = movieInfo.getCategoryList("b");
				
				List<String> miMergedHeroList = Utils.getMergedList(0, miHeroList);
				
				for (String heroName : mergedHeroList) {
					if (miMergedHeroList.contains(heroName)) {
						String miMovieName = miMoviesList.get(0).getValue(0);
						if (!wrongAnswers2.contains(miMovieName)) {
							wrongAnswers2.add(miMovieName);
						}
					}
				}
			}
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			int fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillLCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "l";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			
			String artistName1 = currentValueObj.getValue(0);
			String artistName2 = currentValueObj.getValue(1);
			
			List<String> answers2List = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				Category miMovieObject = miMoviesList.get(0);
				
				List<String> mergedValues = Utils.getAllCategoriesMerged(movieInfo);
				
				if ((mergedValues.contains(artistName1)) && (mergedValues.contains(artistName2))) {
					continue;
				}
				
				if (!wrongAnswers2.contains(miMovieObject.getValue(0))) {
					wrongAnswers2.add(miMovieObject.getValue(0));
				}
		
				
				List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
				finalWongAnswer2.removeAll(answers2List);
				int fillGap = 8 - answers2List.size();
				for (int index = 0; index < fillGap; index++) {
					answers2List.add(finalWongAnswer2.get(index));
				}
				wrongAnswers2.clear();
				finalWongAnswer2.clear();				
				
				currentValueObj.addAnswers(2, answers2List);
			}
		}
	}
	
	
	
	private static void fillJCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "j";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(1));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					String artistName = miCurrentCategory.getValue(0);
					String characterName = miCurrentCategory.getValue(1);
					
					if ((artistName.equals(currentValueObj.getValue(0))) && 
					   (characterName.equals(currentValueObj.getValue(1)))) {
						continue;
					}
					if ((characterName.indexOf(currentValueObj.getValue(1)) > -1) || 
					   (currentValueObj.getValue(1).indexOf(characterName) > -1)) {
						continue;
					}
					if (artistName.equals(currentValueObj.getValue(0))) {
						if (!wrongAnswers1.contains(characterName)) {
							wrongAnswers1.add(characterName);
						}
						wrongAnswers2.add(miMovieObject.getValue(0));
					}
				}
			}
			String nameCategory = "j";
			boolean isFemale = (currentValueObj.getCategoryFieldsSize() == 3);
			if (isFemale) {
				nameCategory = "j1";
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(nameCategory, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void fillHCategory(int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = "h";
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		for (Category currentValueObj : currentValuesList) {
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(2));
			answers2List.add(currentMovieObj.getValue(0));
			
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				
				Category miMovieObject = miMoviesList.get(0);
		
				for (Category miCurrentCategory : miCurrentNamesList) {
					String charName1 = currentValueObj.getValue(0);
					String relationName = currentValueObj.getValue(1);
					String charName2 = currentValueObj.getValue(2);
					
					if ( (charName1.equals(miCurrentCategory.getValue(0))) && 
					   (relationName.equals(miCurrentCategory.getValue(1))) && 
					   (charName2.equals(miCurrentCategory.getValue(2))) ) {
						continue;
					}
					
					if (wrongAnswers1.size() <= 8) {
						String miCharName2 = miCurrentCategory.getValue(2);
						if (relationName.equals(miCurrentCategory.getValue(1))) {
							if (charName1.equals(miCurrentCategory.getValue(0))) {
								if ((!wrongAnswers1.contains(miCharName2)) && (!wrongAnswers2.contains(miMovieObject.getValue(0)))) {
									wrongAnswers1.add(miCharName2);
									wrongAnswers2.add(miMovieObject.getValue(0));
								}
							} else if (charName2.equals(miCurrentCategory.getValue(2))) {
								if ((!wrongAnswers1.contains(miCharName2)) && (!wrongAnswers2.contains(miMovieObject.getValue(0)))) {
									wrongAnswers1.add(miCharName2);
									wrongAnswers2.add(miMovieObject.getValue(0));
								}
							}
						}
					}
				}
			}
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(currentValueObj.getValue(1), wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();				
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
		
	}
	
	private static void fillupGCategory(int currentId, List<Category> allCategoryList) {
		fillCategory("g", currentId, allCategoryList);
	}
	
	private static void fillupFCategory(int currentId, List<Category> allCategoryList) {
		fillCategory("f", currentId, allCategoryList);
	}
	
	private static void fillupECategory(int currentId, List<Category> allCategoryList) {
		fillCategory("e", currentId, allCategoryList);
	}
	
	private static void fillupCCategory(int currentId, List<Category> allCategoryList) {
		fillCategory("c", currentId, allCategoryList);
	}
	
	private static void fillupBCategory(int currentId, List<Category> allCategoryList) {
		fillCategory("b", currentId, allCategoryList);
	}
	
	private static void fillCategory(String categoryName, int currentId, List<Category> allCategoryList) {
		String movieCategoryName = "a";
		String currentCategoryName = categoryName;
		
		List<Category> currentMovieList = getCategoryList(movieCategoryName, allCategoryList);
		List<Category> currentValuesList = getCategoryList(currentCategoryName, allCategoryList);
		
		Category currentMovieObj = currentMovieList.get(0);
		
		List<String> currentValuesMergedList = Utils.getMergedList(0, currentValuesList);
		
		
		for (Category currentValueObj : currentValuesList) {
			
			List<String> answers1List = new ArrayList<>();
			List<String> answers2List = new ArrayList<>();
			
			List<String> wrongAnswers1 = new ArrayList<>();
			List<String> wrongAnswers2 = new ArrayList<>();
			
			answers1List.add(currentValueObj.getValue(0));
			answers2List.add(currentMovieObj.getValue(0));
			wrongAnswers1.addAll(answers1List);
			wrongAnswers2.addAll(answers2List);
			
			for (MovieInfo movieInfo : moviesDataBase) {
				if (movieInfo.getId() == currentId) {
					continue;
				}
				List<Category> miMoviesList = movieInfo.getCategoryList(movieCategoryName);
				List<Category> miCurrentNamesList = movieInfo.getCategoryList(currentCategoryName);
				List<String> miCurrentValuesMergedList = Utils.getMergedList(0, miCurrentNamesList);
				Category miMovieObject = miMoviesList.get(0);
		
				boolean matched = false;
				for (String value : currentValuesMergedList) {
					if (miCurrentValuesMergedList.contains(value)) {
						matched = true;
					}
				}
				if (matched) {
					continue;
				}
				for (String wronhAns : miCurrentValuesMergedList) {
					if (!wrongAnswers1.contains(wronhAns)) {
						wrongAnswers1.add(wronhAns);
					}
				}
				if (!wrongAnswers2.contains(miMovieObject.getValue(0))) {
					wrongAnswers2.add(miMovieObject.getValue(0));
				}
			}
			
			
			List<String> finalWongAnswer1 = fillupWrongAnswers(currentCategoryName, wrongAnswers1);
			finalWongAnswer1.removeAll(answers1List);
			int fillGap = 8 - answers1List.size();
			for (int index = 0; index < fillGap; index++) {
				answers1List.add(finalWongAnswer1.get(index));
			}
			wrongAnswers1.clear();
			finalWongAnswer1.clear();
			
			
			List<String> finalWongAnswer2 = fillupWrongAnswers(movieCategoryName, wrongAnswers2);
			finalWongAnswer2.removeAll(answers2List);
			fillGap = 8 - answers2List.size();
			for (int index = 0; index < fillGap; index++) {
				answers2List.add(finalWongAnswer2.get(index));
			}
			wrongAnswers2.clear();
			finalWongAnswer2.clear();			
			
			currentValueObj.addAnswers(1, answers1List);
			currentValueObj.addAnswers(2, answers2List);
		}
	}
	
	private static void addToLocalCelebrityList(String artistName) {
		if (!perMovieCelebrityNames.contains(artistName)) {
			perMovieCelebrityNames.add(artistName);
		}
	}
	
	private static void formQuestions() throws Exception {
		
		// Get the file reference
    	Path path = Paths.get("D://Projects//Games/T1.txt");
    	int qCount = 0;
    	BufferedWriter writer = Files.newBufferedWriter(path);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			map.clear();
        	finalQuestions.clear();
        	perMovieCelebrityNames.clear();
        	
			for (Category category : movieInfo.getAllCategories()) {
				switch(category.getCategoryName()) {
					case "b": {
						addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("b", "H", category.getType1Answers(), "bc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "c": {
						addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("c", "HR", category.getType1Answers(), "cc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "e": {
						addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("e", "D", category.getType1Answers(), "ec", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "f": {
						addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("f", "MD", category.getType1Answers(), "fc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "g": {
						addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("g", "V", category.getType1Answers(), "gc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "h": {
						map.put("%RELATION_CHAR%", category.getValue(0));
	    				map.put("%RELATION%", category.getValue(1));
	    				addToLocalCelebrityList(category.getValue(0));
	    				addToLocalCelebrityList(category.getValue(2));
	    				List<String> categoryQuestions = getFormedQuestions("h", "RL", category.getType1Answers(), "hc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "j": {
						map.put("%RELATION_CHAR%", category.getValue(0));
	    				map.put("%PERU%", category.getValue(1));
	    				addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("j", "PERU", category.getType1Answers(), "jc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "k": {
						map.put("%RELATION_CHAR%", category.getValue(0));
	    				map.put("%PR%", category.getValue(1));
	    				addToLocalCelebrityList(category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("k", "PR", category.getType1Answers(), "kc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "l": {
						map.put("%ARTIST1%", category.getValue(0));
	    				map.put("%ARTIST2%", category.getValue(1));
	    				addToLocalCelebrityList(category.getValue(0));
	    				addToLocalCelebrityList(category.getValue(1));
	    				List<String> categoryQuestions = getFormedQuestions("l", "IGNORE", category.getType1Answers(), "lc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "m": {
						map.put("%SG%", category.getValue(0));
	    				List<String> categoryQuestions = getFormedQuestions("m", "IGNORE", category.getType1Answers(), "mc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "n": {
						map.put("%FILLSONG%", category.getValue(0));
						List<String> categoryQuestions = getFormedQuestions("n", "FS", category.getType1Answers(), "IGNORE", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "o": {
						map.put("%SS_ARTIST%", category.getValue(0));
						addToLocalCelebrityList(category.getValue(1));
						List<String> categoryQuestions = getFormedQuestions("o", "SSH", category.getType1Answers(), "oc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "p": {
						map.put("%SS_ARTIST%", category.getValue(0));
						addToLocalCelebrityList(category.getValue(0));
						List<String> categoryQuestions = getFormedQuestions("p", "SS", category.getType1Answers(), "IGNORE", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "q": {
						map.put("%SS_ARTIST%", category.getValue(0));
						map.put("%GR%", category.getValue(1));
						addToLocalCelebrityList(category.getValue(1));
						List<String> categoryQuestions = getFormedQuestions("q", "GR", category.getType1Answers(), "qc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
					case "r": {
						break;
					}
					case "s": {
						map.put("%S_ARTIST%", category.getValue(0));
						map.put("%PANI%", category.getValue(1));
						addToLocalCelebrityList(category.getValue(1));
						List<String> categoryQuestions = getFormedQuestions("s", "GR", category.getType1Answers(), "sc", 
	    						category.getType2Answers());
	    				finalQuestions.addAll(categoryQuestions);
						break;
					}
				}
			}
			
			/*for (String lineQuestion : finalQuestions) {
				StringTokenizer finalQuestionStrTokenizer = new StringTokenizer(lineQuestion, ":");
				finalQuestionStrTokenizer.nextToken();
				finalQuestionStrTokenizer.nextToken();
	    		String optionATxt = finalQuestionStrTokenizer.nextToken().trim();
	    		System.out.println("optionATxt :" + optionATxt);
	    		if (celebrityNames.contains(optionATxt)) {
	    			if (!perMovieCelebrityNames.contains(optionATxt)) {
	    				perMovieCelebrityNames.add(optionATxt);
	    			}
	    		}
			}*/
			
			String sqlQry = "INSERT INTO QUIZQUESTIONS (NSTATEMENT,NOPTIONA,NOPTIONB,NOPTIONC,NOPTIOND,NOPTIONE,NOPTIONF,NOPTIONG,NOPTIONH,CORRECTOPTION,CATEGORY) VALUES('";
			List<String> mixedModeCategories = new ArrayList<>();
			mixedModeCategories.add("b");
			mixedModeCategories.add("c");
			mixedModeCategories.add("e");
			mixedModeCategories.add("f");
			
			List<String> personNamesCategories = new ArrayList<>();
			personNamesCategories.add("b1");
			personNamesCategories.add("c1");
			personNamesCategories.add("e1");
			personNamesCategories.add("f1");
			personNamesCategories.add("g1");
			personNamesCategories.add("h1");
			personNamesCategories.add("j1");
			personNamesCategories.add("o1");
			personNamesCategories.add("q1");
			personNamesCategories.add("s");
			
	    	for (String lineQuestion : finalQuestions) {
	    		
	    		StringTokenizer finalQuestionStrTokenizer = new StringTokenizer(lineQuestion, ":");
	    		String categoryNameStr = finalQuestionStrTokenizer.nextToken();
	    		
	    		boolean skip = false;
	    		if (categoryNameStr.indexOf("1") > -1) {
	    			skip = true;
	    		}
	    		
	    		String questionTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionATxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionBTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionCTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionDTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionETxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionFTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionGTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionHTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		
	    		if ((optionATxt.indexOf("%") > -1) || ((optionBTxt.indexOf("%") > -1)) || ((optionCTxt.indexOf("%") > -1))
	    		 || (((optionDTxt.indexOf("%") > -1)))) {
	    			System.out.println(lineQuestion);
	    		}
	    		List<String> uniqueValues = new ArrayList<>();
	    		if (!uniqueValues.contains(optionATxt)) {
	    			uniqueValues.add(optionATxt);
	    		}
	    		if (!uniqueValues.contains(optionBTxt)) {
	    			uniqueValues.add(optionBTxt);
	    		}
	    		if (!uniqueValues.contains(optionCTxt)) {
	    			uniqueValues.add(optionCTxt);
	    		}
	    		if (!uniqueValues.contains(optionDTxt)) {
	    			uniqueValues.add(optionDTxt);
	    		}
	    		if (!uniqueValues.contains(optionETxt)) {
	    			uniqueValues.add(optionETxt);
	    		}
	    		if (!uniqueValues.contains(optionFTxt)) {
	    			uniqueValues.add(optionFTxt);
	    		}
	    		if (!uniqueValues.contains(optionGTxt)) {
	    			uniqueValues.add(optionGTxt);
	    		}
	    		if (!uniqueValues.contains(optionHTxt)) {
	    			uniqueValues.add(optionHTxt);
	    		}
	    		if (uniqueValues.size() != 8) {
	    			System.out.println(lineQuestion);
	    		}
	    		
	    		if (personNamesCategories.contains(categoryNameStr)) {
	    			allArtistNames.addAll(uniqueValues);
	    		}
	    		
	    		StringBuffer celebrityIdSet = new StringBuffer();
		    	for (String celebrityName : perMovieCelebrityNames) {
		    		if (!celebrityNames.contains(celebrityName)) {
		    			continue;
		    		}
		    		if (skip) {
		    			if (optionATxt.equals(celebrityName)) {
		    				continue;
		    			}
		    		}
	    			int id = 1 + celebrityNames.indexOf(celebrityName);
	    			celebrityIdSet.append(id);
	    			celebrityIdSet.append(",");
	    		}
		    	String celebrityIdSetStr = celebrityIdSet.toString();
		    	if (celebrityIdSetStr.length() == 0) {
		    		celebrityIdSetStr = "(64)";
		    	} else {
		    		String categoryName = categoryNameStr;
		    		int position = categoryName.indexOf("1");
		    		if (position > -1) {
		    			categoryName = categoryName.substring(0, position);
		    		}
		    		if (mixedModeCategories.contains(categoryName)) {
		    			celebrityIdSet.append(64);
		    			celebrityIdSet.append(",");
		    			celebrityIdSetStr = celebrityIdSet.toString();
		    		}
		    	}
		    	int pos = celebrityIdSetStr.lastIndexOf(",");
		    	if (pos > -1) {
		    		celebrityIdSetStr = celebrityIdSetStr.substring(0, pos);
		    		celebrityIdSetStr = "('" + celebrityIdSetStr + "')";
		    	}
	    		
	    		StringBuffer strBuffer = new StringBuffer(sqlQry);
	    		strBuffer.append(questionTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionATxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionBTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionCTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionDTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionETxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionFTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionGTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("'");
	    		strBuffer.append(optionHTxt);
	    		strBuffer.append("'");
	    		strBuffer.append(",");
	    		strBuffer.append("1");
	    		strBuffer.append(",");
	    		strBuffer.append(celebrityIdSetStr);
	    		strBuffer.append(");");
	    		
	    		System.out.println(strBuffer.toString());
	    		
	    		//Use try-with-resource to get auto-closeable writer instance
	    	    writer.append(strBuffer.toString());
	    	    writer.append("\n");
	    	    writer.flush();
	    	    qCount++;
		    	//System.out.println("qCount :" + qCount);
	    	}
		}
	}
	
	private static List<String> getRelevantQuestions(String categoryVal) {
		
		String categoryStr = String.valueOf(categoryVal);
		List<String> relevantQuestions = new ArrayList<>();
		
		for (String str : questionsList) {
			StringTokenizer strTokenizer1 = new StringTokenizer(str, ":");
			String token1 = strTokenizer1.nextToken();
			token1 = token1.trim();
			if (token1.startsWith(categoryStr)) {
				relevantQuestions.add(str);
			}
		}
		return relevantQuestions;
	}
	
	private static List<String> getFormedQuestions(String categoryStr, 
			String prefix1, List<String> answers1, String prefix2, List<String> answers2) {
		
		List<String> questions = new ArrayList<>();
		
		fillVarsValsMap(prefix1, answers1);
		fillVarsValsMap(prefix2, answers2);
		
		List<String> ques = getRelevantQuestions(categoryStr);
		
		for (String eachQues : ques) {
			Set<Entry<String,String>> mapSet = map.entrySet();
			for (Map.Entry<String, String> entry : mapSet) {
				String key = entry.getKey();
				String value = entry.getValue();
				eachQues = eachQues.replaceAll(key, value);
			}
			questions.add(eachQues);
		}
		return questions;
	}
	
	private static List<String> fillupWrongAnswers(String categoryName, List<String> wrongAnswers) {
		TreeSet<String> wrongAnswerSet = new TreeSet<String>();
		wrongAnswerSet.addAll(wrongAnswers);
		wrongAnswers.clear();
		wrongAnswers.addAll(wrongAnswerSet);
		
		List<String> fullWrongAnswers = new ArrayList<>();
		
		if (wrongAnswers.size() < 8) {
			//fillFromExtraOptions(categoryName, wrongAnswers);
			fullWrongAnswers.addAll(wrongAnswers);
			List<String> extraOptionsList = extraOptionsMap.get(categoryName);
			int totalTokensCt = 8;
			int fillCt = totalTokensCt - wrongAnswers.size();
			if (extraOptionsList != null) {
				int size = extraOptionsList.size();
				if (size > 0) {
					for (int index = 1; index <= fillCt; index ++) {
						int randomIndex = getRandomNumber(0, size);
						while (fullWrongAnswers.contains(extraOptionsList.get(randomIndex))) {
							randomIndex = getRandomNumber(0, size);
						}
						fullWrongAnswers.add(extraOptionsList.get(randomIndex));
					}
				}
			}
			return fullWrongAnswers;
		}
		
		List<Integer> wrongAnswerIndexes = new ArrayList<>();
		int size = wrongAnswers.size();
		for (int index = 1; index <= 8; index ++) {
			int randomIndex = getRandomNumber(0, size);
			while (wrongAnswerIndexes.contains(randomIndex)) {
				randomIndex = getRandomNumber(0, size);
			}
			wrongAnswerIndexes.add(randomIndex);
			fullWrongAnswers.add(wrongAnswers.get(randomIndex));
		}
		return fullWrongAnswers;
	}
	
	private static List<Category> getCategoryList (String categoryName, List<Category> allCategoryList) {
		List<Category> categoryList = new ArrayList<>();
		for (Category category : allCategoryList) {
			if (categoryName.equals(category.getCategoryName())) {
				categoryList.add(category);
			}
		}
		return categoryList; 
	}
	
	private static void fillFromExtraOptions(String categoryStr, List<String> tokens) {
		List<String> extraOptionsList = extraOptionsMap.get(categoryStr);
		int totalTokensCt = 3;
		int fillCt = totalTokensCt - tokens.size();
		if (extraOptionsList != null) {
			int size = extraOptionsList.size();
			if (size > 0) {
				for (int index = 1; index <= fillCt; index ++) {
					int randomIndex = getRandomNumber(0, size);
					while (tokens.contains(extraOptionsList.get(randomIndex))) {
						randomIndex = getRandomNumber(0, size);
					}
					tokens.add(extraOptionsList.get(randomIndex));
				}
			}
		}
	}
	
	private static int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
	
	public static void main(String[] args) throws Exception {
		
		String movieInputPath = "D:\\Projects\\Games\\MovieDetails.txt";
		fillupMoviesDB(movieInputPath);
		
		String celebritiesInputPath = "D:\\Projects\\Games\\Celebrities.txt";
		formCelebrityList(celebritiesInputPath);
		
		String questionListFileName = "D:\\Projects\\Games\\MovieQuestionsTemplate.txt";
		formQuestionList(questionListFileName);
		
		String extraOptionsFileName = "D:\\Projects\\Games\\MovieExtraOptions.txt";
		formExtraOptions(extraOptionsFileName);
		
		fillupAnswers();
		
		formQuestions();
		
		System.out.println(allArtistNames.size());
		for (String artistName : allArtistNames) {
			System.out.println(artistName);
		}
		
	}
}
