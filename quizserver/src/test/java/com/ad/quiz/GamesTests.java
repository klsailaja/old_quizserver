package com.ad.quiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ab.quiz.pojo.GameDetails;

public class GamesTests extends AbstractTest {
	
   @Override
   @Before
   public void setUp() {
      super.setUp();
   }
   
   @Test
   public void getFutureGameist() throws Exception {
	   fetchFutureGames();
   }
   
   private void fetchFutureGames() throws Exception {
	   String uri = "/game/future";
	   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
	         .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
	      
	   int status = mvcResult.getResponse().getStatus();
	   assertEquals(200, status);
	   String content = mvcResult.getResponse().getContentAsString();
	   GameDetails[] gameList = super.mapFromJson(content, GameDetails[].class);
	   assertTrue(gameList.length > 0);
   }
}
