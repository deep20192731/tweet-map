package com.middleware;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import com.daemonservices.AppServerListener;
import com.google.gson.JsonObject;
import com.models.Tweet;
import com.utilities.JsonParserForTweets;
import com.utilities.JsonParserForTweets.LocationAndSentiment;

public class ElasticSearchProxy {

	private static final Properties awsCredentialsFile = AppServerListener.awsCredentialsFile;
	private JestClient esClient;
	
	public ElasticSearchProxy() {
		this.esClient = initializeESClient();
		createESIndex();
	}

	public JestClient getESClient() {
		return this.esClient;
	}

	private JestClient initializeESClient() {
		// Important to make the client as multi-threaded since mulitple threads attempt to write
		// index to elastic-search
		JestClientFactory factory = new JestClientFactory();
		 factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder(awsCredentialsFile.getProperty("es-endPoint"))
		                        .multiThreaded(true)
		                        .build());
		 return factory.getObject();
	}
	
	private void createESIndex() {
		try {
			Builder settingsBuilder = Settings.settingsBuilder();
			settingsBuilder.put("number_of_shards", awsCredentialsFile.getProperty("shards"));
			settingsBuilder.put("number_of_replicas", awsCredentialsFile.getProperty("replicas"));
			this.esClient.execute(new CreateIndex.Builder(awsCredentialsFile.getProperty("index-name")).
					settings(settingsBuilder.build().getAsMap()).build());
		
			// Create the mapping/schema for documents
			PutMapping putMapping = new PutMapping.Builder(
					awsCredentialsFile.getProperty("index-name"),
					awsCredentialsFile.getProperty("mapping-name"),
			        "{ \""+ awsCredentialsFile.getProperty("mapping-name") +"\" : { \"properties\" : "
			        		+ "{ \"tweet\" : {\"type\" : \"string\","
			        + " \"store\" : \"true\", \"null_value\" : \"na\", \"index\" : \"analyzed\"},"
			        + " \"sentiment-score\" : {\"type\" : \"double\","
			        + " \"store\" : \"true\"},"
			        + " \"sentiment-type\" : {\"type\" : \"string\","
			        + " \"store\" : \"true\"},"
			        + " \"latitude\" : {\"type\" : \"double\","
			        + " \"store\" : \"true\"},"
			        + "\"longitude\" : {\"type\" : \"double\","
			        + " \"store\" : \"true\"} } } }"
			).build();
			
			this.esClient.execute(putMapping);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<LocationAndSentiment> queryIndex(String queryTerm, String from, String size) {
		String query = "{\n" +
	            "    \"query\": {\n" +
	            "                \"query_string\" : {\n" +
	            "                    \"query\" : \" "+ queryTerm +"\",\n" +
	            					  "\"default_field\" : \"tweet\"" + 
	            "                }\n" +
	            "    },\n" +
	            "	 \"from\" : "+ from + ",\n" +
	            "     \"size\" : "+ size + "\n" +
	            "}";

		Search search = new Search.Builder(query)
                // multiple index or types can be added.
                .addIndex(awsCredentialsFile.getProperty("index-name"))
                .addType(awsCredentialsFile.getProperty("mapping-name"))
                .build();

		SearchResult sr = null;
		try {
			sr = this.esClient.execute(search);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(sr != null) {
			JsonObject results = sr.getJsonObject();
			return JsonParserForTweets.parseJsonToTweet(results);
		}
		return null;
	}
	
	public void indexTweet(Tweet t) {
		// TODO: Try and put the tweet str as document id to avoid duplicate tweets
		// TODO: Try and put the documents using bulk api's
		Index index = new Index.Builder(t).index(awsCredentialsFile.getProperty("index-name")).
				type(awsCredentialsFile.getProperty("mapping-name")).build();
		try {
			esClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}