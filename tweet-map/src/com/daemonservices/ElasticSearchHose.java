package com.daemonservices;

import java.io.IOException;
import java.util.Properties;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import com.models.Tweet;
import com.tasks.FetchTweetsTask;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;


public class ElasticSearchHose {
	private static final Properties awsCredentialsFile = TweetMapExecutor.
			getPropertiesFile("AwsCredentials.properties");
	
	public void indexTweet(Tweet t) {
		JestClient esClient = TweetMapExecutor.getESClient();
		// TODO: Try and put the tweet str as document id to avoid duplicate tweets
		// TODO: Try and put the documents using bulk api's
		Index index = new Index.Builder(t).index(awsCredentialsFile.getProperty("index-name")).
				type(awsCredentialsFile.getProperty("mapping-name")).build();
		System.out.println(t.getTweet());
		//Index index = new Index.Builder(t).index(awsCredentialsFile.getProperty("index-name")).build();
		try {
			esClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*public static void main(String[] args) {
		 JestClientFactory factory = new JestClientFactory();
		 factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder(awsCredentialsFile.getProperty("es-endPoint"))
		                        .multiThreaded(true)
		                        .build());
		 JestClient esClient = factory.getObject();
		 try {
				Builder settingsBuilder = Settings.settingsBuilder();
				//settingsBuilder.put("number_of_shards", awsCredentialsFile.getProperty("shards"));
				//settingsBuilder.put("number_of_replicas", awsCredentialsFile.getProperty("replicas"));
				
				esClient.execute(new CreateIndex.Builder(awsCredentialsFile.getProperty("index-name")).
						settings(settingsBuilder.build().getAsMap()).build());
				
				// Create the mapping/schema for documents
				PutMapping putMapping = new PutMapping.Builder(
						awsCredentialsFile.getProperty("index-name"),
						awsCredentialsFile.getProperty("mapping-name"),
				        "{ \""+ awsCredentialsFile.getProperty("mapping-name") +"\" : { \"properties\" : { \"text\" : {\"type\" : \"string\","
				        + " \"store\" : \"true\", \"null_value\" : \"na\", \"index\" : \"analyzed\"},"
				        + " \"latitude\" : {\"type\" : \"double\","
				        + " \"store\" : \"true\"},"
				        + "\"longitude\" : {\"type\" : \"double\","
				        + " \"store\" : \"true\"} } } }"
				).build();
				
				esClient.execute(putMapping);
				
				String source = "{\"text\":\"Deepesh is awsome\","
						+ "\"latitude\":\"47.534\","
						+ "\"longitude\":\"-12.456\"}";
				Tweet source = new Tweet("Ram bharose", 123.56, -9.78);
				Index index = new Index.Builder(source).index("sample-index-1").type("sample-mapping").id("1").build();
				esClient.execute(index);
				//System.out.println("Completed");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
	}*/
}
