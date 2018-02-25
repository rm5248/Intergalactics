package igx.shared;

public class Message
{
  public static final Message SHUT_DOWN_MESSAGE = null;
  public static final int MESSAGE = 0;
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
  public static final int SEND_FLEET = 11;
  public static final int PLAYER_QUIT = 12;
  private int type;
  
  //Note: the strings are dependent on the message,
  //so they get stupid and generic names for now
  private String string1;
  private String string2;
  private int status;
  private int destination;
  private int numShips;
  
  public Message() {}
  
  public static Message abandonGame(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = Message.ABANDON_GAME;
    localMessage.string1 = paramString;
    return localMessage;
  }
  
  public static Message addRobot(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = ADD_ROBOT;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    return localMessage;
  }
  
  public static Message createGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = CREATE_GAME;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    return localMessage;
  }
  
  public static Message gameOver(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = GAME_OVER;
    localMessage.string2 = paramString;
    return localMessage;
  }
  
  public int getDestination()
  {
    return destination;
  }
  
  public String getGameName()
  {
    return string2;
  }
  
  public String getMessageText()
  {
    return string2;
  }
  
  public String getPlayerName()
  {
    return string1;
  }
  
  public String getCustomMap()
  {
    return string1;
  }
  
  public String getRobotName()
  {
    return string1;
  }
  
  public int getShips()
  {
    return numShips;
  }
  
  public int getSource()
  {
    return status;
  }
  
  public int getStatus()
  {
    return status;
  }
  
  public int getType()
  {
    return type;
  }
  
  public static Message joinGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = JOIN_GAME;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    return localMessage;
  }
  
  /**
   * 
   * @param paramString1
   * @param paramString2
   * @param playerId The ID of the player to send this message to.  Since there is a 
   * maximum of 8 people, 9 sends to everybody
   * @return 
   */
  public static Message message(String paramString1, String paramString2, int playerId)
  {
    Message localMessage = new Message();
    localMessage.type = MESSAGE;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    localMessage.destination = playerId;
    return localMessage;
  }
  
  public static Message playerArrived(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = PLAYER_ARRIVED;
    localMessage.string1 = paramString;
    return localMessage;
  }
  
  public static Message playerLeft(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = PLAYER_LEFT;
    localMessage.string1 = paramString;
    return localMessage;
  }
  
  public static Message playerQuit(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = PLAYER_QUIT;
    localMessage.string1 = paramString;
    return localMessage;
  }
  
  public static Message playerQuitGame(String paramString, int paramInt)
  {
    Message localMessage = new Message();
    localMessage.type = PLAYER_QUIT;
    localMessage.string1 = paramString;
    localMessage.status = paramInt;
    return localMessage;
  }
  
  public static Message removeRobot(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = REMOVE_ROBOT;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    return localMessage;
  }
  
  public static Message sendFleet(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    Message localMessage = new Message();
    localMessage.type = SEND_FLEET;
    localMessage.string1 = paramString;
    localMessage.status = paramInt1;
    localMessage.destination = paramInt2;
    localMessage.numShips = paramInt3;
    return localMessage;
  }
  
  public static Message startGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = START_GAME;
    localMessage.string1 = paramString2;
    localMessage.string2 = paramString1;
    return localMessage;
  }
  
  public static Message customMap(String paramString)
  {
    Message localMessage = new Message();
    localMessage.type = CUSTOM_MAP;
    localMessage.string2 = paramString;
    return localMessage;
  }
  
  public static Message watchGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    localMessage.type = WATCH_GAME;
    localMessage.string1 = paramString1;
    localMessage.string2 = paramString2;
    return localMessage;
  }
}