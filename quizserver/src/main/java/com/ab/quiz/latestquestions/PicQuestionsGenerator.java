package com.ab.quiz.latestquestions;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ab.quiz.db.QuestionPicsDBHandler;

public class PicQuestionsGenerator {
	
	private static List<MovieInfo> moviesDataBase = new ArrayList<>();
	private static List<String> celebrityNames = new ArrayList<>();
	private static List<String> questionsList = new ArrayList<>();
	private static Map<String,List<String>> extraOptionsMap = new HashMap<>();
	
	private static HashMap<String,String> map = new HashMap<>();
	private static List<String> finalQuestions = new ArrayList<>();
	private static List<String> perMovieCelebrityNames = new ArrayList<>();
	private static TreeSet<String> allArtistNames = new TreeSet<String>();
	
	private static int CONSIDER_HERO = 1;
	private static int CONSIDER_HEROINE = 2;
	private static int CONSIDER_BOTH = 3;
	
	
	public static void main(String[] args) throws Exception {
		
		String movieInputPath = "D:\\Projects\\Games\\PicsBased\\PicMovieDetails.xlsx";
		
		fillupMoviesDB(movieInputPath);
		
		String celebritiesInputPath = "D:\\Projects\\Games\\PicsBased\\PicCelebrities.txt";
		formCelebrityList(celebritiesInputPath);
		
		String questionListFileName = "D:\\Projects\\Games\\PicsBased\\PicMovieQuestionsTemplate.txt";
		formQuestionList(questionListFileName);
		
		String extraOptionsFileName = "D:\\Projects\\Games\\PicsBased\\PicMovieExtraOptions.txt";
		formExtraOptions(extraOptionsFileName);
		
		fillupAnswers();
		
		formQuestions();
		
		//System.out.println(allArtistNames.size());
		/*for (String artistName : allArtistNames) {
			System.out.println(artistName);
		}*/
		//printAll();
	}
	
	private static void fillupMoviesDB(String fileName) throws Exception {
		
		List<String> fileInputLines = readFromInputFile(fileName);
		
		int uniqueId = 0;
		
		for (String line : fileInputLines) {
        	line = line.trim();
        	if (line.length() == 0) {
        		continue;
        	}
        	
        	if (line.startsWith("//")) {
        		continue;
        	}

        	MovieInfo movieInfo = new MovieInfo();
        	movieInfo.setId(++uniqueId);
        	
        	StringTokenizer strTokenizer1 = new StringTokenizer(line, ";");
        	while (strTokenizer1.hasMoreTokens()) {
	    		String eachCategoryStr = strTokenizer1.nextToken();
	    		eachCategoryStr = eachCategoryStr.trim();
	    		StringTokenizer strTokenizer2 = new StringTokenizer(eachCategoryStr, "-");
	    		String categoryStr = strTokenizer2.nextToken();
	    		categoryStr = categoryStr.trim();
	    		Category category = null;
	    		
	    		switch(categoryStr) {
	    			case "img": {
	    				// Images Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "a": {
	    				// Movie name Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "b": {
	    				// Hero Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    			case "c": {
	    				// Heroine Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
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
	    			case "s": {
	    				// Music director Category
	    				category = buildCategory(strTokenizer2, 1, categoryStr);
	    				break;
	    			}
	    		}
	    		if (category == null) {
	    			continue;
	    		}
	    		movieInfo.addCategory(category);
        	}
        	moviesDataBase.add(movieInfo);
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
	
	private static List<String> readFromInputFile(String fileName) {
		
		List<String> fileContents = new ArrayList<>();
		
		try  
		{  
			File file = new File(fileName);  
			FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file  
			//creating Workbook instance that refers to .xlsx file  
			XSSFWorkbook wb = new XSSFWorkbook(fis);   
			XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object  
			Iterator<Row> itr = sheet.iterator();    //iterating over excel file  
			while (itr.hasNext())                 
			{  
				Row row = itr.next();
				StringBuffer lineStrBuffer = new StringBuffer(); 
				Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column  
				while (cellIterator.hasNext())   
				{  
					Cell cell = cellIterator.next();
					lineStrBuffer.append(cell.getStringCellValue());
					lineStrBuffer.append(";");
				}
				fileContents.add(lineStrBuffer.toString());
			}  
		}  
		catch(Exception e)  
		{  
			e.printStackTrace();  
		}
		return fileContents;
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
	
	private static List<String> getStrTokens(String str) {
		List<String> tokenList = new ArrayList<>();
		
		StringTokenizer strTokenizer = new StringTokenizer(str, ",");
		while (strTokenizer.hasMoreTokens()) {
			tokenList.add(strTokenizer.nextToken().trim());
		}
		return tokenList;
	}
	
	private static void printAll() {
		for (MovieInfo movieInfo : moviesDataBase) {
			List<Category> categoryList = movieInfo.getAllCategories();
			
			for (Category category : categoryList) {
				System.out.println(category);
			}
		}
	}
	
	private static void fillupAnswers() {
		
		for (MovieInfo movieInfo : moviesDataBase) {
			map.clear();
			List<Category> categoryList = movieInfo.getAllCategories();
			
			for (Category category : categoryList) {
				if (category.getCategoryName().equals("img")) {
					String imgCateory = category.getValue(0);
					if (imgCateory.equals("HERO_DRAMA")) {
						String heroIndexStr = null;
						int heroIndex = 1;
						if (category.getCategoryFieldsSize() > 2) {
							heroIndexStr = category.getValue(2);
							heroIndex = Integer.parseInt(heroIndexStr);
						}
						fillACategory(movieInfo.getId(), categoryList, heroIndex, 1, CONSIDER_HERO, category);
						fillCCategory(movieInfo.getId(), categoryList, heroIndex, 1, CONSIDER_HERO, category);
						fillECategory(movieInfo.getId(), categoryList, heroIndex, 1, CONSIDER_HERO, category);
						fillFCategory(movieInfo.getId(), categoryList, heroIndex, 1, CONSIDER_HERO, category);
					} else if (imgCateory.equals("HEROINE_DRAMA")) {
						String heroineIndexStr = null;
						int heroineIndex = 1;
						if (category.getCategoryFieldsSize() > 2) {
							heroineIndexStr = category.getValue(2);
							heroineIndex = Integer.parseInt(heroineIndexStr);
						}
						fillACategory(movieInfo.getId(), categoryList, 1, heroineIndex, CONSIDER_HEROINE, category);
						fillBCategory(movieInfo.getId(), categoryList, 1, heroineIndex, CONSIDER_HEROINE, category);
						fillECategory(movieInfo.getId(), categoryList, 1, heroineIndex, CONSIDER_HEROINE, category);
						fillFCategory(movieInfo.getId(), categoryList, 1, heroineIndex, CONSIDER_HEROINE, category);
					} else if (imgCateory.equals("OTHERS_DRAMA")) {
						fillACategory(movieInfo.getId(), categoryList, 1, 1, CONSIDER_BOTH, category);
						fillBCategory(movieInfo.getId(), categoryList, 1, 1, CONSIDER_HEROINE, category);
						fillCCategory(movieInfo.getId(), categoryList, 1, 1, CONSIDER_HERO, category);
						fillECategory(movieInfo.getId(), categoryList, 1, 1, CONSIDER_BOTH, category);
						fillFCategory(movieInfo.getId(), categoryList, 1, 1, CONSIDER_BOTH, category);
					} else if (imgCateory.equals("SONG")) {
						String heroIndexStr = null;
						int heroIndex = 1;
						String heroineIndexStr = null;
						int heroineIndex = 1;
						int correctSongAnswer = 1;
						
						if (category.getCategoryFieldsSize() <= 3) {
							String correctSongAnswerStr = category.getValue(2);
							correctSongAnswer = Integer.parseInt(correctSongAnswerStr);
						}
						if (category.getCategoryFieldsSize() > 3) {
							heroIndexStr = category.getValue(3);
							heroIndex = Integer.parseInt(heroIndexStr);
							heroineIndexStr = category.getValue(4);
							heroineIndex = Integer.parseInt(heroineIndexStr);
						}
						fillACategory(movieInfo.getId(), categoryList, heroIndex, heroineIndex, CONSIDER_BOTH, category);
						fillECategory(movieInfo.getId(), categoryList, heroIndex, heroineIndex, CONSIDER_BOTH, category);
						fillFCategory(movieInfo.getId(), categoryList, heroIndex, heroineIndex, CONSIDER_BOTH, category);
						fillSCategory(movieInfo.getId(), categoryList, correctSongAnswer, heroIndex, heroineIndex, CONSIDER_BOTH, category);
					}
				}
			}
		}
	}
	
	private static void fillACategory(int currentId, 
			List<Category> allCategoryList, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "a";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputSingleObject.getValue(0);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			if (movieInfo.getId() == currentId) {
				continue;
			}
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("a", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("a", answers1List);
	}
	
	private static void fillBCategory(int currentId, 
			List<Category> allCategoryList, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "b";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		if (outputCategoryList.size() > 1) {
			return;
		}
		
		Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputSingleObject.getValue(0);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			if (movieInfo.getId() == currentId) {
				continue;
			}
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("b", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("b", answers1List);
	}
	
	private static void fillCCategory(int currentId, 
			List<Category> allCategoryList, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "c";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		if (outputCategoryList.size() > 1) {
			return;
		}
		
		Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputSingleObject.getValue(0);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			if (movieInfo.getId() == currentId) {
				continue;
			}
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("c", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("c", answers1List);
	}
	
	private static void fillECategory(int currentId, 
			List<Category> allCategoryList, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "e";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputSingleObject.getValue(0);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			if (movieInfo.getId() == currentId) {
				continue;
			}
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("e", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("e", answers1List);
	}
	
	private static void fillFCategory(int currentId, 
			List<Category> allCategoryList, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "f";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputSingleObject.getValue(0);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			if (movieInfo.getId() == currentId) {
				continue;
			}
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("f", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("f", answers1List);
	}
	
	private static void fillSCategory(int currentId, 
			List<Category> allCategoryList, int correctSongIndex, int heroIndex, int herioneIndex, 
			int optionsType, Category imgCategoryObj) {
		
		String inputHeroCategory = "b";
		String inputHeroineCategory = "c";
		String outputCategory = "s";
		
		List<Category> inputHeroList = getCategoryList(inputHeroCategory, allCategoryList);
		List<Category> inputHeroineList = getCategoryList(inputHeroineCategory, allCategoryList);
		List<Category> outputCategoryList = getCategoryList(outputCategory, allCategoryList);
		
		//Category outputSingleObject = outputCategoryList.get(0);
		
		List<String> currentHeroMergedList = Utils.getMergedList(0, inputHeroList);
		List<String> currentHeroineMergedList = Utils.getMergedList(0, inputHeroineList);
		List<String> outputMergedList = Utils.getMergedList(0, outputCategoryList);
		
		String currentHeroName = currentHeroMergedList.get(0);
		String currentHeroineName = currentHeroineMergedList.get(0);
		
		String currentOutputValue = outputMergedList.get(correctSongIndex - 1);
		
		if (optionsType == CONSIDER_HERO) {
			// Hero name matching..
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
		} else if (optionsType == CONSIDER_HEROINE) {
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		} else if (optionsType == CONSIDER_BOTH) {
			if (heroIndex <= currentHeroMergedList.size()) {
				currentHeroName = currentHeroMergedList.get(heroIndex - 1);
			}
			if (herioneIndex <= currentHeroMergedList.size()) {
				currentHeroineName = currentHeroineMergedList.get(herioneIndex - 1);
			}
		}
		
		List<String> answers1List = new ArrayList<>();
		
		List<String> wrongAnswers1 = new ArrayList<>();
		
		answers1List.add(currentOutputValue);
		wrongAnswers1.addAll(answers1List);
		
		for (MovieInfo movieInfo : moviesDataBase) {
			List<Category> miMoviesList = movieInfo.getCategoryList(outputCategory);
			if (miMoviesList.size() == 0) {
				continue;
			}
			if (movieInfo.getId() == currentId) {
				for (Category sCategory : miMoviesList) {
					if (!sCategory.getValue(0).equals(currentOutputValue)) {
						wrongAnswers1.add(sCategory.getValue(0));
					}
				}
			}
			Category miMovieObject = miMoviesList.get(0);
			String miMovieName = miMovieObject.getValue(0);
			
			List<Category> miHeroNameList = movieInfo.getCategoryList(inputHeroCategory);
			List<String> miHeroNamesMergedList = Utils.getMergedList(0, miHeroNameList);
			
			List<Category> miHeroineNameList = movieInfo.getCategoryList(inputHeroineCategory);
			List<String> miHeroineNamesMergedList = Utils.getMergedList(0, miHeroineNameList);

	
			boolean heroMatched = miHeroNamesMergedList.contains(currentHeroName);
			boolean heroineMatched = miHeroineNamesMergedList.contains(currentHeroineName);
			
			if (optionsType == 1) {
				if (heroMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else if (optionsType == 2) {
				if (heroineMatched) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			} else {
				if ((heroMatched) || (heroineMatched)) {
					if (!wrongAnswers1.contains(miMovieName)) {
						wrongAnswers1.add(miMovieName);
					}
				}
			}
		}
			
		List<String> finalWongAnswer1 = fillupWrongAnswers("s", wrongAnswers1);
		finalWongAnswer1.removeAll(answers1List);
		int fillGap = 8 - answers1List.size();
		for (int index = 0; index < fillGap; index++) {
			answers1List.add(finalWongAnswer1.get(index));
		}
		wrongAnswers1.clear();
		finalWongAnswer1.clear();
		
		imgCategoryObj.addAnswersToMap("s", answers1List);
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
	
	private static int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
	
	private static void fillVarsValsMap(String prefix, List<String> values) {
		
		for (int index = 0; index < values.size(); index ++) {
			String key = "%" + prefix + (index + 1) + "%";
			map.put(key, values.get(index));
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
			String prefix1, List<String> answers1, long picId) {
		
		List<String> questions = new ArrayList<>();
		
		fillVarsValsMap(prefix1, answers1);
		//fillVarsValsMap(prefix2, answers2);
		
		List<String> ques = getRelevantQuestions(categoryStr);
		
		for (String eachQues : ques) {
			Set<Entry<String,String>> mapSet = map.entrySet();
			for (Map.Entry<String, String> entry : mapSet) {
				String key = entry.getKey();
				String value = entry.getValue();
				eachQues = eachQues.replaceAll(key, value);
			}
			eachQues = eachQues + ":" + picId;
			questions.add(eachQues);
		}
		return questions;
	}
	
	private static void addToLocalCelebrityList(String artistName) {
		if (!perMovieCelebrityNames.contains(artistName)) {
			perMovieCelebrityNames.add(artistName);
		}
	}
	
	private static void formQuestions() throws Exception {
		
		// Get the file reference
    	Path path = Paths.get("D:\\Projects\\Games\\PicsBased\\Pics.txt");
    	int qCount = 0;
    	BufferedWriter writer = Files.newBufferedWriter(path);
    	
    	long pictureId = -1;
		
		for (MovieInfo movieInfo : moviesDataBase) {
			map.clear();
        	finalQuestions.clear();
        	perMovieCelebrityNames.clear();
        	int movieDifficultLevel = 1;
        	
			for (Category category : movieInfo.getAllCategories()) {
				switch(category.getCategoryName()) {
					case "a": {
						if (category.getCategoryFieldsSize() > 1) {
							String diffLevelStr = category.getValue(1);
							movieDifficultLevel = Integer.parseInt(diffLevelStr);
						} else {
							movieDifficultLevel = 1;
						}
						//System.out.println("movieDifficultLevel:" + movieDifficultLevel);
						break;
					}
					case "img": {
						Map<String,List<String>> mapAnswers = category.getMapAnswers();
						
						String imgFilePath = category.getValue(1);
						//System.out.println(imgFilePath);
						File imgFileObj = new File(imgFilePath);
						String fileName = imgFileObj.getName();
						
						if (fileName.indexOf(".") > 0) {
							fileName = fileName.substring(0, fileName.lastIndexOf("."));
						} 
						
	    				BufferedImage bImage = ImageIO.read(imgFileObj);
	    			    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    			    ImageIO.write(bImage, "jpg", bos );
	    			    byte [] data = bos.toByteArray();
	    				pictureId = QuestionPicsDBHandler.getInstance().createPictureDBEntry(fileName, data);
						
						// a category
						List<String> results = mapAnswers.get("a");
						List<String> categoryQuestions = null;
						
						if (results != null) {
							categoryQuestions = getFormedQuestions("a", "MV", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
						}
						
						// b category
						results = mapAnswers.get("b");
						if (results != null) {
							addToLocalCelebrityList(results.get(0));
							categoryQuestions = getFormedQuestions("b", "H", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
						}
	    				
	    				// c category
	    				results = mapAnswers.get("c");
	    				if (results != null) {
							addToLocalCelebrityList(results.get(0));
							categoryQuestions = getFormedQuestions("c", "HR", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
	    				}
	    				
	    				// e category
	    				results = mapAnswers.get("e");
	    				if (results != null) {
							addToLocalCelebrityList(results.get(0));
							categoryQuestions = getFormedQuestions("e", "D", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
	    				}
	    				
	    				// f category
	    				results = mapAnswers.get("f");
	    				if (results != null) { 
							addToLocalCelebrityList(results.get(0));
							categoryQuestions = getFormedQuestions("f", "MD", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
	    				}
	    				
	    				// s category
	    				results = mapAnswers.get("s");
	    				if (results != null) {
							categoryQuestions = getFormedQuestions("s", "SG", results, pictureId);
		    				finalQuestions.addAll(categoryQuestions);
	    				}
						break;
					}
				}
			}
			
			//System.out.println("movieDifficultLevel11:" + movieDifficultLevel);
			String sqlQry = "INSERT INTO QUIZQUESTIONS (NSTATEMENT,NOPTIONA,NOPTIONB,NOPTIONC,NOPTIOND,NOPTIONE,NOPTIONF,NOPTIONG,NOPTIONH,CORRECTOPTION,PICID,CATEGORY,DIFF_LEVEL) VALUES('";
			List<String> mixedModeCategories = new ArrayList<>();
			mixedModeCategories.add("a");
			mixedModeCategories.add("b");
			mixedModeCategories.add("c");
			mixedModeCategories.add("e");
			mixedModeCategories.add("f");
			
			List<String> personNamesCategories = new ArrayList<>();
			personNamesCategories.add("b");
			personNamesCategories.add("c");
			personNamesCategories.add("e");
			personNamesCategories.add("f");
			
	    	for (String lineQuestion : finalQuestions) {
	    		StringTokenizer finalQuestionStrTokenizer = new StringTokenizer(lineQuestion, ":");
	    		String categoryNameStr = finalQuestionStrTokenizer.nextToken();
	    		
	    		boolean skip = true;
	    		/*if (categoryNameStr.indexOf("1") > -1) {
	    			System.out.println("Raj:" + lineQuestion);	    			
	    			skip = true;
	    		}*/
	    		
	    		String questionTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionATxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionBTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionCTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionDTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionETxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionFTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionGTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String optionHTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		String picIdTxt = finalQuestionStrTokenizer.nextToken().trim();
	    		
	    		if (lineQuestion.indexOf("%") > -1) {
	    			System.err.println("Question not proper: " + lineQuestion);
	    		}
	    		
	    		if ((optionATxt.indexOf("%") > -1) || ((optionBTxt.indexOf("%") > -1)) || ((optionCTxt.indexOf("%") > -1))
	    		 || (((optionDTxt.indexOf("%") > -1)))) {
	    			System.err.println("Question not proper: " + lineQuestion);
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
	    			System.err.println("Check for duplicates" + lineQuestion);
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
	    		
	    		//int randomSeed = getRandomNumber(1,10);
	    		long picId = -1;
	    		try {
	    			picId = Long.parseLong(picIdTxt);
	    		} catch(NumberFormatException ex) {
	    			picId = -1;
	    		}
	    		/*if ((randomSeed % 2) == 0) {
	    			picId = randomSeed;
	    		} else {
	    			if (randomSeed > 5) {
	    				picId = 1;
	    			} else {
	    				picId = randomSeed;
	    			}
	    		}*/
	    		strBuffer.append(picId);
	    		strBuffer.append(",");
	    		strBuffer.append(celebrityIdSetStr);
	    		strBuffer.append(",");
	    		strBuffer.append(movieDifficultLevel);
	    		strBuffer.append(");");
	    		
	    		//System.out.println(strBuffer.toString());
	    		
	    		//Use try-with-resource to get auto-closeable writer instance
	    	    writer.append(strBuffer.toString());
	    	    writer.append("\n");
	    	    writer.flush();
	    	    qCount++;
		    	//System.out.println("qCount :" + qCount);
	    	}
		}
	}
}
