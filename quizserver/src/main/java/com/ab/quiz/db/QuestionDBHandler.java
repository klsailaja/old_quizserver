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
CREATE TABLE QuizQuestions(id bigint NOT NULL AUTO_INCREMENT, 
		nStatement varchar(200) NOT NULL,
		category bigint NOT NULL,
		timeline int NOT NULL,
		nOptionA varchar(100) NOT NULL,
		nOptionB varchar(100) NOT NULL,
		nOptionC varchar(100) NOT NULL,
		nOptionD varchar(100) NOT NULL, 
		correctOption int, PRIMARY KEY (id)); 
*/

public class QuestionDBHandler {
	private static String TABLE_NAME = "QuizQuestions";
	
	private static String ID = "id";
	private static String NSTATEMENT = "nstatement";
	private static String CATEGORY = "category";
	private static String TIMELINE = "timeline";
	private static String NOPTION_A = "nOptionA";
	private static String NOPTION_B = "nOptionB";
	private static String NOPTION_C = "nOptionC";
	private static String NOPTION_D = "nOptionD";
	private static String CORRECTOPTION = "correctOption";
	
	private static final String CREATE_QUESTION_ENTRY = "INSERT INTO " + TABLE_NAME   
			+ "(" + NSTATEMENT + "," + NOPTION_A + "," + NOPTION_B + "," + NOPTION_C + ","
			+ NOPTION_D + "," + CORRECTOPTION + "," +
			CATEGORY + "," + TIMELINE + ") VALUES"
			+ "(?,?,?,?,?,?,?,?)";
	private static final String GET_QUESTION_ENTRY_SET = "SELECT * FROM " + TABLE_NAME 
			+ " WHERE " + ID + " IN (?,?,?,?,?,?,?,?,?,?,?)";
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
	
	public boolean createQuestion(List<Question> questions) throws SQLException {
		
		System.out.println("questions.size() :" + questions.size());
		ConnectionPool cp = null;
		Connection dbConn = null;
		PreparedStatement ps = null;
		
		try {
			cp = ConnectionPool.getInstance();
			dbConn = cp.getDBConnection();
			dbConn.setAutoCommit(false);
			
			ps = dbConn.prepareStatement(CREATE_QUESTION_ENTRY);
			
			for (int index = 0; index < questions.size(); index ++) {
				
				Question question = questions.get(index);
			
				ps.setString(1, question.getnStatement());
				ps.setString(2, question.getnOptionA());
				ps.setString(3, question.getnOptionB());
				ps.setString(4, question.getnOptionC());
				ps.setString(5, question.getnOptionD());
				ps.setInt(6, question.getCorrectOption());
				ps.setLong(7, question.getCategory());
				ps.setInt(8, question.getTimeLine());
			
				ps.addBatch();
				
				if (index % 200 == 0) {
					int[] result = ps.executeBatch();
					dbConn.setAutoCommit(true);
					dbConn.setAutoCommit(false);
				}
			}
			ps.executeBatch();
			dbConn.setAutoCommit(true);
			
			
			int createResult = ps.executeUpdate();
			return (createResult > 0);
		} catch(SQLException ex) {
			logger.error("Error creating question", ex);
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
		
		if (category != -1) {
			ps.setLong(1, category);
		}
		
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
				rs.close();
			}
		} catch (SQLException ex) {
			logger.error("SQLException in getRandomQues()", ex);
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
	}
	
	private int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
	
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
	}
	
	public static void main(String[] args) {
		readTextFile();
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
    	    	
    	    	String qNo = strTokenizer.nextToken().trim();
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
            QuestionDBHandler.getInstance().createQuestion(questions);
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
