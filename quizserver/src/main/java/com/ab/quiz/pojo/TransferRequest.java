package com.ab.quiz.pojo;

public class TransferRequest {
	private int sourceAccType;
	private int destAccType;
	private String sqlQry;
	private int amount;
	
	public String getSqlQry() {
		return sqlQry;
	}
	public void setSqlQry(String sqlQry) {
		this.sqlQry = sqlQry;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getSourceAccType() {
		return sourceAccType;
	}
	public void setSourceAccType(int sourceAccType) {
		this.sourceAccType = sourceAccType;
	}
	public int getDestAccType() {
		return destAccType;
	}
	public void setDestAccType(int destAccType) {
		this.destAccType = destAccType;
	}
}
