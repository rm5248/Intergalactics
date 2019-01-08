package igx.shared;

// Forum.java 

import java.util.*;

public class Forum implements MessageListener {
  
  // Forum states
  // public static final int NO_GAMES = 0;
  // public static final int NOT_JOINED = 1;
  // public static final int WATCHING_GAME = 2;
  // public static final int IN_PROGRESS = 3;
  // public static final int IN_NEW_GAME = 4;
  // public static final int CREATED_GAME = 5;
  // public static final int CREATED_WITH_ROBOTS = 6;

  // private int state = NO_GAMES;

  protected Vector games = new Vector();
  protected Vector players = new Vector();
  protected Vector gamePool = new Vector();

  public Robot[] botList;

  public Forum (Robot[] botList) {
	this.botList = botList;
  }  
  protected boolean abandonGame(String name) {
	Player p = getPlayer(name);
	Game g = p.game;
	if ((g != null) && (p != null) && (g.getPlayer(name) != null)) {
	  if (!g.inProgress)
	g.removePlayer(p);
	  gamePool.removeElement(p);
	  return true;
	} else
	  return false;
  }  
  /**
   * This method was created in VisualAge.
   * @return boolean
   * @param name java.lang.String
   * @param gameName java.lang.String
   */
  public boolean addCustomRobot(Robot r, String gameName) {
	Game g = getGame(gameName);
	if (g != null) {
	  Player p = r.toPlayer();
	  p.customRobot = true;
	  p.r = r;
	  if (g.getPlayer(r.name) == null)
	return g.addPlayer(p);
	}
	return false;
  }  
  protected Player addPlayer (Player p) {
	players.addElement(p);
	return p;
  }  
  protected Player addPlayer (String name) {
	Player p = new Player(name);
	players.addElement(p);
	return p;
  }  
  protected boolean addRobot(String name, String gameName) {
	Game g = getGame(gameName);
	int n = botList.length;
	Random random = new Random();
	
	Robot r = getRobot(name);
	if ((r != null) && (g != null)) {
	  Player p = r.toPlayer();
	  if (g.getPlayer(name) == null)
	return g.addPlayer(p);
	}
	return false;
  }  
  protected boolean createGame(String name, String gameName) {
	if (games.size() == Params.MAX_GAMES)
	  return false;
	Player p = getPlayer(name);
	if (p != null) {
	  Game g = p.game;
	  if (g == null) {
	games.addElement(new Game(gameName, name));
	return true;
	  }
	}
	return false;
  }  
  protected void gameOver(String gameName) {
	Game g = getGame(gameName);
	if (g != null) {
	  games.removeElement(g);
	  for (int i = 0; i < g.numPlayers; i++) {
	Player p = g.player[i];
	if (p.isHuman && g.activePlayer[i]) {
	  gamePool.removeElement(p);
	  p.inGame = false;
	  p.game = null;
	}
	  }
	}
  }  
  public Game getGame (String name) {
	int n = games.size();
	for (int i = 0; i < n; i++) {
	  Game g = (Game)(games.elementAt(i));
	  if (g.name.equals(name))
	return g;
	}
	return null;
  }  
  public Player getPlayer (String name) {
	int n = players.size();
	for (int i = 0; i < n; i++) {
	  Player p = (Player)(players.elementAt(i));
	  if (p.name.equals(name))
	return p;
	}
	return null;
  }  
  /**
   * This method was created in VisualAge.
   * @return igx.shared.Player
   * @param name java.lang.String
   */
  public Player getPoolPlayer (String name) {
	int n = gamePool.size();
	for (int i = 0; i < n; i++) {
	  Player p = (Player)(gamePool.elementAt(i));
	  if (p.name.equals(name))
	return p;
	}
	return null;
  }  
  public Robot getRobot(String name) {
	int n = botList.length;
	for (int i = 0; i < n; i++) {
	  if (botList[i].name.equals(name))
	return botList[i];
	}
	return null;
  }  
  protected boolean joinGame (String name, String gameName) {
	Game g = getGame(gameName);
	Player p = getPlayer(name);
	if ((g != null) && !g.inProgress && (p != null) && (g.numPlayers < Params.MAXPLAYERS) && (g.getPlayer(name) == null)) {
	  g.addPlayer(p);
	  return true;
	} else
	  return false;
  }  
  protected void message (String player, String text, int destination) {
  }  
  public void messageEvent (Message message) {
	int type = message.getType();
	switch (type) {
	case Message.PLAYER_ARRIVED:
	  addPlayer(message.getPlayerName());
	  break;
	case Message.PLAYER_LEFT: case Message.PLAYER_QUIT:
	  removePlayer(message.getPlayerName());
	  break;
	case Message.CREATE_GAME:
	  createGame(message.getPlayerName(), message.getGameName());
	  joinGame(message.getPlayerName(), message.getGameName());
	  break;
	case Message.JOIN_GAME:
	  joinGame(message.getPlayerName(), message.getGameName());
	  break;
	case Message.ABANDON_GAME:
	  abandonGame(message.getPlayerName());
	  break;
	case Message.WATCH_GAME:
	  watchGame(message.getPlayerName(), message.getGameName());
	  break;
	case Message.START_GAME:
	  startGame(message.getGameName(), message.getCustomMap());
	  break;
	case Message.CUSTOM_MAP:
	  customMap(message.getGameName());
	  break;
	case Message.ADD_ROBOT:
	  addRobot(message.getRobotName(), message.getGameName());
	  break;
	case Message.REMOVE_ROBOT:
	  removeRobot(message.getRobotName(), message.getGameName());
	  break;
	case Message.MESSAGE:
	  message(message.getPlayerName(), message.getMessageText(), message.getDestination());
	  break;
	case Message.GAME_OVER:
	  gameOver(message.getGameName());
	  break;
	}
  }  
  protected void removePlayer (String name) {
	Player p = getPlayer(name);
	if (p != null)
	  players.removeElement(p);
  }  
  protected boolean removeRobot (String name, String gameName) {
	Game g = getGame(gameName);
	Robot r = getRobot(name);
	if ((r != null) && (g != null)) {
	  Player p = g.getPlayer(name);
	  if (p != null) {
	g.removePlayer(p);
	return true;
	  }
	}
	return false;
  }  
  protected void startGame(String gameName,
                           String customMap) {
	Game g = getGame(gameName);
	// Add players to pool
	int n = g.numPlayers;
	for (int i = 0; i < n; i++)
	  if (g.player[i].isHuman) {
	g.player[i].status = Params.DONT_SIGNAL;
	gamePool.addElement(g.player[i]);
	g.setActivePlayer(g.player[i].name, true);
	  }
	if (g != null)
	  g.inProgress = true;
  }  
  protected void customMap (String gameName) {
     	Game g = getGame(gameName);
        if (g != null) {
           g.randomMap = !g.randomMap;
        }
  }
  protected void watchGame (String name, String gameName) {
	Player p = getPlayer(name);
	Game g = getGame(gameName);
	if ((p != null) && (g != null))
	  p.game = g;
  }  
}
