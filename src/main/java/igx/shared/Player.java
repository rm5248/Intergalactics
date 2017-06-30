package igx.shared;

import java.awt.Color;

public class Player
  implements Cloneable
{
  public static final int NOT_PLAYING = 0;
  public static final int PLAYING = 1;
  public static final int READY_TO_QUIT = 2;
  public String name;
  public int number;
  public boolean isActive = true;
  public boolean isHuman;
  public boolean isPresent;
  public boolean inGame = true;
  public int score;
  public int status = 0;
  public Game game = null;
  public boolean customRobot = false;
  public Robot r = null;
  public static final Player NEUTRAL = new Player("Neutral", 9);
  
  public Player() {}
  
  public Player(String paramString)
  {
    name = paramString;
    isHuman = true;
    isPresent = true;
    score = 0;
  }
  
  public Player(String paramString, int paramInt)
  {
    name = paramString;
    number = paramInt;
    isHuman = true;
    isPresent = true;
    score = 0;
  }
  
  public void addScore(int paramInt)
  {
    score += paramInt;
  }
  
  public Color getColor()
  {
    return Params.PLAYERCOLOR[number];
  }
  
  public int getScore()
  {
    return score;
  }
  
  public void resetScore()
  {
    score = 0;
  }
}