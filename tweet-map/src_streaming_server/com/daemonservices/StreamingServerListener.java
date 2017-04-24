package com.daemonservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.middleware.AlchemyProxy;
import com.middleware.ElasticSearchProxy;
import com.middleware.SNSProxy;
import com.middleware.SQSQueueProxy;
import com.tasks.FetchSQSMessages;
import com.tasks.FetchTweetsTask;

/**
 * @author Deepesh
 */
public class StreamingServerListener implements ServletContextListener {

	private static StreamingServerListener listener;

	public static final Properties awsCredentialsFile = StreamingServerListener.
			loadPropertiesFile("aws.properties");
	public static final Properties twitterConfFile = StreamingServerListener.
			loadPropertiesFile("twitter.properties");
	public static final Properties alchemyConfFile = StreamingServerListener.
			loadPropertiesFile("alchemy.properties");

	private static final int numOfThreads = 10;
	
	private ElasticSearchProxy esProxy;
	private SQSQueueProxy sqsProxy;
	private SNSProxy snsProxy;
	private AlchemyProxy alchemyProxy;
	private ExecutorService exeService;
	
	public StreamingServerListener() throws UnknownHostException {
		// Constructor have to be public, else this not work as Listener.
		// So making sure, just one instance of the listener is moved around
		if(StreamingServerListener.listener == null) {
			this.exeService = Executors.newFixedThreadPool(StreamingServerListener.numOfThreads);
			/*System.out.println("============= Establising Connection with Elastic-Search =============");
			this.esProxy = new ElasticSearchProxy();
			System.out.println("============= Established Connection with Elastic-Search =============\n");*/
			System.out.println("============= Establising Connection with SQS =============");
			this.sqsProxy = new SQSQueueProxy();
			System.out.println("============= Established Connection with SQS =============\n");
			System.out.println("============= Establising Connection with SNS =============");
			this.snsProxy = new SNSProxy();
			System.out.println("============= Established Connection with SNS =============\n");

			this.alchemyProxy = new AlchemyProxy();
			
			StreamingServerListener.listener = this;
		}	
	}

	public static StreamingServerListener getListenerInstance() {
		return listener;
	}

	private static Properties loadPropertiesFile(String propFilePath) {
		Properties prop = new Properties();
		try {
		InputStream is = StreamingServerListener.class.getClassLoader().
				getResourceAsStream(propFilePath);
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	public void submitTasks(Runnable task, int count) {
		for(int i=0; i<count; i++) {
			this.exeService.execute(task);
		}
	}
	
	public AlchemyProxy getAlchemyProxy() {
		return this.alchemyProxy;
	}
	
	public SQSQueueProxy getSQSProxy() {
		return this.sqsProxy;
	}

	public SNSProxy getSNSProxy() {
		return this.snsProxy;
	}
	
	public ElasticSearchProxy getESProxy() {
		return this.esProxy;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Shutting Down!!");
		this.exeService.shutdownNow();	
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		String follow = StreamingServerListener.twitterConfFile.getProperty("follow");
		String[] followList = follow.split(",");
		
		this.submitTasks(new FetchTweetsTask("sample-client", followList), 1);
		
		this.submitTasks(new FetchSQSMessages(StreamingServerListener.awsCredentialsFile
				.getProperty("sqsQueueName")), 3);
	}
}
