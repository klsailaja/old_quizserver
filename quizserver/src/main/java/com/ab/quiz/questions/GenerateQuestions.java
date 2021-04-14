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
	private static List<String> questionsList = new ArrayList<>();
	private static List<String> extraOptionsList = new ArrayList<>();
	private static Map<String,List<String>> extraOptionsMap = new HashMap<>();
	
	private static HashMap<String,String> map = new HashMap<>();
	
	private static void formCelebrity(String celebrityName) {
		if (!celebrityNames.contains(celebrityName)) {
			celebrityNames.add(celebrityName);
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

	
	private static List<String> getFormedQuestions(boolean isCelebrity, String tokensList, String prefixStr, String categoryStr) {
		
		tokensList = tokensList.trim();
		//System.out.println("tokensList :" + tokensList);
		List<String> questions = new ArrayList<>();
		List<String> tokens = getStrTokens(tokensList);
		/*for (String pstr : tokens) {
			System.out.println("pstr :" + pstr);
		}
		System.out.println(tokens.toString());*/
		
		
		int totalTokensCt = 4;
		if (categoryStr.equals("a")) {
			totalTokensCt = 7;
		}
		
		int fillCt = totalTokensCt - tokens.size();
		List<String> extraOptionsList = extraOptionsMap.get(categoryStr);
		
		if (extraOptionsList != null) {
			int size = extraOptionsList.size();
			//System.out.println("categoryStr :" + categoryStr);
			//System.out.println("size :" + size);
			//System.out.println("tokens :" + tokens.size());
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
	    				List<String> categoryQuestions = getFormedQuestions(true, str, "H", "b");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "c": {
	    				// Heroines category
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(true, str, "HR", "c");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "d": {
	    				List<String> categoryQuestions = getFormedQuestions(false, "", "H", "d");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "e": {
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(true, str, "D", "e");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "f": {
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(true, str, "MD", "f");
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "g": {
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(true, str, "V", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "h": {
	    				String relatedChar = strTokenizer2.nextToken();
	    				String relation = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				
	    				map.put("%RELATION_CHAR%", relatedChar);
	    				map.put("%RELATION%", relation);
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "RL", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "i": {
	    				String relatedChar = strTokenizer2.nextToken();
	    				String relation = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				
	    				map.put("%RELATION_CHAR%", relatedChar);
	    				map.put("%RELATION%", relation);
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "RN", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "j": {
	    				String artistName = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%RELATION_CHAR%", artistName);
	    				formCelebrity(artistName);	
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "PERU", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "k": {
	    				String artistName = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%RELATION_CHAR%", artistName);
	    				formCelebrity(artistName);	
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "PR", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "l": {
	    				String artistName1 = strTokenizer2.nextToken();
	    				String artistName2 = strTokenizer2.nextToken();
	    				map.put("%ARTIST1%", artistName1);
	    				map.put("%ARTIST2%", artistName2);
	    				formCelebrity(artistName1);
	    				formCelebrity(artistName2);
	    				List<String> categoryQuestions = getFormedQuestions(false, "", "", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "m": {
	    				String str = strTokenizer2.nextToken();
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "SG", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "n": {
	    				String songLyrics = strTokenizer2.nextToken();
	    				String str = strTokenizer2.nextToken();
	    				map.put("%FILLSONG%", songLyrics);
	    				List<String> categoryQuestions = getFormedQuestions(false, str, "FS", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "o": {
	    				String artistType = strTokenizer2.nextToken();
	    				String artistList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistType);
	    				List<String> categoryQuestions = getFormedQuestions(true, artistList, "SSH", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "p": {
	    				String artistName = strTokenizer2.nextToken();
	    				String songList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistName);
	    				List<String> categoryQuestions = getFormedQuestions(false, songList, "SS", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    			case "q": {
	    				String artistType = strTokenizer2.nextToken();
	    				String artistList = strTokenizer2.nextToken();
	    				map.put("%SS_ARTIST%", artistType);
	    				List<String> categoryQuestions = getFormedQuestions(true, artistList, "GR", categoryStr);
	    				finalQuestions.addAll(categoryQuestions);
	    				break;
	    			}
	    		}
	    	}
	    	
	    	List<Integer> celebrityIds = new ArrayList<>();
	    	for (String celebrityName : celebrityNames) {
    			int id = 2 + celebrityNames.indexOf(celebrityName);
    			celebrityIds.add(id);
    		}
	    	//System.out.println(celebrityIds);
	    	
	    	//initialize LCM and GCD with the first element
		    long lcm = celebrityIds.get(0);
		    long gcd = celebrityIds.get(0);
		 
		    //loop through the array to find GCD
		    //use GCD to find the LCM
		    for(int i=1; i<celebrityIds.size(); i++){
		      gcd = findGCD(celebrityIds.get(i), lcm);
		      lcm = (lcm*celebrityIds.get(i))/gcd;
		    }
		    
		    //output the LCM
		    //System.out.println("LCM: "+lcm);
	    	
	    	for (String lineQuestion : finalQuestions) {
	    		StringBuffer strBuffer = new StringBuffer(lineQuestion);
	    		strBuffer.append(":");
	    		strBuffer.append("1");
	    		strBuffer.append(":");
	    		strBuffer.append(lcm);
	    		strBuffer.append(":");
	    		strBuffer.append("-1");
	    		
	    		//System.out.println(strBuffer.toString());
	    		
	    		//Use try-with-resource to get auto-closeable writer instance
	    	    writer.append(strBuffer.toString());
	    	    writer.append("\n");
	    	    qCount++;
		    	System.out.println("qCount :" + qCount);
	    	}
		}
		
		for (int i = 0; i < celebrityNames.size(); i++) {
			String str = String.valueOf(i + 2);
			System.out.println(str);
			System.out.println(celebrityNames.get(i));
		}
	}
	
	public static long findGCD(long a, long b) {
	   // base condition
	    if (b == 0)
	      return a;
	    return findGCD(b, a%b);
	  }
	
	
	private static int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
}