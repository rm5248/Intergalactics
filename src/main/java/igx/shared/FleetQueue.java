package igx.shared;

// FleetQueue.java
// Class FleetQueue

public class FleetQueue
{
  // The fleets are maintained in a priority queue
  public Fleet first = null;
  public GameInstance Game;

  public FleetQueue (GameInstance Game)
  {
	this.Game = Game;
  }  
  public void doScores () {
	Fleet lead = first;
	while (lead != null) {
	  lead.owner.addScore(lead.ships * Params.SHIP_VALUE);
	  lead = lead.next;
	}
  }  
  // INSERT - Inserts a fleet into the queue
  public void insert (Fleet f)
  {
	Fleet lead = first;
	Fleet follow = null;
	if (first == null)
	{
	  first = f;
	  f.next = null;
	}
	else
	{
	  while ((lead != null) && (lead.distance <= f.distance))
	  {
		follow = lead;
		lead = lead.next;
	  }
	  if (lead == null)
	  {
		f.next = null;
		if (follow != null)
		  follow.next = f;
		else
		  first = f;
	  }
	  else
	  {
		f.next = lead;
		if (follow != null)
		  follow.next = f;
		else
		  first = f;
	  }
	}
  }  
  // REMOVE - Removes fleet from queue (NOTE: fleet must be present)
  public void remove (Fleet f)
  {
	Fleet lead = first;
	Fleet follow = null;
	while (lead != f)
	{
	  follow = lead;
	  lead = lead.next;
	}
	if (follow == null)
	  first = f.next;
	else
	  follow.next = f.next;
  }  
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	Fleet fleet = first;
	StringBuffer s = new StringBuffer("");
	while (fleet != null) {
		s.append(fleet.toString());
		fleet = fleet.next;
		s.append("\n");
	}
	return s.toString();
}
  // UPDATE - Updates the status of the fleets. Called each segment
  public void update ()
  {
	Fleet lead = first;
	Fleet follow = null;
	// Initialize stats for intel
	for (int i = 0; i<Params.PLANETS; i++)
	  Game.planet[i].attacker = new int[Params.MAXPLAYERS];
	while (lead != null)
	{
	  if (lead.update()) // Fleet has disbanded, so remove from list
	  {
		if (follow == null)
		  first = lead.next;
		else
		  follow.next = lead.next;
	  }
	  else
	  {
		follow = lead;
		if (lead.distance <= 0) // Count these ships for intel
		  lead.destination.attacker[lead.owner.number] += lead.ships;
	  }
	  lead = lead.next;
	}
  }  
}