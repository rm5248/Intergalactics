package igx.client;

import igx.shared.Fleet;
import igx.shared.FleetQueue;
import igx.shared.GameInstance;
import igx.shared.Monitor;
import igx.shared.Planet;
import igx.shared.Player;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;

public class Galaxy
  extends Canvas
{
  static final int MESSAGE_ROWS = 3;
  static final int LOWRATIO = 30;
  static final int MIDRATIO = 50;
  static final int HIGHRATIO = 60;
  static final Color HIGHCOLOUR = Color.red;
  static final Color MIDCOLOUR = Color.yellow;
  static final Color LOWCOLOUR = Color.green;
  static final int MESSAGE_FONT_RATIO = 32;
  static final Color PLANETCOLOUR = new Color(32, 32, 32);
  static final int ATTACK_WIDTH_RATIO = 250;
  public static final int SCROLLBAR_WIDTH_RATIO = 48;
  public static final int SCROLLBAR_HEIGHT_RATIO = 16;
  public static final Color CONNECTION_COLOUR = Color.yellow;
  public String[] messageRow = new String[3];
  public int sliderValue = -1;
  int planetSize;
  int size;
  Font font;
  FontMetrics fontMetric;
  int fontHeight;
  int ratioWidth;
  int zeroRatioY;
  int lowRatioY;
  int midRatioY;
  int highRatioY;
  int highRatioHeight;
  int midRatioHeight;
  int lowRatioHeight;
  GameInstance game;
  int selectedPlanet = -1;
  int targetPlanet = -1;
  int sliderWidth;
  int sliderHeight;
  int sliderMax;
  Monitor redrawMonitor;
  Font messageFont;
  FontMetrics messageFM;
  int messageFontSize;
  int messageFontHeight;
  int messageFontDescent;
  int messageY;
  String message = "";
  int messageInitialX;
  int[] messageX = new int[3];
  Toolkit toolkit;
  Player me = null;
  Vector oldFleets = new Vector();
  
  public Galaxy(GameInstance paramGameInstance, int paramInt1, int paramInt2, Toolkit paramToolkit, Monitor paramMonitor)
  {
    redrawMonitor = paramMonitor;
    game = paramGameInstance;
    size = paramInt1;
    toolkit = paramToolkit;
    font = new Font("SansSerif", 0, paramInt2);
    planetSize = (paramInt1 / 16);
    FontMetrics localFontMetrics = paramToolkit.getFontMetrics(font);
    fontMetric = localFontMetrics;
    fontHeight = localFontMetrics.getAscent();
    ratioWidth = (paramInt1 / 250);
    zeroRatioY = (planetSize - 3);
    highRatioY = 3;
    lowRatioY = ((zeroRatioY - highRatioY) / 2 + highRatioY);
    midRatioY = ((zeroRatioY - highRatioY) / 6 + highRatioY);
    highRatioHeight = ((zeroRatioY - highRatioY) / 6 + 1);
    lowRatioHeight = ((zeroRatioY - highRatioY) / 2);
    midRatioHeight = ((zeroRatioY - highRatioY) / 3 + 1);
    sliderHeight = (paramInt1 / 16);
    sliderWidth = (paramInt1 / 48);
    sliderMax = paramInt1;
    messageFontSize = (paramInt1 / 32);
    messageFont = new Font("SansSerif", 0, messageFontSize);
    messageFM = paramToolkit.getFontMetrics(messageFont);
    messageFontDescent = messageFM.getDescent();
    messageFontHeight = (messageFM.getAscent() + messageFontDescent + 1);
    messageY = ((planetSize - messageFontHeight) / 2 + messageFontHeight);
    messageInitialX = ((planetSize - messageFontHeight) / 2);
    for (int i = 0; i < 3; i++)
    {
      messageRow[i] = "";
      messageX[i] = messageInitialX;
    }
    setBackground(Color.black);
  }
  
  public void setMe(Player paramPlayer)
  {
    me = paramPlayer;
  }
  
  public void abortOrder()
  {
    Graphics localGraphics = getDefaultGraphics();
    if (targetPlanet != -1)
    {
      connectPlanets(localGraphics, selectedPlanet, targetPlanet);
      selectPlanet(localGraphics, targetPlanet);
      targetPlanet = -1;
      if (sliderValue != -1) {
        eraseSlider(localGraphics);
      }
      sliderValue = -1;
    }
    if (selectedPlanet != -1)
    {
      selectPlanet(localGraphics, selectedPlanet);
      selectedPlanet = -1;
    }
  }
  
  public void addMessageChar(char paramChar)
  {
    Graphics localGraphics = getDefaultGraphics();
    int i;
    for (i = 0; (i < 3) && (messageRow[i].length() > 0); i++) {}
    if (i != 0) {
      i--;
    }
    int j = messageFM.charWidth(paramChar);
    if (messageX[i] + j > size) {
      i++;
    }
    if (i == 3) {
      return;
    }
    int tmp84_83 = i;
    String[] tmp84_80 = messageRow;
    tmp84_80[tmp84_83] = (tmp84_80[tmp84_83] + paramChar);
    localGraphics.setColor(Color.white);
    localGraphics.setFont(messageFont);
    plotMessageChar(localGraphics, paramChar, i);
    messageX[i] += j;
    localGraphics.setFont(font);
  }
  
  public void choosePlanet(int paramInt)
  {
    Graphics localGraphics = getDefaultGraphics();
    if (selectedPlanet != -1) {
      selectPlanet(localGraphics, selectedPlanet);
    }
    selectedPlanet = paramInt;
    selectPlanet(localGraphics, selectedPlanet);
  }
  
  public boolean choosePlanet(int paramInt1, int paramInt2)
  {
    int i = getPlanet(paramInt1, paramInt2);
    if (i == -1) {
      return false;
    }
    choosePlanet(i);
    return true;
  }
  
  public void chooseTargetPlanet(int paramInt)
  {
    Graphics localGraphics = getDefaultGraphics();
    targetPlanet = paramInt;
    connectPlanets(localGraphics, selectedPlanet, targetPlanet);
    selectPlanet(localGraphics, targetPlanet);
  }
  
  public boolean chooseTargetPlanet(int paramInt1, int paramInt2)
  {
    int i = getPlanet(paramInt1, paramInt2);
    if ((i == -1) || (i == selectedPlanet)) {
      return false;
    }
    chooseTargetPlanet(i);
    return true;
  }
  
  public void connectPlanets(Graphics paramGraphics, int paramInt1, int paramInt2)
  {
    paramGraphics.setXORMode(Color.black);
    Planet localPlanet1 = game.planet[paramInt1];
    Planet localPlanet2 = game.planet[paramInt2];
    int i = localPlanet1.x - localPlanet2.x;
    int j = localPlanet1.y - localPlanet2.y;
    float f1 = i;
    float f2 = j;
    int i3 = planetSize - 1;
    int m;
    int i1;
    int i2;
    int k;
    int n;
    if (Math.abs(j) > Math.abs(i))
    {
      if (j < 0)
      {
        m = planetSize * localPlanet1.y - 1;
        i1 = planetSize * localPlanet1.y + planetSize;
        j = -j;
      }
      else
      {
        m = planetSize * localPlanet1.y + planetSize;
        i1 = planetSize * localPlanet1.y - 1;
      }
      i2 = i3 * (planetSize + planetSize * i / j) / 2 / planetSize;
      k = planetSize * localPlanet1.x + i2;
      n = planetSize * localPlanet1.x + planetSize - 1 - i2;
    }
    else
    {
      if (i < 0)
      {
        k = planetSize * localPlanet1.x - 1;
        n = planetSize * localPlanet1.x + planetSize;
        i = -i;
      }
      else
      {
        k = planetSize * localPlanet1.x + planetSize;
        n = planetSize * localPlanet1.x - 1;
      }
      i2 = i3 * (planetSize + planetSize * j / i) / 2 / planetSize;
      m = planetSize * localPlanet1.y + i2;
      i1 = planetSize * localPlanet1.y + planetSize - 1 - i2;
    }
    paramGraphics.setColor(CONNECTION_COLOUR);
    paramGraphics.drawLine(n, i1, k, m);
    float f3 = f2 - f1;
    float f4 = -f1 - f2;
    float f5 = -f2 - f1;
    float f6 = f1 - f2;
    float f7 = (float)Math.sqrt(f5 * f5 + f6 * f6);
    f3 *= planetSize / f7 / 3.0F;
    f4 *= planetSize / f7 / 3.0F;
    f5 *= planetSize / f7 / 3.0F;
    f6 *= planetSize / f7 / 3.0F;
    paramGraphics.drawLine(n, i1, n + (int)f3, i1 + (int)f4);
    paramGraphics.drawLine(n, i1, n + (int)f5, i1 + (int)f6);
    paramGraphics.setPaintMode();
  }
  
  public void drawSlider(Graphics paramGraphics)
  {
    if (sliderValue == -1) {
      return;
    }
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawRect(16 * planetSize, sliderValue - sliderHeight, sliderWidth - 1, sliderHeight);
    paramGraphics.setColor(Color.black);
    paramGraphics.drawLine(16 * planetSize + sliderWidth / 2 - 1, sliderValue + 1 - sliderHeight, 16 * planetSize + sliderWidth / 2 - 1, sliderValue - 1);
  }
  
  public void drawSliderBar(Graphics paramGraphics)
  {
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawLine(16 * planetSize + sliderWidth / 2 - 1, 0, 16 * planetSize + sliderWidth / 2 - 1, size);
  }
  
  public String endMessage()
  {
    repaintMessageRow();
    String str = "";
    for (int i = 0; i < 3; i++)
    {
      str = str + messageRow[i];
      messageRow[i] = "";
      messageX[i] = messageInitialX;
    }
    return str;
  }
  
  public void eraseMessageChar()
  {
    for (int i = 2; i >= 0; i--) {
      if (messageRow[i].length() > 0)
      {
        repaintMessageRow();
        messageRow[i] = messageRow[i].substring(0, messageRow[i].length() - 1);
        messageX[i] = (messageFM.stringWidth(messageRow[i]) + messageInitialX);
        paintMessage();
        return;
      }
    }
  }
  
  public void eraseSlider(Graphics paramGraphics)
  {
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(16 * planetSize, sliderValue - sliderHeight, sliderWidth, sliderHeight + 1);
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawLine(16 * planetSize + sliderWidth / 2 - 1, sliderValue - sliderHeight, 16 * planetSize + sliderWidth / 2 - 1, sliderValue);
  }
  
  public Graphics getDefaultGraphics()
  {
    Graphics localGraphics = getGraphics();
    localGraphics.setFont(font);
    localGraphics.setPaintMode();
    return localGraphics;
  }
  
  int getPlanet(int paramInt1, int paramInt2)
  {
    int i = paramInt1 / planetSize;
    int j = paramInt2 / planetSize;
    if ((i >= 16) || (j >= 16)) {
      return -1;
    }
    int k = game.map[i][j];
    if (k == 46) {
      return -1;
    }
    return Planet.char2num((char)k);
  }
  
  public void movePlanet(int paramInt1, int paramInt2, Planet paramPlanet)
  {
    Graphics localGraphics = getDefaultGraphics();
    int i = planetSize * paramInt1;
    int j = planetSize * paramInt2;
    localGraphics.setColor(Color.black);
    localGraphics.fillRect(i, j, planetSize, planetSize);
    repaintPlanet(localGraphics, Planet.char2num(paramPlanet.planetChar));
  }
  
  public void paint(Graphics paramGraphics)
  {
    redrawMonitor.lock();
    paramGraphics.setFont(font);
    paramGraphics.setPaintMode();
    drawSliderBar(paramGraphics);
    for (int i = 0; i < 36; i++) {
      repaintPlanet(paramGraphics, i);
    }
    if (selectedPlanet != -1)
    {
      selectPlanet(paramGraphics, selectedPlanet);
      if (targetPlanet != -1)
      {
        selectPlanet(paramGraphics, targetPlanet);
        connectPlanets(paramGraphics, selectedPlanet, targetPlanet);
        drawSlider(paramGraphics);
      }
    }
    paramGraphics.setXORMode(Color.black);
    drawOldFleets(paramGraphics);
    paramGraphics.setPaintMode();
    if (message.length() != 0) {
      paintMessage();
    }
    redrawMonitor.unlock();
  }
  
  void paintMessage()
  {
    Graphics localGraphics = getDefaultGraphics();
    localGraphics.setColor(Color.white);
    localGraphics.setFont(messageFont);
    for (int i = 0; i < 3; i++) {
      if (messageRow[i].length() > 0) {
        localGraphics.drawString(messageRow[i], messageInitialX, messageY + messageFontHeight * i - messageFontDescent);
      }
    }
  }
  
  public void plotMessageChar(Graphics paramGraphics, char paramChar, int paramInt)
  {
    paramGraphics.drawString(new Character(paramChar).toString(), messageX[paramInt], messageY + messageFontHeight * paramInt - messageFontDescent);
  }
  
  public void redrawPlanet(int paramInt)
  {
    Graphics localGraphics = getDefaultGraphics();
    repaintPlanet(localGraphics, paramInt);
  }
  
  void repaintMessageRow()
  {
    Graphics localGraphics = getDefaultGraphics();
    int i;
    for (i = 0; (i < 3) && (messageRow[i].length() > 0); i++) {}
    i--;
    if (i == -1) {
      return;
    }
    int j = ((i + 1) * messageFontHeight + messageY) / planetSize + 1;
    for (int k = 0; k < j; k++) {
      for (int m = 0; m < 16; m++) {
        if (game.map[m][k] == '.')
        {
          localGraphics.setColor(Color.black);
          localGraphics.fillRect(m * planetSize, k * planetSize, planetSize, planetSize);
        }
        else
        {
          repaintPlanet(localGraphics, Planet.char2num(game.map[m][k]));
        }
      }
    }
  }
  
  public void repaintPlanet(Graphics paramGraphics, int paramInt)
  {
    Planet localPlanet = game.planet[paramInt];
    int i = planetSize * localPlanet.x;
    int j = planetSize * localPlanet.y;
    int k = i + planetSize - 1;
    int m = j + planetSize - 1;
    int n = planetSize * planetSize / 100;
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(i, j, planetSize, planetSize);
    paramGraphics.setColor(new Color(localPlanet.planetShade, localPlanet.planetShade, localPlanet.planetShade));
    paramGraphics.fillOval(i + planetSize / 2 - n / 2, j + planetSize / 2 - n / 2, n, n);
    paramGraphics.setColor(igx.shared.Params.PLAYERCOLOR[localPlanet.owner.number]);
    if (localPlanet.owner.number == 9)
    {
      paramGraphics.drawString(new Character(localPlanet.planetChar).toString(), i + 2, j + fontHeight - 1);
    }
    else
    {
      paramGraphics.drawString(localPlanet.planetChar + "  " + localPlanet.production, i + 2, j + fontHeight - 1);
      paramGraphics.drawString(new Integer(localPlanet.ships).toString(), i + 2, j + 2 * fontHeight - 1);
    }
    int i2;
    int i3;
    if (localPlanet.attackingPlayer != -1) {
      if (localPlanet.attackingPlayer == -2)
      {
        String str = new Integer(localPlanet.totalAttackingShips).toString();
        i2 = fontMetric.stringWidth(str);
        i3 = 0;
        paramGraphics.setXORMode(Color.white);
        Color localColor;
        int i5;
        for (int i4 = 0; i4 < game.players; i4++) {
          if (localPlanet.attacker[i4] != 0)
          {
            localColor = igx.shared.Params.PLAYERCOLOR[i4];
            i5 = localPlanet.attacker[i4] * i2 / localPlanet.totalAttackingShips;
            paramGraphics.setColor(localColor);
            paramGraphics.fillRect(i + 2 + i3, j + 2 * fontHeight - 1, i5, fontHeight);
            i3 += i5;
          }
        }
        paramGraphics.setPaintMode();
        paramGraphics.setColor(Color.white);
        paramGraphics.drawString(str, i + 2, j + 3 * fontHeight - 1);
        paramGraphics.setXORMode(Color.white);
        i3 = 0;
        int i4;
        for (i4 = 0; i4 < game.players; i4++) {
          if (localPlanet.attacker[i4] != 0)
          {
            localColor = igx.shared.Params.PLAYERCOLOR[i4];
            i5 = localPlanet.attacker[i4] * i2 / localPlanet.totalAttackingShips;
            paramGraphics.setColor(localColor);
            paramGraphics.fillRect(i + 2 + i3, j + 2 * fontHeight - 1, i5, fontHeight);
            i3 += i5;
          }
        }
        paramGraphics.setPaintMode();
      }
      else
      {
        paramGraphics.setColor(igx.shared.Params.PLAYERCOLOR[localPlanet.attackingPlayer]);
        paramGraphics.drawString(new Integer(localPlanet.totalAttackingShips).toString(), i + 2, j + 3 * fontHeight - 1);
      }
    }
    if (localPlanet.owner.number != 9)
    {
      int i1 = localPlanet.ratio;
      i2 = (60 - localPlanet.ratio) * (zeroRatioY - highRatioY) / 60;
      i3 = k - 2 - ratioWidth;
      paramGraphics.setColor(LOWCOLOUR);
      paramGraphics.fillRect(i3, lowRatioY + j, ratioWidth, lowRatioHeight);
      paramGraphics.setColor(MIDCOLOUR);
      paramGraphics.fillRect(i3, midRatioY + j, ratioWidth, midRatioHeight);
      paramGraphics.setColor(HIGHCOLOUR);
      paramGraphics.fillRect(i3, highRatioY + j, ratioWidth, highRatioHeight);
      paramGraphics.setColor(Color.black);
      paramGraphics.fillRect(i3, highRatioY + j, ratioWidth, i2);
    }
  }
  
  public void drawFleet(Graphics paramGraphics, int paramInt1, int paramInt2, int paramInt3)
  {
    paramGraphics.setColor(Color.white);
    paramGraphics.drawLine(paramInt1, paramInt2, paramInt1, paramInt2);
    paramGraphics.setColor(Color.gray);
    paramGraphics.drawLine(paramInt1 - 1, paramInt2 - 1, paramInt1 + 1, paramInt2 - 1);
    paramGraphics.drawLine(paramInt1 - 1, paramInt2 + 1, paramInt1 + 1, paramInt2 + 1);
    paramGraphics.setColor(Color.lightGray);
    paramGraphics.drawLine(paramInt1 - 2, paramInt2, paramInt1 - 2, paramInt2);
    paramGraphics.drawLine(paramInt1 + 2, paramInt2, paramInt1 + 2, paramInt2);
    paramGraphics.setColor(igx.shared.Params.PLAYERCOLOR[paramInt3]);
    paramGraphics.drawLine(paramInt1 - 1, paramInt2, paramInt1 - 1, paramInt2);
    paramGraphics.drawLine(paramInt1 + 1, paramInt2, paramInt1 + 1, paramInt2);
  }
  
  public void drawOldFleets(Graphics paramGraphics)
  {
    paramGraphics.setXORMode(Color.black);
    paramGraphics.setColor(Color.white);
    int i = oldFleets.size();
    for (int j = 0; j < i; j++)
    {
      Point localPoint = (Point)oldFleets.elementAt(j);
      drawFleet(paramGraphics, localPoint.x, localPoint.y, me.number);
    }
    paramGraphics.setPaintMode();
  }
  
  public void drawFleets()
  {
    Graphics localGraphics = getDefaultGraphics();
    drawOldFleets(localGraphics);
    localGraphics.setXORMode(Color.black);
    oldFleets = new Vector();
    localGraphics.setColor(Color.white);
    for (Fleet localFleet = game.fleets.first; localFleet != null; localFleet = localFleet.next) {
      if ((localFleet.owner == me) && (localFleet.distance > 0.0F))
      {
        int i = localFleet.source.x * planetSize + planetSize / 2;
        int j = localFleet.source.y * planetSize + planetSize / 2;
        int k = localFleet.destination.x * planetSize + planetSize / 2;
        int m = localFleet.destination.y * planetSize + planetSize / 2;
        float f1 = (float)Math.sqrt((localFleet.destination.x - localFleet.source.x) * (localFleet.destination.x - localFleet.source.x) + 
                (localFleet.destination.y - localFleet.source.y) * (localFleet.destination.y - localFleet.source.y));
        float f2 = localFleet.distance / f1;
        int n = k + (int)((i - k) * f2);
        int i1 = m + (int)((j - m) * f2);
        Point localPoint = new Point(n, i1);
        oldFleets.addElement(localPoint);
        drawFleet(localGraphics, localPoint.x, localPoint.y, me.number);
      }
    }
    localGraphics.setPaintMode();
  }
  
  public void repaintXORs()
  {
    Graphics localGraphics = getDefaultGraphics();
    if (targetPlanet != -1)
    {
      connectPlanets(localGraphics, selectedPlanet, targetPlanet);
      selectPlanet(localGraphics, targetPlanet);
    }
    if (selectedPlanet != -1) {
      selectPlanet(localGraphics, selectedPlanet);
    }
    drawOldFleets(localGraphics);
  }
  
  public void selectPlanet(Graphics paramGraphics, int paramInt)
  {
    paramGraphics.setXORMode(Color.black);
    Planet localPlanet = game.planet[paramInt];
    int i = planetSize * localPlanet.x;
    int j = planetSize * localPlanet.y;
    int k = i + planetSize - 1;
    int m = j + planetSize - 1;
    paramGraphics.setColor(igx.shared.Params.PLAYERCOLOR[localPlanet.owner.number]);
    paramGraphics.drawLine(i, j, k, j);
    paramGraphics.drawLine(k, j, k, m);
    paramGraphics.drawLine(k, m, i, m);
    paramGraphics.drawLine(i, m, i, j);
    paramGraphics.setPaintMode();
  }
  
  public int sendShips(int paramInt)
  {
    int i = size - sliderHeight;
    paramInt -= sliderHeight;
    if (paramInt > i) {
      paramInt = i;
    }
    if (paramInt < 0) {
      paramInt = 0;
    }
    if (game.planet[selectedPlanet].owner.number == 9) {
      return 0;
    }
    return (i - paramInt) * game.planet[selectedPlanet].ships / i;
  }
  
  public void setShipSlider(int paramInt)
  {
    Graphics localGraphics = getDefaultGraphics();
    if (sliderValue != -1) {
      eraseSlider(localGraphics);
    }
    if (paramInt > sliderMax) {
      sliderValue = sliderMax;
    } else if (paramInt < sliderHeight) {
      sliderValue = sliderHeight;
    } else {
      sliderValue = paramInt;
    }
    drawSlider(localGraphics);
  }
}