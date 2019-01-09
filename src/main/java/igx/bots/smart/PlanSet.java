package igx.bots.smart;

// PlanSet.java
import igx.bots.*;
import java.util.*;

public class PlanSet {

    private Vector plans = new Vector();
    private int[] reservedShips = new int[Constants.PLANETS];

    public PlanSet() {
        for (int i = 0; i < Constants.PLANETS; i++) {
            reservedShips[i] = 0;
        }
    }

    public void addPlan(Plan p) {
        plans.addElement(p);
        int n = p.getNumberOfReservations();
        for (int i = 0; i < n; i++) {
            Reservation r = p.getReservation(i);
            reservedShips[r.getSource()] += r.getShips();
        }
    }

    public void addReservation(Plan p, int source, int ships) {
        p.addReservation(new Reservation(source, p.getTarget(), p.getDeadline(), ships));
        reservedShips[source] += ships;
    }

    public int getDestinedShips(int p) {
        int n = plans.size();
        int total = 0;
        for (int i = 0; i < n; i++) {
            Plan plan = (Plan) (plans.elementAt(i));
            if (plan.getTarget() == p) {
                total += plan.getShips();
            }
        }
        return total;
    }

    public int getNumberOfPlans() {
        return plans.size();
    }

    public Plan[] getPlans() {
        int n = plans.size();
        Plan[] plan = new Plan[n];
        for (int i = 0; i < n; i++) {
            plan[i] = (Plan) (plans.elementAt(i));
        }
        return plan;
    }

    public int getReservedShips(int planet) {
        return reservedShips[planet];
    }

    public boolean hasPlan(int p) {
        int n = plans.size();
        for (int i = 0; i < n; i++) {
            Plan plan = (Plan) (plans.elementAt(i));
            if (plan.getTarget() == p) {
                return true;
            }
        }
        return false;
    }

    /**
     * Should be called by the robot every turn to keep the plan set up to date.
     */
    public void update(GameState g, Bot bot) {
        int n = plans.size();
        int i = 0;
        while (i < n) {
            Plan plan = (Plan) (plans.elementAt(i));
            Fleet[] fleet = plan.getFleets(g, g.getTime());
            for (int j = 0; j < fleet.length; j++) {
                reservedShips[fleet[j].source] -= fleet[j].ships;
            }
            bot.sendBatchFleets(fleet);
            if (plan.getDeadline() <= g.getTime()) {
                plans.removeElementAt(i);
                n--;
            } else {
                i++;
            }
        }
    }
}
