package igx.server;

// ComputerInterface.java 

/****
** Computer Interface
*
**** Always the same
* int yourNumber
* int numPlayers
* int numPlanets
* int mapWidth
* int mapHeight
* String[numPlayers] playerNames
**** Vary from turn to turn
* int time (in segments)
* int numMessages (number of messages to this player this turn)
* int[numMessages] messageSender
* String[numMessages] messageText
*** Planet Info
* int[numPlanets] planetX (x positions of all planets)
* int[numPlanets] planetY (y positions of all planets)
* int[numPlanets] planetOwner (owner of planet)
* int[numPlanets] planetShips (ships on planets)
* int[numPlanets] planetProduction (production on planets)
* int[numPlanets] planetRatio (ratio on planets)
* int[numPlanets] planetYourAttackers (number of your attackers on planet)
* int[numPlanets] planetOtherAttackers (number of other attackers on planet)
*** Fleet Info
* int numFleets (number of Arriving fleets)
* int[numFleets] fleetOwner (owner of fleets)
* int[numFleets] fleetDestination (destination planets of fleets)
* int[numFleets] fleetShips (number of ships on fleets)
* int[numFleets] fleetRatio (ratios of fleets)
****/

// import igx.shared.*;
import igx.bots.*;
import igx.shared.Params;
import igx.shared.Monitor;

public class ComputerInterface extends Bot
{
  private static Monitor monitor = new Monitor();
  
  static {
	System.loadLibrary("chortles");
  }
  
  GameState state = null;
  Message [] message = null;
  
  int gameOver = 0;
  
  int gameID;
  
  private static int gameNumber = 0;
  
  public native ComputerResult beejify
  (int yourNumber,
   int numPlayers,
   int numPlanets,
   int mapWidth,
   int mapHeight,
   String[] playerNames,
   int time,
   int numMessages,
   int[] messageSender,
   String[] messageText,
   int[] planetX,
   int[] planetY,
   int[] planetOwner,
   int[] planetShips,
   int[] planetProduction,
   int[] planetRatio,
   int[] planetYourAttackers,
   int[] planetOtherAttackers,
   int numFleets,
   int[] fleetOwner,
   int[] fleetDestination,
   int[] fleetShips,
   int[] fleetRatio,
   int gameID,
   int gameOver);   
  public String createName (int skillLevel) {
	switch (skillLevel) {
	case 1:
	  return "Drone";
	case 2:
	  return "Wasp";
	case 3:
	  return "Honey";
	case 4:
	  return "Larva";
	case 5:
	  return "Pupa";
	case 6:
	  return "Hornet";
	case 7:
	  return "Queen";
	case 8:
	  return "Worker";
	case 9:
	  return "Stinger";
	default:
	  return "Bumble";
	}
  }  
  public void endGame () {
	gameOver = 1;
	update(state, state, message);
  }  
  public static int generateGameNumber () {
	return gameNumber++;
  }  
  public void newGame (GameState game, int skillLevel) {
	gameID = generateGameNumber();
  }  
  public void update (GameState game, GameState old, Message[] message) {
	// Retarded hack to indicate which robot skill this is to Beej.
	/*    if (gameOver != 1)
	  gameOver = getSkillLevel() + 1;*/
	state = game;
	this.message = message;
	//  public ComputerResult update (int playerNumber, int gameID, GameInstance game, int gameOver) {
	//ComputerPlayer player = (ComputerPlayer)game.player[playerNumber];
	int i;
	// Make a package to send to computer player
	int playerNumber = getNumber();
	int yourNumber = getNumber() + 1;
	int numPlayers = game.getNumberOfPlayers();
	int numPlanets = Params.PLANETS;
	int mapWidth = Params.MAPX;
	int mapHeight = Params.MAPY;
	String[] playerNames = new String[numPlayers];
	for (i = 0; i < numPlayers; i++)
	  playerNames[i] = game.getPlayer(i).getName();
	int time = Params.SEGMENTS * game.getTurn() + game.getSegment();
	// ComputerMessage[] messages = player.getMessages();
	int numMessages = message.length;
	int[] messageSender = new int[numMessages];
	String[] messageText = new String[numMessages];
	for (i = 0; i < numMessages; i++) {
	  messageSender[i] = message[i].getSenderNumber() + 1;
	  messageText[i] = message[i].getText();
	}
	int[] planetX = new int[numPlanets];
	int[] planetY = new int[numPlanets];
	int[] planetOwner = new int[numPlanets];
	int[] planetShips = new int[numPlanets];
	int[] planetProduction = new int[numPlanets];
	int[] planetRatio = new int[numPlanets];
	int[] planetYourAttackers = new int[numPlanets];
	int[] planetOtherAttackers = new int[numPlanets];
	for (i = 0; i < numPlanets; i++) {
	  Planet p = game.getPlanet(i);
	  planetX[i] = p.getX();
	  planetY[i] = p.getY();
	  if (p.getOwner() == Constants.NEUTRAL)
	planetOwner[i] = 0;
	  else
	planetOwner[i] = p.getOwner() + 1;
	  planetShips[i] = p.getShips();
	  planetProduction[i] = p.getProduction();
	  planetRatio[i] = p.getRatio();
	  planetYourAttackers[i] = p.getAttackers(playerNumber);
	  planetOtherAttackers[i] = 0;
	  for (int o = 0; o < numPlayers; o++)
	if (o != playerNumber)
	  planetOtherAttackers[i] += p.getAttackers(o);
	}
	int numFleets = game.getNumberOfArrivedFleets();
	int[] fleetOwner = new int[numFleets];
	int[] fleetDestination = new int[numFleets];
	int[] fleetShips = new int[numFleets];
	int[] fleetRatio = new int[numFleets];
	for (i = 0; i < numFleets; i++) {
	  ArrivedFleet f = game.getArrivedFleet(i);
	  if (f.getOwner() == Constants.NEUTRAL)
	fleetOwner[i] = 0;
	  else
	fleetOwner[i] = f.getOwner() + 1;
	  fleetDestination[i] = f.getPlanetNumber();
	  fleetShips[i] = f.getShips();
	  fleetRatio[i] = 0;
	}
	// Send either game over flag or the skill number
	if (gameOver != 1)
	  gameOver = getSkillLevel() + 2;
	// Call native method here:		
	ComputerResult r;
	monitor.lock();
	r = beejify(yourNumber
		,numPlayers
		,numPlanets
		,mapWidth
		,mapHeight
		,playerNames
		,time
		,numMessages
		,messageSender
		,messageText
		,planetX
		,planetY
		,planetOwner
		,planetShips
		,planetProduction
		,planetRatio
		,planetYourAttackers
		,planetOtherAttackers
		,numFleets
		,fleetOwner
		,fleetDestination
		,fleetShips
		,fleetRatio
		,gameID
		,gameOver);
	monitor.unlock();
	int numSends = r.fleetSource.length;
	Fleet[] fleets = new Fleet[numSends];
	for (i = 0; i < numSends; i++) 
	  fleets[i] = new Fleet(r.fleetSource[i], r.fleetDestination[i], r.fleetShips[i]);
	sendBatchFleets(fleets);
  }  
}