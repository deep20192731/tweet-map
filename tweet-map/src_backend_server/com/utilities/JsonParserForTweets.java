package com.utilities;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonParserForTweets {
	
	public static class LocationAndSentiment {
		public double latitude;
		public double longitude;
		public String sentimentType;
		public double sentimentScore;
	}

	public static JsonObject parseToJsonObject(String req) {
		JsonParser parser = new JsonParser();
		JsonElement jsonValue = parser.parse(req);
		if(jsonValue != null && !jsonValue.isJsonNull()) {
			JsonObject obj = jsonValue.getAsJsonObject();
			if(obj != null && !obj.isJsonNull()) {
				return obj;
			}
		}
		return null;
	}
	
	public static List<LocationAndSentiment> parseJsonToTweet(JsonObject results) {
		List<LocationAndSentiment> allTweets = new ArrayList<LocationAndSentiment>();
		if(results != null && !results.isJsonNull() && results.isJsonObject()) {
			JsonObject obj = results.getAsJsonObject("hits");
			JsonArray arr = obj.getAsJsonArray("hits");
			for(int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.get(i).getAsJsonObject();
				JsonObject oo = o.getAsJsonObject("_source");
				LocationAndSentiment temp = new LocationAndSentiment();
				System.out.println(oo.getAsJsonPrimitive("latitude"));
				System.out.println(oo.getAsJsonPrimitive("longitude"));
				System.out.println(oo.getAsJsonPrimitive("sentimentScore"));
				System.out.println(oo.getAsJsonPrimitive("sentimentType"));
				temp.latitude = oo.getAsJsonPrimitive("latitude").getAsDouble();
				temp.longitude = oo.getAsJsonPrimitive("longitude").getAsDouble();
				if(oo.getAsJsonPrimitive("sentimentScore") != null) {
					temp.sentimentScore = oo.getAsJsonPrimitive("sentimentScore").getAsDouble();
				} else {
					temp.sentimentScore = 0;
				}
				if(oo.getAsJsonPrimitive("sentimentType") != null) {
					temp.sentimentType = oo.getAsJsonPrimitive("sentimentType").getAsString();
				} else {
					temp.sentimentType = "NA";
				}
				allTweets.add(temp);
			}
		}
		return allTweets;
	}
}
