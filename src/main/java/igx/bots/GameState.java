package igx.bots;

// GameState.java
import igx.shared.GameInstance;

/**
 * Contains an entire game state from the player's point of view. You don't need
 * to create one of these, you'll get one passed to you when the server calls
 * your 'bot's {@link Bot#update update} method.
 * <P>
 * The game state contains all information at given turn:segment. Included in
 * this information is the number of players, the player information, the planet
 * information, a list of fleets that arrived this turn, etc. See methods below
 * give you access to this information.
 *
 * @author John Watkinson
 */
public class GameState {

    /**
     * Number of players in the game.
     */
    private int numPlayers;

    /**
     * The map, as an array of characters.
     */
    private int[][] map;

    /**
     * The turn number (Time is in turn:segment format).
     */
    private int turn = 1;

    /**
     * The segment number (Time is in turn:segment format).
     */
    private int segment = 0;

    /**
     * The combined time = (turn-1) * Constants.SEGMENTS + segment
     */
    private int time;

    /**
     * The players.
     */
    private Player[] player;

    /**
     * The planets.
     */
    private Planet[] planet;

    /**
     * The fleets that arrived this turn.
     */
    private ArrivedFleet[] fleet;

    /**
     * Constructor with explicit parameters.
     */
    public GameState(int numPlayers,
            int[][] map,
            int turn,
            int segment,
            Player[] player,
            Planet[] planet,
            ArrivedFleet[] fleet) {
        this.numPlayers = numPlayers;
        this.map = map;
        this.turn = turn;
        this.segment = segment;
        time = (turn - 1) * Constants.SEGMENTS + segment;
        this.player = player;
        this.planet = planet;
        this.fleet = fleet;
    }

    /**
     * Constuctor which passes in the initial igx.shared.GameInstance.
     */
    public GameState(GameInstance game, ArrivedFleet[] fleet) {
        this.numPlayers = game.players;
        map = new int[Constants.MAP_WIDTH][Constants.MAP_HEIGHT];
        for (int x = 0; x < Constants.MAP_WIDTH; x++) {
            for (int y = 0; y < Constants.MAP_HEIGHT; y++) {
                if (game.map[x][y] == igx.shared.Params.SPACE) {
                    map[x][y] = Constants.EMPTY_SPACE;
                } else {
                    map[x][y] = igx.shared.Planet.char2num(game.map[x][y]);
                }
            }
        }
        player = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            player[i] = new Player(game.player[i].name, game.player[i].score, game.player[i].isActive);
        }
        planet = new Planet[Constants.PLANETS];
        for (int i = 0; i < Constants.PLANETS; i++) {
            planet[i] = new Planet(game.planet[i].x,
                    game.planet[i].y,
                    game.planet[i].owner.number,
                    game.planet[i].production,
                    game.planet[i].ratio,
                    game.planet[i].ships,
                    game.planet[i].attacker,
                    game.planet[i].blackHole,
                    numPlayers);
        }
        turn = game.turn;
        segment = game.segment;
        time = (turn) * Constants.SEGMENTS + segment;
        this.fleet = fleet;
        if (fleet == null) {
            this.fleet = new ArrivedFleet[0];
        }
    }

    /**
     * This function returns the arrival time if the given fleet is sent at the
     * given time.
     *
     * @param fleet The fleet who's arrival time is required.
     * @param departureTime The time of departure for that fleet.
     */
    public int arrivalTime(Fleet fleet, int departureTime) {
        double distance = getDistance(fleet.source, fleet.destination);
        return (int) Math.ceil(distance / Constants.FLEET_SPEED) + departureTime;
    }

    /**
     * Returns the requested arrived fleet by number.
     */
    public ArrivedFleet getArrivedFleet(int fleetNumber) {
        if (fleet != null) {
            return fleet[fleetNumber];
        } else {
            return null;
        }
    }

    /**
     * Returns the distance between two planets.
     */
    public double getDistance(int p1, int p2) {
        Planet s = getPlanet(p1);
        Planet d = getPlanet(p2);
        int sx = s.getX();
        int sy = s.getY();
        int dx = d.getX();
        int dy = d.getY();
        return Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy));
    }

    /**
     * Returns the distance between the planet and a point.
     */
    public double getDistance(int p1, int x, int y) {
        Planet p = getPlanet(p1);
        int px = p.getX();
        int py = p.getY();
        return Math.sqrt((px - x) * (px - x) + (py - y) * (py - y));
    }

    /**
     * Returns how many fleets arrived this turn.
     */
    public int getNumberOfArrivedFleets() {
        return fleet.length;
    }

    /**
     * Returns the number of players in the game.
     */
    public int getNumberOfPlayers() {
        return numPlayers;
    }

    /**
     * Returns the requested planet by number.
     */
    public Planet getPlanet(int planetNumber) {
        return planet[planetNumber];
    }

    /**
     * Returns the requested player by number.
     */
    public Player getPlayer(int playerNumber) {
        return player[playerNumber];
    }

    /**
     * Returns the segment number.
     */
    public int getSegment() {
        return segment;
    }

    /**
     * Returns a convenient integer time format. time = (turn-1) *
     * Constants.SEGMENTS + segment.
     *
     * @see Constants
     */
    public int getTime() {
        return time;
    }

    /**
     * Returns the turn number.
     */
    public int getTurn() {
        return turn;
    }

    /**
     * Returns the contents of the map at square (x,y). If a planet occupies the
     * square, the result will be the the planet number, otherwise it will be
     * Constants.EMPTY_SPACE.
     */
    public int getXY(int x, int y) {
        return map[x][y];
    }
}
