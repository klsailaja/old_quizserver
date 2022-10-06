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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.ab.quiz.constants.QuizConstants;
import com.ab.quiz.exceptions.InternalException;
import com.ab.quiz.exceptions.NotAllowedException;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.helper.CelebritySpecialHandler;
import com.ab.quiz.pojo.CelebrityFullDetails;
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
	
	@RequestMapping(value = "/{gametype}/future/{lastGameId}/{slotSize}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<GameDetails> getFutureGames(@PathVariable("gametype") int gametype, 
			@PathVariable("lastGameId") long lastGameId, @PathVariable("slotSize") int slotSize) {
		logger.info("Call to getFutureGames() with {} : {} : {}", gametype, lastGameId, slotSize);
		List<GameDetails> futureGames = GameManager.getInstance().getFutureGames(gametype);
		
		List<GameDetails> returnFutureGames = new ArrayList<>();
		int maxSize = slotSize * QuizConstants.GAMES_RATES_IN_ONE_SLOT_MIXED.length;
		if (gametype == 2) {
			maxSize = slotSize * QuizConstants.GAMES_RATES_IN_ONE_SLOT_SPECIAL.length;
		}
		int count = 0;
		for (GameDetails gd : futureGames) {
			if (gd.getGameId() > lastGameId) {
				returnFutureGames.add(gd);
				count++;
				if (count == maxSize) {
					logger.info("Call to getFutureGames() with maxSize returned with {} ", returnFutureGames.size());
					return returnFutureGames;
				}
			}
		}
		logger.info("Call to getFutureGames() plain returned with {} ", returnFutureGames.size());
		return returnFutureGames;
	}

	@RequestMapping(value = "/{gametype}/enrolled/{userProfileId}", method = RequestMethod.GET)
	public @ResponseBody List<GameDetails> getEnrolledGames(@PathVariable("gametype") int gametype, 
			@PathVariable("userProfileId") long userProfileId) {
		logger.info("Call to getEnrolledGames() with {}", gametype);
		List<GameDetails> enrolledGames = GameManager.getInstance().getEnrolledGames(gametype, userProfileId);
		logger.info("Call to getEnrolledGames() with type {} and size {}", gametype, enrolledGames.size());
		return enrolledGames;
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
	
	@RequestMapping(value = "/{gameId}/{userProfileId}/enrolledstatus", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getEnrolledStatus(@PathVariable("gameId") long gameId, 
			@PathVariable("userProfileId") long userProfileId) throws NotAllowedException{
		Boolean result = GameManager.getInstance().getEnrolledStatus(gameId, userProfileId);
		return Boolean.toString(result);
	}
	
	@RequestMapping(value = "/{gameId}/status", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody GameStatus getGameStatus(@PathVariable("gameId") long gameId) throws NotAllowedException, InternalException {
		try {
			return GameManager.getInstance().getGameStatus(gameId);
		}
		catch(SQLException ex) {
			logger.error("SQLException in getGameStatus", ex);
			throw new InternalException(ex.getMessage());
		}
		catch(Exception ex) {
			logger.error("Error in getGameStatus" , ex);
			String errMessage = "Check beckend connectvity";
            boolean isAPIException = false;
			if (ex instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) ex;
                errMessage = clientExp.getResponseBodyAsString();
                isAPIException = true;
            } else if (ex instanceof HttpServerErrorException) {
                HttpServerErrorException serverExp = (HttpServerErrorException) ex;
                errMessage = serverExp.getResponseBodyAsString();
                isAPIException = true;
            }
			logger.error("Error message is {} : apiexception is {}", errMessage, isAPIException);
			throw new NotAllowedException(errMessage);
		}
	}
	
	@RequestMapping(value = "/{gametype}/allstatus", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody GameStatusHolder getAllGamesStatus(@PathVariable("gametype") int gametype) 
			throws NotAllowedException, InternalException {
		logger.debug("In getAllGamesStatus...{}", gametype);
		try {
			return GameManager.getInstance().getAllGamesStatus(gametype);
		}
		catch(SQLException ex) {
			logger.error("Exception in getAllGamesStatus", ex);
			throw new InternalException(ex.getMessage());
		} catch(Exception ex) {
			logger.error("Error while fetching getAllGamesStatus" , ex);
			String errMessage = "Check beckend connectvity";
            boolean isAPIException = false;
			if (ex instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) ex;
                errMessage = clientExp.getResponseBodyAsString();
                isAPIException = true;
            } else if (ex instanceof HttpServerErrorException) {
                HttpServerErrorException serverExp = (HttpServerErrorException) ex;
                errMessage = serverExp.getResponseBodyAsString();
                isAPIException = true;
            }
			logger.error("Error message is {} : apiexception is {}", errMessage, isAPIException);
			throw new NotAllowedException(errMessage);
		}
	}
	
	@RequestMapping(value = "/{gametype}/enrolled/{userProfileId}/status", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody GameStatusHolder getUserEnrolledGamesStatus(@PathVariable("gametype") int gametype,
			@PathVariable("userProfileId") long userProfileId) throws NotAllowedException, InternalException {
		try {
			return GameManager.getInstance().getUserEnrolledGamesStatus(gametype,userProfileId);
		} catch (SQLException ex) {
			logger.error("Exception in getUserEnrolledGamesStatus", ex);
			throw new InternalException(ex.getMessage());
		} catch(Exception ex) {
			logger.error("Error while fetching getUserEnrolledGamesStatus" , ex);
			String errMessage = "Check beckend connectvity";
            boolean isAPIException = false;
			if (ex instanceof HttpClientErrorException) {
                HttpClientErrorException clientExp = (HttpClientErrorException) ex;
                errMessage = clientExp.getResponseBodyAsString();
                isAPIException = true;
            } else if (ex instanceof HttpServerErrorException) {
                HttpServerErrorException serverExp = (HttpServerErrorException) ex;
                errMessage = serverExp.getResponseBodyAsString();
                isAPIException = true;
            }
			logger.error("Error message is {} : apiexception is {}", errMessage, isAPIException);
			throw new NotAllowedException(errMessage);
		}
	}
	
	@RequestMapping(value = "/{gameId}/join", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String join(@PathVariable("gameId") long gameId, 
		@RequestBody GameOperation gameOper) throws NotAllowedException, InternalException {
		
		try {
			Boolean result = GameManager.getInstance().joinGame(gameId, gameOper);
			return Boolean.toString(result);
		} catch (SQLException ex) {
			logger.error("Exception in join method", ex);
			throw new InternalException(ex.getMessage());
		}
	}
	
	@RequestMapping(value = "/{gameId}/unjoin", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String unjoin(@PathVariable("gameId") long gameId, 
			@RequestBody GameOperation gameOper) throws NotAllowedException, InternalException {
		try {
			logger.debug("unjoin called with {}", gameId);
			Boolean result = GameManager.getInstance().unjoin(gameId, gameOper);
			return Boolean.toString(result);
		} catch (SQLException ex) {
			logger.error("Exception in unjoin method", ex);
			throw new InternalException(ex.getMessage());
		}
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
	
	@RequestMapping(value = "/celebrityschedule", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody CelebrityFullDetails getCelebrityFullDetails() {
		return GameManager.getInstance().getCelebrityFullDetails();
	}
	
	@RequestMapping(value = "/upcoming/{timeHour}", method = RequestMethod.GET, produces = "application/json") 
	public @ResponseBody List<String> getUpcomingCelebrityNames(@PathVariable("timeHour") int hour) {
		
		int hour1 = hour + 1;
		int hour2 = hour + 2;
		
		List<String> firstSet = CelebritySpecialHandler.getInstance().
				getUniqueCelebrityNames(hour1, QuizConstants.GAMES_RATES_IN_ONE_SLOT_SPECIAL.length);
		
		StringBuilder firstStrBuilder = new StringBuilder(); 
		for (String li : firstSet) {
			firstStrBuilder.append(li);
			firstStrBuilder.append(",");
		}
		String firstStr = firstStrBuilder.toString(); 
		int lastPos = firstStr.lastIndexOf(",");
		if (lastPos > -1) {
			firstStr = firstStr.substring(0, lastPos);
		}
		firstStr = firstStr + " games coming at: " + hour1;
		
		List<String> secondSet = CelebritySpecialHandler.getInstance().
				getUniqueCelebrityNames(hour2, QuizConstants.GAMES_RATES_IN_ONE_SLOT_SPECIAL.length);
		StringBuilder secondStrBuilder = new StringBuilder(); 
		for (String li : secondSet) {
			secondStrBuilder.append(li);
			secondStrBuilder.append(",");
		}
		String secondStr = secondStrBuilder.toString();
		
		lastPos = secondStr.lastIndexOf(",");
		if (lastPos > -1) {
			secondStr = secondStr.substring(0, lastPos);
		}
		secondStr = secondStr + " games coming at: " + hour2;
		
		List<String> results = new ArrayList<>(2);
		results.add(firstStr);
		results.add(secondStr);
		
		System.out.println(results);
		return results;
	}
	
	
	@RequestMapping(value = "/chat/{gametype}", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<ChatGameDetails> getBasicGameDetails(@PathVariable("gametype") int gametype) {
		logger.info("Call to chat getBasicGameDetails() start");
		
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
			gameBasicDetails.setCelebrityName(gd.getCelebrityName());
			gameBasicDetails.setGameTimeInMillis(gd.getStartTime());
			
			simpleDateFormat.applyPattern(timePattern);
	        String timeStr = simpleDateFormat.format(new Date(gd.getStartTime()));
	        gameBasicDetails.setGameTime(timeStr);
	        
	        basicDetails.add(gameBasicDetails);
			
		}
		logger.info("Call to chat getBasicGameDetails() returned with {}", basicDetails.size());
		return basicDetails;
	}
}

