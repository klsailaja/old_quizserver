package com.ab.quiz;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ab.quiz.handlers.GameHandler;
import com.ab.quiz.handlers.GameManager;
import com.ab.quiz.helper.GamesGenerator;

@SpringBootApplication
public class TeluguMovieQuizApplication implements ApplicationRunner {
	
	private static final Logger logger = LogManager.getLogger(TeluguMovieQuizApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(TeluguMovieQuizApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			logger.debug("Starting the TeluguMovieQuizApplication application");
			GamesGenerator gameGenerator1 = new GamesGenerator(1);
			gameGenerator1.initialize();
			
			List<GameHandler> initialGameHandlersList = gameGenerator1.getInitialGameSet();
			logger.debug("Fetched initialGameHandlersList successfully. The size is {}", 
					initialGameHandlersList.size());
			
			GameManager.getInstance().addNewGames(initialGameHandlersList);
			
			GamesGenerator gameGenerator2 = new GamesGenerator(2);
			gameGenerator2.initialize();
			
			initialGameHandlersList = gameGenerator2.getInitialGameSet();
			logger.debug("Fetched celebrity special successfully. The size is {}", 
					initialGameHandlersList.size());
			
			GameManager.getInstance().addNewGames(initialGameHandlersList);
			
			
			
		} catch(SQLException ex) {
			logger.error("SQLException in TeluguMovieQuizApplication", ex);
		}

	}
}
