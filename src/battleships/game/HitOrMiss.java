package battleships.game;

public enum HitOrMiss {
	HIT{
		public String toString(){
			return "*BAAAM* You hit. It is your turn again now.";
		}
	},
	MISS{
		public String toString(){
			return "*splosh* You missed.";
		}
	},
	REHIT{
		public String toString(){
			return "*BAM* You wanted to make sure everything is dead there?! It was already hit.";
		}
	},
	// pun intended
	REMISS{ 
		public String toString(){
			return "*splosh* Still nothing there.";
		}
	},
	
	ENEMYHIT{
		public String toString(){
			return "*BAAAM* Your opponent hit and is able to shoot again.";
		}
	},
	ENEMYMISS{
		public String toString(){
			return "*splosh* Your opponent missed. It is your turn now.";
		}
	},
	ENEMYREHIT{
		public String toString(){
			return "*BAM* Your opponent knows no mercy for your ship and hits a part of it a 2nd time. It is your turn now.";
		}
	},
	// pun intended
	ENEMYREMISS{ 
		public String toString(){
			return "*splosh* Opponent is remissive and hit the an empty spot a second time. It is your turn now.";
		}
	},
	
	WON{ 
		public String toString(){
			return "*BAAAM* You hit and sank the last of the Ships. You are victorious. Use the #history-Command for analysis.";
		}
	},
	
	ENEMYWON{ 
		public String toString(){
			return "*BAAAM* The opponent hit and sank the last of your Ships. You lost. Use the #history-Command for analysis.";
		}
	}
	
}
