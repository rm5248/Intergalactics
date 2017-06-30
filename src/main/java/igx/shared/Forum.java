package igx.shared;

import java.util.Random;
import java.util.Vector;

public class Forum
  implements MessageListener
{
  protected Vector games = new Vector();
  protected Vector players = new Vector();
  protected Vector gamePool = new Vector();
  public Robot[] botList;
  
  public Forum(Robot[] paramArrayOfRobot)
  {
    botList = paramArrayOfRobot;
  }
  
  protected boolean abandonGame(String paramString)
  {
    Player localPlayer = getPlayer(paramString);
    Game localGame = localPlayer.game;
    if ((localGame != null) && (localPlayer != null) && (localGame.getPlayer(paramString) != null))
    {
      if (!localGame.inProgress) {
        localGame.removePlayer(localPlayer);
      }
      gamePool.removeElement(localPlayer);
      return true;
    }
    return false;
  }
  
  public boolean addCustomRobot(Robot paramRobot, String paramString)
  {
    Game localGame = getGame(paramString);
    if (localGame != null)
    {
      Player localPlayer = paramRobot.toPlayer();
      localPlayer.customRobot = true;
      localPlayer.r = paramRobot;
      if (localGame.getPlayer(localPlayer.name) == null) {
        return localGame.addPlayer(localPlayer);
      }
    }
    return false;
  }
  
  protected Player addPlayer(Player paramPlayer)
  {
    players.addElement(paramPlayer);
    return paramPlayer;
  }
  
  protected Player addPlayer(String paramString)
  {
    Player localPlayer = new Player(paramString);
    players.addElement(localPlayer);
    return localPlayer;
  }
  
  protected boolean addRobot(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString2);
    int i = botList.length;
    Random localRandom = new Random();
    Robot localRobot = getRobot(paramString1);
    if ((localRobot != null) && (localGame != null))
    {
      Player localPlayer = localRobot.toPlayer();
      if (localGame.getPlayer(paramString1) == null) {
        return localGame.addPlayer(localPlayer);
      }
    }
    return false;
  }
  
  protected boolean createGame(String paramString1, String paramString2)
  {
    if (games.size() == 8) {
      return false;
    }
    Player localPlayer = getPlayer(paramString1);
    if (localPlayer != null)
    {
      Game localGame = localPlayer.game;
      if (localGame == null)
      {
        games.addElement(new Game(paramString2, paramString1));
        return true;
      }
    }
    return false;
  }
  
  protected void gameOver(String paramString)
  {
    Game localGame = getGame(paramString);
    if (localGame != null)
    {
      games.removeElement(localGame);
      for (int i = 0; i < localGame.numPlayers; i++)
      {
        Player localPlayer = localGame.player[i];
        if ((localPlayer.isHuman) && (localGame.activePlayer[i] != false))
        {
          gamePool.removeElement(localPlayer);
          localPlayer.inGame = false;
          localPlayer.game = null;
        }
      }
    }
  }
  
  public Game getGame(String paramString)
  {
    int i = games.size();
    for (int j = 0; j < i; j++)
    {
      Game localGame = (Game)games.elementAt(j);
      if (localGame.name.equals(paramString)) {
        return localGame;
      }
    }
    return null;
  }
  
  public Player getPlayer(String paramString)
  {
    int i = players.size();
    for (int j = 0; j < i; j++)
    {
      Player localPlayer = (Player)players.elementAt(j);
      if (localPlayer.name.equals(paramString)) {
        return localPlayer;
      }
    }
    return null;
  }
  
  public Player getPoolPlayer(String paramString)
  {
    int i = gamePool.size();
    for (int j = 0; j < i; j++)
    {
      Player localPlayer = (Player)gamePool.elementAt(j);
      if (localPlayer.name.equals(paramString)) {
        return localPlayer;
      }
    }
    return null;
  }
  
  public Robot getRobot(String paramString)
  {
    int i = botList.length;
    for (int j = 0; j < i; j++) {
      if (botList[j].name.equals(paramString)) {
        return botList[j];
      }
    }
    return null;
  }
  
  protected boolean joinGame(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString2);
    Player localPlayer = getPlayer(paramString1);
    if ((localGame != null) && (!localGame.inProgress) && (localPlayer != null) && (localGame.numPlayers < 9) && (localGame.getPlayer(paramString1) == null))
    {
      localGame.addPlayer(localPlayer);
      return true;
    }
    return false;
  }
  
  protected void message(String paramString1, String paramString2, int paramInt) {}
  
  public void messageEvent(Message paramMessage)
  {
    int i = paramMessage.getType();
    switch (i)
    {
    case 1: 
      addPlayer(paramMessage.getPlayerName());
      break;
    case 2: 
    case 12: 
      removePlayer(paramMessage.getPlayerName());
      break;
    case 3: 
      createGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      joinGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case 4: 
      joinGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case 5: 
      abandonGame(paramMessage.getPlayerName());
      break;
    case 6: 
      watchGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case 9: 
      startGame(paramMessage.getGameName(), paramMessage.getCustomMap());
      break;
    case 13: 
      customMap(paramMessage.getGameName());
      break;
    case 7: 
      addRobot(paramMessage.getRobotName(), paramMessage.getGameName());
      break;
    case 8: 
      removeRobot(paramMessage.getRobotName(), paramMessage.getGameName());
      break;
    case 0: 
      message(paramMessage.getPlayerName(), paramMessage.getMessageText(), paramMessage.getDestination());
      break;
    case 10: 
      gameOver(paramMessage.getGameName());
    }
  }
  
  protected void removePlayer(String paramString)
  {
    Player localPlayer = getPlayer(paramString);
    if (localPlayer != null) {
      players.removeElement(localPlayer);
    }
  }
  
  protected boolean removeRobot(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString2);
    Robot localRobot = getRobot(paramString1);
    if ((localRobot != null) && (localGame != null))
    {
      Player localPlayer = localGame.getPlayer(paramString1);
      if (localPlayer != null)
      {
        localGame.removePlayer(localPlayer);
        return true;
      }
    }
    return false;
  }
  
  protected void startGame(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString1);
    int i = localGame.numPlayers;
    for (int j = 0; j < i; j++) {
      if (localGame.player[j].isHuman)
      {
        localGame.player[j].status = 2;
        gamePool.addElement(localGame.player[j]);
        localGame.setActivePlayer(localGame.player[j].name, true);
      }
    }
    if (localGame != null) {
      localGame.inProgress = true;
    }
  }
  
  protected void customMap(String paramString)
  {
    Game localGame = getGame(paramString);
    if (localGame != null) {
      localGame.randomMap = (!localGame.randomMap);
    }
  }
  
  protected void watchGame(String paramString1, String paramString2)
  {
    Player localPlayer = getPlayer(paramString1);
    Game localGame = getGame(paramString2);
    if ((localPlayer != null) && (localGame != null)) {
      localPlayer.game = localGame;
    }
  }
}