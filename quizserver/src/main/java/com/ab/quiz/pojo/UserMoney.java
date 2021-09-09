package com.ab.quiz.pojo;

public class UserMoney {
	private long id;
	private long amount;
	private long amtLocked;
	
	public UserMoney() {
	}
	
	public UserMoney(long id, long amount, long amtLocked) {
		super();
		this.id = id;
		this.amount = amount;
		this.amtLocked = amtLocked;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public long getAmtLocked() {
		return amtLocked;
	}

	public void setAmtLocked(long amtLocked) {
		this.amtLocked = amtLocked;
	}
	
}
