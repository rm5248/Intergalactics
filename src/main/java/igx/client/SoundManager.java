package igx.client;

// SoundManager.java 
import igx.shared.*;
import java.util.*;

public class SoundManager extends Thread {

    public static final int SPACING = 100;

    Vector playList;
    static Hashtable playTimes;
    FrontEnd fe;
    Monitor monitor;

    static {
        playTimes = new Hashtable();
        playTimes.put("attack.au", new Integer(1682));
        playTimes.put("combat.au", new Integer(2351));
        playTimes.put("crazy.au", new Integer(2954));
        // playTimes.put("enemyCombat.au", new Integer(2202));
        playTimes.put("event.au", new Integer(3010));
        playTimes.put("lose.au", new Integer(3679));
        playTimes.put("message.au", new Integer(2232));
        playTimes.put("quit.au", new Integer(3952));
        playTimes.put("reinforce.au", new Integer(1702));
        playTimes.put("repelled.au", new Integer(1877));
        playTimes.put("soundon.au", new Integer(1916));
        playTimes.put("victory.au", new Integer(2214));
        playTimes.put("youquit.au", new Integer(3534));
        playTimes.put("arrive.au", new Integer(1174));
        // playTimes.put("resolve.au", new Integer(1927));
        playTimes.put("invaded.au", new Integer(1842));
        playTimes.put("nothing.au", new Integer(3682));
        playTimes.put("left.au", new Integer(974));
    }

    /*  public static void main (String[] args) {
	SoundManager sm = new SoundManager();
	for (int i = 0; i < args.length; i++)
	  sm.play(args[i]);
  }*/
    public SoundManager(FrontEnd fe) {
        this.fe = fe;
        final SoundManager that = this;
        monitor = new Monitor();
        playList = new Vector();
        start();
    }

    public synchronized void play(String sound) {
        if (playList.indexOf(sound) != -1) {
            return;
        } else {
            playList.addElement(sound);
            notify();
        }
    }

    public void playSound(String sound) {
        Integer time = (Integer) playTimes.get(sound);
        int playTime = 1000;
        if (time == null) {
            return;// System.out.println("Unknown sound.");
        } else {
            playTime = time.intValue() + SPACING;
        }
        int timing = playTime;
        fe.playSound(sound);
        try {
            Thread.sleep(timing);
        } catch (InterruptedException e) {
        }
    }

    public void run() {
        String sound;
        while (true) {
            synchronized (this) {
                while (playList.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                sound = (String) playList.elementAt(0);
                playList.removeElementAt(0);
            }
            synchronized (monitor) {
                playSound(sound);
            }
        }
    }
}
