package main.java.ie.fraser.findings;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class SchedulerMain {

    final static Logger logger = LoggerFactory.getLogger(SchedulerMain.class);
    final static ConnectionFactory factory = new ConnectionFactory();
    
    public static void main(String[] args) throws Exception {
        factory.setUri(System.getenv("CLOUDAMQP_URL"));
        factory.setAutomaticRecoveryEnabled(true);
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();

        JobDetail jobDetail = newJob(HelloJob.class).build();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(repeatSecondlyForever(3600))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static class HelloJob implements Job {
        
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                String queueName = "work-queue-1";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("x-ha-policy", "all");
                channel.queueDeclare(queueName, true, false, false, params);

                String msg = "Sent at:" + System.currentTimeMillis();
                byte[] body = msg.getBytes("UTF-8");
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
                logger.info("Message Sent: " + msg);
                connection.close();
            }
            catch (Exception e) {
            	logger.info("error");
                logger.error(e.getMessage(), e);
            }

        }
        
    }

}
