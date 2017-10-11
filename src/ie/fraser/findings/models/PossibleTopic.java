package ie.fraser.findings.models;

import java.util.ArrayList;
import java.util.List;

public class PossibleTopic implements Comparable<PossibleTopic> {

	private Integer index;
	private Double distValue;
	
	public PossibleTopic(Integer index, Double distValue){
		this.index = index;
		this.distValue = distValue;
	}
			
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Double getDistValue() {
		return distValue;
	}

	public void setDistValue(Double distValue) {
		this.distValue = distValue;
	}
	
	public static List<Integer> getTopIndices(ArrayList<PossibleTopic> possibleTopics, int range){
		List<Integer> topIndices = new ArrayList<Integer>();
		for(int i=0; (i<range && i<possibleTopics.size()); i++){
			topIndices.add(possibleTopics.get(i).getIndex());
		}
		return topIndices;
	}

	@Override
	public int compareTo(PossibleTopic arg0) {
		
		if(this.distValue == arg0.getDistValue()){
			return 0;
		}
		if((this.distValue - arg0.getDistValue()) < 0){
			return 11;
		}
		else return -1;
	}
	
}
