package main.java.ie.fraser.findings.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class KeywordExtractionService {
	
	public static ArrayList<String> featureExtraction(InstanceList instanceList, String input){
		
        
        instanceList.addThruPipe(new Instance(input, null, "test instance", null));

		ArrayList<String> features = new ArrayList<String>();
        //for(String value: instances.get(0).getAlphabet())
        Alphabet alpha =  instanceList.get(0).getAlphabet();
		for(int i = 0; i<alpha.size(); i++){
			features.add((String) alpha.lookupObject(i));
		}
		return features;
	}

	
	/*private ParallelTopicModel model;
	private int numTopics;
	private Formatter out;
	private Alphabet dataAlphabet;
	private InstanceList instances;
	
	public KeywordExtractionService(String input) throws IOException{
		// training
		

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        instances = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File("testingFile.txt")), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields
        

        instances.addThruPipe(new Instance(input, null, "test instance", null));
        System.out.print("name " + instances.get(0).getName());
        System.out.println("data: "+instances.get(0).getData());
        

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        numTopics = 100;
        model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        //model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        for(int i=0; i<tokens.getLength(); i++){
        	System.out.println(tokens.get(i));
        }
        LabelSequence topics = model.getData().get(0).topicSequence; 
        
        out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        
	}
	
	*//**
	 * Function to extract keywords from input text.
	 * @param input
	 * @return
	 *//*
	public ArrayList<String> getKeywords(String input){
		ArrayList<String> extractedKeywords = new ArrayList<>();
		
        //System.out.println(out);
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(input, null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities[0]);
        System.out.println("0\t" + testProbabilities[1]);
                
        ArrayList<PossibleTopic> possibleTopics = new ArrayList<>();
        int index = 0;
        for(Double val: testProbabilities){
        	possibleTopics.add(new PossibleTopic(index, val));
        	index++;
        }
        Collections.sort(possibleTopics);        
        
        for(int y=0; y<5; y++){
        	PossibleTopic p = possibleTopics.get(y);
        	
        	Iterator<IDSorter> iteratorB = topicSortedWords.get(p.getIndex()).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", y, testProbabilities[p.getIndex()]);
            int rankB = 0;
            while (iterator.hasNext() && rankB < 5) {
                IDSorter idCountPair = iteratorB.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rankB++;
            }
            System.out.println(out);
        }
		
		return extractedKeywords;
	}*/
	
	
}
