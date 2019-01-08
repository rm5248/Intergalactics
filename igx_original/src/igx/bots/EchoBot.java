package igx.bots;

/**
 * Tester TalkBot that just repeats messages as it receives them.
 *
 * @author John Watkinson
 */

public class EchoBot extends TalkBot {

   // Receive methods -- override
   public void receiveAlly (int sender, GameState game, int round, int segment) {
      sendAlly(sender, round, segment);
   }
   public void receiveDanger (int sender, GameState game, int p, int round, int segment, int ships) {
      sendDanger(sender, p, round, segment, ships);
   }
   public void receiveShips (int sender, GameState game, int p, int round, int segment, int ships) {
      sendShips(sender, p, round, segment, ships);
   }
   public void receiveSend (int sender, GameState game, int p, int round, int segment, int ships) {
      sendSend(sender, p, round, segment, ships);
   }
   public void receiveFleet (int sender, GameState game, int p, int round, int segment, int ships) {
      sendFleet(sender, p, round, segment, ships);
   }
   public void receiveWant (int sender, GameState game, int p, int round, int segment) {
      sendWant(sender, p, round, segment);
   }
   public void receiveAllied (int sender, GameState game, int player, int round, int segment) {
      sendAllied(sender, player, round, segment);
   }
   public void receiveDeclaredWar (int sender, GameState game, int player) {
      sendDeclaredWar(sender, player);
   }
   public void receiveStatus (int sender, GameState game) {
      sendStatus(sender);
   }
   public void receiveSorry (int sender, GameState game) {
      sendSorry(sender);
   }
   public void receiveWar (int sender, GameState game) {
      sendWar(sender);
   }
}