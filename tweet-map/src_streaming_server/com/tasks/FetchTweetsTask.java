package com.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.daemonservices.StreamingServerListener;
import com.middleware.SQSQueueProxy;
import com.models.Tweet;
import com.utilities.JsonUtility;

/**
 * @author Deepesh
 * Twitter Hose for Streaming Tweets
 */
public class FetchTweetsTask implements Runnable {
	private List<String> tracks;
	private SQSQueueProxy sqsProxy;
	
	/**
	 * @param clientName
	 * @param 2 keywords to search
	 */
	public FetchTweetsTask(String clientName, String ... keywordsToTrack) {
		this.sqsProxy = StreamingServerListener.getListenerInstance().getSQSProxy();
		this.tracks = new ArrayList<String>();
		if(keywordsToTrack != null) {
			for(String s : keywordsToTrack) {
				this.tracks.add(s);
			}
		}
	}
	
	// This have to be done here, since Twitter streaming api's permit one active
	// connection from same IP. So using multiple threads is not an option
	private Response connectWithTwitter() {
		OAuthService service = new ServiceBuilder()
                .provider(TwitterApi.class)
                .apiKey(StreamingServerListener.twitterConfFile.getProperty("consumer-key"))
                .apiSecret(StreamingServerListener.twitterConfFile.getProperty("consumer-secret"))
                .build();

        Token accessToken = new Token(StreamingServerListener.twitterConfFile
        		.getProperty("access-token"), 
        		StreamingServerListener.twitterConfFile.getProperty("access-secret"));

        System.out.println("|=======Connection to Twitter Started==========|");
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://stream.twitter.com/1.1/statuses/filter.json");
        request.addHeader("version", "HTTP/1.1");
        request.addHeader("host", "stream.twitter.com");
        request.setConnectionKeepAlive(true);
        request.addHeader("user-agent", "Twitter Reader");
        request.addBodyParameter("track", JsonUtility.convertArrayToDelimiterSepString(',', this.tracks));
        service.signRequest(accessToken, request);
        Response response = request.send();
        
        if(response != null) {
        	System.out.println("|=======Connection to Twitter Done==========|");
        }
        return response;
	}
	
	@Override
	public void run() {
        Response response = this.connectWithTwitter();

        BufferedReader br = new BufferedReader(new InputStreamReader(response.getStream()));
        String tweetMessage;

        try {
			while ((tweetMessage = br.readLine()) != null) {
				if(Tweet.isValidTweetMessage(tweetMessage)) {
					this.sqsProxy.sendMessageInBatchToSQS(tweetMessage, StreamingServerListener.
							awsCredentialsFile.getProperty("sqsQueueName"));
				}
			}
		} catch (Exception e) {
			System.out.println("Exception while getting tweets from stream." + e.getMessage());
			e.printStackTrace();
		}
	}
}