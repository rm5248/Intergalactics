package igx.stats.web;

import net.sf.hibernate.Session;
import java.util.*;
import net.sf.hibernate.*;
import igx.stats.*;
import net.sf.hibernate.type.*;

/**
 * <p>Title: IGX</p>
 * <p>Description: All the finders and WHATEVER</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Matt Hall, John Watkinson
 * @version 1.0
 */

public class StatsHelper {

    public StatsHelper() {
    }

    public static List findAllPlayers() {
        try {
            Session session = SessionTable.getSessionTable().getSession();
            return session.find("from player in class igx.stats.Player");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Player findPlayer(String playerName) {
        try {
            Session session = SessionTable.getSessionTable().getSession();
            List players = session.find("from player in class igx.stats.Player where player.name=?", playerName, Hibernate.STRING);
            if (players.size() > 0) {
                return (Player) players.get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List getLast24HoursGames() {
        try {
            Session session = SessionTable.getSessionTable().getSession();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -24);
            List games = session.find("from game in class igx.stats.Game where game.time>? order by game.time desc", cal.getTime(), Hibernate.DATE);

            return games;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List getFinalGameTimeStateList(Game game) {
        try {
            Iterator timeStates = SessionTable.getSessionTable().getSession().iterate("from timestate in class igx.stats.TimeState"
                +" where timestate.game.id=? order by timestate.time desc, timestate.score desc", game.getId(), Hibernate.LONG);
            ArrayList winners = new ArrayList();
            int maxTime = -1;
            while (timeStates.hasNext()) {
                TimeState state = (TimeState) timeStates.next();
                if (maxTime < 0) {
                    maxTime = state.getTime();
                } else if (maxTime != state.getTime()) {
                    break;
                }

                winners.add(state);
            }
            return winners;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int getGamesWon(Player player) {
        int gamesWon = 0;
        try {
            Iterator games = player.getGames().iterator();
            while (games.hasNext()) {
                Game game = (Game) games.next();
                if (game.getWinner() == player) {
                    gamesWon++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gamesWon;
    }

    public static int getLastTurnNumberForGame(Game game) throws Exception {
        String query = "from timestate in class igx.stats.TimeState where gameID = ? order by time desc";
        Iterator timestates = SessionTable.getSessionTable().getSession().iterate(query, game.getId(), Hibernate.LONG);
        TimeState t = (TimeState)timestates.next();
        return t.getTime();
    }

    public static List getPlanetUpdatesForGameAndTurn(Game game, int turn) throws Exception {
        Object[] params = {game.getId(), new Integer(turn)};
        Type[] types = {Hibernate.LONG, Hibernate.INTEGER};
        String query = "from planetupdate in class igx.stats.PlanetUpdate where gameID = ? and time = ? order by planetID asc";
        return SessionTable.getSessionTable().getSession().find(query, params, types);
    }

    public static List getAttacksForGameAndTurn(Game game, int turn) throws Exception {
        Object[] params = {game.getId(), new Integer(turn)};
        Type[] types = {Hibernate.LONG, Hibernate.INTEGER};
        String query = "from attack in class igx.stats.Attack where gameID = ? and time = ? order by planetNum asc";
        return SessionTable.getSessionTable().getSession().find(query, params, types);
    }

}