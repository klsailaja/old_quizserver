package com.ab.quiz.pojo;

public class Picture {
	
	private long id;
	private int pictureType;
	private String filePath;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getPictureType() {
		return pictureType;
	}
	public void setPictureType(int pictureType) {
		this.pictureType = pictureType;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
