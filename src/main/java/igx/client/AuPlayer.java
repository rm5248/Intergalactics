package igx.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class AuPlayer
{
  static String auDirectory = "au";
  static final String fs = System.getProperty("file.separator");
  static String[] auFiles = null;
  AudioStream as;
  InputStream is;
  
  public AuPlayer() {}
  
  public void finalize()
  {
    try
    {
      System.out.println("close an input stream " + is);
      is.close();
    }
    catch (Exception localException1)
    {
      System.out.println(localException1.getMessage());
    }
    try
    {
      System.out.println("close an audio stream " + as);
      as.close();
    }
    catch (Exception localException2)
    {
      System.out.println(localException2.getMessage());
    }
  }
  
  public static void main(String[] paramArrayOfString)
  {
    for (int i = 0; i < paramArrayOfString.length; i++)
    {
      new AuPlayer().play(paramArrayOfString[i]);
      try
      {
        Thread.currentThread();
        Thread.sleep(2000L);
      }
      catch (InterruptedException localInterruptedException)
      {
        break;
      }
    }
  }
  
  public void play(String paramString)
  {
    try
    {
      File localFile = new File(paramString);
      if (!localFile.exists()) {
        localFile = new File(auDirectory + fs + paramString);
      }
      if (!localFile.exists()) {
        return;
      }
      is = new FileInputStream(localFile);
      AudioStream localAudioStream = new AudioStream(is);
      AudioPlayer.player.start(localAudioStream);
    }
    catch (IOException localIOException)
    {
      System.out.println(localIOException.getMessage());
    }
  }
  
  public static void playRandomly()
  {
    if (auFiles != null)
    {
      int i = (int)Math.floor(auFiles.length * Math.random());
      String str = auDirectory + fs + auFiles[i];
      if (new File(str).exists()) {
        new AuPlayer().play(str);
      } else {
        System.out.println("No such file: " + str);
      }
    }
  }
  
  static
  {
    if (System.getProperty("KBBAUDIR") != null) {
      auDirectory = System.getProperty("KBBAUDIR");
    }
    File localFile = new File(auDirectory);
    if ((localFile.exists()) && (localFile.isDirectory())) {
      auFiles = localFile.list();
    }
  }
}