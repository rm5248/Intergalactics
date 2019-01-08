package igx.shared;

// Message.java 

public class Message {

  public static final Message SHUT_DOWN_MESSAGE = null;

  public static final int MESSAGE = 0;
  // Forum messages
  public static final int PLAYER_ARRIVED = 1;
  public static final int PLAYER_LEFT = 2;
  public static final int CREATE_GAME = 3;
  public static final int JOIN_GAME = 4;
  public static final int ABANDON_GAME = 5;
  public static final int WATCH_GAME = 6;
  public static final int ADD_ROBOT = 7;
  public static final int REMOVE_ROBOT = 8;
  public static final int START_GAME = 9;
  public static final int GAME_OVER = 10;
  public static final int CUSTOM_MAP = 13;
  // Game messages
  public static final int SEND_FLEET = 11;
  public static final int PLAYER_QUIT = 12;

  // Message type
  private int type;

  // Message contents
  private String s1, s2;
  private int i1, i2, i3;

  // Abandoned Game
  public static Message abandonGame (String name) {
	Message m = new Message();
	m.type = ABANDON_GAME;
	m.s1 = name;
	return m;
  }  
  // Added Robot
  public static Message addRobot (String robotName, String gameName) {
	Message m = new Message();
	m.type = ADD_ROBOT;
	m.s1 = robotName;
	m.s2 = gameName;
	return m;
  }  
  // Created Game
  public static Message createGame (String name, String gameName) {
	Message m = new Message();
	m.type = CREATE_GAME;
	m.s1 = name;
	m.s2 = gameName;
	return m;
  }  
  // Game Ends
  public static Message gameOver (String gameName) {
	Message m = new Message();
	m.type = GAME_OVER;
	m.s2 = gameName;
	return m;
  }  
  // Destination of a fleet or a message
  public int getDestination () {
	return i2;
  }  
  public String getGameName () {
	return s2;
  }  
  public String getMessageText () {
	return s2;
  }  
  public String getPlayerName () {
	return s1;
  }  
  public String getCustomMap () {
	return s1;
  }  
  public String getRobotName () {
	return s1; 
  }  
  public int getShips () {
	return i3;
  }  
  public int getSource () {
	return i1;
  }  
  public int getStatus () {
	return i1;
  }  
  public int getType () {
	return type;
  }  
  // Joined Game
  public static Message joinGame (String name, String gameName) {
	Message m = new Message();
	m.type = JOIN_GAME;
	m.s1 = name;
	m.s2 = gameName;
	return m;
  }  
  // Message
  public static Message message (String name, String text, int who) {
	Message m = new Message();
	m.type = MESSAGE;
	m.s1 = name;
	m.s2 = text;
	m.i2 = who;
	return m;
  }  
  // - MESSAGE CREATORS -

  // Arrived
  public static Message playerArrived (String name) {
	Message m = new Message();
	m.type = PLAYER_ARRIVED;
	m.s1 = name;
	return m;
  }  
  // Left
  public static Message playerLeft (String name) {
	Message m = new Message();
	m.type = PLAYER_LEFT;
	m.s1 = name;
	return m;
  }  
  // Player quit
  public static Message playerQuit (String name) {
	Message m = new Message();
	m.type = PLAYER_QUIT;
	m.s1 = name;
	return m;
  }  
  // Player quit
  public static Message playerQuitGame (String name, int status) {
	Message m = new Message();
	m.type = PLAYER_QUIT;
	m.s1 = name;
	m.i1 = status;
	return m;
  }  
  // Removed Robot
  public static Message removeRobot (String robotName, String gameName) {
	Message m = new Message();
	m.type = REMOVE_ROBOT;
	m.s1 = robotName;
	m.s2 = gameName;
	return m;
  }  
  // Sent Fleet
  public static Message sendFleet (String name, int from, int to, int ships) {
	Message m = new Message();
	m.type = SEND_FLEET;
	m.s1 = name;
	m.i1 = from;
	m.i2 = to;
	m.i3 = ships;
	return m;
  }  
  // Started Game
  public static Message startGame (String gameName, String map) {
	Message m = new Message();
	m.type = START_GAME;
        m.s1 = map;
	m.s2 = gameName;
	return m;
  }  
  // Custom Map
  public static Message customMap (String gameName) {
	Message m = new Message();
	m.type = CUSTOM_MAP;
	m.s2 = gameName;
	return m;
  }  
  // Watch Game
  public static Message watchGame (String name, String gameName) {
	Message m = new Message();
	m.type = WATCH_GAME;
	m.s1 = name;
	m.s2 = gameName;
	return m;
  }  
}
