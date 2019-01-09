package igx.bots;

import java.util.*;

// SquidBot.java
public class SquidBot extends Bot {

    public SquidContext context;

    /**
     * This function returns the name of the MoonBot.
     */
    public String createName(int skillLevel) {
        switch (skillLevel) {
            case 0:
                return "Squiddy";
            case 1:
                return "Kraken";
            case 2:
                return "Ink";
            default:
                return "Decapod";
        }
    }

    public void newGame(GameState game, int skillLevel) {
        context = new SquidContext();
        context.initConstants(getNumber(), skillLevel);
        context.debug(1, "Reporting for duty.");
    }

    public int numberOfBots() {
        return 3;
    }

    public void update(GameState game,
            GameState oldState,
            Message[] message) {
        Enumeration e;
        String curMessage;
        SquidMove curMove;
        Vector groups;
        SquidGroup curGroup;
        SquidGroup[] groupForPlanet = new SquidGroup[Constants.PLANETS];

        if ((getSkillLevel() == 2) && (context.getTime(game) == 0)) {
            context.firstTurnSpray(game);
        }
        context.shockHomesEarly(game, (getSkillLevel() == 0));

        context.turnInit(game, oldState);
        context.launchPlayerShocks(game);
        groups = SquidGroup.reformGroups(context, game, groupForPlanet);
        context.debug(5, "Groups are: " + groups);
        e = groups.elements();
        while (e.hasMoreElements()) {
            curGroup = (SquidGroup) e.nextElement();
            curGroup.analyze(context, game, groups);
        }
        SquidGroup.reinforceGroups(context, game, groups);
        context.bailout(game);

        e = context.getMessages().elements();
        while (e.hasMoreElements()) {
            curMessage = (String) e.nextElement();
            sendMessage(Constants.MESSAGE_TO_ALL, "(" + (game.getTime() + 20) + "):" + curMessage);
        }

        e = context.getMovesThisTurn(game).elements();
        while (e.hasMoreElements()) {
            curMove = (SquidMove) e.nextElement();
            sendFleet(curMove.source, curMove.dest, curMove.ships);
        }
    }
}
