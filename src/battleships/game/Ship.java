package battleships.game;

public abstract class Ship {
	protected static int size;
	protected Direction direction;
	protected int row, col;	//latitude and longitude respectively of the most NW(north-western) point of the ship
	
	protected void startup(int _col, int _row, Direction _direction){
		direction = _direction;
		row = _row;
		col = _col;
	}
	
	/**
	 * get Direction, which is either SOUTH or EAST
	 * 
	 * @return
	 */
	public Direction getDirection() {
		return direction;
	}
	
	/**
	 * get latitude of most North-Western coordinate
	 * @return
	 */
	public int getRow() {
		return row;
	}

	/**
	 * get longitude of most North-Western coordinate
	 * @return
	 */
	public int getCol() {
		return col;
	}
	
	/**
	 * returning the size
	 * 
	 * @return
	 */
	abstract int getSize();
	
	/**
	 * returning a String in the format Class.Name(x,y|Direction)
	 * 
	 */
	public String toString(){
		return this.getClass().getName() + "(" + getCol() + "," + getRow() + "|" + getDirection() + ")";
	}
}

