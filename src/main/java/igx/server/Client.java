package igx.server;

// Client.java
// Thread which manages i/o to/from a client
// Imports
import java.net.*;
import java.io.*;
import java.util.*;
import igx.shared.*;

class Client extends SocketAction {

    MessageQueue serverQueue = null;
    private MessageQueue gameQueue = null;
    public Game selectedGame = null;
    public Player me;
    public ServerForum forum;
    public Daemon daemon;

    private static Monitor m = new Monitor();

    public Client(Daemon daemon, MessageQueue queue, ServerForum forum, Socket sock) {
        super(sock);
        serverQueue = queue;
        this.daemon = daemon;
        this.forum = forum;
    }

    public void closeConnections() {
        super.closeConnections();
        if (outStream != null) {
            send("GAME OVER");
        }
    }

    public static void lockClients() {
        m.lock();
    }

    public String receive() throws IOException {
        String s = super.receive();
        return s;
    }

    public void run() {
        if (daemon.initializeClient(this)) {
            String to;
            String from = null;
            String ships;
            // Was it this client who began a game?
            boolean instigator = false;
            boolean bailing = false;
            try {
                // Record this client's visit
                String time = new Date(System.currentTimeMillis()).toString();
                System.out.println(me.name + " arrived at " + time);
                // Get first transmission from client
                from = receive();
                // *** Main loop ***
                while (me.isPresent) {
                    if (from.compareTo(Params.FORUM) == 0) {
                        // Receive data string
                        from = receive();
                        if (from.compareTo(Params.PLAYERQUITTING) == 0) {
                            if ((selectedGame != null) && (gameQueue == null)) {
                                toForum(Message.abandonGame(me.name));
                            }
                            me.isPresent = false;
                            toForum(Message.playerQuit(me.name));
                            break; // This client is done
                        } else {
                            if (from.compareTo(Params.SENDMESSAGE) == 0) {
                                // Receive body of message
                                to = receive();
                                toForum(Message.message(me.name, to, Params.MESSAGE_TO_FORUM));
                            } else {
                                if (from.compareTo(Params.NEWGAME) == 0) {
                                    // Receive name of this game
                                    to = receive();
                                    if (selectedGame == null) {
                                        toForum(Message.createGame(me.name, to));
                                        toForum(Message.joinGame(me.name, to));
                                    }
                                } else {
                                    if (from.compareTo(Params.JOINGAME) == 0) {
                                        // Receive name of this game
                                        to = receive();
                                        if (selectedGame == null) {
                                            toForum(Message.joinGame(me.name, to));
                                        }
                                    } else {
                                        if (from.compareTo(Params.CUSTOMMAP) == 0) {
                                            to = receive();
                                            String[] maps = MapReader.getMaps(Integer.parseInt(to));
                                            lockClients();
                                            send(Params.FORUM);
                                            send(Params.CUSTOMMAP);
                                            for (int i = 0; i < maps.length; i++) {
                                                send(maps[i]);
                                            }
                                            send(Params.CUSTOMMAP);
                                            unlockClients();
                                        } else {
                                            if (from.compareTo(Params.RANDOMMAP) == 0) {
                                                if ((selectedGame != null) && (me.name.equals(selectedGame.creator))) {
                                                    toForum(Message.customMap(selectedGame.name));
                                                }
                                            } else {
                                                if (from.compareTo(Params.STARTGAME) == 0) {
                                                    // Receive custom map
                                                    to = receive();
                                                    if ((selectedGame != null) && (me.name.equals(selectedGame.creator))) {
                                                        toForum(Message.startGame(selectedGame.name, to));
                                                    }
                                                } else {
                                                    if (from.compareTo(Params.ADDCOMPUTERPLAYER) == 0) {
                                                        to = receive(); // What game?
                                                        ships = receive(); // What bot?
                                                        if ((selectedGame != null) && (me.name.equals(selectedGame.creator))) {
                                                            toForum(Message.addRobot(ships, to));
                                                        }
                                                    } else {
                                                        if (from.compareTo(Params.ADDCUSTOMCOMPUTERPLAYER) == 0) {
                                                            to = receive(); // Bot URL
                                                            if ((selectedGame != null) && (me.name.equals(selectedGame.creator))) {
                                                                toForum(Message.addRobot(to, selectedGame.name));
                                                            }
                                                        } else {
                                                            if (from.compareTo(Params.ABANDONGAME) == 0) {
                                                                if (selectedGame != null) {
                                                                    toForum(Message.abandonGame(me.name));
                                                                }
                                                            } else {
                                                                if (from.compareTo(Params.REMOVECOMPUTERPLAYER) == 0) {
                                                                    to = receive(); // What game?
                                                                    ships = receive(); // What bot?
                                                                    if ((selectedGame != null) && (me.name.equals(selectedGame.creator))) {
                                                                        toForum(Message.removeRobot(ships, to));
                                                                    }
                                                                } else {
                                                                    if (from.compareTo(Params.WATCHGAME) == 0) {
                                                                        to = receive(); // What game?
                                                                        toForum(Message.watchGame(me.name, to));
                                                                    } else {
                                                                        System.out.println("Hardcore error!");
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (gameQueue != null) { // Must be game communications
                            //////
                            if (from.compareTo(Params.PLAYERQUITTING) == 0) {
                                int status = Integer.parseInt(receive());
                                if (status == Params.QUIT_SIGNAL) {
                                    me.isActive = false;
                                }
                                toGame(Message.playerQuitGame(me.name, status));
                            } else {
                                if (from.compareTo(Params.SENDMESSAGE) == 0) {
                                    to = receive(); // Get message destination
                                    ships = receive(); // Get message
                                    toGame(Message.message(me.name, ships, Integer.parseInt(to)));
                                } else {
                                    to = receive();
                                    ships = receive();
                                    toGame(Message.sendFleet(me.name, Planet.char2num(from.charAt(0)), Planet.char2num(to.charAt(0)), new Integer(ships).intValue()));
                                }
                            }
                            //////
                        }
                    }
                    // Receive next transmission from client
                    from = receive();
                }
            } catch (IOException e) {
                // me.isActive = false;
                me.isPresent = false;
                if (selectedGame != null) {
                    if (!selectedGame.inProgress) {
                        toForum(Message.abandonGame(me.name));
                    } else {
                        toGame(Message.playerLeft(me.name));
                    }
                }
                toForum(Message.playerQuit(me.name));
            }
            serverQueue = null;
            gameQueue = null;
            selectedGame = null;
            closeConnections();
        }
    }
    // Send an integer

    public void send(int val) {
        send(new Integer(val).toString());
    }

    /**
     * This method was created in VisualAge.
     *
     * @param message java.lang.String
     */
    public void send(String message) {
        //	try {
        //		Thread.sleep(500);
        //	} catch (InterruptedException e) {}
        super.send(message);
    }
    // Send a boolean

    public void send(boolean val) {
        if (val) {
            send("1");
        } else {
            send("0");
        }
    }

    public void setGame(MessageQueue queue) {
        gameQueue = queue;
        if (queue == null) {
            selectedGame = null;
        }
    }

    private void toForum(Message m) {
        if (serverQueue != null) {
            serverQueue.addMessage(m);
        }
    }

    private void toGame(Message m) {
        gameQueue.addMessage(m);
    }

    public static void unlockClients() {
        m.unlock();
    }
}
