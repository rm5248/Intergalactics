package igx.bots.smart;

import igx.bots.*;
import java.util.*;

// BorgBot.java
/**
 * @author John Watkinson
 */
public class BorgBot extends Bot {
    //////////////////////////////////////////////////////////
    // CONSTANTS
    //////////////////////////////////////////////////////////

    /**
     * Number of different bots.
     */
    private static final int NUMBER_OF_BOTS = 9;

    /**
     * Names of the bots.
     */
    private static final String[] NAME
            = {"1of9", "2of9", "3of9",
                "4of9", "5of9", "6of9",
                "7of9", "8of9", "9of9"};

    private static final int[] NEUTRAL_MIN
            = {20, 15, 10,
                18, 14, 10,
                15, 12, 8};
    private static final int[] NEUTRAL_MAX
            = {20, 20, 20,
                20, 19, 18,
                20, 15, 14};
    private static final int[] CLOSE_THRESHOLD
            = {1, 3, 5,
                2, 4, 7,
                3, 5, 10};
    private static final int[] FORK_CHANCE
            = {2, 5, 10,
                3, 8, 15,
                5, 10, 25};
    private static final int[] MIN_ATTACK_AMOUNT
            = {300, 250, 180,
                250, 220, 180,
                200, 180, 150};
    private static final int[] MAX_ATTACK_AMOUNT
            = {400, 300, 240,
                330, 260, 230,
                250, 230, 200};
    private static final int[] PRODUCTION_THRESHOLD
            = {1, 2, 3,
                2, 2, 3,
                2, 3, 5};
    private static final int[] LOCALITY
            = {98, 90, 80,
                95, 90, 80,
                90, 80, 50};
    private static final int[] TERRITORIALITY
            = {98, 90, 80,
                95, 90, 80,
                90, 80, 50};
    private static final int[] CLUSTER_SIZE
            = {3, 2, 1,
                5, 8, 5,
                4, 3, 5};
    private static final int[] LOSING_RATIO
            = {130, 150, 150,
                150, 150, 150,
                150, 160, 200};
    private static final int[] REINFORCE_CHANCE
            = {5, 0, 0,
                5, 0, 0,
                0, 0, 0};
    /**
     * This stat is out of 1000.
     */
    private static final int[] GOD_SQUAD
            = {20, 0, 0,
                0, 0, 0,
                0, 0, 0};
    private static final int[] MIN_ALMOST_EMPTY
            = {2, 1, 1,
                2, 1, 1,
                1, 1, 1};
    private static final int[] MAX_ALMOST_EMPTY
            = {5, 3, 2,
                4, 3, 2,
                3, 2, 1};
    /**
     * Defence ratios out of 1000.
     */
    private static final int[] MIN_DEFENCE_RATIO
            = {200, 100, 50,
                150, 100, 0,
                100, 80, 0};
    private static final int[] MAX_DEFENCE_RATIO
            = {500, 300, 150,
                350, 200, 0,
                300, 180, 0};
    /**
     * Note: the BorgBots are in defensive lock-step. Eerie!
     */
    private static final int[] MIN_DEFENCE_STEP
            = {3, 2, 1,
                3, 1, 0,
                2, 1, 0};
    private static final int[] MAX_DEFENCE_STEP
            = {3, 2, 1,
                3, 1, 0,
                2, 1, 0};

    //////////////////////////////////////////////////////////
    // FIELDS
    //////////////////////////////////////////////////////////
    private int me;
    private int who;

    private boolean[] borg;

    //////////////////////////////////////////////////////////
    // FILTERS
    //////////////////////////////////////////////////////////
    /**
     * The BorgBot does a lot of enumerating of planets by different criteria.
     * Here are the filters for these criteria. The first finds planets that the
     * BorgBot owns.
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
     * Finds a nearby offencive planet filter.
     */
    static class OffenceFilter extends PlanetFilter {

        /**
         * We need to know who we are.
         */
        protected int me;

        protected int threshold;

        /**
         * The target planet.
         */
        protected Planet target;
        protected int targetNum;

        /**
         * Constructor takes a game state and the player number.
         */
        public OffenceFilter(GameState g, int me, int p) {
            super(g);
            this.me = me;
            this.target = target;
            threshold = CLUSTER_SIZE[me] * CLUSTER_SIZE[me];
            targetNum = p;
            target = g.getPlanet(p);
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
         * A planet is valid if it is owned by us and
         */
        public boolean isValid(int p) {
            Planet planet = g.getPlanet(p);
            double distance
                    = (planet.getX() - target.getX()) * (planet.getX() - target.getX())
                    + (planet.getY() - target.getY()) * (planet.getY() - target.getY());
            return ((g.getPlanet(p).getOwner() == me) && (targetNum != p) && (distance <= threshold));
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
    class NearestEnemyFilter extends PlanetFilter {

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
            return ((g.getPlanet(p).isNeutral() || !borg[g.getPlanet(p).getOwner()]) && !planSet.hasPlan(p));

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
     * The BorgBot keeps a list of active plans.
     */
    private PlanSet planSet = new PlanSet();

    /**
     * The BorgBot keeps a mysterious defence factor.
     */
    private int defenceRatio;

    /**
     * The BorgBot keeps track of all its ships in transit.
     */
    private int shipsInTransit = 0;

    /**
     * Checks for BorgBot's arriving fleets.
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
     * This method returns the name of the BorgBot.
     */
    public String createName(int skillLevel) {
        return NAME[skillLevel];
    }

    /**
     * Called when a new game begins.
     */
    public void newGame(GameState game, int skillLevel) {
        // Keeps track of its player number (just for its convenience)
        me = getNumber();
        who = skillLevel;
        defenceRatio = MIN_DEFENCE_RATIO[who];
        // Determine other borg bots      
        int n = game.getNumberOfPlayers();
        borg = new boolean[n];
        for (int i = 0; i < n; i++) {
            borg[i] = false;
            String name = game.getPlayer(i).getName();
            for (int j = 0; j < NUMBER_OF_BOTS; j++) {
                if (name.equals(NAME[j])) {
                    borg[i] = true;
                    break;
                }
            }
        }
    }

    public int numberOfBots() {
        return NUMBER_OF_BOTS;
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
     * This is the BorgBot's patented semi-random selection method. Oh yeah!
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

    protected int getBorgAttackers(GameState g, Planet p) {
        int n = g.getNumberOfPlayers();
        int total = 0;
        for (int i = 0; i < n; i++) {
            if (borg[i]) {
                total += p.getAttackers(i);
            }
        }
        return total;
    }

    /**
     * Here is the cruel heart of the BorgBot.
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
            int totalAttackers = p.getTotalAttackers();
            if (totalAttackers > 0) {
                int borgShips = getBorgAttackers(game, p);
                if ((totalAttackers * 100 / p.getShips() > LOSING_RATIO[who]) || (totalAttackers < borgShips * 2)) {
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
                            } else // Occasionally reinforce the cluster
                            if (percent(REINFORCE_CHANCE[who])) {
                                OffenceFilter offFilter = new OffenceFilter(game, who, i);
                                int cluster = selectPlanet(game, offFilter, 100);
                                if (cluster != -1) {
                                    planSet.addPlan(new Plan(game, cluster, planet[i], availableShips[planet[i]]));
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
        /*
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
                     if (availableShips[source] + availableShips[planet[i]] >= 0)
                        shipsSent = -availableShips[planet[i]];
                     else
                        shipsSent = availableShips[source];
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
         */
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
