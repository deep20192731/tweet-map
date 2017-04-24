package com.daemonservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.middleware.ElasticSearchProxy;
import com.middleware.SNSProxy;
import com.websockets.NotificationSender;

public class AppServerListener implements ServletContextListener{
	
	private static AppServerListener listener;

	public static final Properties awsCredentialsFile = AppServerListener.
			loadPropertiesFile("aws.properties");
	public static final Properties serverConf = AppServerListener.
			loadPropertiesFile("server.properties");
	
	private SNSProxy snsProxy;
	private ElasticSearchProxy esProxy;
	private NotificationSender notiSender;
	
	public AppServerListener() throws UnknownHostException {
		// Constructor have to be public, else this not work as Listener.
		// So making sure, just one instance of the listener is moved around
		if(AppServerListener.listener == null) {
			System.out.println("============= Establising Connection with Elastic-Search =============");
			this.esProxy = new ElasticSearchProxy();
			System.out.println("============= Established Connection with Elastic-Search =============\n");
			
			System.out.println("============= Establising Connection with SNS =============");
			this.snsProxy = new SNSProxy();
			System.out.println("============= Established Connection with SNS =============");
			
			System.out.println("============= Starting Web-Sockets Listener =============");
			this.notiSender = new NotificationSender(Integer.parseInt(serverConf.
							getProperty("web-socket-port")));
			System.out.println("============= Started Web-Sockets Listener =============");
			
			AppServerListener.listener = this;
		}	
	}
	
	public static AppServerListener getListener() {
		return listener;
	}

	public ElasticSearchProxy getEsProxy() {
		return this.esProxy;
	}
	
	public SNSProxy getSnsProxy() {
		return this.snsProxy;
	}
	
	public NotificationSender getNotiSender() {
		return this.notiSender;
	}
	
	private static Properties loadPropertiesFile(String propFilePath) {
		Properties prop = new Properties();
		try {
		InputStream is = AppServerListener.class.getClassLoader().
				getResourceAsStream(propFilePath);
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			this.notiSender.stop();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		this.notiSender.start();
	}
}
