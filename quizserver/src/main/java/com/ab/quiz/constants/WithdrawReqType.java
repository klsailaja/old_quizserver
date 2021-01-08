package com.ab.quiz.constants;

public enum WithdrawReqType {
	BY_PHONE (1), BY_BANK (2);
	
	private final int id;
	
	private WithdrawReqType(int id){
		this.id = id;
	}
	
	public static WithdrawReqType findById(int id){
		for(WithdrawReqType type : WithdrawReqType.values()){
			if(type.id == id) return type;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
