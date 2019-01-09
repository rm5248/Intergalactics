package igx.bots;

// Fleet.java 
/**
 * This class holds the info for a 'bot's fleet.
 */
public class Fleet {

    /**
     * The planet number of the fleet's source.
     */
    public int source;

    /**
     * The planet number of the fleet's destination.
     */
    public int destination;

    /**
     * The number of ships in this fleet.
     */
    public int ships;

    /**
     * Constructor to create a new fleet.
     *
     * @param source The source planet number of the fleet.
     * @param destination The destination planet number of the fleet.
     * @param ships The number of ships in the fleet.
     */
    public Fleet(int source, int destination, int ships) {
        this.source = source;
        this.destination = destination;
        this.ships = ships;
    }
}
