package igx.shared;

// SocketAction Class
// SocketAction.java

// Imports
import java.net.*;
import java.io.*;

public class SocketAction extends Thread {
  private BufferedReader inStream = null;
  protected PrintWriter   outStream = null;
  private Socket          socket = null;

  public SocketAction(Socket sock) {
	super("SocketAction");
	try {
	  inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()), 1024);
	  outStream = new PrintWriter(new
		BufferedOutputStream(sock.getOutputStream(), 1024), true);
	  socket = sock;
	}
	catch (IOException e) {
	  // System.out.println("Couldn't initialize SocketAction: " + e);
	  // System.exit(1);
	}
  }  
  public void closeConnections() {
	try {
	  if (socket != null)
	    socket.close();
	  socket = null;
	}
	catch (IOException e) {
	  // System.out.println("Couldn't close socket: " + e);
	}
  }  
  protected void finalize () {
	if (socket != null) {
	  try {
		socket.close();
	  }
	  catch (IOException e) {
		// System.out.println("Couldn't close socket: " + e);
	  }
	  socket = null;
	}
  }  
  public boolean isConnected() {
	return ((inStream != null) && (outStream != null) &&
	  (socket != null));
	}
  public String receive() throws IOException {
	String retValue = "";
	retValue =  inStream.readLine();
	if (retValue == null)
	  throw new IOException("Connection closed.");
	return retValue;
  }  
  public void run() {
  }  
  public void send(String s) {
	outStream.println(s);
  }  
}