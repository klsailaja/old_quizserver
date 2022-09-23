package com.ad.quiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ab.quiz.pojo.GameDetails;
import com.ad.quiz.helper.TestScheduler;

public class GamesTests extends AbstractTest {
	
   @Override
   @Before
   public void setUp() {
      super.setUp();
   }
   
   @Test
   public void getFutureGameist() throws Exception {
	   for (int i = 1; i <= 2; i++) {
		   TestScheduler.getInstance().submit(new User(i));
	   }
   }
   
	private void fetchFutureGames() throws Exception {
		   System.out.println("In Games");	
		   String uri = "/game/1/future/-1/1";
		   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
		         .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
		      
		   int status = mvcResult.getResponse().getStatus();
		   assertEquals(200, status);
		   String content = mvcResult.getResponse().getContentAsString();
		   GameDetails[] gameList = super.mapFromJson(content, GameDetails[].class);
		   System.out.println(gameList.length);
		   assertTrue(gameList.length > 0);
	   }


	class User implements Runnable {
		int i;
		User(int i) {
			this.i = i;
		}
		public void run() {
			try {
				System.out.println(i + "::" + Thread.currentThread().getName());
				Thread.sleep(500);
				fetchFutureGames();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("error");
				e.printStackTrace();
			}
		}
	}
}