package com.ab.quiz;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.pojo.ChatGameDetails;
import com.ab.quiz.pojo.GameDetails;
import com.ab.quiz.pojo.GameOperation;
import com.ab.quiz.pojo.GameStatus;
import com.ab.quiz.pojo.GameStatusHolder;
import com.ab.quiz.pojo.PlayerAnswer;
import com.ab.quiz.pojo.PlayerSummary;
import com.ab.quiz.pojo.PrizeDetail;
import com.ab.quiz.pojo.UserHistoryGameDetails;

@RestController
@RequestMapping("/game")
public class GamesController extends BaseController {
	
	private static final Logger logger = LogManager.getLogger(GamesController.class);
	
	@RequestMapping(value = "/{gametype}/future", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<GameDetails> getFutureGames(@PathVariable("gametype") int gametype) {
		logger.info("Call to getFutureGames()");
		List<GameDetails> futureGames = GameManager.getInstance().getFutureGames(gametype);
		logger.info("Call to getFutureGames() returned with {}", futureGames.size());
		return futureGames;
	}

	@RequestMapping(value = "/{gametype}/enrolled/{userProfileId}", method = RequestMethod.GET)
	public @ResponseBody List<GameDetails> getEnrolledGames(@PathVariable("gametype") int gametype, 
			@PathVariable("userProfileId") long userProfileId) {
		
		return GameManager.getInstance().getEnrolledGames(gametype, userProfileId);
	}
	
	@RequestMapping(value = "/past/{userProfileId}/{startRowNo}", method = RequestMethod.GET)
	public @ResponseBody UserHistoryGameDetails getHistoryGames(@PathVariable("userProfileId") long userProfileId, 
			@PathVariable("startRowNo") int startRowNo) throws SQLException {
		logger.info("Call to getHistoryGames() with userProfileId {}", userProfileId);
		UserHistoryGameDetails userGameDetails = GameManager.getInstance().getHistoryGames(userProfileId, startRowNo);
		logger.info("Call to getHistoryGames() returned with {}", userGameDetails.getHistoryGames().size());
		return userGameDetails;
	}
	
	@RequestMapping(value = "/{gameId}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody GameDetails getGameById(@PathVariable("gameId") long gameId) throws NotAllowedException {
		return GameManager.getInstance().getGameDetailsById(gameId);
	}
	
	@RequestMapping(value = "/{gameId}/status", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody GameStatus getGameStatus(@PathVariable("gameId") long gameId) throws NotAllowedException, SQLException {
		return GameManager.getInstance().getGameStatus(gameId);
	}
	
	@RequestMapping(value = "/{gametype}/allstatus", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody GameStatusHolder getAllGamesStatus(@PathVariable("gametype") int gametype) throws NotAllowedException, SQLException {
		return GameManager.getInstance().getAllGamesStatus(gametype);
	}
	
	@RequestMapping(value = "/{gametype}/enrolled/{userProfileId}/status", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody GameStatusHolder getUserEnrolledGamesStatus(@PathVariable("gametype") int gametype,
			@PathVariable("userProfileId") long userProfileId) throws NotAllowedException, SQLException {
		return GameManager.getInstance().getUserEnrolledGamesStatus(gametype,userProfileId);
	}
	
	@RequestMapping(value = "/{gameId}/join", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String join(@PathVariable("gameId") long gameId, 
		@RequestBody GameOperation gameOper) throws NotAllowedException, SQLException {
		
		Boolean result = GameManager.getInstance().joinGame(gameId, gameOper);
		return Boolean.toString(result);
	}
	
	@RequestMapping(value = "/{gameId}/unjoin", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String unjoin(@PathVariable("gameId") long gameId, 
			@RequestBody GameOperation gameOper) throws NotAllowedException, SQLException {
		Boolean result = GameManager.getInstance().unjoin(gameId, gameOper);
		return Boolean.toString(result);
	}
	
	@RequestMapping(value = "/{gameId}/submit", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String submitAnswer(@PathVariable("gameId") long gameId, 
			@RequestBody PlayerAnswer answer) throws NotAllowedException {
		Boolean result = GameManager.getInstance().submitAnswer(gameId, answer);
		return Boolean.toString(result);
	}
	
	@RequestMapping(value = "/{gameId}/prize", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody List<PrizeDetail> getGamePrizeDetails(@PathVariable("gameId") long gameId) throws NotAllowedException {
		return GameManager.getInstance().getGamePrizeDetails(gameId);
	}
	
	@RequestMapping(value = "/{gameId}/leaderboard/{qNo}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<PlayerSummary> getLeaderBoard(@PathVariable("gameId") long gameId, @PathVariable("qNo") int qNo) 
			throws NotAllowedException {
		return GameManager.getInstance().getLeaderBoard(gameId, qNo);
	}
	
	@RequestMapping(value = "/chat/{gametype}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<ChatGameDetails> getBasicGameDetails(@PathVariable("gametype") int gametype) {
		logger.info("Call to chat getBasicGameDetails()");
		
		List<GameDetails> futureGames = GameManager.getInstance().getFutureGames(gametype);
		List<ChatGameDetails> basicDetails = new ArrayList<>();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		String timePattern = "hh:mm";
        
		
		for (GameDetails gd : futureGames) {
			
			if (gd.getTicketRate() == 0) {
				continue;
			}
			
			ChatGameDetails gameBasicDetails = new ChatGameDetails();
			gameBasicDetails.setTicketRate(gd.getTicketRate());
			gameBasicDetails.setGameType(gd.getGameType());
			gameBasicDetails.setTempGameId(gd.getTempGameId());
			gameBasicDetails.setCurrentCount(gd.getCurrentCount());
			
			simpleDateFormat.applyPattern(timePattern);
	        String timeStr = simpleDateFormat.format(new Date(gd.getStartTime()));
	        gameBasicDetails.setGameTime(timeStr);
	        
	        basicDetails.add(gameBasicDetails);
			
		}
		logger.info("Call to chat getBasicGameDetails() returned with {}", basicDetails.size());
		return basicDetails;
	}
}

