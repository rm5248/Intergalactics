package igx.bots;

/**
 * The DiploBot is an arrogant little Bot that tries to set up alliances with
 * players (with no particular plan in mind). It will being to distrust those it
 * allies with until a certain (unknown) breaking point at which it breaks off
 * the alliance. After war has been declared, it takes a while to get back on
 * the Diplobot's good side.
 *
 * You can attempt to ally with a Diplobot on your own accord. However, beware
 * because Diplobots can often be insulted by such a request and become even
 * more distrustful.
 *
 * Meanwhile, the Diplobot is an extremely overzealous neutral expander. It is
 * somewhat LucasBotish in its tactics.
 *
 * Understood sentences:
 * <pre>
 * ALLY
 * ALLIED WITH PLAYER
 * WAR
 * STATUS
 * </pre>
 *
 * @author John Watkinson
 * @modified by Toby Hudson to stop exponential runaway communications.
 */
public class DiploBot extends TalkBot {

    int[] status;
    int[] animosity;
    int me = -1;
    int summit = -1;

    public static final int[] ALLY_CHANCE = {50, 75, 25};

    public static final int TRUST = 6;
    public static final int MIN_DISTRUST = 50;
    public static final int MAX_DISTRUST = 200;

    public int numberOfBots() {
        return 3;
    }

    public String createName(int skillLevel) {
        switch (skillLevel) {
            case 0:
                return "Ambassabot";
            case 1:
                return "Pacibot";
            case 2:
                return "Bellibot";
            default:
                return super.createName(skillLevel);
        }
    }

    public void newGame(GameState game, int skillLevel) {
        super.newGame(game, skillLevel);
        status = new int[game.getNumberOfPlayers()];
        animosity = new int[game.getNumberOfPlayers()];
        me = getNumber();
        summit = random(1, 50);
        if (game.getNumberOfPlayers() == 2) {
            // Just for kicks
            sendWar(1 - me);
        }
    }

    /**
     * Returns the planet with the most ships on it that the 'bot owns.
     */
    public int bestPlanetOfMine(GameState game) {
        int maxShips = 0;
        int bestPlanet = -1;
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            int ships = p.getShips();
            if ((p.getOwner() == me) && (ships > maxShips)) {
                maxShips = ships;
                bestPlanet = i;
            }
        }
        return bestPlanet;
    }

    /**
     * Returns the planet with the best attack ratio that the 'bot owns.
     */
    public int bestRatioPlanetOfMine(GameState game) {
        int maxRatio = 0;
        int bestPlanet = -1;
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            int ratio = p.getRatio();
            if ((p.getOwner() == me) && (ratio > maxRatio)) {
                maxRatio = ratio;
                bestPlanet = i;
            }
        }
        return bestPlanet;
    }

    /**
     * Returns the nearest enemy planet to the given planet.
     */
    public int nearestEnemyPlanetTo(int source, GameState game) {
        double minDistance = Constants.MAP_WIDTH + Constants.MAP_HEIGHT;
        int bestPlanet = -1;
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            double distance = game.getDistance(source, i);
            if ((distance < minDistance) && (p.getOwner() != me) && ((p.getOwner() == Constants.NEUTRAL) || (status[p.getOwner()] <= 0))) {
                minDistance = distance;
                bestPlanet = i;
            }
        }
        return bestPlanet;
    }

    public void doTurn(GameState game, GameState oldState) {
        //// STEP 1 - See if any of our allies are messing with us!
        ////          Also, let's see if we are messing with our allies.
        {
            int n = game.getNumberOfArrivedFleets();
            boolean[] sorry = new boolean[game.getNumberOfPlayers()];
            for (int i = 0; i < n; i++) {
                ArrivedFleet f = game.getArrivedFleet(i);
                // Is it an attack on us?
                int targetOwner = game.getPlanet(f.getPlanetNumber()).getOwner();
                if ((f.getOwner() != me) && (targetOwner == me)) {
                    int owner = f.getOwner();
                    animosity[owner]++;
                    // If they are an ally they lose a status point for this-- and once at 0, it's war!
                    if (status[owner] > 0) {
                        if (--status[owner] == 0) {
                            sendWar(owner);
                            animosity[owner] += random(MIN_DISTRUST, MAX_DISTRUST);
                        }
                    }
                } else if ((f.getOwner() == me) && ((targetOwner != Constants.NEUTRAL) && (status[targetOwner] > 0))) {
                    // We've accidentally attacked an ally. We should plan to apologize.
                    sorry[game.getPlanet(f.getPlanetNumber()).getOwner()] = true;
                }
            }
            for (int i = 0; i < game.getNumberOfPlayers(); i++) {
                if (sorry[i]) {
                    sendSorry(i);
                }
            }
        }
        //// STEP 2 - Wartime animosity decays
        for (int i = 0; i < game.getNumberOfPlayers(); i++) {
            if (animosity[i] > 0) {
                animosity[i]--;
            }
        }
        //// STEP 3 - Let's try to get some more allies.
        if (--summit == 0) {
            summit = random(50, 200);
            // Let's try to do some allying.
            int n = game.getNumberOfPlayers();
            // There's not much diplomacy to do with just 2 players.
            if (n > 2) {
                for (int i = 0; i < n; i++) {
                    // Let's not ally with ourselves!
                    if (i != me) {
                        // Ignore if already allied
                        if ((status[i] == 0) && (animosity[i] == 0)) {
                            if (random(0, 99) < ALLY_CHANCE[getSkillLevel()]) {
                                sendAlly(i, -1, -1);
                                // -1 means negotiations are in progress.
                                status[i] = -1;
                            }
                        }
                    }
                }
            }
        }
        //// STEP 3 - Now, let's continue on and do a normal turn.
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet attackPlanet = game.getPlanet(i);
            if (attackPlanet.getOwner() == me) {
                int target = nearestEnemyPlanetTo(i, game);
                if (target != -1) {
                    int attackingShips = attackPlanet.getShips();
                    Planet targetPlanet = game.getPlanet(target);
                    // If it is neutral, then assume it has 3/4*MAX_NEUTRAL_SHIPS defending.
                    int defendingShips = targetPlanet.getShips();
                    if (targetPlanet.isNeutral()) {
                        defendingShips = 3 * Constants.MAX_NEUTRAL_SHIPS / 4;
                    }
                    // If we have double that many ships on our attack planet, then go to town.
                    if (attackingShips > 2 * defendingShips) {
                        // Exception: let's send them all when it's a neutral
                        if (targetPlanet.isNeutral()) {
                            sendFleet(i, target, attackingShips);
                        } else {
                            double d = game.getDistance(i, target);
                            // Only send occasionally if the planet is far, and send more
                            int attackChance = (int) (100 / (d * d));
                            int requiredShips = (int) Math.sqrt(d) * defendingShips * 2;
                            if ((random(0, 99) < attackChance) && (attackingShips > requiredShips)) {
                                if (targetPlanet.getShips() == 0) {
                                    sendFleet(i, target, random(1, 2));
                                } else {
                                    sendFleet(i, target, requiredShips);
                                }
                            }
                        }
                    }
                }
            }
        }
        // About once every four turns, we'd like to reinforce our highest ratio planet.
        if (random(0, Constants.SEGMENTS * 4 - 1) == 0) {
            int bestRatioPlanetNum = bestRatioPlanetOfMine(game);
            for (int i = 0; i < Constants.PLANETS; i++) {
                // No point dealing with the best ratio planet
                if (i == bestRatioPlanetNum) {
                    continue;
                }
                Planet p = game.getPlanet(i);
                int numShips = p.getShips();
                // Make sure it's ours and there is at least 2 ships on it
                if ((p.getOwner() == me) && (numShips > 2)) // Send half of the ships off
                {
                    sendFleet(i, bestRatioPlanetNum, numShips / 2);
                }
            }
        }
    }

    public void receiveAlly(int sender, GameState game, int round, int segment) {
        if (status[sender] == -1) {
            // Pact accepted!
            status[sender] += random(1, 6);
        } else if (status[sender] == 0) {
            // A new offer, let's consider it:
            if ((game.getNumberOfPlayers() > 2) && (random(0, 99) < ALLY_CHANCE[getSkillLevel()])) {
                // We accept.
                sendAllied(sender, sender, -1, -1);
                status[sender] += random(1, TRUST);
            } else {
                // We refuse.
                sendWar(sender);
                animosity[sender] += random(MIN_DISTRUST, MAX_DISTRUST);
            }
        } else {
            // We are already allied. Let's just be ceremonial:
            sendAllied(sender, sender, -1, -1);
        }
    }

    public void receiveAllied(int sender, GameState game, int player, int round, int segment) {
        // If it's me, then just treat it like an ALLY (except don't send ceremonial sendAllied()).
        if (player == me) {
            if (status[sender] == -1) {
                // Pact accepted!
                status[sender] += random(1, 6);
            } else if (status[sender] == 0) {
                // A new offer, let's consider it:
                if ((game.getNumberOfPlayers() > 2) && (random(0, 99) < ALLY_CHANCE[getSkillLevel()])) {
                    // We accept.
                    sendAllied(sender, sender, -1, -1);
                    status[sender] += random(1, TRUST);
                } else {
                    // We refuse.
                    sendWar(sender);
                    animosity[sender] += random(MIN_DISTRUST, MAX_DISTRUST);
                }
            }
        }
    }

    public void receiveStatus(int sender, GameState game) {
        // Let's tell the world who we are allied to
        /*
      for (int i = 0; i < game.getNumberOfPlayers(); i++) {
         Player p = game.getPlayer(i);
         String sitch = "WAR";
         if (i != me) {
            if (status[i] == -1) {
               sitch = "NEGOTIATION";
            } else if (status[i] > 0) {
               sitch = "PEACE";
            }
            sendMessage(Constants.MESSAGE_TO_ALL, p.getName() + ": " + sitch + " (" + animosity[i] + ")");
         }
      }
         */
        Player p = game.getPlayer(sender);
        if (status[sender] > 0) {
            sendAllied(sender, sender, -1, -1);
        } else {
            sendDeclaredWar(sender, sender);
        }
    }

    public void receiveWar(int sender, GameState game) {
        // They don't like us, so we don't like them.
        status[sender] = 0;
        animosity[sender] += MIN_DISTRUST;
    }

    public void receiveDeclaredWar(int sender, GameState game, int player) {
        // Who cares
    }

    public void receiveDanger(int sender, GameState game, int p, int round, int segment, int ships) {
        // Who cares
    }

    public void receiveShips(int sender, GameState game, int p, int round, int segment, int ships) {
        // Who cares
    }

    public void receiveSend(int sender, GameState game, int p, int round, int segment, int ships) {
        // Who cares
    }

    public void receiveFleet(int sender, GameState game, int p, int round, int segment, int ships) {
        // Who cares
    }

    public void receiveWant(int sender, GameState game, int p, int round, int segment) {
        // Who cares
    }

    public void receiveSorry(int sender, GameState game) {
        // Who cares
    }
}
