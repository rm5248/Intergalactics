package igx.server;

// NewGame.java
// NewGame class - Holds preliminary information for games before they start CLIENT VERSION

// Imports

import igx.shared.*;
import java.util.*;

public class NewGame {
  public String name;
  public Vector clients = new Vector();
  public Vector computerPlayers = new Vector();
  public int currentPlayers;
  public Client creator;

  public NewGame (Client creator, String name)
  {
	this.creator = creator;
	this.name = name;
	currentPlayers = 0;
  }  
  public void addComputerPlayer (String name) {
	Player player = new ComputerPlayer(name);
	computerPlayers.addElement(player);
	currentPlayers++;
  }  
  public boolean addPlayer (Client client)
  {
	if (clients.size() == Params.MAXPLAYERS)
	  return false;
	clients.addElement(client);
	currentPlayers++;
	return true;
  }  
  public boolean inGame (String name) {
	for (int i = 0; i < clients.size(); i++)
	  if (name.equals(((Client)clients.elementAt(i)).me.name))
	return true;
	for (int i = 0; i < computerPlayers.size(); i++)
	  if (name.equals(((Player)computerPlayers.elementAt(i)).name))
	return true;
	return false;
  }  
  public void removeAll () {
	for (int i = 0; i < clients.size(); i++) {
	  Client quitter = (Client)clients.elementAt(i);
	  quitter.selectedGame = null;
	}
	currentPlayers = 0;
  }  
  public boolean removePlayer (int playerNum)
  {
	Client quitter = (Client)clients.elementAt(playerNum);
	clients.removeElementAt(playerNum);
	currentPlayers--;
	if (quitter == creator)
	  return true;
	else
	  return false;
  }  
  public boolean removePlayer (Client player)
  {
	currentPlayers--;
	return removePlayer(clients.indexOf(player));
  }  
}