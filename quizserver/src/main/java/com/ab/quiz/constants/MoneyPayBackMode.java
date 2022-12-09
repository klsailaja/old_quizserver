package com.ab.quiz.constants;

public enum MoneyPayBackMode {
	WIN_MONEY (1), REFUND_CANCEL_GAMES(2);
	
	private final int id;
	
	private MoneyPayBackMode(int id){
		this.id = id;
	}
	
	public static MoneyPayBackMode findById(int id){
		for(MoneyPayBackMode operType : MoneyPayBackMode.values()){
			if(operType.id == id) return operType;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
