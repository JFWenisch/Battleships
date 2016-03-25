package battleships.server;

import java.net.*;
import java.io.*;

public class BsServerThread extends Thread{  
	private BsServer       server    = null;
	private Socket           socket    = null;
	private int              ID        = -1;
	private DataInputStream  streamIn  =  null;
	private DataOutputStream streamOut = null;
	private boolean running = true;

	public BsServerThread(BsServer _server, Socket _socket, int _id){
		super();
		server = _server;
		socket = _socket;
		ID     = _id;
	}
	
	/**
	 * handles the hand-off of the Message to the outgoing DataStream that connects to the Client
	 * 
	 * @param msg
	 */
	public void send(String msg){   
		try{
			streamOut.writeUTF(msg);
			streamOut.flush();
		}
		catch(IOException ioe){  
			System.out.println(ID + " ERROR sending: " + ioe.getMessage());
			server.remove(ID);
			running = false;
		}
	}
	public int getID(){
		return ID;
	}
	
	/**
	 * handles the hand-off of the Message to the outgoing DataStream that connects to the Server
	 * 
	 */
	public void run(){
		
		System.out.println("Server Thread " + ID + " running.");
		while (running){
			try{
				server.handle(ID, streamIn.readUTF());
			}
			catch(IOException ioe)         {  
				System.out.println(ID + " ERROR reading: " + ioe.getMessage());
				server.remove(ID);
				running = false;
			}
		}
	}
	
	/**
	 * Packs the socket-streams into the respective DataStreams
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException	{
		streamIn = new DataInputStream(new 
				BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(new
				BufferedOutputStream(socket.getOutputStream()));
	}
	
	/**
	 * Closes the DataStreams and tries to close the Socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException	{
		if (socket != null)    socket.close();
		if (streamIn != null)  streamIn.close();
		if (streamOut != null) streamOut.close();
	}
}