package igx.bots;

import java.lang.*;
import java.lang.Math.*;
import java.util.*;

public class SquidContext extends Object {

    public static final int MAX_MESSAGES = 255;

    public Random generator;

    public int startTime = -99;

    public int NEUTRAL_MIN;
    public int NEUTRAL_MAX;
    public double ATTACK_MIN;
    public double ATTACK_MAX;
    public double SHORT_TERM_MIN_DEFENSE;
    public double SHORT_TERM_MAX_DEFENSE;
    public int THREAT_RADIUS;
    public int MAX_ENEMY_REINFORCE_RADIUS;
    public double MIN_DEFENSE;
    public double SHOCK_CHANCE;
    public int MAX_SHOCK;
    public int SHOCK_CONCERN_RADIUS;
    public int IMMEDIATE_THREAT_RADIUS;
    public int MIN_RATIO_TO_ATTACK;
    public double MIN_LIQUIDITY;

    public int me;
    public int nextProdTurn[];

    public int isShockingPlayer[];
    // I should be using booleans for these, but C habits die hard.
    public int playerShockTime[];

    public int isShockingPlanet[];
    public int shockDefNeeded[];

    public int isAttacking[];
    public int planetAttackTime[];

    public int liquidity[];
    public int shortTermAttack[];
    public boolean mustBail[];

    public int minAttackNeeded[];
    public int maxAttackNeeded[];

    public Vector enemyFleets;
    public Vector moves;

    public GameState gameHack;

    public Vector messages;

    public Vector getMessages() {
        Vector ret = messages;

        messages = new Vector();
        return ret;
    }

    public void debug(int code, String msg) {
//	  if ((code==2)||(code==3)||(code==4)||(code==5)||(code==7)||(code==8)||(code==9)) {
//    if (code==11) {
//    if ((gameHack != null) && (getTime(gameHack) == 199) && (code != 0)) {
//    if (code != 0) {
//      messages.addElement(msg);
//    }
    }

    public SquidContext() {
        super();
        int i;

        generator = new Random();

        isShockingPlayer = new int[Constants.MAXIMUM_PLAYERS];
        playerShockTime = new int[Constants.MAXIMUM_PLAYERS];
        nextProdTurn = new int[Constants.PLANETS];
        isShockingPlanet = new int[Constants.PLANETS];
        isAttacking = new int[Constants.PLANETS];
        planetAttackTime = new int[Constants.PLANETS];
        shockDefNeeded = new int[Constants.PLANETS];
        liquidity = new int[Constants.PLANETS];
        mustBail = new boolean[Constants.PLANETS];
        shortTermAttack = new int[Constants.PLANETS];
        minAttackNeeded = new int[Constants.PLANETS];
        maxAttackNeeded = new int[Constants.PLANETS];

        enemyFleets = new Vector();
        messages = new Vector();
        moves = new Vector();

        for (i = 0; i < Constants.PLANETS; i++) {
            nextProdTurn[i] = -1;
            isShockingPlanet[i] = 0;

            isAttacking[i] = 0;
            planetAttackTime[i] = -1;
        }
        for (i = 0; i < Constants.MAXIMUM_PLAYERS; i++) {
            isShockingPlayer[i] = 0;
            playerShockTime[i] = 0;
        }
    }

    public void initConstants(int plNum, int skillLevel) {
        me = plNum;
        switch (skillLevel) {
            case 0:
                NEUTRAL_MIN = 9;
                NEUTRAL_MAX = 30;
                ATTACK_MIN = 1.4;
                ATTACK_MAX = 1.9;
                SHORT_TERM_MIN_DEFENSE = 1;
                SHORT_TERM_MAX_DEFENSE = 1.5;
                MAX_ENEMY_REINFORCE_RADIUS = 60;
                THREAT_RADIUS = 29;
                MIN_DEFENSE = 0.9;
                SHOCK_CHANCE = 0.02;
                MAX_SHOCK = 10;
                SHOCK_CONCERN_RADIUS = 20;
                IMMEDIATE_THREAT_RADIUS = 3;
                MIN_RATIO_TO_ATTACK = 15;
                MIN_LIQUIDITY = 0.20;
                break;
            case 1:
                NEUTRAL_MIN = 13;
                NEUTRAL_MAX = 30;
                ATTACK_MIN = 1.4;
                ATTACK_MAX = 1.9;
                SHORT_TERM_MIN_DEFENSE = 1;
                SHORT_TERM_MAX_DEFENSE = 1.5;
                THREAT_RADIUS = 29;
                MAX_ENEMY_REINFORCE_RADIUS = 60;
                MIN_DEFENSE = 0.9;
                SHOCK_CHANCE = 0.02;
                MAX_SHOCK = 10;
                SHOCK_CONCERN_RADIUS = 20;
                IMMEDIATE_THREAT_RADIUS = 3;
                MIN_RATIO_TO_ATTACK = 15;
                MIN_LIQUIDITY = 0.20;
                break;
            case 2:
                NEUTRAL_MIN = 13;
                NEUTRAL_MAX = 30;
                ATTACK_MIN = 1.4;
                ATTACK_MAX = 1.9;
                SHORT_TERM_MIN_DEFENSE = 1;
                SHORT_TERM_MAX_DEFENSE = 1.5;
                THREAT_RADIUS = 29;
                MAX_ENEMY_REINFORCE_RADIUS = 60;
                MIN_DEFENSE = 0.9;
                SHOCK_CHANCE = 0.02;
                MAX_SHOCK = 10;
                SHOCK_CONCERN_RADIUS = 20;
                IMMEDIATE_THREAT_RADIUS = 3;
                MIN_RATIO_TO_ATTACK = 15;
                MIN_LIQUIDITY = 0.20;
                break;
        }
    }

//***********
// Utilities and metrics and stuff. 
    public boolean testProbability(double prob) {
        // Returns true with the given probility
        int num;
        boolean ret = false;

        num = Bot.random(0, 999);
        if (((double) num) / 1000 < prob) {
            ret = true;
        }
        return ret;
    }

    public int getTime(GameState game) {
        int tim;

        tim = game.getTime();
        if (startTime == -99) {
            startTime = tim;
        }

        return tim - startTime;
    }

    public int timeDistanceBetweenPlanets(GameState game, int pl1, int pl2) {
        return (int) (game.getDistance(pl1, pl2) * 10 + 0.999);
    }

    public int etaBetweenPlanets(GameState game, int pl1, int pl2) {
        return getTime(game) + timeDistanceBetweenPlanets(game, pl1, pl2);
    }

    public int closestEnemy(GameState game, int pl) {
        int i;
        int minDist = -1;
        int ret = -1;

        for (i = 0; i < Constants.PLANETS; i++) {
            int ownr = game.getPlanet(i).getOwner();
            if ((ownr != me) && (ownr != Constants.NEUTRAL)
                    && (i != pl)) {
                int dist = (int) (game.getDistance(pl, i) + 0.999);
                if ((dist < minDist) || (minDist == -1)) {
                    ret = i;
                    minDist = dist;
                }
            }
        }

        return ret;
    }

    public Vector closestNPlanetsBelongingToPlayer(GameState game, int n,
            int player, int sourcePl) {
        // if player is -1, returns closest enemy or neutral planets.
        // Ignore black holes, since there's "nothing there"
        Vector ret = new Vector();
        int i, j;
        int plDists[] = new int[Constants.PLANETS];

        for (i = 0; i < Constants.PLANETS; i++) {
            if ((!game.getPlanet(i).isBlackHole())
                    && (i != sourcePl)
                    && ((game.getPlanet(i).getOwner() == player)
                    || ((player == -1) && (game.getPlanet(i).getOwner() != me)))) {
                plDists[i] = timeDistanceBetweenPlanets(game, sourcePl, i);
            } else {
                plDists[i] = -1;
            }
        }

        for (i = 0; i < n; i++) {
            int closestDist = (Constants.MAP_WIDTH + Constants.MAP_HEIGHT) * 10;
            int closestPlan = -1;

            for (j = 0; j < Constants.PLANETS; j++) {
                if ((plDists[j] != -1) && (plDists[j] < closestDist)) {
                    closestDist = plDists[j];
                    closestPlan = j;
                }
            }

            if (closestPlan != -1) {
                ret.addElement(new Integer(closestPlan));
                plDists[closestPlan] = -1;
            }
        }

        return ret;
    }

    public int closestPlanetBelongingToPlayer(GameState game, int player, int sourcePl) {
        double minDist = (Constants.MAP_WIDTH + Constants.MAP_HEIGHT) * 10;
        int pl;
        int closest = -1;

        for (pl = 0; pl < Constants.PLANETS; pl++) {
            if ((game.getPlanet(pl).getOwner() == player)
                    && (game.getDistance(pl, sourcePl) < minDist)
                    && (pl != sourcePl)) {
                closest = pl;
                minDist = game.getDistance(pl, sourcePl);
            }
        }

        return closest;
    }

    public int farthestPlanetBelongingToPlayer(GameState game, int player, int sourcePl) {
        double maxDist = -1;
        int pl;
        int farthest = -1;

        for (pl = 0; pl < Constants.PLANETS; pl++) {
            if ((game.getPlanet(pl).getOwner() == player)
                    && (game.getDistance(pl, sourcePl) > maxDist)
                    && (pl != sourcePl)) {
                farthest = pl;
                maxDist = game.getDistance(pl, sourcePl);
            }
        }

        return farthest;
    }

    public int aPlanetBelongingToPlayer(GameState game, int player) {
        int retPl = -1;
        int i;

        for (i = 0; i < Constants.PLANETS; i++) {
            if (game.getPlanet(i).getOwner() == player) {
                retPl = i;
                break;
            }
        }

        return retPl;
    }

    public int planetWillProduceInTime(GameState game, int pl, int tim) {
        int turnCount = nextProdTurn[pl];
        int timeTill = tim + getTime(game);
        int ret = 0;

        while (turnCount <= timeTill) {
            ret += game.getPlanet(pl).getProduction();
            turnCount += Constants.SEGMENTS;
        }

        return ret;
    }

//***********************************
    public Vector getMovesThisTurn(GameState game) {
        Vector ret = new Vector();
        Enumeration e;
        SquidMove curMove;
        int tim = getTime(game);

        e = moves.elements();
        while (e.hasMoreElements()) {
            curMove = (SquidMove) e.nextElement();
            if (curMove.launchTime <= tim) {
                ret.addElement(curMove);
            }
        }

        e = ret.elements();
        while (e.hasMoreElements()) {
            curMove = (SquidMove) e.nextElement();
            moves.removeElement(curMove);
        }

        return ret;
    }

    public void processFleets(GameState game, GameState prevGame) {
        int i;
        int time;
        Vector fleetsToRemove;
        Enumeration e;
        SquidFleet curEnemyFleet;
        int numFleets;

        time = getTime(game);

        // process planets to determine deltas
        for (i = 0; i < Constants.PLANETS; i++) {
            int owner;
            int oldOwner;
            Planet p;
            Planet oldP;
            int shipsThatShouldBeThere;
            int delta;

            oldP = prevGame.getPlanet(i);
            p = game.getPlanet(i);
            owner = p.getOwner();
            oldOwner = oldP.getOwner();
            shipsThatShouldBeThere = oldP.getShips();
            if ((nextProdTurn[i] == -1) && (p.getOwner() != Constants.NEUTRAL)) {
                // Planet was taken
                nextProdTurn[i] = time + Constants.SEGMENTS;
                if (time > 0) {
                    nextProdTurn[i] -= 1;
                    // This is a hack to make the bots view of production consistent with what
                    // happens in the actual game.
                }
                shipsThatShouldBeThere = p.getShips();
                debug(1, "Planet captured: " + i + " next prod: " + nextProdTurn[i]);
            } else if ((nextProdTurn[i] != -1) && (nextProdTurn[i] <= time)) {

                nextProdTurn[i] += Constants.SEGMENTS;
//				debug(1, "nextProdTurn for " + i + " is now " + nextProdTurn[i]);
                shipsThatShouldBeThere += p.getProduction();
            }
            if ((owner == oldOwner) && (owner != me) && (p.getTotalAttackers() == 0)) {
                delta = shipsThatShouldBeThere - p.getShips();
            } else if ((owner != oldOwner) && (oldOwner != me) && (oldOwner != Constants.NEUTRAL)) {
                delta = oldP.getShips();
            } else {
                delta = 0;
            }

            if (delta > 0) {
                SquidFleet newFleet = new SquidFleet(oldOwner, i, delta, time - 1);
                debug(1, "Enemy fleet: " + newFleet);
                enemyFleets.addElement(newFleet);
            }
            if (delta < 0) {
                debug(1, "Negative delta at: " + i);
            }
        }

        // process arrived fleets
        numFleets = game.getNumberOfArrivedFleets();
        fleetsToRemove = new Vector();
        for (i = 0; i < numFleets; i++) {
            ArrivedFleet curArrivedFleet;
            int owner;
            int removedOne = 0;

            curArrivedFleet = game.getArrivedFleet(i);
            debug(1, "Arrived fleet: " + curArrivedFleet);
            owner = curArrivedFleet.getOwner();

            if (owner != me) {
                e = enemyFleets.elements();
                while (e.hasMoreElements()) {
                    curEnemyFleet = (SquidFleet) e.nextElement();
                    if ((curEnemyFleet.matches(game, time, curArrivedFleet))
                            || (curEnemyFleet.outOfRange(time))) {
                        fleetsToRemove.addElement(curEnemyFleet);
                        removedOne = 1;
                        if (curEnemyFleet.outOfRange(time)) {
                            debug(1, "Expiring fleet: " + curEnemyFleet);
                        } else {
                            debug(1, "Removing fleet: " + curEnemyFleet);
                        }
                    }
                }
                if (removedOne == 0) {
                    debug(1, "Didn't find a match: fleets = " + enemyFleets);
                }
            }
        }

        // Wipe out fleets that have arrived or are out of range
        e = fleetsToRemove.elements();
        while (e.hasMoreElements()) {
            curEnemyFleet = (SquidFleet) e.nextElement();
            enemyFleets.removeElement(curEnemyFleet);
        }
    }

    public int threatFromEnemyPlanets(GameState game, int pl, int player, int maxDist,
            int scaleDownFactor) {
        int curPl;
        int enemyThreat = 0;
        int i;

        for (i = 0; i < Constants.PLANETS; i++) {
            int ownr = game.getPlanet(i).getOwner();
            int dist = timeDistanceBetweenPlanets(game, pl, i);
            if ((ownr == player) && (dist <= maxDist)) {
                int threatFromPlanet = game.getPlanet(i).getShips() - scaleDownFactor
                        * (dist / Constants.SEGMENTS);
                if (threatFromPlanet > 0) {
                    enemyThreat += threatFromPlanet;
                }
            }
        }

        return enemyThreat;
    }

    public int threatFromEnemyPlanets(GameState game, int pl, int maxDist,
            int scaleDownFactor) {
        int curPl;
        int enemyThreat[];
        int i;
        int maxEnemyThreat;

        enemyThreat = new int[Constants.MAXIMUM_PLAYERS];
        for (i = 0; i < Constants.MAXIMUM_PLAYERS; i++) {
            enemyThreat[i] = 0;
        }
        maxEnemyThreat = 0;

        for (i = 0; i < Constants.PLANETS; i++) {
            int ownr = game.getPlanet(i).getOwner();
            int dist = timeDistanceBetweenPlanets(game, pl, i);

            if ((ownr != me) && (ownr != Constants.NEUTRAL) && (dist <= maxDist)) {
                int threatFromPlanet = game.getPlanet(i).getShips() - scaleDownFactor
                        * (dist / Constants.SEGMENTS);
                if (threatFromPlanet > 0) {
                    enemyThreat[ownr] += threatFromPlanet;
                }
                if (enemyThreat[ownr] > maxEnemyThreat) {
                    maxEnemyThreat = enemyThreat[ownr];
                }
            }
        }

        return maxEnemyThreat;
    }

    public int threatFromEnemyFleets(GameState game, int pl, int maxDist, int maxSize,
            int scaleDownFactor) {
        int retShips = 0;
        Enumeration e;
        SquidFleet curEnemyFleet;

        e = enemyFleets.elements();
        while (e.hasMoreElements()) {
            curEnemyFleet = (SquidFleet) e.nextElement();
            if (((maxSize == -1) || (curEnemyFleet.ships <= maxSize))
                    && (curEnemyFleet.source != pl)) {
                int dist;

                dist = curEnemyFleet.distToPlanet(game, getTime(game), pl);
                if ((dist > 0) && (dist <= maxDist)) {
                    int threatFromFleet = curEnemyFleet.ships - scaleDownFactor
                            * (dist / Constants.SEGMENTS);
                    if (threatFromFleet > 0) {
                        retShips += threatFromFleet;
                    }
                }
            }
        }

        return retShips;
    }

    public void processPlanets(GameState game, GameState prevGame) {
        int i, j;
        Enumeration e;
        SquidMove curMove;

        for (i = 0; i < Constants.PLANETS; i++) {
            Planet curPlanet = game.getPlanet(i);
            int ownr = curPlanet.getOwner();

            // First, if we just completed a plan, then reset isAttacking, so we can
            // attack again if needed
            if ((isAttacking[i] == 1) && (getTime(game) > planetAttackTime[i])) {
                debug(5, "Completed the plan to: " + Planet.planetChar(i));
                isAttacking[i] = 0;
                planetAttackTime[i] = -1;
            }

            if (ownr == me) {
//			  int minLiquid;
                int reallyDangerousFleets;
                // Check for immediate bail necessity
                mustBail[i] = false;
                reallyDangerousFleets = threatFromEnemyFleets(game, i, IMMEDIATE_THREAT_RADIUS, -1, 0);
                if (reallyDangerousFleets > curPlanet.getShips()) {
                    debug(3, "Dangerous fleet (size " + reallyDangerousFleets + ") is coming to "
                            + "my planet " + Planet.planetChar(i) + " next turn (maybe)");
                    mustBail[i] = true;
                }
                if (curPlanet.getTotalAttackers() > curPlanet.getShips()) {
                    mustBail[i] = true;
                }

                liquidity[i] = curPlanet.getShips();
//			  minLiquid = (int)(MIN_LIQUIDITY*curPlanet.getShips());
                // Check for shock fleets threating the planet
                shortTermAttack[i] = threatFromEnemyFleets(game, i, SHOCK_CONCERN_RADIUS, -1, 0);
                shortTermAttack[i] += threatFromEnemyPlanets(game, i, SHOCK_CONCERN_RADIUS, 0);

                // For purposes of shock defense, ignore big fleets.
                if ((shortTermAttack[i] > 0) && (shortTermAttack[i] <= MAX_SHOCK)) {
                    shockDefNeeded[i] = shortTermAttack[i] + 1;
                    liquidity[i] -= shockDefNeeded[i];
                    debug(2, "Shock defence needed at " + i + " : " + shockDefNeeded[i] + " liquidity: " + liquidity[i]);
                } else {
                    int shortTermDefNeeded;
                    int shortTermDefMax;

                    shockDefNeeded[i] = 0;
                    // If we have enough to defend a short term threat, make sure we don't
                    // leave the planet
                    shortTermDefNeeded = (int) (SHORT_TERM_MIN_DEFENSE * shortTermAttack[i]);
                    shortTermDefMax = (int) (SHORT_TERM_MAX_DEFENSE * shortTermAttack[i]);

                    if (liquidity[i] >= shortTermDefNeeded) {
                        liquidity[i] -= shortTermDefMax;
                    }
                    debug(2, "Planet: " + Planet.planetChar(i) + " Short term defense needed: " + shortTermDefNeeded
                            + ", but would defend with up to " + shortTermDefMax + " liquidity: " + liquidity[i]);
                }

//				if (liquidity[i] < minLiquid) {
//				  liquidity[i] = minLiquid;
//				}
            } else if (ownr != Constants.NEUTRAL) {
                // determine shockability of enemy planets
                if ((curPlanet.getShips() <= 1) && (curPlanet.getProduction() > 0)
                        && (isShockingPlayer[ownr] == 0)) {
                    // We see that an enemy may be vulnerable to a shock.
                    if (testProbability(SHOCK_CHANCE)) {
                        isShockingPlayer[ownr] = 1;
                        playerShockTime[ownr] = -1;
                        debug(2, "I'm shocking player " + ownr);
                    }
                }
            }
        }

        // Now reduce liquidity for planets engaged in "plans"
        e = moves.elements();
        while (e.hasMoreElements()) {
            curMove = (SquidMove) e.nextElement();
            liquidity[curMove.source] -= curMove.ships;
        }

    }

    public void turnInit(GameState game, GameState prevGame) {
        gameHack = game;
        processFleets(game, prevGame);
        processPlanets(game, prevGame);
    }

    public void launchPlayerShocks(GameState game) {
        int i, j, k;

        for (i = 1; i < Constants.MAXIMUM_PLAYERS; i++) {
            if (isShockingPlayer[i] == 1) {
                if (playerShockTime[i] == -1) {
                    int aPlanetOfMine;
                    int farthestPlan;

                    aPlanetOfMine = aPlanetBelongingToPlayer(game, me);
                    if (aPlanetOfMine != -1) {
                        farthestPlan = farthestPlanetBelongingToPlayer(game, i, aPlanetOfMine);
                        playerShockTime[i] = etaBetweenPlanets(game, aPlanetOfMine, farthestPlan);
                        for (j = 0; j < Constants.PLANETS; j++) {
                            if (game.getPlanet(j).getOwner() == i) {
                                isShockingPlanet[j] = 0;
                            }
                        }
                    }
                }
                for (j = 0; j < Constants.PLANETS; j++) {
                    if ((game.getPlanet(j).getOwner() == i) && (isShockingPlanet[j] == 0)) {
                        for (k = 0; k < Constants.PLANETS; k++) {
                            if ((game.getPlanet(k).getOwner() == me)
                                    && (etaBetweenPlanets(game, j, k) == playerShockTime[i])
                                    && (liquidity[k] > 0)) {
                                moves.addElement(new SquidMove(k, j, 1, getTime(game)));
                                debug(2, "Sending a shock fleet from " + Planet.planetChar(k) + " to "
                                        + Planet.planetChar(j));
                                isShockingPlanet[j] = 1;
                            }
                        }
                    }
                }

                if (getTime(game) >= playerShockTime[i]) {
                    isShockingPlayer[i] = 0;
                }
            }
        }
    }

    public void firstTurnSpray(GameState game) {
        // for skill 2, send 5 shock troops on turn zero
        Vector closest6;
        int home;
        int closest;
        Enumeration e;
        int curTarget;

        home = aPlanetBelongingToPlayer(game, me);
        if (home != -1) {
            //Would be awfully unfortunately if we didn't have planet on turn 0, but
            //why take chances with an exception toss??
            closest = ((Integer) closestNPlanetsBelongingToPlayer(
                    game, 1, -1, home).lastElement()).intValue();
            closest6 = closestNPlanetsBelongingToPlayer(game, 6, -1, home);
            e = closest6.elements();
            while (e.hasMoreElements()) {
                curTarget = ((Integer) e.nextElement()).intValue();
                if (curTarget != closest) {
                    moves.addElement(new SquidMove(home, curTarget, 1, 0));
                }
            }
        }
    }

    public void bailout(GameState game) {
        int i;

        for (i = 0; i < Constants.PLANETS; i++) {
            if ((game.getPlanet(i).getOwner() == me) && (mustBail[i])
                    && (game.getPlanet(i).getShips() > 0)) {
                int dest = closestPlanetBelongingToPlayer(game, i, me);

                if (dest != -1) {
                    SquidMove newMove = new SquidMove(i, dest, game.getPlanet(i).getShips(),
                            getTime(game));
                    debug(10, "Bailing out: " + newMove);
                    moves.addElement(newMove);
                }
            }
        }
    }

    public void shockHomesEarly(GameState game, boolean onlyOne) {
        int tim = getTime(game);
        int pl;
        int home = aPlanetBelongingToPlayer(game, me);

        if ((home != -1) && ((tim == 0) || (tim == 20))) {
            // 0.0:  
            // If dist = 40, send shock immediately.
            // If dist from 60-79, send shock to arrive at 3:19
            // If dist from 100-119, sind shock to arrive at 5:19

            // 1.0:
            // If dist from 41-59, send shock to arrive at 3:19
            // If dist from 80-99, send shock to arrive at 5:19
            for (pl = Constants.PLANETS - 1; pl >= 0; pl--) {
                //start from the top, so if we send a limited number, we will send to the last player, who
                //is more likely to be a bot than a human.
                int ownr = game.getPlanet(pl).getOwner();
                if ((ownr != me) && (ownr != Constants.NEUTRAL)) {
                    int dist = timeDistanceBetweenPlanets(game, home, pl);
                    int timeToSend = -1;

                    if (tim == 0) {
                        if (dist == 40) {
                            timeToSend = 0;
                        } else if ((60 <= dist) && (dist <= 79)) {
                            timeToSend = 79 - dist;
                            // Arrive at 3:19
                        } else if ((100 <= dist) && (dist <= 119)) {
                            timeToSend = 119 - dist;
                            // Arrive at 5:19
                        }
                    } else if (tim == 20) {
                        if ((41 <= dist) && (dist <= 59)) {
                            timeToSend = 79 - dist;
                            // Arrive at 3:19
                        } else if ((80 <= dist) && (dist <= 99)) {
                            timeToSend = 119 - dist;
                            // Arrive at 5:19
                        }
                    }

                    if (timeToSend != -1) {
                        SquidMove newMove = new SquidMove(home, pl, 1, timeToSend);
                        moves.addElement(newMove);
                        debug(11, "Doing a basterd shock: " + newMove + " (eta = " + (timeToSend + dist) + ")");
                        if (onlyOne) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
