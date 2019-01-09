package igx.bots;

// Robot Messenger.java 
import igx.shared.GameInstance;
import java.util.*;

/**
 * This class is responsible for messaging between the 'bot and it's respective
 * game server.
 *
 * @author John Watkinson
 */
public class RobotMessenger extends Thread {

    public static final int NONE = 0;

    public static final int FLEET = 1;

    public static final int MESSAGE = 2;

    public static final int DONE = 3;

    private Bot[] bot;

    private int numBots;

    private int numActiveBots;

    private RobotServer rs;

    private boolean gameOver = false;

    private Vector commands = new Vector();

    public RobotMessenger(RobotServer rs, int numBots) {
        super("Robot Messenger");
        numActiveBots = numBots;
        this.numBots = numBots;
        this.rs = rs;
        start();
    }

    public synchronized void debug(Bot bot, String text) {
        System.out.println("Debug bot#" + bot.getNumber() + ": " + text);
    }

    private void messengerLoop() {
        while (!gameOver) {
            synchronized (this) {
                while ((commands.size() == 0) && (!gameOver)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!gameOver) {
                Command command = (Command) (commands.elementAt(0));
                commands.removeElementAt(0);
                processMessage(command);
            }
        }
    }

    private void processMessage(Command command) {
        switch (command.type) {
            case FLEET:
                rs.robotSendFleet(command.who, command.fleet);
                break;
            case MESSAGE:
                rs.robotSendMessage(command.who, command.messageTo, command.messageText);
                break;
            case DONE:
                rs.robotDone(command.who);
                break;
        }
    }

    public synchronized void quit() {
        if (--numActiveBots == 0) {
            gameOver = true;
            notify();
        }
    }

    public void run() {
        messengerLoop();
    }

    public synchronized void sendCommand(Command command) {
        commands.addElement(command);
        notify();
    }

    public synchronized void sendCommands(Command[] command) {
        for (int i = 0; i < command.length; i++) {
            commands.addElement(command[i]);
        }
        notify();
    }
}
