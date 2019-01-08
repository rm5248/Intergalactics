package igx.client;

// ClientUI.java 

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import igx.shared.*;

public class ClientUI extends Panel implements UI, MouseListener, MouseMotionListener, KeyListener, ActionListener, ButtonListener
{
  //// Constants
  // Ratio from font size to screen height
  final static int GALAXY_FONT_RATIO  = 60;
  final static int MESSAGE_FONT_RATIO = 54;
  final static int STATUS_FONT_RATIO  = 42;
  // UI Modes
  final static int MODE_NORMAL     = 0;
  final static int MODE_SELECT     = 1;
  final static int MODE_SEND_SHIPS = 2;
  final static int MODE_MESSAGE_TO = 3;
  final static int MODE_MESSAGE    = 4;
  final static int MODE_QUIT       = 5;
  // UI Components
  final static int COMPONENT_GALAXY       = 0;
  final static int COMPONENT_MESSAGEBOARD = 1;
  final static int COMPONENT_STATUSBAR    = 2;
  // Mouse Buttons
  final static int BUTTON_ACTION = 0;
  final static int BUTTON_CANCEL = 1;
  // Screen Buttons
  final static String MESSAGE_ACTION = "MESSAGE";
  final static String QUIT_ACTION    = "QUIT";

  //// Data fields
  // Width and hieght of UI
  int width, height;
  // Font size
  int fontSize;
  // Galaxy size
  int galaxySize;
  // Sidebar width
  int sideWidth;
  // Sidebar panel
  Panel sidebar;
  // Status Bar
  StatusBar statusBar;
  // Buton Bar
  ButtonCanvas buttonBar;
  // Message board
  ScrollText messageBoard;
  // Galaxy
  Galaxy galaxy;
  // Dispatcher
  Dispatcher dispatcher;
  // Game
  GameInstance game;
  // UI mode
  int mode = MODE_NORMAL;
  int messageReceiver;
  // Popup Message Menu
  PopupMenu messageMenu;
  // Quit Menu
  PopupMenu quitMenu;
  // Message and quit button locations
  Point messagePoint, quitPoint;
  // Front End
  FrontEnd frontEnd;
  ClientForum forum;
  Monitor drawMonitor = new Monitor();
  boolean watcher = false;
  //// Methods
  // Constructor
  public ClientUI (Dispatcher dispatcher, FrontEnd frontEnd, ClientForum forum, boolean watcher) {
    super(new BorderLayout());
    this.frontEnd = frontEnd;
    this.dispatcher = dispatcher;
    this.game = dispatcher.Game;
    this.forum = forum;
    this.watcher = watcher;
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    Dimension dim = frontEnd.getDimensions();
    this.width = dim.width;
    this.height = dim.height;
    fontSize = height / GALAXY_FONT_RATIO;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    // New font sizer:
    galaxySize = (height / Params.MAPX) * Params.MAPX;
    fontSize = FontFinder.getFont(toolkit, "SansSerif", Params.MAPY * 3, galaxySize).getSize() + 2;
    galaxy = new Galaxy(game, galaxySize, fontSize, toolkit, drawMonitor);
    galaxy.setMe(dispatcher.getMe());
    sidebar = new Panel(new BorderLayout());
    sidebar.setBackground(Color.black);
    add(galaxy, BorderLayout.WEST);
    int statusFontSize = FontFinder.getFont(toolkit, "SansSerif", 5, 13*height/100).getSize(); 
    if (watcher)
      statusBar = new StatusBar(game, statusFontSize, toolkit, width - height - height / Galaxy.SCROLLBAR_WIDTH_RATIO, -1, forum.clientName);
    else
      statusBar = new StatusBar(game, height / STATUS_FONT_RATIO, toolkit, width - height - height / Galaxy.SCROLLBAR_WIDTH_RATIO, dispatcher.getMe().number, forum.clientName);
    buttonBar = new ButtonCanvas(height / MESSAGE_FONT_RATIO, toolkit, width - height - height / Galaxy.SCROLLBAR_WIDTH_RATIO, 0);
    buttonBar.height = ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1) * 2;
    buttonBar.setButtonListener(this);
    // Set up buttons
    int halfWidth = buttonBar.width / 2;
    // Message
    String buttonText = "(M)essage (F1)";
    Dimension d = buttonBar.buttonDimensions(buttonText);
    Point p = new Point((halfWidth - d.width) / 2, 0 * ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(p.x, p.y, buttonText);
    messagePoint = p;
    // Sound On/Off
    buttonText = "(S)ound On/Off";
    d = buttonBar.buttonDimensions(buttonText);
    p = new Point((halfWidth - d.width) / 2 + halfWidth, 0 * ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(p.x, p.y, buttonText);
    // Forum
    buttonText = "(F)orum";
    d = buttonBar.buttonDimensions(buttonText);
    if (watcher)
      p = new Point((buttonBar.width - d.width) / 2, 1 * ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
    else
      p = new Point((halfWidth - d.width) / 2, 1 * ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
  buttonBar.addButton(p.x, p.y, buttonText);
  // Quit
  // Not needed if we are a watcher
  if (!watcher) {
    buttonText = "(Q)uit Game";
    d = buttonBar.buttonDimensions(buttonText);
    p = new Point((halfWidth - d.width) / 2 + halfWidth, 1 * ((10 + ClientForum.BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(p.x, p.y, buttonText);
  }
  quitPoint = new Point(p.x + d.width, p.y);     
  // Prepare buttons
  buttonBar.prepareButtons();
  messageBoard = new ScrollText(fontSize/*height / MESSAGE_FONT_RATIO*/, toolkit, width - height - height / Galaxy.SCROLLBAR_WIDTH_RATIO, height - statusBar.height - buttonBar.height);
  sidebar.add(statusBar, BorderLayout.NORTH);
  sidebar.add(buttonBar, BorderLayout.CENTER);
  sidebar.add(messageBoard, BorderLayout.SOUTH);
  add(sidebar, BorderLayout.EAST);
  galaxy.setSize(galaxySize + height/Galaxy.SCROLLBAR_WIDTH_RATIO, height);
  statusBar.setSize(statusBar.width, statusBar.height);
  messageBoard.setSize(width - height - height/Galaxy.SCROLLBAR_WIDTH_RATIO, height - statusBar.height);
  sidebar.setSize(width - height - height/Galaxy.SCROLLBAR_WIDTH_RATIO, height);
  // Set up quit popup-menu
  quitMenu = new PopupMenu();
  buttonBar.add(quitMenu);
  MenuItem quitItem = new MenuItem("Don't Quit");
  quitItem.setActionCommand("*" + Params.DONT_SIGNAL);
  quitItem.addActionListener(this);
  quitMenu.add(quitItem);
  //
  if (!watcher) {
    quitItem = new MenuItem("Ready to Quit");
    quitItem.setActionCommand("*" + Params.READY_SIGNAL);
    quitItem.addActionListener(this);
    quitMenu.add(quitItem);
  }
  //
  quitItem = new MenuItem("Quit");
  quitItem.setActionCommand("*" + Params.QUIT_SIGNAL);
  quitItem.addActionListener(this);
  quitMenu.add(quitItem);	// Set up message popup-menu
	
  messageMenu = new PopupMenu();
  buttonBar.add(messageMenu);
  MenuItem allItem = new MenuItem("This Game");
  allItem.setActionCommand("0");
  allItem.addActionListener(this);
  messageMenu.add(allItem);
  messageMenu.add("-");
  MenuItem forumItem = new MenuItem("Forum");
  forumItem.setActionCommand("-1");
  forumItem.addActionListener(this);
  messageMenu.add(forumItem);
  messageMenu.add("-");
  MenuItem playerItem;
  for (int i = 0; i < game.players; i++) {
    playerItem = new MenuItem(game.player[i].name);
    playerItem.setActionCommand(new Integer(i + 1).toString());
    playerItem.addActionListener(this);
    messageMenu.add(playerItem);
  }
  // Add listeners
  galaxy.addMouseListener(this);
  galaxy.addMouseMotionListener(this);
  galaxy.addKeyListener(this);
  messageBoard.addKeyListener(this);
  statusBar.addKeyListener(this);
  buttonBar.addKeyListener(this);
  sidebar.addKeyListener(this);
  this.addKeyListener(this);
  // messageBoard.addMouseListener(this);
  // messageBoard.addMouseMotionListener(this);
  setSize(width+1, height+1);
  // Key listeners
  frontEnd.getContainer().addKeyListener(this);
  game.registerUI(this);
  dispatcher.registerUI(this);
}  
//  public void quit () {
// LOCK
//  drawMonitor.lock();
//  frontEnd.play(Params.AU_YOU_QUIT);
//  frontEnd.quitGame();
// UNLOCK
//   drawMonitor.unlock();
// }
// For messages
public void actionPerformed(ActionEvent e) {
  String command = e.getActionCommand();
  if (command.charAt(0) == '*') {
    // Quit command
    int what = Integer.parseInt(command.substring(1));
    dispatcher.quit(what);
  } else {
    int who = Integer.parseInt(command);
    if (who == -1) {
      messageReceiver = Player.NEUTRAL.number + 1;
      statusBar.messageMode(messageReceiver);
      mode = MODE_MESSAGE;
      setFocus();
    } else
      if (who == 0) {
	messageReceiver = Player.NEUTRAL.number;
	statusBar.messageMode(messageReceiver);
	mode = MODE_MESSAGE;
	setFocus();
      } else {
	statusBar.messageMode(who - 1);
	messageReceiver = who - 1;
	mode = MODE_MESSAGE;
	setFocus();
      }
  }
}
/**
 * Button pressed.
 * @param text java.lang.String
 */
public void buttonPressed(String text) {
  if (text.equals("(M)essage (F1)")) {
    messageMenu.show(buttonBar, messagePoint.x, messagePoint.y + buttonBar.buttonFontHeight);
  } else
    if (text.equals("(S)ound On/Off")) {
      frontEnd.setSoundMode(!frontEnd.getSoundMode());
      CText ctext;
      if (frontEnd.getSoundMode())
	ctext = new CText("<<Sound is on>>", Color.white);
      else
	ctext = new CText("<<Sound is off>>", Color.white);
      messageBoard.addText(ctext);
      messageBoard.newLine();
    } else
      if (text.equals("(F)orum")) {
	if (watcher)
	  dispatcher.quit(Params.QUIT_SIGNAL);
	else
	  toForum(false);
      } else
	if (text.equals("(Q)uit Game")) {
	  if (mode == MODE_QUIT) {
	    mode = MODE_NORMAL;
	    statusBar.normalMode();
	  } else {
	    if (mode == MODE_MESSAGE)
	      galaxy.endMessage();
	    if ((mode == MODE_SELECT) || (mode == MODE_SEND_SHIPS))
	      galaxy.abortOrder();
	    mode = MODE_NORMAL;
	    statusBar.normalMode();
	    quitMenu.show(buttonBar, quitPoint.x, quitPoint.y + buttonBar.buttonFontHeight);
	  }
	}
}

int getButton(MouseEvent e) {
  // System.out.println("Modifiers: " + e.getModifiers());
  if (((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0) || ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0))
    return BUTTON_CANCEL;
  else
    return BUTTON_ACTION;
}
//// Mouse events

int getComponent (MouseEvent e) {
  if (e.getComponent() == galaxy) {
    // System.out.println("Gal");
    return COMPONENT_GALAXY;
  } else if (e.getComponent() == messageBoard) {
    // System.out.println("MessageBoard");
    return COMPONENT_MESSAGEBOARD;
  } else if (e.getComponent() == statusBar) {
    // System.out.println("Status");
    return COMPONENT_STATUSBAR;
  } else {
    // System.out.println("Neither:" + e.getComponent());
    return -1;
  }
}  
/**
 * Make it traversable
 * @return boolean
 */
public boolean isFocusTraversable() {
  return true;
}
public boolean isMessageChar(char key) {
  if (Character.isDigit(key) || Character.isLetter(key))
    return true;
  for (int i = 0; i < Params.MESSAGE_KEYS.length; i++)
    if (Params.MESSAGE_KEYS[i] == key)
      return true;
  return false;
}  
public void keyPressed(KeyEvent e) {
  int code = e.getKeyCode();
  char key = e.getKeyChar();
  // LOCK
  drawMonitor.lock();
  switch (mode) {
  case MODE_MESSAGE_TO :
    if ((code == KeyEvent.VK_F1) || (code == KeyEvent.VK_ESCAPE)) {
      statusBar.normalMode();
      mode = MODE_NORMAL;
    } else
      if ((code == KeyEvent.VK_ENTER) || (code == KeyEvent.VK_0)) {
	messageReceiver = Player.NEUTRAL.number;
	statusBar.messageMode(messageReceiver);
	mode = MODE_MESSAGE;
      } else
	if (key == '-') {
	  messageReceiver = Player.NEUTRAL.number + 1;
	  statusBar.messageMode(messageReceiver);
	  mode = MODE_MESSAGE;
	} else
	  if (Character.isDigit(key)) {
	    int playerNum = Character.digit(key, 10);
	    if ((playerNum > 0) && (playerNum <= game.players)) {
	      statusBar.messageMode(playerNum - 1);
	      messageReceiver = playerNum - 1;
	      mode = MODE_MESSAGE;
	    }
	  } else
	    if (isMessageChar(key)) {
	      messageReceiver = Player.NEUTRAL.number + 1;
	      statusBar.messageMode(messageReceiver);
	      mode = MODE_MESSAGE;
	      galaxy.addMessageChar(key);
	    }
    break;
  case MODE_MESSAGE :
    if (code == KeyEvent.VK_ESCAPE) {
      galaxy.endMessage();
      statusBar.normalMode();
      mode = MODE_NORMAL;
    } else
      if ((code == KeyEvent.VK_ENTER) || (code == KeyEvent.VK_F1)) {
	String message = galaxy.endMessage();
	dispatcher.sendMessage(messageReceiver, message);
	statusBar.normalMode();
	mode = MODE_NORMAL;
      } else
	if ((code == KeyEvent.VK_BACK_SPACE) || (code == KeyEvent.VK_DELETE)) {
	  galaxy.eraseMessageChar();
	} else
	  if (isMessageChar(key)) {
	    galaxy.addMessageChar(key);
	  }
    break;
  case MODE_NORMAL :
    if ((code == KeyEvent.VK_F1) || ((code == KeyEvent.VK_M) && e.isAltDown())) {
      statusBar.messageMode();
      mode = MODE_MESSAGE_TO;
    } else
      if ((code == KeyEvent.VK_ESCAPE) || ((code == KeyEvent.VK_Q) && e.isAltDown())) {
	mode = MODE_QUIT;
	statusBar.quitMode();
      } else
	if ((code == KeyEvent.VK_F) && e.isAltDown()) {
	  if (watcher)
	    dispatcher.quit(Params.QUIT_SIGNAL);
	  else	    
	    toForum(false);
	} else
	  if ((code == KeyEvent.VK_S) && e.isAltDown()) {
	    frontEnd.setSoundMode(!frontEnd.getSoundMode());
	    CText text;
	    if (frontEnd.getSoundMode())
	      text = new CText("<<Sound is on>>", Color.white);
	    else
	      text = new CText("<<Sound is off>>", Color.white);
	    messageBoard.addText(text);
	    messageBoard.newLine();
	  } else
	    if (Character.isLetter(key) || Character.isDigit(key)) {
	      galaxy.choosePlanet(Planet.char2num(Character.toUpperCase(key)));
	      mode = MODE_SELECT;
	      statusBar.selectMode(game.planet[galaxy.selectedPlanet]);
	    }
    break;
  case MODE_SELECT :
    if ((code == KeyEvent.VK_ESCAPE) || (code == KeyEvent.VK_BACK_SPACE) || (code == KeyEvent.VK_DELETE)) {
      galaxy.abortOrder();
      mode = MODE_NORMAL;
      statusBar.normalMode();
    } else
      if (Character.isLetter(key) || Character.isDigit(key)) {
	int target = Planet.char2num(Character.toUpperCase(key));
	if (target != galaxy.selectedPlanet) {
	  mode = MODE_SEND_SHIPS;
	  galaxy.chooseTargetPlanet(target);
	  statusBar.sendShipsMode(game.planet[galaxy.targetPlanet]);
	}
      }
    break;
  case MODE_SEND_SHIPS :
    if ((code == KeyEvent.VK_ESCAPE) || (code == KeyEvent.VK_BACK_SPACE) || (code == KeyEvent.VK_DELETE)) {
      galaxy.abortOrder();
      mode = MODE_NORMAL;
      statusBar.normalMode();
    } else
      if (Character.isDigit(key)) {
	statusBar.addShips(Character.digit(key, 10));
      } else
	if (code == KeyEvent.VK_ENTER) {
	  int numShips = statusBar.getShips();
	  dispatcher.dispatch(game.planet[galaxy.selectedPlanet].planetChar, game.planet[galaxy.targetPlanet].planetChar, numShips);
	  galaxy.abortOrder();
	  mode = MODE_NORMAL;
	  statusBar.normalMode();
	}
    break;
  case MODE_QUIT :
    if (code == KeyEvent.VK_Y) {
      dispatcher.quit(Params.QUIT_SIGNAL);
      mode = MODE_NORMAL;
      statusBar.normalMode();
    } else if (code == KeyEvent.VK_R) {
      dispatcher.quit(Params.READY_SIGNAL);
      mode = MODE_NORMAL;
      statusBar.normalMode();
    } else {
      dispatcher.quit(Params.DONT_SIGNAL);
      mode = MODE_NORMAL;
      statusBar.normalMode();
    }
    break;
  }
  // UNLOCK
  drawMonitor.unlock();
  e.consume();
}
//// Key Events
public void keyReleased (KeyEvent e) {
  // Who cares
}  
public void keyTyped (KeyEvent e) {
  // Not interested
}  
public void messageMenu () {
  if (mode != MODE_MESSAGE) {
    messageMenu.show(messageBoard, 2, messageBoard.topMargin);
  } else {
    String message = galaxy.endMessage();
    dispatcher.sendMessage(messageReceiver, message);
    statusBar.normalMode();
    mode = MODE_NORMAL;
  }
}  
public void mouseClicked (MouseEvent e) {
  // Ignore
}  
public void mouseDragged (MouseEvent e) {
  // Ignore
}  
public void mouseEntered (MouseEvent e) {
  // Ignore
}  
public void mouseExited (MouseEvent e) {
  // Ignore
}  
public void mouseMoved (MouseEvent e) {
  if (mode == MODE_SEND_SHIPS) {
    // LOCK
    drawMonitor.lock();
    galaxy.setShipSlider(e.getY());
    // UNLOCK
    drawMonitor.unlock();
    statusBar.setShips(galaxy.sendShips(e.getY()));
  }
}  
public void mousePressed (MouseEvent e) {
  // LOCK
  drawMonitor.lock();
  int component = getComponent(e);
  if (getButton(e) == BUTTON_ACTION) {
    switch (mode) {
    case MODE_NORMAL : case MODE_QUIT :
      if (component == COMPONENT_GALAXY) {
	if (galaxy.choosePlanet(e.getX(), e.getY())) {
	  mode = MODE_SELECT;
	  statusBar.selectMode(game.planet[galaxy.selectedPlanet]);
	}
      }
      break;
    case MODE_SELECT :
      if (component == COMPONENT_GALAXY) {
	if (galaxy.chooseTargetPlanet(e.getX(), e.getY())) {
	  mode = MODE_SEND_SHIPS;
	  statusBar.sendShipsMode(game.planet[galaxy.targetPlanet]);
	}
      }
      break;
    case MODE_SEND_SHIPS :
      int numShips = galaxy.sendShips(e.getY());
      dispatcher.dispatch(game.planet[galaxy.selectedPlanet].planetChar,
			  game.planet[galaxy.targetPlanet].planetChar,
			  numShips);
      galaxy.abortOrder();
      mode = MODE_NORMAL;
      statusBar.normalMode();
      break;
    }
  } else { // Abort button
    galaxy.abortOrder();
    if (mode == MODE_MESSAGE)
      galaxy.endMessage();
    mode = MODE_NORMAL;
    statusBar.normalMode();
  }	
  // UNLOCK
  drawMonitor.unlock();
}  
public void mouseReleased (MouseEvent e) {
  // Ignore
}  
// Handy functions
boolean ours (Fleet f) {
  if (f.owner == dispatcher.getMe())
    return true;
  else
    return false;
}  
boolean ours (Planet p) {
  if (p.owner == dispatcher.getMe())
    return true;
  else
    return false;
}  
// Player arrival
public void postArrival (String name) {
  frontEnd.play(Params.AU_ARRIVED);
  Player p = game.getPlayer(name);
  if ((p == null) || (!p.isActive))
    messageBoard.addText(new CText(name + " just arrived at the forum!", Color.orange));
  else {
    messageBoard.addText(new CText(name + " ", p.getColor()));
    messageBoard.addText(new CText(" returns to power!", Color.lightGray));
    statusBar.repaint();
  }
  messageBoard.newLine();
}  
// Attack
public void postAttack (Fleet fleet, Planet planet) {
  if (ours(fleet))
    frontEnd.play(Params.AU_YOU_ATTACK);
  else if (ours(planet))
    frontEnd.play(Params.AU_ATTACK_YOU);
  CText text1 = new CText(fleet.owner.name + " ", fleet.owner.getColor());
  CText text2 = new CText("attacks ", Color.lightGray);
  CText text3 = new CText(new Character(planet.planetChar).toString() + " ", planet.owner.getColor());
  CText text4 = new CText("with ", Color.lightGray);
  CText text5 = new CText(new Integer(fleet.ships).toString() + " ", Color.white);
  CText text6 = new CText("ships.", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.addText(text5);
  messageBoard.addText(text6);
  messageBoard.newLine();
}  
public void postBlackHole (Fleet fleet) {
  if (ours(fleet))
    frontEnd.play(Params.AU_BLACKHOLE);
  CText text1 = new CText("" + fleet.ships, fleet.owner.getColor());
  CText text2 = new CText(" ships fly in to the black hole at ", Color.lightGray);
  CText text3 = new CText("" + fleet.destination.planetChar, fleet.destination.owner.getColor());
  CText text4 = new CText(".", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.newLine();    
}  
// Error
public void postError (String errorMessage) {
}  
// Player arrival
public void postForumEvent (String text) {
  messageBoard.addText(new CText(text, Color.orange));
  messageBoard.newLine();
}  
public void postForumMessage (String sender, Player receiver, String message) {
  frontEnd.play(Params.AU_MESSAGE);
  CText text1 = new CText(sender, Color.white);
  CText text2 = new CText("->", Color.lightGray);
  CText text3;
  if (receiver != Player.NEUTRAL)
    text3 = new CText(receiver.name, receiver.getColor());
  else
    text3 = new CText("GAME", Color.white);
  CText text4 = new CText(": ", Color.lightGray);
  CText text5 = new CText(message, Color.white);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.addText(text5);
  messageBoard.newLine();
}  
public void postForumMessage (String sender, String message) {
  frontEnd.play(Params.AU_MESSAGE);
  messageBoard.addText(new CText(sender, Color.white));
  messageBoard.addText(new CText(": ", Color.lightGray));
  messageBoard.addText(new CText(message, Color.white));
  messageBoard.newLine();
}  
// Game end
public void postGameEnd (int winnerNumber) {
}  
// Game start
public void postGameStart (GameInstance game) {
  frontEnd.play(Params.AU_SOUNDON);
}  
// Invasion
public void postInvasion (Fleet fleet, Planet planet) {
  if (ours(fleet))
    frontEnd.play(Params.AU_INVADE);
  else if (ours(planet))
    frontEnd.play(Params.AU_YOU_INVADED);
  CText text1 = new CText(fleet.owner.name + " ", fleet.owner.getColor());
  CText text2 = new CText("invades ", Color.lightGray);
  CText text3 = new CText(new Character(planet.planetChar).toString(), planet.owner.getColor());
  CText text4 = new CText(".", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.newLine();
}  
// Players sends message
public void postMessage (Player sender, Player receiver, String message) {
  frontEnd.play(Params.AU_MESSAGE);
  CText text1, text2, text3;
  boolean toSelf = ((receiver == sender) && (sender == dispatcher.getMe()));
  if (!toSelf) {		
    text1 = new CText(sender.name, sender.getColor());
    text2 = new CText("->", Color.lightGray);
    if (receiver != Player.NEUTRAL)
      text3 = new CText(receiver.name, receiver.getColor());
    else
      text3 = new CText("GAME", Color.white);
  } else {
    text1 = new CText("Note to self", sender.getColor());
    text2 = new CText("", Color.white);
    text3 = new CText("", Color.white);
  }
  CText text4 = new CText(": ", Color.lightGray);
  CText text5 = new CText(message, Color.white);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.addText(text5);
  messageBoard.newLine();
}  
// Next turn
public void postNextTurn () {
  statusBar.nextTurn();
  // LOCK
  drawMonitor.lock();
  galaxy.repaintXORs();
  for (int i = 0; i < Params.PLANETS; i++) 
    if (game.dirty[i]) {
      redrawPlanet(i);
      statusBar.planetChanged(i);
    }
  galaxy.repaintXORs();
  galaxy.drawFleets();
  // UNLOCK
  drawMonitor.unlock();
  game.dirty = new boolean[Params.PLANETS];
}  
// Planet moves
public void postPlanetMove (int oldX, int oldY, Planet planet) {
  // LOCK
  drawMonitor.lock();
  galaxy.repaintXORs();
  galaxy.movePlanet(oldX, oldY, planet);
  galaxy.repaintXORs();
  // UNLOCK
  drawMonitor.unlock();
  statusBar.planetMoved(planet);
}  
// Player leaves
public void postPlayerLeft (String name, int num) {
  // frontEnd.play(Params.AU_PLAYER_LEFT);
  if (num >= 0) {
    messageBoard.addText(new CText(name + " ", Params.PLAYERCOLOR[num]));
    messageBoard.addText(new CText(" has gone missing!", Color.lightGray));
    statusBar.repaint();
  } else {
    messageBoard.addText(new CText(name + " called it a day!", Color.orange));
  }
  messageBoard.newLine();
}  
// Player quits
public void postPlayerQuit(Player player) {
  frontEnd.play(Params.AU_QUIT);
  CText text1 = new CText(player.name + " ", player.getColor());
  CText text2 = new CText("abdicates!", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.newLine();
  statusBar.repaint();
}
// Player quits
public void postPlayerQuit(Player player, int status) {
  if (status == Params.QUIT_SIGNAL) {
    frontEnd.play(Params.AU_QUIT);
    CText text1 = new CText(player.name + " ", player.getColor());
    CText text2 = new CText("abdicates!", Color.lightGray);
    messageBoard.addText(text1);
    messageBoard.addText(text2);
    messageBoard.newLine();
  } else if (status == Params.READY_SIGNAL) {
    CText text1 = new CText(player.name + " ", player.getColor());
    CText text2 = new CText("is ready to quit.", Color.lightGray);
    messageBoard.addText(text1);
    messageBoard.addText(text2);
    messageBoard.newLine();
  }
  statusBar.repaint();
}
//// Post game events
// Repaint galaxy
public void postRedrawGalaxy () {
  redrawAll();    
}  
// Reinforcements
public void postReinforcements (int numberOfShips, Planet planet) {
  if (ours(planet))
    frontEnd.play(Params.AU_REINFORCE);
  CText text1 = new CText(new Integer(numberOfShips).toString() + " ", Color.white);
  CText text2 = new CText("reinforcements arrive at ", Color.lightGray);
  CText text3 = new CText(new Character(planet.planetChar).toString(), planet.owner.getColor());
  CText text4 = new CText(".", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.newLine();
}  
// Repulsion
public void postRepulsion (Player attacker, Planet planet) {
  if (dispatcher.getMe() == attacker)
    frontEnd.play(Params.AU_YOU_LOSE);
  else if (ours(planet))
    frontEnd.play(Params.AU_REPELLED);
  CText text1 = new CText(attacker.name + " ", attacker.getColor());
  CText text2 = new CText("was repelled from ", Color.lightGray);
  CText text3 = new CText(new Character(planet.planetChar).toString(), planet.owner.getColor());
  CText text4 = new CText(".", Color.lightGray);
  messageBoard.addText(text1);
  messageBoard.addText(text2);
  messageBoard.addText(text3);
  messageBoard.addText(text4);
  messageBoard.newLine();
}  
// Special Event
public void postSpecial (String[] text, Color[] color) {
  frontEnd.play(Params.AU_EVENT);
  for (int i = 0; i < text.length; i++)
    messageBoard.addText(new CText(text[i], color[i]));
  messageBoard.newLine();    
}  
// Redraw all planets
public void redrawAll () {
  galaxy.repaint();
  statusBar.repaint();
}  
// Redraw a planet
public void redrawPlanet (int planetNum) {
  Planet planet = game.planet[planetNum];
  galaxy.redrawPlanet(planetNum);
  if (planet.y == 0)
    galaxy.paintMessage();
}  
public void setFocus () {
  // Hack for MS VMs
  galaxy.requestFocus();
}  
/**
 * This method was created in VisualAge.
 */
public void toForum(boolean quitGame) {
  forum.displayForum(quitGame);
}
}
