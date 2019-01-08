package igx.bots;

// Statistics.java 

/**
 * The class responsible for reporting the results of games.
 */

public class Statistics {
  
  /**
   * Contains the average processing time for the robot.
   */
  public long[] time;
  /**
   * Contains the score achieved by this robot.
   */
  public int[] score;
  /**
   * The bot number of the winner.
   */
  public  int winner;

  private int numBots;
  
  public Statistics (int numBots) {
	time = new long[numBots];
	score = new int[numBots];
	for (int i = 0; i < numBots; i++)
	  time[i] = 0;
	this.numBots = numBots;
  }  
  public void gameOver (int bot, int score, int numSegments) {
	time[bot] /= numSegments;
	this.score[bot] = score;
  }  
  public void reportTime (int bot, long time) {
	this.time[bot] += time;
  }  
  public void setWinner (int winner) {
	this.winner = winner;
  }  
}