package com.middleware;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.daemonservices.StreamingServerListener;

public class SQSQueueProxy {

	private static final int BATCH_MEGS_SIZE = 10;

	private AmazonSQS sqsQueueEndpoint;
	private List<SendMessageBatchRequestEntry> batchRequests;
	
	public SQSQueueProxy() {
		this.batchRequests = new ArrayList<SendMessageBatchRequestEntry>();

		this.sqsQueueEndpoint = initializeSQSClientAndQueue();
	}
	
	private AmazonSQS initializeSQSClientAndQueue() {
		// Create new SQS Client
		AWSCredentials credentials = new BasicAWSCredentials(StreamingServerListener.awsCredentialsFile
				.getProperty("accessKey"), StreamingServerListener.awsCredentialsFile.getProperty("secretKey"));
		
		AmazonSQS sqsClient = new AmazonSQSClient(credentials);
		Region usEast = Region.getRegion(Regions.US_EAST_1);
		sqsClient.setRegion(usEast);
        
		// Create queue using the Client
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(StreamingServerListener.
				awsCredentialsFile.getProperty("sqsQueueName"));
		
        String queueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();
        return sqsClient;
	}

	public String getQueueUrl(String name) {
		return this.sqsQueueEndpoint.getQueueUrl(name).getQueueUrl();
	}

	public void sendMessageInBatchToSQS(String message, String queueName) {
		if(batchRequests.size() < BATCH_MEGS_SIZE) {
			batchRequests.add(this.createSQSMsgSendRequest(message));
		} else {
			this.sendMessageToSQS(batchRequests, queueName);
			batchRequests.clear();
		}
	}
	
	public void sendMessageSeparatelyToSQS(String message, String queueName) {
		List<SendMessageBatchRequestEntry> requests = new ArrayList<SendMessageBatchRequestEntry>();
		requests.add(this.createSQSMsgSendRequest(message));
		this.sendMessageToSQS(requests, queueName);
	}
	
	private void sendMessageToSQS(List<SendMessageBatchRequestEntry> messages, String queueName) {
		String url = this.getQueueUrl(queueName);
		SendMessageBatchRequest req = new SendMessageBatchRequest(url, messages);
		
		try {
			this.sqsQueueEndpoint.sendMessageBatch(req);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public SendMessageBatchRequestEntry createSQSMsgSendRequest(String message) {
		SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry();
		entry.withId(Integer.toString(batchRequests.size()))
		.withMessageBody(message);
		return entry;
	}
	
	public List<Message> receiveMsgs(String queueName) {
		List<Message> msgs = this.sqsQueueEndpoint.receiveMessage(this.
				getQueueUrl(queueName)).getMessages();
		return msgs;
	}

	public void deleteMsg(String queueName, String receiptHandle) {
		this.sqsQueueEndpoint.deleteMessage(this.getQueueUrl(queueName), receiptHandle);
	}
}