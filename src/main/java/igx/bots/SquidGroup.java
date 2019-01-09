package igx.bots;

import java.lang.*;
import java.util.*;

public class SquidGroup extends Object {

    public static final int RADIUS = 19;
    public static final int ENEMY_TARGETS_TO_LOOK_AT = 8;
    public static final double CHANCE_TO_SKIP_TO_NEXT_TARGET = 0.20;
    public static final int TIME_TO_START_RANDOM = 41;
    public static final int FLEET_THREAT_DETERIORATION_WITH_DISTANCE = 10;
    public static final int PLANET_THREAT_DETERIORATION_WITH_DISTANCE = 10;
    public static final int PRODUCTION_TO_BE_VALUABLE = 5;
    public static final double REDUCTION_FOR_CHEAP_PLANETS = 0.4;
    public static final int MIN_ATTACK_SIZE = 5;
    public static final int MIN_REINFORCE_SIZE = 20;
    public static final int[] MAX_DEFENSE_BY_PRODUCTION
            = {0, 1, 1, 10, 20, 40, 50, 80, 100, 150, 250, 300, 500, 1000, 1000, 1000};
    // This is for a little randomness, so we won't always attack the closest target.

    public int id;
    public int home;
    public Vector members;

    public int threat;
    public int defNeed;
    public int totalAvailable;
    public boolean madeAnAttack;
    public int totalShips;
    public int totalProd;
    public int maxDefence;
    public int nPlanets;

    public SquidGroup() {
        super();

        totalProd = 0;
        totalShips = 0;
        maxDefence = 0;
        nPlanets = 0;
        members = new Vector();
    }

    public void addPlanet(GameState game, int pl) {
        members.addElement(new Integer(pl));
        totalProd += game.getPlanet(pl).getProduction();
        totalShips += game.getPlanet(pl).getShips();
        maxDefence += MAX_DEFENSE_BY_PRODUCTION[game.getPlanet(pl).getProduction()];
        nPlanets++;
    }

    public String toString() {
        Enumeration e;
        String ret = "Group of planets ( ";
        int curPlan;

        e = members.elements();
        while (e.hasMoreElements()) {
            curPlan = ((Integer) e.nextElement()).intValue();
            ret += Planet.planetChar(curPlan) + " ";
        }
        ret += ")";

        return ret;
    }

    public static int bestRatioPlanetOfMine(SquidContext context, GameState game,
            SquidGroup[] groupForPlanet) {
        int maxRatio = 0;
        int bestPlanet = -1;
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            int ratio = p.getRatio();
            if ((groupForPlanet[i] == null)
                    && (p.getOwner() == context.me)
                    && (ratio > maxRatio)) {
                maxRatio = ratio;
                bestPlanet = i;
            }
        }
        return bestPlanet;
    }

    public static Vector reformGroups(SquidContext context, GameState game,
            SquidGroup[] groupForPlanet) {
        int i;
        int curHome;
        Vector retGroups;

        retGroups = new Vector();

        for (i = 0; i < Constants.PLANETS; i++) {
            groupForPlanet[i] = null;
        }
        while ((curHome = bestRatioPlanetOfMine(context, game, groupForPlanet)) != -1) {
            SquidGroup newGroup;

            newGroup = new SquidGroup();
            groupForPlanet[curHome] = newGroup;
            newGroup.home = curHome;
            newGroup.addPlanet(game, curHome);

            for (i = 0; i < Constants.PLANETS; i++) {
                Planet curPlanet;

                curPlanet = game.getPlanet(i);
                if ((i != curHome) && (curPlanet.getOwner() == context.me)) {
                    context.debug(0, "Seeing if planet " + Planet.planetChar(i) + " belongs in the "
                            + "group with home: " + Planet.planetChar(curHome));
                    context.debug(0, "distance is " + context.timeDistanceBetweenPlanets(game, i, curHome));
                }
                if ((i != curHome) && (curPlanet.getOwner() == context.me)
                        && (groupForPlanet[i] == null)
                        && (context.timeDistanceBetweenPlanets(game, i, curHome) < RADIUS)) {
                    groupForPlanet[i] = newGroup;
                    newGroup.addPlanet(game, i);
                }
            }

            retGroups.addElement(newGroup);
        }

        return retGroups;
    }

    public boolean isBetterGroupCloserThanEnemy(SquidContext context, GameState game, Vector groups) {
        int enemyDist = (Constants.MAP_WIDTH + Constants.MAP_HEIGHT) * 10;
        int closeEnemy;
        SquidGroup curGroup;
        Enumeration e;
        boolean ret = false;

        closeEnemy = context.closestEnemy(game, home);
        if (closeEnemy != -1) {
            enemyDist = context.timeDistanceBetweenPlanets(game, home, closeEnemy);
        }

        e = groups.elements();
        while (e.hasMoreElements()) {
            int dist;
            curGroup = (SquidGroup) e.nextElement();
            if (curGroup.totalProd > totalProd) {
                // Note this excludes comparing a group with itself.
                dist = context.timeDistanceBetweenPlanets(game, home, curGroup.home);
                if (dist < enemyDist) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    public boolean tryToAttackPlanet(SquidContext context, GameState game, int target) {
        Vector orderedBases = new Vector();
        int distToTarget[] = new int[Constants.PLANETS];
        boolean hasInserted[] = new boolean[Constants.PLANETS];
        int turnToLeave[] = new int[Constants.PLANETS];
        Enumeration e;
        int curPlanet;
        boolean doneSorting = false;
        int shipsAvail;
        boolean foundAttack = false;
        int furthestPlanetToAttackFrom = -1;
        int shipsToSendFromFurthest = -1;

        e = members.elements();
        while (e.hasMoreElements()) {
            curPlanet = ((Integer) e.nextElement()).intValue();
            distToTarget[curPlanet] = context.timeDistanceBetweenPlanets(
                    game, curPlanet, target);
            hasInserted[curPlanet] = false;
        }

        // Do lame sort of planets in group, closest to target first.
        while (!doneSorting) {
            int minDist = (Constants.MAP_HEIGHT + Constants.MAP_WIDTH) * 10;
            int closest = -1;

            doneSorting = true;
            e = members.elements();
            while (e.hasMoreElements()) {
                curPlanet = ((Integer) e.nextElement()).intValue();
                if ((!hasInserted[curPlanet]) && (distToTarget[curPlanet] < minDist)) {
                    doneSorting = false;
                    closest = curPlanet;
                    minDist = distToTarget[curPlanet];
                }
            }

            if (closest != -1) {
                orderedBases.addElement(new Integer(closest));
                hasInserted[closest] = true;
            }
        }

        context.debug(7, "ordered bases: " + orderedBases + " total available = "
                + totalAvailable);

        shipsAvail = 0;
        // Go through the planets in the group in ascending distance order.
        // at each step, see if an attack is feasible
        e = orderedBases.elements();
        while (e.hasMoreElements() && (shipsAvail < totalAvailable) && (!foundAttack)) {
            int enemyDefenses;
            int minAttack;
            int maxAttack;
            int availForThisPlanet;

            curPlanet = ((Integer) e.nextElement()).intValue();
            if ((context.liquidity[curPlanet] == 0)
                    || (game.getPlanet(curPlanet).getRatio() < context.MIN_RATIO_TO_ATTACK)) {
                continue;
            }
            availForThisPlanet = context.liquidity[curPlanet];
            shipsAvail += availForThisPlanet;
            context.debug(7, "On planet " + Planet.planetChar(curPlanet) + " I have "
                    + availForThisPlanet + " avail, and " + shipsAvail + " total avail so far.");
            if (shipsAvail > totalAvailable) {
                // make sure we don't strain the group past the defensive need
                context.debug(7, "(but we have exceeded total avail in the group: " + totalAvailable
                        + " so we adjust downward.)");
                availForThisPlanet -= (shipsAvail - totalAvailable);
                shipsAvail = totalAvailable;
            }

            if (game.getPlanet(target).getOwner() != Constants.NEUTRAL) {
                int rad = distToTarget[curPlanet];

                if (rad > context.MAX_ENEMY_REINFORCE_RADIUS) {
                    rad = context.MAX_ENEMY_REINFORCE_RADIUS;
                }
                enemyDefenses = game.getPlanet(target).getShips();
                context.debug(7, "Considering an enemy planet with defenses: " + enemyDefenses);
                enemyDefenses += context.threatFromEnemyPlanets(game, target,
                        game.getPlanet(target).getOwner(), rad,
                        PLANET_THREAT_DETERIORATION_WITH_DISTANCE);
                context.debug(7, "After looking at planets at radius " + rad + ", "
                        + "defenses are: " + enemyDefenses);
                enemyDefenses += context.threatFromEnemyFleets(game, target,
                        rad, -1, FLEET_THREAT_DETERIORATION_WITH_DISTANCE);
                context.debug(7, "After looking at fleets, defenses are: " + enemyDefenses);
                enemyDefenses += context.planetWillProduceInTime(game,
                        target, rad);
                context.debug(7, "After considering production, defenses are: " + enemyDefenses);
                minAttack = (int) (context.ATTACK_MIN * enemyDefenses);
                maxAttack = (int) (context.ATTACK_MIN * enemyDefenses);
            } else {
                minAttack = context.NEUTRAL_MIN;
                maxAttack = context.NEUTRAL_MAX;
            }

            context.debug(7, "Minimum attack needed: " + minAttack);

            if (shipsAvail >= minAttack) {
                // We have a viable attack plan
                context.debug(7, "We have found a viable plan!");
                furthestPlanetToAttackFrom = curPlanet;
                foundAttack = true;
                shipsToSendFromFurthest = availForThisPlanet - (shipsAvail - maxAttack);
                // If we have more available than the most it makes sense to attack with, 
                // subtract the difference.  E.g. 200 avail for two planets, 100 each, need
                // a max attack of 150.  shipsToSend = 100 - (200-150) = 50
                if (shipsToSendFromFurthest > availForThisPlanet) {
                    shipsToSendFromFurthest = availForThisPlanet;
                }
            }
        }

        if (foundAttack) {
            boolean doneSending = false;
            int eta = context.getTime(game) + distToTarget[furthestPlanetToAttackFrom];

            context.debug(5, "Formulating a plan (eta is " + eta + ")");
            context.isAttacking[target] = 1;
            context.planetAttackTime[target] = eta;

            e = orderedBases.elements();
            while (e.hasMoreElements() && !doneSending) {
                int shipsToSend;
                int timeToSend;
                SquidMove newMove;

                curPlanet = ((Integer) e.nextElement()).intValue();
                if ((context.liquidity[curPlanet] == 0)
                        || (game.getPlanet(curPlanet).getRatio() < context.MIN_RATIO_TO_ATTACK)) {
                    continue;
                }
                if (furthestPlanetToAttackFrom == curPlanet) {
                    shipsToSend = shipsToSendFromFurthest;
                    doneSending = true;
                } else {
                    shipsToSend = context.liquidity[curPlanet];
                }
                timeToSend = eta - distToTarget[curPlanet];

                newMove = new SquidMove(curPlanet, target, shipsToSend, timeToSend);
                context.debug(5, newMove.toString());
                context.moves.addElement(newMove);
            }
        }

        return foundAttack;
    }

    public void analyze(SquidContext context, GameState game, Vector groups) {
        Vector potentialTargets = new Vector();
        Enumeration e;
        int curTarget;
        int curPlanet;
        int minLiquid;
        int shouldBeAvail;
        int closestGroup;

        madeAnAttack = false;

        // Determine ships available for attacking
        totalAvailable = 0;
        e = members.elements();
        while (e.hasMoreElements()) {
            curPlanet = ((Integer) e.nextElement()).intValue();
            if (context.liquidity[curPlanet] > 0) {
                totalAvailable += context.liquidity[curPlanet];
            }
        }

        // If there is a stronger group of ours closer than any enemy planet, then we don't bother with any group
        // defense (note that we still consider short term defense of individual planets)
        if (isBetterGroupCloserThanEnemy(context, game, groups)) {
            context.debug(9, "Group: " + this + " has a protecting group closer than any enemy, so no group defence is "
                    + "needed (I hope)");
            defNeed = 0;
        } else {
            // Determine defensive requirements
            threat = context.threatFromEnemyFleets(game, home, context.THREAT_RADIUS, -1,
                    PLANET_THREAT_DETERIORATION_WITH_DISTANCE);
            threat += context.threatFromEnemyPlanets(game, home, context.THREAT_RADIUS,
                    FLEET_THREAT_DETERIORATION_WITH_DISTANCE);
            defNeed = (int) (threat * context.MIN_DEFENSE);

            if (((double) totalProd) < PRODUCTION_TO_BE_VALUABLE) {
                defNeed = (int) (REDUCTION_FOR_CHEAP_PLANETS * defNeed);
            }
        }

        if (defNeed > maxDefence) {
            context.debug(9, "Group: " + this + " calculated a total defensive need of " + defNeed + ".  However, the "
                    + "quality of planets can only justify a defense of " + maxDefence);
            defNeed = maxDefence;
        }
        if (defNeed > totalShips + totalProd) {
            // Don't bother defending this planet, because there's so many enemies nearby that it seems like a lost cause.
            context.debug(9, "Group: " + this + " needs a defence of " + defNeed + " and I can't possibly put up that "
                    + "many ships (can do " + (totalShips + totalProd) + ") so we just say screw it.");
            defNeed = 0;
        }

        context.debug(9, "Group: " + this + " ships available: " + totalAvailable
                + " defensive need: " + defNeed + " total ships: " + totalShips);
        shouldBeAvail = totalShips - defNeed;

        if (shouldBeAvail < totalAvailable) {
            totalAvailable = shouldBeAvail;
        }

        context.debug(9, "Final avail: " + totalAvailable);

        if (totalAvailable >= MIN_ATTACK_SIZE) {
            potentialTargets = context.closestNPlanetsBelongingToPlayer(
                    game, ENEMY_TARGETS_TO_LOOK_AT, -1, home);
            e = potentialTargets.elements();
            while (e.hasMoreElements()) {
                Planet curTargetObj;
                curTarget = ((Integer) e.nextElement()).intValue();
                curTargetObj = game.getPlanet(curTarget);
                if (((context.getTime(game) < TIME_TO_START_RANDOM)
                        || (!context.testProbability(CHANCE_TO_SKIP_TO_NEXT_TARGET)))
                        && (context.isAttacking[curTarget] == 0)
                        && (curTargetObj.getAttackers(context.me) == 0)
                        && ((curTargetObj.getOwner() == Constants.NEUTRAL)
                        || (curTargetObj.getProduction() > 0))) {
                    // Don't try to launch attacks on planets we're already pounding on, but
                    // "splitting the difference" is okay, of course.
                    context.debug(5, "Going to consider attacking " + Planet.planetChar(curTarget)
                            + " (my attackers = " + game.getPlanet(curTarget).getAttackers(context.me)
                            + ")");
                    madeAnAttack = tryToAttackPlanet(context, game, curTarget);
                    if (madeAnAttack) {
                        break;
                    }
                }
            }
        }

        // now consolidate the group
        if (!madeAnAttack) {
            e = members.elements();
            while (e.hasMoreElements()) {
                curPlanet = ((Integer) e.nextElement()).intValue();
                if ((curPlanet != home) && (context.liquidity[curPlanet] > 0)) {
                    SquidMove newMove = new SquidMove(curPlanet, home,
                            context.liquidity[curPlanet], context.getTime(game));
                    context.debug(7, "Reinforcing group: " + newMove);
                    context.moves.addElement(newMove);
                }
            }
        }
    }

    public static void reinforceGroups(SquidContext context, GameState game, Vector groups) {
        Enumeration e, f;
        SquidGroup curGroup, curBenefactor;

        e = groups.elements();
        while (e.hasMoreElements()) {

            curGroup = (SquidGroup) e.nextElement();
            if (curGroup.madeAnAttack) {
                continue;
            }
            if (curGroup.totalAvailable > MIN_REINFORCE_SIZE) {
                f = groups.elements();
                while (f.hasMoreElements()) {
                    curBenefactor = (SquidGroup) f.nextElement();
                    if ((curBenefactor != curGroup)
                            && (curBenefactor.totalShips < curBenefactor.defNeed)) {
                        int ships = curGroup.totalAvailable;
                        int needed = curBenefactor.defNeed - curBenefactor.totalShips;

                        if (ships > needed) {
                            ships = needed;
                        }
                        if (ships > context.liquidity[curGroup.home]) {
                            ships = context.liquidity[curGroup.home];
                        }
                        if (ships < MIN_REINFORCE_SIZE) {
                            continue;
                        }
                        SquidMove newMove = new SquidMove(curGroup.home, curBenefactor.home,
                                ships, context.getTime(game));
                        context.debug(10, "Reinforcing a group: " + newMove);
                        context.moves.addElement(newMove);
                        break;
                    }
                }
            }
        }
    }
}
