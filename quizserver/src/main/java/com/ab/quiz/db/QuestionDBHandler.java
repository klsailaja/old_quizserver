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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.pojo.Question;

/*
CREATE TABLE QUIZQUESTIONS(ID BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, 
		NSTATEMENT VARCHAR(200) NOT NULL,
		CATEGORY BIGINT NOT NULL,
		TIMELINE INT NOT NULL,
		NOPTIONA VARCHAR(100) NOT NULL,
		NOPTIONB VARCHAR(100) NOT NULL,
		NOPTIONC VARCHAR(100) NOT NULL,
		NOPTIONd VARCHAR(100) NOT NULL, 
		CORRECTOPTION INT, PRIMARY KEY (ID)) ENGINE = INNODB;
		
CREATE INDEX QUIZQUESTIONS_Inx ON QUIZQUESTIONS(CATEGORY);        
DROP INDEX QUIZQUESTIONS_Inx ON QUIZQUESTIONS;        
CREATE INDEX QUIZQUESTIONS_Inx ON QUIZQUESTIONS(CATEGORY);

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
	private static String CORRECTOPTION = "CORRECTOPTION";
	
	private static final String CREATE_QUESTION_ENTRY = "INSERT INTO " + TABLE_NAME   
			+ "(" + NSTATEMENT + "," + NOPTION_A + "," + NOPTION_B + "," + NOPTION_C + ","
			+ NOPTION_D + "," + CORRECTOPTION + "," +
			CATEGORY + "," + TIMELINE + ") VALUES"
			+ "(?,?,?,?,?,?,?,?)";
	/*private static final String GET_QUESTION_ENTRY_SET = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + ID + " IN (?,?,?,?,?,?,?,?,?,?,?)";*/
	private static final String GET_QUESTIONS_BY_RANDOM = "SELECT * FROM " + TABLE_NAME
			+ " ORDER BY RAND() LIMIT 11";
	private static final String GET_QUESTIONS_RANDOM_CELEBRITY = "SELECT * FROM " +
			TABLE_NAME + " WHERE MOD(" + CATEGORY + ",?) = 0 ORDER BY RAND() LIMIT 11";
	
	private static final Logger logger = LogManager.getLogger(QuestionDBHandler.class);
	private static QuestionDBHandler instance = null;
	
	private QuestionDBHandler() {
	}
	
	public static QuestionDBHandler getInstance() {
		if (instance == null) {
			logger.debug("In QuestionDBHandler getInstance() method instance created");
			instance = new QuestionDBHandler();
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
				
				if (index % batchSize == 0) {
					int results[] = ps.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
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
				dbConn.setAutoCommit(true);
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
	
	public List<Question> getRandomQues(int category) throws SQLException {
		String psSql = GET_QUESTIONS_BY_RANDOM;
		if (category != -1) {
			psSql = GET_QUESTIONS_RANDOM_CELEBRITY;
		}
		
		ConnectionPool cp = ConnectionPool.getInstance();
		Connection dbConn = cp.getDBConnection();
		PreparedStatement ps = dbConn.prepareStatement(psSql);
		ResultSet rs = null;
		
		if (category != -1) {
			ps.setLong(1, category);
		}
		
		List<Question> questionSet = new ArrayList<>(11);
		int qNo = 1;
		
		try {
			rs = ps.executeQuery();
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
		readTextFile();
		UserProfileDBHandler.main(args);
		MyTransactionDBHandler.main(args);
		GameHistoryDBHandler.main(args);
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
