package com.ab.quiz.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.Question;

/*
CREATE TABLE QUIZQUESTIONS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		NSTATEMENT VARCHAR(200) NOT NULL,
		NOPTIONA VARCHAR(100) NOT NULL,
		NOPTIONB VARCHAR(100) NOT NULL,
		NOPTIONC VARCHAR(100) NOT NULL,
		NOPTIOND VARCHAR(100) NOT NULL,
		NOPTIONE VARCHAR(100) NOT NULL,
		NOPTIONF VARCHAR(100) NOT NULL,
		NOPTIONG VARCHAR(100) NOT NULL,
		NOPTIONH VARCHAR(100) NOT NULL, 
		CORRECTOPTION INT,
		PICID BIGINT DEFAULT(-1), 
        CATEGORY SET('1','2','3','4','5','6','7','8','9','10',
        '11','12','13','14','15','16','17','18','19','20',
        '21','22','23','24','25','26','27','28','29','30',
        '31','32','33','34','35','36','37','38','39','40', 
        '41','42','43','44','45','46','47','48','49','50',
        '51','52','53','54','55','56','57','58','59','60',
        '61','62','63','64'
        )NOT NULL, PRIMARY KEY (ID)) ENGINE = INNODB;
*/

public class QuestionDBHandler {
	private static String TABLE_NAME = "QUIZQUESTIONS";
	
	//private static String ID = "ID";
	private static String NSTATEMENT = "NSTATEMENT";
	private static String CATEGORY = "CATEGORY";
	private static String TIMELINE = "TIMELINE";
	private static String NOPTION_A = "NOPTIONA";
	private static String NOPTION_B = "NOPTIONB";
	private static String NOPTION_C = "NOPTIONC";
	private static String NOPTION_D = "NOPTIOND";
	private static String NOPTION_E = "NOPTIONE";
	private static String NOPTION_F = "NOPTIONF";
	private static String NOPTION_G = "NOPTIONG";
	private static String NOPTION_H = "NOPTIONH";
	private static String CORRECTOPTION = "CORRECTOPTION";
	private static String PICID = "PICID";
	
	private static final String CREATE_QUESTION_ENTRY = "INSERT INTO " + TABLE_NAME   
			+ "(" + NSTATEMENT + "," + NOPTION_A + "," + NOPTION_B + "," + NOPTION_C + ","
			+ NOPTION_D + "," + NOPTION_E + "," + NOPTION_F + "," + NOPTION_G + "," + NOPTION_H + ","
			+ CORRECTOPTION + ","
			+ PICID + "," + 
			CATEGORY + "," + TIMELINE + ") VALUES"
			+ "(?,?,?,?,?,?,?,?,?)";
	/*private static final String GET_QUESTION_ENTRY_SET = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + ID + " IN (?,?,?,?,?,?,?,?,?,?,?)";*/
	private static final String GET_QUESTIONS_BY_RANDOM = "SELECT * FROM " + TABLE_NAME
			+ " ORDER BY RAND() LIMIT 11";
	/*private static final String GET_QUESTIONS_RANDOM_CELEBRITY = "SELECT * FROM " +
			TABLE_NAME + " WHERE MOD(" + CATEGORY + ",?) = 0 ORDER BY RAND() LIMIT 11";*/
	private static final String GET_QUESTIONS_RANDOM_CELEBRITY = "SELECT * FROM " + 
			TABLE_NAME + " WHERE FIND_IN_SET(?," + CATEGORY + ") > 0 ORDER BY RAND() LIMIT 9";
	private static final String GET_QUESTIONS_RANDOM_CELEBRITY_PIC = "SELECT * FROM " + 
			TABLE_NAME + " WHERE FIND_IN_SET(?," + CATEGORY + ") > 0 AND " + PICID + "> -1 ORDER BY RAND() LIMIT 3";
	 
			
	
	private static final Logger logger = LogManager.getLogger(QuestionDBHandler.class);
	private static QuestionDBHandler instance = null;
	private String pictureQuestionPrefix = null;
	
	private QuestionDBHandler() {
	}
	
	public static QuestionDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In QuestionDBHandler getInstance() method instance created");
			instance = new QuestionDBHandler();
			try {
				instance.pictureQuestionPrefix = UsefulInfoDBHandler.getInstance().getPicQuestionPrefix();
			} catch (SQLException e) {
				instance.pictureQuestionPrefix = "Picture based question. Click here to view";
			}
		}
		return instance;
	}
	
	public void createQuestionsInBulk(List<Question> questionsList, int batchSize) throws SQLException {
		
		System.out.println("questions.size() :" + questionsList.size());
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_QUESTION_ENTRY);
			
			int index = 0;
			int totalFailureCount = 0;
			int totalSuccessCount = 0;
			for (Question question : questionsList) {
				
				ps.setString(1, question.getnStatement());
				ps.setString(2, question.getnOptionA());
				ps.setString(3, question.getnOptionB());
				ps.setString(4, question.getnOptionC());
				ps.setString(5, question.getnOptionD());
				ps.setInt(6, question.getCorrectOption());
				ps.setLong(7, question.getCategory());
				ps.setInt(8, question.getTimeLine());
			
				ps.addBatch();
				index++;
				
				if (index % batchSize == 0) {
					int results[] = ps.executeBatch();
					dbConn.setAutoCommit(false);
					dbConn.commit();
					index = 0;
					for (int result : results) {
						if (result == 1) {
							++totalSuccessCount;
						} else {
							++totalFailureCount;
						}
					}
				}
			}
			if (index > 0) {
				int results[] = ps.executeBatch();
				dbConn.setAutoCommit(false);
				dbConn.commit();
				for (int result : results) {
					if (result == 1) {
						++totalSuccessCount;
					} else {
						++totalFailureCount;
					}
				}
			}
			logger.info("questions creation in bulk with success row count {} : failure row count {}", 
					totalSuccessCount, totalFailureCount);
		} catch(SQLException ex) {
			logger.error("Error creating questions in bulk mode", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
	}
	
	private List<Question> queryQuestions(String sqlQry, int category) throws SQLException {
		
		List<Question> questionSet = new ArrayList<>();
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(sqlQry);
		ResultSet rs = null;
		
		if (category != -1) {
			ps.setString(1, String.valueOf(category));
		}
		
		int qNo = 1;
		
		try {
			rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Question question = new Question();
					
					question.setQuestionNumber(qNo++);
					question.setnStatement(rs.getString(NSTATEMENT));
					
					
					String optionA = rs.getString(NOPTION_A);
					question.setnOptionA(optionA);
					
					String optionB = rs.getString(NOPTION_B);
					String optionC = rs.getString(NOPTION_C);
					String optionD = rs.getString(NOPTION_D);
					String optionE = rs.getString(NOPTION_E);
					String optionF = rs.getString(NOPTION_F);
					String optionG = rs.getString(NOPTION_G);
					String optionH = rs.getString(NOPTION_H);
					question.setCorrectOption(rs.getInt(CORRECTOPTION));
					long picId = rs.getLong(PICID);
					if (picId > -1) {
						if (category != -1) {
							byte[] picBytes = QuestionPicsDBHandler.getInstance().getPictureFileContents(picId);
							question.setPictureBytes(picBytes);
						}
					}
					
					TreeSet<Integer> uniqueValues = new TreeSet<>();
					
					while (uniqueValues.size() < 3) {
						int wrongOption = getRandomNumber(2,8);
						while (uniqueValues.contains(wrongOption)) {
							wrongOption = getRandomNumber(2,8);
						}
						uniqueValues.add(wrongOption);
					}
					
					List<String> wrongOptions = new ArrayList<>();
					for (Integer wrongVal : uniqueValues) {
						switch(wrongVal) {
							case 2: {
								wrongOptions.add(optionB);
								break;
							}
							case 3: {
								wrongOptions.add(optionC);
								break;
							}
							case 4: {
								wrongOptions.add(optionD);
								break;
							}
							case 5: {
								wrongOptions.add(optionE);
								break;
							}
							case 6: {
								wrongOptions.add(optionF);
								break;
							}
							case 7: {
								wrongOptions.add(optionG);
								break;
							}
							case 8: {
								wrongOptions.add(optionH);
								break;
							}
						}
					}
					
					question.setnOptionB(wrongOptions.get(0));
					question.setnOptionC(wrongOptions.get(1));
					question.setnOptionD(wrongOptions.get(2));
	
					
					int randomVal = getRandomNumber(1,5);
					switch (randomVal) {
						case 1: {
							break;
						}
						case 2: {
							String temp = question.getnOptionB();
							question.setnOptionB(question.getnOptionA());
							question.setnOptionA(temp);
							question.setCorrectOption(2);
							break;
						}
						case 3: {
							String temp = question.getnOptionC();
							question.setnOptionC(question.getnOptionA());
							question.setnOptionA(temp);
							question.setCorrectOption(3);
							break;
						}
						case 4: {
							String temp = question.getnOptionD();
							question.setnOptionD(question.getnOptionA());
							question.setnOptionA(temp);
							question.setCorrectOption(4);
							break;
						}
					}
					questionSet.add(question);
				}
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getRandomQues()", ex);
			throw ex;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return questionSet;
	}
	
	public List<Question> getRandomPicBasedQues(int category) throws SQLException {
		
		List<Question> picQuestionSet = new ArrayList<>();
		
		if (category != -1) {
			String psSql = GET_QUESTIONS_RANDOM_CELEBRITY_PIC;
			List<Question> set = queryQuestions(psSql, category);
			for (Question question : set) {
				String questionStatement = question.getnStatement(); 
				question.setnStatement(pictureQuestionPrefix + " " + questionStatement);
			}
			picQuestionSet.addAll(set);
		}
		return picQuestionSet;
	}
	
	
	public List<Question> getRandomQues(int category) throws SQLException {
		
		List<Question> questionSet = new ArrayList<>();
		
		String psSql = GET_QUESTIONS_BY_RANDOM;
		
		if (category != -1) {
			psSql = GET_QUESTIONS_RANDOM_CELEBRITY;
		}
		
		List<Question> set = queryQuestions(psSql, category);
		questionSet.addAll(set);
		return questionSet;
	}
	
	private int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
	
	/*
	public List<Question> getQuestionSet(Long[] ids) throws SQLException {
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		
		PreparedStatement ps = dbConn.prepareStatement(GET_QUESTION_ENTRY_SET);
		ps.setLong(1, ids[0]);
		ps.setLong(2, ids[1]);
		ps.setLong(3, ids[2]);
		ps.setLong(4, ids[3]);
		ps.setLong(5, ids[4]);
		ps.setLong(6, ids[5]);
		ps.setLong(7, ids[6]);
		ps.setLong(8, ids[7]);
		ps.setLong(9, ids[8]);
		ps.setLong(10, ids[9]);
		ps.setLong(11, ids[10]);
		
		List<Question> questionSet = new ArrayList<>(11);
		int qNo = 1;
		
		try {
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					Question question = new Question();
					
					question.setQuestionNumber(qNo++);
					question.setnStatement(rs.getString(NSTATEMENT));
					question.setnOptionA(rs.getString(NOPTION_A));
					question.setnOptionB(rs.getString(NOPTION_B));
					question.setnOptionC(rs.getString(NOPTION_C));
					question.setnOptionD(rs.getString(NOPTION_D));
					question.setCorrectOption(rs.getInt(CORRECTOPTION));
					
					questionSet.add(question);
				}
				rs.close();
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getQuestionSet()", ex);
			throw ex;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (dbConn != null) {
				dbConn.close();
			}
		}
		return questionSet;
	}*/
	
	public static void main(String[] args) throws Exception {
		System.out.println("Start");
		//readTextFile();
		//UserProfileDBHandler.main(args);
		//MyTransactionDBHandler.main(args);
		//GameHistoryDBHandler.main(args);
		System.out.println("End");
	}
	
	public static void readTextFile() {
		
		String file = "D:\\Projects\\Games\\Test1.txt";
		Path path = Paths.get(file);
        try {
        	
            // Java 8
            List<String> list = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Question> questions = new ArrayList<>();
            
            for (String line : list) {
            	line = line.trim();
            	if (line.length() == 0) {
            		continue;
            	}
            	
            	//System.out.println("line2 : " + line);
    	    	StringTokenizer strTokenizer = new StringTokenizer(line, ":");
    	    	
    	    	strTokenizer.nextToken().trim();
    	    	String statement = strTokenizer.nextToken().trim();
    	    	String optionA = strTokenizer.nextToken().trim();
    	    	String optionB = strTokenizer.nextToken().trim();
    	    	String optionC = strTokenizer.nextToken().trim();
    	    	String optionD = strTokenizer.nextToken().trim();
    	    	String correctOptionStr = strTokenizer.nextToken().trim();
    	    	int correctOption = Integer.parseInt(correctOptionStr);
    	    	String categoryStr = strTokenizer.nextToken().trim();
    	    	String timeLineStr = strTokenizer.nextToken().trim();
    	    	int timeLineInt = Integer.parseInt(timeLineStr);

    	    	Question question = new Question();
    	    	question.setnStatement(statement);
    	    	question.setnOptionA(optionA);
    	    	question.setnOptionB(optionB);
    	    	question.setnOptionC(optionC);
    	    	question.setnOptionD(optionD);
    	    	question.setCorrectOption(correctOption);
    	    	question.setTimeLine(timeLineInt);
    	    	
    	    	//System.out.println("categoryStr :" + categoryStr);
    	    	StringTokenizer catTokenizer = new StringTokenizer(categoryStr, ",");
    	    	long finalCategoryInt = 1;
    	    	while (catTokenizer.hasMoreTokens()) {
    	    		String token = catTokenizer.nextToken().trim();
    	    		long tokenInt = Long.parseLong(token);
    	    		finalCategoryInt = finalCategoryInt * tokenInt; 
    	    	}
    	    	question.setCategory(finalCategoryInt);
    	    	//System.out.println("Ques " + question);
    	    	questions.add(question);
            }
            QuestionDBHandler.getInstance().createQuestionsInBulk(questions, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	

	/*
	public static void readAndLoadintoDB() {
		try  
	    {  
	    	File file = new File("D:\\Projects\\Games\\quiz.xlsx");
	    	FileInputStream fis = new FileInputStream(file);     
	    	XSSFWorkbook wb = new XSSFWorkbook(fis);   
	    	XSSFSheet sheet = wb.getSheetAt(0);       
	    	Iterator<Row> itr = sheet.iterator();
	    	Question question = new Question();
	    	
	    	while (itr.hasNext()) 
	    	{  
	    		Row row = itr.next();  
	    		Iterator<Cell> cellIterator = row.cellIterator();     
	    		while (cellIterator.hasNext()) 
	    		{  
	    			Cell cell = cellIterator.next();  
	    			switch (cell.getColumnIndex())               
	    			{  
	    				case 0:
    					break;  
	    				case 1:
	    					question.setnStatement(cell.getStringCellValue());
	    					break;
	    				case 2:
	    					break;
	    				case 3:
	    					question.setnOptionA(cell.getStringCellValue());
	    					break;
	    				case 4:
	    					break;
	    				case 5:
	    					question.setnOptionB(cell.getStringCellValue());
	    					break;
	    				case 6:
	    					break;
	    				case 7:
	    					question.setnOptionC(cell.getStringCellValue());
	    					break;
	    				case 8:
	    					break;
	    				case 9:
	    					question.setnOptionD(cell.getStringCellValue());
	    					break;
	    				case 10:
	    					//System.out.println(cell.);
	    					question.setCorrectOption(Integer.parseInt("1"));
	    					break;	
	    			}  
	    		}
	    		QuestionDBHandler.getInstance().createQuestion(question);
	    	}
	    	wb.close();
	    }  
	    catch(Exception e)  
	    {  
	    	e.printStackTrace();  
	    }
	}*/
}
