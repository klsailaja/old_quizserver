package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ab.quiz.common.TAGS;
import com.ab.quiz.constants.CustomerCareReqType;
import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.pojo.MoneyTransaction;
import com.ab.quiz.pojo.MoneyUpdaterGameDetails;
import com.ab.quiz.pojo.UsersCompleteMoneyDetails;
import com.ab.quiz.tasks.DeleteCompletedGamesTask;
import com.ab.quiz.tasks.HistoryGameSaveTask;
import com.ab.quiz.tasks.UpdateMaxGameIdTask;

public class PaymentTask implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(PaymentTask.class);
	
	private List<GameHandler> completedGames;
	private List<PaymentProcessor> paymentProcessors = new ArrayList<>();
	private Map<Long, Long> userIdVsBossId = new HashMap<>();
	private int realGamesPlayedCount = 0;
	
	
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
		
		realGamesPlayedCount = tobePaidGameIds.size(); 
		
		long gameSlotsStartTime = -1;
		long paymentTimeTaken = System.currentTimeMillis();
		
		for (GameHandler completedGame : completedGames) {
			
			logger.info("Making payments for Game server id# {} and client id# {}", 
					completedGame.getGameDetails().getGameId(), completedGame.getGameDetails().getTempGameId());
		
			// Bulk processing changes start
			PaymentProcessor pp = completedGame.getPaymentHandler();
			if (pp != null) {
				
				if (gameSlotsStartTime == -1) {
					gameSlotsStartTime = completedGame.getGameDetails().getStartTime();
				}
				paymentProcessors.add(pp);
				
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
				
			
				userIdVsBossId.putAll(gamePlayers);
				// Bulk processing End
				completedGame.getGameDetails().setStatus(0);
			}
			
			UsersCompleteMoneyDetails winnerMoneyDetails = new UsersCompleteMoneyDetails();
			String logTag = TAGS.WIN_MONEY + " WinnersMoney : sid : " 
					+ QuizConstants.MY_SERVER_ID + " : SlotTime :" + new Date(gameSlotsStartTime).toString();
			winnerMoneyDetails.setLogTag(logTag);
			List<MoneyTransaction> winUsersTransactions = new ArrayList<>();
			List<MoneyUpdaterGameDetails> moneyUpdaterGDListObjs = new ArrayList<>();

			for (PaymentProcessor processor : paymentProcessors) {
				processor.processPayments(userIdVsBossId, winUsersTransactions, moneyUpdaterGDListObjs);
			}
			
			winnerMoneyDetails.setUsersMoneyTransactionList(winUsersTransactions);
			String trackKey = "server" + QuizConstants.MY_SERVER_ID + "-win-" + String.valueOf(gameSlotsStartTime);
			winnerMoneyDetails.setTrackStatusKey(trackKey);
			
			MoneyUpdateRequest request = new MoneyUpdateRequest(CustomerCareReqType.WIN_MONEY_NOT_ADDED.getId(),
					gameSlotsStartTime,
					winnerMoneyDetails, moneyUpdaterGDListObjs);
			request.run();
		}
		
		logger.info("{} Completed game list {}", TAGS.WIN_MONEY, completedGameIds);
		logger.info("{} Payment to be done game list size {} and game ids {}", TAGS.WIN_MONEY, 
				tobePaidGameIds.size(), tobePaidGameIds);
		
		logger.info("{} Time taken for processing payments {}" , TAGS.WIN_MONEY, (System.currentTimeMillis() - paymentTimeTaken)/1000);
		
		LazyScheduler.getInstance().submit(new UpdateMaxGameIdTask(maxId));
		if (realGamesPlayedCount > 0) {
			LazyScheduler.getInstance().submit(new HistoryGameSaveTask(completedGames));
		}
		LazyScheduler.getInstance().submit(new DeleteCompletedGamesTask(completedGameIds), 3, TimeUnit.MINUTES);
	}
}
