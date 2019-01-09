package igx.bots;

public class SquidFleet extends Object {
// Tracked enemy fleet

    public int owner;
    public int ships;
    public int source;
    public int left;

    public SquidFleet(int ownr, int src, int shps, int lft) {
        super();

        owner = ownr;
        ships = shps;
        source = src;
        left = lft;
    }

    public String toString() {
        return "Player " + owner + " sent " + ships + " from planet " + Planet.planetChar(source)
                + " at time: " + left;
    }

    public int etaToPlanet(GameState game, int pl) {
        return game.arrivalTime(new Fleet(source, pl, ships), left);
    }

    public boolean matches(GameState game, int tim, ArrivedFleet fl) {
        boolean ret;

        ret = false;

        if ((ships == fl.getShips()) && (owner == fl.getOwner())) {
            // strong candidate.  Check if time matches
            int wouldArrive;

            wouldArrive = etaToPlanet(game, fl.getPlanetNumber());
            if (java.lang.Math.abs(wouldArrive - tim) <= 1) {
                ret = true;
            }
        }

        return ret;
    }

    public boolean outOfRange(int tim) {
        return ((tim - left) > (Constants.MAP_HEIGHT + Constants.MAP_WIDTH) * 10);
    }

    public int distToPlanet(GameState game, int tim, int pl) {
        int t;

        t = etaToPlanet(game, pl) - tim;
        if (t > 0) {
            return t;
        } else {
            return -1;
        }
    }
}
