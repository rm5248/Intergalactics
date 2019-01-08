package igx.server;

// Daemon.java

// Imports
import java.net.*;
import java.io.*;
import java.util.*;
import igx.shared.*;

class Daemon extends Thread {

  private static final String USER_DIRECTORY = "users";

  private ServerSocket port;

  private ServerForum server;

  private Monitor daemonMonitor = new Monitor();

  public static String path;

  public Daemon (ServerForum server, String path) {
	super("igx Daemon");
	this.server = server;
	this.path = path;
	// Try to grab port
	try {
	  port = new ServerSocket(Params.PORTNUM);
	}
	catch (IOException e) {
	  System.out.println("Couldn't access port " + Params.PORTNUM + ": " + e);
	  System.exit(1);
	}
  }
  public String[] getBulletins() {
	daemonMonitor.lock();
	BufferedReader reader = null;
	try {
	  reader = new BufferedReader(new FileReader(path + Params.BULLETIN_FILE));
	} catch (FileNotFoundException e) {
	  System.out.println("Bulletin file not found: " + path + Params.BULLETIN_FILE + ".");
	  daemonMonitor.unlock();
	  return new String[0];
	}
	Vector lines = new Vector();
	String line = null;
	while (true) {
	  try {
	line = reader.readLine();
	  } catch (IOException e) {}
	  if (line == null)
	break;
	  lines.addElement(line);
	}
	String[] bulletins = new String[lines.size()];
	for (int i = 0; i < lines.size(); i++)
	  bulletins[i] = (String)lines.elementAt(i);
	daemonMonitor.unlock();
	return bulletins;
  }
  private String getPassword (String alias) {
	daemonMonitor.lock();
	BufferedReader br;
	try {
	  br = new BufferedReader(new FileReader(path + USER_DIRECTORY + File.separator + alias));
	  String password = br.readLine();
	  br.close();
	  daemonMonitor.unlock();
	  return password;
	} catch (IOException e) {
	  daemonMonitor.unlock();
	  return null;
	}
  }

  protected boolean isValid (String alias) {
    if (alias.length() == 0) {
      return false;
    }
    char[] c = alias.toCharArray();
    for (int i = 0; i < c.length; i++) {
      if (Character.isLetter(c[i]) ||
          Character.isDigit(c[i]) ||
          (c[i] == '_') ||
          (c[i] == ' ')) {
            continue;
      } else {
         return false;
      }
    }
    return true;
  }

/**
 * This method was created in VisualAge.
 * @return boolean
 * @param p igx.server.Client
 */
public boolean initializeClient(Client p) {
	try {
		// Send version
		p.send(Params.VERSION);
		String okayVersion = p.receive();
		if (okayVersion.equals(Params.NACK))
			throw new IOException("Incompatible versions");
		boolean authenticated = false;
		String alias = "";
		while (!authenticated) {
			// Receive alias
			alias = p.receive();
         if (!isValid(alias)) {
            p.send(Params.UPDATE);
         } else if (server.getRobot(alias) != null) {
				p.send(Params.ADDCOMPUTERPLAYER);
         } else {
				// Look up the password...
				String password = getPassword(alias);
				if (password == null) {
					p.send(Params.NEW_ALIAS);
					// Get new password
					password = p.receive();
					if (!password.equals("")) {
						setPassword(alias, password);
						authenticated = true;
					}
				} else {
					p.send(Params.OLD_ALIAS);
					String candidate = p.receive();
					if (candidate.equals(password))
						authenticated = true;
				}
				// Indicate whether user is authenticated
				if (authenticated)
					p.send(Params.ACK);
				else
					p.send(Params.NACK);
			}
		}
		if (p != null) {
			String[] bulletins = getBulletins();
			for (int i = 0; i < bulletins.length; i++)
				p.send(bulletins[i]);
			// Indicate no more bulletins
			p.send(Params.ENDTRANSMISSION);
			// Send robot list with rankings
			int n = server.botList.length;
			for (int j = 0; j < n; j++) {
				p.send(server.botList[j].botType);
				p.send(server.botList[j].name);
				p.send(new Integer(server.botList[j].ranking).toString());
			}
			// Indicate no more robots
			p.send(Params.ENDTRANSMISSION);
			// Synchronized registration with server
			server.registerClient(alias, p);
			return true;
		}
	} catch (IOException e) {
		System.out.println("New client: " + e);
	}
	return false;
}
// Main loop that accepts new clients
public void run() {
	Socket clientSocket;
	while (true) {
		if (port == null) {
			System.out.println("The port has disappeared!");
			System.exit(1);
		}
		try {
			clientSocket = port.accept();
			Client p = new Client(this, server.queue, server, clientSocket);
			p.start();
		} catch (IOException e) {
			System.out.println("New client error: " + e);
		}
	}
}
  private void setPassword (String alias, String password) {
	daemonMonitor.lock();
	BufferedWriter fw;
	try {
	  fw = new BufferedWriter(new FileWriter(path + USER_DIRECTORY + File.separator + alias));
	  fw.write(password);
	  fw.close();
	} catch (IOException e) {
	  System.out.println("Error writing password: " + e);
	} finally {
	  daemonMonitor.unlock();
	}
  }
}