package com.models;

import java.io.IOException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utilities.JsonUtility.Location;

import io.searchbox.annotations.JestId;

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
	
	public Tweet processJson(String message) {
		Tweet t = new Tweet();
		// Check for text
		JsonValue value = Json.parse(message);
		if(value != null && !value.isNull() && value.isObject()) {
			JsonObject obj = value.asObject();
			if(obj != null && !obj.isNull() && !obj.isEmpty()) {				
				String tweetText = obj.getString("text", "null");
				String tweetId = obj.getString("id_str", "");
				t.setTweetId(tweetId);
				t.setTweet(tweetText);

				// Check for actual position
				JsonValue loc = obj.get("coordinates");
				if(loc != null && !loc.isNull() && loc.isObject()) {
					JsonObject locObj = loc.asObject();
					if(locObj != null && !locObj.isNull() && !locObj.isEmpty()) {
						JsonValue coordinates = locObj.get("coordinates");
						if(coordinates != null && !coordinates.isNull() && coordinates.isArray()) {
							JsonArray arr = coordinates.asArray();
							if(arr != null && !arr.isNull() && !arr.isEmpty()) {
								double longi = arr.get(0).asDouble();
								double lat = arr.get(1).asDouble();
								t.setLatitude(lat);
								t.setLongitude(longi);
								return t;
							}
						}
					}
				} else {
					Location appLoc = lookForAppLocation(obj);
					if(appLoc != null) {
						t.setLatitude(appLoc.latitude);
						t.setLongitude(appLoc.longitude);
						return t;
					}	
				}
			}
		}
		return null;
	}

	
	private Location lookForAppLocation(JsonObject obj) {
		Location appLoc = new Location();
		JsonValue loc = obj.get("place");
		if(loc != null && !loc.isNull() && loc.isObject()) {
			JsonObject locObj = loc.asObject();
			if(locObj != null && !locObj.isNull() && !locObj.isEmpty()) {
				JsonValue coordinates = locObj.get("bounding_box");
				if(coordinates != null && !coordinates.isNull() && coordinates.isObject()) {
					JsonObject boundedBox = coordinates.asObject();
					if(boundedBox != null && !boundedBox.isNull() && boundedBox.isObject()) {
						JsonValue val = boundedBox.get("coordinates");
						if(val != null && !val.isNull() && val.isArray()) {
							JsonArray o = val.asArray();
							JsonValue v = o.get(0);
							if(v != null && !v.isNull() && v.isArray()) {
								JsonArray temp = v.asArray();
								double[] longi = new double[4];
								double[] lati = new double[4];
								for(int i = 0; i < 4; i++) {
									JsonValue tup = temp.get(i);
									JsonArray tupArr = tup.asArray();
									longi[i] = tupArr.get(0).asDouble();
									lati[i] = tupArr.get(1).asDouble();
								}
								appLoc.latitude = (lati[0] + lati[2])/2;
								appLoc.longitude = (longi[0] + longi[2])/2;
								return appLoc;
							}
						}
					}
				}
			}
		}
		return null;
	}
}