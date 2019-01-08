package igx.client;

// Server.java
// class Server - Client-side communications engine with server

import igx.shared.*;
import java.net.*;
import java.awt.Point;
import java.io.*;
import java.util.*;
import java.awt.Color;

public class Server extends SocketAction
{
  public ClientForum forum;
  public Dispatcher dispatch;
  public String name;

  public Server (Socket sock) {
	super(sock);
  }
  public String receive () throws IOException {
	String s = super.receive();
	return s;
  }
  public boolean receiveBoolean () throws IOException {
	String s = receive();
	if (s.equals("1"))
	  return true;
	else
	  return false;
  }
  public int receiveInt () throws IOException {
	String s = receive();
	return Integer.parseInt(s);
  }
// Main server-reading loop
  public void run() {
    String from, to, ships;
    try {
      Debug.d("Starting to read from server...");
      // Receive version info
      from = receive();
      if (!from.substring(0, Params.MAJOR_VERSION_LENGTH).equals(Params.VERSION.substring(0, Params.MAJOR_VERSION_LENGTH))) {
	send(Params.NACK);
	forum.frontEnd.versionProblem(from);
      } else
	send(Params.ACK);
      forum.setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", null);
      // Password loop
      boolean authenticated = false;
      while (!authenticated) {
	from = receive();
	if (from.equals(Params.ADDCOMPUTERPLAYER))
	  forum.setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", "That alias already belongs to a robot");
	else
	if (from.equals(Params.UPDATE))
	  forum.setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", "That alias contains illegal characters");
	else
	  if (from.equals(Params.NEW_ALIAS))
	    forum.setDialog(ClientForum.DIALOG_NEW_PASSWORD, "If you are a new user, enter you password. Otherwise, hit <CANCEL> to re-enter your alias.", null);
	  else
	    if (from.equals(Params.OLD_ALIAS))
	      forum.setDialog(ClientForum.DIALOG_PASSWORD, "Enter your password", null);
	    else
	      if (from.equals(Params.NACK)) {
		if (forum.dialogMode == ClientForum.DIALOG_NEW_PASSWORD)
		  forum.setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", null);
		else
		  forum.setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", "Incorrect Password");
	      } else
		if (from.equals(Params.ACK)) {
		  // Receive forum info
		  String info;
		  info = receive();
		  while (!info.equals(Params.ENDTRANSMISSION)) {
		    forum.post(info, ClientForum.BULLETIN_COLOUR);
		    info = receive();
		  }
		  info = receive();
		  Vector robots = new Vector();
		  while (!info.equals(Params.ENDTRANSMISSION)) {
		    String botType = info;
		    String botName = receive();
		    int rank = Integer.parseInt(receive());
		    robots.addElement(new Robot(botType, botName, rank));
		    info = receive();
		  }
		  int n = robots.size();
		  Robot[] robot = new Robot[n];
		  for (int i = 0; i < n; i++)
		    robot[i] = (Robot) (robots.elementAt(i));
		  forum.setBotList(robot);
		  // Begin registration
		  forum.registerClient();
		  authenticated = true;
		}
      }
      // Main loop
      while (true) {
	from = receive();
	if (from.compareTo(Params.FORUM) == 0) {
	  from = receive();
	  if (from.compareTo(Params.PLAYERARRIVED) == 0) {
	    to = receive(); // Get the player's name
	    forum.addPlayer(to);
	    if (dispatch != null)
	      dispatch.playerArrived(to);
	    else
	      forum.frontEnd.play(Params.AU_ARRIVED);
	  } else
	    if (from.compareTo(Params.NEWGAME) == 0) {
	      to = receive(); // Game name
	      from = receive(); // Player's name
	      forum.createGame(from, to);
	    } else
	      if (from.compareTo(Params.JOINGAME) == 0) {
		to = receive(); // Game name
		ships = receive(); // Player's name
		forum.joinGame(ships, to);
	      } else
		if (from.compareTo(Params.STARTGAME) == 0) {
		  to = receive(); // Game name
                  ships = receive(); // Game map
		  Game g = forum.startNewGame(to, ships);
		  if (g != null) { // True if player is in game
		    long seed = Long.parseLong(receive());
                    String mapLine = receive();
                    Point[] map = new Point[Params.PLANETS];
                    int points = 0;
                    while (!mapLine.equals(Params.RANDOMMAP)) {
                       int x = Integer.parseInt(mapLine);
                       mapLine = receive();
                       int y = Integer.parseInt(mapLine);
                       mapLine = receive();
                       map[points] = new Point(x,y);
                       points++;
                    }
		    GameInstance gameInstance = new GameInstance(seed, g.numPlayers, g.player);
                    if (points > 0) {
                       gameInstance.setMap(map);
                    }
		    dispatch = new Dispatcher(gameInstance, this, g.name);
		    forum.displayGame(dispatch, false);
		  }
		} else
		  if (from.compareTo(Params.CUSTOMMAP) == 0) {
                     Vector maps = new Vector();
                     String map = receive();
                     while (!map.equals(Params.CUSTOMMAP)) {
                        maps.addElement(map);
                        map = receive();
                     }
                     forum.doChooseMap(maps);
                  } else
		  if (from.compareTo(Params.ABANDONGAME) == 0) {
		    ships = receive(); // Player's name
		    forum.abandonGame(ships);
		  } else
		    if (from.compareTo(Params.PLAYERQUITTING) == 0) {
		      to = receive(); // Player's name
		      if (to.equals(name))
			// We got booted!
			forum.frontEnd.quitProgram();
		      if (forum.playerQuit(to))
			break;
		      else
			if (dispatch != null)
			  dispatch.playerLeft(to);
		    } else
		      if (from.compareTo(Params.SENDMESSAGE) == 0) {
			to = receive(); // Player's name
			from = receive(); // Destination of message (unused here)
			ships = receive(); // Message text
			forum.message(to, ships);
			if (dispatch != null)
			  dispatch.forumMessage(to, ships);
			else
			  forum.frontEnd.play(Params.AU_MESSAGE);
		      } else
			if (from.compareTo(Params.ADDCOMPUTERPLAYER) == 0) {
			  to = receive(); // Game name
			  ships = receive(); // Player's name
			  forum.addRobot(ships, to);
			} else
			  if (from.compareTo(Params.ADDCUSTOMCOMPUTERPLAYER) == 0) {
			    to = receive(); // Game name
			    String result = receive(); // Either ACK or NACK
			    ships = receive(); // Either name, or reason of failure
			    if (result.equals(Params.ACK)) {
			      Robot r = new Robot("Custom", ships, 0);
			      forum.addCustomRobot(r, to);
			    } else
			      forum.post("Couldn't add robot: " + ships, Color.red);
			  } else
			    if (from.compareTo(Params.REMOVECOMPUTERPLAYER) == 0) {
			      to = receive(); // Game name
			      ships = receive(); // Player's name
			      forum.removeRobot(ships, to);
			    } else
			      if (from.compareTo(Params.GAMEENDED) == 0) {
				to = receive(); // Game name
				ships = receive(); // Game winner
				forum.gameOver(to, ships);
			      } else
				if (from.compareTo(Params.WATCHGAME) == 0) {
				  forum.dialog.setMessageText("Please wait while game is downloaded...");
				  GameInstance gameInstance = new GameInstance();
				  String gameName = receive();
				  // Game info packet
				  int numPlayers = receiveInt();
				  int turn = receiveInt();
				  int segment = receiveInt();
				  Player[] player = new Player[numPlayers];
				  for (int i = 0; i < numPlayers; i++) {
				    player[i] = new Player(receive());
				    player[i].isActive = receiveBoolean();
				    player[i].isHuman = receiveBoolean();
				    player[i].status = receiveInt();
				    player[i].number = i;
				    if (player[i].isHuman && (forum.getPlayer(player[i].name) == null))
				      player[i].isPresent = false;
				  }
				  Planet[] planet = new Planet[Params.PLANETS];
				  for (int i = 0; i < Params.PLANETS; i++) {
				    planet[i] = new Planet(i, gameInstance);
				    planet[i].x = receiveInt();
				    planet[i].y = receiveInt();
				    int playerNum = receiveInt();
				    if (playerNum == Params.MAXPLAYERS)
				      planet[i].owner = Player.NEUTRAL;
				    else
				      planet[i].owner = player[playerNum];
				    planet[i].production = receiveInt();
				    planet[i].ratio = receiveInt();
				    planet[i].ships = receiveInt();
				    planet[i].defenceRatio = receiveInt();
				    planet[i].prodTurns = receiveInt();
				    planet[i].blackHole = receiveBoolean();
				    String info = receive();
				    while (!info.equals(Params.ENDTRANSMISSION)) {
				      int j = Integer.parseInt(info);
				      planet[i].attacker[j] = receiveInt();
				      info = receive();
				    }
				  }
				  FleetQueue fleetQueue = new FleetQueue(gameInstance);
				  String info = receive();
				  while (!info.equals(Params.ENDTRANSMISSION)) {
				    Planet dest = planet[Integer.parseInt(info)];
                                    Planet source = planet[receiveInt()];
				    int numShips = receiveInt();
				    int ratio = receiveInt();
				    Player owner = player[receiveInt()];
				    float distance = new Float(receive()).floatValue();
				    Fleet f = new Fleet(gameInstance, dest, source, owner, numShips, ratio, distance);
				    fleetQueue.insert(f);
				    info = receive();
				  }
				  gameInstance.setGameInstance(numPlayers, player, planet, fleetQueue, turn, segment);
				  // Now, do something with the game Instance.
				  forum.selectGame(gameName);
				  dispatch = new Dispatcher(gameInstance, this, gameName);
				  forum.watchGame(name, gameName);
				  Player gamePlayer = gameInstance.getPlayer(name);
				  if (gamePlayer != null) {
				    gamePlayer.isPresent = true;
				    forum.displayGame(dispatch, false);
				  } else
				    forum.displayGame(dispatch, true);
				}
	} else
	  if (dispatch != null) {
	    if (from.compareTo(Params.UPDATE) == 0) { // Update seed
	      to = receive();
	      long seed = Long.parseLong(to);
	      dispatch.Game.setSeed(seed);
	    } else
	      if (from.compareTo(Params.ENDTRANSMISSION) == 0)
		dispatch.advance();
	      else
		if (from.compareTo(Params.PLAYERQUITTING) == 0) {
		  to = receive(); // Determine who is quitting
		  int who = Integer.parseInt(to);
		  ships = receive(); // What kind of quitting?
		  int status = Integer.parseInt(ships);
		  //
		  if (status == Params.QUIT_SIGNAL) {
		    if (who == -1) { // Game watcher - us!
		      Player p = forum.getPlayer(name);
		      p.isActive = false;
		      p.inGame = false;
		      p.game = null;
		      forum.displayForum(true);
		    } else {
		      dispatch.playerQuit(who, status);
		      if (name.equals(dispatch.Game.player[who].name)) {
			forum.frontEnd.play(Params.AU_YOU_QUIT);
			// It's us who's quitting
			forum.displayForum(true);
		      }
		    }
		  } else
		    dispatch.playerQuit(who, status);
		} else
		  if (from.compareTo(Params.SENDMESSAGE) == 0) {
		    to = receive(); // Determine who is sending
		    ships = receive(); // Determine who is the recipient
		    from = receive(); // Get actual message
		    int receiver = Integer.parseInt(ships);
		    Player sender = dispatch.Game.getPlayer(to);
		    if (sender == null) {
		      if (receiver == Params.MAXPLAYERS)
			dispatch.watcherMessage(to, Player.NEUTRAL, from);
		      else
			dispatch.watcherMessage(to, dispatch.Game.player[receiver], from);
		    } else {
		      if (receiver == Params.MAXPLAYERS)
			dispatch.message(sender, Player.NEUTRAL, from);
		      else
			dispatch.message(sender, dispatch.Game.player[receiver], from);
		    }
		  } else { // Fleet
		    to = receive();
		    ships = receive();
		    dispatch.addFleet(new Fleet(dispatch.Game, dispatch.Game.planet[Integer.valueOf(from).intValue()], dispatch.Game.planet[Integer.valueOf(to).intValue()], Integer.valueOf(ships).intValue()));
		  }
	  }
      }
    } catch (IOException e) {
    }
    closeConnections();
    forum.frontEnd.quitProgram();
  }
  public void setForum (ClientForum forum) {
    this.forum = forum;
  }
}
