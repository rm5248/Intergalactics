package  igx.stats.client;

import  igx.shared.Fleet;
import  igx.shared.GameInstance;
import  java.util.*;
import  igx.stats.*;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.Configuration;
import igx.bots.GameState;
import igx.bots.Constants;
import igx.bots.Planet;
import java.sql.Timestamp;


/**
 * put your documentation comment here
 */
public class Statistics extends Thread {

    private static SessionFactory sessions = null;

    static {
        Configuration cfg = null;
        try {
            // The link to the database
            cfg = new Configuration()
                .addClass(Attack.class)
                .addClass(Event.class)
                .addClass(Game.class)
                .addClass(PlanetUpdate.class)
                .addClass(Player.class)
                .addClass(TimeState.class);

            // The source of persistence sessions
            sessions = cfg.buildSessionFactory();
        } catch (Exception e) {
            System.out.println("Couldn't even get started:");
            e.printStackTrace();
        }
    }

    public static final int TIME_THRESHOLD = 200;
    private Date date;
    private int winnerID;
    private igx.stats.Player[] players;
    private List timeStates = new ArrayList();
    private List attacks = new ArrayList();
    private List events = new ArrayList();
    private List updates = new ArrayList();
    private static final int PLANETS = Constants.PLANETS;
    private int[] pTotalShips;
    private int[] pTotalPlanets;
    private int[] pTotalProduction;
    private int numPlayers;
    private int time = 0;
    private Session session;
    private Game statGame;

    /**
     * put your documentation comment here
     * @param     Date date
     * @param     igx.shared.Player[] player
     * @param     int numPlayers
     */
    public Statistics (Date date, igx.shared.Player[] player, int numPlayers) {
        super("Statistics");
        try {
            session = sessions.openSession();
            statGame = new Game();
            statGame.setTime(new Date());
            this.date = date;
            this.numPlayers = numPlayers;
            players = new igx.stats.Player[igx.bots.Constants.NEUTRAL+1];
            // NEUTRAL
            List l = session.find("from player in class igx.stats.Player where name = ?", "(neutral)", Hibernate.STRING);
            if (l.size() == 0) {
                Player neutral = new Player("(neutral)", true, null);
                session.save(neutral);
                players[igx.bots.Constants.NEUTRAL] = neutral;
            } else {
                players[igx.bots.Constants.NEUTRAL] = (Player)l.get(0);
            }
            try {
                // Cough up the players
                for (int i = 0; i < numPlayers; i++) {
                    System.out.println("Player " + i + ":" + player[i].name);
                    l = session.find("from player in class igx.stats.Player where name = ?", player[i].name, Hibernate.STRING);
                    if (l.size() == 0) {
                        players[i] = new Player();
                        players[i].setName(player[i].name);
                        players[i].setRobot(!player[i].isHuman);
                        session.save(players[i]);
                    } else {
                        players[i] = (Player)l.get(0);
                    }
                }
            } catch (Exception e) {
                System.out.println("Not cool:");
                e.printStackTrace();
            }
            pTotalShips = new int[numPlayers];
            pTotalPlanets = new int[numPlayers];
            pTotalProduction = new int[numPlayers];
        } catch (Exception e) {
            System.out.println("Couldn't create session factory:");
            e.printStackTrace();
        }
    }

    /**
     * put your documentation comment here
     */
    public void gameOver () {
        if (time >= TIME_THRESHOLD) {
            try {
                start();
            } catch (Exception e) {
                System.out.println("Tank city:");
                e.printStackTrace();
            }
        }
    }

    /**
     * put your documentation comment here
     */
    public void run () {
        try {
            System.out.println("Saving our shit...");
            // Players
            Set playerSet = new HashSet();
            for (int i = 0; i < numPlayers; i++) {
                playerSet.add(players[i]);
            }
            statGame.setPlayers(playerSet);
            System.out.println("TimeStates size: " + timeStates.size());
            // statGame.setTimeStates(timeStates);
            //statGame.setAttacks(attacks);
            //statGame.setPlanetUpdates(updates);
            //statGame.setEvents(events);
            statGame.setNumPlayers(numPlayers);
            statGame.setWinner(players[winnerID]);
            statGame.setNeutral(players[Constants.NEUTRAL]);
            session.save(statGame);
            for (int i = 0; i < timeStates.size(); i++) {
                session.save(timeStates.get(i));
            }
            for (int i = 0; i < updates.size(); i++) {
                session.save(updates.get(i));
            }
            for (int i = 0; i < attacks.size(); i++) {
                session.save(attacks.get(i));
            }
            for (int i = 0; i < events.size(); i++) {
                session.save(events.get(i));
            }
            System.out.println("Flushing...");
            session.flush();
            System.out.println("Closing...");
            session.close();
            System.out.println("Done.");
        } catch (Exception e) {
            System.out.println("Couldn't save game:");
            e.printStackTrace();
        }
    }

    /**
     * put your documentation comment here
     * @param code
     * @param text
     */
    public void reportEvent (int code, String text) {
        Event e = new Event();
        e.setGame(statGame);
        e.setEventID(code);
        e.setTime(time);
        e.setMessage(text);
        events.add(e);
    }

    /**
     * put your documentation comment here
     * @param numShips
     * @param p
     */
    public void reportReinforcements (int numShips, igx.shared.Planet p) {
        Attack a = new Attack();//time, igx.shared.Planet.char2num(p.planetChar),
                //p.owner.number, numShips, 0, true);
        a.setGame(statGame);
        a.setTime((short)time);
        a.setPlanetID((byte)igx.shared.Planet.char2num(p.planetChar));
        a.setPlayer(players[p.owner.number]);
        a.setShips((short)numShips);
        a.setRatio((byte)0);
        a.setNewAttack(true);
        attacks.add(a);
    }

    // Main update method
    public void update (GameState game, GameInstance gameInstance) {
        try {
            // Begin collecting stats from planets
            for (int i = 0; i < PLANETS; i++) {
                Planet p = game.getPlanet(i);
                int owner = p.getOwner();
                PlanetUpdate pu = new PlanetUpdate();//i, time, owner, p.getShips(),
                        //p.getRatio(), p.getProduction());
                pu.setGame(statGame);
                pu.setPlanetID((byte)i);
                pu.setTime((short)time);
                pu.setPlayer(players[owner]);
                pu.setShips((short)p.getShips());
                pu.setRatio((byte)p.getRatio());
                pu.setProduction((byte)p.getProduction());
                pu.setX((byte)p.getX());
                pu.setY((byte)p.getY());
                updates.add(pu);
                if (owner != Constants.NEUTRAL) {
                    pTotalShips[owner] += p.getShips();
                    pTotalPlanets[owner]++;
                    pTotalProduction[owner] += p.getProduction();
                }
            }
            // Begin collecting stats from fleets
            Fleet f = gameInstance.fleets.first;
            while (f != null) {
                if (f.distance <= 0) {
                    boolean newAttack = false;
                    if ((f.distance + Constants.FLEET_SPEED) > 0)
                        newAttack = true;
                    Attack a = new Attack();//(time, igx.shared.Planet.char2num(f.destination.planetChar),
                            //f.owner.number, f.ships, f.ratio, newAttack);
                    a.setGame(statGame);
                    a.setTime((short)time);
                    a.setPlanetID((byte)igx.shared.Planet.char2num(f.destination.planetChar));
                    a.setPlayer(players[f.owner.number]);
                    a.setShips((short)f.ships);
                    a.setRatio((byte)f.ratio);
                    a.setNewAttack(newAttack);
                    //a.setPlayerID();
                    attacks.add(a);
                }
                pTotalShips[f.owner.number] += f.ships;
                // Next fleet
                f = f.next;
            }
            // Do timestates
            int winnerScore = gameInstance.player[winnerID].score;
            for (int i = 0; i < numPlayers; i++) {
                int score = gameInstance.player[i].score;
                TimeState ts = new TimeState();//0, i, time, pTotalShips[i], pTotalPlanets[i],
                        //pTotalProduction[i], score);
                ts.setGame(statGame);
                ts.setTime((short) time);
                ts.setPlayer(players[i]);
                ts.setNumPlanets((byte) pTotalPlanets[i]);
                ts.setNumShips(pTotalShips[i]);
                ts.setTotalProduction((short) pTotalProduction[i]);
                ts.setScore(score);
                if (score > winnerScore) {
                    winnerScore = score;
                    winnerID = i;
                }
                timeStates.add(ts);
                pTotalShips[i] = 0;
                pTotalPlanets[i] = 0;
                pTotalProduction[i] = 0;
            }
            time++;
        } catch (Exception e) {
            System.out.println("Stats error:");
            e.printStackTrace();
        }
    }
}



