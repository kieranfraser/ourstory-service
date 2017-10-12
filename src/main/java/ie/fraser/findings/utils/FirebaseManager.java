package main.java.ie.fraser.findings.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {

	
	private static final Logger LOGGER = Logger.getLogger( FirebaseManager.class.getName() );
	
	private static FirebaseDatabase database = null;
	
	private static void newInstance(){
		try {
			// Fetch the service account key JSON file contents
			FileInputStream serviceAccount;
			serviceAccount = new FileInputStream("discoveru-c59f5-firebase-adminsdk-w4xaw-9abb83e6a5.json");
			// Initialize the app with a service account, granting admin privileges
			FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
			    .setDatabaseUrl("https://discoveru-c59f5.firebaseio.com/")
			    .build();
			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Could not open firebase admin credentials or init admin app.");
		}
		database = FirebaseDatabase.getInstance();
	}
	
	public static FirebaseDatabase getDatabase(){
		if(database==null){
			newInstance();
			return database;
		}
		else return database;
	}

}
