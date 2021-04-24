package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.tasks.DeleteCompletedGamesTask;
import com.ab.quiz.tasks.HistoryGameSaveTask;
import com.ab.quiz.tasks.UpdateMaxGameIdTask;

public class PaymentTask implements Runnable {
	
	private List<GameHandler> completedGames;
	private static final Logger logger = LogManager.getLogger(PaymentTask.class);
	
	public PaymentTask(List<GameHandler> completedGames) {
		this.completedGames = completedGames;
	}
	
	@Override
	public void run() {
		
		if (completedGames == null) {
			return;
		}
		int completedGameCount = completedGames.size();
		
		logger.info("Completed games count is {}", completedGameCount);
		
		if (completedGameCount == 0) {
			return;
		}
		
		long maxId = -1;
		List<Long> completedGameIds = new ArrayList<>();
		
		BatchPaymentProcessor batchPaymentProcessor = new BatchPaymentProcessor();
		
		long paymentTimeTaken = System.currentTimeMillis();
		
		for (GameHandler completedGame : completedGames) {
			
			logger.info("Making payments for Game# {}", completedGame.getGameDetails().getGameId());
		
			// Bulk processing changes start
			PaymentProcessor pp = completedGame.getPaymentHandler();
			if (pp != null) {
				
				batchPaymentProcessor.addPaymentProcessor(pp);
				
				List<Long> actualWinUserIds = pp.getWinnerUserIdSet();
				Map<Long, Long> gamePlayers = completedGame.getUserIdToBossIdDetails();
				
				Iterator<Long> it = gamePlayers.keySet().iterator();
				while (it.hasNext())
				{
					Long userId = it.next();
					if (!actualWinUserIds.contains(userId)) {
						it.remove();
					}
				}
				
				batchPaymentProcessor.addUserBossIds(gamePlayers);
				logger.info("gamePlayers contents {}", gamePlayers);
				// Bulk processing End
			}
			
			Long gameId = completedGame.getGameDetails().getGameId(); 
			if (gameId > maxId) {
				maxId = gameId;
			}
			completedGameIds.add(gameId);
		}
		
		logger.debug("completed game list {}", completedGameIds);
		
		batchPaymentProcessor.run();
		
		logger.info("Time taken for processing payments {}" , (System.currentTimeMillis() - paymentTimeTaken)/1000);
		
		LazyScheduler.getInstance().submit(new UpdateMaxGameIdTask(maxId));
		LazyScheduler.getInstance().submit(new HistoryGameSaveTask(completedGames));
		LazyScheduler.getInstance().submit(new DeleteCompletedGamesTask(completedGameIds), 3, TimeUnit.MINUTES);
		
	}
}
