README.txt

BattleShips

Eine simple Netzwerklösung für das Brettspiel Schiffe versenken.



1. Starten des Servers
		Zum Starten des Servers wird
			java BsjnServer portnummer
		ausgeführt. Also z.B.:
			java BsjnServer 4444
		bzw. in JAR-Format:
			java -jar BattleShipServer_JNeutze_packaged.jar 4444

2. Starten der Clients
		Zum Starten der Clients wird
			java BsjnClient host portnummer
		ausgeführt. Also z.B. lokal:
			java BsjnClient 127.0.0.1 4444
		bzw. mit dem JAR-Paket:
			java -jar BattleShipClient_JNeutze_packaged.jar 127.0.0.1 4444

Clients können jederzeit die Programm-interne Hilfe mit dem Kommando "/help"(ohne die Anführungszeichen) einsehen.

Nachfolgend ein...
---------Beispiel-Spielablauf:----------------
Beide Nutzer schlagen eine Feldgröße vor, mit dem Befehl:
	#size10
Da beide das gleiche wählten, wird 10 als Größe gewählt. (ansonsten der Durchschnitt beider)
Nun können beide Spieler ihre Schiffe setzen. Bei einem 10x10-Spiel z.B. so:
	#place A(0,0|E); C(4,9|E); D(2,2|S); D(4,2|S); D(6,2|S); P(1,6|E); P(1,8|E); P(4,7|E); P(6,7|S);
Ein Spieler muss die Beendigung der Platzierungs-Phase einleiten, in dem das Kommando:
	#ready
gesendet wird.
Ab nun schießen die Spieler abwechselnd auf ihre Schlachtfelder. Um zum Beispiel die nordwestlichste Ecke zu beschießen, sendet man:
	#shoot(0,0)
Dies wird nun fortgesetzt, bis ein Spieler alle Schiffe komplett verloren hat.
Anschließend kann man sich die einzelnen vergangenen Schlachtfelder-Status anschauen. Um die Positionierung von Spieler0 zu sehen, sendet man:
	#history(0,0)
Um den Zustand der Flotte von Spieler1 vor dem 3. Schuss von Spieler0 zu sehen, sendet man:
	#history(1,2)