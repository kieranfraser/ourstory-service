package main.java.ie.fraser.findings.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

/**
 * @author Kieran
 *
 */
public class NERService {
	
	/**
	 * 
	 * ToDo: Add multi-token functionality.
	 * @param text
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static ArrayList<String> getPeopleFromText(AbstractSequenceClassifier<CoreLabel> classifier, 
			String text) throws ClassCastException, ClassNotFoundException, IOException{
		
		ArrayList<String> people = new ArrayList<String>();

		
		List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(text);
		for (Triple<String,Integer,Integer> trip : triples) {
			people.add(text.substring(trip.second, trip.third));
		}
		return people;
	}
	
	/*public static ArrayList<String> getPeopleFromText(String text) throws ClassCastException, ClassNotFoundException, IOException{
		
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

		String[] example = {text };
		for (String str : example) {
		System.out.println(classifier.classifyToString(str));
		}
		System.out.println("---");
		
		for (String str : example) {
		// This one puts in spaces and newlines between tokens, so just print not println.
		System.out.print(classifier.classifyToString(str, "slashTags", false));
		}
		System.out.println("---");
		
		for (String str : example) {
		// This one is best for dealing with the output as a TSV (tab-separated column) file.
		// The first column gives entities, the second their classes, and the third the remaining text in a document
		System.out.print(classifier.classifyToString(str, "tabbedEntities", false));
		}
		System.out.println("---");
		
		for (String str : example) {
		System.out.println(classifier.classifyWithInlineXML(str));
		}
		System.out.println("---");
		
		for (String str : example) {
		System.out.println(classifier.classifyToString(str, "xml", true));
		}
		System.out.println("---");
		
		for (String str : example) {
		System.out.print(classifier.classifyToString(str, "tsv", false));
		}
		System.out.println("---");
		
		// This gets out entities with character offsets
		int j = 0;
		for (String str : example) {
		j++;
		List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(str);
		for (Triple<String,Integer,Integer> trip : triples) {
		System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
		trip.first(), trip.second(), trip.third, j);
		System.out.println(str.substring(trip.second, trip.third));
		}
		}
		System.out.println("---");
		
		// This prints out all the details of what is stored for each token
		int i=0;
		for (String str : example) {
		for (List<CoreLabel> lcl : classifier.classify(str)) {
		for (CoreLabel cl : lcl) {
		System.out.print(i++ + ": ");
		System.out.println(cl.toShorterString());
		}
		}
		}
		
		System.out.println("---");
		return null;
	}*/
	
	/*public static ArrayList<String> getPeopleFromText(String text){
		ArrayList<String> people = new ArrayList<>();
		
		
		
		try {
			 InputStream inputStreamTokenizer = new 
			         FileInputStream("en-token.bin");
			      TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer); 
			       
			//Instantiating the TokenizerME class 
		      TokenizerME tokenizer = new TokenizerME(tokenModel); 
		       
		      //Tokenizing the sentence in to a string array 
		      String sentence = text; 
		      String tokens[] = tokenizer.tokenize(sentence); 
		       
		      //Loading the NER-person model 
		      InputStream inputStreamNameFinder = new 
		         FileInputStream("en-ner-person.bin");       
		      TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
		      
		      //Instantiating the NameFinderME class 
		      NameFinderME nameFinder = new NameFinderME(model);       
		      
		      //Finding the names in the sentence 
		      Span nameSpans[] = nameFinder.find(tokens);        
		      
		      //Printing the names and their spans in a sentence 
		      for(Span s: nameSpans)        
		         System.out.println(s.toString()+"  "+tokens[s.getStart()]); 
			}
			catch (Exception ex) {}
		
		
		return people;
	}*/
}
