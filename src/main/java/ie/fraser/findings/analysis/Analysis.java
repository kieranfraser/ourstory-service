package main.java.ie.fraser.findings.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import cc.mallet.types.InstanceList;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import main.java.ie.fraser.findings.models.CompareContactSimilarity;
import main.java.ie.fraser.findings.models.KeywordVisual;
import main.java.ie.fraser.findings.models.Story;
import main.java.ie.fraser.findings.models.StoryContact;
import main.java.ie.fraser.findings.models.StoryContact.App;
import main.java.ie.fraser.findings.models.StoryInterceptedNotification;
import main.java.ie.fraser.findings.models.StoryUser;

public class Analysis implements Runnable{
	
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private InstanceList instanceList;
	private StoryInterceptedNotification notification;
	private String userId;

	private static final Logger LOGGER = Logger.getLogger( Analysis.class.getName() );
	
	
	public Analysis(AbstractSequenceClassifier<CoreLabel> classifier,
			InstanceList instanceList, StoryInterceptedNotification notification, String userId){
		this.classifier = classifier;
		this.instanceList = instanceList;
		this.notification = notification;
		this.userId = userId;
	}
	
	/*
	 * Runnable:
	 * 
	 * 	identify people in notification 
	 * 	- if none, delete notification
	 *	- if yes,
	 *	match people with user contacts
	 *	- if none, delete notification
	 *	- if yes,
	 *	get top 6 keywords from notification
	 *	- if none, save the story, delete notification
	 *	- if yes,
	 *	get 4 pictures from top 6 keywords
	 *	- save the story, delete the notification
	*/
	
	@Override 
	public void run() {
		LOGGER.info("Thread started for notification: "+notification.getId());
		identifyPeople();
	}
	
	/**
	 * Using NER, identify the names of people associated with the notification. If found, proceed to match 
	 * with user contacts.
	 */
	public void identifyPeople(){
		String notificationText = notificationTextExtraction();
		ArrayList<String> names = new ArrayList<>();
		try {
			names = NERService.getPeopleFromText(classifier, notificationText);
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			LOGGER.log(Level.SEVERE, "NERService extraction error caught for text: "+notificationText, e.getMessage());
		}
		if(!names.isEmpty()){
			matchNotificationToUserContact(names, notificationText);
		}
		else{
			removeNotification();
		}
	}
	
	/**
	 * Extracts the raw text from the notification for analysis
	 * @return the raw notification text.
	 */
	public String notificationTextExtraction(){		
		String tickerText = notification.getTickerText()!=null?notification.getTickerText():" ";
		String extraLineText = notification.getBigText()!=null?notification.getBigText():" ";
		String extraText =	notification.getTitleBig()!=null?notification.getTitleBig():" ";
		String extraBigText =	notification.getTitle()!=null?notification.getTitle():" ";
		String text =	notification.getText()!=null?notification.getText():" ";
		String textLines = notification.getTextLines()!=null?notification.getTextLines():" ";
		return tickerText+" "+extraLineText+" "+extraText+" "+extraBigText+" "+text+" "+textLines;
	}
	
	/**
	 * Removes the Notification from FireBase.
	 */
	public void removeNotification(){
		LOGGER.info("Removing notification: "+notification.getId());
		FirebaseDatabase.getInstance().getReference("notifications/"+userId+"/"+notification.getId()).removeValue();
	}
	
	/**
	 * Matches the names associated with the notification to user contacts.
	 * If matched, proceed to identify keywords, icons and add a new story.
	 * @param names
	 * @param notificationText
	 */
	public void matchNotificationToUserContact(final ArrayList<String> names, final String notificationText){
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+userId);
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot) {
		    	StoryUser user = dataSnapshot.getValue(StoryUser.class);

	        	ArrayList<StoryContact> relevantPhoneContacts = new ArrayList<StoryContact>();
		        ArrayList<StoryContact> relevantFacebookContacts = new ArrayList<StoryContact>();
		        ArrayList<StoryContact> relevantTwitterContacts = new ArrayList<StoryContact>();

		        boolean stranger = true;
		     
		        // for loop fills up the relevant contact lists above
		        for(String name: names){
		        	switch(cleanAppName(notification.getAppPackage())){
			        	case "FACEBOOK":
			        		if(user.getOptions().isFacebookOn()){
					        	CompareContactSimilarity facebookContactSimilarity = checkContactListForSender(user.getFacebookContacts(), name);
						        if(facebookContactSimilarity!=null){
						        	relevantFacebookContacts.add(facebookContactSimilarity.getContact());
						        	if(stranger){
						        		stranger = false;
						        	}			      
						        }			        	
					        }
			        		break;
			        	case "TWITTER":
			        		if(user.getOptions().isTwitterOn()){
					        	CompareContactSimilarity twitterContactSimilarity = checkContactListForSender(user.getTwitterContacts(), name);
						        if(twitterContactSimilarity!=null){
						        	relevantTwitterContacts.add(twitterContactSimilarity.getContact());
						        	if(stranger){
						        		stranger = false;
						        	}
						        }			        	
					        }
			        		break;
			        	default:
			        		CompareContactSimilarity phoneContactSimilarity = checkContactListForSender(user.getPhoneContacts(), name);
					        if(phoneContactSimilarity!=null){
					        	relevantPhoneContacts.add(phoneContactSimilarity.getContact());
					        	if(stranger){
					        		stranger = false;
					        	}
					        }
			        		break;
		        	}				        
		        }

		        // If no contact found - might want to save for later? for now, delete and end.
		        if(!stranger){

		        	ArrayList<String> topSixKeywords = KeywordExtractionService.featureExtraction(instanceList, notificationText);
		        	ArrayList<KeywordVisual> topFourVisuals = getKeywordIcons(topSixKeywords);
		        	
		        	switch(cleanAppName(notification.getAppPackage())){
			        	case "FACEBOOK":
			        		if(!relevantFacebookContacts.isEmpty()){
					        	removeDuplicateContacts(relevantFacebookContacts);
				        		addStoryToRelevantContacts(user, "facebookContacts", relevantFacebookContacts, topSixKeywords, topFourVisuals);
			        		}
			        		break;
			        	case "TWITTER":
			        		if(!relevantTwitterContacts.isEmpty()){
				        		removeDuplicateContacts(relevantTwitterContacts);
				        		addStoryToRelevantContacts(user, "twitterContacts", relevantTwitterContacts, topSixKeywords, topFourVisuals);
			        		}
			        		break;
			        	default:
			        		if(!relevantPhoneContacts.isEmpty()){
				        		removeDuplicateContacts(relevantPhoneContacts);
				        		addStoryToRelevantContacts(user, "phoneContacts", relevantPhoneContacts, topSixKeywords, topFourVisuals);			        			
			        		}
			        		break;
		        	}
		        }
		        removeNotification();
		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError) {
		        LOGGER.log(Level.SEVERE, "Unable to reach firebase for name comparison. Error: "+databaseError);
		    }
		});
	}
	
	/**
	 * Clean the app package name to ensure matching
	 * @param packageName - raw text from notification
	 * @return usable app name
	 */
	private String cleanAppName(String packageName){
		if(packageName.contains("facebook")){
			return "FACEBOOK";
		}
		else if(packageName.contains("twitter")){
			return "TWITTER";
		}
		else return "PHONE";
	}
	
	/**
	 * Using JaroWinklerDistance algorithm to identify similarity in contact names of notification and 
	 * user contact list.
	 * @param contacts - the app specific contacts of the user
	 * @param relevantName - the name found to be associated with the notification
	 * @return the score of the relevantContact and the user they are found to be in the user's contact list.
	 */
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
	
	/**
	 * Remove duplicate relevant contacts - this would occur if the same name is mentioned more than once.
	 * TODO: cater for scenarios where two different people of same name occur in notification.
	 * @param contacts
	 */
	private static void removeDuplicateContacts(ArrayList<StoryContact> contacts){
		Set<StoryContact> uniqueSet = new HashSet<StoryContact>();
		uniqueSet.addAll(contacts);
		contacts.clear();
		contacts.addAll(uniqueSet);
	}
	
	/**
	 * Gets pictures from splashbase and pixabay to use as representations of the notification in the mobile application.
	 * @param topSixKeywords
	 * @return
	 */
	private ArrayList<KeywordVisual> getKeywordIcons(ArrayList<String> topSixKeywords){
		ArrayList<KeywordVisual> keywordVisuals = new ArrayList<KeywordVisual>();
		
		for(int i=0; i<4; i++){
			String key = topSixKeywords.get(i);
			ArrayList<String> urls = new ArrayList<String>();
			
			// try splashbase
			
			try {
				HttpResponse<JsonNode> response = null;
				if(key.trim().contains(" ")){
					response = Unirest.get("http://www.splashbase.co/api/v1/images/search?query="+prepKeywordForQuery(key))
							.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
							.header("Accept", "application/json")
							.asJson();
				}
				else{
					response = Unirest.get("http://www.splashbase.co/api/v1/images/search?query="+key)
							.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
							.header("Accept", "application/json")
							.asJson();
				}
				JSONObject res = new JSONObject(response.getBody().toString());
				JSONArray array = res.getJSONArray("images");
				if(array.length()>0){
					urls.add(array.getJSONObject(0).getString("url"));
					keywordVisuals.add(new KeywordVisual(key, urls));
				}
				else{
					// try pixabay
					
					HttpResponse<JsonNode> responsePixa = null;
					if(key.trim().contains(" ")){
						responsePixa = Unirest.get("https://pixabay.com/api/?key=6702831-7da991c0b5c7f8168cd371e42&q="+prepKeywordForQuery(key))
								.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
								.header("Accept", "application/json")
								.asJson();
					}
					else{
						responsePixa = Unirest.get("https://pixabay.com/api/?key=6702831-7da991c0b5c7f8168cd371e42&q="+key)
								.header("X-Mashape-Key", "PErRRS41JSmshAzPG6Z8kPLkRTCxp1uSuh4jsnbJtspOcyc4Ii")
								.header("Accept", "application/json")
								.asJson();
					}
					if(!responsePixa.getBody().toString().contains("ERROR")){
						JSONObject resPixa = new JSONObject(responsePixa.getBody().toString());
						JSONArray arrayPixa = resPixa.getJSONArray("hits");
						if(arrayPixa.length()>0){
							urls.add(arrayPixa.getJSONObject(0).getString("previewURL"));
							keywordVisuals.add(new KeywordVisual(key, urls));
						}
					}	
				}
			} catch(Exception e){
				LOGGER.warning("Error retrieving images for notification: "+notification.getId());
			}
		}
        return keywordVisuals;
	}
	
	
	/**
	 * Prepares the keywords for the Pixabay query
	 * @param key
	 * @return
	 */
	private static String prepKeywordForQuery(String key) {
		String keyFormatted = "";
		for(String word : key.split(" ")) {
		    keyFormatted += "+"+word;
		}
		return keyFormatted;		
	}
	
	
	/**
	 * Could use the key value of the notification as the story id as opposed the "when"?
	 * @param userId
	 * @param user
	 * @param notification
	 */
	/*private void createUnkownContactAndAddStory(String userId, StoryUser user,
			StoryInterceptedNotification notification, String name, ArrayList<String> topics, ArrayList<KeywordVisual> visuals){
		Story newStory = new Story(notification.getId(), notification.getAppPackage(), notification.getTickerText());
		newStory.setTopics(topics);
		newStory.setKeywordVisuals(visuals);
		
		StoryContact contact = new StoryContact("123", name, App.PHONE);
		contact.addStory(newStory);
		
		DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+userId+"/unknownContacts/");
		user.addUknownContact(contact);
		ref.setValue(user.getUnknownContacts());
	}
	*/
	
	
	/**
	 * Add a story to the relevant matching user contact.
	 * @param user - the user object
	 * @param relevantList - the application the user contact is associated with e.g. FaceBook, Twitter etc.
	 * @param contacts - the list of matched contacts
	 * @param topics - the top 6 keywords associated with the notification
	 * @param visuals - 4 picture icons associated with the notification (based on keywords)
	 */
	private void addStoryToRelevantContacts(StoryUser user,	String relevantList, ArrayList<StoryContact> contacts, ArrayList<String> topics, ArrayList<KeywordVisual> visuals){
		Story newStory = new Story(	notification.getId(),
									App.valueOf(cleanAppName(notification.getAppPackage())),
									notification.getTickerText());
		newStory.setTopics(topics);
		newStory.setKeywordVisuals(visuals);
		DatabaseReference ref = null;

		for(StoryContact contact: contacts){
			String contactId = contact.getId();
			if(contactId.contains("."))
				contactId = contactId.replace(".", "");

			ref = FirebaseDatabase.getInstance().getReference("stories/"+userId+"/"+contactId+"/"+notification.getId());
			ref.setValue(newStory);
			
			String refCount ="";
			String contactPos = "";
			
			switch(relevantList){
			case "phoneContacts":
				LOGGER.info("Adding story to phone contact: "+contact.getName());
				refCount = "stories/"+userId+"/"+contactId+"/";
				contactPos = "users/"+userId+"/phoneContacts/"+user.getPhoneContacts().indexOf(contact)+"/totalStories";
				updateTotal(refCount, contactPos);
				break;
			case "facebookContacts":
				LOGGER.info("Adding story to facebook contact: "+contact.getName());
				refCount = "stories/"+userId+"/"+contactId+"/";
				contactPos = "users/"+userId+"/facebookContacts/"+user.getFacebookContacts().indexOf(contact)+"/totalStories";
				updateTotal(refCount, contactPos);
				break;
			case "twitterContacts":
				LOGGER.info("Adding story to twitter contact: "+contact.getName());
				refCount = "stories/"+userId+"/"+contactId+"/";
				contactPos = "users/"+userId+"/twitterContacts/"+user.getTwitterContacts().indexOf(contact)+"/totalStories";
				updateTotal(refCount, contactPos);
				break;
			}
		}
			
	}
	
	/**
	 * Update the total number of stories in the FireBase user object. Necessary for mobile App summary.
	 * @param reference
	 * @param referenceContact
	 */
	private void updateTotal(String reference, final String referenceContact){
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
	
	
	/*
	 
	*//**
	 * Will be used later to find associated things with keywords - not currently used.
	 * @param keywordStrings
	 * @return
	 *//*
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

	public static class ParameterStringBuilder {
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
	}
	
	*/
	

}
