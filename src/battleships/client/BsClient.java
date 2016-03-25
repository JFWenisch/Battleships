package battleships.client;

import java.net.*;
import java.io.*;

/**
 * console-based Standard-Implementation for a Bsjn-Client
 * 
 */
public class BsClient implements Runnable{
	private Socket socket              = null;
	private Thread thread              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;
	private BsClientThread client    = null;

	public BsClient(String serverName, int serverPort){
		System.out.println("Establishing connection...");
		try{
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			start();
		}
		catch(UnknownHostException uhe){
			System.out.println("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe){
			System.out.println("Unexpected IO-Exception: " + ioe.getMessage());
		}
	}
	
	/**
	 * handles the hand-off of the Message to the outgoing DataStream that connects to the Server
	 * 
	 * @param msg
	 */
	public void run(){
		while (thread != null){
			try{
				streamOut.writeUTF(console.readLine());
				streamOut.flush();
			}
			catch(IOException ioe){
				System.out.println("Sending error: " + ioe.getMessage());
				stop();
			}
		}
	}

	/**
	 * handles the hand-off of the Message from the ingoing DataStream to the Standard-Output
	 * 
	 * @param msg
	 */
	public void handle(String msg){
		if (msg.equals("/exit")){
			System.out.println("Good bye. Press RETURN to exit ...");
			stop();
		}
		else{
			System.out.println(msg);
		}
	}

	/**
	 * initiates the ClientThread and binds the DataStreams
	 * @throws IOException
	 */
	public void start() throws IOException{
		console   = new DataInputStream(System.in);
		streamOut = new DataOutputStream(socket.getOutputStream());
		if (thread == null){
			client = new BsClientThread(this, socket);
			thread = new Thread(this);                   
			thread.start();
		}
	}
	public void stop(){
		if (thread != null){
			thread.stop();  
			thread = null;
		}
		try{
			if (console   != null)  console.close();
			if (streamOut != null)  streamOut.close();
			if (socket    != null)  socket.close();
		}
		catch(IOException ioe){
			System.out.println("Error closing ...");
		}
		client.close();  
		client.offSwitch();
	}
	
	/**
	 * parses for Exec-Format: java BsClient host port
	 * 
	 * @param args
	 */
	public static void main(String args[]){
		BsClient client = null;
		if (args.length != 2){
			System.out.println("Usage: java BsClient host port");
		}
		else{
			client = new BsClient(args[0], Integer.parseInt(args[1]));
		}
	}
}




