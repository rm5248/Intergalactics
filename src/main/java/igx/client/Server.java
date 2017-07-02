package igx.client;

import igx.shared.Fleet;
import igx.shared.FleetQueue;
import igx.shared.Game;
import igx.shared.GameInstance;
import igx.shared.Planet;
import igx.shared.Player;
import igx.shared.Robot;
import igx.shared.SocketAction;
import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server
  extends SocketAction
{
  public ClientForum forum;
  public Dispatcher dispatch;
  public String name;
  
  private static final Logger logger = LogManager.getLogger();
  
  public Server(Socket paramSocket)
  {
    super(paramSocket);
  }
  
  public String receive()
    throws IOException
  {
    String str = super.receive();
    return str;
  }
  
  public boolean receiveBoolean()
    throws IOException
  {
    String str = receive();
    return str.equals("1");
  }
  
  public int receiveInt()
    throws IOException
  {
    String str = receive();
    return Integer.parseInt(str);
  }
  
  public void run()
  {
    try
    {
      logger.debug( "Starting to read from server" );
      String str1 = receive();
      if (!str1.substring(0, 3).equals("3.8".substring(0, 3)))
      {
        send("]");
        forum.frontEnd.versionProblem(str1);
      }
      else
      {
        send("[");
      }
      forum.setDialog(0, "Enter your alias", null);
      int i = 0;
      Object localObject1;
      Object localObject4;
      int m;
      while (i == 0)
      {
        str1 = receive();
        if (str1.equals("*"))
        {
          forum.setDialog(0, "Enter your alias", "That alias already belongs to a robot");
        }
        else if (str1.equals("|"))
        {
          forum.setDialog(0, "Enter your alias", "That alias contains illegal characters");
        }
        else if (str1.equals("{"))
        {
          forum.setDialog(1, "If you are a new user, enter you password. Otherwise, hit <CANCEL> to re-enter your alias.", null);
        }
        else if (str1.equals("}"))
        {
          forum.setDialog(2, "Enter your password", null);
        }
        else if (str1.equals("]"))
        {
          if (forum.dialogMode == 1) {
            forum.setDialog(0, "Enter your alias", null);
          } else {
            forum.setDialog(0, "Enter your alias", "Incorrect Password");
          }
        }
        else if (str1.equals("["))
        {
          for (localObject1 = receive(); !((String)localObject1).equals("~"); localObject1 = receive()) {
            forum.post((String)localObject1, ClientForum.BULLETIN_COLOUR);
          }
          logger.debug( "after forum post" );
          localObject1 = receive();
          Vector localVector = new Vector();
          while (!((String)localObject1).equals("~"))
          {
            Object localObject3 = localObject1;
            localObject4 = receive();
            int i1 = Integer.parseInt(receive());
            localVector.addElement(new Robot(localObject3.toString(), (String)localObject4, i1));
            localObject1 = receive();
          }
          m = localVector.size();
          Robot[] robotArray = new Robot[m];
          for (int i1 = 0; i1 < m; i1++) {
              Robot r = (Robot)localVector.elementAt(i1);
            robotArray[i1] = r;
          }
          forum.setBotList(robotArray);
          forum.registerClient();
          i = 1;
        }
      }
      for (;;)
      {
        str1 = receive();
        String str2;
        String str3;
        if (str1.compareTo("+") == 0)
        {
          str1 = receive();
          if (str1.compareTo("^") == 0)
          {
            str2 = receive();
            forum.addPlayer(str2);
            if (dispatch != null) {
              dispatch.playerArrived(str2);
            } else {
              forum.frontEnd.play("arrive.au");
            }
          }
          else if (str1.compareTo("#") == 0)
          {
            str2 = receive();
            str1 = receive();
            forum.createGame(str1, str2);
          }
          else if (str1.compareTo("$") == 0)
          {
            str2 = receive();
            str3 = receive();
            forum.joinGame(str3, str2);
          }
          else
          {
            int i6;
            if (str1.compareTo("&") == 0)
            {
              str2 = receive();
              str3 = receive();
              localObject1 = forum.startNewGame(str2, str3);
              if (localObject1 != null)
              {
                long l2 = Long.parseLong(receive());
                localObject4 = receive();
                Point[] arrayOfPoint = new Point[36];
                int i3;
                //ROBERT unsure where numPlayers and player array come from
                int numPlayers = 0;
                Player[] player = new Player[ 0 ];
                for (i3 = 0; !((String)localObject4).equals("<"); i3++)
                {
                  int i4 = Integer.parseInt((String)localObject4);
                  localObject4 = receive();
                  i6 = Integer.parseInt((String)localObject4);
                  localObject4 = receive();
                  arrayOfPoint[i3] = new Point(i4, i6);
                }
                GameInstance localGameInstance = new GameInstance(l2, numPlayers, player);
                if (i3 > 0) {
                  localGameInstance.setMap(arrayOfPoint);
                }
                dispatch = new Dispatcher(localGameInstance, this, name);
                forum.displayGame(dispatch, false);
              }
            }
            else
            {
              Object localObject2;
              if (str1.compareTo(">") == 0)
              {
                localObject1 = new Vector();
                for (localObject2 = receive(); !((String)localObject2).equals(">"); localObject2 = receive()) {
                  ((Vector)localObject1).addElement(localObject2);
                }
                forum.doChooseMap((Vector)localObject1);
              }
              else if (str1.compareTo("%") == 0)
              {
                str3 = receive();
                forum.abandonGame(str3);
              }
              else if (str1.compareTo("!") == 0)
              {
                str2 = receive();
                if (str2.equals(name)) {
                  forum.frontEnd.quitProgram();
                }
                if (forum.playerQuit(str2)) {
                  break;
                }
                if (dispatch != null) {
                  dispatch.playerLeft(str2);
                }
              }
              else if (str1.compareTo("@") == 0)
              {
                str2 = receive();
                str1 = receive();
                str3 = receive();
                forum.message(str2, str3);
                if (dispatch != null) {
                  dispatch.forumMessage(str2, str3);
                } else {
                  forum.frontEnd.play("message.au");
                }
              }
              else if (str1.compareTo("*") == 0)
              {
                str2 = receive();
                str3 = receive();
                forum.addRobot(str3, str2);
              }
              else if (str1.compareTo("?") == 0)
              {
                str2 = receive();
                localObject1 = receive();
                str3 = receive();
                if (((String)localObject1).equals("["))
                {
                  localObject2 = new Robot("Custom", str3, 0);
                  forum.addCustomRobot((Robot)localObject2, str2);
                }
                else
                {
                  forum.post("Couldn't add robot: " + str3, Color.red);
                }
              }
              else if (str1.compareTo("\\") == 0)
              {
                str2 = receive();
                str3 = receive();
                forum.removeRobot(str3, str2);
              }
              else if (str1.compareTo("(") == 0)
              {
                str2 = receive();
                str3 = receive();
                forum.gameOver(str2, str3);
              }
              else if (str1.compareTo(")") == 0)
              {
                forum.dialog.setMessageText("Please wait while game is downloaded...");
                localObject1 = new GameInstance();
                localObject2 = receive();
                m = receiveInt();
                int n = receiveInt();
                int i2 = receiveInt();
                Player[] arrayOfPlayer = new Player[m];
                for (int i5 = 0; i5 < m; i5++)
                {
                  arrayOfPlayer[i5] = new Player(receive());
                  arrayOfPlayer[i5].isActive = receiveBoolean();
                  arrayOfPlayer[i5].isHuman = receiveBoolean();
                  arrayOfPlayer[i5].status = receiveInt();
                  arrayOfPlayer[i5].number = i5;
                  if ((arrayOfPlayer[i5].isHuman) && (forum.getPlayer(name) == null)) {
                    arrayOfPlayer[i5].isPresent = false;
                  }
                }
                Planet[] arrayOfPlanet = new Planet[36];
                for (i6 = 0; i6 < 36; i6++)
                {
                  arrayOfPlanet[i6] = new Planet(i6, (GameInstance)localObject1);
                  arrayOfPlanet[i6].x = receiveInt();
                  arrayOfPlanet[i6].y = receiveInt();
                  int i7 = receiveInt();
                  if (i7 == 9) {
                    arrayOfPlanet[i6].owner = Player.NEUTRAL;
                  } else {
                    arrayOfPlanet[i6].owner = arrayOfPlayer[i7];
                  }
                  arrayOfPlanet[i6].production = receiveInt();
                  arrayOfPlanet[i6].ratio = receiveInt();
                  arrayOfPlanet[i6].ships = receiveInt();
                  arrayOfPlanet[i6].defenceRatio = receiveInt();
                  arrayOfPlanet[i6].prodTurns = receiveInt();
                  arrayOfPlanet[i6].blackHole = receiveBoolean();
                  for (String localObject5 = receive(); !((String)localObject5).equals("~"); localObject5 = receive())
                  {
                    int i8 = Integer.parseInt((String)localObject5);
                    arrayOfPlanet[i6].attacker[i8] = receiveInt();
                  }
                }
                FleetQueue localFleetQueue = new FleetQueue((GameInstance)localObject1);
                for (String str4 = receive(); !str4.equals("~"); str4 = receive())
                {
                  Planet localObject5 = arrayOfPlanet[Integer.parseInt(str4)];
                  Planet localPlanet = arrayOfPlanet[receiveInt()];
                  int i9 = receiveInt();
                  int i10 = receiveInt();
                  Player localPlayer3 = arrayOfPlayer[receiveInt()];
                  float f = new Float(receive()).floatValue();
                  Fleet localFleet = new Fleet((GameInstance)localObject1, (Planet)localObject5, localPlanet, localPlayer3, i9, i10, f);
                  localFleetQueue.insert(localFleet);
                }
                ((GameInstance)localObject1).setGameInstance(m, arrayOfPlayer, arrayOfPlanet, localFleetQueue, n, i2);
                forum.selectGame((String)localObject2);
                dispatch = new Dispatcher((GameInstance)localObject1, this, (String)localObject2);
                forum.watchGame(name, (String)localObject2);
                Player localObject5 = ((GameInstance)localObject1).getPlayer(name);
                if (localObject5 != null)
                {
                  localObject5.isPresent = true;
                  forum.displayGame(dispatch, false);
                }
                else
                {
                  forum.displayGame(dispatch, true);
                }
              }
            }
          }
        }
        else if (dispatch != null)
        {
          if (str1.compareTo("|") == 0)
          {
            str2 = receive();
            long l1 = Long.parseLong(str2);
            dispatch.Game.setSeed(l1);
          }
          else if (str1.compareTo("~") == 0)
          {
            dispatch.advance();
          }
          else
          {
            int j;
            if (str1.compareTo("!") == 0)
            {
              str2 = receive();
              j = Integer.parseInt(str2);
              str3 = receive();
              int k = Integer.parseInt(str3);
              if (k == 0)
              {
                if (j == -1)
                {
                  Player localPlayer2 = forum.getPlayer(name);
                  localPlayer2.isActive = false;
                  localPlayer2.inGame = false;
                  localPlayer2.game = null;
                  forum.displayForum(true);
                }
                else
                {
                  dispatch.playerQuit(j, k);
                  if (name.equals(dispatch.Game.player[j].name))
                  {
                    forum.frontEnd.play("youquit.au");
                    forum.displayForum(true);
                  }
                }
              }
              else {
                dispatch.playerQuit(j, k);
              }
            }
            else if (str1.compareTo("@") == 0)
            {
              str2 = receive();
              str3 = receive();
              str1 = receive();
              j = Integer.parseInt(str3);
              Player localPlayer1 = dispatch.Game.getPlayer(str2);
              if (localPlayer1 == null)
              {
                if (j == 9) {
                  dispatch.watcherMessage(str2, Player.NEUTRAL, str1);
                } else {
                  dispatch.watcherMessage(str2, dispatch.Game.player[j], str1);
                }
              }
              else if (j == 9) {
                dispatch.message(localPlayer1, Player.NEUTRAL, str1);
              } else {
                dispatch.message(localPlayer1, dispatch.Game.player[j], str1);
              }
            }
            else
            {
              str2 = receive();
              str3 = receive();
              dispatch.addFleet(new Fleet(dispatch.Game, dispatch.Game.planet[Integer.valueOf(str1).intValue()], dispatch.Game.planet[Integer.valueOf(str2).intValue()], Integer.valueOf(str3).intValue()));
            }
          }
        }
      }
    }
    catch (IOException localIOException) {}
    closeConnections();
    forum.frontEnd.quitProgram();
  }
  
  public void setForum(ClientForum paramClientForum)
  {
    forum = paramClientForum;
  }
}