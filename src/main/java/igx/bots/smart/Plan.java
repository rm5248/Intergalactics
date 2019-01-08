package igx.bots.smart;

// Plan.java

import igx.bots.*;
import java.util.*;

public class Plan
{
  private int target;
  private Vector reservations = new Vector();
  private int deadline;
  private int totalShips = 0;
  
  /**
   * Constructor for new plan with given deadline.
   */
  public Plan (int target, int deadline) {
	this.target = target;
	this.deadline = deadline;
  }  
  /**
   * Constructor for an immediate plan.
   */
  public Plan (GameState game, int target, int source, int ships) {
	this.target = target;
	Fleet f = new Fleet(source, target, ships);
	deadline = game.arrivalTime(f, game.getTime());
	addReservation(source, ships);
  }  
  /**
   * Adds a reservation to the plan.
   */
  public void addReservation (int source, int ships) {
	addReservation(new Reservation(source, target, deadline, ships));
  }  
  /**
   * Adds a reservation to the plan.
   */
  public void addReservation (Reservation r) {
	totalShips += r.getShips();
	reservations.addElement(r);
  }  

public void setDeadline(int dLine) {
	deadline = dLine;
}
  /**
   * Adjusts the deadline to a reasonable value assuming that all fleets
   * are sent immediately. Use only on immediate plans.
   */
  public void adjustDeadline (GameState game) {
	int n = reservations.size();
	for (int i = 0; i < n; i++) {
	  Reservation r = (Reservation)(reservations.elementAt(i));
	  Fleet f = new Fleet(r.getSource(), target, r.getShips());
	  int arrivalTime = game.arrivalTime(f, game.getTime());
	  if (arrivalTime > deadline)
	deadline = arrivalTime;
	}
  }  
  /**
   * Returns the deadline of of this plan.
   */
  public int getDeadline () {
	return deadline;
  }  
  /**
   * Gets the fleets to send if any for this plan at this time.
   */
  public Fleet[] getFleets (GameState game, int time) {
	int n = reservations.size();
	Vector fleets = new Vector();
	int i = 0;
	while (i < n) {
	  Reservation r = (Reservation)(reservations.elementAt(i));
	  Fleet f = new Fleet(r.getSource(), target, r.getShips());
	  if (game.arrivalTime(f, game.getTime()) >= deadline) {
	fleets.addElement(f);
	reservations.removeElementAt(i);
	n--;
	  } else
	i++;
	}
	int m = fleets.size();
	Fleet[] fleet = new Fleet[m];
	for (i = 0; i < m; i++)
	  fleet[i] = (Fleet)(fleets.elementAt(i));
	return fleet;
  }  
  /**
   * Gets the number of reservations.
   */
  public int getNumberOfReservations () {
	return reservations.size();
  }  
  /**
   * Gets reservation by number.
   */
  public Reservation getReservation (int i) {
	return (Reservation)(reservations.elementAt(i));
  }  
  /**
   * Returns the total ships in this plan.
   */
  public int getShips () {
	return totalShips;
  }  
  /**
   * Returns the target of this plan.
   */
  public int getTarget () {
	return target;
  }  
}