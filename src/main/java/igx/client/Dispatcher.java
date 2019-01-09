package igx.client;

// Dispatcher.java
// Class Dispatcher 
import java.util.*;
import igx.shared.*;

class Dispatcher {

    GameInstance Game;
    ClientUI ui;
    Server server;
    Vector fleets = new Vector();
    String name;

    public Dispatcher(GameInstance Game, Server server, String name) {
        this.Game = Game;
        this.server = server;
        this.name = name;
    }

    public void addFleet(Fleet f) {
        fleets.addElement(f);
    }

    public void advance() {
        Game.update(fleets);
        ui.postNextTurn();
        fleets.removeAllElements();
    }

    public void dispatch(char from, char to, int ships) {
        // Still in this game, or watching?
        Player p = getMe();
        if ((p != null) && (p.isActive)) {
            server.send(new Character(from).toString());
            server.send(new Character(to).toString());
            server.send(String.valueOf(ships));
        }
    }

    public void forumMessage(String sender, String text) {
        ui.postForumMessage(sender, text);
    }

    public Player getMe() {
        return Game.getPlayer(server.name);
    }

    public void message(Player sender, Player recipient, String message) {
        ui.postMessage(sender, recipient, message);
    }

    public void playerArrived(String name) {
        ui.postArrival(name);
    }

    public void playerLeft(String name) {
        for (int i = 0; i < Game.players; i++) {
            if ((Game.player[i].name.equals(name)) && (Game.player[i].isActive)) {
                Game.player[i].isPresent = false;
                ui.postPlayerLeft(name, i);
                return;
            }
        }
        ui.postPlayerLeft(name, -1);
    }

    public void playerQuit(int quitter, int status) {
        if (status != Params.QUIT_SIGNAL) {
            Game.player[quitter].status = status;
            ui.postPlayerQuit(Game.player[quitter], status);
        } else {
            Game.player[quitter].isActive = false;
            if (!Game.player[quitter].name.equals(server.name)) {
                ui.postPlayerQuit(Game.player[quitter], status);
            }
        }
    }

    public void quit(int command) {
        server.send(Params.PLAYERQUITTING);
        server.send(new Integer(command).toString());
    }

    public void registerUI(ClientUI ui) {
        this.ui = ui;
    }

    public void sendMessage(int receiver, String message) {
        server.send(Params.SENDMESSAGE);
        if (receiver == -1) // Send to all
        {
            receiver = Params.MAXPLAYERS;
        }
        server.send(new Integer(receiver).toString());
        server.send(message);
    }

    /**
     * This method was created in VisualAge.
     *
     * @param sender java.lang.String
     * @param recipient igx.shared.Player
     * @param text java.lang.String
     */
    public void watcherMessage(String sender, Player recipient, String text) {
        ui.postForumMessage(sender, recipient, text);
    }

    public void youQuit() {
        // ui.quit();
    }
}
