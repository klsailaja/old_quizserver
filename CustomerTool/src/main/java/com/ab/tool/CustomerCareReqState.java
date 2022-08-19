package com.ab.tool;

public enum CustomerCareReqState {
	OPEN (1), CLOSED(2), NOT_AN_ISSUE(3), CANCELLED(4);
	
	private final int id;
	
	
	private CustomerCareReqState(int id){
		this.id = id;
	}
	
	public static CustomerCareReqState findById(int id){
		for(CustomerCareReqState state : CustomerCareReqState.values()){
			if(state.id == id) return state;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
