package igx.server;

// ComputerPlayer.java

import igx.shared.*;
import java.util.*;

public class ComputerPlayer extends Player
{
  
  Vector messages = new Vector();

  public ComputerPlayer (String name) {
	super(name);
	isHuman = false;
  }  
  public void addMessage (int sender, String message) {
	messages.addElement(new ComputerMessage(sender, message));
  }  
  public ComputerMessage[] getMessages () {
	int numMessages = messages.size();
	ComputerMessage[] result = new ComputerMessage[numMessages];
	for (int i = 0; i < numMessages; i++) 
	  result[i] = (ComputerMessage)messages.elementAt(i);
	messages = new Vector();
	return result;
  }  
}