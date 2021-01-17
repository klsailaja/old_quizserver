package com.ab.quiz.tasks;

import java.sql.SQLException;
import java.util.List;

import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.pojo.GamePlayers;
import com.ab.quiz.pojo.GameResults;
import com.ab.quiz.pojo.PlayerSummary;

public class HistoryGameSaveTask implements Runnable {
	private List<GameHandler> completedGames;
	
	public HistoryGameSaveTask(List<GameHandler> completedGames) {
		this.completedGames = completedGames;
	}

	@Override
	public void run() {
		
		long gameId; 
		int ticketRate; 
		long startTime;
		
		for (GameHandler gameHandler : completedGames) {
			
			if (gameHandler.isGameCancelled()) {
				continue;
			}
			
			ticketRate = gameHandler.getGameDetails().getTicketRate();
			gameId = gameHandler.getGameDetails().getGameId();
			startTime = gameHandler.getGameDetails().getStartTime();
			
			GameResults gameResult = new GameResults();
			gameResult.setGameId(gameId);
			gameResult.setGamePlayedTime(startTime);
			gameResult.setTktRate(ticketRate);
			gameResult.setCelebrityName("NA");
			if (gameHandler.getGameDetails().getGameType() == 2) {
				gameResult.setCelebrityName(gameHandler.getGameDetails().getCelabrityName());
			}
			
			List<PlayerSummary> winnersList = gameHandler.getLeaderBoardPositions(10);
			StringBuffer strBuffer = new StringBuffer();
			//userName;rank;correctCount;totalTime;amountWon
			for (PlayerSummary winner : winnersList) {
				
				strBuffer.append(winner.getUserName());
				strBuffer.append(";");
				strBuffer.append(winner.getRank());
				strBuffer.append(";");
				strBuffer.append(winner.getCorrectCount());
				strBuffer.append(";");
				String timeStr = String.valueOf(winner.getTotalTime());
				strBuffer.append(timeStr);
				strBuffer.append(";");
				strBuffer.append(winner.getAmountWon());
				strBuffer.append(":");
				
				UpdateUserLastLoggedTime run = new UpdateUserLastLoggedTime(winner.getUserProfileId());
				//LazyScheduler.getInstance().submit(run);
				run.run();
			}
			gameResult.setWinnersList(strBuffer.toString());
			
			List<GamePlayers> playersList = gameHandler.getPlayerDetails();
			try {
				GameHistoryDBHandler.getInstance().createGameHistoryWithPlayers(gameResult, playersList);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
