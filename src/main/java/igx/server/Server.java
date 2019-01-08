package igx.server;

// Server.java
// Server class

import igx.shared.*;

class Server {
  public static void main(String args[]) {
	if (args.length != 2) {
	  System.out.println("Specify root path for resource files, and a time slice in milliseconds");
	  System.exit(0);
	}
	String path = args[0];
	try {
	  Params.TIMESLICE = Integer.parseInt(args[1]);
	} catch (NumberFormatException e) {
	  System.out.println("Timeslice improperly specified.");
	  System.exit(0);
	}
	System.out.println("igx server is now active.");
	MessageQueue queue = new MessageQueue("Forum Queue");
	ServerForum.rankingSystem = new RankingSystem(path, Params.RANKING_FILE);
	ServerForum forum = new ServerForum(queue, path);
	queue.setMessageListener(forum);
	Daemon daemon = new Daemon(forum, path);
	queue.start();
	daemon.start();
  }  
}