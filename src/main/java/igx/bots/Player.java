package igx.bots;

// Player.java 

/**
 * Contains everything a robot needs to know about a player.
 *
 * @author John Watkinson
 */
public class Player
{
  /**
   * Player's name.
   */
  private String name;

  /**
   * The current score of the player.
   */
  private int score;

  /**
   * True if this player has quit playing.
   */
  private boolean quit = false;

  /**
   * The number of the neutral player.
   */
  public final static int NEUTRAL = Constants.NEUTRAL;

  /**
   * Constructor for a player.
   */
  public Player (String name, int score, boolean quit) {
	this.name = name;
	this.score = score;
	this.quit = quit;
  }  
  /**
   * Returns the player's name.
   */
  public String getName () {
	return name;
  }  
  /**
   * Returns the current score of the player.
   */
  public int getScore () {
	return score;
  }  
  /**
   * Returns true if this player has quit.
   */
  public boolean hasQuit () {
	return quit;
  }  
  /**
   * Returns a string representation of this Player.
   */
  public String toString () {
	String s = "Name: " + name + " Score: " + score;
	if (quit)
	  s+= " (quit)";
	return s;
  }  
}