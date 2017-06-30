package igx.shared;

import java.util.Vector;

public class MessageQueue
  extends Thread
{
  private Vector queue = new Vector();
  private MessageListener listener = null;
  private boolean shutDown = false;
  
  public MessageQueue(String paramString)
  {
    super(paramString);
  }
  
  public synchronized void addMessage(Message paramMessage)
  {
    if (paramMessage == null) {
      shutDown = true;
    } else {
      queue.addElement(paramMessage);
    }
    notify();
  }
  
  public void mainLoop()
  {
    while (!shutDown)
    {
      synchronized (this)
      {
        while ((queue.size() == 0) && (!shutDown)) {
          try
          {
            wait();
          }
          catch (InterruptedException localInterruptedException) {}
        }
      }
      if (!shutDown)
      {
        Message msg = (Message)queue.elementAt(0);
        queue.removeElementAt(0);
        if (listener != null) {
          listener.messageEvent(msg);
        }
      }
    }
  }
  
  public void run()
  {
    mainLoop();
  }
  
  public synchronized void setMessageListener(MessageListener paramMessageListener)
  {
    listener = paramMessageListener;
  }
  
  public synchronized void shutDownQueue()
  {
    addMessage(null);
  }
}