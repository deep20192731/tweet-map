package com.middleware;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.daemonservices.AppServerListener;

public class SNSProxy {

	private AmazonSNSClient snsClient;
	
	public SNSProxy() throws UnknownHostException {
		this.snsClient = initializeSNS();
	}

	public String subscribeToTweetTopic() throws UnknownHostException {
		SubscribeRequest subscribeReq = new SubscribeRequest()
		   .withTopicArn(getTopicArn())
		   .withProtocol("http")
		   .withEndpoint("http://" + InetAddress.getLocalHost().getHostAddress() + "/sns");
		SubscribeResult subsResult = this.snsClient.subscribe(subscribeReq);
		return subsResult.getSubscriptionArn();
	}

	private String getTopicArn() {
		// If a topic is already created, it does not create a new one, instead return the same one.
		CreateTopicRequest createReq = new CreateTopicRequest().withName(AppServerListener.awsCredentialsFile
				.getProperty("snsTopicForTweets"));
		CreateTopicResult createRes = snsClient.createTopic(createReq);
		return createRes.getTopicArn();
	}

	private AmazonSNSClient initializeSNS() {
		AWSCredentials credentials = new BasicAWSCredentials(AppServerListener.awsCredentialsFile
				.getProperty("accessKey"), AppServerListener.awsCredentialsFile.getProperty("secretKey"));
		
		Region usEast = Region.getRegion(Regions.US_EAST_1);
		AmazonSNSClient snsClient = new AmazonSNSClient(credentials);
		snsClient.setRegion(usEast);
		
		return snsClient;
	}
}
