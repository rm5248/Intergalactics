package igx.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class I
  extends Frame
  implements FrontEnd
{
  Dimension size = null;
  static Dimension customSize = null;
  boolean soundOnOff = false;
  Server server;
  SoundManager player = new SoundManager(this);
  String host;
  AuPlayer au = new AuPlayer();
  
  public I(String paramString)
  {
    super(paramString);
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
  
  public static void main(String[] paramArrayOfString)
  {
    
    if ((paramArrayOfString.length != 1) && (paramArrayOfString.length != 4))
    {
      showUsage();
      System.exit(1);
    }
    int i = -1;
    int j = -1;
    String str = null;
    if (paramArrayOfString.length == 4)
    {
      try
      {
        if (paramArrayOfString[0].equals("-s"))
        {
          i = Integer.parseInt(paramArrayOfString[1]);
          j = Integer.parseInt(paramArrayOfString[2]);
          str = paramArrayOfString[3];
        }
        else if (paramArrayOfString[1].equals("-s"))
        {
          i = Integer.parseInt(paramArrayOfString[2]);
          j = Integer.parseInt(paramArrayOfString[3]);
          str = paramArrayOfString[0];
        }
        else
        {
          showUsage();
          System.exit(1);
        }
      }
      catch (NumberFormatException localNumberFormatException)
      {
        showUsage();
        System.exit(1);
      }
      if (i != -1) {
        customSize = new Dimension(i, j);
      }
    }
    else
    {
      str = paramArrayOfString[0];
    }
    I localI = new I("intergalactics - " + str);
    localI.setLayout(new BorderLayout());
    localI.setBackground(Color.black);
    localI.preGame(str, true);
    localI.host = str;
  }
  
  public void play(String paramString)
  {
    if (soundOnOff) {
      player.play(paramString);
    }
  }
  
  public void playSound(String paramString)
  {
    au.play(paramString);
  }
  
  public void preGame(String paramString, boolean paramBoolean)
  {
    InetAddress localInetAddress = null;
    try
    {
      localInetAddress = InetAddress.getByName(paramString);
    }
    catch (UnknownHostException localUnknownHostException)
    {
      System.out.println("Unknown host.");
      System.exit(1);
    }
    try
    {
      server = new Server(new Socket(localInetAddress, 1542));
    }
    catch (IOException localIOException)
    {
      System.out.println("Unknown host.");
      System.exit(1);
    }
    if (!server.isConnected()) {
      throw new NullPointerException("Socket unexpectedly closed!");
    }
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    Dimension localDimension = localToolkit.getScreenSize();
    if (customSize != null) {
      localDimension = customSize;
    }
    setSize(localDimension);
    show();
    size = new Dimension(localDimension.width, localDimension.height);
    Insets localInsets = getInsets();
    size.width = (size.width - localInsets.left - localInsets.right);
    size.height = (size.height - localInsets.top - localInsets.bottom - 20);
    ClientForum localClientForum = new ClientForum(this, "the HiVE", localToolkit, server);
    server.setForum(localClientForum);
    Image localImage = localToolkit.getImage("hive.gif");
    setIconImage(localImage);
    pack();
    localClientForum.setToPreferredSize();
    validate();
    server.start();
  }
  
  public void quitProgram()
  {
    System.exit(0);
  }
  
  public void setSoundMode(boolean paramBoolean)
  {
    soundOnOff = paramBoolean;
  }
  
  public static void showIntro()
  {
    System.out.println("intergalactics v3.8");
    System.out.println("Copyright 1999 HiVE Software");
  }
  
  public static void showUsage()
  {
    System.out.println("Usage:");
    System.out.println("");
    System.out.println("  intergalactics [ie] <server>");
    System.out.println("");
    System.out.println("See manual.html for instructions.");
  }
  
  public void versionProblem(String paramString)
  {
    System.out.println("This version is out of date! The server is using version " + paramString + ".");
    System.out.println("Please get an updated client from the intergalactics homepage.");
    System.exit(0);
  }
}