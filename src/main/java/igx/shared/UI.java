package igx.shared;

import java.awt.Color;

public abstract interface UI
{
  public abstract void postAttack(Fleet paramFleet, Planet paramPlanet);
  
  public abstract void postBlackHole(Fleet paramFleet);
  
  public abstract void postError(String paramString);
  
  public abstract void postGameEnd(int paramInt);
  
  public abstract void postGameStart(GameInstance paramGameInstance);
  
  public abstract void postInvasion(Fleet paramFleet, Planet paramPlanet);
  
  public abstract void postMessage(Player paramPlayer1, Player paramPlayer2, String paramString);
  
  public abstract void postNextTurn();
  
  public abstract void postPlanetMove(int paramInt1, int paramInt2, Planet paramPlanet);
  
  public abstract void postPlayerQuit(Player paramPlayer);
  
  public abstract void postRedrawGalaxy();
  
  public abstract void postReinforcements(int paramInt, Planet paramPlanet);
  
  public abstract void postRepulsion(Player paramPlayer, Planet paramPlanet);
  
  public abstract void postSpecial(String[] paramArrayOfString, Color[] paramArrayOfColor);
  
  public abstract void redrawAll();
  
  public abstract void redrawPlanet(int paramInt);
}