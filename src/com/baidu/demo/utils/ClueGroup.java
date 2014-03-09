package com.baidu.demo.utils;

import java.util.List;

public class ClueGroup {
	private String id;
	private String keywords = "";
	private long phoneTime = 0L;
	private String userText = "";
	private List<Clue> clues;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public long getPhoneTime() {
		return phoneTime;
	}
	public void setPhoneTime(long phoneTime) {
		this.phoneTime = phoneTime;
	}
	public String getUserText() {
		return userText;
	}
	public void setUserText(String userText) {
		this.userText = userText;
	}
	public List<Clue> getClues() {
		return clues;
	}
	public void setClues(List<Clue> clues) {
		this.clues = clues;
	}
	
}
