package com.ab.quiz.constants;

public enum TransactionType {
	
	LOADED (1), WITHDRAWN (2), CREDITED (3), DEBITED(4), 
	TRANSFERRED_TO(5), TRANSFERRED_FROM(6); 
	
	private final int id;
	
	private TransactionType(int id){
		this.id = id;
	}
	
	public static TransactionType findById(int id){
		for(TransactionType transactionType : TransactionType.values()){
			if(transactionType.id == id) return transactionType;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
