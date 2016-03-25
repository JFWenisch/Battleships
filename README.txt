README.txt

BattleShips

A simple network-solution for the boardgame battle ships.



1. Starting of the server
		to start the server, use the command:
			java BsServer portnummer
		e.g.:
			java BsServer 4444
		or in JAR-format:
			java -jar BattleShipServer_packaged.jar 4444

2. Starting of the client
		To start the client, use the command:
			java BsClient host portnummer
		e.g.:
			java BsClient 127.0.0.1 4444
		or in JAR-format:
			java -jar BattleShipClient_packaged.jar 127.0.0.1 4444

Clients can always see the help with the command "/help"(without the quotes).
Clients can always just chat, if they dont use "/" or "#" as the first sign.

---------Example course of a game:----------------
Both users propose a size between 10 and 50 with the command:
	#size10
Since both chose 10, it will be 10. (the average of both is used)
Now both players can position their ships. For example, in a 10x10-match, like this:
	#place A(0,0|E); C(4,9|E); D(2,2|S); D(4,2|S); D(6,2|S); P(1,6|E); P(1,8|E); P(4,7|E); P(6,7|S);
One of the players has to initiate ending the positioning-phase, with the command:
	#ready
From now on, the players alternatingly shoot on their battlegrounds.(If you hit, you get another turn.) To shoot the north-western corner, you send:
	#shoot(0,0)
This goes round and round until one player has lost all his ships.(or to be more exact: all quadrants where part of a ship is)

After one player has won, each player may look at the past states of the battlegrounds. To see the ship placement of player0, you send: 
	#history(0,0)
To view the state of player1's fleet before the 3rd shot of player0, you send:
	#history(1,2)