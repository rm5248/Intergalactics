package igx.shared;

import java.io.PrintStream;

public class Monitor
{
  boolean locked = false;
  
  public Monitor() {}
  
  public synchronized void lock()
  {
    while (locked) {
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException) {}
    }
    locked = true;
  }
  
  public static void main(String[] paramArrayOfString)
  {
    Monitor localMonitor = new Monitor();
    Thread[] arrayOfThread = new Thread[6];
    for (int i = 0; i < 6; i++)
    {
      int j = i;
      arrayOfThread[i = new Thread(new Runnable()
      {
        private final Monitor val$m;
        private final int val$n;
        
        public void run()
        {
          val$m.lock();
          for (int i = 0; i < 20; i++) {
            System.out.println("- " + val$n + ":" + i);
          }
          val$m.unlock();
        }
      });
    }
    for (i = 0; i < 6; i++) {
      arrayOfThread[i].start();
    }
  }
  
  public synchronized void unlock()
  {
    locked = false;
    notify();
  }
}