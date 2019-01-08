package igx.shared;

// Player.java
// Player class

import java.awt.*;

public class Player implements Cloneable
{
  public static final int NOT_PLAYING = 0;
  public static final int PLAYING = 1;
  public static final int READY_TO_QUIT = 2;

  public String name;
  public int number;
  public boolean isActive = true;
  public boolean isHuman;
  public boolean isPresent;
  // Used by server
  public boolean inGame = true;
  public int score;
  public int status = NOT_PLAYING;
  public Game game = null;
  // Custom robot stuff
  public boolean customRobot = false;
  public Robot r = null;

  public static final Player NEUTRAL = new Player("Neutral", Params.NEUTRAL);
  public Player ()
  {}  
  public Player (String name) {
	this.name = name;
	isHuman = true;
	isPresent = true;
	score = 0;
  }  
  public Player (String name, int number)
  {
	this.name = name;
	this.number = number;
	isHuman = true;
	isPresent = true;
	score = 0;
  }  
  public void addScore (int value) {
	score += value;
  }  
  public Color getColor() {
	return Params.PLAYERCOLOR[number];
  }  
  public int getScore () {
	return score;
  }  
  public void resetScore () {
	score = 0;
  }  
}