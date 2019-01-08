// HelloWorldBot.java 

// This is the "Hello, world" robot. It doesn't do anything but send a "Hello, world!" message
// to all players on the first turn. This class is useful to see how to get started on your 
// own 'bot.

// Once you understand this 'bot, check out the MoonBot

// Import the classes from package igx.bots.
import igx.bots.*;

// Declare this class as extending Bot
public class HelloWorldBot extends Bot
{
  
  // Override update method to say "Hello, world!"
  public void update (GameState game,
		     GameState oldState,
		     Message[] message) {
    // If it's first turn, 0th segment, then say something...
    if ((game.getTurn() == 1) && (game.getSegment() == 0))
      sendMessage(Constants.MESSAGE_TO_ALL, "Hello, world!");
  }
}
