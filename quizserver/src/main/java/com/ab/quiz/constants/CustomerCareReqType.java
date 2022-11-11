package com.ab.quiz.constants;

public enum CustomerCareReqType {
	ADDED_MONEY_NOT_UPDATED (1), WIN_MONEY_NOT_ADDED(2), WD_NOT_PROCESSED(3), 
	QUESTION_WRONG(4), OTHERS(5),CANCELLED_GAME_MONEY_NOT_ADDED(6);
	
	private final int id;
	
	private CustomerCareReqType(int id){
		this.id = id;
	}
	
	public static CustomerCareReqType findById(int id){
		for(CustomerCareReqType state : CustomerCareReqType.values()){
			if(state.id == id) return state;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
