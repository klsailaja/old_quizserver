package com.ab.quiz.pojo;

public class PrizeDetail {
	private int rank;
	private int prizeMoney;
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getPrizeMoney() {
		return prizeMoney;
	}
	public void setPrizeMoney(int prizeMoney) {
		this.prizeMoney = prizeMoney;
	}
	@Override
	public String toString() {
		return "PrizeDetail [rank=" + rank + ", prizeMoney=" + prizeMoney + "]";
	}
}
