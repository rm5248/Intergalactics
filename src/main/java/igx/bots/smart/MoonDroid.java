package igx.bots.smart;

import igx.bots.*;
import java.util.*;

// MoonDroid.java
/**
 * @author John Watkinson
 */
public class MoonDroid extends Bot {

    /**
     * Number of different bots.
     */
    private static final int NUMBER_OF_BOTS = 4;

    /**
     * Names of the bots.
     */
    private static final String[] NAME
            = {"Prime", "RinTin", "Watkit", "Karp"};

    private static final int[] NEUTRAL_MIN = {15, 15, 10, 12};
    private static final int[] NEUTRAL_MAX = {20, 15, 20, 20};
    private static final int[] CLOSE_THRESHOLD = {5, 8, 5, 7};
    private static final int[] FORK_CHANCE = {50, 25, 10, 5};
    /**
     * All ratios in %.
     */
    private static final int[] MIN_ATTACK_AMOUNT = {180, 200, 170, 200};
    private static final int[] MAX_ATTACK_AMOUNT = {240, 330, 260, 330};
    private static final int[] PRODUCTION_THRESHOLD = {3, 2, 4, 3};
    private static final int[] LOCALITY = {90, 90, 75, 90};
    private static final int[] TERRITORIALITY = {90, 90, 80, 70};
    private static final int[] LOSING_RATIO = {150, 150, 130, 140};
    private static final int[] REINFORCE_CHANCE = {0, 2, 5, 20};
    /**
     * This stat is out of 1000.
     */
    private static final int[] GOD_SQUAD = {50, 20, 60, 80};
    private static final int[] MIN_ALMOST_EMPTY = {1, 1, 1, 2};
    private static final int[] MAX_ALMOST_EMPTY = {2, 3, 3, 4};
    /**
     * Defence ratios out of 1000.
     */
    private static final int[] MIN_DEFENCE_RATIO = {0, 0, 100, 200};
    private static final int[] MAX_DEFENCE_RATIO = {400, 300, 300, 400};
    private static final int[] MIN_DEFENCE_STEP = {8, 8, 24, 24};
    private static final int[] MAX_DEFENCE_STEP = {10, 10, 30, 30};

    private int me;
    private int who;

    //////////////////////////////////////////////////////////
    /**
     * The MoonDroid does a lot of enumerating of planets by different criteria.
     * Here are the filters for these criteria. The first finds planets that the
     * MoonDroid owns.
     */
    static class MyPlanetFilter extends PlanetFilter {

        /**
         * We need to know who we are.
         */
        protected int me;

        /**
         * Constructor takes a game state and the player number.
         */
        public MyPlanetFilter(GameState g, int me) {
            super(g);
            this.me = me;
        }

        /**
         * The compare method will return the higher ratio planet.
         */
        public int compare(int a, int b) {
            int aR = g.getPlanet(a).getRatio();
            int bR = g.getPlanet(b).getRatio();
            if (aR > bR) {
                return 1;
            } else if (aR < bR) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * A planet is valid if it is owned by us.
         */
        public boolean isValid(int p) {
            return (g.getPlanet(p).getOwner() == me);
        }
    }

    /**
     * This filter finds planets that are close to a point that are owned by us
     * and have available ships.
     */
    static class AvailableFilter extends PlanetFilter {

        /**
         * We need to know who we are.
         */
        protected int me;
        protected int x, y;
        protected int[] availableShips;

        /**
         * Constructor takes a game state and the player number.
         */
        public AvailableFilter(GameState g, int[] availableShips, int x, int y, int me) {
            super(g);
            this.availableShips = availableShips;
            this.x = x;
            this.y = y;
            this.me = me;
        }

        /**
         * Favours the planet that is closer to the point.
         */
        public int compare(int a, int b) {
            double aDistance = g.getDistance(a, x, y);
            double bDistance = g.getDistance(b, x, y);
            if (aDistance < bDistance) {
                return 1;
            } else if (bDistance < aDistance) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * A planet is valid if it is owned by us and has ships available.
         */
        public boolean isValid(int p) {
            return ((g.getPlanet(p).getOwner() == me) && (availableShips[p] > 0));
        }
    }

    /**
     * This filter finds the nearest unowned planets.
     */
    static class NearestEnemyFilter extends PlanetFilter {

        protected int x, y;
        protected PlanSet planSet;
        protected int me;

        /**
         * Consructor that takes a game state, a set of plans, a point and our
         * player number.
         */
        public NearestEnemyFilter(GameState g, PlanSet planSet, int x, int y, int me) {
            super(g);
            this.x = x;
            this.y = y;
            this.me = me;
            this.planSet = planSet;
        }

        /**
         * Constructor that takes a game state and a planet and our player
         * number.
         */
        public NearestEnemyFilter(GameState g, PlanSet planSet, Planet p, int me) {
            super(g);
            this.x = p.getX();
            this.y = p.getY();
            this.me = me;
            this.planSet = planSet;
        }

        /**
         * Favours the planet that is closer to the point, is neutral or has
         * high production.
         */
        public int compare(int a, int b) {
            double aDistance = g.getDistance(a, x, y);
            double bDistance = g.getDistance(b, x, y);
            // Account for production
            Planet aP = g.getPlanet(a);
            Planet bP = g.getPlanet(b);
            int aProd, bProd;
            if (aP.isNeutral()) {
                aProd = 10;
            } else {
                aProd = aP.getProduction();
            }
            if (bP.isNeutral()) {
                bProd = 10;
            } else {
                bProd = bP.getProduction();
            }
            aDistance /= (aProd / 4 + 1);
            bDistance /= (bProd / 4 + 1);
            if (aDistance < bDistance) {
                return 1;
            } else if (bDistance < aDistance) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * Valid if not owned by us and for which there is not already a plan.
         */
        public boolean isValid(int p) {
            return ((g.getPlanet(p).getOwner() != me) && !(g.getPlanet(p).isNeutral() && (planSet.hasPlan(p))));
        }
    }

    /**
     * This filter finds the nearest friendly planets.
     */
    static class NearestFriendlyFilter extends PlanetFilter {

        protected Planet p;
        protected PlanSet planSet;
        protected int me;

        /**
         * Constructor that takes a game state and a planet and our player
         * number.
         */
        public NearestFriendlyFilter(GameState g, PlanSet planSet, Planet p, int me) {
            super(g);
            this.p = p;
            this.me = me;
            this.planSet = planSet;
        }

        /**
         * Favours the planet that is closer to the point.
         */
        public int compare(int a, int b) {
            int x = p.getX();
            int y = p.getY();
            double aDistance = g.getDistance(a, x, y);
            double bDistance = g.getDistance(b, x, y);
            if (aDistance < bDistance) {
                return 1;
            } else if (bDistance < aDistance) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * Valid if owned by us and not the center planet.
         */
        public boolean isValid(int i) {
            return ((g.getPlanet(i).getOwner() == me) && (g.getPlanet(i) != p));
        }
    }

    /**
     * The MoonDroid keeps a list of active plans.
     */
    private PlanSet planSet = new PlanSet();

    /**
     * The MoonDroid keeps a mysterious defence factor.
     */
    private int defenceRatio;

    /**
     * The MoonDroid keeps track of all its ships in transit.
     */
    private int shipsInTransit = 0;

    /**
     * Checks for MoonDroid's arriving fleets.
     */
    private void checkArrivals(GameState g) {
        int n = g.getNumberOfArrivedFleets();
        for (int i = 0; i < n; i++) {
            ArrivedFleet f = g.getArrivedFleet(i);
            if (f.getOwner() == me) {
                shipsInTransit -= f.getShips();
            }
        }
    }

    /**
     * This method returns the name of the MoonDroid.
     */
    public String createName(int skillLevel) {
        return NAME[skillLevel];
    }

    /**
     * Called when a new game begins.
     */
    public void newGame(GameState game, int skillLevel) {
        // Keeps track of its player number (just for his convenience)
        me = getNumber();
        who = skillLevel;
        defenceRatio = MIN_DEFENCE_RATIO[who];
    }

    public int numberOfBots() {
        return 4;
    }
    //////////////////////////////////////////////////////////

    /**
     * Returns true if a coin flip biased accordingly comes up heads.
     */
    public final boolean percent(int prob) {
        if (random(0, 100) < prob) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This is the MoonDroid's patented semi-random selection method. Oh yeah!
     */
    public int selectPlanet(GameState game, PlanetFilter filter, int prob) {
        PlanetPicker picker = new PlanetPicker(game, filter);
        int candidate = -1;
        while (true) {
            int planet = picker.getNextPlanet();
            if (planet == -1) {
                return candidate;
            } else {
                candidate = planet;
            }
            if (percent(prob)) {
                return candidate;
            }
        }
    }

    /**
     * Override the send batch fleets function to keep track of fleets in
     * transit.
     */
    public void sendBatchFleets(Fleet[] fleet) {
        super.sendBatchFleets(fleet);
        for (int i = 0; i < fleet.length; i++) {
            shipsInTransit += fleet[i].ships;
        }
    }

    /**
     * Here's the MoonDroid in all its splendor.
     */
    public void update(GameState game, GameState oldState, Message[] message) {
        ///////////
        // Phase 1: Calculate global data.
        ///////////
        checkArrivals(game);
        MyPlanetFilter myFilter = new MyPlanetFilter(game, me);
        int totalShips = 0;
        int totalProd = 0;
        int middleX = 0, middleY = 0;
        int[] planet = myFilter.getValidPlanets();
        // Compute numbers for planets
        for (int i = 0; i < planet.length; i++) {
            Planet p = game.getPlanet(planet[i]);
            // Sum total ships on planets
            totalShips += p.getShips();
            // Total production.
            totalProd += p.getProduction();
            // Sum the co-ordinates of the planets
            middleX += p.getX();
            middleY += p.getY();
        }
        totalShips += shipsInTransit;
        if (planet.length > 0) {
            middleX /= planet.length;
            middleY /= planet.length;
        } else {
            totalProd = 1;
            totalShips = 1;
        }
        ///////////
        // Phase 2: Process each planet.
        ///////////
        int[] availableShips = new int[Constants.PLANETS];
        boolean[] mustEvacuate = new boolean[Constants.PLANETS];
        for (int i = 0; i < planet.length; i++) {
            mustEvacuate[planet[i]] = false;
            availableShips[planet[i]] = 0;
            Planet p = game.getPlanet(planet[i]);
            // First line of business is this: Are we getting booted out of this
            // planet? If so, then let's split now...
            // Also, let's not do anything on this planet if we are under survivable
            // attack.
            boolean usable = true;
            if (p.getTotalAttackers() > 0) {
                if (p.getTotalAttackers() * 100 / p.getShips() > LOSING_RATIO[who]) {
                    mustEvacuate[planet[i]] = true;
                    availableShips[planet[i]] = p.getShips();
                } else {
                    usable = false;
                }
            }
            if (usable) {
                // Find nearest unowned planet if neutral, otherwise something good that's within range.
                NearestEnemyFilter enemyFilter = new NearestEnemyFilter(game, planSet, p, me);
                int target = selectPlanet(game, enemyFilter, LOCALITY[who]);
                if (target != -1) {
                    Planet t = game.getPlanet(target);
                    // If the target is neutral, then try to attack.
                    if (t.getOwner() == Constants.NEUTRAL) {
                        int needed = NEUTRAL_MIN[who];
                        int avail;
                        avail = p.getShips() - planSet.getReservedShips(planet[i]);
                        if (mustEvacuate[planet[i]]) {
                            planSet.addPlan(new Plan(game, target, planet[i], availableShips[planet[i]]));
                            availableShips[planet[i]] = 0;
                        } else {
                            if (avail >= needed) {
                                planSet.addPlan(new Plan(game, target, planet[i], Math.max(avail, NEUTRAL_MAX[who])));
                            } else {
                                needed = 0;
                            }
                        }
                        // Make remaining ships (minus shock defence) available for offence.
                        if (!mustEvacuate[planet[i]]) {
                            int available = avail - needed - random(MIN_ALMOST_EMPTY[who], MAX_ALMOST_EMPTY[who]);
                            if (available > 0) {
                                availableShips[planet[i]] = available;
                            } else {
                                availableShips[planet[i]] = 0;
                            }
                        }
                    } else {
                        // Compute defence requirement
                        int defence;
                        if (p.getProduction() >= PRODUCTION_THRESHOLD[who]) {
                            int productionWeight = (p.getProduction());
                            defence = productionWeight * totalShips * defenceRatio / 1000 / totalProd;
                        } else {
                            defence = 0;
                        }
                        if (!mustEvacuate[planet[i]]) {
                            availableShips[planet[i]] = p.getShips() - defence - planSet.getReservedShips(planet[i]);
                        }
                        if (game.getDistance(i, target) <= CLOSE_THRESHOLD[who]) {
                            // Non-neutral. Attack only if it should be an easy kill.
                            int ratio = random(MIN_ATTACK_AMOUNT[who], MAX_ATTACK_AMOUNT[who]);
                            int ratioDifference = (t.getRatio()) * 100 / p.getRatio() - 100;
                            int needed = (ratio + ratioDifference) * t.getShips() / 100;
                            if (needed <= 0) {
                                needed = 1;
                            }
                            if (needed <= availableShips[planet[i]]) {
                                if (!mustEvacuate[planet[i]]) {
                                    planSet.addPlan(new Plan(game, target, planet[i], needed));
                                    availableShips[planet[i]] -= needed;
                                } else {
                                    planSet.addPlan(new Plan(game, target, planet[i], availableShips[planet[i]]));
                                    availableShips[planet[i]] = 0;
                                }
                            }
                        }
                    }
                }
            }
        }
///////////
// Phase 3: Reinforce weak planets.
///////////
// Only do this phase occasionally.
        if (percent(REINFORCE_CHANCE[who])) {
            for (int i = 0; i < planet.length; i++) {
                if (availableShips[planet[i]] < 0) {
                    // We'll also consider the ships that are destined for this planet in the defence requirements.
                    availableShips[planet[i]] += planSet.getDestinedShips(planet[i]);
                    if (availableShips[planet[i]] < 0) {
                        Planet p = game.getPlanet(planet[i]);
                        AvailableFilter availFilter = new AvailableFilter(game, availableShips, p.getX(), p.getY(), me);
                        PlanetPicker picker = new PlanetPicker(game, availFilter);
                        int source = picker.getNextPlanet();
                        // Send reinforcements.
                        Plan plan = new Plan(planet[i], 0);
                        planSet.addPlan(plan);
                        while ((source != -1) && (availableShips[planet[i]] < 0)) {
                            int shipsSent;
                            if (availableShips[source] + availableShips[planet[i]] >= 0) {
                                shipsSent = -availableShips[planet[i]];
                            } else {
                                shipsSent = availableShips[source];
                            }
                            planSet.addReservation(plan, source, shipsSent);
                            availableShips[source] -= shipsSent;
                            availableShips[planet[i]] += shipsSent;
                            source = picker.getNextPlanet();
                        }
                        // Adjust the deadline for these immediate fleets.
                        plan.adjustDeadline(game);
                    }
                }
            }
        }
///////////
// Phase 4: Pull a big fork, if possible.
///////////
// There's a chance we want to attack, and a chance we want to build a "God squad".
        if (random(0, 999) >= GOD_SQUAD[who]) {
            // Big attack
            // Find a good planet close to the center of our territory.
            NearestEnemyFilter enemyFilter = new NearestEnemyFilter(game, planSet, middleX, middleY, me);
            int target = selectPlanet(game, enemyFilter, TERRITORIALITY[who]);
            // Find total available ships remaining.
            int avail = 0;
            for (int i = 0; i < planet.length; i++) {
                avail += availableShips[planet[i]];
            }
            if (target != -1) {
                Planet p = game.getPlanet(target);
                int needed;
                if (p.getOwner() == Constants.NEUTRAL) {
                    needed = NEUTRAL_MIN[who];
                } else {
                    int ratio = MAX_ATTACK_AMOUNT[who];
                    needed = ratio * p.getShips() / 100;
                    // Account for production	  
                    needed += (int) (game.getDistance(target, middleX, middleY) * p.getProduction());
                    if (needed <= 0) {
                        needed = 1;
                    }
                }
                // Only try a big fork occasionally, unless it's a neutral
                if ((p.getOwner() == Constants.NEUTRAL) || percent(FORK_CHANCE[who])) {
                    if (avail >= needed) {
                        // System.out.println(getBotName() + ": planning to rock " + target + "(" + p.getShips() + ") with " + avail + ".");
                        AvailableFilter availFilter = new AvailableFilter(game, availableShips, p.getX(), p.getY(), me);
                        PlanetPicker picker = new PlanetPicker(game, availFilter);
                        int source = picker.getNextPlanet();
                        // Send attack fleets.
                        Plan plan = new Plan(target, 0);
                        planSet.addPlan(plan);
                        while ((needed > 0) && (source != -1)) {
                            int shipsSent;
                            if (availableShips[source] - needed >= 0) {
                                shipsSent = needed;
                            } else {
                                shipsSent = availableShips[source];
                            }
                            needed -= shipsSent;
                            planSet.addReservation(plan, source, shipsSent);
                            availableShips[source] -= shipsSent;
                            source = picker.getNextPlanet();
                        }
                        // Adjust the deadline for these immediate fleets.
                        plan.adjustDeadline(game);
                    }
                }
            }
        } else {
            // Send it all to the highest ratio planet.
            PlanetPicker pickBestRatio = new PlanetPicker(game, myFilter);
            int target = pickBestRatio.getNextPlanet();
            if (target != -1) {
                Planet p = game.getPlanet(target);
                AvailableFilter availFilter = new AvailableFilter(game, availableShips, p.getX(), p.getY(), me);
                PlanetPicker picker = new PlanetPicker(game, availFilter);
                int source = picker.getNextPlanet();
                // Send attack fleets.
                Plan plan = new Plan(target, 0);
                planSet.addPlan(plan);
                while (source != -1) {
                    if (source != target) {
                        int shipsSent = availableShips[source];
                        planSet.addReservation(plan, source, shipsSent);
                        availableShips[source] -= shipsSent;
                    }
                    source = picker.getNextPlanet();
                }
                // Adjust the deadline for these immediate fleets.
                plan.adjustDeadline(game);
            }
        }
///////////
// Phase 5: Deal with evacuations... send to nearest friendly planet.
///////////
        for (int i = 0; i < Constants.PLANETS; i++) {
            if ((mustEvacuate[i]) && (availableShips[i] > 0)) {
                Planet p = game.getPlanet(i);
                NearestFriendlyFilter friendlyFilter = new NearestFriendlyFilter(game, planSet, p, me);
                int target = selectPlanet(game, friendlyFilter, LOCALITY[who]);
                if (target != -1) {
                    planSet.addPlan(new Plan(game, target, i, availableShips[i]));
                    availableShips[i] = 0;
                }
            }
        }
        ///////////
        // Phase 6: Clean up and execute plans.
        ///////////
        // Increment defence counters.
        defenceRatio += random(MIN_DEFENCE_STEP[who], MAX_DEFENCE_STEP[who]);
        if (defenceRatio > MAX_DEFENCE_RATIO[who]) {
            defenceRatio = MIN_DEFENCE_RATIO[who];
        }
// Execute plans
        planSet.update(game, this);
    }
}
