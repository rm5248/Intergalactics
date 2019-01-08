package igx.bots;

// Planet.java

/**
 * Holds everything a robot ever cared to know about a planet.
 */
public class Planet
{
    /**
     * X co-ordinate on the map.
     */
    private int x;

    /**
     * Y co-ordinate on the map.
     */
    private int y;

    /**
     * True if the planet is neutral (and therefore no other information is available).
     */
    private boolean neutral;

    /**
     * The number of the player who owns the planet.
     */
    private int owner;

    /**
     * Production.
     */
    private int production;

    /**
     * Attack ratio.
     */
    private int ratio;

    /**
     * Number of ships.
     */
    private int ships;

    /**
     * Array of attackers... attackers[i] is the number of attackers of this planet owned by player i.
     */
    private int[] attackers = new int[Constants.MAXIMUM_PLAYERS];

    private int totalAttackers = 0;

    /**
     * True if the planet has been consumed by a black hole.
     */
    private boolean blackHole;

    /**
     * Constructor for a planet.
     */
    public Planet (int x,
                   int y,
                   int owner,
                   int production,
                   int ratio,
                   int ships,
                   int[] attacker,
                   boolean blackHole,
                   int numPlayers) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.blackHole = blackHole;
        if (blackHole) {
            neutral = true;
            blackHole = true;
        } else if (owner == Constants.NEUTRAL)
            neutral = true;
        else {
            neutral = false;
            this.production = production;
            this.ratio = ratio;
            this.ships = ships;
        }
        attackers = new int[numPlayers];
        for (int j = 0; j < numPlayers; j++) {
            attackers[j] = attacker[j];
            totalAttackers += attackers[j];
        }
    }
    /**
     * Gets the number of attackers by a particular player.
     */
    public int getAttackers (int attackingPlayerNumber) {
        return attackers[attackingPlayerNumber];
    }
    /**
     * Returns the number of the player who owns the planet. This will be Constants.NEUTRAL
     * if the planet is neutral.
     */
    public int getOwner () {
        return owner;
    }
    /**
     * Returns the production of the planet.
     */
    public int getProduction () {
        return production;
    }
    /**
     * Returns the attack ratio of the planet.
     */
    public int getRatio () {
        return ratio;
    }
    /**
     * Returns the number of ships on the planet.
     */
    public int getShips () {
        return ships;
    }
    /**
     * Returns the total number of enemy ships attacking this planet.
     */
    public int getTotalAttackers () {
        return totalAttackers;
    }
    /**
     * Returns the x position of this planet.
     */
    public int getX () {
        return x;
    }
    /**
     * Returns the y position of this planet.
     */
    public int getY () {
        return y;
    }
    /**
     * Returns true if this planet has been consumed by a black hole (whatever that means...)
     */
    public boolean isBlackHole () {
        return blackHole;
    }
    /**
     * Returns true if this planet is neutral.
     */
    public boolean isNeutral () {
        return neutral;
    }
    /**
     * Given a planet number, returns its representative character.
     */
    public static final char planetChar (int n) {
        return igx.shared.Params.PLANETCHAR[n];
    }
    /**
     * Given a planet character, returns the planet number.
     */
    public static final int planetNumber (char c) {
        return igx.shared.Planet.char2num(c);
    }
    /**
     * Returns a string representation of this planet.
     */
    public String toString () {
        String s = "X: " + x + " Y: " + y + "Owner: " + owner + "production: " + production +
                   "ratio: " + ratio + "ships: " + ships;
        for (int i = 1; i < attackers.length; i++)
            if (attackers[i] > 0)
                s += "Attackers(" + i +"): " + attackers[i];
        return s;
    }
}