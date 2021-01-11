package com.ab.quiz.helper;

import java.util.List;

import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GameOperation;
import com.ab.quiz.pojo.PlayerAnswer;

public class Test {
	public void method1() {
		System.out.println(System.currentTimeMillis());
		List<GameDetails> futureGames = GameManager.getInstance().getFutureGames(1);
		
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
		new Test().method1();
	}
}

