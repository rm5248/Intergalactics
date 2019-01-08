package igx.bots;

// Bot.java

import java.util.*;

/**
 * The class containing the guts of an intergalactics-playing robot. Extend this class by extending
 * {@link #update update} and {@link #createName createName} and you've got yourself a 'bot. You can also
 * override {@link #newGame newGame} and {@link #endGame endGame} if you feel the need.
 *
 * @author John Watkinson
 */
public class Bot extends Thread
{
  /**
   * This 'bot's player number.
   */
  private int me;

  private int skillLevel;

  private String name;

  private class Update
  {
	public GameState game, oldState;

	public Message[] message;

	public Update (GameState game, GameState oldState, Message[] message) {
	  this.game = game;
	  this.oldState = oldState;
	  this.message = message;
	}
  }

  private Vector updates = new Vector();

  /**
   * The flag indicating the game is over.
   */
  private boolean gameOver = false;

  private boolean debug;

  private RobotMessenger rm;

  private static Random generator = new Random();

  /**
   * Constructor.
   */
  public Bot () {
	super();
  }
  /**
   * This is called by the server to determine the name of your 'bot at some skill level. Your 'bot
   * needs to a return a unique name for each skill level (a value between 1 and 10). Override this,
   * or your 'bots will have some pretty hokey names.
   */
  public String createName (int skillLevel) {
	switch (skillLevel) {
	case 1:
	  return "Potluck";
	case 2:
	  return "Whim";
	case 3:
	  return "Boatbot";
	case 4:
	  return "Digi";
	case 5:
	  return "Olga";
	case 6:
	  return "Niner";
	case 7:
	  return "Dispatch";
	case 8:
	  return "Dr. Droid";
	case 9:
	  return "Sir Kit";
	default:
	  return "Enemy";
	}
  }
  /**
   * Call this to spit out any debug info you may need. If you run the {@link RobotArena RobotArena} with
   * debug mode on, you'll see this info associated with the 'bot number that produced it. Otherwise, it
   * won't do anything. However, in the final version of your 'bot, don't call this excessively.
   * Even with debug info off, it will still take up a small
   * amount of time. We don't get <CODE>#ifdef</CODE>s like in C.
   */
  public final void debug (String text) {
	if (!debug)
	  return;
	rm.debug(this, text);
  }
  /**
   * Called when the game ends. Your 'bot can have it's last "hurrah" before it's unplugged.
   * Override this if you'd like. Sending fleets or messages will have no effect, though.
   */
  public void endGame () {}
  /**
   * Called by server to end game.
   */
  public final synchronized void gameEnding () {
	gameOver = true;
	updates.addElement(new Update(null, null, null));
    System.out.println("Bot " + getName() + " shutting down!");
	notify();
  }
  private final synchronized void gameLoop () {
	while (!gameOver) {
	  while (updates.size() == 0) {
	try {
	  wait();
	} catch (InterruptedException e) {}
	  }
	  Update ud = (Update)(updates.elementAt(0));
	  updates.removeElementAt(0);
	  if (gameOver) {
	endGame();
	rm.quit();
	rm = null; // Aids in garbage collection.
	  } else {
	update(ud.game, ud.oldState, ud.message);
	// Send the 'DONE' message to robot server
	rm.sendCommand(new Command(this));
	  }
	}
  }
  /**
   * Returns the name of this player... generated using {@link #createName createName}.
   */
  public String getBotName () {
	return name;
  }
  /**
   * Returns this 'bot's player number.
   */
  public final int getNumber () {
	return me;
  }
  /**
   * The skill level of this 'bot. This will be between 1 and 10. You can interpret this
   * parameter any way you like, but our canonical interpretation is this: A Skill 1
   * 'bot doesn't know its CPU from its motivator chip, while a Skill 10 robot is pretty
   * much R2D2... except this one <I>won't</I> let the wookie win.
   */
  public final int getSkillLevel () {
	return skillLevel;
  }
  /**
   * Called by the server to initialize the 'bot for activity.
   */
  public final void initializeBot (GameState game, RobotMessenger rm, int me, int skillLevel, boolean debug) {
	this.me = me;
	this.rm = rm;
	this.skillLevel = skillLevel;
	this.debug = debug;
	this.name = createName(skillLevel);
	setName("Bot: " + name);
	newGame(game, skillLevel);
	start();
  }
  /**
   * Called when a game begins. Your 'bot should override this method to do any preliminary setup
   * your 'bot may need.
   *
   * @param game Contains the initial game state.
   * @param skillLevel The skill level of this 'bot. See {@link #getSkillLevel getSkillLevel} for
   *  more info.
   */
  public void newGame (GameState game, int skillLevel) {}
  /**
   * This is called by the server to determine how many different 'bot skill levels you've implemented.
   * The maximum is 10, the minimum is 1. Override this to indicate how many different 'bots you want
   * generated from this code. For example, if you return 4, then the server will only create 'bots with
   * skill levels ranging from 1 to 4.
   */
  public int numberOfBots () {
	return 10;
  }
  /**
   * Generates a uniformly distributed pseudo-random number between min and max.
   * Handy if your robot uses randomization.
   */
  public static synchronized int random (int min, int max)
  {
	int value = generator.nextInt();
	return value<0 ? (-value) % (max - min + 1) + min : value % (max - min + 1) + min;
  }
  /**
   * Begins the 'bot's thread.
   */
  public final void run () {
	gameLoop();
  }
  /**
   * Call this to send multiple fleets at once. If your 'bot is sending lots of fleets, this can
   * speed things up tremendously.
   *
   * @param fleet An array of the fleets the 'bot wants sent.
   */
  public void sendBatchFleets (Fleet[] fleet) {
	Command[] command = new Command[fleet.length];
	for (int i = 0; i < fleet.length; i++)
	  command[i] = new Command(this, fleet[i]);
	rm.sendCommands(command);
  }
  /**
   * Call this to send a fleet. The server will filter the fleet as follows:
   * <UL>
   *  <LI>If this 'bot doesn't own the planet from which it's trying to send, then the server drops the
   *   fleet.
   *  <LI>If the 'bot tries to send more ships than there are on the planet, then the server will send
   *   all the ships on the planet.
   * </UL>
   * <B>NOTE:</B> When you send a fleet, the ships do not "disappear" from the planet in your GameState
   * until the next segment.
   *
   * @param from The number of the planet from which the ships should be sent
   * @param to The number of the destination planet.
   * @param ships The number of ships to send.
   */
  public void sendFleet (int from, int to, int ships) {
	sendFleet(new Fleet(from, to, ships));
  }
  /**
   * Call this to send a fleet. The server will filter the fleet as follows:
   * <UL>
   *  <LI>If this 'bot doesn't own the planet from which it's trying to send, then the server drops the
   *   fleet.
   *  <LI>If the 'bot tries to send more ships than there are on the planet, then the server will send
   *   all the ships on the planet.
   * </UL>
   * <B>NOTE:</B> When you send a fleet, the ships do not "disappear" from the planet in your GameState
   * until the next segment.
   *
   * @param fleet The fleet the 'bot wants sent.
   */
  public void sendFleet (Fleet fleet) {
	rm.sendCommand(new Command(this, fleet));
  }
  /**
   * Call this to send a message.
   *
   * @param recipient The player number of the recipient. Use Constants.MESSAGE_TO_ALL
   * to send to all players.
   * @param message The text of the message.
   */
  public void sendMessage (int recipient, String message) {
	rm.sendCommand(new Command(this, recipient, message));
  }
  /**
   * Called to set the random seed to something. Used by the {@link RobotArena RobotArena} to
   * to set the seed for debugging purposes.
   */
  public static synchronized void setRandomSeed (long seed) {
	generator = new Random(seed);
  }
  /**
   * Returns a string representation of this 'Bot.
   */
  public String toString () {
	return getBotName() + "(" + getNumber() + ")";
  }
  /**
   * Called every time the server sends an update. This is the <I>brain</I> of your 'bot. Extend
   * this method to make your 'bot do something other than look pretty.
   *
   * @param game Contains the current game state. Everything that a human player could possibly
   * know is here... however, a human can infer a lot of things, but you'll have to code that up
   * yourself.
   * @param oldState This is the game state from the last segment. Very useful for computing deltas
   * on planets, etc.
   * @param message This contains all the messages that this particular 'bot can read. That includes
   * messages addressed to all players and messages strictly to the 'bot. The sender and recipient(s)
   * are available. I'm not sure what you can do with this info, but it's here for purity.
   */
  public void update (GameState game, GameState oldState, Message[] message) {}
  /**
   * Called by server to update the game state.
   */
  public final synchronized void updateBot (GameState game, GameState oldState, Message[] message) {
	updates.addElement(new Update(game, oldState, message));
	notify();
  }
}