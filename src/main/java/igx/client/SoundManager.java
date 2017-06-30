package igx.client;

import igx.shared.Monitor;
import java.util.Hashtable;
import java.util.Vector;

public class SoundManager
  extends Thread
{
  public static final int SPACING = 100;
  Vector playList;
  static Hashtable playTimes = new Hashtable();
  FrontEnd fe;
  Monitor monitor;
  
  public SoundManager(FrontEnd paramFrontEnd)
  {
    fe = paramFrontEnd;
    SoundManager localSoundManager = this;
    monitor = new Monitor();
    playList = new Vector();
    start();
  }
  
  public synchronized void play(String paramString)
  {
    if (playList.indexOf(paramString) != -1) {
      return;
    }
    playList.addElement(paramString);
    notify();
  }
  
  public void playSound(String paramString)
  {
    Integer localInteger = (Integer)playTimes.get(paramString);
    int i = 1000;
    if (localInteger == null) {
      return;
    }
    i = localInteger.intValue() + 100;
    int j = i;
    fe.playSound(paramString);
    try
    {
      Thread.sleep(j);
    }
    catch (InterruptedException localInterruptedException) {}
  }
  
  public void run()
  {
    for (;;)
    {
      String str;
      synchronized (this)
      {
        if (playList.size() == 0)
        {
          try
          {
            wait();
          }
          catch (InterruptedException localInterruptedException) {}
          continue;
        }
        str = (String)playList.elementAt(0);
        playList.removeElementAt(0);
      }
      synchronized (monitor)
      {
        playSound(str);
      }
    }
  }
  
  static
  {
    playTimes.put("attack.au", new Integer(1682));
    playTimes.put("combat.au", new Integer(2351));
    playTimes.put("crazy.au", new Integer(2954));
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
    playTimes.put("invaded.au", new Integer(1842));
    playTimes.put("nothing.au", new Integer(3682));
    playTimes.put("left.au", new Integer(974));
  }
}