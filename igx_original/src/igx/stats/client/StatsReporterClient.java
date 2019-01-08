package igx.stats.client;

import java.util.*;
import java.rmi.Naming;
import igx.stats.*;
import java.sql.Timestamp;
import net.sf.hibernate.*;

public class StatsReporterClient {
/*
    private static SessionFactory sessions = null;

    static {
        Datastore ds = null;
        try {
            // The link to the database
            ds = Hibernate.createDatastore();
            // The objects this datastore is configured to persist
            ds.storeClass(Attack.class);
            ds.storeClass(Event.class);
            ds.storeClass(Game.class);
            ds.storeClass(PlanetUpdate.class);
            ds.storeClass(Player.class);
            ds.storeClass(TimeState.class);

            // The source of persistence sessions
            sessions = ds.buildSessionFactory();
        } catch (Exception e) {
            System.out.println("Couldn't even get started:");
            e.printStackTrace();
        }
    }

    public static void doReport (Date date,
				 int winnerID,
				 String[] playerNames,
				 Vector timeStates,
				 Vector attacks,
				 Vector events,
				 Vector updates) {
        try {
            System.out.println("Persisting game.");
            Session session = sessions.openSession();
            Player[] players = new Player[playerNames.length];
            for (int i = 0; i < players.length; i++) {
                players[i] = new Player(playerNames[i]);
            }
            Game game = new Game(Calendar.getInstance().getTime(), players, winnerID);
            session.save(game);
            long gameID = game.getId();
            System.out.println("Saved Game ID: " + gameID);
            System.out.println("Persisting timestates.");
            for (int i = 0; i < timeStates.size(); i++) {
                System.out.println("Timestate #" + i);
                TimeState o = (TimeState)timeStates.elementAt(i);
                o.setGameID(gameID);
                session.save(o);
            }
            System.out.println("Flushing...");
            session.flush();
            System.out.println("Flushed.");
            session.close();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    */
}
