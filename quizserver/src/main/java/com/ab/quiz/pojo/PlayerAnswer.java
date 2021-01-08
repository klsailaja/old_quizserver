package com.ab.quiz.pojo;

public class PlayerAnswer {
	
	private long userProfileId;
	private int questionNo;
	private int userAnswer = -1;
	private int timeDiff = 0;
	private boolean isFlipUsed;
	
	public int getQuestionNo() {
		return questionNo;
	}
	public void setQuestionNo(int questionNo) {
		this.questionNo = questionNo;
	}
	public int getUserAnswer() {
		return userAnswer;
	}
	public void setUserAnswer(int userAnswer) {
		this.userAnswer = userAnswer;
	}
	public int getTimeDiff() {
		return timeDiff;
	}
	public void setTimeDiff(int timeDiff) {
		this.timeDiff = timeDiff;
	}
	public long getUserProfileId() {
		return userProfileId;
	}
	public void setUserProfileId(long userProfileId) {
		this.userProfileId = userProfileId;
	}
	public boolean isFlipUsed() {
		return isFlipUsed;
	}
	public void setFlipUsed(boolean isFlipUsed) {
		this.isFlipUsed = isFlipUsed;
	}
}
