package igx.client;

import igx.shared.Game;
import igx.shared.Player;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Vector;

public class GameListCanvas
  extends ForumCanvas
{
  public static final int HEADER_ROWS = 3;
  public static final int GAME_WIDTH_RATIO = 10;
  public static final Color HEADING_COLOUR = Color.lightGray;
  public static final Color HEADING_UNDERLINE_COLOUR = Color.gray;
  public static final Color NEW_GAME_COLOUR = Color.white;
  public static final Color IN_PROGRESS_GAME_COLOUR = Color.gray;
  public static final Color COMMA_COLOUR = Color.lightGray;
  public static final Color TITLE_COLOUR = Color.orange;
  public static final Color SERVER_COLOUR = Color.orange;
  public static final Color BORDER_COLOUR = Color.gray;
  int statusColumn;
  int gameColumn;
  int playerColumn;
  TextRow heading0;
  TextRow heading1;
  Vector gameRows;
  int games = 0;
  String serverName;
  ClientForum forum;
  
  public GameListCanvas(ClientForum paramClientForum, String paramString, int paramInt1, int paramInt2, int paramInt3, Toolkit paramToolkit)
  {
    super(paramInt1, computeHeight(paramInt3, paramInt2 * 2 + 3, paramToolkit), paramInt3, paramToolkit);
    serverName = paramString;
    forum = paramClientForum;
    statusColumn = fontHeight;
    gameColumn = (statusColumn + fm.stringWidth("In Progress  "));
    playerColumn = (gameColumn + 10 * fontHeight);
    Vector localVector = new Vector();
    String str = "intergalactics - " + paramString;
    int i = fm.stringWidth(str);
    localVector.addElement(new TextElement("intergalactics", TITLE_COLOUR, (paramInt1 - i) / 2));
    localVector.addElement(new TextElement(" - ", COMMA_COLOUR, -1));
    localVector.addElement(new TextElement(paramString, SERVER_COLOUR, -1));
    heading0 = new TextRow(localVector);
    row[0] = heading0;
    localVector = new Vector();
    localVector.addElement(new TextElement("Status", HEADING_COLOUR, statusColumn));
    localVector.addElement(new TextElement("Game", HEADING_COLOUR, gameColumn));
    localVector.addElement(new TextElement("Players", HEADING_COLOUR, playerColumn));
    heading1 = new TextRow(localVector);
    heading1.underline(HEADING_UNDERLINE_COLOUR);
    row[2] = heading1;
    setRowListener(this);
  }
  
  public void addGame(Game paramGame)
  {
    for (int i = games * 2 + 3 - 1; i >= 3; i--) {
      row[(i + 2)] = row[i];
    }
    games += 1;
    if (selectedRow != -1) {
      selectedRow += 2;
    }
    setRow(0, paramGame);
    row[3].setSelect(false);
    repaint();
  }
  
  public int getRowNum(Game paramGame)
  {
    for (int i = 3; i < games * 2 + 3; i += 2)
    {
      String str = ((TextElement)(row[i].elements.elementAt(1))).text;
      if (str.equals(paramGame.name)) {
        return i;
      }
    }
    return -1;
  }
  
  public void manualSelectRow(int paramInt)
  {
    if ((paramInt + 3) % 2 == 0) {
      super.rowSelected(paramInt);
    } else {
      super.rowSelected(paramInt - 1);
    }
  }
  
  public void paint(Graphics paramGraphics)
  {
    super.paint(paramGraphics);
    paramGraphics.setColor(BORDER_COLOUR);
    paramGraphics.drawRect(0, 0, width - 1, height);
  }
  
  public void removeGame(Game paramGame)
  {
    int i = getRowNum(paramGame);
    if (i == -1) {
      return;
    }
    if (selectedRow >= i) {
      selectedRow -= 2;
    }
    for (int j = i; j < games * 2 + 3; j++) {
      row[j] = row[(j + 2)];
    }
    games -= 1;
    repaint();
  }
  
  public void rowSelected(int paramInt)
  {
    if ((paramInt >= 3) && (forum.gameSelected(paramInt - 3))) {
      manualSelectRow(paramInt);
    }
  }
  
  public void setRow(int paramInt, Game paramGame)
  {
    int i = 0;
    if ((row[(paramInt + 3)] != null) && (row[(paramInt + 3)].selected)) {
      i = 1;
    }
    Vector localVector1 = new Vector();
    Vector localVector2 = new Vector();
    if (paramGame.inProgress)
    {
      localVector1.addElement(new TextElement("In Progress", IN_PROGRESS_GAME_COLOUR, statusColumn));
      localVector1.addElement(new TextElement(paramGame.name, IN_PROGRESS_GAME_COLOUR, gameColumn));
    }
    else
    {
      localVector1.addElement(new TextElement("New", NEW_GAME_COLOUR, statusColumn));
      localVector1.addElement(new TextElement(paramGame.name, NEW_GAME_COLOUR, gameColumn));
    }
    for (int j = 0; j < paramGame.numPlayers; j++)
    {
      Vector localVector3 = null;
      if (j < 4) {
        localVector3 = localVector1;
      } else {
        localVector3 = localVector2;
      }
      if ((j == 0) || (j == 4)) {
        localVector3.addElement(new TextElement(paramGame.player[j].name, igx.shared.Params.PLAYERCOLOR[j], playerColumn));
      } else {
        localVector3.addElement(new TextElement(paramGame.player[j].name, igx.shared.Params.PLAYERCOLOR[j], -1));
      }
      if (j < paramGame.numPlayers - 1) {
        localVector3.addElement(new TextElement(", ", COMMA_COLOUR, -1));
      }
    }
    row[(paramInt + 3)] = new TextRow(localVector1);
    row[(paramInt + 3 + 1)] = new TextRow(localVector2);
    if (i != 0) {
      row[(paramInt + 3)].setSelect(true);
    }
    redrawRow(paramInt + 3);
    redrawRow(paramInt + 3 + 1);
  }
}