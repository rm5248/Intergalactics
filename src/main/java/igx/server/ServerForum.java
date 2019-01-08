package igx.server;

// ServerForum.java
import igx.shared.*;
import igx.bots.Bot;
import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.Point;

public class ServerForum extends Forum {

    protected Monitor m = new Monitor();
    private Vector clients = new Vector();
    private Vector dispatchers = new Vector();
    public MessageQueue queue;
    public static final String BOT_FILE_NAME = "bots.txt";
    public String rootPath;
    static RankingSystem rankingSystem;

    public ServerForum(MessageQueue queue, String path) {
        super(generateBotList(path));
        this.rootPath = path;
        this.queue = queue;
    }

    protected boolean abandonGame(String name) {
        Player p = getPlayer(name);
        Game g = p.game;
        p.isActive = false;
        if (p != null) {
            if (p.game != null) {
                if (super.abandonGame(name)) {
                    Client.lockClients();
                    sendAll(Params.FORUM);
                    sendAll(Params.ABANDONGAME);
                    sendAll(name);
                    Client c = getClient(name);
                    c.selectedGame = null;
                    Client.unlockClients();
                    if (g.creator.equals(name) && !g.inProgress) {
                        for (int i = 0; i < g.numPlayers; i++) {
                            g.player[i].game = null;
                            Client cl = getClient(g.player[i].name);
                            if (cl != null) {
                                cl.selectedGame = null;
                            }
                        }
                        games.removeElement(g);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected Player addPlayer(String name) {
        Client.lockClients();
        sendAll(Params.FORUM);
        sendAll(Params.PLAYERARRIVED);
        sendAll(name);
        Client.unlockClients();
        // Player p = getOldPlayer(name);
        Player p = getPoolPlayer(name);
        if (p != null) {
            p.isPresent = true;
            return super.addPlayer(p);
        } else {
            return super.addPlayer(name);
        }
    }

    protected boolean addRobot(String name, String gameName) {
        try {
            int split = name.indexOf('#');
            int skillNumber = 0;
            String error = null;
            Bot botInstance = null;
            if (split != -1) {
                // CUSTOM ROBOT!
                // Unlock in case this look-up takes a while...
                m.unlock();
                URL url = null;
                Class robotClass = null;
                if (split == name.length() - 1) {
                    error = "No skill number indicated.";
                } else {
                    try {
                        int classSplit = name.lastIndexOf('|');
                        int extSplit = name.lastIndexOf('.');
                        String location = "";
                        if (classSplit < 3) {
                            throw new MalformedURLException("URL too short");
                        } else if (name.substring(classSplit - 3, classSplit).equals("jar")) {
                            location = name.substring(0, classSplit);
                        } else {
                            location = name.substring(0, classSplit) + '/';
                        }
                        url = new URL(location);
                        String file = name.substring(classSplit + 1, extSplit);
                        System.out.println("Trying to get \"" + file + "\" at \"" + location + "\".");
                        skillNumber = Integer.parseInt(name.substring(split + 1));
                        robotClass = CustomRobot.getRobot(url, file, skillNumber);
                    } catch (MalformedURLException e) {
                        error = e.toString();
                    } catch (NumberFormatException f) {
                        error = "Skill number not an integer.";
                    } catch (Exception g) {
                        error = "Unexpected error: " + g;
                    }
                    if ((robotClass == null) && (error == null)) {
                        error = "Robot class not found";
                    }
                    if (error == null) {
                        try {
                            botInstance = (Bot) robotClass.newInstance();
                            if (botInstance == null) {
                                throw new NullPointerException("Couldn't instantiate robot.");
                            }
                        } catch (ClassCastException g) {
                            error = "That class is not a robot!";
                        } catch (Exception h) {
                            error = "Problem with robot: " + h;
                        }
                        if (error == null) {
                            System.out.println("Getting number of bots...");
                            int n = botInstance.numberOfBots();
                            if (skillNumber >= n) {
                                error = "Skill range from 0 to " + (n - 1) + " for this robot.";
                            } else {
                                System.out.println("Creating bot...");
                                String botName = botInstance.createName(skillNumber);
                                Robot r = new Robot("Custom", botName, 0);
                                r.setClass(robotClass);
                                r.setSkill(skillNumber);
                                // Better lock it up again
                                m.lock();
                                if (super.addCustomRobot(r, gameName)) {
                                    System.out.println("Added bot...");
                                    Client.lockClients();
                                    sendAll(Params.FORUM);
                                    sendAll(Params.ADDCUSTOMCOMPUTERPLAYER);
                                    sendAll(gameName);
                                    sendAll(Params.ACK);
                                    sendAll(botName);
                                    Client.unlockClients();
                                    System.out.println("Sent success");
                                    System.out.println("Bot class: " + robotClass);
                                    return true;
                                } else {
                                    m.unlock();
                                    error = "Couldn't add robot to game.";
                                }
                            }
                        }
                    }
                }
                if (error != null) {
                    m.lock();
                    Game g = getGame(gameName);
                    Client c = getClient(g.creator);
                    Client.lockClients();
                    c.send(Params.FORUM);
                    c.send(Params.ADDCUSTOMCOMPUTERPLAYER);
                    c.send(gameName);
                    c.send(Params.NACK);
                    c.send(error);
                    Client.unlockClients();
                    System.out.println("Custom robot didn't work: " + error);
                }
                return false;
            } else {
                boolean okay = true;
                if (name.equals("?")) {
                    int n = botList.length;
                    Random random = new Random();
                    int i = random.nextInt() % n;
                    i = (i >= 0) ? i : -i;
                    Game g = getGame(gameName);
                    if (g != null) {
                        while (g.getPlayer(botList[i].name) != null) {
                            i = random.nextInt() % n;
                            i = (i >= 0) ? i : -i;
                        }
                        name = botList[i].name;
                    } else {
                        okay = false;
                    }
                }
                if (okay && super.addRobot(name, gameName)) {
                    Client.lockClients();
                    sendAll(Params.FORUM);
                    sendAll(Params.ADDCOMPUTERPLAYER);
                    sendAll(gameName);
                    sendAll(name);
                    Client.unlockClients();
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("Troubs: " + e);
        }
        return false;
    }

    public static void complain(String complaint) {
        System.out.println(complaint);
        System.exit(1);
    }

    protected boolean createGame(String name, String gameName) {
        Client c = getClient(name);
        Game g = getGame(gameName);
        c.me.game = c.selectedGame;
        if ((c != null) && (g == null)) {
            if (super.createGame(name, gameName)) {
                Client.lockClients();
                sendAll(Params.FORUM);
                sendAll(Params.NEWGAME);
                sendAll(gameName);
                sendAll(name);
                c.selectedGame = g;
                Client.unlockClients();
                return true;
            }
        }
        return false;
    }

    protected void gameOver(String gameName) {
        Game g = getGame(gameName);
        super.gameOver(gameName);
        Dispatcher d = getDispatcher(gameName);
        if ((g != null) && (d != null)) {
            int winNumber = d.doRankings(rankingSystem);
            String winner;
            if (winNumber == Params.NEUTRAL) {
                winner = "<nobody>";
            } else {
                winner = g.player[winNumber].name;
            }
            String gameReport = gameName + " ends at " + d.game.turn + ":" + d.game.segment;
            gameReport += ". Winner: " + winner;
            System.out.println(gameReport);
            // Get all the watchers out of there
            for (int i = 0; i < clients.size(); i++) {
                Client c = (Client) (clients.elementAt(i));
                if (c.selectedGame == g) {
                    c.selectedGame = null;
                    c.me.game = null;
                    c.me.isActive = false;
                    c.me.inGame = false;
                }
            }
            // Get all additional players out of there
            for (int i = 0; i < g.numPlayers; i++) {
                Player p = g.player[i];
                if (p.isHuman && p.isActive && (p.game == g)) {
                    Client c = getClient(p.name);
                    c.selectedGame = null;
                    c.me.game = null;
                    c.me.isActive = false;
                    c.me.inGame = false;
                }
            }
            Client.lockClients();
            sendAll(Params.FORUM);
            sendAll(Params.GAMEENDED);
            sendAll(gameName);
            sendAll(winner);
            Client.unlockClients();
            dispatchers.removeElement(d);
        }
    }

    protected static Robot[] generateBotList(String path) {
        BufferedReader br;
        Vector bots = new Vector();
        File botFile = new File(path + BOT_FILE_NAME);
        try {
            br = new BufferedReader(new FileReader(path + BOT_FILE_NAME));
            String botType = br.readLine();
            String bot = br.readLine();
            while (bot != null) {
                Class botClass = getBotClass(bot);
                Bot botInstance = null;
                try {
                    botInstance = (Bot) botClass.newInstance();
                } catch (Exception f) {
                }
                int n = botInstance.numberOfBots();
                for (int i = 0; i < n; i++) {
                    String botName = botInstance.createName(i);
                    Rank rank = rankingSystem.getRank(botName + "(robot)");
                    int ranking = Params.STANDARD_RANKING;
                    if (rank != null) {
                        ranking = rank.rank;
                    }
                    Robot robot = new Robot(botType, botName, ranking);
                    robot.setClass(botClass);
                    robot.setSkill(i);
                    bots.addElement(robot);
                }
                botType = br.readLine();
                bot = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Problem with 'bot file: " + e);
            System.out.println( botFile.getAbsolutePath() );
            return new Robot[0];
        }
        int n = bots.size();
        Robot[] robotList = new Robot[n];
        for (int i = 0; i < n; i++) {
            robotList[i] = (Robot) (bots.elementAt(i));
        }
        return robotList;
    }
    // Gets bot class and makes sure it's cool...

    protected static Class getBotClass(String className) {
        Class robotClass = null;
        try {
            robotClass = Class.forName(className);
            // Try to instantiate as a robot
            Bot robot = (Bot) robotClass.newInstance();
        } catch (ClassNotFoundException e) {
            complain("'bot class name: " + className + " not found.");
        } catch (ClassCastException e) {
            complain("This... thing... is not a 'bot: " + className + "!");
        } catch (Exception e) {
            complain("Problem with your 'bot, " + className + ". " + e + ".");
        }
        return robotClass;
    }

    public Client getClient(String name) {
        int n = clients.size();
        for (int i = 0; i < n; i++) {
            Client c = (Client) (clients.elementAt(i));
            if (c.me.name.equals(name)) {
                return c;
            }
        }
        return null;
    }
    // Gets a game dispatcher (once it's started)

    public Dispatcher getDispatcher(String name) {
        int n = dispatchers.size();
        for (int i = 0; i < n; i++) {
            Dispatcher d = (Dispatcher) (dispatchers.elementAt(i));
            if (d.name.equals(name)) {
                return d;
            }
        }
        return null;
    }
    // Gets an old player back in the loop

    private Player getOldPlayer(String name) {
        int n = dispatchers.size();
        for (int i = 0; i < n; i++) {
            Dispatcher d = (Dispatcher) (dispatchers.elementAt(i));
            Player p = d.getPlayer(name);
            if ((p != null) && (p.isActive)) {
                p.isPresent = true;
                return p;
            }
        }
        return null;
    }
    // Returns the game that this player is in, or null otherwise

    public Game inGame(String name) {
        int n = games.size();
        for (int i = 0; i < n; i++) {
            Game g = (Game) (games.elementAt(i));
            Player p = g.getPlayer(name);
            if ((p != null) && p.isActive) {
                return g;
            }
        }
        return null;
    }

    protected boolean joinGame(String name, String gameName) {
        Client c = getClient(name);
        c.me.game = c.selectedGame;
        if (super.joinGame(name, gameName)) {
            Client.lockClients();
            sendAll(Params.FORUM);
            sendAll(Params.JOINGAME);
            sendAll(gameName);
            sendAll(name);
            c.selectedGame = getGame(gameName);
            Client.unlockClients();
            Player p = getPlayer(name);
            p.inGame = true;
            return true;
        } else {
            return false;
        }
    }

    protected void message(String player, String text, int destination) {
        super.message(player, text, destination);
        Client.lockClients();
        sendAll(Params.FORUM);
        sendAll(Params.SENDMESSAGE);
        sendAll(player);
        sendAll(new Integer(destination).toString());
        sendAll(text);
        Client.unlockClients();
    }

    public void messageEvent(Message message) {
        m.lock();
        try {
            super.messageEvent(message);
        } catch (Exception e) {
            System.out.println("Messed");
            e.printStackTrace();
        }
        m.unlock();
    }

    public void registerClient(String alias, Client c) {
        c.setName(alias);
        m.lock();
        // See if anybody else by this name is here...
        Client oldClient = getClient(alias);
        // Give them the boot!
        if (oldClient != null) {
            Client.lockClients();
            oldClient.send(Params.FORUM);
            oldClient.send(Params.PLAYERQUITTING);
            oldClient.send(alias);
            Client.unlockClients();
            //removePlayer(alias);
            if (oldClient.me != null) {
                Game g = oldClient.me.game;
                if ((g != null) && (!g.inProgress)) {
                    if (g.creator.equals(oldClient.me.name)) {
                        abandonGame(alias);
                    }
                }
            }
            removePlayer(alias);
            oldClient.serverQueue = null;
        }
        // Notify other players of this client arriving
        c.me = addPlayer(alias);
        // Send list of players to client
        int n = players.size();
        int i;
        Player p;
        for (i = 0; i < n; i++) {
            p = (Player) players.elementAt(i);
            c.send(p.name);
            c.send(getPoolPlayer(p.name) != null);
        }
        // Indicate no more players
        c.send(Params.ENDTRANSMISSION);
        // Send list of games to client
        n = games.size();
        Game g;
        for (i = 0; i < n; i++) {
            g = (Game) games.elementAt(i);
            // Send game name
            c.send(g.name);
            // Send status (in progress or new)
            if (g.inProgress) {
                c.send(Params.ACK);
            } else {
                c.send(Params.NACK);
            }
            // Send number of players
            int numPlayers = g.numPlayers;
            c.send(numPlayers);
            // Send players of game
            for (int j = 0; j < numPlayers; j++) {
                // Name
                c.send(g.player[j].name);
                // Active
                c.send(g.player[j].isActive);
            }
        }
        // Indicate no more games
        c.send(Params.ENDTRANSMISSION);
        //
        // g = inGame(c.me.name);
        // if (g != null) {
        if (c.me.game != null) {
            g = c.me.game;
            c.selectedGame = g;
            c.me.isPresent = true;
            c.send(Params.ACK);
            c.send(g.name);
            if (g.inProgress) {
                Dispatcher d = getDispatcher(g.name);
                if (d != null) {
                    d.briefWatcher(c);
                    c.me = d.getPlayer(alias);
                }
                // Replace the old client with the new
                // d.substituteClient(c);
                // d.clients.setElementAt(c, c.me.number);
            }
        } else {
            c.send(Params.NACK);
        }
        // Add client to list
        clients.addElement(c);
        m.unlock();
    }

    protected void removePlayer(String name) {
        super.removePlayer(name);
        Client c = getClient(name);
        if (c != null) {
            clients.removeElement(c);
            Client.lockClients();
            sendAll(Params.FORUM);
            sendAll(Params.PLAYERQUITTING);
            sendAll(name);
            Client.unlockClients();
        }
    }

    protected boolean removeRobot(String name, String gameName) {
        if (super.removeRobot(name, gameName)) {
            Client.lockClients();
            sendAll(Params.FORUM);
            sendAll(Params.REMOVECOMPUTERPLAYER);
            sendAll(gameName);
            sendAll(name);
            Client.unlockClients();
            return true;
        } else {
            return false;
        }
    }

    protected void sendAll(String message) {
        for (int i = 0; i < clients.size(); i++) {
            ((Client) (clients.elementAt(i))).send(message);
        }
    }

    protected void startGame(String gameName,
            String customMap) {
        Game g = getGame(gameName);
        if (g != null) {
            super.startGame(gameName, customMap);
            // Do custom map
            Point[] map = null;
            if (!customMap.equals(Params.RANDOMMAP)) {
                try {
                    map = MapReader.readMap(g.numPlayers, customMap);
                } catch (IOException e) {
                    System.out.println("Map: " + g.numPlayers + "/" + customMap + " is toast.");
                }
            }
            Vector players = new Vector();
            Vector robots = new Vector();
            for (int i = 0; i < g.numPlayers; i++) {
                if (g.player[i].isHuman) {
                    Client c = getClient(g.player[i].name);
                    players.addElement(c);
                } else {
                    if (!g.player[i].customRobot) {
                        robots.addElement(getRobot(g.player[i].name));
                    } else {
                        robots.addElement(g.player[i].r);
                    }
                }
            }
            Robot[] bot = new Robot[robots.size()];
            for (int i = 0; i < robots.size(); i++) {
                bot[i] = (Robot) (robots.elementAt(i));
            }
            Client.lockClients();
            sendAll(Params.FORUM);
            sendAll(Params.STARTGAME);
            sendAll(g.name);
            sendAll(customMap);
            String gameReport = "Game " + g.name + " with";
            for (int i = 0; i < g.numPlayers; i++) {
                if (i > 0) {
                    gameReport += ",";
                }
                gameReport += " " + g.player[i].name;
            }
            gameReport += " (";
            gameReport += new Date(System.currentTimeMillis()).toString();
            gameReport += ") begins.";
            System.out.println(gameReport);
            Client.unlockClients();
            Dispatcher d = new Dispatcher(g, players, bot, queue, map);
            dispatchers.addElement(d);
        }
    }

    public boolean uniqueName(Client c) {
        String name = c.me.name;
        m.lock();
        if (getPlayer(name) != null) {
            c.send(Params.PLAYERQUITTING);
            m.unlock();
            return false;
        }
        c.send(Params.ENDTRANSMISSION);
        m.unlock();
        return true;
    }

    protected void watchGame(String name, String gameName) {
        Client c = getClient(name);
        Dispatcher d = getDispatcher(gameName);
        if ((d != null) && (c != null)) {
            d.briefWatcher(c);
        }
    }
}
