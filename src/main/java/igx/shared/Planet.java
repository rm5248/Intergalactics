package igx.shared;

import java.util.Vector;

public class Planet
{
  public static final int NO_ATTACKERS = -1;
  public static final int MULTIPLE_ATTACKERS = -2;
  public int x;
  public int y;
  public Player owner = Player.NEUTRAL;
  public int production;
  public int ratio;
  public int ships;
  public int defenceRatio;
  public int prodTurns = 20;
  public Vector attackers = new Vector();
  public int[] attacker = new int[9];
  public int attackingPlayer = -1;
  public int totalAttackingShips = 0;
  public GameInstance Game;
  public char planetChar;
  public int planetSize;
  public int planetShade;
  public boolean blackHole = false;
  
  public Planet(int paramInt, GameInstance paramGameInstance)
  {
    Game = paramGameInstance;
    planetChar = num2char(paramInt);
    planetShade = paramGameInstance.pseudo(24, 48);
    planetSize = paramGameInstance.pseudo(25, 90);
  }
  
  public Planet(GameInstance paramGameInstance, int paramInt)
  {
    Game = paramGameInstance;
    ships = paramGameInstance.pseudo(1, 10);
    production = paramGameInstance.pseudo(0, 10);
    ratio = paramGameInstance.pseudo(20, 49);
    defenceRatio = (ratio + paramGameInstance.pseudo(0, 30));
    planetChar = num2char(paramInt);
    planetShade = paramGameInstance.pseudo(24, 48);
    planetSize = paramGameInstance.pseudo(25, 90);
  }
  
  public static int char2num(char paramChar)
  {
    if (Character.isDigit(paramChar)) {
      return Character.digit(paramChar, 10) + 26;
    }
    return Character.digit(paramChar, 36) - 10;
  }
  
  public static char num2char(int paramInt)
  {
    if (paramInt > 36) {
      return '\000';
    }
    if (paramInt < 26) {
      return (char)(65 + paramInt);
    }
    return (char)(48 + paramInt - 26);
  }
  
  public int update()
  {
    if (!attackers.isEmpty())
    {
      Game.dirty[char2num(planetChar)] = true;
      int totalShips = 0;
      for (int j = 0; j < attackers.size(); j++) {
        totalShips += ((Fleet)(attackers.elementAt(j))).ships;
      }
      for (int k = 0; k < totalShips; k++) {
        if ((Game.pseudo(0, 99) < 25) && (Game.pseudo(0, 99) < defenceRatio) && (totalShips > 0))
        {
          int m = 0;
          int n = Game.pseudo(0, totalShips - 1);
          //((Fleet)(attackers.elementAt(k))).ships -= shipsToSub;
          while ((n -= ((Fleet)(attackers.elementAt(m))).ships) >= 0) {
            m++;
          }
          Fleet localFleet = (Fleet)attackers.elementAt(m);
          totalShips--;
          attacker[owner.number] -= 1;
          if (--ships == 0)
          {
            Game.ui.postRepulsion(owner, this);
            Game.fleets.remove(localFleet);
            attackers.removeElementAt(m);
          }
        }
      }
    }
    attackingPlayer = -1;
    totalAttackingShips = 0;
    for (int i = 0; i < Game.players; i++) {
      if (attacker[i] != 0)
      {
        if (attackingPlayer == -1) {
          attackingPlayer = i;
        } else {
          attackingPlayer = -2;
        }
        totalAttackingShips += attacker[i];
      }
    }
    attackers = new Vector();
    if (owner != Player.NEUTRAL)
    {
      prodTurns -= 1;
      if (prodTurns == 0)
      {
        Game.dirty[char2num(planetChar)] = true;
        prodTurns = 20;
        ships += production;
        defenceRatio = (ratio + Game.pseudo(0, 30));
      }
    }
    return ships;
  }
}