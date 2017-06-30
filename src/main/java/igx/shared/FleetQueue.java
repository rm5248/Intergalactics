package igx.shared;

public class FleetQueue
{
  public Fleet first = null;
  public GameInstance Game;
  
  public FleetQueue(GameInstance paramGameInstance)
  {
    Game = paramGameInstance;
  }
  
  public void doScores()
  {
    for (Fleet localFleet = first; localFleet != null; localFleet = next) {
      owner.addScore(ships * 3);
    }
  }
  
  public void insert(Fleet paramFleet)
  {
    Fleet localFleet1 = first;
    Fleet localFleet2 = null;
    if (first == null)
    {
      first = paramFleet;
      next = null;
    }
    else
    {
      while ((localFleet1 != null) && (distance <= distance))
      {
        localFleet2 = localFleet1;
        localFleet1 = next;
      }
      if (localFleet1 == null)
      {
        next = null;
        if (localFleet2 != null) {
          next = paramFleet;
        } else {
          first = paramFleet;
        }
      }
      else
      {
        next = localFleet1;
        if (localFleet2 != null) {
          next = paramFleet;
        } else {
          first = paramFleet;
        }
      }
    }
  }
  
  public void remove(Fleet paramFleet)
  {
    Fleet localFleet1 = first;
    Fleet localFleet2 = null;
    while (localFleet1 != paramFleet)
    {
      localFleet2 = localFleet1;
      localFleet1 = next;
    }
    if (localFleet2 == null) {
      first = next;
    } else {
      next = next;
    }
  }
  
  public String toString()
  {
    Fleet localFleet = first;
    StringBuffer localStringBuffer = new StringBuffer("");
    while (localFleet != null)
    {
      localStringBuffer.append(localFleet.toString());
      localFleet = next;
      localStringBuffer.append("\n");
    }
    return localStringBuffer.toString();
  }
  
  public void update()
  {
    Fleet localFleet1 = first;
    Fleet localFleet2 = null;
    for (int i = 0; i < 36; i++) {
      Game.planet[i].attacker = new int[9];
    }
    while (localFleet1 != null)
    {
      if (localFleet1.update())
      {
        if (localFleet2 == null) {
          first = next;
        } else {
          next = next;
        }
      }
      else
      {
        localFleet2 = localFleet1;
        if (distance <= 0.0F) {
          destination.attacker[owner.number] += ships;
        }
      }
      localFleet1 = next;
    }
  }
}