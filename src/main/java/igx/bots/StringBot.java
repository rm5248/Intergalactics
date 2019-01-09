package igx.bots;

import java.lang.Math;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// StringBot.java 
/**
 * For each planet, check if there is a nearby planet with less than
 * <ratio> times as many ships, and attack it. Otherwise, pool resources and
 * wait.
 *
 * @author Bill Baker
 */
public class StringBot extends Bot {

    /**
     * the ratio of attacker strength to defender strength that means an
     * attacking planet should send a fleet to the defending planet, indexed by
     * skill level
     */
    static final double[] OVERWHELMING = {3.5, 5, 2.5, 5, 3.5, 3.5, 2.5, 3.5, 5};

    /**
     * the ratio of the hypothetical attack fleet's strength to the defending
     * planet's strength, indexed by skill level
     */
    static final double[] ATTACK_RATIO = {3.5, 5, 2.5, 5, 3.5, 2.5, 2.5, 3.5, 5};

    /**
     * the number of ships to maintain at a non-attacking planet, times
     * production, indexed by skill level -- for example, if the GARRISON_RATIO
     * is 1.7 and production is 10, then the number of ships to keep is 17.
     */
    static final double[] GARRISON_RATIO = {1, 2, .8, 1.4, .7, 1.2, 2, 2, 2};

    /**
     * The minimum number of ships to attack with (sometimes the attacking fleet
     * size calculation will come up with 0).
     */
    static final int MIN_ATTACK = 1;

    static int MAX_DIST2 = Constants.MAP_HEIGHT * Constants.MAP_HEIGHT
            + Constants.MAP_WIDTH * Constants.MAP_WIDTH;

    int me;

    static final String[] STRING_NAMES = {
        "Hair", "Twine", "Ligament", "Fiber", "Filament",
        "Thread", "String", "Sinew", "Strand", "Floss"};
    // debugging names for clarity
    // "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine" };

    /**
     * The name of a StringBot.
     */
    public String createName(int skillLevel) {
        int index = skillLevel - 1;
        while (index >= STRING_NAMES.length) {
            index -= STRING_NAMES.length;
        }
        while (index < 0) {
            index += STRING_NAMES.length;
        }
        // 0 <= index < STRING_NAMES.length
        return STRING_NAMES[index];
    }

    /**
     * Order all planets by their proximity to iSourceP, closest first
     */
    public Vector closestPlanets(int iSourceP) {
        return closestPlanets(iSourceP, Constants.PLANETS);
    }

    /**
     * Pick out the numPlanets closest planets to iSourceP
     */
    public Vector closestPlanets(int iSourceP, int numPlanets) {
        // build table of (distance to, planet number)
        Hashtable distToPlanet = new Hashtable();
        Planet sourceP = curGame.getPlanet(iSourceP);
        for (int i = 0; i < Constants.PLANETS; ++i) {
            Integer d2 = new Integer(dist2(curGame.getPlanet(i), sourceP));
            // make sure we don't duplicate distances
            while (distToPlanet.containsKey(d2)) {
                d2 = new Integer(d2.intValue() + 1);
            }
            distToPlanet.put(d2, new Integer(i));
        }
        // pull out the n closest
        Vector result = new Vector(numPlanets);
        for (int i = 0; i < numPlanets; ++i) {
            int closestP = -1;
            Integer closestD = new Integer(MAX_DIST2);
            // - find the closest remaining planet
            for (Enumeration j = distToPlanet.keys();
                    j.hasMoreElements();) {
                Integer jd = (Integer) j.nextElement();
                if (jd.intValue() < closestD.intValue()) {
                    closestD = jd;
                    closestP = ((Integer) distToPlanet.get(jd)).intValue();
                }
            }
            // - add it to the result
            result.addElement(new Integer(closestP));
            // - and remove it from the table
            distToPlanet.remove(closestD);
        }
        // done
        return result;
    }

    /**
     * Key = planet number, Value = segment when attack will arrive
     */
    Hashtable attacks = new Hashtable();

    /**
     * The method is called when a new game is begun. The StringBot doesn't need
     * to do anything here, but other 'bots might find use for this.
     */
    public void newGame(GameState game, int skillLevel) {
        // Keeps track of its player number (just for his convenience)
        me = getNumber();
        debug("StringBot reporting for duty...");
    }

    /**
     * This function is overridden so there are only five StringBots (Only five
     * skill levels).
     */
    public int numberOfBots() {
        return 6;
    }

    /**
     * The square of the distance between two planets
     */
    int dist2(Planet p1, Planet p2) {
        int dx = p1.getX() - p2.getX(), dy = p1.getY() - p2.getY();
        return dx * dx + dy * dy;
    }

    public static int expectedDefBonus
            = (Constants.MAX_DEFENCE_BONUS + Constants.MIN_DEFENCE_BONUS) / 2;
    public static int neutralRatio
            = (Constants.MAX_RATIO + Constants.MIN_RATIO) / 2;
    public static int neutralShips
            = (Constants.MAX_NEUTRAL_SHIPS + Constants.MAX_NEUTRAL_SHIPS
            + Constants.MIN_NEUTRAL_SHIPS) / 3;

    /**
     * Defensive strength if we send a fleet now: the square of the number of
     * ships times the defense ratio at the time that an attack would occur if a
     * fleet left now from pAtt to attack pDef -- assume expected value of max
     * defense bonus and assume that no additional reinforcements will arrive
     */
    public int defensiveStrength(int iDef, int iAtt) {
        Planet pDef = curGame.getPlanet(iDef);
        // defense ratio = ratio + defense bonus
        int defRatio = (pDef.isNeutral() ? neutralRatio : pDef.getRatio())
                + expectedDefBonus;
        // number of ships = current + expected accumulation before
        // attackers arrive
        int addShips = (int) (pDef.isNeutral() ? neutralShips
                : (pDef.getProduction() * transitTime(iAtt, iDef)
                / Constants.SEGMENTS));
        int totalShips = (pDef.isNeutral() ? 0 : pDef.getShips()) + addShips;
        return totalShips * totalShips * defRatio;
    }

    /**
     * How many ships do we expect at a certain time at a certain planet?
     * Calculated as current ships + generated there between now and then.
     */
    public int expectedShips(int iPlanet, int time) {
        int dt = time - curGame.getTime();
        Planet p = curGame.getPlanet(iPlanet);
        if (p.isNeutral()) {
            return neutralShips;
        } else {
            return p.getShips() + p.getProduction() * dt / Constants.SEGMENTS;
        }
    }

    /**
     * Offensive strength for a fleet of a certain size, attacking from planet
     * iAtt
     */
    public int offensiveStrength(int iAtt, int numShips) {
        return numShips * numShips * curGame.getPlanet(iAtt).getRatio();
    }

    /**
     * Offensive strength for the planet iAtt, using all of its ships
     */
    public int offensiveStrength(int iAtt) {
        return offensiveStrength(iAtt, curGame.getPlanet(iAtt).getShips());
    }

    /**
     * the time, in segments, that it would take for a fleet to get between
     * planets p1 and p2
     */
    public int transitTime(int p1, int p2) {
        return curGame.arrivalTime(new Fleet(p1, p2, 0), 0);
    }

    GameState curGame, oldGame;

    /**
     * The mind of the StringBot.
     *
     * For each planet, the StringBot checks: <ul>
     * <li>if the closest enemy planet is closer than the closest friendly
     * planet with a better attack ratio then, <ul>
     * <li>if we have more than a certain number of ships available, attack that
     * enemy planet
     * <li>otherwise sit and wait (we could do something more sophisticated
     * though, mind you)
     * </ul>
     * <li>otherwise, send a certain number of ships off to that better friendly
     * planet
     * </ul>
     */
    public void update(GameState game,
            GameState oldState,
            Message[] message) {
        curGame = game;
        oldGame = oldState;
        // 1 - clean up attacks - remove attacks that arrive this segment
        Enumeration keys = null;
        // debug("Starting time " + curGame.getTime());
        for (keys = attacks.keys(); keys.hasMoreElements();) {
            Integer iTo = (Integer) keys.nextElement();
            Integer iArrival = (Integer) attacks.get(iTo);
            /*
	 debug("Pending attack on " + Planet.planetChar(iTo.intValue()) +
	       " arriving at time " + iArrival.intValue());
             */
            if (iArrival.intValue() == curGame.getTime()) {
                attacks.remove(iTo);
            }
        }

        // 2 - loop over all of our planets
        for (int i = 0; i < Constants.PLANETS; ++i) {
            Planet ourP = game.getPlanet(i);
            // skip planets that are not ours
            if (ourP.getOwner() != me) {
                continue;
            }
            // if under attack ...
            int numAttackers = ourP.getTotalAttackers();
            if (numAttackers > 0) {
                // ... if we're outnumbered (DEBUG to test if we're
                // outmatched), clear out ...
                if (numAttackers > ourP.getShips()) {
                    bugOut(i);
                }
                // ... but if it looks good, sit tight at this planet
                // until the battle is over
            } // if not under attack, distribute resources sans panic
            else {
                i = thinkCalmly(i);
            }
        }
    }

    /**
     * Would sending all ships from sourceP to destP be a wise attack? That is,
     * would our offensive strength be greater than or equal to ATTACK_RATIO
     * times their defensive strength?
     */
    public boolean attackGoodIdea(int sourceP, int destP) {
        return attackGoodIdea(sourceP, destP, curGame.getPlanet(sourceP).getShips());
    }

    /**
     * How about if we only send numShips?
     */
    public boolean attackGoodIdea(int sourceP, int destP, int numShips) {
        return (offensiveStrength(sourceP, numShips)
                >= (ATTACK_RATIO[getSkillLevel()] * defensiveStrength(destP, sourceP)));
    }

    /**
     * Let's get outta here - either to a safe place or to a planet we can take
     * over safely
     */
    public void bugOut(int iOurP) {
        Planet ourP = curGame.getPlanet(iOurP);
        Vector vClosest = closestPlanets(iOurP);
        String planets = "";
        for (Enumeration j = vClosest.elements(); j.hasMoreElements();) {
            int jClosest = ((Integer) j.nextElement()).intValue();
            planets = planets + Planet.planetChar(jClosest);
            if (jClosest == iOurP) {
                continue;
            }
            Planet pClosest = curGame.getPlanet(jClosest);
            // if it's ours, send all ships as reinforcements
            if (pClosest.getOwner() == me) {
                sendFleet(iOurP, jClosest, ourP.getShips());
                // debug("^%&  --  Refugees  --  " + planets);
                return;
            } // if it's an enemy's, attack if we can win
            else if (attackGoodIdea(iOurP, jClosest)) {
                attack(iOurP, jClosest);
                // debug("^%&  --  Scrambling  --  " + planets);
                return;
            }
        }
    }

    /**
     * Decide if and where to send ships from this planet. Return the current
     * planet to consider (return iOurP means go on, Constants.PLANETS means
     * we're done).
     */
    protected int thinkCalmly(int iOurP) {
        Planet ourP = curGame.getPlanet(iOurP);
        // find closest enemy planet
        Planet closestEnemyP = null;
        int enemyDist2 = 0, jClosestEnemy = 0;
        // 2a - find the closest enemy planet
        for (int j = 0; j < Constants.PLANETS; ++j) {
            Planet enemyP = curGame.getPlanet(j);
            // don't attack own planets
            if (enemyP.getOwner() == me) {
                continue;
            }
            // skip planets that we are already attacking
            if (attacks.containsKey(new Integer(j))) {
                continue;
            }
            int d2 = dist2(enemyP, ourP);
            if (enemyDist2 == 0 || d2 < enemyDist2) {
                enemyDist2 = d2;
                closestEnemyP = enemyP;
                jClosestEnemy = j;
            }
        }
        /*
	debug("closest enemy to " + Planet.planetChar(i) + " is " +
	Planet.planetChar(jClosestEnemy));
         */
        // if no enemy planets, skip the rest and just gloat
        if (enemyDist2 == 0) {
            // debug("No enemies in sight.");
            return Constants.PLANETS;
        }

        // find (1) closest friendly planet with a better attack
        // ratio and (2) best attack ratio that is closer than enemy
        // planet and
        // (3) better attack ratio that is closer to enemy but not as
        // far from here as enemy (not implemented)
        Planet betterP = null, // closest friendly with better ratio
                bestP = null; // best ratio that is closer than closest enemy
        int kBetterP = 0, kBestP = 0;
        int betterMinDist2 = 0, bestMinDist2 = 0;
        for (int k = 0; k < Constants.PLANETS; ++k) {
            if (k == iOurP) {
                continue; // skip current planet (ourP)
            }
            Planet qP = curGame.getPlanet(k);
            if (qP.getRatio() > ourP.getRatio()) { // better ratio?
                // closest friendly with better ratio?
                int qDist2 = dist2(qP, ourP);
                if (betterMinDist2 == 0
                        || // haven't found one yet or ...
                        qDist2 < betterMinDist2
                        || // ... closer or ...
                        (qDist2 == betterMinDist2
                        && // ... same dist and better
                        qP.getRatio() > betterP.getRatio())) {
                    kBetterP = k;
                    betterP = qP;
                    betterMinDist2 = qDist2;
                }
                // best ratio that is closer than closest enemy
                if (qDist2 < enemyDist2 && qP.getRatio() > ourP.getRatio()) {
                    if (bestP == null
                            || // if haven't found one yet, ...
                            // ... or ratio is better ...
                            bestP.getRatio() < qP.getRatio()
                            || // ... or ratio is same and distance is less
                            (bestP.getRatio() == qP.getRatio()
                            && qDist2 < bestMinDist2)) {
                        kBestP = k;
                        bestP = qP;
                        bestMinDist2 = qDist2;
                    }
                }
            }
        }
        /*
	if (betterMinDist2 != 0)
	debug("closest friend with better attack than " +
	Planet.planetChar(i) + " is " + Planet.planetChar(kBetterP));
	if (bestMinDist2 != 0)
	debug("best attack ratio closer than enemy to " +
	Planet.planetChar(i) + " is " + Planet.planetChar(kBestP));
         */

        // so how does the situation look?
        int defStr = defensiveStrength(jClosestEnemy, iOurP);
        int attStr = offensiveStrength(iOurP);
        /*
	debug("attack strength from " + Planet.planetChar(i) + " = " +
	attStr + "; defensive strength at " +
	Planet.planetChar(jClosestEnemy) + " = " + defStr);
         */

        // decide what to do about it:
        //  - if odds are not overwhelmingly good enough, dig in
        if (ourP.getRatio() == 0
                || // prevent DivBy0 in else{} below
                attStr < OVERWHELMING[getSkillLevel()] * defStr) {
            // send all but a few to the best nearby planet
            int shipsToSend
                    = ourP.getShips() - (int) (GARRISON_RATIO[getSkillLevel()] * ourP.getProduction());
            if (shipsToSend > 0) {
                if (bestP != null) {
                    sendFleet(iOurP, kBestP, shipsToSend);
                    /*
	       debug("reinforce " + Planet.planetChar(kBestP) + " with " +
		     shipsToSend + " ships from " + Planet.planetChar(iOurP));
                     */
                }
                // else if (betterP != null)
                //    sendFleet(iOurP, kBetterP, shipsToSend);
            }
        } //  - if overwhelmingly good odds, charge!
        else {
            // how many to send, based on ATTACK_RATIO, which is the
            // desired ratio of attacking strength to defending
            // strength
            int shipsToSend
                    = (int) Math.sqrt(ATTACK_RATIO[getSkillLevel()] * defStr / ourP.getRatio());
            if (shipsToSend <= 0) {
                shipsToSend = MIN_ATTACK;
            }
            attack(iOurP, jClosestEnemy, shipsToSend);
        }
        // done
        return iOurP;
    }

    /**
     * attack destPlanet with all ships from sourcePlanet
     */
    public void attack(int sourcePlanet, int destPlanet) {
        attack(sourcePlanet, destPlanet,
                curGame.getPlanet(sourcePlanet).getShips());
    }

    /**
     * send an attack fleet of numShips ships from sourcePlanet to destPlanet
     */
    public void attack(int sourcePlanet, int destPlanet, int numShips) {
        Planet sourceP = curGame.getPlanet(sourcePlanet);
        if (sourceP.getOwner() == me) {
            if (sourceP.getShips() < numShips) {
                debug("Warning: number of ships (" + sourceP.getShips()
                        + ") at " + Planet.planetChar(sourcePlanet)
                        + " is less than number ordered to depart (" + numShips
                        + ").");
                numShips = sourceP.getShips();
            }
            sendFleet(sourcePlanet, destPlanet, numShips);
            int arrivalTime
                    = curGame.getTime() + transitTime(destPlanet, sourcePlanet);
            attacks.put(new Integer(destPlanet), new Integer(arrivalTime));
            debug("attack " + Planet.planetChar(destPlanet) + " with "
                    + numShips + " ships from " + Planet.planetChar(sourcePlanet)
                    + " (" + Planet.planetChar(destPlanet) + " has "
                    + curGame.getPlanet(destPlanet).getShips() + " ships, expect "
                    + expectedShips(destPlanet, arrivalTime) + ")");
        } else {
            debug("Error: can't attack from planet "
                    + Planet.planetChar(sourcePlanet)
                    + " because we don't own it.");
        }
    }
}
