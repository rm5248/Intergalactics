package igx.bots;

// RobotServer.java 

/**
 * This interface is implemented by a class which wants to receive events from a 'bot.
 * In particular, the RobotArena is a RobotServer, as is the main igx server.
 *
 * @author John Watkinson
 */
public interface RobotServer
{
  /**
   * Called when a robot is done processing.
   */
  public void robotDone (int botNum);  
  /**
   * Called to send a fleet on behalf of a robot.
   */
  public void robotSendFleet (int botNum, Fleet fleet);  
  /**
   * Called to send a message on behalf of a robot.
   */
  public void robotSendMessage (int botNum, int recipient, String text);  
}