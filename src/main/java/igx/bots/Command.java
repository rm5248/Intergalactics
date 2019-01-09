package igx.bots;

// Command.java 
/**
 * Class that stores a command from a 'bot to the server. The command is one of
 * either
 * <B>FLEET</B>, <B>MESSAGE</B>, or <B>DONE</B>.
 *
 * @author John Watkinson
 */
public class Command {

    /**
     * Type of command, one of <B>FLEET</B>, <B>MESSAGE</B>, or <B>DONE</B>.
     */
    public int type;

    /**
     * 'Bot who produced the command.
     */
    public int who;

    /**
     * Fleet info.
     */
    public Fleet fleet;

    /**
     * Text of message.
     */
    public String messageText;

    /**
     * Recipient of message.
     */
    public int messageTo;

    /**
     * Constructor for a <B>DONE</B> command.
     */
    public Command(Bot bot) {
        type = RobotMessenger.DONE;
        who = bot.getNumber();
    }

    /**
     * Constructor for a <B>MESSAGE</B> command.
     */
    public Command(Bot bot, int messageTo, String messageText) {
        type = RobotMessenger.MESSAGE;
        who = bot.getNumber();
        this.messageTo = messageTo;
        this.messageText = messageText;
    }

    /**
     * Constructor for a <B>FLEET</B> command.
     */
    public Command(Bot bot, Fleet fleet) {
        type = RobotMessenger.FLEET;
        who = bot.getNumber();
        this.fleet = fleet;
    }
}
