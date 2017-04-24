package com.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.daemonservices.StreamingServerListener;
import com.middleware.ElasticSearchProxy;
import com.utilities.JsonUtility.Location;

@Path("tweets")
public class TweetsResource {
	
	@GET
	@Path("{query_term}/{from}/{size}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Location> getTweets(
			@PathParam("query_term") String queryTerm,
			@PathParam("size") int size,
			@PathParam("from") int from) {
		ElasticSearchProxy esProxy = StreamingServerListener.getListenerInstance().getESProxy();
		return esProxy.queryIndex(queryTerm, from, size);
	}
}