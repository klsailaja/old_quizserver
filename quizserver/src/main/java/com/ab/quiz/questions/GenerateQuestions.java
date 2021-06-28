package com.ab.quiz.questions;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

public class GenerateQuestions {
	
	private static List<String> celebrityNames = new ArrayList<>();
	private static List<String> perMovieCelebrityNames = new ArrayList<>();
	private static List<String> questionsList = new ArrayList<>();
	private static List<String> extraOptionsList = new ArrayList<>();
	private static Map<String,List<String>> extraOptionsMap = new HashMap<>();
	
	private static HashMap<String,String> map = new HashMap<>();
	
	private static void formCelebrity(String celebrityName) {
		if (!celebrityNames.contains(celebrityName)) {
			celebrityNames.add(celebrityName);
		}
		if (!perMovieCelebrityNames.contains(celebrityName)) {
			perMovieCelebrityNames.add(celebrityName);
		}
	}
	
	private static void fillVarsValsMap(String prefix, List<String> values) {
		
		for (int index = 0; index < values.size(); index ++) {
			String key = "%" + prefix + (index + 1) + "%";
			map.put(key, values.get(index));
		}
	}
	
	private static List<String> getStrTokens(String str) {
		
		List<String> tokenList = new ArrayList<>();
		
		StringTokenizer strTokenizer = new StringTokenizer(str, ",");
		while (strTokenizer.hasMoreTokens()) {
			tokenList.add(strTokenizer.nextToken().trim());
		}
		return tokenList;
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
	
	private static List<String> getFormedQuestions(boolean isCelebrity, 
			String tokensList, String prefixStr, String categoryStr) {
		return getFormedQuestions(isCelebrity, tokensList, prefixStr, categoryStr, null);
	}
	
	private static List<String> getFormedQuestions(boolean isCelebrity, 
			String tokensList, String prefixStr, String categoryStr, String relation) {
		
		tokensList = tokensList.trim();
		List<String> questions = new ArrayList<>();
		List<String> tokens = getStrTokens(tokensList);
		
		int totalTokensCt = 4;
		if (categoryStr.equals("a")) {
			totalTokensCt = 7;
		}
		
		int fillCt = totalTokensCt - tokens.size();
		String extraOptionsKey = categoryStr;
		if (extraOptionsKey.equals("h")) {
			extraOptionsKey = relation;
		} 
		List<String> extraOptionsList = extraOptionsMap.get(extraOptionsKey);
		System.out.println("extraOptionsKey " + extraOptionsKey);
		System.out.println(extraOptionsMap);
		
		if (extraOptionsList != null) {
			int size = extraOptionsList.size();
			if ((size > 0) && (tokens.size() > 0)) {
				for (int index = 1; index <= fillCt; index ++) {
					int randomIndex = getRandomNumber(0, size);
					while (tokens.contains(extraOptionsList.get(randomIndex))) {
						randomIndex = getRandomNumber(0, size);
					}
					tokens.add(extraOptionsList.get(randomIndex));
				}
			}
		}
		
		if (isCelebrity) {
			formCelebrity(tokens.get(0));
		}
		
		fillVarsValsMap(prefixStr, tokens);
		
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
	
	
	public static void main(String[] args) throws Exception {
		
		List<String> finalQuestions = new ArrayList<>();
		
		String coreDetails = "D:\\Projects\\Games\\MovieDetails.txt";
		Path coreDetailsPath = Paths.get(coreDetails);
		
		List<String> list = Files.readAllLines(coreDetailsPath, StandardCharsets.UTF_8);
		
		String questionListFileName = "D:\\Projects\\Games\\MovieQuestionsTemplate.txt";
		Path questionListFileNamePath = Paths.get(questionListFileName);
		
		List<String> questionsTempList = Files.readAllLines(questionListFileNamePath, StandardCharsets.UTF_8);
		
		String extraOptionsFileName = "D:\\Projects\\Games\\MovieExtraOptions.txt";
		Path extraOptionsFileNamePath = Paths.get(extraOptionsFileName);
		extraOptionsList = Files.readAllLines(extraOptionsFileNamePath, StandardCharsets.UTF_8);
		
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
		
		
		for (String line : questionsTempList) {
        	line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	questionsList.add(line);
		}
		
		// Get the file reference
    	Path path = Paths.get("D://Projects//Games/T1.txt");
    	int qCount = 0;
    	BufferedWriter writer = Files.newBufferedWriter(path);
		
		for (String line : list) {
        	line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	
        	if (line.startsWith("//")) {
        		continue;
        	}
        	
        	map.clear();
        	finalQuestions.clear();
        	perMovieCelebrityNames.clear();
        	
	    	StringTokenizer strTokenizer1 = new StringTokenizer(line, ":");
	    	
	    	while (strTokenizer1.hasMoreTokens()) {
	    		String eachCategoryStr = strTokenizer1.nextToken();
	    		eachCategoryStr = eachCategoryStr.trim();
	    		StringTokenizer strTokenizer2 = new StringTokenizer(eachCategoryStr, "-");
	    		String categoryStr = strTokenizer2.nextToken();
	    		categoryStr = categoryStr.trim();
	    		
	    		switch(categoryStr) {
	    			case "a": {
	    				// Movies Category
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "M", "a");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "b": {
	    				// Hero category
	    				String str = strTokenizer2.nextToken();
	    				
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("bc", cinemaTokens);
	    				
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, str, "H", "b");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "c": {
	    				// Heroines category
	    				String str = strTokenizer2.nextToken();
	    				
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("cc", cinemaTokens);
	    				
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, str, "HR", "c");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "d": {
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("dc", cinemaTokens);
	    				List<String> categoryQuestions = getFormedQuestions(false, "", "H", "d");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "e": {
	    				// Directors
	    				String str = strTokenizer2.nextToken();
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("ec", cinemaTokens);
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, str, "D", "e");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "f": {
	    				// Music director
	    				String str = strTokenizer2.nextToken();
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("fc", cinemaTokens);
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, str, "MD", "f");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "g": {
	    				// Villian
	    				String str = strTokenizer2.nextToken();
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("gc", cinemaTokens);
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, str, "V", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "h": {
	    				// x Relation artist name
	    				String relatedChar = strTokenizer2.nextToken();
	    				String relation = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("hc", cinemaTokens);
	    				
	    				String isCelebrityStr1 = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr1 = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity1 = false; 
	    				if ((isCelebrityStr1 != null) && (isCelebrityStr1.equals("1"))) {
	    					isCelebrity1 = true;
	    				}
	    				
	    				String isCelebrityStr2 = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr2 = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity2 = false; 
	    				if ((isCelebrityStr2 != null) && (isCelebrityStr2.equals("1"))) {
	    					isCelebrity2 = true;
	    				}
	    				if (isCelebrity1) {
	    					formCelebrity(relatedChar);
	    				}
	    				
	    				map.put("%RELATION_CHAR%", relatedChar);
	    				map.put("%RELATION%", relation);
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity2, str, "RL", categoryStr, relation);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "i": {
	    				String relatedChar = strTokenizer2.nextToken();
	    				String relation = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("ic", cinemaTokens);
	    				map.put("%RELATION_CHAR%", relatedChar);
	    				map.put("%RELATION%", relation);
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "RN", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "j": {
	    				// x relation name
	    				String artistName = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%RELATION_CHAR%", artistName);
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("jc", cinemaTokens);
	    				
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				
	    				
	    				if (isCelebrity) {	
	    					formCelebrity(artistName);
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "PERU", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "k": {
	    				// x profession ??
	    				String artistName = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%RELATION_CHAR%", artistName);
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("kc", cinemaTokens);
	    				
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				if (isCelebrity) {
	    					formCelebrity(artistName);
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "PR", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "l": {
	    				// Combination of 2 artists
	    				String artistName1 = strTokenizer2.nextToken();
	    				String artistName2 = strTokenizer2.nextToken();
	    				map.put("%ARTIST1%", artistName1);
	    				map.put("%ARTIST2%", artistName2);
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("lc", cinemaTokens);
	    				
	    				String isCelebrityStr1 = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr1 = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity1 = false; 
	    				if ((isCelebrityStr1 != null) && (isCelebrityStr1.equals("1"))) {
	    					isCelebrity1 = true;
	    				}
	    				String isCelebrityStr2 = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr2 = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity2 = false; 
	    				if ((isCelebrityStr2 != null) && (isCelebrityStr2.equals("1"))) {
	    					isCelebrity2 = true;
	    				}
	    				
	    				if (isCelebrity1) {
	    					formCelebrity(artistName1);
	    				}
	    				if (isCelebrity2) {
	    					formCelebrity(artistName2);
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(false, "", "", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "m": {
	    				// Song lyrics in which movie
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "SG", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "n": {
	    				// Fill the song lyrics with a missing word
	    				String songLyrics = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%FILLSONG%", songLyrics);
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "FS", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "o": {
	    				// Special song
	    				String artistType = strTokenizer2.nextToken();
	    				String artistList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistType);
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("oc", cinemaTokens);
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, artistList, "SSH", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "p": {
	    				// Song lyrics for which the special song performed by x
	    				String artistName = strTokenizer2.nextToken();
	    				String songList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistName);
	    				
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	    				if (isCelebrity) {
	    					formCelebrity(artistName);
	    				}

	    				List<String> categoryQuestions = getFormedQuestions(false, songList, "SS", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "q": {
	    				// guest role done by artist
	    				String artistType = strTokenizer2.nextToken();
	    				String artistList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistType);
	    				String cinemaList = strTokenizer2.nextToken();
	    				List<String> cinemaTokens = getStrTokens(cinemaList);
	    				fillVarsValsMap("qc", cinemaTokens);
	    				String isCelebrityStr = null;
	    				if (strTokenizer2.hasMoreTokens()) {
	    					isCelebrityStr = strTokenizer2.nextToken(); 
	    				}
	    				boolean isCelebrity = false; 
	    				if ((isCelebrityStr != null) && (isCelebrityStr.equals("1"))) {
	    					isCelebrity = true;
	    				}
	
	    				List<String> categoryQuestions = getFormedQuestions(isCelebrity, artistList, "GR", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "r": {
	    				// movie caption
	    				String caption = strTokenizer2.nextToken();
	    				map.put("%MC%", caption);
	    				List<String> categoryQuestions = getFormedQuestions(false, caption, "MC", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    		}
	    	}
	    	
	    	String sqlQry = "INSERT INTO QUIZQUESTIONS (NSTATEMENT,NOPTIONA,NOPTIONB,NOPTIONC,NOPTIOND,CORRECTOPTION,CATEGORY) VALUES('";
	    	
	    	for (String lineQuestion : finalQuestions) {
	    		
	    		StringTokenizer finalQuestionStrTokenizer = new StringTokenizer(lineQuestion, ":");
	    		String categoryNameStr = finalQuestionStrTokenizer.nextToken();
	    		
	    		boolean skip = false;
	    		if (categoryNameStr.indexOf("1") > -1) {
	    			skip = true;
	    		}
	    		
	    		String questionTxt = finalQuestionStrTokenizer.nextToken();
	    		String optionATxt = finalQuestionStrTokenizer.nextToken();
	    		String optionBTxt = finalQuestionStrTokenizer.nextToken();
	    		String optionCTxt = finalQuestionStrTokenizer.nextToken();
	    		String optionDTxt = finalQuestionStrTokenizer.nextToken();
	    		
	    		StringBuffer celebrityIdSet = new StringBuffer();
		    	for (String celebrityName : perMovieCelebrityNames) {
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
		    	System.out.println("qCount :" + qCount);
	    	}
		}
		
		for (int i = 0; i < celebrityNames.size(); i++) {
			String str = String.valueOf(i + 1);
			System.out.println(str);
			System.out.println(celebrityNames.get(i));
		}
	}
	
	private static int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
}
