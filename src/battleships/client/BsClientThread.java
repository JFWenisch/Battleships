package battleships.client;

import java.net.*;
import java.io.*;

public class BsClientThread extends Thread{
	private Socket           socket   = null;
	private BsClient       client   = null;
	private DataInputStream  streamIn = null;
	private boolean running = true;

	public BsClientThread(BsClient _client, Socket _socket){
		client   = _client;
		socket   = _socket;
		open();  
		start();
	}
	
	/**
	 * handles the opening of the InputStream
	 */
	public void open(){
		try{
			streamIn  = new DataInputStream(socket.getInputStream());
		}
		catch(IOException ioe){
			System.out.println("Error getting input stream: " + ioe);
			client.stop();
		}
	}
	
	/**
	 * handles the closing of the InputStream
	 */
	public void close(){
		try{  
			if (streamIn != null){
				streamIn.close();
			}
		}
		catch(IOException ioe){
			System.out.println("Error closing input stream: " + ioe);
		}
	}
	
	public void offSwitch(){
		running = false;
	}
	
	/**
	 * handles the hand-off of the Message from the Server to the ingoing DataStream that connects to the Client
	 * 
	 */
	public void run(){
		while (running){
			try{
				client.handle(streamIn.readUTF());
			}
			catch(IOException ioe){
				System.out.println("Listening error: " + ioe.getMessage());
				client.stop();
			}
		}
	}
}