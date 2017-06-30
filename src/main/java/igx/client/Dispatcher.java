package igx.client;

import igx.shared.Fleet;
import igx.shared.GameInstance;
import igx.shared.Player;
import java.util.Vector;

class Dispatcher
{
  GameInstance Game;
  ClientUI ui;
  Server server;
  Vector fleets = new Vector();
  String name;
  
  public Dispatcher(GameInstance paramGameInstance, Server paramServer, String paramString)
  {
    Game = paramGameInstance;
    server = paramServer;
    name = paramString;
  }
  
  public void addFleet(Fleet paramFleet)
  {
    fleets.addElement(paramFleet);
  }
  
  public void advance()
  {
    Game.update(fleets);
    ui.postNextTurn();
    fleets.removeAllElements();
  }
  
  public void dispatch(char paramChar1, char paramChar2, int paramInt)
  {
    Player localPlayer = getMe();
    if ((localPlayer != null) && (isActive))
    {
      server.send(new Character(paramChar1).toString());
      server.send(new Character(paramChar2).toString());
      server.send(String.valueOf(paramInt));
    }
  }
  
  public void forumMessage(String paramString1, String paramString2)
  {
    ui.postForumMessage(paramString1, paramString2);
  }
  
  public Player getMe()
  {
    return Game.getPlayer(server.name);
  }
  
  public void message(Player paramPlayer1, Player paramPlayer2, String paramString)
  {
    ui.postMessage(paramPlayer1, paramPlayer2, paramString);
  }
  
  public void playerArrived(String paramString)
  {
    ui.postArrival(paramString);
  }
  
  public void playerLeft(String paramString)
  {
    for (int i = 0; i < Game.players; i++) {
      if ((Game.player[i].name.equals(paramString)) && (Game.player[i].isActive))
      {
        Game.player[i].isPresent = false;
        ui.postPlayerLeft(paramString, i);
        return;
      }
    }
    ui.postPlayerLeft(paramString, -1);
  }
  
  public void playerQuit(int paramInt1, int paramInt2)
  {
    if (paramInt2 != 0)
    {
      Game.player[paramInt1].status = paramInt2;
      ui.postPlayerQuit(Game.player[paramInt1], paramInt2);
    }
    else
    {
      Game.player[paramInt1].isActive = false;
      if (!Game.player[paramInt1].name.equals(server.name)) {
        ui.postPlayerQuit(Game.player[paramInt1], paramInt2);
      }
    }
  }
  
  public void quit(int paramInt)
  {
    server.send("!");
    server.send(new Integer(paramInt).toString());
  }
  
  public void registerUI(ClientUI paramClientUI)
  {
    ui = paramClientUI;
  }
  
  public void sendMessage(int paramInt, String paramString)
  {
    server.send("@");
    if (paramInt == -1) {
      paramInt = 9;
    }
    server.send(new Integer(paramInt).toString());
    server.send(paramString);
  }
  
  public void watcherMessage(String paramString1, Player paramPlayer, String paramString2)
  {
    ui.postForumMessage(paramString1, paramPlayer, paramString2);
  }
  
  public void youQuit() {}
}