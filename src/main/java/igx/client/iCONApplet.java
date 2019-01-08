package igx.client;

// iCONApplet.java 
// Be sure to set BASE before compiling

import igx.shared.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.applet.*;

public class iCONApplet extends Applet implements FrontEnd
{
  // Set this to the base URL of the applet
  public static String BASE = "http://intergalactics.net/";
  // public static String BASE = null;
  // Set this to the introductory web page
  public static final String QUIT = "index.html";
  public static final String CLOSE = "closer.html";
  // Set this to the error reporting web page
  public static final String ERROR = "error.html";
  // Set this to the version reporting page
  public static final String VERSION = "version.html";
  
  Class[] dummy;
  Dimension size;
  boolean soundOnOff = false;
  Server server;
  SoundManager player;
  String host;
  public void destroy () {
	if (server != null)
	  server.closeConnections();
  }  
  public void error () {
	try {
	  getAppletContext().showDocument(new URL(BASE+ERROR));
	} catch (MalformedURLException e) {}
  }  
  public String getAppletInfo () {
	  try {
			return "intergalactics v3.0 -- Copyright 1999 HiVE Software -- Written by John Watkinson and BJ Parker"
			 + getCodeBase() + getDocumentBase();
	  } catch (Exception e) {
	  	return "Dubious.";
	  }
  }  
  /**
   * This method was created in VisualAge.
   * @return java.awt.Container
   */
  public Container getContainer() {
	return this;
  }  
  public Dimension getDimensions () {
	return size;
  }  
  public boolean getSoundMode () {
	return soundOnOff;
  }  
  public void init () {
	host = getCodeBase().getHost();
	if (BASE == null) {
	    BASE = getCodeBase().toString();
        }
	player = new SoundManager(this);
	setLayout(new BorderLayout());
	setBackground(Color.black);
	Label label = new Label("Please wait for the game to load...", Label.CENTER);
	label.setForeground(Color.white);
	add(label, BorderLayout.CENTER);
	validate();
	setVisible(true);
	dummy = new Class[38];
	// Preload, baby!
	try {
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
	} catch (ClassNotFoundException e) {}
	// End of preload
	removeAll();
  }  
  public void play (String sound) {
	if (soundOnOff)
	  player.play(sound);
  }  
  public void playSound (String sound) {
	try {
	  super.play(new URL(BASE+sound));
	} catch (MalformedURLException e) {}
  }  
public void preGame(String host) {
	InetAddress location = null;
	try {
		location = InetAddress.getByName(host);
	} catch (UnknownHostException e) {
		error();
	}
	try {
		server = new Server(new Socket(location, Params.PORTNUM));
	} catch (IOException e) {
		error();
	}
	if (!server.isConnected())
		throw new NullPointerException("Socket unexpectedly closed!");
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	size = getSize();
	ClientForum cf = new ClientForum(this, Params.SERVER_NAME, toolkit, server);
	server.setForum(cf);
	cf.setToPreferredSize();
	validate();
	repaint();
	server.start();
}
public void quitProgram() {
	stop();
}
public void setSoundMode(boolean mode) {
	soundOnOff = mode;
}
  public void start () {
		preGame(host);
  }  
public void stop() {
	destroy();
	try {
		if (getParameter("close") != null) {
			getAppletContext().showDocument(new URL(BASE + CLOSE));
		} else {
			getAppletContext().showDocument(new URL(BASE + QUIT));
		}
	} catch (MalformedURLException e) {
	}
}
/**
 * This method was created in VisualAge.
 * @param serverVersion java.lang.String
 */
public void versionProblem(String serverVersion) {
	try {
	  getAppletContext().showDocument(new URL(BASE+VERSION));
	} catch (MalformedURLException e) {}
}
}