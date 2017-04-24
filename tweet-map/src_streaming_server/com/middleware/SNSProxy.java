package com.middleware;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.daemonservices.StreamingServerListener;

public class SNSProxy {

	AmazonSNSClient snsClient;
	String topicArn;
	
	public SNSProxy() {
		this.snsClient = initializeSNS();
	}
	
	public String getTopicArn() {
		return this.topicArn;
	}
	
	private AmazonSNSClient initializeSNS() {
		AWSCredentials credentials = new BasicAWSCredentials(StreamingServerListener.awsCredentialsFile
				.getProperty("accessKey"), StreamingServerListener.awsCredentialsFile.getProperty("secretKey"));
		
		Region usEast = Region.getRegion(Regions.US_EAST_1);
		AmazonSNSClient snsClient = new AmazonSNSClient(credentials);
		snsClient.setRegion(usEast);
		
		// Create a topic using snsClient
		CreateTopicRequest createReq = new CreateTopicRequest().withName(StreamingServerListener.
				awsCredentialsFile.
				getProperty("snsTopicForTweets"));
		
		// action is idempotent so, duplicate topics wont be created
		CreateTopicResult createRes = snsClient.createTopic(createReq);
		this.topicArn = createRes.getTopicArn();

		return snsClient;
	}
	
	public void publishToTopic(String message) {
		PublishRequest publishRequest = new PublishRequest(this.topicArn, message).withSubject("TweetInfo");
		try {
			PublishResult publishResult = snsClient.publish(publishRequest);
			publishResult.getMessageId();
		} catch(Exception e) {
			System.out.println("Error while pushing messages to SQS. Message failed = " +
				message + " and error message is " + e.getMessage());
		}
			}
}
