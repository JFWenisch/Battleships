package battleships.game;


import java.util.ArrayList;

public class BattleGround {
	private ArrayList<ArrayList<Status>> battleGrounds = new ArrayList<ArrayList<Status>>();
	private int size;
	//private ArrayList<Ship> ships = new ArrayList<Ship>();

	//number of Ship-types for testing placement
	private int numberOfAircraftCarriers = 0;
	private int numberOfCruisers = 0;
	private int numberOfDestroyers = 0;
	private int numberOfPatrolBoats = 0;

	private int allowedAircraftCarriers = 0;
	private int allowedCruisers = 0;
	private int allowedDestroyers = 0;
	private int allowedPatrolBoats = 0;
	
	private int possibleHits = -1;
	private int individualHits = 0;

	public BattleGround(int _size){
		size=_size;
		for(int rows=0; rows<size; rows++){
			ArrayList<Status> row = new ArrayList<Status>();
			for(int cols=0; cols<size; cols++){

				row.add(Status.WATER);
			}
			battleGrounds.add(row);
		}
		calculateFleetSize();
	}
	
	/**
	 * getBattleGroundSnapshot is used by the Server to log the opponents BattleGround's States 
	 * 
	 * @return
	 */
	public ArrayList<ArrayList<Status>> getBattleGroundSnapshot(){
		// creating a new twodimensional Status-ArrayList
		ArrayList<ArrayList<Status>> battleGroundSnapshot = new ArrayList<ArrayList<Status>>();
		for(int rows=0; rows<size; rows++){
			// creating a new Status-ArrayList-row
			ArrayList<Status> row = new ArrayList<Status>();
			for(int cols=0; cols<size; cols++){
				row.add(battleGrounds.get(rows).get(cols));	//copying the value of the original
			}
			battleGroundSnapshot.add(row);	//adding the complete row
		}
		return battleGroundSnapshot;
	}
	
	

	/**
	 * Posts the Battlegrounds
	 * Used by the Player, who owns the ships in that territory
	 * It shows everything.
	 * 
	 * @return Multiple Lines of String. First line is introduction, the rest is the ASCII-representation of the view
	 */
	public String getOurBattleGrounds(){
		String returnValue = "\r\nOur Battle Grounds:\r\n";
		for(int rows=0; rows<size; rows++){
			ArrayList<Status> row = battleGrounds.get(rows);
			for(int cols=0; cols<size; cols++){
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
		return returnValue;
	}

	/**
	 * Posts the Battlegrounds
	 * Used by the Opponent of the Player, who owns the ships in that territory
	 * It doesn't show ships.
	 * 
	 * @return Multiple Lines of String. First line is introduction, the rest is the ASCII-representation of the view
	 */
	public String getTheirBattleGrounds(){
		String returnValue = "\r\nTheir Battle Grounds:\r\n";
		for(int rows=0; rows<size; rows++){
			ArrayList<Status> row = battleGrounds.get(rows);
			for(int cols=0; cols<size; cols++){
				switch(row.get(cols)){
				case WATER:
					returnValue += "~";
					break;
				case SHIP:
					returnValue += "~";
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
		return returnValue;
	}
	
	public int getSize() {
		return size;
	}
	
	protected ArrayList<ArrayList<Status>> getBattleGrounds() {
		return battleGrounds;
	}

	public int getNumberOfStillAllowedAircraftCarriers() {
		return allowedAircraftCarriers - numberOfAircraftCarriers;
	}

	public int getNumberOfStillAllowedCruisers() {
		return allowedCruisers - numberOfCruisers;
	}

	public int getNumberOfStillAllowedDestroyers() {
		return allowedDestroyers - numberOfDestroyers;
	}

	public int getNumberOfStillAllowedPatrolBoats() {
		return allowedPatrolBoats - numberOfPatrolBoats;
	}

	public int getNumberOfAllowedShipsOfThisType(Ship ship) {
		int allowedShipsOfThisType;
		if(ship instanceof AircraftCarrier){
			allowedShipsOfThisType = getNumberOfStillAllowedAircraftCarriers();
		}else if(ship instanceof Cruiser){
			allowedShipsOfThisType = getNumberOfStillAllowedCruisers();
		}else if(ship instanceof Destroyer){
			allowedShipsOfThisType = getNumberOfStillAllowedDestroyers();
		}else if(ship instanceof PatrolBoat){
			allowedShipsOfThisType = getNumberOfStillAllowedPatrolBoats();
		}else{
			allowedShipsOfThisType = 0;
		}
		return allowedShipsOfThisType;
	}


	/**
	 * increments the number of ships of this type on this battleground
	 * 
	 * @param ship
	 */
	private void addShipCount(Ship ship) {
		if(ship instanceof AircraftCarrier){
			numberOfAircraftCarriers++;
		}else if(ship instanceof Cruiser){
			numberOfCruisers++;
		}else if(ship instanceof Destroyer){
			numberOfDestroyers++;
		}else if(ship instanceof PatrolBoat){
			numberOfPatrolBoats++;
		}
	}

	/**
	 * calculates the maximum allowed Instances of a ship-type
	 * 
	 */
	public void calculateFleetSize() {
		// ~5% of the Space
		allowedAircraftCarriers = (size*size*5)/(100*AircraftCarrier.getStaticSize());
		// ~6% of the Space (so rounded to 1 in the standard 10x10-Field, but potentially more than Carriers)
		allowedCruisers = (size*size*6)/(100*Cruiser.getStaticSize());
		// ~9% of the Space (so 3 Destroyers in Standard-Game)
		allowedDestroyers = (size*size*9)/(100*Destroyer.getStaticSize());
		// ~8% of the Space (so 4 PatrolBoats in Standard-Game)
		allowedPatrolBoats = (size*size*8)/(100*PatrolBoat.getStaticSize());
		
		//calculating the individual hits that are necessary for all Ships
		possibleHits = allowedAircraftCarriers*AircraftCarrier.getStaticSize() + allowedCruisers*Cruiser.getStaticSize() + allowedDestroyers*Destroyer.getStaticSize() + allowedPatrolBoats*PatrolBoat.getStaticSize();
	}

	/**
	 * tests if all Ships are positioned on this BattleGround. returns true if so.
	 * 
	 * @return
	 */
	public boolean checkPlacement(){
		boolean allPlaced = true;
		if(getNumberOfStillAllowedAircraftCarriers()>0 || getNumberOfStillAllowedCruisers()>0 || getNumberOfStillAllowedDestroyers()>0 || getNumberOfStillAllowedPatrolBoats()>0){
			allPlaced = false;
		}
		return allPlaced;
	}

	/**
	 * tries to place the ship and returns Status-Report
	 * 
	 * @param ship
	 * @return
	 */
	public String placeShip(Ship ship){
		String report = "";
		int cols = ship.getCol();
		int rows = ship.getRow();
		Direction direction = ship.getDirection();
		int size = ship.getSize();
		//boolean taken = false; // represents the finding that the placing is not possible 
		try{
			if(getNumberOfAllowedShipsOfThisType(ship) > 0){
				if(direction == Direction.EAST){
					// for Direction EAST, we only have to check in one row for the columns
					ArrayList<Status> row = battleGrounds.get(rows);
					for(int i=0;i<size;i++){
						if(row.get(cols+i) != Status.WATER){
							throw new IndexOutOfBoundsException();	// one potential space doesnt have water
						}
					}
					// since the whole desired space for the ship is free, we can place it, and forget about the Ship
					for(int i=0;i<size;i++){
						row.set(cols+i,Status.SHIP);
					}

				}else if(direction == Direction.SOUTH){
					// for Direction EAST, we have to check one column within multiple rows
					for(int i=0;i<size;i++){
						if(battleGrounds.get(rows+i).get(cols) != Status.WATER){
							throw new IndexOutOfBoundsException();	// one potential space doesnt have water
						}
					}
					// since the whole desired space for the ship is free, we can place it, and forget about the Ship
					for(int i=0;i<size;i++){
						battleGrounds.get(rows+i).set(cols,Status.SHIP);
					}
				}else{
					// not really possible without strange hacking
					throw new IndexOutOfBoundsException("Non-Allowed Direction");
				}

				report = ship.toString() + " was positioned.";
				addShipCount(ship);
				ship=null;
			} 
		}
		catch(IndexOutOfBoundsException e){
			report = ship.toString() + " doesn't fit into that Area.";
		}

		return report;
	}
	
	/**
	 * shoots at the given coordinate
	 * returns 
	 * 
	 * @param cols
	 * @param rows
	 * @return
	 */
	public HitOrMiss shoot(int cols, int rows){
			HitOrMiss returnMessage;
			Status coordinate = battleGrounds.get(rows).get(cols);
			switch(coordinate){
				case WATER:
					//MISS
					battleGrounds.get(rows).set(cols,Status.MISS);
					returnMessage = HitOrMiss.MISS;
					break;
				case SHIP:
					//HIT
					battleGrounds.get(rows).set(cols,Status.HIT);
					returnMessage = HitOrMiss.HIT;
					//counting REAL hits:
					individualHits++;
					//if all individual Ship-Parts got hit once
					if(individualHits>=possibleHits){
						returnMessage = HitOrMiss.WON;
					}
					break;
				case MISS:
					//missed again
					returnMessage = HitOrMiss.REMISS;
					break;
				case HIT:
					//hit again
					returnMessage = HitOrMiss.REHIT;
					break;
				default:
					returnMessage = HitOrMiss.MISS;
					break;
			}
			return returnMessage;
	}
}
