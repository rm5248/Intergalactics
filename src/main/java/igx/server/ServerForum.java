package igx.server;

// ServerForum.java
import igx.shared.*;
import igx.bots.Bot;
import igx.generated.Tables;
import igx.generated.tables.records.RankRecord;
import igx.generated.tables.records.UsersRecord;
import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class ServerForum extends Forum {

    protected Monitor m = new Monitor();
    private List<ClientConnection> clients;
    private Vector dispatchers = new Vector();
    public MessageQueue queue;
    public static final String BOT_FILE_NAME = "bots.txt";
    public String rootPath;
    static RankingSystem rankingSystem;
    
    private DSLContext m_context;
    
    private static final Logger logger = LogManager.getLogger();

    public ServerForum(MessageQueue queue, String path) {
        super(generateBotList(path));
        this.rootPath = path;
        this.queue = queue;
    }
    
    public ServerForum(MessageQueue queue, String path, DSLContext ctx) {
        super(generateBotList(path));
        this.rootPath = path;
        this.queue = queue;
        m_context = ctx;
        clients = new LinkedList<>();
    }
    
    /**
     * Checks to see if the user already exists in the database or not.
     * 
     * @param alias The username to check
     * @return true if the user already exists, false otherwise
     */
    boolean userAlreadyExists( String alias ){
        int exists = m_context.selectCount().from( Tables.USERS )
                .where( Tables.USERS.USERNAME.eq( alias ) )
                .fetchOne().value1();
        
        if( exists > 0 ){
            return true;
        }
        
        return false;
    }
    
    /**
     * Create a new user in the system.
     * 
     * @param alias The username of the user
     * @param password The password for the user
     * @return true if we were able to create them, false if we were not.
     */
    boolean createNewUser( String alias, String password ){ 
        m_context.transaction( configuration -> {
            DSLContext ctx = DSL.using( configuration );
            
            UsersRecord newUser = ctx.newRecord( Tables.USERS );
            newUser.setUsername( alias );
            // TODO get shiro in here to encrypt the password
            newUser.setPassword( password );
            
            newUser.store();
            
            RankRecord newRank = ctx.newRecord( Tables.RANK );
            newRank.setUserid( newUser.getUserid() );
            newRank.store();
        });
        
        return true;
    }
    
    boolean checkUserPassword( String alias, String password ){
        String dbPassword = m_context.select( Tables.USERS.PASSWORD )
                .from( Tables.USERS )
                .where( Tables.USERS.USERNAME.eq( alias ) )
                .fetchOne( Tables.USERS.PASSWORD );
        
        if( dbPassword == null ){
            return false;
        }
        
        //TODO get shiro in here to encrypt
        return dbPassword.equals( password );
    }
    
    String[] getBulletins(){
        return new String[0];
    }
    
    Robot[] getBots(){
        return botList;
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
                    ClientConnection c = getClient(name);
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
        for( ClientConnection c : clients ){
            c.playerArrived( name );
        }
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
                                    Game g = getGame( gameName );
                                    for( ClientConnection conn : clients ){
                                        conn.addedCustomBotToGame( r, g );
                                    }
                                    logger.debug( "Added bot with class {}", robotClass );
                                    return true;
                                } else {
                                    m.unlock();
                                    error = "Couldn't add robot to game.";
                                }
                            }
                        }
                    }
                }
                //TODO is this next block needed at all?
//                if (error != null) {
//                    m.lock();
//                    Game g = getGame(gameName);
//                    ClientConnection c = getClient(g.creator);
//                    Client.lockClients();
//                    c.send(Params.FORUM);
//                    c.send(Params.ADDCUSTOMCOMPUTERPLAYER);
//                    c.send(gameName);
//                    c.send(Params.NACK);
//                    c.send(error);
//                    Client.unlockClients();
//                    System.out.println("Custom robot didn't work: " + error);
//                }
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
                    Game g = getGame(gameName);
                    Robot r = getRobot(name);
                    for( ClientConnection conn : clients ){
                        conn.addedBotToGame(r, g);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error( e );
        }
        return false;
    }

    public static void complain(String complaint) {
        System.out.println(complaint);
        System.exit(1);
    }

    protected boolean createGame(String name, String gameName) {
        ClientConnection c = getClient(name);
        Game g = getGame(gameName);
        c.getPlayer().game = g;
        //TODO what is the next line for??
        //c.me.game = c.selectedGame;
        if ((c != null) && (g == null)) {
            if (super.createGame(name, gameName)) {
                for( ClientConnection conn : clients ){
                    conn.gameCreated( name, g );
                }
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
            String gameReport = String.format( "%s ends at %d:%d.  Winner: %s",
                    gameName,
                    d.game.turn,
                    d.game.segment,
                    winner );
            System.out.println(gameReport);
            // Get all the watchers out of there
            for (int i = 0; i < clients.size(); i++) {
                Client c = (Client) (clients.get(i));
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
            for( ClientConnection conn : clients ){
                conn.gameEnded(g, winner);
            }
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
            System.out.println(botFile.getAbsolutePath());
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

    private ClientConnection getClient(String name) {        
        for( ClientConnection client : clients ){
            if( client.getName().equals( name ) ){
                return client;
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
            Game g = games.get(i);
            Player p = g.getPlayer(name);
            if ((p != null) && p.isActive) {
                return g;
            }
        }
        return null;
    }

    protected boolean joinGame(String name, String gameName) {
        ClientConnection c = getClient(name);
        //TODO need to figure out what the next line is supposed to do
        //c.me.game = c.selectedGame;
        Game g = getGame(gameName);
        if (super.joinGame(name, gameName)) {
            for( ClientConnection conn : clients ){
                conn.playerJoinedGame(g, name);
            }
            //TODO need to figure out what the next line is supposed to do
            //c.selectedGame = getGame(gameName);
            Player p = getPlayer(name);
            p.inGame = true;
            return true;
        } else {
            return false;
        }
    }

    protected void message(String player, String text, int destination) {
        super.message(player, text, destination);
        for( ClientConnection conn : clients ){
            conn.sendMessageToForum(player, text, destination);
        }
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

    void registerClient(String alias, ClientConnection c) throws IOException {
        m.lock();
        // See if anybody else by this name is here...
        ClientConnection oldClient = getClient(alias);
        // Give them the boot!
//        if (oldClient != null) {
//            Client.lockClients();
//            oldClient.send(Params.FORUM);
//            oldClient.send(Params.PLAYERQUITTING);
//            oldClient.send(alias);
//            Client.unlockClients();
//            //removePlayer(alias);
//            if (oldClient.me != null) {
//                Game g = oldClient.me.game;
//                if ((g != null) && (!g.inProgress)) {
//                    if (g.creator.equals(oldClient.me.name)) {
//                        abandonGame(alias);
//                    }
//                }
//            }
//            removePlayer(alias);
//            oldClient.serverQueue = null;
//        }
        // Notify other players of this client arriving
        //c.me = addPlayer(alias);
        // Send list of players to client
        c.sendPlayerList( players );
        // Send list of games to client
        c.sendGameList( games );

        c.nack();
        // This block of code appears to add the client to the game if they 
        // disconnected and then reconnected?
//        if (c.me.game != null) {
//            g = c.me.game;
//            c.selectedGame = g;
//            c.me.isPresent = true;
//            c.send(Params.ACK);
//            c.send(g.name);
//            if (g.inProgress) {
//                Dispatcher d = getDispatcher(g.name);
//                if (d != null) {
//                    d.briefWatcher(c);
//                    c.me = d.getPlayer(alias);
//                }
//                // Replace the old client with the new
//                // d.substituteClient(c);
//                // d.clients.setElementAt(c, c.me.number);
//            }
//        } else {
//            c.send(Params.NACK);
//        }
        // Add client to list
        clients.add(c);
        m.unlock();
    }

    protected void removePlayer(String name) {
        super.removePlayer(name);
        ClientConnection c = getClient(name);
        if (c != null) {
            clients.remove(c);
            for( ClientConnection conn : clients ){
                conn.clientQuit( name );
            }
        }
    }

    protected boolean removeRobot(String name, String gameName) {
        if (super.removeRobot(name, gameName)) {
            for( ClientConnection client : clients ){
                client.robotRemovedFromGame( getRobot(name), getGame(gameName) );
            }
            return true;
        } else {
            return false;
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
                } else if (!g.player[i].customRobot) {
                    robots.addElement(getRobot(g.player[i].name));
                } else {
                    robots.addElement(g.player[i].r);
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
            String gameReport =  "Game " + g.name + " with";
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
