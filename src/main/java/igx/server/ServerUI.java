package igx.server;

// UI.java 
// Implementation of server UI... does nothing.

import igx.shared.*;
import java.awt.Color;

public class ServerUI implements UI
{
  // Player Arrival
  public void postArrival (String name)
  {}  
  // Attack
  public void postAttack (Fleet fleet, Planet planet)
  {}  
  // Black hole event
  public void postBlackHole (Fleet fleet)
  {}  
  // Error
  public void postError (String errorMessage)
  {}  
  // Game end
  public void postGameEnd (int winnerNumber)
  {}  
  //// Post game events
  // Game start
  public void postGameStart (GameInstance game)
  {}  
  // Invasion
  public void postInvasion (Fleet fleet, Planet planet)
  {}  
  // Players sends message
  public void postMessage (Player sender, Player recipient, String message)
  {}  
  // Next turn
  public void postNextTurn ()
  {}  
  public void postPlanetMove (int oldX, int oldY, Planet planet) 
  {}  
  // Player quits
  public void postPlayerQuit (Player player)
  {}  
  public void postRedrawGalaxy ()
  {}  
  // Reinforcements
  public void postReinforcements (int numberOfShips, Planet planet)
  {}  
  // Repulsion
  public void postRepulsion (Player player, Planet planet)
  {}  
  // Special Event
  public void postSpecial (String[] text, Color[] color)
  {}  
  // Redraw all planets
  public void redrawAll ()
  {}  
  // Redraw a planet
  public void redrawPlanet (int planetNum)
  {}  
}