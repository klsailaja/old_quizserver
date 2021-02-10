package com.ab.quiz.constants;

public enum TransactionType {
	
	LOADED (1), WITHDRAW_IN_PROGRESS (2), CREDITED (3), DEBITED(4), 
	TRANSFERRED_TO(5), TRANSFERRED_FROM(6), WITHDRAW_CANCELLED (7), WITHDRAW_CLOSED (8); 
	
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
