package com.ab.quiz.constants;

public enum ReceiptType {
	WITHDRAW (1), PROBLEM_REPORTED(2), PROBLEM_RESOLVED (3);
	
	private final int id;
	
	private ReceiptType(int id){
		this.id = id;
	}
	
	public static ReceiptType findById(int id){
		for(ReceiptType type : ReceiptType.values()){
			if(type.id == id) return type;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}