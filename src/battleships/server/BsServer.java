package battleships.server;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.io.*;

import battleships.game.*;
import battleships.texts.Text;

public class BsServer implements Runnable{
	private BsServerThread clients[] = new BsServerThread[2];
	private BattleGround battleGrounds[] = new BattleGround[2];
	//a two-element list of a list of two-Dimensional Status-ArrayLists, so we need four-dimensional Status-ArrayLists:
	private ArrayList<ArrayList<ArrayList<ArrayList<Status>>>> history = new ArrayList<ArrayList<ArrayList<ArrayList<Status>>>>(2); 
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private int sizeProposition = 0;
	private int tempSize;
	private int phase = 0;	//phase 0: Players joining. Phase 1: proposing size, Phase 2: placing ships, Phase 3: Bombardment, Phase 4: Analysis
	private List<String> parsingResult = null;
	private int turn = -1;
	private static Random rnd = new Random();

	/**
	 * Constructor, that binds to the Port that was given as an argument, and calls for its start.
	 * 
	 * @param port
	 */
	public BsServer(int port){
		try{
			System.out.println("Binding to port " + port);
			server = new ServerSocket(port);  
			System.out.println("Serversocket opened. Server started: " + server.toString());
			start();
		}
		catch(IOException ioe){
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); 
		}
	}

	/**
	 * accepts and calls for adding the the Client-Connection  
	 * 
	 */
	public void run(){
		while (thread != null){
			try{
				System.out.println("Waiting for a client ..."); 
				addClient(server.accept()); }
			catch(IOException ioe){
				System.out.println("Server accept error: " + ioe);
				stop(); 
			}
		}
	}
	public void start()  {
		if (thread == null){
			thread = new Thread(this); 
			thread.start();
		}
	}
	public void stop()   {
		if (thread != null){
			thread.stop();
			thread = null;
		}
	}

	/**
	 * Handling of messages from Client
	 * 
	 * @param ID	ID of User, who sent the message
	 * @param input	sent message 
	 */
	public synchronized void handle(int ID, String input){
		//trimming the input from the Client
		input = input.trim();
		//exiting command
		if (input.equals("/exit")){
			clients[ID].send("/exit");
			remove(ID); 
		}
		//help command
		else if (input.equals("/help")){
			clients[ID].send("For chatting, you can just send anything that isnt a Command.");
			clients[ID].send("/help \twill open this help.");
			clients[ID].send("/exit \twill close the connection.");
			clients[ID].send("Following #game-Commands are sorted after game-phases:");
			clients[ID].send("#size [10-50] \twill propose width(+height) of square battlegrounds");
			clients[ID].send("#info \twill show the battlegrounds");
			clients[ID].send("#place \twill (without parameters) list placeable ships.");
			clients[ID].send("#place \tparameters are: Shiptypeletter(x,y|Direction), is either (S)outh or (E)ast:");
			clients[ID].send("#place A(0,0|E)\twill place an AircraftCarrier at 0,0 and East from there.");
			clients[ID].send("#place C(9,0|S)\twill place a Cruiser at the NE-side(size 10).");
			clients[ID].send("#place D(0,9|E)\twill place a Destroyer at the SW-bottom(size 10).");
			clients[ID].send("#place P(4,4|S)\twill place a PatrolBoat facing South.");
			clients[ID].send("You may even place multiple in parallel, e.g.:");
			clients[ID].send("#place P(6,4|S); P(8,5|E); p(8,8|S) \twill place three PatrolBoats.");
			clients[ID].send("#ready \twill test, if you placed all your ships.");
			clients[ID].send("#shoot(x,y) \twill shoot at those coordinates of the Opponent's BattleGround.");
			clients[ID].send("#history \twithout parameters will try to show both full User-histories.");
			clients[ID].send("#history(ID,x) \twill show the history of Player with ID at turn x (starting with 0)");
		}
		//actual game Commands
		else if(input.startsWith("#")){
			//Phase 0: initialization
			if(phase==0){
				clients[ID].send(Text.OpponentNeeded.toString());
			}
			//Phase 1: negotiating size
			else if(phase==1){
				if(input.startsWith("#size")){
					size(ID, input.substring(5));
				}else{
					clients[ID].send("Use the #size-Command.");
				}
			}
			//Phase 2: placing ships
			else if(phase==2){
				if(input.startsWith("#info")){
					clients[ID].send(battleGrounds[ID].getOurBattleGrounds());
					clients[ID].send(battleGrounds[(ID+1)%2].getTheirBattleGrounds()); // (ID+1)%2 means "my opponent's ID"
				}else if(input.equalsIgnoreCase("#place")){	//Posts how many ships of which type still fit on the map.
					clients[ID].send(Text.YouCanStillPlace.toString() + ":");
					clients[ID].send(Text.AircraftCarriers.toString() + ":" + (battleGrounds[ID].getNumberOfStillAllowedAircraftCarriers()));
					clients[ID].send(Text.Cruisers.toString() + ":" + (battleGrounds[ID].getNumberOfStillAllowedCruisers()));
					clients[ID].send(Text.Destroyers.toString() + ":" + (battleGrounds[ID].getNumberOfStillAllowedDestroyers()));
					clients[ID].send(Text.PatrolBoats.toString() + ":" + (battleGrounds[ID].getNumberOfStillAllowedPatrolBoats()));
				}else if(input.startsWith("#place")){//Tries to make sense of a #place-command with parameters.
					input = input.substring(6);
					placeShips(ID, input);
					clients[ID].send(battleGrounds[ID].getOurBattleGrounds());
				}else if(input.equalsIgnoreCase("#ready")){
					if(checkReady(ID)){
						clients[turn].send("Both Players are ready. The Bombardment can begin. You shoot first.");
						clients[(turn+1)%2].send("Both Players are ready. The Bombardment can begin. You opponent shoots first.");
					}
				}else{
					clients[ID].send("Use the #info-, #place- and #ready-Commands.");
				}
			}
			//Phase 3: Bombardment
			else if(phase==3){
				if(input.startsWith("#info")){
					clients[ID].send(battleGrounds[ID].getOurBattleGrounds());
					clients[ID].send(battleGrounds[(ID+1)%2].getTheirBattleGrounds());
				}else if(input.startsWith("#shoot")){
					// checking if it is the the player's turn
					if(turn==ID){
						shoot(ID, input.substring(6));
					}else{
						clients[ID].send("It is not your turn yet.");
					}
				}else{
					clients[ID].send("Use the #info- and #shoot-Commands.");
				}
			}
			//Phase 4: Analysis
			else if(phase==4){
				// #history-Command without parameters throws the whole history
				if(input.equalsIgnoreCase("#history")){
					for(int iUser = 0; iUser<2; iUser++){
						for(int i = 0;i<history.get(iUser).size(); i++){
							showHistory(ID, iUser, i);
						}
					}
					
				}else if(input.startsWith("#history")){
					clients[ID].send(showHistory(ID, input.substring(8)));
				}
				
			}

		}
		//normal Chat
		else{
			// (ID+1)%2 means "my opponent's ID"
			if(clients[((ID+1)%2)] != null){
				clients[((ID+1)%2)].send(ID + ": " + input);
			}else{
				clients[ID].send(Text.OpponentNeeded.toString());
			}
		}
	}
	
	/**
	 * creates the Client/PlayerSlot as BsServerThread
	 * initiates Phase 1
	 * 
	 * @param socket
	 */
	private void addClient(Socket socket)	{
		if (clientCount < clients.length)	{
			System.out.println("Client accepted: " + socket);
			clients[clientCount] = new BsServerThread(this, socket, clientCount);
			try	{
				clients[clientCount].open(); 
				clients[clientCount].start();
				clients[clientCount].send(Text.Intro1.toString()+ clientCount +Text.Intro2.toString());
				clientCount++;
				if(clientCount == 2){
					clients[0].send(Text.Start.toString());
					clients[1].send(Text.Start.toString());
					phase = 1;
				}
			}
			catch(IOException ioe)	{  
				System.out.println("Error opening thread: " + ioe); 
			} 
		}
		else{
			System.out.println("Client refused: maximum " + clients.length + " reached.");
		}
	}
	
	/**
	 *
	 *	negotiating BattleGround-Size
	 * 	initiates the battleGround-instances
	 * 	initiates Phase 2
	 * 
	 * @param iD of the User who sent the Msg
	 * @param placeMsg
	 */
	private void size(int iD, String placeMsg) {
		try{
			tempSize = Integer.parseInt(placeMsg.trim());
			if(tempSize>= 10 && tempSize<= 50){
				clients[iD].send("You proposed " + tempSize);
				if(sizeProposition==0){
					sizeProposition = tempSize;
					turn = (iD+1)%2;	//setting Turn, so the opponent
				}else{
					//if it is the Users turn to propose Size, the negotiation will finish.
					if(turn==iD){
						sizeProposition = (sizeProposition + tempSize)/2;
						clients[0].send("Negotiated size is " + sizeProposition + ". Next is: your ship-placement.");
						clients[1].send("Negotiated size is " + sizeProposition + ". Next is: your ship-placement.");
						battleGrounds[0] = new BattleGround(sizeProposition);
						battleGrounds[1] = new BattleGround(sizeProposition);

						turn = -1;
						phase = 2;
					}else{
						clients[iD].send("It is your opponents turn to propose a BattleGround-size.");
						clients[(iD+1)%2].send("Please, propose a BattleGroundSize from 10 to 50 e.g. with the command \"#size25\".");
					}
				}
			}else{
				throw new java.lang.NumberFormatException();
			}
		}catch(java.lang.NumberFormatException nfe){
			clients[iD].send("Allowed are values between 10 and 50. Usage is e.g.: #size 23");
		}
	}

	/**
	 * handles the Ship-placement-Commands. Cuts the whole command into A(23,5,S)-kind of commands and maps them to the Ship-Constructors
	 * Main Part of Phase 2
	 * 
	 * @param ID	User-ID, of the User who places them
	 * @param input
	 */
	private void placeShips(int ID, String input) {
		parsingResult = Arrays.asList(input.split("\\s*;\\s*"));	//Cutting into List(whitespace- or semicolon-separation)
		clients[ID].send("Your placing commands were:" + parsingResult.toString());
		String nextCommand = "";
		int commaPos = -1;
		int pipePos = -1;
		Direction direction = null;

		// each command is splitted into Ship-type (first letter), longitude, latitude and Direction
		for(int i = 0; i<parsingResult.size(); i++){
			nextCommand = parsingResult.get(i).trim();
			commaPos = nextCommand.lastIndexOf(",");
			pipePos = nextCommand.lastIndexOf("|");
			//number after the opening bracket and before the comma is the longitude
			int col = Integer.parseInt(nextCommand.substring(2, commaPos));
			//number after the comma and before the pipe is the latitude
			int row = Integer.parseInt(nextCommand.substring(commaPos+1,pipePos));
			//Letter for South or East is after the pipe and before the closing bracket
			if( (nextCommand.substring(pipePos+1,pipePos+2)).equalsIgnoreCase("S") ){
				direction = Direction.SOUTH;
			}else if( (nextCommand.substring(pipePos+1,pipePos+2)).equalsIgnoreCase("E") ){
				direction = Direction.EAST;
			}

			//now that the parameters are clear, we try to place the ship and send the Status back to the Player
			if(nextCommand.startsWith("A(") || nextCommand.startsWith("a(")){
				clients[ID].send(battleGrounds[ID].placeShip(new AircraftCarrier(col, row, direction)));
			}else if(nextCommand.startsWith("C(") || nextCommand.startsWith("c(")){
				clients[ID].send(battleGrounds[ID].placeShip(new Cruiser(col, row, direction)));
			}else if(nextCommand.startsWith("D(") || nextCommand.startsWith("d(")){
				clients[ID].send(battleGrounds[ID].placeShip(new Destroyer(col, row, direction)));
			}else if(nextCommand.startsWith("P(") || nextCommand.startsWith("p(")){
				clients[ID].send(battleGrounds[ID].placeShip(new PatrolBoat(col, row, direction)));
			}
		}

	}


	/**
	 * checks, if both Players have positioned all ships
	 * initiates Phase 3 and sets turn-Member in preparation of that.
	 * 
	 * @param ID	User-ID, of the Player, who does the ready-check
	 * @return true if both have placed all their ships, false otherwise
	 */
	private boolean checkReady(int ID) {
		boolean readyValue = false;
		//checking if Player him/herself is ready
		if(battleGrounds[ID].checkPlacement()){
			//checking if opponent is ready
			if(battleGrounds[(ID+1)%2].checkPlacement()){
				// both battleGrounds are full, the shooting begins
				phase=3;
				readyValue = true;
				//randomize whose turn it is, if not yet done
				if(turn<0){
					if(rnd.nextBoolean()){
						turn = 0;
					}else{
						turn = 1;
					}
				}
				//instanciate the two(for each Player) history-ArrayLists,
				//which represent the turn-dependent ArrayList of the two-dimensional Status-ArrayList that is a BattleGround-Status
				if(history == null ){
					System.out.println("History neu initialisiert.");
					history = new ArrayList<ArrayList<ArrayList<ArrayList<Status>>>>(2);
				}
				while(history.size() < 2 ){
					history.add(new ArrayList<ArrayList<ArrayList<Status>>>());
				}
			}else{
				turn = ID; // for being ready first, he/she gets to shoot first
				clients[ID].send("Your opponent is not yet ready. Please wait a moment.");
				clients[(ID+1)%2].send("Your opponent is ready. Please place your remaining ships and send #ready.");
			}
		}else{
			clients[ID].send("Place all your ships first.");
		}
		return readyValue;
	}

	/**
	 * Shooting-Command. Tries to shoot at the commanded spot and sends Reports to both Players.
	 * 
	 * initiates Phase 4
	 * 
	 * @param ID	User-ID of the User, who shoots
	 * @param substring Syntax of substring is already in this kinda format: (x,y) with x and y as integer.
	 */
	private void shoot(int ID, String substring) {
		String command = substring.trim();
		int firstBracketPos = -1;
		int commaPos = -1;
		int secondBracketPos = -1;
		firstBracketPos = command.lastIndexOf("(");
		commaPos = command.lastIndexOf(",");
		secondBracketPos = command.lastIndexOf(")");
		//number after the opening bracket and before the comma is the longitude
		int col = Integer.parseInt(command.substring(firstBracketPos+1, commaPos).trim());
		//number after the comma and before closing bracket is the latitude
		int row = Integer.parseInt(command.substring(commaPos+1,secondBracketPos).trim());

		//check if the values are valid, so we can be sure, that the last State of battleGrounds gets copied into the History
		if(col<sizeProposition && row<sizeProposition){	//sizeProposition equals the width/height, after Phase 1
			//copying BattleGround-Snapshot to the history of the opponent
			history.get((ID+1)%2).add(battleGrounds[(ID+1)%2].getBattleGroundSnapshot());

			clients[ID].send("You shoot at " + col + " longitude and " + row +" latitude.");
			clients[(ID+1)%2].send("Your opponent shoots at " + col + " longitude and " + row +" latitude.");
			HitOrMiss coordinateReport = battleGrounds[(ID+1)%2].shoot(col, row);
			clients[ID].send(coordinateReport.toString());	//sending the Report to the Player who shot
			clients[ID].send(battleGrounds[(ID+1)%2].getTheirBattleGrounds());	//sending the Opponent's Status to the Player who shot
			clients[(ID+1)%2].send(battleGrounds[(ID+1)%2].getOurBattleGrounds());	//showing own fleet Status to the attacked Player

			//switching turns, if necessary; sending the opposite Report to the Player who shot
			switch(coordinateReport){
			case HIT:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYHIT.toString());
				break;
			case MISS:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYMISS.toString());
				turn = (ID+1)%2;	// back to opponent
				break;
			case REHIT:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYREHIT.toString());
				turn = (ID+1)%2;	// back to opponent
				break;
			case REMISS:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYREMISS.toString());
				turn = (ID+1)%2;	// back to opponent
				break;
			case WON:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYWON.toString());
				clients[ID].send("Player 0 did " + history.get(0).size() + " turns.");
				clients[ID].send("Player 1 did " + history.get(1).size() + " turns.");
				clients[(ID+1)%2].send("Player 0 did " + history.get(0).size() + " turns.");
				clients[(ID+1)%2].send("Player 1 did " + history.get(1).size() + " turns.");
				phase = 4;
				turn = -1;	//noone's turn again
				break;
			default:
				clients[(ID+1)%2].send(HitOrMiss.ENEMYMISS.toString());
				turn = (ID+1)%2;	// back to opponent
				break;
			}
		}
	}

	
	/**
	 * shows History of BattleGrounds-Status
	 * Phase 4
	 * 
	 * @param iD
	 * @param user
	 * @param turn
	 * @return
	 */
	private String showHistory(int iD, int user, int turn) {
		return showHistory(iD, "(" + user + "," + turn + ")");
	}
	
	/**
	 * shows History of BattleGrounds-Status
	 * Phase 4
	 * 
	 * @param iD
	 * @param substring
	 * @return
	 */
	private String showHistory(int iD, String substring) {
		String command = substring.trim();
		int firstBracketPos = -1;
		int commaPos = -1;
		int secondBracketPos = -1;
		firstBracketPos = command.lastIndexOf("(");
		commaPos = command.lastIndexOf(",");
		secondBracketPos = command.lastIndexOf(")");
		String returnValue = "";
		//number after the opening bracket and before the comma is the User-ID whose history gets written out
		int userId = Integer.parseInt(command.substring(firstBracketPos+1, commaPos).trim());
		//number after the comma and before closing bracket is the Step/Turn-Number of the User whose history gets written out
		int userTurnNumber = Integer.parseInt(command.substring(commaPos+1,secondBracketPos).trim());

		//check if the values are valid, so we can reach the State of the battleGrounds at that turn
		if(userId<2 && userId>=0 && history.get(userId).size() > userTurnNumber){
			returnValue += "State of the Battle Ground of Player "+userId+ " at Turn No."+userTurnNumber+ "\r\n";
			ArrayList<ArrayList<Status>> battleGroundStates = history.get(userId).get(userTurnNumber);

			for(int rows=0; rows<sizeProposition; rows++){
				ArrayList<Status> row = battleGroundStates.get(rows);
				for(int cols=0; cols<sizeProposition; cols++){
					switch(row.get(cols)){
					case WATER:
						returnValue += "~";
						break;
					case SHIP:
						returnValue += "#";
						break;
					case HIT:
						returnValue += "X";
						break;
					case MISS:
						returnValue += "O";
						break;
					default:
						break;	
					}
				}
				returnValue += "\r\n"; //separate the rows
			}
		}
		return returnValue;
	}
	
	
	/**
	 * restarting the game. Sets back some state-representing member-variables
	 */
	private void restartGame(){
		sizeProposition = 0;
		phase = 0;
		turn = -1;
		battleGrounds[0] = null;
		battleGrounds[1] = null;
		parsingResult = null;
		history = null;
		rnd = new Random();
	}


	/**
	 * removing the Client-Thread (restarts game-state)
	 * 
	 * @param pos
	 */
	public synchronized void remove(int pos){
		if (pos >= 0){
			BsServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + pos);
			if (pos < clientCount-1){
				for (int i = pos+1; i < clientCount; i++){
					clients[i-1] = clients[i];
				}
			}
			clientCount--;
			restartGame();
			try{
				toTerminate.close(); 
			}
			catch(IOException ioe){
				System.out.println("Error closing thread: " + ioe); 
			}
			toTerminate.stop(); 
		}
	}
	
	public static void main(String args[]) {
		BsServer server = null;
		if (args.length != 1){
			System.out.println("Usage: java BsServer port");
		}
		else{
			server = new BsServer(Integer.parseInt(args[0]));
		}
	}
}