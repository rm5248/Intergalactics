package igx.shared;

import java.util.Random;
import java.util.Vector;

/**
 * A forum is the main interface that people interact with.
 * 
 * A forum will have :
 * * Games that people can join / watch
 * * A list of players
 * * A pool of games
 * * A list of bots
 */
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
  
  protected boolean abandonGame(String playerName)
  {
    Player localPlayer = getPlayer(playerName);
    Game localGame = localPlayer.game;
    if ((localGame != null) && (localPlayer != null) && (localGame.getPlayer(playerName) != null))
    {
      if (!localGame.inProgress) {
        localGame.removePlayer(localPlayer);
      }
      gamePool.removeElement(localPlayer);
      return true;
    }
    return false;
  }
  
  /**
   * Add a robot to the specified game.
   * 
   * @param paramRobot The robot to add to the game.
   * @param gameName The name of the game to add the robot to
   * @return true if added, false otherwise
   */
  public boolean addCustomRobot(Robot paramRobot, String gameName)
  {
    Game localGame = getGame(gameName);
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
  
  /**
   * Add an already-existing player.
   * 
   * @param paramPlayer
   * @return 
   */
  protected Player addPlayer(Player paramPlayer)
  {
    players.addElement(paramPlayer);
    return paramPlayer;
  }
  
  /**
   * Add a player based only on the username.
   * 
   * @param paramString
   * @return 
   */
  protected Player addPlayer(String paramString)
  {
    Player localPlayer = new Player(paramString);
    players.addElement(localPlayer);
    return localPlayer;
  }
  
  /**
   * Add the robot with the spcified name to the specified game.
   * 
   * @param robotName
   * @param gameName
   * @return 
   */
  protected boolean addRobot(String robotName, String gameName)
  {
    Game localGame = getGame(gameName);
    int i = botList.length;
    Random localRandom = new Random();
    Robot localRobot = getRobot(robotName);
    if ((localRobot != null) && (localGame != null))
    {
      Player localPlayer = localRobot.toPlayer();
      if (localGame.getPlayer(robotName) == null) {
        return localGame.addPlayer(localPlayer);
      }
    }
    return false;
  }
  
  /**
   * Create a game
   * @param playerName
   * @param gameName
   * @return 
   */
  protected boolean createGame(String playerName, String gameName)
  {
    if (games.size() == 8) {
      return false;
    }
    Player localPlayer = getPlayer(playerName);
    if (localPlayer != null)
    {
      Game localGame = localPlayer.game;
      if (localGame == null)
      {
        games.addElement(new Game(gameName, playerName));
        return true;
      }
    }
    return false;
  }
  
  protected void gameOver(String gameName)
  {
    Game localGame = getGame(gameName);
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
  
  /**
   * Get a game with the specified name.
   * 
   * @param gameName
   * @return 
   */
  public Game getGame(String gameName)
  {
    int i = games.size();
    for (int j = 0; j < i; j++)
    {
      Game localGame = (Game)games.elementAt(j);
      if (localGame.name.equals(gameName)) {
        return localGame;
      }
    }
    return null;
  }
  
  /**
   * Get a player with the specified name.
   * 
   * @param playerName
   * @return 
   */
  public Player getPlayer(String playerName)
  {
    int i = players.size();
    for (int j = 0; j < i; j++)
    {
      Player localPlayer = (Player)players.elementAt(j);
      if (localPlayer.name.equals(playerName)) {
        return localPlayer;
      }
    }
    return null;
  }
  
  public Player getPoolPlayer(String playerName)
  {
    int i = gamePool.size();
    for (int j = 0; j < i; j++)
    {
      Player localPlayer = (Player)gamePool.elementAt(j);
      if (localPlayer.name.equals(playerName)) {
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
  
  protected boolean joinGame(String playerName, String gameName)
  {
    Game localGame = getGame(gameName);
    Player localPlayer = getPlayer(playerName);
    if ((localGame != null) && (!localGame.inProgress) && (localPlayer != null) && (localGame.numPlayers < 9) && (localGame.getPlayer(playerName) == null))
    {
      localGame.addPlayer(localPlayer);
      return true;
    }
    return false;
  }
  
  protected void message(String playerName, String messageText, int paramInt) {}
  
  public void messageEvent(Message paramMessage)
  {
    int i = paramMessage.getType();
    switch (i)
    {
    case Message.PLAYER_ARRIVED: 
      addPlayer(paramMessage.getPlayerName());
      break;
    case Message.PLAYER_LEFT: 
    case Message.PLAYER_QUIT: 
      removePlayer(paramMessage.getPlayerName());
      break;
    case Message.CREATE_GAME: 
      createGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      joinGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case Message.JOIN_GAME: 
      joinGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case Message.ABANDON_GAME: 
      abandonGame(paramMessage.getPlayerName());
      break;
    case Message.WATCH_GAME: 
      watchGame(paramMessage.getPlayerName(), paramMessage.getGameName());
      break;
    case Message.START_GAME: 
      startGame(paramMessage.getGameName(), paramMessage.getCustomMap());
      break;
    case Message.CUSTOM_MAP: 
      customMap(paramMessage.getGameName());
      break;
    case Message.ADD_ROBOT: 
      addRobot(paramMessage.getRobotName(), paramMessage.getGameName());
      break;
    case Message.REMOVE_ROBOT: 
      removeRobot(paramMessage.getRobotName(), paramMessage.getGameName());
      break;
    case Message.MESSAGE: 
      message(paramMessage.getPlayerName(), paramMessage.getMessageText(), paramMessage.getDestination());
      break;
    case Message.GAME_OVER: 
      gameOver(paramMessage.getGameName());
    }
  }
  
  protected void removePlayer(String playerName)
  {
    Player localPlayer = getPlayer(playerName);
    if (localPlayer != null) {
      players.removeElement(localPlayer);
    }
  }
  
  protected boolean removeRobot(String robotName, String gameName)
  {
    Game localGame = getGame(gameName);
    Robot localRobot = getRobot(robotName);
    if ((localRobot != null) && (localGame != null))
    {
      Player localPlayer = localGame.getPlayer(robotName);
      if (localPlayer != null)
      {
        localGame.removePlayer(localPlayer);
        return true;
      }
    }
    return false;
  }
  
  protected void startGame(String gameName, String paramString2)
  {
    Game localGame = getGame(gameName);
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
  
  protected void customMap(String gameName)
  {
    Game localGame = getGame(gameName);
    if (localGame != null) {
      localGame.randomMap = (!localGame.randomMap);
    }
  }
  
  protected void watchGame(String playerName, String gameName)
  {
    Player localPlayer = getPlayer(playerName);
    Game localGame = getGame(gameName);
    if ((localPlayer != null) && (localGame != null)) {
      localPlayer.game = localGame;
    }
  }
}