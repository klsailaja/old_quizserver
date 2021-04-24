package com.ad.quiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import com.ab.quiz.pojo.GameDetails;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class Test1 {
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	public void greetingShouldReturnDefaultMessage() throws Exception {
		System.out.println("Port is " + port);
		this.restTemplate.getForObject("http://localhost:" + port + "/game/1/future", GameDetails[].class);
	}
}
