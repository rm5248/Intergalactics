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
  private String s1;
  private String s2;
  private int i1;
  private int i2;
  private int i3;
  
  public Message() {}
  
  public static Message abandonGame(String paramString)
  {
    Message localMessage = new Message();
    type = 5;
    s1 = paramString;
    return localMessage;
  }
  
  public static Message addRobot(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 7;
    s1 = paramString1;
    s2 = paramString2;
    return localMessage;
  }
  
  public static Message createGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 3;
    s1 = paramString1;
    s2 = paramString2;
    return localMessage;
  }
  
  public static Message gameOver(String paramString)
  {
    Message localMessage = new Message();
    type = 10;
    s2 = paramString;
    return localMessage;
  }
  
  public int getDestination()
  {
    return i2;
  }
  
  public String getGameName()
  {
    return s2;
  }
  
  public String getMessageText()
  {
    return s2;
  }
  
  public String getPlayerName()
  {
    return s1;
  }
  
  public String getCustomMap()
  {
    return s1;
  }
  
  public String getRobotName()
  {
    return s1;
  }
  
  public int getShips()
  {
    return i3;
  }
  
  public int getSource()
  {
    return i1;
  }
  
  public int getStatus()
  {
    return i1;
  }
  
  public int getType()
  {
    return type;
  }
  
  public static Message joinGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 4;
    s1 = paramString1;
    s2 = paramString2;
    return localMessage;
  }
  
  public static Message message(String paramString1, String paramString2, int paramInt)
  {
    Message localMessage = new Message();
    type = 0;
    s1 = paramString1;
    s2 = paramString2;
    i2 = paramInt;
    return localMessage;
  }
  
  public static Message playerArrived(String paramString)
  {
    Message localMessage = new Message();
    type = 1;
    s1 = paramString;
    return localMessage;
  }
  
  public static Message playerLeft(String paramString)
  {
    Message localMessage = new Message();
    type = 2;
    s1 = paramString;
    return localMessage;
  }
  
  public static Message playerQuit(String paramString)
  {
    Message localMessage = new Message();
    type = 12;
    s1 = paramString;
    return localMessage;
  }
  
  public static Message playerQuitGame(String paramString, int paramInt)
  {
    Message localMessage = new Message();
    type = 12;
    s1 = paramString;
    i1 = paramInt;
    return localMessage;
  }
  
  public static Message removeRobot(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 8;
    s1 = paramString1;
    s2 = paramString2;
    return localMessage;
  }
  
  public static Message sendFleet(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    Message localMessage = new Message();
    type = 11;
    s1 = paramString;
    i1 = paramInt1;
    i2 = paramInt2;
    i3 = paramInt3;
    return localMessage;
  }
  
  public static Message startGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 9;
    s1 = paramString2;
    s2 = paramString1;
    return localMessage;
  }
  
  public static Message customMap(String paramString)
  {
    Message localMessage = new Message();
    type = 13;
    s2 = paramString;
    return localMessage;
  }
  
  public static Message watchGame(String paramString1, String paramString2)
  {
    Message localMessage = new Message();
    type = 6;
    s1 = paramString1;
    s2 = paramString2;
    return localMessage;
  }
}