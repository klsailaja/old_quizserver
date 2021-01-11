package com.ab.quiz.pojo;

public class Question {
	
	private int questionNumber;
	private long category;
	private int timeLine;
	private String nStatement;
	private String nOptionA;
	private String nOptionB;
	private String nOptionC;
	private String nOptionD;
	private int correctOption;
	private long questionStartTime;
	
	public long getCategory() {
		return category;
	}
	public void setCategory(long category) {
		this.category = category;
	}
	
	public int getTimeLine() {
		return timeLine;
	}
	public void setTimeLine(int timeline) {
		this.timeLine = timeline;
	}
	
	public int getQuestionNumber() {
		return questionNumber;
	}
	public void setQuestionNumber(int questionNumber) {
		this.questionNumber = questionNumber;
	}
	
	public String getnStatement() {
		return nStatement;
	}
	public void setnStatement(String nStatement) {
		this.nStatement = nStatement;
	}
	public String getnOptionA() {
		return nOptionA;
	}
	public void setnOptionA(String nOptionA) {
		this.nOptionA = nOptionA;
	}
	public String getnOptionB() {
		return nOptionB;
	}
	public void setnOptionB(String nOptionB) {
		this.nOptionB = nOptionB;
	}
	public String getnOptionC() {
		return nOptionC;
	}
	public void setnOptionC(String nOptionC) {
		this.nOptionC = nOptionC;
	}
	public String getnOptionD() {
		return nOptionD;
	}
	public void setnOptionD(String nOptionD) {
		this.nOptionD = nOptionD;
	}
	public int getCorrectOption() {
		return correctOption;
	}
	public void setCorrectOption(int correctOption) {
		this.correctOption = correctOption;
	}
	public long getQuestionStartTime() {
		return questionStartTime;
	}
	public void setQuestionStartTime(long questionStartTime) {
		this.questionStartTime = questionStartTime;
	}
	@Override
	public String toString() {
		return "Question [category=" + category + ", timeLine=" + timeLine + ", nStatement=" + nStatement
				+ ", nOptionA=" + nOptionA + ", nOptionB=" + nOptionB + ", nOptionC=" + nOptionC + ", nOptionD="
				+ nOptionD + ", correctOption=" + correctOption + "]";
	}
}
