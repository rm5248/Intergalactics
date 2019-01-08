package igx.bots.smart;

/**
 * Class that holds a ship reservation.
 * @author John Watkinson
 */

import igx.bots.*;

public class Reservation
{
  /**
   * Source of reservation.
   */
  private int source;

  /**
   * Target of reservation.
   */
  private int target;

  /**
   * Defensive reservation.
   */
  private boolean stationary;

  /**
   * Reservation deadline.
   */
  private int deadline;

  /**
   * Number of ships.
   */
  private int ships;

  /**
   * Constructor for stationary reservation.
   */
  public Reservation (int planet, int deadline, int ships) {
	source=target=planet;
	this.deadline = deadline;
	this.ships = ships;
	stationary = true;
  }  
  /**
   * Constructor for non-stationary reservation.
   */
  public Reservation (int source, int target, int deadline, int ships) {
	this.source = source;
	this.target = target;
	this.deadline = deadline;
	this.ships = ships;
  }  
  public int getDeadline () {
	return deadline;
  }  
  public int getShips () {
	return ships;
  }  
  public int getSource () {
	return source;
  }  
  public int getTarget () {
	return target;
  }  
  public boolean isStationary () {
	return stationary;
  }  
}