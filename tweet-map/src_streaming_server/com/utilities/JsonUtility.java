package com.utilities;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonUtility {
	
	public static class Location {
		public double latitude;
		public double longitude;
	}

	public static String convertArrayToDelimiterSepString(char delimiter, List<String> tracks) {
		StringBuilder sb = new StringBuilder();
		for(String s : tracks) {
			sb.append(s);
			sb.append(delimiter);
		}
		return sb.substring(0, sb.length()-1).toString();
	}
	
	public static List<Location> parseJsonToTweet(JsonObject results) {
		List<Location> allTweets = new ArrayList<Location>();
		if(results != null && !results.isJsonNull() && results.isJsonObject()) {
			JsonObject obj = results.getAsJsonObject("hits");
			JsonArray arr = obj.getAsJsonArray("hits");
			for(int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.get(i).getAsJsonObject();
				JsonObject oo = o.getAsJsonObject("_source");
				Location temp = new Location();
				temp.latitude = oo.getAsJsonPrimitive("latitude").getAsDouble();
				temp.longitude = oo.getAsJsonPrimitive("longitude").getAsDouble();
				allTweets.add(temp);
			}
		}
		return allTweets;
	}
}