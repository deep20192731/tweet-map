package com.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.daemonservices.AppServerListener;
import com.google.gson.JsonObject;
import com.models.Tweet;
import com.utilities.JsonParserForTweets;

public class SNSMsgReceiver extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			 throws ServletException, IOException {
		String messagetype = request.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;

		Scanner scan = new Scanner(request.getInputStream());
	    StringBuilder builder = new StringBuilder();

		while (scan.hasNextLine()) {
		      builder.append(scan.nextLine());
		}
		
		JsonObject msg = JsonParserForTweets.parseToJsonObject(builder.toString());
		        
		if (msg.get("SignatureVersion").getAsString().equals("1")) {
			// Check the signature and throw an exception if the signature verification fails.
			System.out.println("Signature Version Correct");
		} else {
			System.out.println(">>Unexpected signature version. Unable to verify signature.");
		    throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		  }

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			String message = msg.get("Message").getAsString();
			System.out.println("Message Recieved from SNS = " + message);
			sendNotificationToClients(processAndIndexTweet(message));
			
		} else if (messagetype.equals("SubscriptionConfirmation")) {
			// Confirm Subscription by going to the url
		    Scanner sc = new Scanner(new URL(msg.get("SubscribeURL").getAsString()).openStream());
		    StringBuilder sb = new StringBuilder();
		    while (sc.hasNextLine()) {
		        sb.append(sc.nextLine());
		    }
		    
		    System.out.println(">>Subscription confirmation (" + msg.get("SubscribeURL").getAsString() +") "
		    		+ "Return value: " + sb.toString());
		} else if (messagetype.equals("UnsubscribeConfirmation")) {
			 System.out.println(">>Unsubscribe confirmation: " + msg.get("Message").getAsString());
		} else {
		    //TODO: Handle unknown message type.
			System.out.println(">>Unknown message type.");
		 }
	}

	private String processAndIndexTweet(String message) {
		Tweet t = Tweet.fromJsonString(message);
		if(t != null) {
			AppServerListener.getListener().getEsProxy().indexTweet(t);
			return t.getTweetId();
		}
		return null;
	}
	
	private void sendNotificationToClients(String tweetId) {
		if(tweetId != null) {
			AppServerListener.getListener().getNotiSender().sendToAll(tweetId);
		}
	}
}
