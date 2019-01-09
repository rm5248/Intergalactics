package igx.bots;

// AdamsBot.java
/**
 * Written by Peter Maxwell 17/9/99 with help from Keith Houston
 *
 */
public class AdamsBot extends Bot {

    private int BotNumber;
    private int neutralAssault;
    private int attackpercentage;
    private int reinforce;

    private int Nearestenemy;
    private int Nearestfriend;
    private int Nearestneutral;
    private double NearestenemyDistance;
    private double NearestfriendDistance;
    private double NearestneutralDistance;

    public void newGame(GameState game, int skillLevel) {
        BotNumber = getNumber();

        neutralAssault = 9;
        attackpercentage = 30;
        reinforce = 7;
    }

    /**
     * Added by Moonman *
     */
    public int numberOfBots() {
        return 2;
    }

    /**
     * End of Moonman code *
     */
    public String createName(int skillLevel) {
        switch (skillLevel) {
            case 0:
                return "Marvin";
            default:
                return "Deep Thought";
        }
    }

    public void doPlanet(GameState game, int planet) {
        Planet thisPlanet = game.getPlanet(planet);
        int ships = thisPlanet.getShips();
        int production = thisPlanet.getProduction();
        int ratio = thisPlanet.getRatio();

        if (ships == 0) {
            return;
        }

        findClosestPlanets(game, planet);
        Planet nearestFriend = game.getPlanet(Nearestfriend);

        if (Nearestenemy == -1) {
            return;
        }

        if (Nearestfriend != -1) {
            int attackers = thisPlanet.getTotalAttackers();
            if (attackers > 3 * ships) /* evacuate! */ {
                sendFleet(planet, Nearestfriend, ships);
            }
            if (attackers > 0) {
                return;
            }

            int friendAttackers = nearestFriend.getTotalAttackers();
            int friendShips = nearestFriend.getShips();

            if (friendAttackers > friendShips && friendAttackers < (friendShips + (ships / 2))) {
                sendFleet(planet, Nearestfriend, ships / 2);
            }
        }

        if (Nearestneutral != -1) {
            if (NearestneutralDistance < NearestenemyDistance
                    && ships > neutralAssault) {
                sendFleet(planet, Nearestneutral, neutralAssault);
                return;
            }
        }

        Planet nearestEnemy = game.getPlanet(Nearestenemy);
        int NE = Nearestenemy;
        int NN = Nearestneutral;

        int defence = ((production + (ratio / 10)));

        if (ships > ((defence * defence) / reinforce)) {
            findClosestPlanets(game, Nearestenemy);
            if (Nearestfriend != planet) {
                sendFleet(planet, Nearestfriend, ships - (defence + 1));
                if (nearestEnemy.getShips() == 0) {
                    sendFleet(planet, NE, 1);
                } else if (NN != -1) {
                    sendFleet(planet, NN, 1);
                }
                return;
            }
        }

        int enemyVal = (int) (nearestEnemy.getShips()
                + nearestEnemy.getProduction() + 1)
                * (nearestEnemy.getRatio() + 100);

        int botVal = ships * (ratio + 100);

        if ((enemyVal * (100 + attackpercentage)) < (botVal * 100)) {
            sendFleet(planet, NE, (enemyVal / (ratio + 100)));
            return;
        }

    }

    public void findClosestPlanets(GameState game, int source) {
        Nearestfriend = -1;
        Nearestenemy = -1;
        Nearestneutral = -1;

        NearestenemyDistance = Constants.MAP_WIDTH + Constants.MAP_HEIGHT;
        NearestneutralDistance = Constants.MAP_WIDTH + Constants.MAP_HEIGHT;
        NearestfriendDistance = Constants.MAP_WIDTH + Constants.MAP_HEIGHT;

        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            double distance = game.getDistance(source, i);
            if (p.getOwner() == BotNumber && distance < NearestfriendDistance) {
                Nearestfriend = i;
                NearestfriendDistance = distance;
                continue;
            }
            if (p.isNeutral() && distance < NearestneutralDistance) {
                Nearestneutral = i;
                NearestneutralDistance = distance;
                continue;
            }
            if (p.getOwner() != BotNumber && distance < NearestenemyDistance) {
                Nearestenemy = i;
                NearestenemyDistance = distance;
                continue;
            }
        }

    }

    public void update(GameState game, GameState oldState, Message[] message) {
        for (int i = 0; i < Constants.PLANETS; i++) {
            Planet p = game.getPlanet(i);
            if (p.getOwner() == BotNumber) {
                doPlanet(game, i);
            }
        }
    }
}
