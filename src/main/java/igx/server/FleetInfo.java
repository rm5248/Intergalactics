package igx.server;

// Dispatcher.java 
import igx.shared.*;
import java.util.*;
import igx.bots.Bot;
import igx.bots.RobotServer;
import igx.bots.RobotMessenger;
import igx.bots.GameState;

class FleetInfo {

    public String from, to, ships;

    public FleetInfo(String from, String to, String ships) {
        this.from = from;
        this.to = to;
        this.ships = ships;
    }
}
