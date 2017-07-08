package igx.client;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.JApplet;

public class iCONApplet
  extends Applet
  implements FrontEnd
{
  public static String BASE = "http://intergalactics.net/";
  public static final String QUIT = "index.html";
  public static final String CLOSE = "closer.html";
  public static final String ERROR = "error.html";
  public static final String VERSION = "version.html";
  Class[] dummy;
  Dimension size;
  boolean soundOnOff = false;
  Server server;
  SoundManager player;
  String host;
  
  public iCONApplet() {}
  
  public void destroy()
  {
    if (server != null) {
      server.closeConnections();
    }
  }
  
  public void error()
  {
    try
    {
      getAppletContext().showDocument(new URL(BASE + "error.html"));
    }
    catch (MalformedURLException localMalformedURLException) {}
  }
  
  public String getAppletInfo()
  {
    try
    {
      return "intergalactics v3.0 -- Copyright 1999 HiVE Software -- Written by John Watkinson and BJ Parker" + getCodeBase() + getDocumentBase();
    }
    catch (Exception localException) {}
    return "Dubious.";
  }
  
  public Container getContainer()
  {
    return this;
  }
  
  public Dimension getDimensions()
  {
    return size;
  }
  
  public boolean getSoundMode()
  {
    return soundOnOff;
  }
  
  public void init()
  {
    host = getCodeBase().getHost();
    if (BASE == null) {
      BASE = getCodeBase().toString();
    }
    player = new SoundManager(this);
    setLayout(new BorderLayout());
    setBackground(Color.black);
    Label localLabel = new Label("Please wait for the game to load...", 1);
    localLabel.setForeground(Color.white);
    add(localLabel, "Center");
    validate();
    setVisible(true);
    dummy = new Class[38];
    try
    {
      dummy[0] = Class.forName("igx.client.ButtonListener");
      dummy[1] = Class.forName("igx.client.ListCanvas");
      dummy[2] = Class.forName("igx.client.Server");
      dummy[3] = Class.forName("igx.client.StatusBar");
      dummy[4] = Class.forName("igx.client.Debug");
      dummy[5] = Class.forName("igx.client.DialogCanvas");
      dummy[6] = Class.forName("igx.client.ScrollText");
      dummy[7] = Class.forName("igx.client.ClientForum$1");
      dummy[8] = Class.forName("igx.client.ClientForum");
      dummy[9] = Class.forName("igx.client.ClientForum$MainPanel");
      dummy[10] = Class.forName("igx.client.SoundManager");
      dummy[11] = Class.forName("igx.client.TextRow");
      dummy[12] = Class.forName("igx.client.ForumCanvas");
      dummy[13] = Class.forName("igx.client.ButtonCanvas");
      dummy[14] = Class.forName("igx.client.GameListCanvas");
      dummy[15] = Class.forName("igx.client.Galaxy");
      dummy[16] = Class.forName("igx.client.RowListener");
      dummy[17] = Class.forName("igx.client.Dispatcher");
      dummy[18] = Class.forName("igx.client.CText");
      dummy[19] = Class.forName("igx.client.FrontEnd");
      dummy[20] = Class.forName("igx.client.SoftButton");
      dummy[21] = Class.forName("igx.client.TextElement");
      dummy[23] = Class.forName("igx.client.ClientUI");
      dummy[24] = Class.forName("igx.shared.GameInstance");
      dummy[25] = Class.forName("igx.shared.UI");
      dummy[26] = Class.forName("igx.shared.Events");
      dummy[27] = Class.forName("igx.shared.Robot");
      dummy[28] = Class.forName("igx.shared.Planet");
      dummy[29] = Class.forName("igx.shared.Params");
      dummy[30] = Class.forName("igx.shared.Game");
      dummy[31] = Class.forName("igx.shared.Player");
      dummy[32] = Class.forName("igx.shared.SocketAction");
      dummy[33] = Class.forName("igx.shared.Monitor");
      dummy[34] = Class.forName("igx.shared.FleetQueue");
      dummy[35] = Class.forName("igx.shared.Fleet");
      dummy[36] = Class.forName("igx.shared.Forum");
      dummy[37] = Class.forName("igx.client.FontFinder");
    }
    catch (ClassNotFoundException localClassNotFoundException) {}
    removeAll();
  }
  
  public void play(String paramString)
  {
    if (soundOnOff) {
      player.play(paramString);
    }
  }
  
  public void playSound(String paramString)
  {
    try
    {
      super.play(new URL(BASE + paramString));
    }
    catch (MalformedURLException localMalformedURLException) {}
  }
  
  public void preGame(String paramString)
  {
    InetAddress localInetAddress = null;
    try
    {
      localInetAddress = InetAddress.getByName(paramString);
    }
    catch (UnknownHostException localUnknownHostException)
    {
      error();
    }
    try
    {
      server = new Server(new Socket(localInetAddress, 1542));
    }
    catch (IOException localIOException)
    {
      error();
    }
    if (!server.isConnected()) {
      throw new NullPointerException("Socket unexpectedly closed!");
    }
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    size = getSize();
    ClientForum localClientForum = new ClientForum(this, "the HiVE", localToolkit, server);
    server.setForum(localClientForum);
    localClientForum.setToPreferredSize();
    validate();
    repaint();
    server.start();
  }
  
  public void quitProgram()
  {
    stop();
  }
  
  public void setSoundMode(boolean paramBoolean)
  {
    soundOnOff = paramBoolean;
  }
  
  public void start()
  {
    preGame(host);
  }
  
  public void stop()
  {
    destroy();
    try
    {
      if (getParameter("close") != null) {
        getAppletContext().showDocument(new URL(BASE + "closer.html"));
      } else {
        getAppletContext().showDocument(new URL(BASE + "index.html"));
      }
    }
    catch (MalformedURLException localMalformedURLException) {}
  }
  
  public void versionProblem(String paramString)
  {
    try
    {
      getAppletContext().showDocument(new URL(BASE + "version.html"));
    }
    catch (MalformedURLException localMalformedURLException) {}
  }
}