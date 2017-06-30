package igx.shared;

import java.util.Vector;

public class Fleet
{
  public Player owner;
  public int ships;
  public int ratio;
  public Planet destination;
  public Planet source;
  public float distance;
  public Fleet next;
  public GameInstance Game;
  
  public Fleet(Fleet paramFleet)
  {
      this.Game = paramFleet.Game;
      this.owner = paramFleet.owner;
      this.ships = paramFleet.ships;
      this.ratio = paramFleet.ratio;
      this.destination = paramFleet.destination;
      this.distance = paramFleet.distance;
      this.next = paramFleet.next;
  }
  
  public Fleet(GameInstance paramGameInstance, Planet paramPlanet1, Planet paramPlanet2, int paramInt)
  {
    Game = paramGameInstance;
    source = paramPlanet1;
    owner = owner;
    ships = paramInt;
    ships -= paramInt;
    paramGameInstance.dirty[ Planet.char2num( paramPlanet1.planetChar ) ] = true;
    //dirty[Planet.char2num(planetChar)] = true;
    ratio = ratio;
    destination = paramPlanet2;
    int x1 = paramPlanet1.x;
    int x2 = paramPlanet2.x;
    int y1 = paramPlanet1.y;
    int y2 = paramPlanet2.y;
    distance = new Double(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))).floatValue();
    next = null;
  }
  
  public Fleet(GameInstance paramGameInstance, Planet paramPlanet1, Planet paramPlanet2, Player paramPlayer, int paramInt1, int paramInt2, float paramFloat)
  {
    destination = paramPlanet1;
    owner = paramPlayer;
    ships = paramInt1;
    ratio = paramInt2;
    distance = paramFloat;
    Game = paramGameInstance;
  }
  
  public String toString()
  {
    return "(" + owner.name + ", S: " + ships + ", %: " + ratio + ", P: " + destination.planetChar + ", D: " + distance + ")";
  }
  
  boolean update()
  {
    char[] arrayOfChar = { destination.planetChar };
    String str = new String(arrayOfChar);
    distance -= 0.100001F;
    if ((distance <= 0.0F) || (Game.resolveAllConflicts))
    {
      if (destination.blackHole)
      {
        Game.ui.postBlackHole(this);
        return true;
      }
      Game.dirty[Planet.char2num(destination.planetChar)] = true;
      if (owner == destination.owner)
      {
        Game.fleetArrived(this);
        if (distance + 0.100001F > 0.0F) {
          Game.ui.postReinforcements(ships, destination);
        }
        destination.ships += ships;
        return true;
      }
      if (distance + 0.100001F > 0.0F)
      {
        Game.ui.postAttack(this, destination);
        Game.fleetArrived(this);
      }
      int i = ships;
      for (int j = 0; j < i; j++) {
        if ((Game.pseudo(0, 99) < 25) || (destination.ships == 0))
        {
          if (Game.pseudo(0, 99) < ratio)
          {
            if (--destination.ships <= 0) {}
          }
          else {
            if (destination.ships != 0) {
              continue;
            }
          }
          Game.ui.postInvasion(this, destination);
          destination.ships = ships;
          destination.owner = owner;
          return true;
        }
      }
      destination.attackers.addElement(this);
      return false;
    }
    return false;
  }
}