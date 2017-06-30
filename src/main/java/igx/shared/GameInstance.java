package igx.shared;

import java.awt.Point;
import java.util.Random;
import java.util.Vector;

public class GameInstance
{
  Random generator;
  public int players;
  public Player[] player;
  public Planet[] planet;
  public char[][] map = new char[16][16];
  public FleetQueue fleets = new FleetQueue(this);
  public Events events = new Events(this);
  public int turn = 0;
  public int segment = 0;
  public boolean[] dirty = new boolean[36];
  public boolean resolveAllConflicts = false;
  public UI ui;
  public Vector arrivalList = new Vector();
  
  public GameInstance()
  {
    generator = new Random();
  }
  
  public GameInstance(long paramLong, int paramInt, Player[] paramArrayOfPlayer)
  {
    players = paramInt;
    player = paramArrayOfPlayer;
    generator = new Random(paramLong);
    planet = new Planet[36];
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        map[i][j] = '.';
      }
    }
    for (int i = 0; i < 36; i++)
    {
      planet[i] = new Planet(this, i);
      do
      {
        planet[i].x = pseudo(0, 15);
        planet[i].y = pseudo(0, 15);
      } while ((map[planet[i].x][planet[i].y] != '.') || (!acceptableByBeej(i)));
      map[planet[i].x][planet[i].y] = Planet.num2char(i);
    }
    for (int i = 0; i < paramInt; i++)
    {
      planet[i].owner = paramArrayOfPlayer[i];
      planet[i].ships = 10;
      planet[i].production = 10;
      planet[i].ratio = 40;
    }
    for (int j = 0; j < players; j++)
    {
      paramArrayOfPlayer[j].addScore(100);
      paramArrayOfPlayer[j].isActive = true;
    }
  }
  
  boolean acceptableByBeej(int paramInt)
  {
    if (paramInt >= players) {
      return true;
    }
    for (int i = 0; i < paramInt; i++) {
      if (squaredDistance(i, paramInt) < 16) {
        return false;
      }
    }
    return true;
  }
  
  public void setMap(Point[] paramArrayOfPoint)
  {
    for (int i = 0; i < paramArrayOfPoint.length; i++) {
        int x = paramArrayOfPoint[ i ].x;
        int y = paramArrayOfPoint[ i ].y;
      if (x != -1)
      {
        planet[i].x = x;
        planet[i].y = y;
      }
    }
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        map[i][j] = '.';
      }
    }
    for (int i = 0; i < 36; i++) {
      map[planet[i].x][planet[i].y] = Planet.num2char(i);
    }
  }
  
  public void fleetArrived(Fleet paramFleet)
  {
    arrivalList.addElement(new Fleet(paramFleet));
  }
  
  public Player getPlayer(String paramString)
  {
    for (int i = 0; i < players; i++) {
      if (player[i].name.equals(paramString)) {
        return player[i];
      }
    }
    return null;
  }
  
  public int pseudo(int paramInt1, int paramInt2)
  {
    int i = generator.nextInt();
    return i < 0 ? -i % (paramInt2 - paramInt1 + 1) + paramInt1 : i % (paramInt2 - paramInt1 + 1) + paramInt1;
  }
  
  public void registerUI(UI paramUI)
  {
    ui = paramUI;
  }
  
  public void setGameInstance(int paramInt1, Player[] paramArrayOfPlayer, Planet[] paramArrayOfPlanet, FleetQueue paramFleetQueue, int paramInt2, int paramInt3)
  {
    players = paramInt1;
    player = paramArrayOfPlayer;
    planet = paramArrayOfPlanet;
    fleets = paramFleetQueue;
    turn = paramInt2;
    segment = paramInt3;
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        map[i][j] = '.';
      }
    }
    for (int i = 0; i < 16; i++) {
        for( int j = 0; j < 16; j++ ){
        map[i][j] = Planet.num2char(i);
        }
    }
  }
  
  public void setSeed(long paramLong)
  {
    generator = new Random(paramLong);
  }
  
  int squaredDistance(int paramInt1, int paramInt2)
  {
    return (planet[paramInt1].x - planet[paramInt2].x) * (planet[paramInt1].x - planet[paramInt2].x) + (planet[paramInt1].y - planet[paramInt2].y) * (planet[paramInt1].y - planet[paramInt2].y);
  }
  
  public void update(Vector paramVector)
  {
    for (int i = 0; i < players; i++) {
      player[i].resetScore();
    }
    while (!paramVector.isEmpty())
    {
      Fleet localFleet = (Fleet)paramVector.elementAt(0);
      paramVector.removeElementAt(0);
      fleets.insert(localFleet);
    }
    if (++segment == 20)
    {
      turn += 1;
      events.update();
      segment = 0;
    }
    fleets.update();
    for (int i = 0; i < 36; i++)
    {
      planet[i].update();
      planet[i].owner.addScore(planet[i].ratio * 1);
      planet[i].owner.addScore(planet[i].ships * 3);
      planet[i].owner.addScore(planet[i].production * 10);
    }
    fleets.doScores();
    for (int i = 0; i < players; i++) {
      player[i].score = (player[i].score * 10 / 17);
    }
    resolveAllConflicts = false;
  }
}