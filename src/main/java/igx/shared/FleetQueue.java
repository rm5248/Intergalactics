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
    for (Fleet localFleet = first; localFleet != null; localFleet = localFleet.next) {
      localFleet.owner.addScore(localFleet.ships * 3);
    }
  }
  
  public void insert(Fleet paramFleet)
  {
    Fleet localFleet1 = first;
    Fleet localFleet2 = null;
    if (first == null)
    {
      first = paramFleet;
      paramFleet.next = null;
    }
    else
    {
      while ((localFleet1 != null) && (localFleet1.distance <= localFleet2.distance))
      {
        localFleet2 = localFleet1;
        localFleet1 = localFleet2.next;
      }
      if (localFleet1 == null)
      {
        localFleet1.next = null;
        if (localFleet2 != null) {
          localFleet1.next = paramFleet;
        } else {
          first = paramFleet;
        }
      }
      else
      {
        localFleet1.next = localFleet1;
        if (localFleet2 != null) {
          localFleet1.next = paramFleet;
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
      localFleet1 = localFleet2.next;
    }
    if (localFleet2 == null) {
      first = localFleet1.next;
    } else {
      localFleet1.next = localFleet2.next;
    }
  }
  
  public String toString()
  {
    Fleet localFleet = first;
    StringBuffer localStringBuffer = new StringBuffer("");
    while (localFleet != null)
    {
      localStringBuffer.append(localFleet.toString());
      localFleet = localFleet.next;
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
          first = localFleet1.next;
        } else {
          localFleet1.next = localFleet2.next;
        }
      }
      else
      {
        localFleet2 = localFleet1;
        if (localFleet1.distance <= 0.0F) {
            localFleet1.destination.attacker[localFleet1.owner.number] += localFleet1.ships;
        }
      }
      localFleet1 = localFleet1.next;
    }
  }
}