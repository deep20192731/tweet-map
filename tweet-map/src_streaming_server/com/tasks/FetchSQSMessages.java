package com.tasks;

import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.daemonservices.StreamingServerListener;
import com.middleware.AlchemyProxy;
import com.middleware.AlchemyProxy.Sentiment;
import com.middleware.ElasticSearchProxy;
import com.middleware.SNSProxy;
import com.middleware.SQSQueueProxy;
import com.models.Tweet;

public class FetchSQSMessages implements Runnable {

	private String queueName;
	
	public FetchSQSMessages(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public void run() {
		SQSQueueProxy sqsProxy = StreamingServerListener.getListenerInstance().getSQSProxy();
		AlchemyProxy alchemyProxy = StreamingServerListener.getListenerInstance().getAlchemyProxy();
		SNSProxy snsProxy = StreamingServerListener.getListenerInstance().getSNSProxy();

		List<Message> msgs = sqsProxy.receiveMsgs(this.queueName);
		while(true) {
			msgs = sqsProxy.receiveMsgs(this.queueName);
			for(Message m : msgs) {
				String handle = m.getReceiptHandle();
				String msg = m.getBody();

				Tweet t = new Tweet().processJson(msg);
				if(t != null) {
					Sentiment sentiment = alchemyProxy.getTextSentiment(t.getTweet());
					t.setSentimentType(sentiment.sentimentType);
					t.setSentimentScore(sentiment.sentimentScore);
					
					// Publish Message to SNS
					snsProxy.publishToTopic(t.toString());
				}
				
				// Delete the msg from queue
				sqsProxy.deleteMsg(this.queueName, handle);
			}
			msgs.clear();
		}
	}
}
