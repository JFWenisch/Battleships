package battleships.texts;

public enum Text {
	Intro1{
		public String toString(){
			return "Welcome to BattleShips, Player ";
		}
	},
	
	Intro2{
		public String toString(){
			return ". Use /exit for exiting and /help for further help.";
		}
	},
	
	Start{
		public String toString(){
			return "Both Players are present. The game starts.";
		}
	},
	
	OpponentNeeded{
		public String toString(){
			return "Your opponent is not yet present. A 2nd player is needed.";
		}
	},
	
	AircraftCarriers{
		public String toString(){
			return "AircraftCarriers";
		}
	},
	
	Cruisers{
		public String toString(){
			return "Cruisers";
		}
	},
	
	Destroyers{
		public String toString(){
			return "Destroyers";
		}
	},
	
	PatrolBoats{
		public String toString(){
			return "PatrolBoats";
		}
	},
	
	YouCanStillPlace{
		public String toString(){
			return "You can still place";
		}
	},
}
