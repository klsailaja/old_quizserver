package com.ab.quiz.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GameOperation;
import com.ab.quiz.pojo.PlayerAnswer;

public class Test {
	
	public void method1() {
		
		System.out.println(System.currentTimeMillis());
		
		List<GameDetails> futureGames = GameManager.getInstance().getFutureGames(1);
		int mixGameProfileId = 30;
		
		for (GameDetails gd : futureGames) {
			
			GameOperation go = new GameOperation();
			go.setUserProfileId(mixGameProfileId++);
			go.setUserAccountType(1);
		}
		
		
		
		GameDetails gd = futureGames.get(0);
		
		PlayerAnswer pa = new PlayerAnswer();
		pa.setUserProfileId(12);
		pa.setQuestionNo(1);
		pa.setUserAnswer(gd.getGameQuestions().get(0).getCorrectOption());
		pa.setTimeDiff(3 * 1000);
		
		GameOperation go = new GameOperation();
		go.setUserProfileId(12);
		go.setUserAccountType(1);
		
		try {
			GameManager.getInstance().joinGame(gd.getGameId(), go);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GameManager.getInstance().submitAnswer(gd.getGameId(), pa);
		GameManager.getInstance().getLeaderBoard(gd.getGameId(), 1);
		
		pa = new PlayerAnswer();
		pa.setUserProfileId(12);
		pa.setQuestionNo(2);
		pa.setUserAnswer(gd.getGameQuestions().get(1).getCorrectOption());
		pa.setTimeDiff(3 * 1000);
		
		GameManager.getInstance().submitAnswer(gd.getGameId(), pa);
		GameManager.getInstance().getLeaderBoard(gd.getGameId(), 2);
		
		pa = new PlayerAnswer();
		pa.setUserProfileId(12);
		pa.setQuestionNo(3);
		pa.setUserAnswer(gd.getGameQuestions().get(2).getCorrectOption());
		pa.setTimeDiff(3 * 1000);
		
		GameManager.getInstance().submitAnswer(gd.getGameId(), pa);
		GameManager.getInstance().getLeaderBoard(gd.getGameId(), 3);
	}
	
	public static void main(String[] args) {
		//new Test().method1();
		/*System.out.println(19 / 20);
		System.out.println(20 / 20);*/
		/*Test test = new Test();
		for (int index = 1; index <= 10; index ++) {
			int val = test.getRandomNumber(1, 10);
			System.out.println(val);
		}*/
		/*long time1 = 25 * 1000 + 16 * 1000;
		
		String str1 = getUserNotionTimeStr(time1, true);
		System.out.println("str1 :" + str1);*/
		//IterationDemo.main(new String[]{"test"});
		int total = 0;
		for (int i = 3; i <= 199; i+=2) {
			total = total + i;
		}
		System.out.println("Total " + total);
	}
	
	private int getRandomNumber(int min, int max) {
        return min + (int)(Math.random() * (max - min));
    }
	
	public static String getUserNotionTimeStr(long timeTaken, boolean includeMinutes) {
		
        if (timeTaken == 0) {
            return null;
        }
        long minutes = (timeTaken / 1000) / 60;
        long seconds = (timeTaken / 1000);
        timeTaken = timeTaken - (minutes * 60 * 1000) - (seconds * 1000);

        StringBuilder stringBuilder = new StringBuilder();

        if (includeMinutes) {
            String str = String.valueOf(minutes);
            if (str.length() == 1) {
                str = "0" + str;
            }
            stringBuilder.append(str);
            stringBuilder.append(" m: ");
        }
        String str = String.valueOf(seconds);
        if (str.length() == 1) {
            str = "0" + str;
        }
        stringBuilder.append(seconds);
        stringBuilder.append(" s: ");

        str = String.valueOf(timeTaken);
        if (str.length() == 1) {
            str = "00" + str;
        } else if (str.length() == 2) {
            str = "0" + str;
        }
        stringBuilder.append(str);
        stringBuilder.append(" ms ");
        return stringBuilder.toString();
    }
	
}

class IterationDemo
{
    public static void main(String[] arg)
    {
        Map<String,String> gfg = new HashMap<String,String>();
     
        // enter name/url pair
        gfg.put("GFG", "geeksforgeeks.org");
        gfg.put("Practice", "practice.geeksforgeeks.org");
        gfg.put("Code", "code.geeksforgeeks.org");
        gfg.put("Quiz", "quiz.geeksforgeeks.org");
         
        // using keySet() for iteration over keys
        for (String name : gfg.keySet()) {
            System.out.println("key: " + name);
            gfg.remove(name);
        }
         
        // using values() for iteration over values
        for (String url : gfg.values())
            System.out.println("value: " + url);
    }
}