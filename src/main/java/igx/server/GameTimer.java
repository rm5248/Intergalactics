package igx.server;

// GameTimer.java
// GameTimer class - server
import igx.shared.*;

public class GameTimer extends Thread {

    Dispatcher dispatch;
    int interval;
    boolean stopped = false;

    public GameTimer(Dispatcher dispatch, int interval) {
        super(dispatch.name + " timer");
        this.dispatch = dispatch;
        this.interval = interval;
    }

    public void run() {
        long delay, time;
        try {
            sleep(Params.STARTDELAY);
        } catch (InterruptedException e) {
            System.out.println("Error: " + e);
        }
        delay = interval;
        while (true) {
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e);
            }
            time = System.currentTimeMillis();
            synchronized (this) {
                if (stopped) {
                    return;
                } else {
                    dispatch.advance();
                }
            }
            delay = interval - (System.currentTimeMillis() - time);
            if (delay < 0) {
                delay = 0;
            }
        }
    }

    public synchronized void stopTimer() {
        stopped = true;
        dispatch = null;
    }
}
