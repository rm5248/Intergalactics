package igx.bots;

// ArrivedFleet.java 
/**
 * This class stores everything a 'bot should know and nothing a bot shouldn't
 * know about arriving fleets.
 *
 * @author John Watkinson
 */
public class ArrivedFleet {

    /**
     * Number of ships in the fleet.
     */
    private int ships;

    /**
     * Planet number at which these ships have arrived.
     */
    private int planet;

    private int owner;

    /**
     * Constructor.
     */
    public ArrivedFleet(int ships, int planet, int owner) {
        this.ships = ships;
        this.planet = planet;
        this.owner = owner;
    }

    /**
     * Returns the player number of the owner of the fleet.
     */
    public int getOwner() {
        return owner;
    }

    /**
     * Returns the planet number at which the ships arrived.
     */
    public int getPlanetNumber() {
        return planet;
    }

    /**
     * Returns the number of ships in the fleet.
     */
    public int getShips() {
        return ships;
    }

    /**
     * Returns a string representation of this arrived fleet.
     */
    public String toString() {
        return "Ships: " + ships + " Owner: " + owner + " Planet: " + planet;
    }
}
