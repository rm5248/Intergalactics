package igx.bots;

// RobotArena.java 

import igx.shared.GameInstance;
import java.util.*;
import java.io.*;
import java.awt.Color;

/**
 * This is the testing grounds for your 'bot. The RobotArena will run a game or multiple games with your
 * robot(s) and report the outcome. You can interface this class in two ways. The standard use of the Arena
 * is to call the {@link #main main} method with the appropriate parameters from the command-line. For more
 * advanced techniques (such as genetic algorithms or other kinds of statistical testing) you can write 
 * your own testing code that calls the {@link #runGame runGame} method.
 * <P>
 * Unlike the igx server, the RobotArena is kinder to your 'bots with respect to time. Your 'bots each run
 * in their own thread asynchronously to the game thread. Thus, if your 'bots are taking too long processing,
 * they can miss their chance to make moves if the game thread advances. The RobotArena won't do this, however.
 * It will always wait until all 'bot threads are finished processing before it advances to the next segment.
 * The Arena can return information on how long it took the 'bots to process, however, so be sure to make
 * sure your 'bot isn't sleeping on the job.
 */

public class RobotArena implements RobotServer, igx.shared.UI
{

  /**
   * The 'bots themselves.
   */
  public Bot[] bot = new Bot[Constants.MAXIMUM_PLAYERS];

  /**
   * The classes of the bots that will compete.
   */
  private Class[] botClass = new Class[Constants.MAXIMUM_PLAYERS];

  /**
   * The skill level of each competing 'bot.
   */
  private int[] skill = new int[Constants.MAXIMUM_PLAYERS];

  /**
   * The number of bots competing.
   */
  private int numBots = 0;

  /**
   * The amount of robot activity to report during a game (between 0 and 4).
   */
  public int activityLevel = 0;
  
  /**
   * Indicates whether a full report should follow each game.
   */
  public boolean fullReport = false;
 
  /**
   * Indicates whether random events should be reported.
   */
  public boolean reportEvents = false;

  /**
   * Indicates whether the bots' debug info should be output.
   */
  public boolean debugMode = false;

  /**
   * Indicates whether the bots should be timed or not.
   */
  public boolean timeRobots = false;

  /**
   * The random number seed to be used.
   */
  public long seed = System.currentTimeMillis();

  private int numGames = 1;

  /**
   * The number of segments to play.
   */ 
  public int numSegments = 1000;

  /**
   * Number of segments before updates.
   */
  public int updatePeriod = 0;

  private Vector[] messages;
  private Vector dispatches;
  private Vector arrivals;
  private boolean robotDone = false;
  private GameInstance gameInstance;
  private GameState game;

  /**
   * Adds a robot to the arena given a {@link Bot Bot} object and a skill.
   */
  public void addRobot (Bot bot, int skill) {
	addRobot(bot.getClass(), skill);
  }  
  /**
   * Adds a robot to the arena given the class name and skill. You can also add a
   * robot with {@link #addRobot(igx.bots.Bot,int) the other addRobot}.
   */
  public void addRobot (Class robotClass, int skill) {
	if (numBots == Constants.MAXIMUM_PLAYERS)
	  return;
	Bot robot;
	try {
	  // Try to instantiate as a robot
	  robot = (Bot)robotClass.newInstance();
	  botClass[numBots] = robotClass;
	  this.skill[numBots] = skill;
	  numBots++;
	} catch (ClassCastException e) {
	  complain("This... thing... is not a 'bot: " + robotClass + "!");
	} catch (Exception e) {
	  complain("Problem with your 'bot, " + robotClass + ". " + e + ".");
	}
  }  
  private static void complain (String complaint) {
	System.out.println(complaint);
	showUsage();
  }  
  /**
   * Given a {@link GameState GameState}, outputs the state of the galaxy to the screen.
   */
  public void displayGalaxy (GameState game) {
	System.out.println("Time: " + game.getTurn() + ":" + game.getSegment());
	for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
	  for (int x = 0; x < Constants.MAP_WIDTH; x++) {
	int point = game.getXY(x, y);
	char c = '.';
	if (point != Constants.EMPTY_SPACE)
	  c = Planet.planetChar(point);
	System.out.print(new Character(c).toString());
	  }
	  System.out.println("");
	}
	for (int i = 0; i < Constants.PLANETS; i++) {
	  System.out.print(new Character(Planet.planetChar(i)).toString() +
		       "(" + i + ") - owner: ");
	  Planet p = game.getPlanet(i);
	  if (p.getOwner() == Constants.NEUTRAL)
	System.out.println("Neutral");
	  else {
	System.out.print(game.getPlayer(p.getOwner()).getName() + "(" + p.getOwner() + ")");
	System.out.print(" - ships: " + p.getShips());
	System.out.print(" - production: " + p.getProduction());
	System.out.print(" - ratio: " + p.getRatio());
	for (int j = 0; j < numBots; j++) 
	  if (p.getAttackers(j) != 0) 
	    System.out.print(" - " + game.getPlayer(j).getName() + "(" + j + "): " + p.getAttackers(j));
	System.out.println("");
	  }
	}
  }  
  /**
   * Called by main to run the command-line Arena.
   */
  public void doArena () {    
	if (numGames == 1)
	  runGame();
	else {
	  int[] stats = new int[numBots];
	  for (int i = 0; i < numBots; i++) 
	stats[i] = 0;
	  for (int i = 0; i < numGames; i++) {
	System.out.println("----------");
	System.out.println("Game #" + (i+1));
	int winner = runGame().winner;
	stats[winner]++;
	seed++;
	  }
	  // Output final results
	  System.out.println("-------------");
	  System.out.println("Final Results");
	  for (int i = 0; i < numBots; i++) 
	robotReport(bot[i], ": Won " + stats[i] + " games.");
	}
  }  
  /**
   * Gets a particular bot (by number)
   */
  public Bot getBot (int i) {
	return bot[i];
  }  
  private static String[] getFileArgs (String file) {
	Vector result = new Vector();
	try {
	  BufferedReader br = new BufferedReader(new FileReader(file));
	  String line = br.readLine();
	  while (line != null) {
	if (line.charAt(0) == '#') {
	  line = br.readLine();
	  continue;
	}
	int end = line.length();
	int begin = 0;
	int space = line.indexOf(' ', begin);
	while (space != -1) {
	  result.addElement(line.substring(begin, space));
	  begin = space+1;
	  space = line.indexOf(' ', begin);
	}
	result.addElement(line.substring(begin));
	line = br.readLine();
	  }
	} catch (FileNotFoundException e) {
	  complain("Couldn't find parameter file: " + file + ".");
	} catch (IOException e) {
	  complain("IO Error: " + e);
	}
	String[] retVal = new String[result.size()];
	for (int i = 0; i < result.size(); i++) 
	  retVal[i] = (String)(result.elementAt(i));
	return retVal;
  }  
  private Message[] getMessages (int botNum) {
	Vector queue = messages[botNum];
	Message[] retVal = new Message[queue.size()];
	for (int i = 0; i < queue.size(); i++)
	  retVal[i] = (Message)queue.elementAt(i);
	// Empty queue
	queue = new Vector();
	return retVal;
  }  
  private static int getNumberParam (String s) {
	int val = 0;
	try {
	  val = Integer.parseInt(s);
	} catch (NumberFormatException e) {
	  System.out.println("Invalid number format: " + s + ".");
	  showUsage();
	}
	return val;
  }  
  /**
   * The command-line interface to the Arena. Here's the usage:
   * <P>
   * <CODE>java igx.bots.RobotArena <I>bot<B>1</B>[skill]</I> <I>bot<B>2</B>[skill]</I> ... </I>bot<B>n</B>[skill]</I>
   * -<I>option</I> -<I>option</I> ...</CODE>
   * <P>
   * Where <I>bot<B>i</B></I> is the <B>class name</B> of the 'bot who should be the <B>i</B>th player. Put
   * the skill level in square brackets after each 'bot class name. There can be no more than 9 robots in 
   * a game. For example, let's say the class name
   * of the class that extends {@link Bot Bot} is igx.bots.MoonBot. Then if I wanted the top 3 skill-level
   * MoonDroids to face off, here's the command line:
   * <P>
   * <CODE>java igx.bots.RobotArena igx.bots.MoonBot[8] igx.bots.MoonBot[9] igx.bots.MoonBot[10]</CODE>
   * <P>
   * By default, a game with a random number seed will be played for 1000 segments, then the winner
   * will be reported. The options can override this behaviour, and they are as follows:
   * <UL>
   *  <LI><B>-file <I>filename</I></B> - This opens the specified file and reads the contents as if it was
   *   the command line. You'll need to use this since Windows only allows a maximum of eight parameters on
   *   the command line. (Micky Mouse!) You can have linefeeds in the file, but make sure there are no
   *   unnecessary spaces at the end of any line.
   *  <LI><B>-seed <I>#</I></B> - This allows you to specify the random number seed. Very handy for debugging.
   *   If the option is not specified, the seed will be "randomly" selected.
   *  <LI><B>-games <I>#</I></B> - Specifies the number of games to play in sequence. The Arena will report
   *   basic statistics over all the games at the end.
   *  <LI><B>-time <I>#</I></B> - This specifies the number of segments to play. The default is 1000.
   *  <LI><B>-update <I>#</I></B> - This specifies how often you want the Arena to output the state of the
   *   galaxy. If you specify 0, then no reports will occur. Otherwise, every # segments you'll see a
   *   report.
   *  <LI><B>-report</B> - This will give a detailed report at the end of the game (similar to the updates).
   *  <LI><B>-events</B> - This will cause random events to be reported when they occur.
   *  <LI><B>-activity <I>n</I></B> - This reports the activities of the droids.
   *   <UL>
   *    <LI>n=0 is the default, and this reports none of their activity.
   *    <LI>n=1 reports all fleets and messages that the droids send.
   *    <LI>n=2 also reports invasions.
   *    <LI>n=3 also reports all attacking and repelled fleets.
   *    <LI>n=4 also reports all reinforcements.
   *   </UL>
   *  <LI><B>-stopwatch</B> - This will cause the arena to time the robots each segment and report how long
   *   they spent processing.
   *  <LI><B>-debug</B> - This will cause <B>your</B> debugging information to be reported. See the
   *   {@link Bot#debug Bot.debug} method for more information.
   * </UL>
   */
  public static void main (String[] args) {
	System.out.println("The intergalactics RobotArena: It's like the Turing Test... only better!");
	System.out.println("Version " + Constants.VERSION + ", HiVE Software.");
	System.out.println("");
	RobotArena arena = new RobotArena();
	int numArgs = args.length;
	if (numArgs == 0)
	  showUsage();
	for (int i = 0; i < numArgs; i++) {
	  String arg = args[i];
	  if (arg.charAt(0) == '-') {
	// Option
	arg = arg.substring(1);
	if (arg.equals("file")) {
	  if (i == numArgs-1)
	    complain("Parameter file not specified.");
	  else {
	    i++;
	    arg = args[i];
	    args = getFileArgs(arg);
	    numArgs = args.length;
	    i = -1;
	  }
	} else if (arg.equals("seed")) {
	  if (i == numArgs-1)
	    complain("Random seed not specified.");
	  else {
	    i++;
	    arg = args[i];
	    arena.seed = (long)getNumberParam(arg);
	  }
	} else if (arg.equals("games")) {
	  if (i == numArgs-1)
	    complain("Number of games not specified.");
	  else {
	    i++;
	    arg = args[i];
	    arena.numGames = getNumberParam(arg);
	    if (arena.numGames <= 0) {
	      System.out.println("Must play at least 1 game.");
	      showUsage();
	    }
	  }
	} else if (arg.equals("time")) {
	  if (i == numArgs-1) 
	    complain("Number of segments not specified.");
	  else {
	    i++;
	    arg = args[i];
	    arena.numSegments = getNumberParam(arg);
	    if (arena.numSegments < 0)
	      complain("Can't play negative time.");
	  }
	} else if (arg.equals("update")) {
	  if (i == numArgs-1)
	    complain("Update period not specified.");
	  else {
	    i++;
	    arg = args[i];
	    arena.updatePeriod = getNumberParam(arg);
	    if (arena.updatePeriod < 0) 
	      complain("Negative update period makes no sense.");
	  }
	} else if (arg.equals("activity")) {
	  if (i == numArgs-1)
	    complain("Activity level not specified.");
	  else {
	    i++;
	    arg = args[i];
	    arena.activityLevel = getNumberParam(arg);
	    if ((arena.activityLevel < 0) || (arena.activityLevel > 4)) 
	      complain("Activity level must be between 0 and 4");
	  }
	} else if (arg.equals("report")) {
	  arena.fullReport = true;
	} else if (arg.equals("events")) {
	  arena.reportEvents = true;
	} else if (arg.equals("debug")) {
	  arena.debugMode = true;
	} else if (arg.equals("stopwatch")) {
	  arena.timeRobots = true;
	} else 
	  complain("Invalid option: " + arg + ".");
	  } else {
	// robot class specification
	int leftBracket = arg.indexOf('[');
	int rightBracket = arg.indexOf(']');
	if ((leftBracket < 0) || (rightBracket < leftBracket)) 
	  complain("Robot improperly specified:" + arg + ".");
	String className = arg.substring(0, leftBracket);
	int skill = getNumberParam(arg.substring(leftBracket+1, rightBracket));
	if ((skill < 0) || (skill > 9))
	  complain("Skill levels must be between 0 and 9.");
	Class robotClass;
	Bot robot;
	try {
	  robotClass = Class.forName(className);
	  // Try to instantiate as a robot
	  robot = (Bot)robotClass.newInstance();
	  if (skill >= robot.numberOfBots())
	    complain("Skill for robot " + className + " must be between 0 and "
		     + (robot.numberOfBots() - 1) + ".");
	  arena.botClass[arena.numBots] = robotClass;
	  arena.skill[arena.numBots] = skill;
	  arena.numBots++;
	} catch (ClassNotFoundException e) {
	  complain("'bot class name: " + className + " not found.");
	} catch (ClassCastException e) {
	  complain("This... thing... is not a 'bot: " + className + "!");
	} catch (Exception e) {
	  complain("Problem with your 'bot, " + className + ". " + e + ".");
	}
	  }
	}
	if (arena.numBots == 0) 
	  complain("Get some 'bots in here!");
	else if (arena.numBots > Constants.MAXIMUM_PLAYERS)
	  complain("Too many 'bots for this arena! Maximum is " + Constants.MAXIMUM_PLAYERS + ".");
	arena.doArena();
  }  
  // Attack
  public void postAttack (igx.shared.Fleet fleet, igx.shared.Planet planet) {
	if (activityLevel >= 3) {
	  int botNum = fleet.owner.number;
	  robotReport(bot[botNum], " Attacks " + planet.planetChar + " with " + fleet.ships + " ships.");
	}
	arrivals.addElement(new ArrivedFleet(fleet.ships, Planet.planetNumber(planet.planetChar), fleet.owner.number));
  }  
  // Black hole event
  public void postBlackHole (igx.shared.Fleet fleet) {}  
  // Error
  public void postError (String errorMessage) {
	System.out.println("OUCH! Error in the game engine... report this to HiVE Software!");
  }  
  // Game end
  public void postGameEnd (int winnerNumber) {}  
  //// Post game events
  // Game start
  public void postGameStart (GameInstance game) {}  
  // Invasion
  public void postInvasion (igx.shared.Fleet fleet, igx.shared.Planet planet) {
	if (activityLevel >= 2) {
	  int botNum = fleet.owner.number;
	  robotReport(bot[botNum], " Invades " + planet.planetChar + ".");
	}
  }  
  // Players sends message
  public void postMessage (igx.shared.Player sender, igx.shared.Player recipient, String message) {}  
  // Next turn
  public void postNextTurn () {}  
  // Planet moves
  public void postPlanetMove (int oldX, int oldY, igx.shared.Planet planet) {}  
  // Player quits
  public void postPlayerQuit (igx.shared.Player player) {}  
  // Redraw galaxy
  public void postRedrawGalaxy () {}  
  // Reinforcements
  public void postReinforcements (int numberOfShips, igx.shared.Planet planet) {
	if (activityLevel >= 4) {
	  int botNum = planet.owner.number;
	  robotReport(bot[botNum], " " + numberOfShips + " reinforcements arrive at " + planet.planetChar + ".");
	}
	arrivals.addElement(new ArrivedFleet(numberOfShips, Planet.planetNumber(planet.planetChar), planet.owner.number));
  }  
  // Repulsion
  public void postRepulsion (igx.shared.Player attacker, igx.shared.Planet planet) {
	if (activityLevel >= 3) {
	  int botNum = attacker.number;
	  robotReport(bot[botNum], " Repelled from " + planet.planetChar + ".");
	}
  }  
// Special Event
public void postSpecial(String[] text, Color[] color) {
	if (reportEvents) {
		String output = "";
		for (int i = 0; i < text.length; i++)
			output += text[i];
		System.out.println(output);
	}
}
  // UI STUFF

  // Redraw all planets
  public void redrawAll () {}  
  // Redraw a planet
  public void redrawPlanet (int planetNum) {}  
  /**
   * Called when a robot is done processing.
   */
  public synchronized void robotDone (int botNum) {
	robotDone = true;
	notify();
  }  
  private void robotReport (Bot robot, String text) {
	System.out.println(robot.getBotName() + "(" + robot.getNumber() + "): " + text);
  }  
  /**
   * Called to send a fleet on behalf of a robot.
   */
  public synchronized void robotSendFleet (int botNum, Fleet fleet) {
	// Check for source ownership
	Planet source = game.getPlanet(fleet.source);
	if (botNum != source.getOwner()) {
	  if (debugMode)
	robotReport(bot[botNum], " Tried to send a fleet from a planet it didn't own: " + Planet.planetChar(fleet.source) + ".");
	  return;
	} else if (fleet.ships > gameInstance.planet[fleet.source].ships) {
	  if (debugMode)
	robotReport(bot[botNum], " Tried to send " + fleet.ships + " from planet " + Planet.planetChar(fleet.source) + ", where there are only " + gameInstance.planet[fleet.source].ships + " ships.");
	  fleet.ships = source.getShips();
	  return;
	}
	// Handle useless fleets
	if ((fleet.ships <= 0) || (fleet.source == fleet.destination))
	  return;
	igx.shared.Fleet gameFleet = new igx.shared.Fleet(gameInstance, 
							gameInstance.planet[fleet.source],
							gameInstance.planet[fleet.destination],
							fleet.ships);
	dispatches.addElement(gameFleet);
	if (activityLevel >= 1)
	  robotReport(bot[botNum], " Sent " + fleet.ships + " from " + Planet.planetChar(fleet.source) + " to " +
		  Planet.planetChar(fleet.destination) + ".");
  }  
  /**
   * Called to send a message on behalf of a robot.
   */
  public synchronized void robotSendMessage (int botNum, int recipient, String text) {
	Message message = new Message(botNum, recipient, text);
	if (recipient == Constants.MESSAGE_TO_ALL) 
	  for (int i = 0; i < numBots; i++)
	messages[i].addElement(message);
	else
	  messages[recipient].addElement(message);
	if (activityLevel >= 1) {
	  String output = "Message to ";
	  if (recipient == Constants.MESSAGE_TO_ALL)
	output += "ALL";
	  else
	output += bot[recipient].getBotName();
	  output += ": " + text;
	  robotReport(bot[botNum], output);
	}
  }  
  /**
   * Runs a game of igx for the robots. Returns a {@link Statistics Statistics} object
   * with info about how the 'bots did. This is called by {@link #main main}, but if you
   * want to use it (say for a genetic algorithm) then here's how:
   * <UL>
   *  <LI>Create an instance of {@link RobotArena RobotArena}.
   *  <LI>Set the parameters you want for the game by modifying the public variables of
   *   the RobotArena object. In particular, make sure that the random number
   *   {@link #seed seed} differs between games (if you want the games to differ).
   *  <LI>Call {@link #addRobot addRobot} for each 'bot you want in the game.
   *  <LI>You're ready to call <I>runGame</I>. May the best 'bot win!
   * </UL>
   *
   * @return a set of statistics giving the scores and average running times of the bots.
   */
  public Statistics runGame () {
	// Set up the random seedBo
	Bot.setRandomSeed(seed + 1);
	// Make some robots
	try {
	  for (int i = 0; i < numBots; i++) 
	bot[i] = (Bot)botClass[i].newInstance();
	} catch (Exception e) {
	  System.out.println("Hardcore error: " + e);
	  System.exit(1);
	}
	// Set up message repositories
	messages = new Vector[numBots];
	for (int i = 0; i < numBots; i++)
	  messages[i] = new Vector();
	// Fleet dispatching queue
	dispatches = new Vector();
	// Fleet arrivals queue
	arrivals = new Vector();
	// Initialize go-between messenger
	RobotMessenger rm = new RobotMessenger(this, numBots);
	// Generate player structures...
	igx.shared.Player player[] = new igx.shared.Player[numBots];
	for (int i = 0; i < numBots; i++) {
	  if (skill[i] > bot[i].numberOfBots())
	complain("Invalid skill number for bot " + i + ".");
	  player[i] = new igx.shared.Player(bot[i].createName(skill[i]), i);
	}
	// Generate game
	gameInstance = new GameInstance(seed, numBots, player);
	gameInstance.registerUI(this);
	// Init stats structure
	Statistics stats = new Statistics(numBots);
	// Initialize game structure for robots
	game = new GameState(gameInstance, null);
	GameState oldState = game;
	// Initialize robots with game
	for (int i = 0; i < numBots; i++)
	  bot[i].initializeBot(game, rm, i, skill[i], debugMode);
	// Let's play igx!
	for (int i = 0; i < numSegments; i++) {
	  // Display galaxy if required
	  if ((updatePeriod > 0) && ((i % updatePeriod) == 0))
	displayGalaxy(game);
	  for (int j = 0; j < numBots; j++) {
	// Call and time user's update code
	long startTime = System.currentTimeMillis();
	bot[j].updateBot(game, oldState, getMessages(j));
	synchronized(this) {
	  try {
	    while (!robotDone)
	      wait();
	  } catch (InterruptedException e) {
	    System.out.println("Hardcore error: " + e);
	    System.exit(1);
	  }
	}
	long totalTime = System.currentTimeMillis() - startTime;
	stats.reportTime(j, totalTime);
	if (timeRobots)
	  robotReport(bot[j], "Running Time: " + totalTime);
	robotDone = false;
	  }
	  gameInstance.update(dispatches);
	  oldState = game;
	  ArrivedFleet[] arrival = new ArrivedFleet[arrivals.size()];
	  for (int j = 0; j < arrivals.size(); j++) 
	arrival[j] = (ArrivedFleet)(arrivals.elementAt(j));
	  game = new GameState(gameInstance, arrival);
	  arrivals = new Vector();
	}
	int winner = 0;
	int bestScore = 0;
	for (int i = 0; i < numBots; i++) {
	  stats.gameOver(i, game.getPlayer(i).getScore(), numSegments);
	  if (game.getPlayer(i).getScore() > bestScore) {
	bestScore = game.getPlayer(i).getScore();
	winner = i;
	  }
	  bot[i].gameEnding();
	}
	stats.setWinner(winner);
	// End game report if needed
	if (fullReport) {
	  System.out.println("Game ends...");
	  displayGalaxy(game);
	  for (int i = 0; i < numBots; i++) 
	System.out.println(game.getPlayer(i).getName() + "(" + i + "): " + game.getPlayer(i).getScore());
	  System.out.println("----------------");
	  System.out.println("Winner: " + bot[winner].getBotName() + "(" + winner + ").");
	}
	return stats;
  }  
  /**
   * Shows how to use this damn thing.
   */
  protected static void showUsage () {
	System.out.println("Usage:");
	System.out.println("");
	System.out.println("java igx.bots.RobotArena bot1[skill] bot2[skill] ... botn[skill] -option -option ...");
	System.out.println("");
	System.out.println("The options are as follows:");
	System.out.println("-file filename - This opens the specified file and reads the contents as if it was");
	System.out.println("the command line. You'll need to use this since Windows only allows a maximum of eight parameters on");
	System.out.println("the command line. (Micky Mouse!) You can have linefeeds in the file, but make sure there are no");
	System.out.println("unnecessary spaces at the end of any line.");
	 System.out.println("-seed # - This allows you to specify the random number seed. Very handy for debugging. If the option is not specified, the seed will be \"randomly\" selected.");
	System.out.println("-games # - Specifies the number of games to play in sequence. The Arena will report basic statistics over all the games at the end.");
System.out.println("-time # - This specifies the number of segments to play. The default is 1000.");
	System.out.println("-update # - This specifies how often you want the Arena to output the state of the galaxy. If you specify 0, then no reports will occur. Otherwise, every # segments you'll see a report.");
	System.out.println("-report - This will give a detailed report at the end of the game (similar to the updates).");
	System.out.println("-events - This will cause random events to be reported when they occur.");
	System.out.println("-activity n - This reports the activities of the droids, where:");
	System.out.println(" n=0 is the default, and this reports none of their activity."); 
	System.out.println(" n=1 reports all fleets and messages that the droids send.");
	System.out.println(" n=2 also reports invasions.");
	System.out.println(" n=3 also reports all attacking and repelled fleets.");
	System.out.println(" n=4 also reports all reinforcements.");
	System.out.println("-stopwatch - This will cause the arena to time the robots each segment and report how long they spent processing.");
	System.out.println("-debug - This will cause your debugging information to be reported. See the Bot.Debug method for more information.");
	System.out.println("");
	System.out.println("See the javadocs for more details on using the Robot Arena.");
	System.exit(0);
  }  
}