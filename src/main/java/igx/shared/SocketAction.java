package igx.shared;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketAction
  extends Thread
{
  private BufferedReader inStream = null;
  protected PrintWriter outStream = null;
  private Socket socket = null;
  
  public SocketAction(Socket paramSocket)
  {
    super("SocketAction");
    try
    {
      inStream = new BufferedReader(new InputStreamReader(paramSocket.getInputStream()), 1024);
      outStream = new PrintWriter(new BufferedOutputStream(paramSocket.getOutputStream(), 1024), true);
      socket = paramSocket;
    }
    catch (IOException localIOException) {}
  }
  
  public void closeConnections()
  {
    try
    {
      if (socket != null) {
        socket.close();
      }
      socket = null;
    }
    catch (IOException localIOException) {}
  }
  
  protected void finalize()
  {
    if (socket != null)
    {
      try
      {
        socket.close();
      }
      catch (IOException localIOException) {}
      socket = null;
    }
  }
  
  public boolean isConnected()
  {
    return (inStream != null) && (outStream != null) && (socket != null);
  }
  
  public String receive()
    throws IOException
  {
    String str = "";
    str = inStream.readLine();
    if (str == null) {
      throw new IOException("Connection closed.");
    }
    return str;
  }
  
  public void run() {}
  
  public void send(String paramString)
  {
    outStream.println(paramString);
  }
}