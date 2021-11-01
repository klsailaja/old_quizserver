package com.ab.quiz.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.db.GameHistoryDBHandler;
import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.pojo.GamePlayers;
import com.ab.quiz.pojo.GameResults;
import com.ab.quiz.pojo.PlayerSummary;

public class HistoryGameSaveTask implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(HistoryGameSaveTask.class);
	private List<GameHandler> completedGames;
	
	public HistoryGameSaveTask(List<GameHandler> completedGames) {
		this.completedGames = completedGames;
	}

	@Override
	public void run() {
		
		long gameId; 
		int ticketRate; 
		long startTime;

		List<GameResults> allGameResults = new ArrayList<>();
		List<GamePlayers> allGamePlayers = new ArrayList<>();
		List<Long> updateLastLoggedIn = new ArrayList<>();
		
		for (GameHandler gameHandler : completedGames) {
			
			if (gameHandler.isGameCancelled()) {
				continue;
			}
			
			ticketRate = gameHandler.getGameDetails().getTicketRate();
			gameId = gameHandler.getGameDetails().getTempGameId();
			startTime = gameHandler.getGameDetails().getStartTime();
			
			GameResults gameResult = new GameResults();
			gameResult.setGameId(gameId);
			gameResult.setGamePlayedTime(startTime);
			gameResult.setTktRate(ticketRate);
			gameResult.setCelebrityName("NA");
			if (gameHandler.getGameDetails().getGameType() == 2) {
				gameResult.setCelebrityName(gameHandler.getGameDetails().getCelebrityName());
			}
			
			
			
			List<PlayerSummary> winnersList = gameHandler.getLeaderBoardPositions(10, false);
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
				
				updateLastLoggedIn.add(winner.getUserProfileId());
			}
			gameResult.setWinnersList(strBuffer.toString());
			
			allGameResults.add(gameResult);
			
			List<GamePlayers> playersList = gameHandler.getPlayerDetails();
			allGamePlayers.addAll(playersList);
		}

		logger.info("Inserting completed games of size {}", allGameResults.size());
		try {
			GameHistoryDBHandler.getInstance().bulkInsertGameResults(allGameResults, 20);
			GameHistoryDBHandler.getInstance().bulkInsertGamePlayers(allGamePlayers, 50);
			//UserProfileDBHandler.getInstance().updateLastLoggedTimeInBulkMode(updateLastLoggedIn, 50);
		} catch (SQLException e) {
			logger.error("SQLException while doing bulk insert of game results and game players", e);
		}
	}
}
