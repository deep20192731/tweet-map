package com.models;

import io.searchbox.annotations.JestId;

import java.io.IOException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilities.JsonParserForTweets.LocationAndSentiment;

public class Tweet {
	@JestId
	private String tweetId;
	
	private String tweet;
	private double latitude;
	private double longitude;
	private double sentimentScore;
	private String sentimentType;
	
	public Tweet() {}
	public Tweet(String tweetId, String tweetText, double lat, double longi) {
		this.tweetId = tweetId;
		this.tweet = tweetText;
		this.latitude = lat;
		this.longitude = longi;
	}
	
	public String getTweetId() {
		return this.tweetId;
	}
	
	public void setTweetId(String textId) {
		this.tweetId = textId;
	}
	
	public String getTweet() {
		return this.tweet;
	}
	
	public void setTweet(String text) {
		this.tweet = text;
	}
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public void setLatitude(double lat) {
		this.latitude = lat;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public void setLongitude(double longi) {
		this.longitude = longi;
	}
	
	public String getSentimentType() {
		return this.sentimentType;
	}
	
	public void setSentimentType(String type) {
		this.sentimentType = type;
	}
	
	public double getSentimentScore() {
		return this.sentimentScore;
	}
	
	public void setSentimentScore(double score) {
		this.sentimentScore = score;
	}
	
	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String stringRep = "";
		try {
			stringRep = mapper.writeValueAsString(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(!stringRep.isEmpty()) {
			return stringRep;
		} else {
			return this.toString();
		}
	}

	public static Tweet fromJsonString(String s) {
		ObjectMapper mapper = new ObjectMapper();
		Tweet t = new Tweet();
		try {
			t = mapper.readValue(s, Tweet.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	// See if the geo location is available and the tweet is in English
	public static boolean isValidTweetMessage(String message) {
		JsonValue value = Json.parse(message);
		if(value != null && !value.isNull() && value.isObject()) {
			JsonObject obj = value.asObject();
			if(obj != null && !obj.isNull() && !obj.isEmpty()) {
				String lang = obj.getString("lang", "");
				
				if(lang.equals("en")) {
					return true;
				}
			}
		}
		return false;
	}
}
