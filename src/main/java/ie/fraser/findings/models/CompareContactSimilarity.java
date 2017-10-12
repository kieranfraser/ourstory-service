package main.java.ie.fraser.findings.models;

public class CompareContactSimilarity{
	private double score;
	private StoryContact contact;
	
	public CompareContactSimilarity(double score, StoryContact contact){
		this.score = score;
		this.contact = contact;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public StoryContact getContact() {
		return contact;
	}
	public void setContact(StoryContact contact) {
		this.contact = contact;
	}
}
