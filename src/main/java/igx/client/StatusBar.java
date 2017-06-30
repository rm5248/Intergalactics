package igx.client;

import igx.shared.GameInstance;
import igx.shared.Params;
import igx.shared.Planet;
import igx.shared.Player;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;

public class StatusBar
  extends Canvas
{
  public static final int MODE_NORMAL = 0;
  public static final int MODE_SELECT = 1;
  public static final int MODE_SHIPS = 2;
  public static final int MODE_MESSAGE = 3;
  public static final int MODE_QUIT = 4;
  public static final int MARGIN = 1;
  public static final int MAX_SHIP_DIGITS = 6;
  int fontSize;
  int fontHeight;
  int fontStart;
  int spaceWidth;
  public int width;
  public int height;
  int numPlayers;
  int mode = 0;
  int messageTo = -1;
  int numShips = -1;
  int turn;
  int segment;
  Planet selectedPlanet;
  Planet targetedPlanet;
  Font font;
  FontMetrics fm;
  GameInstance game;
  int playerNum;
  String playerName;
  
  public StatusBar(GameInstance paramGameInstance, int paramInt1, Toolkit paramToolkit, int paramInt2, int paramInt3, String paramString)
  {
    game = paramGameInstance;
    width = paramInt2;
    playerNum = paramInt3;
    playerName = paramString;
    setBackground(Color.black);
    font = new Font("SansSerif", 0, paramInt1);
    fm = paramToolkit.getFontMetrics(font);
    spaceWidth = fm.charWidth(' ');
    fontHeight = (fm.getAscent() + fm.getDescent() + 2);
    fontStart = fm.getAscent();
    numPlayers = players;
    height = ((numPlayers + 1 + 4) * fontHeight + 1 + 1);
  }
  
  public void addShips(int paramInt)
  {
    if (numShips == -1) {
      str = "";
    } else {
      str = new Integer(numShips).toString();
    }
    String str = str + new Integer(paramInt).toString();
    setShips(Integer.parseInt(str));
  }
  
  public void clearLine(Graphics paramGraphics, int paramInt)
  {
    Color localColor = paramGraphics.getColor();
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(0, paramInt * fontHeight + 1, width, fontHeight - 1);
    paramGraphics.setColor(localColor);
  }
  
  Graphics clearMode()
  {
    Graphics localGraphics = getDefaultGraphics();
    mode = 0;
    messageTo = -1;
    numShips = -1;
    selectedPlanet = (this.targetedPlanet = null);
    clearStatus(localGraphics);
    return localGraphics;
  }
  
  public void clearStatus(Graphics paramGraphics)
  {
    for (int i = numPlayers + 1; i < numPlayers + 5; i++) {
      clearLine(paramGraphics, i);
    }
  }
  
  public void drawDistanceInfo(Graphics paramGraphics)
  {
    CText[] arrayOfCText = new CText[5];
    arrayOfCText[0] = new CText("ETA:", Color.lightGray);
    arrayOfCText[1] = new CText(" " + turn, Color.white);
    arrayOfCText[2] = new CText(":", Color.lightGray);
    arrayOfCText[3] = new CText(new Integer(segment).toString(), Color.white);
    arrayOfCText[4] = new CText(".", Color.lightGray);
    drawLine(paramGraphics, arrayOfCText, numPlayers + 3);
  }
  
  public void drawLine(Graphics paramGraphics, CText[] paramArrayOfCText, int paramInt)
  {
    int i = paramInt * fontHeight + 1 + fontStart;
    int j = 1;
    for (int k = 0; k < paramArrayOfCText.length; k++)
    {
      paramGraphics.setColor(color);
      if ((text.length() > 0) && (text.charAt(0) == ' ')) {
        j += spaceWidth;
      }
      paramGraphics.drawString(text, j, i);
      j += fm.stringWidth(text);
    }
  }
  
  public void drawMessageInfo(Graphics paramGraphics)
  {
    CText[] arrayOfCText = new CText[2];
    arrayOfCText[0] = new CText("Message to", Color.lightGray);
    if (messageTo == -1) {
      arrayOfCText[1] = new CText(" ", Color.lightGray);
    } else if (messageTo == NEUTRALnumber + 1) {
      arrayOfCText[1] = new CText(" Forum", Color.white);
    } else if (messageTo == NEUTRALnumber) {
      arrayOfCText[1] = new CText(" Game", Color.white);
    } else {
      arrayOfCText[1] = new CText(" " + game.player[messageTo].name, game.player[messageTo].getColor());
    }
    drawLine(paramGraphics, arrayOfCText, numPlayers + 1);
    if (messageTo != -1)
    {
      arrayOfCText = new CText[1];
      arrayOfCText[0] = new CText("Type message", Color.lightGray);
      drawLine(paramGraphics, arrayOfCText, numPlayers + 2);
      arrayOfCText = new CText[3];
      arrayOfCText[0] = new CText("Click", Color.lightGray);
      arrayOfCText[1] = new CText(" MESSAGE", Color.white);
      arrayOfCText[2] = new CText(" button to send", Color.lightGray);
      drawLine(paramGraphics, arrayOfCText, numPlayers + 3);
    }
  }
  
  public void drawPlanetInfo(Graphics paramGraphics, Planet paramPlanet, int paramInt)
  {
    CText[] arrayOfCText = new CText[6];
    arrayOfCText[0] = new CText(new Character(planetChar).toString(), owner.getColor());
    if (owner == Player.NEUTRAL)
    {
      arrayOfCText[1] = new CText(" (", Color.lightGray);
      if (blackHole) {
        arrayOfCText[2] = new CText("Black Hole", Params.NEUTRALCOLOR);
      } else {
        arrayOfCText[2] = new CText("Neutral", Params.NEUTRALCOLOR);
      }
      arrayOfCText[3] = new CText(")", Color.lightGray);
      void tmp141_138 = new CText("", Color.black);
      arrayOfCText[5] = tmp141_138;
      arrayOfCText[4] = tmp141_138;
    }
    else
    {
      arrayOfCText[1] = new CText(" (", Color.lightGray);
      arrayOfCText[2] = new CText(new Integer(ships).toString(), Color.white);
      arrayOfCText[3] = new CText(" ships,", Color.lightGray);
      arrayOfCText[4] = new CText(" " + new Integer(ratio).toString(), Color.white);
      arrayOfCText[5] = new CText("% ratio)", Color.lightGray);
    }
    drawLine(paramGraphics, arrayOfCText, paramInt);
  }
  
  public void drawQuitInfo(Graphics paramGraphics)
  {
    CText[] arrayOfCText = new CText[3];
    arrayOfCText[0] = new CText("Press <", Color.lightGray);
    arrayOfCText[1] = new CText("R", Color.white);
    arrayOfCText[2] = new CText("> for \"ready to quit\" or", Color.lightGray);
    drawLine(paramGraphics, arrayOfCText, numPlayers + 2);
    arrayOfCText[0] = new CText("press <", Color.lightGray);
    arrayOfCText[1] = new CText("Y", Color.white);
    arrayOfCText[2] = new CText("> to quit.", Color.lightGray);
    drawLine(paramGraphics, arrayOfCText, numPlayers + 3);
  }
  
  public void drawScoreInfo(Graphics paramGraphics)
  {
    Player[] arrayOfPlayer = new Player[numPlayers];
    for (int i = 0; i < numPlayers; i++)
    {
      int j = 0;
      for (int k = 0; k < numPlayers; k++) {
        if (i != k) {
          if (game.player[k].score > game.player[i].score) {
            j++;
          } else if ((game.player[k].score == game.player[i].score) && (k < i)) {
            j++;
          }
        }
      }
      arrayOfPlayer[j] = game.player[i];
    }
    for (i = 0; i < numPlayers; i++)
    {
      String str1 = "(" + (number + 1) + ") " + name;
      paramGraphics.setColor(arrayOfPlayer[i].getColor());
      if (!isHuman) {
        str1 = str1 + "(robot)";
      } else if (!isActive) {
        str1 = str1 + "(quit)";
      } else if (!isPresent) {
        str1 = str1 + "(missing)";
      } else if (status == 1) {
        str1 = str1 + "(ready to quit)";
      }
      clearLine(paramGraphics, i);
      paramGraphics.drawString(str1, 1, fontHeight * i + 1 + fontStart);
      String str2 = new Integer(score).toString();
      if (isActive) {
        paramGraphics.drawString(str2, 3 * width / 4, fontHeight * i + 1 + fontStart);
      }
    }
  }
  
  public void drawSelectInfo(Graphics paramGraphics)
  {
    drawPlanetInfo(paramGraphics, selectedPlanet, numPlayers + 1);
  }
  
  public void drawSendInfo(Graphics paramGraphics)
  {
    drawPlanetInfo(paramGraphics, selectedPlanet, numPlayers + 1);
    drawPlanetInfo(paramGraphics, targetedPlanet, numPlayers + 2);
    drawDistanceInfo(paramGraphics);
    if (numShips != -1) {
      drawShipInfo(paramGraphics);
    }
  }
  
  public void drawShipInfo(Graphics paramGraphics)
  {
    CText[] arrayOfCText = new CText[3];
    arrayOfCText[0] = new CText("Send", Color.lightGray);
    arrayOfCText[1] = new CText(" " + new Integer(numShips).toString(), Color.white);
    arrayOfCText[2] = new CText(" ships.", Color.lightGray);
    drawLine(paramGraphics, arrayOfCText, numPlayers + 4);
  }
  
  public void drawTimeInfo(Graphics paramGraphics)
  {
    CText[] arrayOfCText = new CText[6];
    arrayOfCText[0] = new CText("Time", Color.lightGray);
    arrayOfCText[1] = new CText(" " + game.turn, Color.white);
    arrayOfCText[2] = new CText(":", Color.lightGray);
    arrayOfCText[3] = new CText(new Integer(game.segment).toString(), Color.white);
    arrayOfCText[4] = new CText(" You: ", Color.lightGray);
    if ((playerNum != -1) && (game.player[playerNum].isActive)) {
      arrayOfCText[5] = new CText(game.player[playerNum].name, Params.PLAYERCOLOR[playerNum]);
    } else {
      arrayOfCText[5] = new CText(playerName, Color.white);
    }
    drawLine(paramGraphics, arrayOfCText, numPlayers);
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawLine(0, (numPlayers + 1) * fontHeight + 1 - 1, width, (numPlayers + 1) * fontHeight + 1 - 1);
  }
  
  public Graphics getDefaultGraphics()
  {
    Graphics localGraphics = getGraphics();
    localGraphics.setFont(font);
    return localGraphics;
  }
  
  public int getShips()
  {
    return numShips;
  }
  
  public void messageMode()
  {
    Graphics localGraphics = clearMode();
    mode = 3;
    drawMessageInfo(localGraphics);
  }
  
  public void messageMode(int paramInt)
  {
    Graphics localGraphics = clearMode();
    messageTo = paramInt;
    mode = 3;
    drawMessageInfo(localGraphics);
  }
  
  public void nextTurn()
  {
    Graphics localGraphics = getDefaultGraphics();
    clearLine(localGraphics, numPlayers);
    drawScoreInfo(localGraphics);
    drawTimeInfo(localGraphics);
    if (mode == 2)
    {
      planetDistance();
      clearLine(localGraphics, numPlayers + 3);
      drawDistanceInfo(localGraphics);
    }
  }
  
  public void normalMode()
  {
    clearMode();
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setFont(font);
    drawScoreInfo(paramGraphics);
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawLine(0, fontHeight * numPlayers + 1 - 1, width, fontHeight * numPlayers + 1 - 1);
    drawTimeInfo(paramGraphics);
    switch (mode)
    {
    case 1: 
      drawSelectInfo(paramGraphics);
      break;
    case 2: 
      drawSendInfo(paramGraphics);
      break;
    case 3: 
      drawMessageInfo(paramGraphics);
      break;
    case 4: 
      drawQuitInfo(paramGraphics);
    }
    paramGraphics.setColor(Color.gray);
  }
  
  public void planetChanged(int paramInt)
  {
    Planet localPlanet = game.planet[paramInt];
    if ((selectedPlanet != null) && (planetChar == selectedPlanet.planetChar)) {
      updateSelectedPlanet();
    } else if ((targetedPlanet != null) && (planetChar == targetedPlanet.planetChar)) {
      updateTargetedPlanet();
    }
  }
  
  public void planetDistance()
  {
    int i = selectedPlanet.x - targetedPlanet.x;
    int j = selectedPlanet.y - targetedPlanet.y;
    int k = (int)(Math.sqrt(i * i + j * j) * 20.0D / 2.0D + 0.99999999D);
    turn = (game.turn + k / 20);
    segment = (game.segment + k % 20);
    if (segment >= 20)
    {
      segment -= 20;
      turn += 1;
    }
  }
  
  public void planetMoved(Planet paramPlanet)
  {
    if ((targetedPlanet != null) && ((planetChar == selectedPlanet.planetChar) || (planetChar == targetedPlanet.planetChar)))
    {
      planetDistance();
      Graphics localGraphics = getDefaultGraphics();
      clearLine(localGraphics, numPlayers + 3);
      drawDistanceInfo(localGraphics);
    }
  }
  
  public void quitMode()
  {
    Graphics localGraphics = clearMode();
    mode = 4;
    drawQuitInfo(localGraphics);
  }
  
  public void selectMode(Planet paramPlanet)
  {
    Graphics localGraphics = clearMode();
    mode = 1;
    selectedPlanet = paramPlanet;
    drawSelectInfo(localGraphics);
  }
  
  public void sendShipsMode(Planet paramPlanet)
  {
    Planet localPlanet = selectedPlanet;
    Graphics localGraphics = clearMode();
    mode = 2;
    selectedPlanet = localPlanet;
    targetedPlanet = paramPlanet;
    planetDistance();
    drawSendInfo(localGraphics);
  }
  
  public void setShips(int paramInt)
  {
    Graphics localGraphics = getDefaultGraphics();
    clearLine(localGraphics, numPlayers + 4);
    numShips = paramInt;
    drawShipInfo(localGraphics);
  }
  
  public void updateSelectedPlanet()
  {
    Graphics localGraphics = getDefaultGraphics();
    clearLine(localGraphics, numPlayers + 1);
    drawPlanetInfo(localGraphics, selectedPlanet, numPlayers + 1);
  }
  
  public void updateTargetedPlanet()
  {
    Graphics localGraphics = getDefaultGraphics();
    clearLine(localGraphics, numPlayers + 2);
    drawPlanetInfo(localGraphics, targetedPlanet, numPlayers + 2);
  }
}