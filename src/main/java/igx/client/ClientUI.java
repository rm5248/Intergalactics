package igx.client;

import igx.shared.Fleet;
import igx.shared.GameInstance;
import igx.shared.Monitor;
import igx.shared.Params;
import igx.shared.Planet;
import igx.shared.Player;
import igx.shared.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ClientUI
  extends Panel
  implements UI, MouseListener, MouseMotionListener, KeyListener, ActionListener, ButtonListener
{
  static final int GALAXY_FONT_RATIO = 60;
  static final int MESSAGE_FONT_RATIO = 54;
  static final int STATUS_FONT_RATIO = 42;
  static final int MODE_NORMAL = 0;
  static final int MODE_SELECT = 1;
  static final int MODE_SEND_SHIPS = 2;
  static final int MODE_MESSAGE_TO = 3;
  static final int MODE_MESSAGE = 4;
  static final int MODE_QUIT = 5;
  static final int COMPONENT_GALAXY = 0;
  static final int COMPONENT_MESSAGEBOARD = 1;
  static final int COMPONENT_STATUSBAR = 2;
  static final int BUTTON_ACTION = 0;
  static final int BUTTON_CANCEL = 1;
  static final String MESSAGE_ACTION = "MESSAGE";
  static final String QUIT_ACTION = "QUIT";
  int width;
  int height;
  int fontSize;
  int galaxySize;
  int sideWidth;
  Panel sidebar;
  StatusBar statusBar;
  ButtonCanvas buttonBar;
  ScrollText messageBoard;
  Galaxy galaxy;
  Dispatcher dispatcher;
  GameInstance game;
  int mode = 0;
  int messageReceiver;
  PopupMenu messageMenu;
  PopupMenu quitMenu;
  Point messagePoint;
  Point quitPoint;
  FrontEnd frontEnd;
  ClientForum forum;
  Monitor drawMonitor = new Monitor();
  boolean watcher = false;
  
  public ClientUI(Dispatcher paramDispatcher, FrontEnd paramFrontEnd, ClientForum paramClientForum, boolean paramBoolean)
  {
    super(new BorderLayout());
    frontEnd = paramFrontEnd;
    dispatcher = paramDispatcher;
    game = paramDispatcher.Game;
    forum = paramClientForum;
    watcher = paramBoolean;
    enableEvents(8L);
    Dimension localDimension1 = paramFrontEnd.getDimensions();
    width = localDimension1.width;
    height = localDimension1.height;
    fontSize = (height / 60);
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    galaxySize = (height / 16 * 16);
    fontSize = (FontFinder.getFont(localToolkit, "SansSerif", 48, galaxySize).getSize() + 2);
    galaxy = new Galaxy(game, galaxySize, fontSize, localToolkit, drawMonitor);
    galaxy.setMe(paramDispatcher.getMe());
    sidebar = new Panel(new BorderLayout());
    sidebar.setBackground(Color.black);
    add(galaxy, "West");
    int i = FontFinder.getFont(localToolkit, "SansSerif", 5, 13 * height / 100).getSize();
    if (paramBoolean) {
      statusBar = new StatusBar(game, i, localToolkit, width - height - height / 48, -1, paramClientForum.clientName);
    } else {
      statusBar = new StatusBar(game, height / 42, localToolkit, width - height - height / 48, -1, paramClientForum.clientName);
    }
    buttonBar = new ButtonCanvas(height / 54, localToolkit, width - height - height / 48, 0);
    buttonBar.height = ((14 * buttonBar.buttonHeight / 10 + 1) * 2);
    buttonBar.setButtonListener(this);
    int j = buttonBar.width / 2;
    String str = "(M)essage (F1)";
    Dimension localDimension2 = buttonBar.buttonDimensions(str);
    Point localPoint = new Point((j - width) / 2, 0 * (14 * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(localPoint.x, localPoint.y, str);
    messagePoint = localPoint;
    str = "(S)ound On/Off";
    localDimension2 = buttonBar.buttonDimensions(str);
    localPoint = new Point((j - width) / 2 + j, 0 * (14 * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(localPoint.x, localPoint.y, str);
    str = "(F)orum";
    localDimension2 = buttonBar.buttonDimensions(str);
    if (paramBoolean) {
      localPoint = new Point((buttonBar.width - width) / 2, 1 * (14 * buttonBar.buttonHeight / 10 + 1));
    } else {
      localPoint = new Point((j - width) / 2, 1 * (14 * buttonBar.buttonHeight / 10 + 1));
    }
    buttonBar.addButton(localPoint.x, localPoint.y, str);
    if (!paramBoolean)
    {
      str = "(Q)uit Game";
      localDimension2 = buttonBar.buttonDimensions(str);
      localPoint = new Point((j - width) / 2 + j, 1 * (14 * buttonBar.buttonHeight / 10 + 1));
      buttonBar.addButton(localPoint.x, localPoint.y, str);
    }
    quitPoint = new Point(localPoint.x + width, localPoint.y);
    buttonBar.prepareButtons();
    messageBoard = new ScrollText(fontSize, localToolkit, width - height - height / 48, height - statusBar.height - buttonBar.height);
    sidebar.add(statusBar, "North");
    sidebar.add(buttonBar, "Center");
    sidebar.add(messageBoard, "South");
    add(sidebar, "East");
    galaxy.setSize(galaxySize + height / 48, height);
    statusBar.setSize(statusBar.width, statusBar.height);
    messageBoard.setSize(width - height - height / 48, height - statusBar.height);
    sidebar.setSize(width - height - height / 48, height);
    quitMenu = new PopupMenu();
    buttonBar.add(quitMenu);
    MenuItem localMenuItem1 = new MenuItem("Don't Quit");
    localMenuItem1.setActionCommand("*2");
    localMenuItem1.addActionListener(this);
    quitMenu.add(localMenuItem1);
    if (!paramBoolean)
    {
      localMenuItem1 = new MenuItem("Ready to Quit");
      localMenuItem1.setActionCommand("*1");
      localMenuItem1.addActionListener(this);
      quitMenu.add(localMenuItem1);
    }
    localMenuItem1 = new MenuItem("Quit");
    localMenuItem1.setActionCommand("*0");
    localMenuItem1.addActionListener(this);
    quitMenu.add(localMenuItem1);
    messageMenu = new PopupMenu();
    buttonBar.add(messageMenu);
    MenuItem localMenuItem2 = new MenuItem("This Game");
    localMenuItem2.setActionCommand("0");
    localMenuItem2.addActionListener(this);
    messageMenu.add(localMenuItem2);
    messageMenu.add("-");
    MenuItem localMenuItem3 = new MenuItem("Forum");
    localMenuItem3.setActionCommand("-1");
    localMenuItem3.addActionListener(this);
    messageMenu.add(localMenuItem3);
    messageMenu.add("-");
    for (int k = 0; k < game.players; k++)
    {
      MenuItem localMenuItem4 = new MenuItem(game.player[k].name);
      localMenuItem4.setActionCommand(new Integer(k + 1).toString());
      localMenuItem4.addActionListener(this);
      messageMenu.add(localMenuItem4);
    }
    galaxy.addMouseListener(this);
    galaxy.addMouseMotionListener(this);
    galaxy.addKeyListener(this);
    messageBoard.addKeyListener(this);
    statusBar.addKeyListener(this);
    buttonBar.addKeyListener(this);
    sidebar.addKeyListener(this);
    addKeyListener(this);
    setSize(width + 1, height + 1);
    paramFrontEnd.getContainer().addKeyListener(this);
    game.registerUI(this);
    paramDispatcher.registerUI(this);
  }
  
  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str = paramActionEvent.getActionCommand();
    int i;
    if (str.charAt(0) == '*')
    {
      i = Integer.parseInt(str.substring(1));
      dispatcher.quit(i);
    }
    else
    {
      i = Integer.parseInt(str);
      if (i == -1)
      {
        messageReceiver = (Params.NEUTRAL + 1);
        statusBar.messageMode(messageReceiver);
        mode = 4;
        setFocus();
      }
      else if (i == 0)
      {
        messageReceiver = Params.NEUTRAL;
        statusBar.messageMode(messageReceiver);
        mode = 4;
        setFocus();
      }
      else
      {
        statusBar.messageMode(i - 1);
        messageReceiver = (i - 1);
        mode = 4;
        setFocus();
      }
    }
  }
  
  public void buttonPressed(String paramString)
  {
    if (paramString.equals("(M)essage (F1)"))
    {
      messageMenu.show(buttonBar, messagePoint.x, messagePoint.y + buttonBar.buttonFontHeight);
    }
    else if (paramString.equals("(S)ound On/Off"))
    {
      frontEnd.setSoundMode(!frontEnd.getSoundMode());
      CText localCText;
      if (frontEnd.getSoundMode()) {
        localCText = new CText("<<Sound is on>>", Color.white);
      } else {
        localCText = new CText("<<Sound is off>>", Color.white);
      }
      messageBoard.addText(localCText);
      messageBoard.newLine();
    }
    else if (paramString.equals("(F)orum"))
    {
      if (watcher) {
        dispatcher.quit(0);
      } else {
        toForum(false);
      }
    }
    else if (paramString.equals("(Q)uit Game"))
    {
      if (mode == 5)
      {
        mode = 0;
        statusBar.normalMode();
      }
      else
      {
        if (mode == 4) {
          galaxy.endMessage();
        }
        if ((mode == 1) || (mode == 2)) {
          galaxy.abortOrder();
        }
        mode = 0;
        statusBar.normalMode();
        quitMenu.show(buttonBar, quitPoint.x, quitPoint.y + buttonBar.buttonFontHeight);
      }
    }
  }
  
  int getButton(MouseEvent paramMouseEvent)
  {
    if (((paramMouseEvent.getModifiers() & 0x8) != 0) || ((paramMouseEvent.getModifiers() & 0x4) != 0)) {
      return 1;
    }
    return 0;
  }
  
  int getComponent(MouseEvent paramMouseEvent)
  {
    if (paramMouseEvent.getComponent() == galaxy) {
      return 0;
    }
    if (paramMouseEvent.getComponent() == messageBoard) {
      return 1;
    }
    if (paramMouseEvent.getComponent() == statusBar) {
      return 2;
    }
    return -1;
  }
  
  public boolean isFocusTraversable()
  {
    return true;
  }
  
  public boolean isMessageChar(char paramChar)
  {
    if ((Character.isDigit(paramChar)) || (Character.isLetter(paramChar))) {
      return true;
    }
    for (int i = 0; i < Params.MESSAGE_KEYS.length; i++) {
      if (Params.MESSAGE_KEYS[i] == paramChar) {
        return true;
      }
    }
    return false;
  }
  
  public void keyPressed(KeyEvent paramKeyEvent)
  {
    int i = paramKeyEvent.getKeyCode();
    char c = paramKeyEvent.getKeyChar();
    drawMonitor.lock();
    Object localObject;
    int k;
    switch (mode)
    {
    case 3: 
      if ((i == 112) || (i == 27))
      {
        statusBar.normalMode();
        mode = 0;
      }
      else if ((i == 10) || (i == 48))
      {
        messageReceiver = Params.NEUTRAL;
        statusBar.messageMode(messageReceiver);
        mode = 4;
      }
      else if (c == '-')
      {
        messageReceiver = (Params.NEUTRAL + 1);
        statusBar.messageMode(messageReceiver);
        mode = 4;
      }
      else if (Character.isDigit(c))
      {
        int j = Character.digit(c, 10);
        if ((j > 0) && (j <= game.players))
        {
          statusBar.messageMode(j - 1);
          messageReceiver = (j - 1);
          mode = 4;
        }
      }
      else if (isMessageChar(c))
      {
        messageReceiver = (Params.NEUTRAL + 1);
        statusBar.messageMode(messageReceiver);
        mode = 4;
        galaxy.addMessageChar(c);
      }
      break;
    case 4: 
      if (i == 27)
      {
        galaxy.endMessage();
        statusBar.normalMode();
        mode = 0;
      }
      else if ((i == 10) || (i == 112))
      {
        localObject = galaxy.endMessage();
        dispatcher.sendMessage(messageReceiver, (String)localObject);
        statusBar.normalMode();
        mode = 0;
      }
      else if ((i == 8) || (i == 127))
      {
        galaxy.eraseMessageChar();
      }
      else if (isMessageChar(c))
      {
        galaxy.addMessageChar(c);
      }
      break;
    case 0: 
      if ((i == 112) || ((i == 77) && (paramKeyEvent.isAltDown())))
      {
        statusBar.messageMode();
        mode = 3;
      }
      else if ((i == 27) || ((i == 81) && (paramKeyEvent.isAltDown())))
      {
        mode = 5;
        statusBar.quitMode();
      }
      else if ((i == 70) && (paramKeyEvent.isAltDown()))
      {
        if (watcher) {
          dispatcher.quit(0);
        } else {
          toForum(false);
        }
      }
      else if ((i == 83) && (paramKeyEvent.isAltDown()))
      {
        frontEnd.setSoundMode(!frontEnd.getSoundMode());
        if (frontEnd.getSoundMode()) {
          localObject = new CText("<<Sound is on>>", Color.white);
        } else {
          localObject = new CText("<<Sound is off>>", Color.white);
        }
        messageBoard.addText((CText)localObject);
        messageBoard.newLine();
      }
      else if ((Character.isLetter(c)) || (Character.isDigit(c)))
      {
        galaxy.choosePlanet(Planet.char2num(Character.toUpperCase(c)));
        mode = 1;
        statusBar.selectMode(game.planet[galaxy.selectedPlanet]);
      }
      break;
    case 1: 
      if ((i == 27) || (i == 8) || (i == 127))
      {
        galaxy.abortOrder();
        mode = 0;
        statusBar.normalMode();
      }
      else if ((Character.isLetter(c)) || (Character.isDigit(c)))
      {
        k = Planet.char2num(Character.toUpperCase(c));
        if (k != galaxy.selectedPlanet)
        {
          mode = 2;
          galaxy.chooseTargetPlanet(k);
          statusBar.sendShipsMode(game.planet[galaxy.targetPlanet]);
        }
      }
      break;
    case 2: 
      if ((i == 27) || (i == 8) || (i == 127))
      {
        galaxy.abortOrder();
        mode = 0;
        statusBar.normalMode();
      }
      else if (Character.isDigit(c))
      {
        statusBar.addShips(Character.digit(c, 10));
      }
      else if (i == 10)
      {
        k = statusBar.getShips();
        dispatcher.dispatch(game.planet[galaxy.selectedPlanet].planetChar, game.planet[galaxy.targetPlanet].planetChar, k);
        galaxy.abortOrder();
        mode = 0;
        statusBar.normalMode();
      }
      break;
    case 5: 
      if (i == 89)
      {
        dispatcher.quit(0);
        mode = 0;
        statusBar.normalMode();
      }
      else if (i == 82)
      {
        dispatcher.quit(1);
        mode = 0;
        statusBar.normalMode();
      }
      else
      {
        dispatcher.quit(2);
        mode = 0;
        statusBar.normalMode();
      }
      break;
    }
    drawMonitor.unlock();
    paramKeyEvent.consume();
  }
  
  public void keyReleased(KeyEvent paramKeyEvent) {}
  
  public void keyTyped(KeyEvent paramKeyEvent) {}
  
  public void messageMenu()
  {
    if (mode != 4)
    {
      messageMenu.show(messageBoard, 2, messageBoard.topMargin);
    }
    else
    {
      String str = galaxy.endMessage();
      dispatcher.sendMessage(messageReceiver, str);
      statusBar.normalMode();
      mode = 0;
    }
  }
  
  public void mouseClicked(MouseEvent paramMouseEvent) {}
  
  public void mouseDragged(MouseEvent paramMouseEvent) {}
  
  public void mouseEntered(MouseEvent paramMouseEvent) {}
  
  public void mouseExited(MouseEvent paramMouseEvent) {}
  
  public void mouseMoved(MouseEvent paramMouseEvent)
  {
    if (mode == 2)
    {
      drawMonitor.lock();
      galaxy.setShipSlider(paramMouseEvent.getY());
      drawMonitor.unlock();
      statusBar.setShips(galaxy.sendShips(paramMouseEvent.getY()));
    }
  }
  
  public void mousePressed(MouseEvent paramMouseEvent)
  {
    drawMonitor.lock();
    int i = getComponent(paramMouseEvent);
    if (getButton(paramMouseEvent) == 0)
    {
      switch (mode)
      {
      case 0: 
      case 5: 
        if ((i == 0) && (galaxy.choosePlanet(paramMouseEvent.getX(), paramMouseEvent.getY())))
        {
          mode = 1;
          statusBar.selectMode(game.planet[galaxy.selectedPlanet]);
        }
        break;
      case 1: 
        if ((i == 0) && (galaxy.chooseTargetPlanet(paramMouseEvent.getX(), paramMouseEvent.getY())))
        {
          mode = 2;
          statusBar.sendShipsMode(game.planet[galaxy.targetPlanet]);
        }
        break;
      case 2: 
        int j = galaxy.sendShips(paramMouseEvent.getY());
        dispatcher.dispatch(game.planet[galaxy.selectedPlanet].planetChar, game.planet[galaxy.targetPlanet].planetChar, j);
        galaxy.abortOrder();
        mode = 0;
        statusBar.normalMode();
      }
    }
    else
    {
      galaxy.abortOrder();
      if (mode == 4) {
        galaxy.endMessage();
      }
      mode = 0;
      statusBar.normalMode();
    }
    drawMonitor.unlock();
  }
  
  public void mouseReleased(MouseEvent paramMouseEvent) {}
  
  boolean ours(Fleet paramFleet)
  {
    return paramFleet.owner == dispatcher.getMe();
  }
  
  boolean ours(Planet paramPlanet)
  {
    return paramPlanet.owner == dispatcher.getMe();
  }
  
  public void postArrival(String paramString)
  {
    frontEnd.play("arrive.au");
    Player localPlayer = game.getPlayer(paramString);
    if ((localPlayer == null) || (!localPlayer.isActive))
    {
      messageBoard.addText(new CText(paramString + " just arrived at the forum!", Color.orange));
    }
    else
    {
      messageBoard.addText(new CText(paramString + " ", localPlayer.getColor()));
      messageBoard.addText(new CText(" returns to power!", Color.lightGray));
      statusBar.repaint();
    }
    messageBoard.newLine();
  }
  
  public void postAttack(Fleet paramFleet, Planet paramPlanet)
  {
    if (ours(paramFleet)) {
      frontEnd.play("combat.au");
    } else if (ours(paramPlanet)) {
      frontEnd.play("attack.au");
    }
    CText localCText1 = new CText(paramFleet.owner.name + " ", paramFleet.owner.getColor());
    CText localCText2 = new CText("attacks ", Color.lightGray);
    CText localCText3 = new CText(new Character(paramPlanet.planetChar).toString() + " ", paramPlanet.owner.getColor());
    CText localCText4 = new CText("with ", Color.lightGray);
    CText localCText5 = new CText(new Integer(paramFleet.ships).toString() + " ", Color.white);
    CText localCText6 = new CText("ships.", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.addText(localCText5);
    messageBoard.addText(localCText6);
    messageBoard.newLine();
  }
  
  public void postBlackHole(Fleet paramFleet)
  {
    if (ours(paramFleet)) {
      frontEnd.play("nothing.au");
    }
    CText localCText1 = new CText("" + paramFleet.ships, paramFleet.owner.getColor());
    CText localCText2 = new CText(" ships fly in to the black hole at ", Color.lightGray);
    CText localCText3 = new CText("" + paramFleet.destination.planetChar, paramFleet.destination.owner.getColor());
    CText localCText4 = new CText(".", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.newLine();
  }
  
  public void postError(String paramString) {}
  
  public void postForumEvent(String paramString)
  {
    messageBoard.addText(new CText(paramString, Color.orange));
    messageBoard.newLine();
  }
  
  public void postForumMessage(String paramString1, Player paramPlayer, String paramString2)
  {
    frontEnd.play("message.au");
    CText localCText1 = new CText(paramString1, Color.white);
    CText localCText2 = new CText("->", Color.lightGray);
    CText localCText3;
    if (paramPlayer != Player.NEUTRAL) {
      localCText3 = new CText(paramPlayer.name, paramPlayer.getColor());
    } else {
      localCText3 = new CText("GAME", Color.white);
    }
    CText localCText4 = new CText(": ", Color.lightGray);
    CText localCText5 = new CText(paramString2, Color.white);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.addText(localCText5);
    messageBoard.newLine();
  }
  
  public void postForumMessage(String paramString1, String paramString2)
  {
    frontEnd.play("message.au");
    messageBoard.addText(new CText(paramString1, Color.white));
    messageBoard.addText(new CText(": ", Color.lightGray));
    messageBoard.addText(new CText(paramString2, Color.white));
    messageBoard.newLine();
  }
  
  public void postGameEnd(int paramInt) {}
  
  public void postGameStart(GameInstance paramGameInstance)
  {
    frontEnd.play("soundon.au");
  }
  
  public void postInvasion(Fleet paramFleet, Planet paramPlanet)
  {
    if (ours(paramFleet)) {
      frontEnd.play("victory.au");
    } else if (ours(paramPlanet)) {
      frontEnd.play("invaded.au");
    }
    CText localCText1 = new CText(paramFleet.owner.name + " ", paramFleet.owner.getColor());
    CText localCText2 = new CText("invades ", Color.lightGray);
    CText localCText3 = new CText(new Character(paramPlanet.planetChar).toString(), paramPlanet.owner.getColor());
    CText localCText4 = new CText(".", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.newLine();
  }
  
  public void postMessage(Player paramPlayer1, Player paramPlayer2, String paramString)
  {
    frontEnd.play("message.au");
    int i = (paramPlayer2 == paramPlayer1) && (paramPlayer1 == dispatcher.getMe()) ? 1 : 0;
    CText localCText1;
    CText localCText2;
    CText localCText3;
    if (i == 0)
    {
      localCText1 = new CText(paramPlayer1.name, paramPlayer1.getColor());
      localCText2 = new CText("->", Color.lightGray);
      if (paramPlayer2 != Player.NEUTRAL) {
        localCText3 = new CText(paramPlayer2.name, paramPlayer2.getColor());
      } else {
        localCText3 = new CText("GAME", Color.white);
      }
    }
    else
    {
      localCText1 = new CText("Note to self", paramPlayer1.getColor());
      localCText2 = new CText("", Color.white);
      localCText3 = new CText("", Color.white);
    }
    CText localCText4 = new CText(": ", Color.lightGray);
    CText localCText5 = new CText(paramString, Color.white);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.addText(localCText5);
    messageBoard.newLine();
  }
  
  public void postNextTurn()
  {
    statusBar.nextTurn();
    drawMonitor.lock();
    galaxy.repaintXORs();
    for (int i = 0; i < 36; i++) {
      if (game.dirty[i] != false)
      {
        redrawPlanet(i);
        statusBar.planetChanged(i);
      }
    }
    galaxy.repaintXORs();
    galaxy.drawFleets();
    drawMonitor.unlock();
    game.dirty = new boolean[36];
  }
  
  public void postPlanetMove(int paramInt1, int paramInt2, Planet paramPlanet)
  {
    drawMonitor.lock();
    galaxy.repaintXORs();
    galaxy.movePlanet(paramInt1, paramInt2, paramPlanet);
    galaxy.repaintXORs();
    drawMonitor.unlock();
    statusBar.planetMoved(paramPlanet);
  }
  
  public void postPlayerLeft(String paramString, int paramInt)
  {
    if (paramInt >= 0)
    {
      messageBoard.addText(new CText(paramString + " ", Params.PLAYERCOLOR[paramInt]));
      messageBoard.addText(new CText(" has gone missing!", Color.lightGray));
      statusBar.repaint();
    }
    else
    {
      messageBoard.addText(new CText(paramString + " called it a day!", Color.orange));
    }
    messageBoard.newLine();
  }
  
  public void postPlayerQuit(Player paramPlayer)
  {
    frontEnd.play("quit.au");
    CText localCText1 = new CText(paramPlayer.name + " ", paramPlayer.getColor());
    CText localCText2 = new CText("abdicates!", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.newLine();
    statusBar.repaint();
  }
  
  public void postPlayerQuit(Player paramPlayer, int paramInt)
  {
    CText localCText1;
    CText localCText2;
    if (paramInt == 0)
    {
      frontEnd.play("quit.au");
      localCText1 = new CText(paramPlayer.name + " ", paramPlayer.getColor());
      localCText2 = new CText("abdicates!", Color.lightGray);
      messageBoard.addText(localCText1);
      messageBoard.addText(localCText2);
      messageBoard.newLine();
    }
    else if (paramInt == 1)
    {
      localCText1 = new CText(paramPlayer.name + " ", paramPlayer.getColor());
      localCText2 = new CText("is ready to quit.", Color.lightGray);
      messageBoard.addText(localCText1);
      messageBoard.addText(localCText2);
      messageBoard.newLine();
    }
    statusBar.repaint();
  }
  
  public void postRedrawGalaxy()
  {
    redrawAll();
  }
  
  public void postReinforcements(int paramInt, Planet paramPlanet)
  {
    if (ours(paramPlanet)) {
      frontEnd.play("reinforce.au");
    }
    CText localCText1 = new CText(new Integer(paramInt).toString() + " ", Color.white);
    CText localCText2 = new CText("reinforcements arrive at ", Color.lightGray);
    CText localCText3 = new CText(new Character(paramPlanet.planetChar).toString(), paramPlanet.owner.getColor());
    CText localCText4 = new CText(".", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.newLine();
  }
  
  public void postRepulsion(Player paramPlayer, Planet paramPlanet)
  {
    if (dispatcher.getMe() == paramPlayer) {
      frontEnd.play("lose.au");
    } else if (ours(paramPlanet)) {
      frontEnd.play("repelled.au");
    }
    CText localCText1 = new CText(paramPlayer.name + " ", paramPlayer.getColor());
    CText localCText2 = new CText("was repelled from ", Color.lightGray);
    CText localCText3 = new CText(new Character(paramPlanet.planetChar).toString(), paramPlanet.owner.getColor());
    CText localCText4 = new CText(".", Color.lightGray);
    messageBoard.addText(localCText1);
    messageBoard.addText(localCText2);
    messageBoard.addText(localCText3);
    messageBoard.addText(localCText4);
    messageBoard.newLine();
  }
  
  public void postSpecial(String[] paramArrayOfString, Color[] paramArrayOfColor)
  {
    frontEnd.play("event.au");
    for (int i = 0; i < paramArrayOfString.length; i++) {
      messageBoard.addText(new CText(paramArrayOfString[i], paramArrayOfColor[i]));
    }
    messageBoard.newLine();
  }
  
  public void redrawAll()
  {
    galaxy.repaint();
    statusBar.repaint();
  }
  
  public void redrawPlanet(int paramInt)
  {
    Planet localPlanet = game.planet[paramInt];
    galaxy.redrawPlanet(paramInt);
    if (localPlanet.y == 0) {
      galaxy.paintMessage();
    }
  }
  
  public void setFocus()
  {
    galaxy.requestFocus();
  }
  
  public void toForum(boolean paramBoolean)
  {
    forum.displayForum(paramBoolean);
  }
}