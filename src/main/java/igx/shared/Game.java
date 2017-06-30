package igx.shared;

public class Game
{
  public String name = null;
  public String creator;
  public Player[] player = new Player[9];
  public boolean[] activePlayer = new boolean[9];
  public int numPlayers = 0;
  public boolean inProgress = false;
  public boolean randomMap = true;
  GameInstance game;
  
  public Game(String paramString1, String paramString2)
  {
    name = paramString1;
    creator = paramString2;
  }
  
  public boolean addPlayer(Player paramPlayer)
  {
    if ((numPlayers < 9) && (!inProgress))
    {
      player[numPlayers] = paramPlayer;
      paramPlayer.game = this;
      numPlayers += 1;
      return true;
    }
    return false;
  }
  
  public boolean getActivePlayer(String paramString)
  {
    int i = 0;
    if (i < numPlayers)
    {
      if ((player[i].name.equals(paramString)) && (player[i].isHuman)) {
        return activePlayer[i];
      }
      return false;
    }
    return false;
  }
  
  public Player getPlayer(String paramString)
  {
    for (int i = 0; i < numPlayers; i++) {
      if (player[i].name.equals(paramString)) {
        return player[i];
      }
    }
    return null;
  }
  
  public boolean removePlayer(Player paramPlayer)
  {
    for (int i = 0; i < numPlayers; i++) {
      if (paramPlayer == player[i])
      {
        player[i].game = null;
        for (int j = i + 1; j < numPlayers; j++) {
          player[(j - 1)] = player[j];
        }
        numPlayers -= 1;
        return true;
      }
    }
    return false;
  }
  
  public void setActivePlayer(String paramString, boolean paramBoolean)
  {
    for (int i = 0; i < numPlayers; i++) {
      if (player[i].name.equals(paramString))
      {
        activePlayer[i] = paramBoolean;
        break;
      }
    }
  }
  
  public void setInstance(GameInstance paramGameInstance)
  {
    game = paramGameInstance;
  }
}