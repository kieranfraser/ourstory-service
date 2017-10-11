package ie.fraser.findings.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.types.InstanceList;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import ie.fraser.findings.models.*;
import ie.fraser.findings.models.StoryContact.App;

public class Analysis{
	
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private InstanceList instanceList;

	private static final Logger LOGGER = Logger.getLogger( Analysis.class.getName() );
	
	
	public Analysis(){
		try {
			classifier = CRFClassifier.getClassifier("classifiers/english.all.3class.distsim.crf.ser.gz");
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        instanceList = new InstanceList (new SerialPipes(pipeList));
	}
	
	public void run(StoryInterceptedNotification notification, String userId) {

        LOGGER.info("New analysis thread started for: "+notification.getId());
		try {
			identifyPeople(userId, notification);
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 1. Infer the sender - now using stanford core nlp instead of freme
	 * @param userId
	 * @param notification
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	private void identifyPeople(String userId, StoryInterceptedNotification notification) throws ClassCastException, ClassNotFoundException, IOException{

		
		String tickerText = notification.getTickerText()!=null?notification.getTickerText():" ";
		String extraLineText = notification.getBigText()!=null?notification.getBigText():" ";
		String extraText =	notification.getTitleBig()!=null?notification.getTitleBig():" ";
		String extraBigText =	notification.getTitle()!=null?notification.getTitle():" ";
		String text =	notification.getText()!=null?notification.getText():" ";
		String textLines = notification.getTextLines()!=null?notification.getTextLines():" ";
		
		
		String textToCheck = tickerText+" "+extraLineText+" "+extraText+" "+extraBigText+" "+text+" "+textLines;
									

		// Get the people associated with the notification
		ArrayList<String> names = NERService.getPeopleFromText(classifier, textToCheck);

		// Get the keywords from the text
		ArrayList<String> keywordStrings = KeywordExtractionService.featureExtraction(instanceList, textToCheck);        

		// Get visuals associated with the keywords
        ArrayList<KeywordVisual> keywordVisuals = identifyKeywordIconsPixabay(keywordStrings);

		// Compare extracted names with user contacts for overlap to create stories
		compareToUserContacts(userId, notification, names, keywordStrings, keywordVisuals);

	}
	
	/**
	 * Will be used later to find associated things with keywords - not currently used.
	 * @param keywordStrings
	 * @return
	 */
	private ArrayList<String> identifyKeywordIconsDuck(ArrayList<String> keywordStrings) {

		try {
			HttpResponse<JsonNode> response = Unirest.get("https://duckduckgo-duckduckgo-zero-click-info.p.mashape.com/?format=json&no_html=1&no_redirect=1&q=Science&skip_disambig=1")
					.header("Accept", "application/json")
					.asJson();
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;		
	}
	
	/**
	 * Used to find visual representations of keywords of notification to add to story.
	 * @param keywordStrings
	 * @return
	 */
	private ArrayList<KeywordVisual> identifyKeywordIconsPixabay(ArrayList<String> keywordStrings){
		ArrayList<KeywordVisual> keywordVisuals = new ArrayList<>();

		for(String key: keywordStrings){
			ArrayList<String> urls = new ArrayList<>();
			
			try {
				HttpResponse<JsonNode> response = null;
				if(key.trim().contains(" ")){
					response = Unirest.get("https://pixabay.com/api/?key=6091487-bd3476122fd18802e13fe9b4c&q="+prepKeywordForQuery(key))
							.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
							.header("Accept", "application/json")
							.asJson();
				}
				else{
					response = Unirest.get("https://pixabay.com/api/?key=6091487-bd3476122fd18802e13fe9b4c&q="+key)
							.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
							.header("Accept", "application/json")
							.asJson();
				}
				if(!response.getBody().toString().contains("ERROR")){
					JSONObject res = new JSONObject(response.getBody().toString());
					JSONArray array = res.getJSONArray("hits");
					if(array.length()>0){
						for(int i=0; (i<array.length() && i<3);i++){
							urls.add(array.getJSONObject(i).getString("previewURL"));
						}
						keywordVisuals.add(new KeywordVisual(key, urls));
					}
				}	
			} catch (UnirestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Unable to convert response to jsonobject "+e.getMessage());
				e.printStackTrace();
			}
		}
        return keywordVisuals;
	}
	
	/**
	 * Prepares the keywords for the Pixabay query
	 * @param key
	 * @return
	 */
	private String prepKeywordForQuery(String key) {
		String keyFormatted = "";
		for(String word : key.split(" ")) {
		    keyFormatted += "+"+word;
		}
		return keyFormatted;		
	}

	/*public static class ParameterStringBuilder {
	    public static String getParamsString(Map<String, String> params) 
	      throws UnsupportedEncodingException{
	        StringBuilder result = new StringBuilder();
	 
	        for (Map.Entry<String, String> entry : params.entrySet()) {
	          result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
	          result.append("=");
	          result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
	          result.append("&");
	        }
	 
	        String resultString = result.toString();
	        return resultString.length() > 0
	          ? resultString.substring(0, resultString.length() - 1)
	          : resultString;
	    }
	}*/

	/**
	 * Could do some recommendation here - if two contacts found in separate lists and have similar name, recommend 
	 * the same person?
	 * @param userId
	 * @param notification
	 * @param names
	 */
	private void compareToUserContacts(String userId, StoryInterceptedNotification notification, ArrayList<String> names, 
			ArrayList<String> topics, ArrayList<KeywordVisual> visuals){
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+userId);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot) {
		    	StoryUser user = dataSnapshot.getValue(StoryUser.class);

		        if(!names.isEmpty()){
		        	
		        	ArrayList<StoryContact> relevantPhoneContacts = new ArrayList<>();
			        ArrayList<StoryContact> relevantFacebookContacts = new ArrayList<>();
			        ArrayList<StoryContact> relevantTwitterContacts = new ArrayList<>();

			        boolean stranger = true;
			     
			        for(String name: names){
				        if(notification.getAppPackage().trim().contains("facebook")){
				        	if(user.getOptions().isFacebookOn()){
					        	CompareContactSimilarity facebookContactSimilarity = checkContactListForSender(user.getFacebookContacts(), name);
						        if(facebookContactSimilarity!=null){
						        	relevantFacebookContacts.add(facebookContactSimilarity.getContact());
						        	if(stranger){
						        		stranger = false;
						        	}			      
						        }			        	
					        }
				        }
				        else if(notification.getAppPackage().trim().contains("twitter")){
				        	if(user.getOptions().isTwitterOn()){
					        	CompareContactSimilarity twitterContactSimilarity = checkContactListForSender(user.getTwitterContacts(), name);
						        if(twitterContactSimilarity!=null){
						        	relevantTwitterContacts.add(twitterContactSimilarity.getContact());
						        	if(stranger){
						        		stranger = false;
						        	}
						        }			        	
					        }
				        }
				        else if(notification.getAppPackage().trim().contains("whatsapp") ||
				        		notification.getAppPackage().trim().contains("snapchat") ||
				        		notification.getAppPackage().trim().contains("viber")) {
				        	CompareContactSimilarity phoneContactSimilarity = checkContactListForSender(user.getPhoneContacts(), name);
					        if(phoneContactSimilarity!=null){
					        	relevantPhoneContacts.add(phoneContactSimilarity.getContact());
					        	if(stranger){
					        		stranger = false;
					        	}
					        }
				        }				        
			        }

			        if(stranger){
			        	for(String name: names){
				        	LOGGER.log(Level.INFO, "Creating unkown user: 	"+name);
				        	
				        	//createUnkownContactAndAddStory(userId, user, notification, name, topics, visuals);
			        	}
			        }
			        else{
			        	
			        	if(notification.getAppPackage().contains("facebook")){
				        	// remove duplicates from contact list
				        	removeDuplicateContacts(relevantFacebookContacts);
			        		addStoryToRelevantContacts(userId, user, notification, "facebookContacts", relevantFacebookContacts, topics, visuals);
			        	}
			        	else if(notification.getAppPackage().contains("twitter")){
				        	// remove duplicates from contact list
			        		removeDuplicateContacts(relevantTwitterContacts);
			        		addStoryToRelevantContacts(userId, user, notification, "twitterContacts", relevantTwitterContacts, topics, visuals);
			        	}
			        	else if (notification.getAppPackage().trim().contains("whatsapp") ||
				        		notification.getAppPackage().trim().contains("snapchat") ||
				        		notification.getAppPackage().trim().contains("viber")){
				        	// remove duplicates from contact list
			        		removeDuplicateContacts(relevantPhoneContacts);
			        		addStoryToRelevantContacts(userId, user, notification, "phoneContacts", relevantPhoneContacts, topics, visuals);
			        	}
			        }
		        }
				FirebaseDatabase.getInstance().getReference("notifications/"+user.getId()+"/"+notification.getId()).removeValue();
		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError) {
		        LOGGER.log(Level.SEVERE, "Unable to reach firebase for name comparison. Error: "+databaseError);
		    }
		});
	}
	
	private static void removeDuplicateContacts(ArrayList<StoryContact> contacts){
		Set<StoryContact> uniqueSet = new HashSet<>();
		uniqueSet.addAll(contacts);
		contacts.clear();
		contacts.addAll(uniqueSet);
	}
	
	/**
	 * Could use the key value of the notification as the story id as opposed the "when"?
	 * @param userId
	 * @param user
	 * @param notification
	 */
	private void createUnkownContactAndAddStory(String userId, StoryUser user,
			StoryInterceptedNotification notification, String name, ArrayList<String> topics, ArrayList<KeywordVisual> visuals){
		/*Story newStory = new Story(notification.getId(), notification.getAppPackage(), notification.getTickerText());
		newStory.setTopics(topics);
		newStory.setKeywordVisuals(visuals);
		
		StoryContact contact = new StoryContact("123", name, App.PHONE);
		contact.addStory(newStory);
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+userId+"/unknownContacts/");
		user.addUknownContact(contact);
		ref.setValue(user.getUnknownContacts());*/
	}
	
	private void addStoryToRelevantContacts(String userId, StoryUser user, StoryInterceptedNotification notification,
			String relevantList, ArrayList<StoryContact> contacts, ArrayList<String> topics, ArrayList<KeywordVisual> visuals){
		if(!contacts.isEmpty()){
			Story newStory = new Story(notification.getId(),
					App.valueOf(cleanAppName(notification.getAppPackage())),
							notification.getTickerText());
			newStory.setTopics(topics);
			newStory.setKeywordVisuals(visuals);
			DatabaseReference ref = null;

			for(StoryContact contact: contacts){
				String contactId = contact.getId();
				if(contactId.contains("."))
					contactId = contactId.replace(".", "");
				//System.out.println("This is the contactId "+contactId);
				ref = FirebaseDatabase.getInstance().getReference("stories/"+userId+"/"+contactId+"/"+notification.getId());
				ref.setValue(newStory);
				
				String refCount ="";
				String contactPos = "";
				
				switch(relevantList){
				case "phoneContacts":
					LOGGER.log(Level.INFO, "Adding story to phone contact: "+contact.getName());
					refCount = "stories/"+userId+"/"+contactId+"/";
					contactPos = "users/"+userId+"/phoneContacts/"+user.getPhoneContacts().indexOf(contact)+"/totalStories";
					updateTotal(refCount, contactPos);
					break;
				case "facebookContacts":
					LOGGER.log(Level.INFO, "Adding story to facebook contact: "+contact.getName());
					refCount = "stories/"+userId+"/"+contactId+"/";
					contactPos = "users/"+userId+"/facebookContacts/"+user.getFacebookContacts().indexOf(contact)+"/totalStories";
					updateTotal(refCount, contactPos);
					break;
				case "twitterContacts":
					LOGGER.log(Level.INFO, "Adding story to twitter contact: "+contact.getName());
					refCount = "stories/"+userId+"/"+contactId+"/";
					contactPos = "users/"+userId+"/twitterContacts/"+user.getTwitterContacts().indexOf(contact)+"/totalStories";
					updateTotal(refCount, contactPos);
					break;
				}
				
			}
			/**/
		}
	}
	
	private void updateTotal(String reference, String referenceContact){
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference(reference);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				long count = snapshot.getChildrenCount();
				DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference(referenceContact);
				ref2.setValue(count);
			}
			
			@Override
			public void onCancelled(DatabaseError error) {}
		});
	}
	
	private CompareContactSimilarity checkContactListForSender(ArrayList<StoryContact> contacts, String relevantName){
		CompareContactSimilarity response = null;
		double highestVal = 0.8;
		if(contacts!=null && relevantName!=null){
			for(StoryContact contact:contacts){
				double val = StringUtils.getJaroWinklerDistance(contact.getName(), relevantName.trim());
				if(val > highestVal){
					highestVal = val;
					response = new CompareContactSimilarity(val, contact);
				}
			}
		}		
		return response;
	}
	
	private String cleanAppName(String packageName){
		if(packageName.contains("facebook")){
			return "FACEBOOK";
		}
		else if(packageName.contains("twitter")){
			return "TWITTER";
		}
		else return "PHONE";
	}
	

}
