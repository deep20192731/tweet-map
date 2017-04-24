package com.middleware;

import org.json.JSONObject;

import com.daemonservices.StreamingServerListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class AlchemyProxy {
	public static class Sentiment {
		public String text;
		public String sentimentType = "NA";
		public double sentimentScore = 0;
	}

	private static final String baseUrl = "http://gateway-a.watsonplatform.net/calls/text/";
	
	public Sentiment getTextSentiment(String phrase) {
		HttpResponse<JsonNode> entities = null;
		try {
			entities = Unirest.get(AlchemyProxy.baseUrl +
					"TextGetTextSentiment").queryString("apikey", StreamingServerListener.
							alchemyConfFile.getProperty("accessKey"))
					.queryString("text", phrase)
					.queryString("outputMode", "json").asJson();
		} catch (UnirestException e) {
			e.printStackTrace();
		}

		// Set sentimentType = "" and sentimentScore = 0 if sentiment not found at all
		// Set sentimentScore = 0 if sentmentType = neutral
		Sentiment s = new Sentiment();
		try {
			s.text = phrase;
			JSONObject baseObj = entities.getBody().getObject();
			s.sentimentType = baseObj.getJSONObject("docSentiment").getString("type");
			s.sentimentScore = baseObj.getJSONObject("docSentiment").getDouble("score");
		} catch(Exception e) {
			//IGNORE..since default values are already been set
		}
		return s;
	}
}
