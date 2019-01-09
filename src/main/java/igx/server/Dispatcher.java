package igx.server;

// Dispatcher.java
import igx.shared.*;
import java.util.*;
import igx.bots.Bot;
import igx.bots.RobotServer;
import igx.bots.RobotMessenger;
import igx.bots.GameState;
import java.awt.Color;
import java.awt.Point;

public class Dispatcher implements RobotServer, UI, MessageListener {

    private MessageQueue serverQueue;
    private MessageQueue gameQueue;
    public String name;
    GameInstance game;
    Game thisGame;
    GameTimer timer;
    Vector clients;
    Player[] player;
    Bot[] bot;
    int numPlayers, numBots, numActivePlayers;
    RobotMessenger rm = null;
    long randomSeed;
    private Vector arrivals;
    GameState gameState, oldState;
    private Vector[] messages;
    private Monitor m;
    Vector fleets = new Vector();
    Vector fleetInfos = new Vector();
    long newSeed;
    boolean needNewSeed = false;
    boolean stopTimer = false;
    //Statistics stats;

    public Dispatcher(Game thisGame, Vector clients, Robot[] robot, MessageQueue serverQueue, Point[] map) {
        this.serverQueue = serverQueue;
        this.clients = clients;
        this.thisGame = thisGame;
        m = new Monitor();
        gameQueue = new MessageQueue("Game \"" + thisGame.name + "\" Queue");
        for (int i = 0; i < clients.size(); i++) {
            ((Client) (clients.elementAt(i))).setGame(gameQueue);
        }
        numActivePlayers = clients.size();
        name = thisGame.name;
        player = thisGame.player;
        numPlayers = thisGame.numPlayers;
        for (int i = 0; i < thisGame.numPlayers; i++) {
            player[i].number = i;
            player[i].isActive = true;
            player[i].inGame = true;
        }
        // STATS
        //stats = new Statistics(new Date(System.currentTimeMillis()), player, thisGame.numPlayers);
        numBots = robot.length;
        bot = new Bot[numBots];
        try {
            for (int i = 0; i < numBots; i++) {
                bot[i] = (Bot) (robot[i].botClass.newInstance());
            }
        } catch (Exception e) {
            System.out.println("Problem instantiating bot: " + e);
        }
        randomSeed = System.currentTimeMillis();
        game = new GameInstance(randomSeed, numPlayers, player);
        game.registerUI(this);
        // Set up robot stuff
        gameState = new GameState(game, null);
        // Set up message repositories
        messages = new Vector[numBots];
        for (int i = 0; i < numBots; i++) {
            messages[i] = new Vector();
        }
        // Initialize robots with game
        if (numBots > 0) {
            rm = new RobotMessenger(this, numBots);
        }
        for (int i = 0; i < numBots; i++) {
            bot[i].initializeBot(gameState, rm, nameToNumber(robot[i].name), robot[i].skill, false);
        }
        arrivals = new Vector();
        gameQueue.setMessageListener(this);
        gameQueue.start();
        timer = new GameTimer(this, Params.TIMESLICE);
        // Send initial seed.
        Client.lockClients();
        send(new Long(randomSeed).toString());
        if (map == null) {
            send(Params.RANDOMMAP);
        } else {
            for (int i = 0; i < map.length; i++) {
                send(new Integer(map[i].x).toString());
                send(new Integer(map[i].y).toString());
            }
            send(Params.RANDOMMAP);
        }
        Client.unlockClients();
        if (map != null) {
            game.setMap(map);
        }
        // Begin timer
        timer.start();
        // Call first robot update
        updateRobots(game);
    }

    // Called by the timer to advance the fleets.
    public void advance() {
        m.lock();
        Client.lockClients();
        if (needNewSeed) {
            send(Params.UPDATE);
            send(new Long(newSeed).toString());
            game.setSeed(newSeed);
            needNewSeed = false;
        }
        for (int i = 0; i < fleets.size(); i++) {
            FleetInfo fi = ((FleetInfo) fleetInfos.elementAt(i));
            send(fi.from);
            send(fi.to);
            send(fi.ships);
        }
        send(Params.ENDTRANSMISSION);
        game.update(fleets);
        fleets.removeAllElements();
        fleetInfos.removeAllElements();
        updateRobots(game);
        game.arrivalList = new Vector();
        Client.unlockClients();
        m.unlock();
        if (game.turn == 300) {
            for (int i = 0; i < clients.size(); i++) {
                Client c = (Client) clients.elementAt(i);
                playerQuit(c, Params.QUIT_SIGNAL);
            }
            if (stopTimer) {
                timer.stopTimer();
            }
        }
    }

    /**
     * This method was created in VisualAge.
     *
     * @return boolean
     */
    public boolean anybodyPlaying() {
        boolean result = false;
        int readyMan = -1;
        for (int i = 0; i < numPlayers; i++) {
            if (player[i].isHuman && thisGame.activePlayer[i] && player[i].isPresent) {
                if (player[i].status == Params.READY_SIGNAL) {
                    readyMan = i;
                } else {
                    result = true;
                    break;
                }
            }
        }
        if (!result) {
            // Make the ready people bail
            if (readyMan != -1) {
                gameQueue.addMessage(Message.playerQuitGame(player[readyMan].name, Params.QUIT_SIGNAL));
                return true;
            }
            // Boot everybody out
            for (int i = 0; i < numPlayers; i++) {
                if (player[i].isHuman && player[i].isActive) {
                    Client c = getClient(player[i].name);
                    if (c != null) {
                        c.me.isActive = false;
                        c.me.inGame = false;
                        c.setGame(null);
                        //thisGame.gamePool.removeElement(c.me);
                    }
                }
            }
        }
        return result;
    }

    // Lets the watcher know what's what and who's who.
    public void briefWatcher(Client c) {
        m.lock();
        Client.lockClients();
        // Need to use a new number seed
        needNewSeed = true;
        newSeed = System.currentTimeMillis();
        // Start transfer
        c.send(Params.FORUM);
        c.send(Params.WATCHGAME);
        // Send name of game
        c.send(name);
        c.send(game.players);
        c.send(game.turn);
        c.send(game.segment);
        for (int i = 0; i < numPlayers; i++) {
            Player p = player[i];
            c.send(p.name);
            c.send(p.isActive);
            c.send(p.isHuman);
            c.send(p.status);
        }
        for (int i = 0; i < Params.PLANETS; i++) {
            Planet p = game.planet[i];
            c.send(p.x);
            c.send(p.y);
            c.send(p.owner.number);
            c.send(p.production);
            c.send(p.ratio);
            // Use gameState here, because ships could have been dispatched, unless neutral
            if (p.owner.number != Params.NEUTRAL) {
                c.send(gameState.getPlanet(i).getShips());
            } else {
                c.send(p.ships);
            }
            c.send(p.defenceRatio);
            c.send(p.prodTurns);
            c.send(p.blackHole);
            for (int j = 0; j < numPlayers; j++) {
                if (p.attacker[j] > 0) {
                    c.send(j);
                    c.send(p.attacker[j]);
                }
            }
            c.send(Params.ENDTRANSMISSION);
        }
        Fleet f = game.fleets.first;
        while (f != null) {
            c.send(Planet.char2num(f.destination.planetChar));
            c.send(Planet.char2num(f.source.planetChar));
            c.send(f.ships);
            c.send(f.ratio);
            c.send(f.owner.number);
            c.send(new Float(f.distance).toString());
            f = f.next;
        }
        c.send(Params.ENDTRANSMISSION);
        // Add client to list of recipients
        Client old = getClient(c.me.name);
        if (old != c) {
            clients.addElement(c);
        }
        c.setGame(gameQueue);
        Client.unlockClients();
        m.unlock();
    }

    public void dispatch(Player sender, int from, int to, int ships) {
        Planet source = game.planet[from];
        if ((source.owner == sender) && (ships > 0)) {
            if (ships > source.ships) {
                if (source.ships > 0) {
                    ships = source.ships;
                } else {
                    return;
                }
            }
            Fleet f = new Fleet(game, source, game.planet[to], ships);
            FleetInfo fi = new FleetInfo(String.valueOf(from), String.valueOf(to), String.valueOf(ships));
            fleets.addElement(f);
            fleetInfos.addElement(fi);
        }
    }

    public int doRankings(RankingSystem rankingSystem) {
        if (game.turn < Params.MINGAMELENGTH) {
            return Params.NEUTRAL;
        }
        String winner;
        String[] losers = new String[numPlayers - 1];
        int highestScore = -1;
        int winningPlayer = 0;
        int numHumans = 0;
        for (int i = 0; i < numPlayers; i++) {
            if (player[i].score >= highestScore) {
                highestScore = player[i].score;
                winningPlayer = i;
            }
            if (player[i].isHuman) {
                numHumans++;
            }
        }
        boolean againstHumans = (numHumans > 1);
        winner = player[winningPlayer].name;
        if (!player[winningPlayer].isHuman) {
            winner += "(robot)";
        }
        int j = 0;
        for (int i = 0; i < numPlayers; i++) {
            if (i != winningPlayer) {
                losers[j] = player[i].name;
                if (!player[i].isHuman) {
                    losers[j] += "(robot)";
                }
                j++;
            }
        }
        rankingSystem.rankGame(winner, losers, againstHumans);
        return winningPlayer;
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

    // MAJOR HACK REQUIRED...
    private igx.bots.Message[] getMessages(int botNum) {
        Vector queue = messages[botNum];
        igx.bots.Message[] retVal = new igx.bots.Message[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            retVal[i] = (igx.bots.Message) queue.elementAt(i);
        }
        // Empty queue
        messages[botNum] = new Vector();
        return retVal;
    }

    public Player getPlayer(String name) {
        for (int i = 0; i < numPlayers; i++) {
            if (player[i].name.equals(name)) {
                return player[i];
            }
        }
        return null;
    }
    // *************** GAME INTERFACE ***************

    // Main message receiving interface for human players
    public void messageEvent(Message message) {
        boolean gamePlayer = true;
        m.lock();
        int type = message.getType();
        String playerName = message.getPlayerName();
        Player p = getPlayer(playerName);
        Client c = getClient(playerName);
        if (p == null) { // Must be a game watcher
            if (c != null) {
                p = c.me;
                gamePlayer = false;
            }
        }
        if (p != null) {
            switch (type) {
                case Message.MESSAGE:
                    String text = message.getMessageText();
                    int who = message.getDestination();
                    sendMessage(p, who, text);
                    break;
                case Message.SEND_FLEET:
                    if (gamePlayer) {
                        int from = message.getSource();
                        int to = message.getDestination();
                        int ships = message.getShips();
                        dispatch(p, from, to, ships);
                    }
                    break;
                case Message.PLAYER_QUIT:
                    playerQuit(c, message.getStatus());
                    break;
                case Message.PLAYER_LEFT:
                    playerLeft(c);
                    break;
            }
        }
        m.unlock();
        if (stopTimer) {
            timer.stopTimer();
        }
    }

    public void messageToRobot(igx.bots.Message message, int bot) {
        messages[bot].addElement(message);
    }

    protected int nameToNumber(String name) {
        for (int i = 0; i < numPlayers; i++) {
            if (player[i].name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    protected int numberToRobot(int num) {
        for (int i = 0; i < numBots; i++) {
            if (bot[i].getNumber() == num) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method was created in VisualAge.
     *
     * @param c igx.server.Client
     */
    public void playerLeft(Client c) {
        Client.lockClients();
        stopTimer = !anybodyPlaying();
        if (stopTimer) // Nobody is playing, so quit this game
        {
            gameQueue.shutDownQueue();
        }
        Client.unlockClients();
        // Tell server it's done
        if (stopTimer) {
            //stats.gameOver();
            serverQueue.addMessage(Message.gameOver(name));
            for (int i = 0; i < numBots; i++) {
                bot[i].gameEnding();
            }
        }
    }

    // This is the ugliest method in the whole damn game.
    public void playerQuit(Client quitter, int status) {
        Client.lockClients();
        quitter.me.status = status;
        if (status == Params.QUIT_SIGNAL) {
            quitter.setGame(null);
            quitter.me.isActive = false;
        }
        if ((nameToNumber(quitter.me.name) == -1) || !quitter.me.inGame) {
            // Game watcher
            if (status == Params.QUIT_SIGNAL) {
                quitter.send(Params.PLAYERQUITTING);
                quitter.send(-1);
                quitter.send(Params.QUIT_SIGNAL);
                clients.removeElement(quitter);
            }
            Client.unlockClients();
        } else {
            if (status == Params.QUIT_SIGNAL) {
                quitter.me.inGame = false;
                thisGame.setActivePlayer(quitter.me.name, false);
            }
            // Actual game player
            send(Params.PLAYERQUITTING);
            send(new Integer(quitter.me.number).toString());
            send(new Integer(status).toString());
            if (status == Params.QUIT_SIGNAL) {
                clients.removeElement(quitter);
            }
            // Send to quitter as well...
            // quitter.send(Params.PLAYERQUITTING);
            // quitter.send(new Integer(quitter.me.number).toString());
            stopTimer = !anybodyPlaying();
            if (stopTimer) // Nobody is playing, so quit this game
            {
                gameQueue.shutDownQueue();
            }
            Client.unlockClients();
            if (status == Params.QUIT_SIGNAL) {
                serverQueue.addMessage(Message.abandonGame(quitter.me.name));
            }
            // Tell server it's done
            if (stopTimer) {
                serverQueue.addMessage(Message.gameOver(name));
                for (int i = 0; i < numBots; i++) {
                    bot[i].gameEnding();
                }
                //stats.gameOver();
            }
        }
    }

    // Attack
    public void postAttack(igx.shared.Fleet fleet, igx.shared.Planet planet) {
        arrivals.addElement(new igx.bots.ArrivedFleet(fleet.ships, igx.bots.Planet.planetNumber(planet.planetChar), fleet.owner.number));
    }

    // Black hole event
    public void postBlackHole(igx.shared.Fleet fleet) {
    }

    // Error
    public void postError(String errorMessage) {
        System.out.println("OUCH! Error in the game engine... report this to HiVE Software!");
    }

    // Game end
    public void postGameEnd(int winnerNumber) {
    }

    //// Post game events
    // Game start
    public void postGameStart(GameInstance game) {
    }

    // Invasion
    public void postInvasion(igx.shared.Fleet fleet, igx.shared.Planet planet) {
    }

    // Players sends message
    public void postMessage(igx.shared.Player sender, igx.shared.Player recipient, String message) {
    }

    // Next turn
    public void postNextTurn() {
    }

    // Planet moves
    public void postPlanetMove(int oldX, int oldY, igx.shared.Planet planet) {
    }

    // Player quits
    public void postPlayerQuit(igx.shared.Player player) {
        //stats.reportEvent(igx.stats.StatsConstants.QUIT, player.name + " quit.");
    }

    // Redraw galaxy
    public void postRedrawGalaxy() {
    }

    // Reinforcements
    public void postReinforcements(int numberOfShips, igx.shared.Planet planet) {
        arrivals.addElement(new igx.bots.ArrivedFleet(numberOfShips, igx.bots.Planet.planetNumber(planet.planetChar), planet.owner.number));
    }

    // Repulsion
    public void postRepulsion(igx.shared.Player attacker, igx.shared.Planet planet) {
    }

    // Special Event
    public void postSpecial(String[] text, Color[] color) {
        String message = "";
        for (int i = 0; i < text.length; i++) {
            if (text[i] != null) {
                message += text[i];
            }
        }
        //stats.reportEvent(igx.stats.StatsConstants.SPECIAL, message);
    }
    // *****************  UI STUFF  *****************

    // Redraw all planets
    public void redrawAll() {
    }

    // Redraw a planet
    public void redrawPlanet(int planetNum) {
    }

    /**
     * Called when a robot is done processing.
     */
    public void robotDone(int botNum) {
        // Who cares?
    }
    // ************** Robot Interace ****************

    /**
     * Called to send a fleet on behalf of a robot.
     */
    public void robotSendFleet(int botNum, igx.bots.Fleet fleet) {
        m.lock();
        dispatch(player[botNum], fleet.source, fleet.destination, fleet.ships);
        m.unlock();
    }

    /**
     * Called to send a message on behalf of a robot.
     */
    public void robotSendMessage(int botNum, int recipient, String text) {
        m.lock();
        sendMessage(player[botNum], recipient, text);
        m.unlock();
    }

    // SEND - Sends a string to all clients
    protected void send(String message) {
        int n = clients.size();
        Client c;
        for (int i = 0; i < n; i++) {
            c = (Client) (clients.elementAt(i));
            if ((c != null) && (c.me.isPresent)) {
                c.send(message);
            }
        }
    }

    public void sendMessage(Player p, int who, String text) {
        Client.lockClients();
        // bot version of message
        igx.bots.Message message = new igx.bots.Message(p.number, who, text);
        if (who == Params.MESSAGE_TO_FORUM) {
            serverQueue.addMessage(Message.message(p.name, text, Params.MESSAGE_TO_FORUM));
        } else {
            if (who == Params.MESSAGE_TO_ALL) {
                send(Params.SENDMESSAGE);
                send(p.name);
                send(new Integer(who).toString());
                send(text);
                for (int i = 0; i < numBots; i++) {
                    messageToRobot(message, i);
                }
            } else {
                if (who < numPlayers) {
                    if ((player[who].isHuman) && (player[who].isActive)) {
                        Client c = getClient(player[who].name);
                        if (c != null) {
                            c.send(Params.SENDMESSAGE);
                            c.send(p.name);
                            c.send(new Integer(who).toString());
                            c.send(text);
                        }
                    } else {
                        if (!player[who].isHuman) {
                            messageToRobot(message, numberToRobot(who));
                        }
                    }
                    // Also send to sender
                    if (p.isHuman) {
                        Client c = getClient(p.name);
                        if (c != null) {
                            c.send(Params.SENDMESSAGE);
                            c.send(p.name);
                            c.send(new Integer(who).toString());
                            c.send(text);
                        }
                    } //else messageToRobot(message, numberToRobot(who));
                }
            }
        }
        Client.unlockClients();
    }

    /**
     * Replaces the client with the same name with this client.
     *
     * @param c igx.server.Client
     */
    public void substituteClient(Client c) {
        int n = clients.size();
        for (int i = 0; i < n; i++) {
            Client cl = (Client) (clients.elementAt(i));
            if (cl.me.name.equals(c.me.name)) {
                clients.setElementAt(c, i);
            }
        }
    }

    protected void updateRobots(GameInstance game) {
        oldState = gameState;
        int n = arrivals.size();
        igx.bots.ArrivedFleet[] arrival = new igx.bots.ArrivedFleet[n];
        for (int i = 0; i < n; i++) {
            arrival[i] = (igx.bots.ArrivedFleet) (arrivals.elementAt(i));
        }
        gameState = new GameState(game, arrival);
        arrivals = new Vector();
        // Do stats update
        //stats.update(gameState, game);
        // Do the update for each bot
        for (int i = 0; i < numBots; i++) {
            bot[i].updateBot(gameState, oldState, getMessages(i));
        }
    }
}
