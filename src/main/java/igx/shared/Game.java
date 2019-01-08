package igx.shared;

// Game.java 

public class Game
{
  public String name = null;
  public String creator;
  public Player[] player = new Player[Params.MAXPLAYERS];
  public boolean[] activePlayer = new boolean[Params.MAXPLAYERS];
  public int numPlayers = 0;
  public boolean inProgress = false;
   public boolean randomMap = true;
  GameInstance game;
 
  public Game (String name, String creator) {
	this.name = name;
	this.creator = creator;
  }  
  public boolean addPlayer (Player player) {
	if ((numPlayers < Params.MAXPLAYERS) && !inProgress) {
	  this.player[numPlayers] = player;
	  player.game = this;
	  numPlayers++;
	  return true;
	} else
	  return false;
  }  
/**
 * This method was created in VisualAge.
 * @return boolean
 * @param name java.lang.String
 */
public boolean getActivePlayer (String name) {
	for (int i = 0; i < numPlayers; i++)
		if (player[i].name.equals(name) && player[i].isHuman)
			return activePlayer[i];
		else
			return false;
	return false;
}
  public Player getPlayer (String name) {
	for (int i = 0; i < numPlayers; i++)
	  if (player[i].name.equals(name))
	return player[i];
	return null;
  }  
  public boolean removePlayer (Player removal) {
	for (int i = 0; i < numPlayers; i++) 
	  if (removal == player[i]) {
	player[i].game = null;
	int j;
	for (j = i+1; j < numPlayers; j++)
	  player[j-1] = player[j];
	numPlayers--;
	return true;
	  }
	return false;
  }  
/**
 * This method was created in VisualAge.
 * @param name java.lang.String
 * @param active boolean
 */
public void setActivePlayer(String name, boolean active) {
	for (int i = 0; i < numPlayers; i++) 
		if (player[i].name.equals(name)) {
			activePlayer[i] = active;
			break;
		}
}
  public void setInstance (GameInstance g) {
	game = g;
  }  
}
