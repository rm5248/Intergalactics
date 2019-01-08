package igx.shared;

// Fleet.java
// Class Fleet

public class Fleet {
	// Data fields
	public Player owner;
	public int ships, ratio;
	public Planet destination, source;
	public float distance;
	public Fleet next;
	public GameInstance Game;
  public Fleet (Fleet fleet) {
	Game = fleet.Game;
	owner = fleet.owner;
	ships = fleet.ships;
	ratio = fleet.ratio;
	destination = fleet.destination;
	distance = fleet.distance;
	next = fleet.next;
  }
  public Fleet (GameInstance Game, Planet source, Planet destination, int ships)
  {
	this.Game = Game;
        this.source = source;
	owner = source.owner;
	this.ships = ships;
	source.ships -= ships;
	Game.dirty[Planet.char2num(source.planetChar)] = true;
	ratio = source.ratio;
	this.destination = destination;
	distance = new Double(Math.sqrt((source.x - destination.x) * (source.x - destination.x) +
									(source.y - destination.y) * (source.y - destination.y))).floatValue();
	next = null;
  }
  public Fleet (GameInstance Game, Planet destination, Planet source, Player owner, int ships, int ratio, float distance) {
	this.destination = destination;
	this.owner = owner;
	this.ships = ships;
	this.ratio = ratio;
	this.distance = distance;
	this.Game = Game;
  }
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String toString() {
	return "(" + owner.name +
	  ", S: " + ships +
	  ", %: " + ratio +
	  ", P: " + destination.planetChar +
	  ", D: " + distance + ")";
}
// UPDATE - Updates a fleet for this segment.  Returns true if it lands
boolean update() {
	char[] planetChar = {destination.planetChar};
	String planetString = new String(planetChar);
	distance -= Params.FLEETSPEED;
	if ((distance <= 0) || (Game.resolveAllConflicts))
		// Fleet has arrived
		{
		if (destination.blackHole) {
			Game.ui.postBlackHole(this);
			return true;
		}
		Game.dirty[Planet.char2num(destination.planetChar)] = true;
		if (owner == destination.owner)
			// Reinforcements
			{
			Game.fleetArrived(this);
			if (distance + Params.FLEETSPEED > 0)
				Game.ui.postReinforcements(ships, destination);
			destination.ships += ships;
			return true;
		} else
			// Combat
			{
			if (distance + Params.FLEETSPEED > 0) {
				Game.ui.postAttack(this, destination);
				Game.fleetArrived(this);
			}
      // PHASING THIS OUT - MAKES 1 SHIP FLEETS TOO POWERFUL
      /*
			int attackNumber = ships * Params.ATTACKSTRENGTH / 100;
			if ((ships * Params.ATTACKSTRENGTH) % 100 > 0)
				// Ceiling
				attackNumber++;
      */
      int attackNumber = ships;
      for (int i = 0; i < attackNumber; i++)
         if ((Game.pseudo(0, 99) < Params.ATTACKSTRENGTH) || (destination.ships == 0)) {
            if (((Game.pseudo(0, 99) < ratio) && ((--destination.ships) <= 0)) || (destination.ships == 0)) {
  					Game.ui.postInvasion(this, destination);
  					destination.ships = ships; // Planet has been invaded
  					destination.owner = owner;
  					return true;
  				}
         }
			destination.attackers.addElement(this);
			return false;
		}
	} else
		return false;
   }
}
