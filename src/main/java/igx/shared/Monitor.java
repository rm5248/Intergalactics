package igx.shared;

// Monitor.java 

public class Monitor {
  
  boolean locked = false;
  
  public synchronized void lock () {
	while (locked)
	  try 
	{
	  wait();
	} catch (InterruptedException e) {}
	locked = true;
  }  
  public static void main (String[] args) {
	final Monitor m = new Monitor();
	final int N = 6;
	final int M = 20;
	Thread[] t = new Thread[N];
	for (int i = 0; i < N; i++) {
	  final int n = i;
	  t[i] = new Thread(new Runnable () {
	public void run () {
	  m.lock();
	  for (int j = 0; j < M; j++)
	    System.out.println("- " + n + ":" + j);
	  m.unlock();
	}
	  });
	}
	for (int i = 0; i < N; i++)
	  t[i].start();
  }  
  public synchronized void unlock () {
	locked = false;
	notify();
  }  
}