package igx.bots;

// Message.java 

/**
 * This class contains the information associated with an incoming message.
 *
 * @author John Watkinson
 */
public class Message
{
  /**
   * The player number of the sender.
   */
  private int from;

  /**
   * The player number of the recipient. This will be Constants.MESSAGE_TO_ALL if the
   * message was sent to all players.
   */
  private int to;

  /**
   * The text of the message.
   */
  private String text;

  /**
   * Constructor for a new message.
   */
  public Message (int from, int to, String text) {
	this.from = from;
	this.to = to;
	this.text = text;
  }  
  /**
   * Returns the player number of the message recipient. This will be Constants.MESSAGE_TO_ALL if the
   * message was sent to all players.
   */
  public int getReceiverNumber () {
	return to;
  }  
  /**
   * Returns the player number who sent the message.
   */
  public int getSenderNumber () {
	return from;
  }  
  /**
   * Returns the text of the message.
   */
  public String getText () {
	return text;
  }  
  /**
   * Returns a string representation of this message.
   */
  public String toString () {
	return "(" + from + "->" + to + text;
  }  
}