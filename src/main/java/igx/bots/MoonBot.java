package igx.bots;

// MoonBot.java
/**
 * This is a sample robot. It isn't too great a player, but the code is
 * available if you want to see just what's involved in writing a 'bot.
 *
 * @author John Watkinson
 */
public class MoonBot extends Bot {

    private int me;

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
     * This function returns the name of the MoonBot.
     */
    public String createName(int skillLevel) {
        switch (skillLevel) {
            case 0:
                return "MoonMom";
            case 1:
                return "MoonDad";
            case 2:
                return "MoonGirl";
            case 3:
                return "MoonBoy";
            case 4:
                return "MoonBaby";
            case 5:
                return "MoonDog";
            case 6:
                return "MoonCat";
            default:
                return "MoonFish";
        }
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
            if ((distance < minDistance) && (p.getOwner() != me) && (!p.isBlackHole())) {
                minDistance = distance;
                bestPlanet = i;
            }
        }
        return bestPlanet;
    }

    /**
     * The method is called when a new game is begun. The MoonBot doesn't need
     * to do anything here, but other 'bots might find use for this.
     */
    public void newGame(GameState game, int skillLevel) {
        // Keeps track of its player number (just for his convenience)
        me = getNumber();
        debug("MoonBot reporting for duty...");
    }

    /**
     * This function is overridden so there are only two MoonBots (Only two
     * skill levels).
     */
    public int numberOfBots() {
        return 8;
    }

    /**
     * Here's is the heart & soul of the MoonBot.
     *
     * The MoonBot is very simplified. It finds the closest planet it doesn't
     * own and tries to attack that planet. Occasionally, it will reinforce the
     * planet it owns with the highest kill ratio.
     */
    public void update(GameState game, GameState oldState, Message[] message) {
        // Find "best" planet we own (has most ships).
        int attackPlanetNum = bestPlanetOfMine(game);
        // If we don't have any planets, then forget it! Just pout.
        if (attackPlanetNum == -1) {
            return;
        }
        Planet attackPlanet = game.getPlanet(attackPlanetNum);
        int attackingShips = attackPlanet.getShips();
        // Find nearest planet to attack planet that we don't own.
        int targetPlanetNum = nearestEnemyPlanetTo(attackPlanetNum, game);
        // If we own them all, then who cares?
        if (targetPlanetNum == -1) {
            return;
        }
        Planet targetPlanet = game.getPlanet(targetPlanetNum);
        // If it is neutral, then assume it has 3/4*MAX_NEUTRAL_SHIPS defending.
        int defendingShips = targetPlanet.getShips();
        if (targetPlanet.isNeutral()) {
            defendingShips = 3 * Constants.MAX_NEUTRAL_SHIPS / 4;
        }
        // If we have double that many ships on our attack planet, then go to town.
        if (attackingShips >= 2 * defendingShips) {
            // Exception: let's send them all when it's a neutral
            if (targetPlanet.isNeutral()) {
                sendFleet(attackPlanetNum, targetPlanetNum, attackingShips);
            } else if (targetPlanet.getShips() == 0) {
                sendFleet(attackPlanetNum, targetPlanetNum, random(1, 2));
            } else {
                sendFleet(attackPlanetNum, targetPlanetNum, 2 * defendingShips);
            }
        }
        // About once every two turns, we'd like to reinforce our highest ratio planet.
        if (random(0, Constants.SEGMENTS * 2 - 1) == 0) {
            int bestRatioPlanetNum = bestRatioPlanetOfMine(game);
            for (int i = 0; i < Constants.PLANETS; i++) {
                // Let's not further strain our attack planet
                if (i == attackPlanetNum) {
                    continue;
                }
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
        // Also, let's randomly say something ludicrous
        int mood = random(0, 20000);
        switch (mood) {
            case 0:
                sendMessage(Constants.MESSAGE_TO_ALL, "How y'all doin' out thare?");
                break;
            case 1:
                sendMessage(Constants.MESSAGE_TO_ALL, "Shocky!");
                break;
            case 2:
                // Grab a random player name and insult
                int insultTarget = random(0, game.getNumberOfPlayers() - 1);
                // This would be embarassing
                if (insultTarget != me) {
                    sendMessage(Constants.MESSAGE_TO_ALL, game.getPlayer(insultTarget).getName() + ", you're not lasting.");
                }
                break;
            case 3:
                // Odds are that this will be the case!
                sendMessage(Constants.MESSAGE_TO_ALL, "I'm in a whole world of hurt.");
                break;
        }
        // That's it.
    }
}
