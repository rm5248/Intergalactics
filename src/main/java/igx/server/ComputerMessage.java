package igx.server;

// ComputerMessage.java 
public class ComputerMessage {

    public String message;
    public int sender;

    public ComputerMessage(int sender, String message) {
        this.message = message;
        this.sender = sender;
    }
}
