package igx.bots;

import java.util.*;

/**
 * The TalkBot is an abstract bot that has built-in support for the standard
 * IGX communications protocol.
 *
 * Instead of the {@link Bot#update update} method, use {@link doTurn doTurn}
 * method. The TalkBot will automatically call the appropriate message notifiers
 * for each turn <b>before</b> <code>doTurn</code> is called.
 *
 * The language so far:
 * <pre>
 * ALLY [UNTIL T:S]
 * DANGER AT P [AT T:S] [N SHIPS]
 * SHIPS TO P [AT T:S] [N SHIPS]
 * SEND TO P [AT T:S] [N SHIPS]
 * FLEET LEFT P [AT T:S] [N SHIPS]
 * WANT P [AT T:S]
 * SORRY
 * WAR
 * STATUS
 * ALLIED WITH PLAYER [UNTIL T:S]
 * DECLARED WAR WITH PLAYER
 * WHAT?
 * </pre>
 * @author John Watkinson
 * @modified slightly by Toby Hudson to cope with the fact that messaging is done differently in the RobotArena
 * as compared to the server.
 */
public class TalkBot extends Bot {

   private GameState gameForMessages = null;
   private int numMesgSoFar = 0;
   private Message[] oldmessage;

   public static final int planetNum (char c) {
      return igx.shared.Planet.char2num(c);
   }

   public static final char planetChar (int n) {
      return igx.shared.Planet.num2char(n);
   }

   /**
    * If you override this be <b>sure</b> to call
    * <code>super.newGame</code>.
    */
   public void newGame (GameState game, int skillLevel) {
      gameForMessages = game;
	  oldmessage = new Message[0];
   }

   public final void update (GameState game, GameState oldState, Message[] message) {
      gameForMessages = game;

	  int startreadingfrom = 0;
      if (mesgArrayResent(message, oldmessage, numMesgSoFar)) {
          startreadingfrom = numMesgSoFar;
      }
      oldmessage = message;

      parseMessages(game, message,startreadingfrom);
      doTurn(game, oldState);
   }

   /**
    * Override this to write your TalkBot!
    */
   public void doTurn (GameState game, GameState oldState) {}

   // Receive methods -- override
   public void receiveAlly (int sender, GameState game, int round, int segment) {}
   public void receiveDanger (int sender, GameState game, int p, int round, int segment, int ships) {}
   public void receiveShips (int sender, GameState game, int p, int round, int segment, int ships) {}
   public void receiveSend (int sender, GameState game, int p, int round, int segment, int ships) {}
   public void receiveFleet (int sender, GameState game, int p, int round, int segment, int ships) {}
   public void receiveWant (int sender, GameState game, int p, int round, int segment) {}
   public void receiveAllied (int sender, GameState game, int player, int round, int segment) {}
   public void receiveDeclaredWar (int sender, GameState game, int player) {}
   public void receiveStatus (int sender, GameState game) {}
   public void receiveSorry (int sender, GameState game) {}
   public void receiveWar (int sender, GameState game) {}
   // Not needed:
   // public void receiveWhat (int sender, GameState game) {}

   // Send methods
   public final void sendAlly (int to, int round, int segment) {
      if (round == -1) {
         sendMessage(to, "ALLY");
      } else {
         sendMessage(to, "ALLY UNTIL " + round + ":" + segment);
      }
   }

   public final void sendDanger (int to, int p, int round, int segment, int ships) {
      sendGeneric(to, p, round, segment, ships, "DANGER AT");
   }

   public final void sendShips (int to, int p, int round, int segment, int ships) {
      sendGeneric(to, p, round, segment, ships, "SHIPS TO");
   }

   public final void sendSend (int to, int p, int round, int segment, int ships) {
      sendGeneric(to, p, round, segment, ships, "SEND TO");
   }

   public final void sendFleet (int to, int p, int round, int segment, int ships) {
      sendGeneric(to, p, round, segment, ships, "FLEET LEFT");
   }

   public final void sendGeneric (int to, int p, int round, int segment, int ships, String prefix) {
      if (round == -1) {
         if (ships == -1) {
            sendMessage(to, prefix + " " + planetChar(p));
         } else {
            sendMessage(to, prefix + " " + planetChar(p) + " " + ships + " SHIPS");
         }
      } else {
         if (ships == -1) {
            sendMessage(to, prefix + " " + planetChar(p) + " AT " + round + ":" + segment);
         } else {
            sendMessage(to, prefix + " " + planetChar(p) + " AT " + round + ":" + segment + " " + ships + " SHIPS");
         }
      }
   }

   public final void sendWant (int to, int p, int round, int segment) {
      if (round == -1) {
         sendMessage(to, "WANT " + planetChar(p));
      } else {
         sendMessage(to, "WANT " + planetChar(p) + " AT " + round + ":" + segment);
      }
   }

   public final void sendAllied (int to, int player, int round, int segment) {
      if (round == -1) {
         sendMessage(to, "ALLIED WITH " + gameForMessages.getPlayer(player).getName());
      } else {
         sendMessage(to, "ALLIED WITH " + gameForMessages.getPlayer(player).getName() +
            " UNTIL " + round + ":" + segment);
      }
   }

   public final void sendDeclaredWar (int to, int player) {
      sendMessage(to, "DECLARED WAR WITH " + gameForMessages.getPlayer(player).getName());
   }

   public final void sendStatus (int to) {
      sendMessage(to, "STATUS");
   }

   public final void sendSorry (int to) {
      sendMessage(to, "SORRY");
   }

   public final void sendWar (int to) {
      sendMessage(to, "WAR");
   }

   public final void sendWhat (int to) {
      sendMessage(to, "WHAT?");
   }

   static class Time {
      int round;
      int segment;
   }

   private Time getTime (String t) throws NumberFormatException {
      int cut = t.indexOf(':');
      if ((cut == -1) || (cut == (t.length()-1))) {
         return null;
      } else {
         int r = Integer.parseInt(t.substring(0, cut));
         int s = Integer.parseInt(t.substring(cut+1));
         Time time = new Time();
         time.round = r;
         time.segment = s;
         return time;
      }
   }

   private int getPlanet (String t) {
      char c = t.charAt(0);
      int p = planetNum(c);
      if (p >=0 && p < 36) {
         return p;
      } else {
         throw new NumberFormatException("Planet out of range.");
      }
   }

   private int getPlayer (GameState g, String t) throws NullPointerException {
      for (int i = 0; i < g.getNumberOfPlayers(); i++) {
         Player p = g.getPlayer(i);
         if (t.equals(p.getName().toUpperCase())) {
            return i;
         }
      }
      throw new NullPointerException("Unknown player.");
   }


	public boolean mesgArrayResent(Message[] message, Message[] oldmessage, int numsofar) {
		boolean Resent = true;

		if (message.length < numsofar) {
			Resent = false;
		} else {
			for (int i = 0; i < Math.min(oldmessage.length,10); i++) {
				if (!message[i].toString().equals(oldmessage[i].toString())) {
					Resent = false;
				}
			}
		}
		return Resent;
	}
                  
   public void parseMessages (GameState game, Message[] m, int startreadingfrom) {
      for (int i = startreadingfrom; i < m.length; i++) {
      
      	 numMesgSoFar++;
      
         int from = m[i].getSenderNumber();
         int to = m[i].getReceiverNumber();
         if (to != getNumber()) {
            // Ignore messages not sent privately to us.
            continue;
         }
         StringTokenizer text = new StringTokenizer(m[i].getText().toUpperCase(), " ");
         String[] tokens = new String[7];
         int n = 0;
         for (int j = 0; (j < 7 && text.hasMoreTokens()); j++) {
            n++;
            tokens[j] = text.nextToken();
         }
         String command = tokens[0];
         try {
            if (command == null) {
               // Blank message, complain
               sendWhat(from);
               continue;
            }
            if (command.equals("ALLY")) {
               // ALLY
               if (n == 1) {
                  receiveAlly(from, game, -1, -1);
               } else if (n == 3) {
                  Time t = getTime(tokens[2]);
                  receiveAlly(from, game, t.round, t.segment);
               } else {
                  sendWhat(from);
               }
            } else if (command.equals("WANT")) {
               // WANT
               int p = getPlanet(tokens[1]);
               if (n == 2) {
                  receiveWant(from, game, p, -1, -1);
               } else if (n == 4) {
                  Time t = getTime(tokens[3]);
                  receiveWant(from, game, p, t.round, t.segment);
               } else {
                  sendWhat(from);
               }
            } else if (command.equals("ALLIED")) {
               // ALLIED
               int player = getPlayer(game, tokens[2]);
               if (n == 3) {
                  receiveAllied(from, game, player, -1, -1);
               } else if (n == 5) {
                  Time t = getTime(tokens[4]);
                  receiveAllied(from, game, player, t.round, t.segment);
               } else {
                  sendWhat(from);
               }
            } else if (command.equals("DECLARED")) {
               // DECLARED WAR
               int player = getPlayer(game, tokens[3]);
               receiveDeclaredWar(from, game, player);
            } else if (command.equals("STATUS")) {
               // STATUS
               receiveStatus(from, game);
            } else if (command.equals("SORRY")) {
               // SORRY
               receiveSorry(from, game);
            } else if (command.equals("WAR")) {
               // WAR
               receiveWar(from, game);
            } else if (command.equals("WHAT")) {
               // WHAT
               System.out.println("Oops! I messed up a message!");
            } else {
               int planet = getPlanet(tokens[2]);
               Time t = new Time();
               t.round = -1;
               t.segment = -1;
               int ships = -1;
               if (n >= 5) {
                  if ("AT".equals(tokens[3])) {
                     t = getTime(tokens[4]);
                  } else {
                     ships = Integer.parseInt(tokens[3]);
                  }
               }
               if (n == 7) {
                  if ("AT".equals(tokens[5])) {
                     t = getTime(tokens[6]);
                  } else {
                     ships = Integer.parseInt(tokens[5]);
                  }
               }
               if (command.equals("DANGER")) {
                  // DANGER
                  receiveDanger(from, game, planet, t.round, t.segment, ships);
               } else if (command.equals("SHIPS")) {
                  // SHIPS
                  receiveShips(from, game, planet, t.round, t.segment, ships);
               } else if (command.equals("SEND")) {
                  // SEND
                  receiveSend(from, game, planet, t.round, t.segment, ships);
               } else if (command.equals("FLEET")) {
                  // FLEET
                  receiveFleet(from, game, planet, t.round, t.segment, ships);
               } else {
                  sendWhat(from);
               }
            }
         } catch (NumberFormatException e) {
            e.printStackTrace();
            sendWhat(from);
         } catch (NullPointerException f) {
            f.printStackTrace();
            sendWhat(from);
         } catch (ArrayIndexOutOfBoundsException g) {
            g.printStackTrace();
            sendWhat(from);
         }
      }
   }
}