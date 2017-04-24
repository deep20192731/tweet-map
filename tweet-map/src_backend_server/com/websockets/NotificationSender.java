package com.websockets;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class NotificationSender extends WebSocketServer {

	public NotificationSender(int port) {
		super(new InetSocketAddress(port));
		System.out.println("Satrted on address" + this.getAddress());
		System.out.println("Started on port = " + port);
	}
	
	public NotificationSender(InetSocketAddress address) {
		super(address);
		System.out.println("Satrted on address" + this.getAddress());
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		//this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " joined the "
				+ "connection!" );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		//this.sendToAll( conn + " has left the room!" );
		System.out.println( conn + " has left the connection!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		System.out.println("Message Recieved from Browser = " + message);
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		System.out.println("Error in establising connection " + ex.getMessage());
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
	
	public void sendToAll(String text) {
		System.out.println("Text Recieved = " + text);
		Collection<WebSocket> con = connections();
		synchronized (con) {
			for( WebSocket c : con ) {
				c.send(text);
			}
		}
	}
}
