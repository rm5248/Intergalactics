package igx.shared;

// MessageQueue.java 
import java.util.*;

public class MessageQueue extends Thread {

    private Vector queue = new Vector();

    private MessageListener listener = null;

    private boolean shutDown = false;

    /**
     * New MessageQueue.
     *
     * @param name java.lang.String
     */
    public MessageQueue(String name) {
        super(name);
    }

    public synchronized void addMessage(Message message) {
        if (message == null) {
            shutDown = true;
        } else {
            queue.addElement(message);
        }
        notify();
    }

    public void mainLoop() {
        while (!shutDown) {
            synchronized (this) {
                while ((queue.size() == 0) && (!shutDown)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!shutDown) {
                Message message = (Message) (queue.elementAt(0));
                queue.removeElementAt(0);
                if (listener != null) {
                    listener.messageEvent(message);
                }
            }
        }
    }

    public void run() {
        mainLoop();
    }

    public synchronized void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public synchronized void shutDownQueue() {
        addMessage(null);
    }
}
