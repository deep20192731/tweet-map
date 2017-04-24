package com.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.daemonservices.AppServerListener;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.middleware.ElasticSearchProxy;
import com.utilities.JsonParserForTweets.LocationAndSentiment;

public class TweetResource extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws JsonGenerationException,
		JsonMappingException, IOException {
		String path = request.getPathInfo();
		String[] paths = path.split("/");

		String queryTerm = paths[1];
		String from = paths[2];
		String size = paths[3];
		
		ElasticSearchProxy esProxy = AppServerListener.getListener().getEsProxy();
		List<LocationAndSentiment> results = esProxy.queryIndex(queryTerm, from, size);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getOutputStream(), results);
	}
}
