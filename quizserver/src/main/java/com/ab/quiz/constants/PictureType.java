package com.ab.quiz.constants;

public enum PictureType {
	
	TICKET_OPENED(1), TICKET_CLOSED(2), WDRECEIPT(3), AADHAR_FRONT(4), AADHAR_BACK(5), PAN(6), MOVIE_QUESTION(7); 
	
	private final int id;
	
	private PictureType(int id){
		this.id = id;
	}
	
	public static PictureType findById(int id){
		for(PictureType type : PictureType.values()){
			if(type.id == id) return type;
		}
		return null;
	}

	public int getId() {
		return id;
	}
}
