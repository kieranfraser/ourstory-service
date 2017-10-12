package main.java.ie.fraser.findings.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class KeywordVisual implements Serializable{

	private String keyword;
	private ArrayList<String> keywordIcons;
	
	public KeywordVisual(String keyword, ArrayList<String> keywordIcons){
		this.keyword = keyword;
		this.keywordIcons = keywordIcons;
	}
	
	public KeywordVisual(){
		this.keyword = "";
		this.keywordIcons = new ArrayList<String>();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public ArrayList<String> getKeywordIcons() {
		return keywordIcons;
	}

	public void setKeywordIcons(ArrayList<String> keywordIcons) {
		this.keywordIcons = keywordIcons;
	}

	public String getRandomIcon(){
		Random rand = new Random();
		return this.getKeywordIcons().get(rand.nextInt(this.keywordIcons.size()));
	}
	
}
