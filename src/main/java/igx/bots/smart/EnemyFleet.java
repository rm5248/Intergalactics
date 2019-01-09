package igx.bots.smart;

/**
 * @author John Watkinson
 */
import igx.bots.*;

public class EnemyFleet {

    private int owner;
    private int origin;
    private int ratio;
    private int ships;
    private int radius;

    public EnemyFleet(int owner, int origin, int ratio, int ships, int radius) {
        this.owner = owner;
        this.origin = origin;
        this.ratio = ratio;
        this.ships = ships;
        this.radius = radius;
    }

    public int getOrigin() {
        return origin;
    }

    public int getOwner() {
        return owner;
    }

    public int getRadius() {
        return radius;
    }

    public int getRatio() {
        return ratio;
    }

    public int getShips() {
        return ships;
    }
}
