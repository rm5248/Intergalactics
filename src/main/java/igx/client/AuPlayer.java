package igx.client;

import sun.audio.*;    //import the sun.audio package

import java.io.*;

import java.net.URL;



// sun.audio.* stuff based on tip from http://www.javaworld.com/javaworld/javatips/jw-javatip24.html



// From an Application: try AudioPlayer.player.start(inputstream)



public class AuPlayer 

{

  // Static Class Variables



  static String auDirectory = "au";

  static final String fs = System.getProperty("file.separator");

  static String auFiles[] = null;

  static

	{

	  if (System.getProperty("KBBAUDIR") != null)

		auDirectory = System.getProperty("KBBAUDIR");

	  File auDir = new File(auDirectory);

	  if (auDir.exists() && auDir.isDirectory())

		auFiles = auDir.list();

	}





  // Instance variables

  AudioStream as;

  InputStream is;



  public void finalize()

	{

	  try

		{

		  System.out.println("close an input stream " + is);

		  is.close();

		}

	  catch (Exception e)

		{

		  System.out.println(e.getMessage());

		}

	  try

		{

		  System.out.println("close an audio stream " + as);

		  as.close();

		}

	  catch (Exception e)

		{

		  System.out.println(e.getMessage());

		}

	}
  public static void main(String argv[])

	{

	  for(int au = 0; au < argv.length; au++)

		{

		  new AuPlayer().play(argv[au]);

		  try

			{

			  Thread.currentThread().sleep(2000);

			}

		  catch (InterruptedException ie)

			{

			  break;

			}

		}

	}
  public void play(String filename)
       
  {


	  try

		{

		  // Open an input stream  to the audio file.

		  File inFile = new File(filename);

		  if (!inFile.exists())

			inFile = new File(auDirectory + fs + filename);

		  if (!inFile.exists())

			return;



		  is = new FileInputStream(inFile);

	  

		  // Create an AudioStream object from the input stream.

		  AudioStream as = new AudioStream(is);

	  

		  // Use the static class member "player" from class AudioPlayer to play

		  // clip.

		  AudioPlayer.player.start(as);

	  

		  // Similarly, to stop the audio.

		  // AudioPlayer.player.stop(as);

		}

	  catch (IOException ioe)

		{

		  System.out.println(ioe.getMessage());

		}

	}
  public static void playRandomly()

	{

	  if (auFiles != null)

		{

		  int auIndex = (int) Math.floor(auFiles.length * Math.random());

		  String auName = auDirectory + fs + auFiles[auIndex];

		  if (new File(auName).exists())

			new AuPlayer().play(auName);

		  else

			System.out.println("No such file: " + auName);

		}

	}
}
