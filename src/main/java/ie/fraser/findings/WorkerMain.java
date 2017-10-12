package main.java.ie.fraser.findings;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import main.java.ie.fraser.findings.analysis.Analysis;
import main.java.ie.fraser.findings.models.StoryInterceptedNotification;
import main.java.ie.fraser.findings.utils.FirebaseManager;


public class WorkerMain {

    final static Logger logger = LoggerFactory.getLogger(WorkerMain.class);
	private static final FirebaseDatabase database = FirebaseManager.getDatabase();

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(System.getenv("CLOUDAMQP_URL"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        String queueName = "work-queue-1";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x-ha-policy", "all");
        channel.queueDeclare(queueName, true, false, false, params);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);
       
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(); 
            if (delivery != null) {
                String msg = new String(delivery.getBody(), "UTF-8");
                logger.info("Message Received: " + msg);
                logger.info("Starting work.");
                
                //doWork();
                
                
                
                
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        }

    }
    
    private static void doWork(){

		final Analysis analysis = new Analysis();
    	DatabaseReference ref = database.getReference("notifications/");
		
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				int numThreads = 0;
				for(DataSnapshot userSnapshot: snapshot.getChildren()){
					final String userId = userSnapshot.getKey();
					
					for(DataSnapshot notificationSnapshot: userSnapshot.getChildren()){
						
						StoryInterceptedNotification notification = notificationSnapshot.getValue(StoryInterceptedNotification.class);
					    analysis.run(notification, userId);
					    numThreads++;
						
					}
					
				}
				logger.info(numThreads+" analysis threads started.");
				
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				
			}
		});
    }

}
