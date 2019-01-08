package igx.shared;

// GameInstance.java
// GameInstance Class

import java.util.*;
import java.awt.Point;

public class GameInstance
{
   // Data fields
   Random generator;
   public int players;
   public Player[] player;
   public Planet[] planet;
   public char[][] map = new char[Params.MAPX][Params.MAPY];
   public FleetQueue fleets = new FleetQueue(this);
   public Events events = new Events(this);
   public int turn = 0;
   public int segment = 0;
   public boolean[] dirty = new boolean[Params.PLANETS]; // Has the planet changed?
   public boolean resolveAllConflicts = false;
   public UI ui;
   public Vector arrivalList = new Vector();

   public GameInstance() {
      generator = new Random();
   }
   // Constructor called with the seed supplied by the server
   public GameInstance (long seed, int numPlayers, Player[] player)
   {
      players = numPlayers;
      this.player = player;
      generator = new Random(seed);
      planet = new Planet[Params.PLANETS];
      boolean collision;
      for (int x = 0; x<Params.MAPX; x++)
         for (int y = 0; y<Params.MAPY; y++)
            map[x][y] = Params.SPACE;
      for (int i = 0; i<Params.PLANETS; i++)
         {
            planet[i] = new Planet(this, i);
            do
               {
                  planet[i].x = pseudo(0, Params.MAPX-1);
                  planet[i].y = pseudo(0, Params.MAPY-1);
               } while ((map[planet[i].x][planet[i].y] != Params.SPACE) || !acceptableByBeej(i));
            map[planet[i].x][planet[i].y] = Planet.num2char(i);
         }
      // Distribute home planets
      for (int i = 0; i<numPlayers; i++)
         {
            planet[i].owner = player[i];
            planet[i].ships = Params.HOMEPLANETSHIPS;
            planet[i].production  = Params.HOMEPLANETPROD;
            planet[i].ratio = Params.HOMEPLANETRATIO;
            player[i].number = i;
         }
      // Assign initial scores
      int startScore =
         (Params.HOMEPLANETRATIO * Params.RATIO_VALUE +
	  Params.HOMEPLANETSHIPS * Params.SHIP_VALUE +
	  Params.HOMEPLANETPROD * Params.PRODUCTION_VALUE) * 10 / 17;
      for (int i = 0; i < players; i++) {
         player[i].addScore(startScore);
         player[i].isActive = true;
         player[i].score = 100;
      }
   }  
   boolean acceptableByBeej (int num) {
      if (num >= players)
         return true;
      for (int i = 0; i < num; i++)
         if (squaredDistance(i, num) < Params.SQUARED_HOME_PLANET_DISTANCE_TOLERANCE)
            return false;
      return true;
   }  

   public void setMap (Point[] newMap) {
      for (int i = 0; i < newMap.length; i++) {
         if (newMap[i].x != -1) {
            planet[i].x = newMap[i].x;
            planet[i].y = newMap[i].y;
         }
      }
      for (int x = 0; x<Params.MAPX; x++) {
         for (int y = 0; y<Params.MAPY; y++) {
            map[x][y] = Params.SPACE;
         }
      }
      for (int i = 0; i<Params.PLANETS; i++) {
         map[planet[i].x][planet[i].y] = Planet.num2char(i);
      }
   }

   public void fleetArrived (Fleet fleet) {
      arrivalList.addElement(new Fleet(fleet));
   }  
   public Player getPlayer(String name) {
      for (int i = 0; i < players; i++)
         if (player[i].name.equals(name))
            return player[i];
      return null;
   }
   // Generates a pseudo random number synchronized across all clients in the game
   public int pseudo (int min, int max)
   {
      int value = generator.nextInt();
      return value<0 ? (-value) % (max - min + 1) + min : value % (max - min + 1) + min;
   }  
   public void registerUI (UI ui) {
      this.ui = ui;
   }  
   public void setGameInstance (int numPlayers,
                                Player[] player,
                                Planet[] planet,
                                FleetQueue fleets,
                                int turn,
                                int segment) {
      players = numPlayers;
      this.player = player;
      this.planet = planet;
      this.fleets = fleets;
      this.turn = turn;
      this.segment = segment;
      for (int x = 0; x<Params.MAPX; x++)
         for (int y = 0; y<Params.MAPY; y++)
            map[x][y] = Params.SPACE;
      for (int i = 0; i < Params.PLANETS; i++) 
         map[planet[i].x][planet[i].y] = Planet.num2char(i);
   }  
   public void setSeed (long seed) {
      generator = new Random(seed);
   }  
   int squaredDistance (int i, int j) {
      return ((planet[i].x - planet[j].x) * (planet[i].x - planet[j].x) +
              (planet[i].y - planet[j].y) * (planet[i].y - planet[j].y));
   }  
   // Takes an update segment from the server and updates the local game
   public void update(Vector dispatches) {
      Fleet f;
      // Reset all scores
      for (int i = 0; i < players; i++)
         player[i].resetScore();
      while (!dispatches.isEmpty()) {
         f = (Fleet) dispatches.elementAt(0);
         dispatches.removeElementAt(0);
         fleets.insert(f);
      }
      if (++segment == Params.SEGMENTS) {
         turn++;
         events.update(); // Check for random events
         segment = 0;
      }
      fleets.update(); // Advance fleets
      for (int i = 0; i < Params.PLANETS; i++) {
         planet[i].update();
         planet[i].owner.addScore(planet[i].ratio * Params.RATIO_VALUE);
         planet[i].owner.addScore(planet[i].ships * Params.SHIP_VALUE);
         planet[i].owner.addScore(planet[i].production * Params.PRODUCTION_VALUE);
      }
      fleets.doScores();
      for (int i = 0; i < players; i++)
         player[i].score = player[i].score * 10 / 17; // And THIS is intuitive?
      resolveAllConflicts = false;
   }
}
