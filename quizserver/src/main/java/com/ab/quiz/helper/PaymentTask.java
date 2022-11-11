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
		List<Long> tobePaidGameIds = new ArrayList<>();
		List<Long> completedGameIds = new ArrayList<>();
		for (GameHandler completedGame : completedGames) {
			Long gameId = completedGame.getGameDetails().getGameId();
			completedGameIds.add(gameId);
			if (gameId > maxId) {
				maxId = gameId;
			}
			if (!completedGame.isGameCancelled()) {
				tobePaidGameIds.add(gameId);
			}
		}
		
		List<PaymentGameDetails> paymentGameDetails = new ArrayList<>();
		
		BatchPaymentProcessor batchPaymentProcessor = new BatchPaymentProcessor(completedGames);
		
		long paymentTimeTaken = System.currentTimeMillis();
		
		for (GameHandler completedGame : completedGames) {
			
			logger.info("Making payments for Game server id# {} and client id# {}", 
					completedGame.getGameDetails().getGameId(), completedGame.getGameDetails().getTempGameId());
		
			// Bulk processing changes start
			PaymentProcessor pp = completedGame.getPaymentHandler();
			if (pp != null) {
				
				batchPaymentProcessor.addPaymentProcessor(pp);
				
				List<Long> actualWinUserIds = pp.getWinnerUserIdSet();
				Map<Long, Long> gamePlayers = completedGame.getUserIdToBossIdDetails();
				
				// Filter for the winner userids and their boss ids. Others ignore them.
				
				Iterator<Long> it = gamePlayers.keySet().iterator();
				while (it.hasNext())
				{
					Long userId = it.next();
					if (!actualWinUserIds.contains(userId)) {
						it.remove();
					}
				}
				
			
				batchPaymentProcessor.addUserBossIds(gamePlayers);
				// Bulk processing End
				completedGame.getGameDetails().setStatus(0);
				PaymentGameDetails pgd = new PaymentGameDetails(completedGame.getGameDetails().getGameId(),
						completedGame.getGameDetails().getTempGameId(), 
						actualWinUserIds, 
						completedGame.getGameDetails().getStartTime());
				paymentGameDetails.add(pgd);
			}
		}
		
		logger.info("Completed game list {}", completedGameIds);
		logger.info("Payment to be done game list {}", tobePaidGameIds);
		
		if (paymentGameDetails.size() > 0) {
			batchPaymentProcessor.setCompletedGameIds(tobePaidGameIds);
			batchPaymentProcessor.setPaymentGD(paymentGameDetails);
			batchPaymentProcessor.run();
		}
		
		logger.info("Time taken for processing payments {}" , (System.currentTimeMillis() - paymentTimeTaken)/1000);
		
		LazyScheduler.getInstance().submit(new UpdateMaxGameIdTask(maxId));
		if (paymentGameDetails.size() > 0) {
			LazyScheduler.getInstance().submit(new HistoryGameSaveTask(completedGames));
		}
		LazyScheduler.getInstance().submit(new DeleteCompletedGamesTask(completedGameIds), 3, TimeUnit.MINUTES);
	}
}
