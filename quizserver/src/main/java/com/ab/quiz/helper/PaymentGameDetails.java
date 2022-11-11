package com.ab.quiz.helper;

import java.util.ArrayList;
import java.util.List;

public class PaymentGameDetails {
	long serverGameId;
	long clientGameId;
	long gameStartTime;
	List<Long> enrolledUids = new ArrayList<>();
	
	public PaymentGameDetails(long serverGameId, long clientGameId, List<Long> enrolledUids,
			long gameStartTime) {
		this.serverGameId = serverGameId;
		this.clientGameId = clientGameId;
		this.gameStartTime = gameStartTime; 
		this.enrolledUids.addAll(enrolledUids);
	}

	public long getServerGameId() {
		return serverGameId;
	}

	public long getClientGameId() {
		return clientGameId;
	}

	public long getGameStartTime() {
		return gameStartTime;
	}

	public List<Long> getEnrolledUids() {
		return enrolledUids;
	}
}
