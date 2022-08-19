package com.ab.tool;

public enum PhonePaymentTypes {
	PHONEPE (1), PAYTM(2);
	
	private final int id;
	
	private PhonePaymentTypes(int id){
		this.id = id;
	}
	
	public static PhonePaymentTypes findById(int id){
		for(PhonePaymentTypes state : PhonePaymentTypes.values()){
			if(state.id == id) return state;
		}
		return null;
	}

	public int getId() {
		return id;
	}

}
