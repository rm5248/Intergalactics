package igx.client;

import igx.shared.Forum;
import igx.shared.Game;
import igx.shared.GameInstance;
import igx.shared.Params;
import igx.shared.Player;
import igx.shared.Robot;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientForum
  extends Forum
  implements KeyListener, ButtonListener, ActionListener
{
  public static final int FONT_RATIO = 49;
  public static final int FONT_PERCENT = 50;
  public static final int STATUS_ROWS = 3;
  public static final int MAX_BUTTONS = 7;
  public static final int BUTTON_SPACING = 4;
  public static final Color BORDER_COLOUR = Color.GRAY;
  public static final Color BULLETIN_COLOUR = Color.orange;
  public static final Color STATUS_COLOUR = Color.white;
  public static final Color ACTIVE_PLAYER_COLOUR = Color.lightGray;
  public static final Color IDLE_PLAYER_COLOUR = Color.white;
  public static final Color BOLD_COLOUR = Color.white;
  public static final Color PLAIN_COLOUR = new Color(96, 96, 255);
  public static final Color MESSAGE_AUTHOR_COLOUR = Color.white;
  public static final Color MESSAGE_TEXT_COLOUR = Color.lightGray;
  public static final int DIALOG_NONE = -1;
  public static final int DIALOG_ALIAS = 0;
  public static final int DIALOG_NEW_PASSWORD = 1;
  public static final int DIALOG_PASSWORD = 2;
  public static final int DIALOG_MESSAGE = 3;
  public static final int DIALOG_NEW_GAME = 4;
  public static final int DIALOG_CUSTOM_COMPUTER = 5;
  public static final int MODE_NO_GAMES = 0;
  public static final int MODE_NOT_IN_GAME = 1;
  public static final int MODE_IN_PROGRESS = 2;
  public static final int MODE_IN_NEW = 3;
  public static final int MODE_CREATED = 4;
  public static final int MODE_CREATED_WITH_ROBOTS = 5;
  public static final String BUTTON_CREATE_GAME = "(C)reate Game";
  public static final String BUTTON_JOIN_GAME = "(J)oin Game";
  public static final String BUTTON_WATCH_GAME = "(W)atch Game";
  public static final String BUTTON_ABANDON_GAME = "(A)bandon Game";
  public static final String BUTTON_ABDICATE_GAME = "(A)bdicate";
  public static final String BUTTON_QUIT_WATCHING = "Quit W(a)tching";
  public static final String BUTTON_PLAY_GAME = "Continue (P)lay";
  public static final String BUTTON_CONTINUE_WATCHING_PLAY = "Continue Watching (P)lay";
  public static final String BUTTON_ADD_ROBOT = "Add (R)obot";
  public static final String BUTTON_START_GAME = "(B)egin Game";
  public static final String BUTTON_DROP_ROBOT = "(D)rop Robot";
  public static final String BUTTON_MESSAGE = "(M)essage (F1)";
  public static final String BUTTON_SOUND_ON = "(S)ound On/Off";
  public static final String BUTTON_QUIT = "(Q)uit";
  public static final String CARD_FORUM = "forum";
  public static final String CARD_GAME = "game";
  private GameListCanvas gameList;
  DialogCanvas dialog;
  private ListCanvas playerList;
  ForumCanvas statusBar;
  private ButtonCanvas buttonBar;
  ScrollText eventList;
  private JPanel westContainer;
  private JPanel eastContainer;
  private MainPanel mainContainer;
  int dialogMode = -1;
  int mode = 0;
  Point addRobotPoint;
  Point dropRobotPoint;
  Point startGamePoint;
  boolean soundOn = false;
  Game selectedGame = null;
  Server server;
  FrontEnd frontEnd;
  PopupMenu robotMenu;
  PopupMenu dropMenu;
  CardLayout card;
  ClientUI gameUI = null;
  String clientName;
  String firstPassword = null;
  
  private static final Logger logger = LogManager.getLogger();
  
  public ClientForum(FrontEnd paramFrontEnd, String serverName, Toolkit paramToolkit, Server paramServer)
  {
    super(null);
    frontEnd = paramFrontEnd;
    server = paramServer;
    Dimension localDimension = paramFrontEnd.getSize();
    mainContainer = new MainPanel(localDimension);
    card = new CardLayout();
    paramFrontEnd.getContainer().setLayout(card);
    mainContainer.setBackground(Color.BLACK);
    mainContainer.setLayout( new BorderLayout() );
    int i = localDimension.height / 49;
    i = FontFinder.getFont(paramToolkit, "SansSerif", 19, 50 * localDimension.height / 100).getSize();
    int j = 11;
    logger.debug( "height is {}", localDimension.height );
    gameList = new GameListCanvas(this, serverName, localDimension.height, j, i, paramToolkit);
    logger.debug( "game list size {}", gameList.getSize() );
    int k = gameList.getSize().height;
    dialog = new DialogCanvas(localDimension.height, i, paramToolkit);
    int m = dialog.getSize().height;
    dialog.setButtonListener(this);
    int n = localDimension.height - m - k;
    playerList = new ListCanvas(localDimension.width, n, i, paramToolkit);
//    {
//      public void paint(Graphics paramAnonymousGraphics)
//      {
//        super.paint(paramAnonymousGraphics);
//        paramAnonymousGraphics.setColor(ClientForum.BORDER_COLOUR);
//        paramAnonymousGraphics.drawRect(0, -1, localDimension.width - 1, localDimension.height);
//      }
//    };
    playerList.addText("Players: ", Color.orange);
    int i1 = localDimension.width - localDimension.height;
    int i2 = ForumCanvas.computeHeight(i, 3, paramToolkit);
    statusBar = new ForumCanvas(i1, i2, i, paramToolkit);
    buttonBar = new ButtonCanvas(i, paramToolkit, i1, 0);
    Dimension buttonBarDim = buttonBar.getSize();
    buttonBar.setSize( buttonBarDim.width,((14 * buttonBar.buttonHeight / 10 + 1) * 7) );
    buttonBar.setButtonListener(this);
    int i3 = localDimension.height - buttonBar.getSize().height - i2;
    eventList = new ScrollText(i, paramToolkit, i1, i3);
    westContainer = new JPanel(new BorderLayout());
    eastContainer = new JPanel(new BorderLayout());
    westContainer.add(gameList, BorderLayout.NORTH);
    westContainer.add(dialog, BorderLayout.CENTER);
    westContainer.add(playerList, BorderLayout.SOUTH);
    eastContainer.add(statusBar, BorderLayout.NORTH);
    eastContainer.add(buttonBar, BorderLayout.CENTER);
    eastContainer.add(eventList, BorderLayout.SOUTH);
    
    JPanel gridWest = new JPanel(new GridLayout());
    gridWest.add( westContainer );
    mainContainer.add(gridWest, BorderLayout.WEST );
    mainContainer.add(eastContainer, BorderLayout.EAST );  
    
    paramFrontEnd.getContainer().add(mainContainer, "forum");
    card.show(paramFrontEnd.getContainer(), "forum");
    gameList.addKeyListener(this);
    dialog.addKeyListener(this);
    playerList.addKeyListener(this);
    statusBar.addKeyListener(this);
    buttonBar.addKeyListener(this);
    eventList.addKeyListener(this);
    eastContainer.addKeyListener(this);
    westContainer.addKeyListener(this);
    mainContainer.addKeyListener(this);
    mainContainer.requestFocus();
    
//    Dimension prefSize = new Dimension( 880, 400 );
//    westContainer.setPreferredSize( prefSize );
//    eastContainer.setPreferredSize( prefSize );
//    playerList.setPreferredSize( new Dimension( 600, 100 ) );
//    mainContainer.setSize( 1440, 700 );
//    westContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED, Color.RED, Color.RED ) );
//    eastContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED, Color.BLUE, Color.BLUE ) );
//    gameList.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED, Color.GREEN, Color.GREEN ) );
//    //mainContainer.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED, Color.RED, Color.BLUE ) );
//    mainContainer.validate();
  }
  
  public boolean abandonGame(String paramString)
  {
    Player localPlayer = getPlayer(paramString);
    Game localGame = localPlayer.game;
    boolean bool = true;
    if (localGame == null) {
      bool = false;
    } else if (!localGame.inProgress) {
      bool = super.abandonGame(paramString);
    } else {
      localPlayer.game = null;
    }
    if (bool)
    {
      int i = getRow(localGame);
      gameList.setRow(i, localGame);
      post(new CText(paramString, BOLD_COLOUR));
      if (localGame.inProgress)
      {
        post(new CText(" quits game ", PLAIN_COLOUR));
        playerList.changeColour(paramString, IDLE_PLAYER_COLOUR);
      }
      else
      {
        post(new CText(" abandons game ", PLAIN_COLOUR));
      }
      post(new CText(localPlayer.name, BOLD_COLOUR));
      post(new CText(".", PLAIN_COLOUR));
      newLine();
      if (isMe(paramString)) {
        setMode(MODE_NOT_IN_GAME);
      }
      if ((localGame.creator.equals(paramString)) && (!localGame.inProgress))
      {
        post(new CText("Game ", PLAIN_COLOUR));
        post(new CText(localGame.name, BOLD_COLOUR));
        post(new CText(" evaporates.", PLAIN_COLOUR));
        newLine();
        games.removeElement(localGame);
        gameList.removeGame(localGame);
        for (int j = 0; j < localGame.numPlayers; j++)
        {
          localGame.player[j].game = null;
          if (localGame.player[j].name.equals(clientName))
          {
            selectGame(localGame.name);
            setMode(MODE_NOT_IN_GAME);
          }
        }
        if (games.size() == 0)
        {
          setMode(MODE_NO_GAMES);
          selectedGame = null;
        }
        else if (selectedGame == localGame)
        {
          selectGame(localGame.name);
          setMode(mode);
        }
      }
      setMode(mode);
      return true;
    }
    return false;
  }
  
  public void acceptDialog()
  {
    String str = dialog.getText().trim();
    switch (dialogMode)
    {
    case DIALOG_ALIAS: 
      if (str.length() > 8)
      {
        setDialog(0, "Enter your alias", "Must not exceed 8 characters.");
      }
      else if (str.length() < 2)
      {
        setDialog(0, "Enter your alias", "Must have at least 2 characters.");
      }
      else if ((str.startsWith(" ")) || (str.startsWith("  ")))
      {
        setDialog(0, "Enter your alias", "Must not begin with blank characters.");
      }
      else
      {
        clientName = str;
        clearDialog();
        server.send(str);
      }
      break;
    case DIALOG_NEW_PASSWORD: 
      firstPassword = str;
      setDialog(2, "Confirm your new password", null);
      break;
    case DIALOG_PASSWORD: 
      if ((firstPassword != null) && (!firstPassword.equals(str)))
      {
        setDialog(1, "If you are a new user, enter you password. Otherwise, hit <CANCEL> to re-enter your alias.", "Passwords did not match.");
      }
      else
      {
        clearDialog();
        server.send(str);
      }
      break;
    case DIALOG_MESSAGE: 
      clearDialog();
      server.send("+");
      server.send("@");
      server.send(str);
      break;
    case DIALOG_NEW_GAME: 
      clearDialog();
      server.send("+");
      server.send("#");
      server.send(str);
      break;
    case DIALOG_CUSTOM_COMPUTER: 
      clearDialog();
      server.send("+");
      server.send("?");
      server.send(str);
    }
  }
  
  public void actionPerformed(ActionEvent paramActionEvent)
  {
    String str1 = paramActionEvent.getActionCommand();
    String str2 = str1.substring(1);
    if (str1.charAt(0) == '+')
    {
      server.send("+");
      server.send("*");
      server.send(selectedGame.name);
      server.send(str2);
    }
    else if (str1.charAt(0) == '-')
    {
      server.send("+");
      server.send("\\");
      server.send(selectedGame.name);
      server.send(str2);
    }
    else if (str1.charAt(0) == '!')
    {
      setDialog(5, "Enter URL, a '|', the robot class name, a '#' and then skill level of the custom robot.", "(eg. http://www.cs.utoronto.ca/~jw|igx.bots.MoonBot.class#2)");
    }
    else if (str1.charAt(0) == '?')
    {
      server.send("+");
      server.send("*");
      server.send(selectedGame.name);
      server.send("?");
    }
    else if (str1.charAt(0) == '<')
    {
      server.send("+");
      server.send("&");
      server.send(str2);
    }
  }
  
  private Point addButton(String paramString, int buttonRow)
  {
    Dimension localDimension = buttonBar.buttonDimensions(paramString);
    Point localPoint = new Point((buttonBar.getSize().width - buttonBar.getSize().width) / 2, 
            buttonRow * (14 * buttonBar.buttonHeight / 10 + 1));
    buttonBar.addButton(localPoint.x, localPoint.y, paramString);
    return localPoint;
  }
  
  public boolean addCustomRobot(Robot paramRobot, String paramString)
  {
    String str = paramRobot.name;
    boolean bool = super.addCustomRobot(paramRobot, paramString);
    Game localGame = getGame(paramString);
    gameList.setRow(getRow(localGame), localGame);
    post(new CText("Robot ", PLAIN_COLOUR));
    post(new CText(str, BOLD_COLOUR));
    post(new CText(" joins game ", PLAIN_COLOUR));
    post(new CText(paramString, BOLD_COLOUR));
    post(new CText(".", PLAIN_COLOUR));
    newLine();
    if ((isMe(localGame.creator)) && (mode == MODE_CREATED)) {
      setMode(MODE_CREATED_WITH_ROBOTS);
    }
    return bool;
  }
  
  public Player addPlayer(String playerName)
  {
    Player localPlayer = null;
    int i = 0;
    Game localGame;
    if (server.dispatch != null)
    {
      localGame = getClientGame(playerName, server.dispatch.name);
      if (localGame != null)
      {
        localPlayer = server.dispatch.Game.getPlayer(playerName);
        boolean bool = localPlayer.isActive;
        super.addPlayer(localPlayer);
        if (bool)
        {
          localPlayer.isPresent = true;
          localPlayer.game = localGame;
          i = 1;
        }
        else
        {
          localPlayer.game = null;
          localPlayer.inGame = false;
        }
      }
    }
    if (localPlayer == null)
    {
      localPlayer = super.addPlayer(playerName);
      localGame = getPlayerGame(localPlayer);
      if (localGame != null) {
        i = 1;
      }
    }
    post(new CText(playerName, BOLD_COLOUR));
    post(new CText(" just showed up!", PLAIN_COLOUR));
    if (i != 0) {
      playerList.addText(playerName, ACTIVE_PLAYER_COLOUR);
    } else {
      playerList.addText(playerName, IDLE_PLAYER_COLOUR);
    }
    newLine();
    return localPlayer;
  }
  
  public boolean addRobot(String paramString1, String paramString2)
  {
    boolean bool = super.addRobot(paramString1, paramString2);
    Game localGame = getGame(paramString2);
    gameList.setRow(getRow(localGame), localGame);
    post(new CText("Robot ", PLAIN_COLOUR));
    post(new CText(paramString1, BOLD_COLOUR));
    post(new CText(" joins game ", PLAIN_COLOUR));
    post(new CText(paramString2, BOLD_COLOUR));
    post(new CText(".", PLAIN_COLOUR));
    newLine();
    if (isMe(localGame.creator)) {
      setMode(MODE_CREATED_WITH_ROBOTS);
    }
    return bool;
  }
  
  public void buttonPressed(String paramString)
  {
    if (paramString.equals("(M)essage (F1)"))
    {
      if (dialogMode != 3) {
        setDialog(3, "Enter Message Text", null);
      }
    }
    else if (paramString.equals("(S)ound On/Off")) {
      toggleSound();
    } else if (paramString.equals("(Q)uit")) {
      frontEnd.quitProgram();
    } else if (paramString.equals("Cancel"))
    {
      if (dialogMode == 0) {
        frontEnd.quitProgram();
      } else if ((dialogMode == 2) || (dialogMode == 1)) {
        server.send("");
      } else {
        clearDialog();
      }
    }
    else if (paramString.equals("Okay")) {
      acceptDialog();
    } else if (paramString.equals("(C)reate Game")) {
      setDialog(4, "Invent a name for this game", null);
    } else if (paramString.equals("(J)oin Game")) {
      sendJoinGame();
    } else if (paramString.equals("(W)atch Game")) {
      sendWatchGame();
    } else if (paramString.equals("(A)bandon Game")) {
      sendAbandonGame();
    } else if ((paramString.equals("(A)bdicate")) || (paramString.equals("Quit W(a)tching"))) {
      sendAbdicateGame();
    } else if ((paramString.equals("Continue (P)lay")) || (paramString.equals("Continue Watching (P)lay"))) {
      playGame();
    } else if (paramString.equals("Add (R)obot")) {
      doAddRobot();
    } else if (paramString.equals("(B)egin Game")) {
      sendGetMaps();
    } else if (paramString.equals("(D)rop Robot")) {
      doDropRobot();
    }
  }
  
  public void clearDialog()
  {
    dialog.clearDialog();
    dialogMode = -1;
  }
  
  public boolean createGame(String paramString1, String paramString2)
  {
    if (super.createGame(paramString1, paramString2))
    {
      Game localGame = getGame(paramString2);
      gameList.addGame(localGame);
      post(new CText(paramString1, BOLD_COLOUR));
      post(new CText(" creates a new game, ", PLAIN_COLOUR));
      post(new CText(paramString2, BOLD_COLOUR));
      post(new CText(".", PLAIN_COLOUR));
      newLine();
      if (isMe(paramString1))
      {
        selectGame(paramString2);
      }
      else if (mode == 0)
      {
        selectGame(paramString2);
        setMode(MODE_NOT_IN_GAME);
      }
      return true;
    }
    return false;
  }
  
  public void displayForum(boolean paramBoolean)
  {
    if (server.dispatch != null) {
      selectGame(server.dispatch.name);
    }
    if (!paramBoolean)
    {
      setMode(MODE_IN_PROGRESS);
    }
    else
    {
      setMode(MODE_NOT_IN_GAME);
      server.dispatch = null;
    }
    card.show(frontEnd.getContainer(), "forum");
    mainContainer.requestFocus();
  }
  
  public void displayGame(Dispatcher paramDispatcher, boolean paramBoolean)
  {
    clearDialog();
    if (gameUI != null) {
      card.removeLayoutComponent(gameUI);
    }
    gameUI = new ClientUI(paramDispatcher, frontEnd, this, paramBoolean);
    frontEnd.getContainer().add(gameUI, "game");
    card.show(frontEnd.getContainer(), "game");
    gameUI.setFocus();
  }
  
  public void doAddRobot()
  {
    robotMenu.show(buttonBar, addRobotPoint.x, addRobotPoint.y + buttonBar.buttonFontHeight);
  }
  
  public void doChooseMap(Vector paramVector)
  {
    PopupMenu localPopupMenu = new PopupMenu();
    buttonBar.add(localPopupMenu);
    MenuItem localMenuItem = new MenuItem("Randomized Map");
    localMenuItem.setActionCommand("<<");
    localMenuItem.addActionListener(this);
    localPopupMenu.add(localMenuItem);
    localPopupMenu.add("-");
    int i = paramVector.size();
    for (int j = 0; j < i; j++)
    {
      String str = (String)paramVector.elementAt(j);
      localMenuItem = new MenuItem(str);
      localMenuItem.setActionCommand("<" + str);
      localMenuItem.addActionListener(this);
      localPopupMenu.add(localMenuItem);
    }
    localPopupMenu.show(buttonBar, startGamePoint.x, startGamePoint.y + buttonBar.buttonFontHeight);
  }
  
  public void doDropRobot()
  {
    dropMenu = new PopupMenu();
    buttonBar.add(dropMenu);
    for (int i = 0; i < selectedGame.numPlayers; i++) {
      if (!selectedGame.player[i].isHuman)
      {
        Robot localRobot = getRobot(selectedGame.player[i].name);
        MenuItem localMenuItem = new MenuItem(localRobot.name + "(" + localRobot.ranking + ")");
        localMenuItem.setActionCommand("-" + localRobot.name);
        localMenuItem.addActionListener(this);
        dropMenu.add(localMenuItem);
      }
    }
    dropMenu.show(buttonBar, dropRobotPoint.x, dropRobotPoint.y + buttonBar.buttonFontHeight);
  }
  
  public void gameOver(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString1);
    if (localGame != null)
    {
      super.gameOver(paramString1);
      post(new CText("Game ", PLAIN_COLOUR));
      post(new CText(paramString1, BOLD_COLOUR));
      post(new CText(" ends.", PLAIN_COLOUR));
      newLine();
      post(new CText("Winner: ", PLAIN_COLOUR));
      post(new CText(paramString2, BOLD_COLOUR));
      post(new CText(".", PLAIN_COLOUR));
      newLine();
      for (int i = 0; i < localGame.numPlayers; i++) {
        playerList.changeMultiColour(localGame.player[i].name, IDLE_PLAYER_COLOUR);
      }
      playerList.repaint();
      gameList.removeGame(localGame);
      if (selectedGame == localGame)
      {
        selectGame(localGame.name);
        Player localPlayer = getPlayer(clientName);
        localPlayer.isActive = false;
        localPlayer.inGame = false;
        localPlayer.game = null;
        displayForum(true);
        setMode(MODE_NOT_IN_GAME);
      }
      if (games.size() == 0)
      {
        setMode(MODE_NO_GAMES);
        selectedGame = null;
      }
    }
  }
  
  public boolean gameSelected(int paramInt)
  {
    paramInt /= 2;
    if (paramInt >= games.size()) {
      return false;
    }
    if (paramInt < 0) {
      return false;
    }
    if (mode == 1)
    {
      selectedGame = ((Game)games.elementAt(games.size() - 1 - paramInt));
      setMode(mode);
      return true;
    }
    return false;
  }
  
  public Game getClientGame(String paramString1, String paramString2)
  {
    Game localGame = getGame(paramString2);
    if (localGame == null) {
      return null;
    }
    if (localGame.getPlayer(paramString1) == null) {
      return null;
    }
    return localGame;
  }
  
  public Game getPlayerGame(Player paramPlayer)
  {
    int i = games.size();
    for (int j = 0; j < i; j++)
    {
      Game localGame = (Game)games.elementAt(j);
      if (localGame.getPlayer(paramPlayer.name) != null)
      {
        paramPlayer.game = localGame;
        return localGame;
      }
    }
    return null;
  }
  
  public Game getPlayerGame(String paramString)
  {
    int i = games.size();
    for (int j = 0; j < i; j++)
    {
      Game localGame = (Game)games.elementAt(j);
      if (localGame.getPlayer(paramString) != null) {
        return localGame;
      }
    }
    return null;
  }
  
  public int getRow(Game paramGame)
  {
    for (int i = 0; i < games.size(); i++)
    {
      Game localGame = (Game)games.elementAt(i);
      if (localGame == paramGame) {
        return (games.size() - i - 1) * 2;
      }
    }
    return -1;
  }
  
  public boolean isMe(String paramString)
  {
    return paramString.equals(clientName);
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
  
  public boolean joinGame(String paramString1, String paramString2)
  {
    if (super.joinGame(paramString1, paramString2))
    {
      Game localGame = getGame(paramString2);
      gameList.setRow(getRow(localGame), localGame);
      post(new CText(paramString1, BOLD_COLOUR));
      post(new CText(" joins game ", PLAIN_COLOUR));
      post(new CText(paramString2, BOLD_COLOUR));
      post(new CText(".", PLAIN_COLOUR));
      newLine();
      if (isMe(paramString1))
      {
        selectGame(paramString2);
        if (isMe(localGame.creator)) {
          setMode(MODE_CREATED);
        } else {
          setMode(MODE_IN_NEW);
        }
      }
      else
      {
        setMode(mode);
      }
      return true;
    }
    return false;
  }
  
  public void keyPressed(KeyEvent paramKeyEvent)
  {
    int i = paramKeyEvent.getKeyCode();
    char c = paramKeyEvent.getKeyChar();
    if (dialogMode != -1) {
      switch (i)
      {
      case 27: 
        buttonPressed("Cancel");
        break;
      case 10: 
        acceptDialog();
        break;
      default: 
        dialog.keyPressed(paramKeyEvent);
        break;
      }
    } else if (i == 112) {
      setDialog(3, "Enter Message Text", null);
    } else if (i == 38) {
      scroll(true);
    } else if (i == 40) {
      scroll(false);
    } else if (paramKeyEvent.isAltDown()) {
      switch (Character.toLowerCase(c))
      {
      case 'c': 
        setDialog(4, "Invent a name for this game", null);
        break;
      case 'j': 
        sendJoinGame();
        break;
      case 'w': 
        sendWatchGame();
        break;
      case 'a': 
        if (selectedGame.inProgress) {
          sendAbdicateGame();
        } else {
          sendAbandonGame();
        }
        break;
      case 'p': 
        playGame();
        break;
      case 'r': 
        doAddRobot();
        break;
      case 'b': 
        sendGetMaps();
        break;
      case 'd': 
        doDropRobot();
        break;
      case 'm': 
        setDialog(3, "Enter Message Text", null);
        break;
      case 's': 
        toggleSound();
        break;
      case 'q': 
        frontEnd.quitProgram();
      }
    }
    paramKeyEvent.consume();
  }
  
  public void keyReleased(KeyEvent paramKeyEvent) {}
  
  public void keyTyped(KeyEvent paramKeyEvent) {}
  
  public static void main(String[] paramArrayOfString)
  {
    Frame localFrame = new Frame("Inner Demons");
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    Dimension localDimension = localToolkit.getScreenSize();
    localFrame.pack();
    localFrame.show();
    localFrame.setSize(localDimension);
    Insets localInsets = localFrame.getInsets();
    int i = localDimension.width - localInsets.left - localInsets.right;
    int j = localDimension.height - localInsets.top - localInsets.bottom;
    Robot[] arrayOfRobot = new Robot[0];
    I iClass = new I( "Foo" );
    Socket sock = new Socket();
    Server s = new Server( sock );
    ClientForum localObject = new ClientForum( iClass, "param", localToolkit, s );
    localFrame.add(localObject.mainContainer);
    localObject.setToPreferredSize();
    localFrame.validate();
  }
  
  public void message(String paramString1, String paramString2)
  {
    super.message(paramString1, paramString2, 10);
    post(new CText(paramString1, MESSAGE_AUTHOR_COLOUR));
    post(new CText(": " + paramString2, MESSAGE_TEXT_COLOUR));
    newLine();
  }
  
  public void newLine()
  {
    eventList.newLine();
  }
  
  public boolean playerQuit(String paramString)
  {
    if (isMe(paramString)) {
      return true;
    }
    super.removePlayer(paramString);
    playerList.removeText(paramString);
    post(new CText(paramString, BOLD_COLOUR));
    post(new CText(" called it a day!", PLAIN_COLOUR));
    newLine();
    return false;
  }
  
  public void playGame()
  {
    if (gameUI != null)
    {
      Player localPlayer = getPlayer(server.name);
      if ((localPlayer != null) && (localPlayer.isActive))
      {
        card.show(frontEnd.getContainer(), "game");
        gameUI.requestFocus();
      }
    }
  }
  
  public void post(CText paramCText)
  {
    eventList.addText(paramCText);
  }
  
  public void post(String paramString, Color paramColor)
  {
    post(new CText(paramString, paramColor));
    newLine();
  }
  
  public void registerClient()
    throws IOException
  {
    Game localObject1 = null;
    server.name = clientName;
    logger.debug( "Registering client" );
    for (String str1 = server.receive(); !str1.equals("~"); str1 = server.receive())
    {
        logger.debug( "Creating new player with name {}", str1 );
      Player localObject2 = new Player(str1);
      players.addElement(localObject2);
      localObject2.isActive = server.receiveBoolean();
      logger.debug( "is active? {}", localObject2.isActive );
    }
    
    logger.debug( "About to rx games" );
    boolean i1;
    for (String gameName = server.receive(); !gameName.equals("~"); gameName = server.receive())
    {
      Game gameInformationFor = null;
      boolean gameInProgress;
      if (server.receive().equals("[")) {
        gameInProgress = true;
      } else {
        gameInProgress = false;
      }
      int numberOfPlayers = Integer.parseInt(server.receive());
      for (int k = 0; k < numberOfPlayers; k++)
      {
        String creator = server.receive();
        i1 = server.receiveBoolean();
        if (k == 0) {
            logger.debug( "Creating new game named {} and created by {}", gameName, creator );
          gameInformationFor = new Game(gameName, creator);
        }
        Player localPlayer2 = getPlayer(creator);
        if (localPlayer2 == null)
        {
          Robot localRobot = getRobot(creator);
          if (localRobot == null)
          {
            localPlayer2 = new Player(creator);
            localPlayer2.isPresent = false;
          }
          else
          {
            localPlayer2 = localRobot.toPlayer();
          }
        }
        localPlayer2.game = ((Game)gameInformationFor);
        gameInformationFor.player[k] = localPlayer2;
      }
      gameInformationFor.inProgress = gameInProgress;
      gameInformationFor.numPlayers = numberOfPlayers;
      games.addElement(gameInformationFor);
      gameList.addGame((Game)gameInformationFor);
    }
    
    logger.debug( "Got all game info.  Client name {}", clientName );
    Player localObject2 = getPlayer(clientName);
    if (server.receive().equals("["))
    {
      Game localGame1 = getGame(server.receive());
      if (localGame1 != null)
      {
        localObject1 = localGame1;
        localObject2.game = localObject1;
        localObject2.inGame = true;
      }
    }
    else
    {
      localObject1 = null;
      localObject2.game = null;
      localObject2.inGame = false;
    }
    int i = players.size();
    for (int j = 0; j < i; j++)
    {
      Player localPlayer1 = (Player)players.elementAt(j);
      if (localPlayer1.isActive) {
        playerList.addText(localPlayer1.name, ACTIVE_PLAYER_COLOUR);
      } else {
        playerList.addText(localPlayer1.name, IDLE_PLAYER_COLOUR);
      }
    }
    Vector localVector = new Vector();
    int column = (statusBar.getSize().width - statusBar.fm.stringWidth(clientName)) / 2;
    localVector.addElement(new TextElement(clientName, STATUS_COLOUR, column));
    statusBar.row[1] = new TextRow(localVector);
    statusBar.repaint();
    if (localObject1 != null)
    {
      selectGame(localObject1.name);
      if (localObject1.inProgress)
      {
        setMode(MODE_IN_PROGRESS);
      }
      else if (localObject1.creator.equals(clientName))
      {
        int n = 0;
        for (int p = 0; p < localObject1.numPlayers; p++) {
          if (!localObject1.player[p].isHuman)
          {
            n = 1;
            break;
          }
        }
        if (n != 0) {
          setMode(MODE_CREATED_WITH_ROBOTS);
        } else {
          setMode(MODE_CREATED);
        }
      }
      else
      {
        setMode(MODE_IN_NEW);
      }
    }
    else if (games.size() == 0)
    {
      setMode(MODE_NO_GAMES);
    }
    else
    {
      gameList.manualSelectRow(3);
      Game localGame2 = (Game)games.elementAt(games.size() - 1);
      selectGame(localGame2.name);
      setMode(MODE_NOT_IN_GAME);
    }
  }
  
  public boolean removeRobot(String paramString1, String paramString2)
  {
    boolean bool = super.removeRobot(paramString1, paramString2);
    Game localGame = getGame(paramString2);
    int i = getRow(localGame);
    gameList.setRow(i, localGame);
    post(new CText("Robot ", PLAIN_COLOUR));
    post(new CText(paramString1, BOLD_COLOUR));
    post(new CText(" is unplugged from game ", PLAIN_COLOUR));
    post(new CText(localGame.name, BOLD_COLOUR));
    post(new CText(".", PLAIN_COLOUR));
    newLine();
    int j = 0;
    for (int k = 0; k < localGame.numPlayers; k++) {
      if (!localGame.player[k].isHuman)
      {
        j = 1;
        break;
      }
    }
    if (isMe(localGame.creator)) {
      if (j == 0) {
        setMode(MODE_CREATED);
      } else {
        setMode(MODE_CREATED_WITH_ROBOTS);
      }
    }
    return bool;
  }
  
  public void scroll(boolean paramBoolean)
  {
    int i = gameList.selectedRow;
    if (paramBoolean) {
      gameList.rowSelected(i - 1);
    } else {
      gameList.rowSelected(i + 1);
    }
  }
  
  public void selectGame(String paramString)
  {
    int i = games.size();
    for (int j = 0; j < i; j++)
    {
      Game localGame = (Game)games.elementAt(j);
      if (localGame.name.equals(paramString))
      {
        gameList.manualSelectRow((games.size() - j - 1) * 2 + 3);
        selectedGame = localGame;
        return;
      }
    }
    if (i == 0)
    {
      gameList.manualSelectRow(-1);
      selectedGame = null;
    }
    else
    {
      gameList.manualSelectRow(3);
      selectedGame = ((Game)games.elementAt(games.size() - 1));
    }
  }
  
  public void sendAbandonGame()
  {
    server.send("+");
    server.send("%");
  }
  
  public void sendAbdicateGame()
  {
    server.send("!");
    server.send(new Integer(0).toString());
  }
  
  public void sendJoinGame()
  {
    server.send("+");
    server.send("$");
    server.send(selectedGame.name);
  }
  
  public void sendGetMaps()
  {
    if (selectedGame != null)
    {
      server.send("+");
      server.send(">");
      server.send(new Integer(selectedGame.numPlayers).toString());
    }
    else
    {
      System.out.println("No selected game?");
    }
  }
  
  public void sendWatchGame()
  {
    server.send("+");
    server.send(")");
    server.send(selectedGame.name);
  }
  
  public void setBotList(Robot[] paramArrayOfRobot)
  {
    botList = paramArrayOfRobot;
    robotMenu = new PopupMenu();
    buttonBar.add(robotMenu);
    MenuItem localMenuItem1 = new MenuItem("Random Robot");
    localMenuItem1.setActionCommand("?");
    localMenuItem1.addActionListener(this);
    robotMenu.add(localMenuItem1);
    robotMenu.add("-");
    String str = "";
    Menu localMenu = new Menu("");
    for (int i = 0; i < paramArrayOfRobot.length; i++)
    {
      if (!paramArrayOfRobot[i].botType.equals(str))
      {
        str = paramArrayOfRobot[i].botType;
        localMenu = new Menu(str);
        robotMenu.add(localMenu);
      }
      MenuItem localMenuItem3 = new MenuItem(paramArrayOfRobot[i].name + " (" + paramArrayOfRobot[i].ranking + ")");
      localMenuItem3.setActionCommand("+" + paramArrayOfRobot[i].name);
      localMenuItem3.addActionListener(this);
      localMenu.add(localMenuItem3);
    }
    robotMenu.add("-");
    MenuItem localMenuItem2 = new MenuItem("Custom Robot...");
    localMenuItem2.setActionCommand("!");
    localMenuItem2.addActionListener(this);
    robotMenu.add(localMenuItem2);
  }
  
  public void setDialog(int paramInt, String paramString1, String paramString2)
  {
    dialog.clearDialog();
    dialog.setDialogText(paramString1);
    if (paramString2 != null) {
      dialog.setErrorText(paramString2);
    }
    dialogMode = paramInt;
    if ((dialogMode == 2) || (dialogMode == 1)) {
      dialog.passwordMode = true;
    }
    mainContainer.requestFocus();
  }
  
  public void setMode(int paramInt)
  {
    mode = paramInt;
    addButton("(M)essage (F1)", 4);
    addButton("(S)ound On/Off", 5);
    addButton("(Q)uit", 6);
    switch (paramInt)
    {
    case MODE_NO_GAMES: 
      addButton("(C)reate Game", 0);
      break;
    case 1: 
      addButton("(C)reate Game", 0);
      if (selectedGame != null) {
        if (selectedGame.inProgress) {
          addButton("(W)atch Game", 1);
        } else {
          addButton("(J)oin Game", 1);
        }
      }
      break;
    case 2: 
      Player localPlayer = selectedGame.getPlayer(clientName);
      if ((localPlayer != null) && (localPlayer.isActive))
      {
        addButton("Continue (P)lay", 0);
        addButton("(A)bdicate", 1);
      }
      else
      {
        addButton("Continue Watching (P)lay", 0);
        addButton("Quit W(a)tching", 1);
      }
      break;
    case 3: 
      addButton("(A)bandon Game", 0);
      break;
    case 4: 
      if (selectedGame.numPlayers < 9) {
        addRobotPoint = addButton("Add (R)obot", 0);
      }
      if (selectedGame.numPlayers > 1) {
        startGamePoint = addButton("(B)egin Game", 1);
      }
      addButton("(A)bandon Game", 2);
      break;
    case 5: 
      if (selectedGame.numPlayers < 9) {
        addRobotPoint = addButton("Add (R)obot", 0);
      }
      startGamePoint = addButton("(B)egin Game", 1);
      dropRobotPoint = addButton("(D)rop Robot", 2);
      addButton("(A)bandon Game", 3);
    }
    buttonBar.repaint();
  }
  
  public void setToPreferredSize()
  {
      //logger.debug( "Setting preferred size: {} {}", playerList.width, playerList.height );
    //gameList.setSize(gameList.width, gameList.height);
    //dialog.setSize(dialog.width, dialog.height);
    //playerList.setSize(playerList.width, playerList.height);
    //statusBar.setSize(statusBar.width, statusBar.height);
    //buttonBar.setSize(buttonBar.width, buttonBar.height);
    //eventList.setSize(eventList.width, eventList.height);
    //westContainer.setSize(height, height);
    //eastContainer.setSize(width - height, height);
    //logger.debug( "main size {} {}", width, height );
    //mainContainer.setSize(width, height);
  }
  
  public Game startNewGame(String paramString1, String paramString2)
  {
    super.startGame(paramString1, paramString2);
    Game localGame1 = getGame(paramString1);
    gameList.setRow(getRow(localGame1), localGame1);
    post(new CText("Game ", PLAIN_COLOUR));
    post(new CText(paramString1, BOLD_COLOUR));
    post(new CText(" begins.", PLAIN_COLOUR));
    newLine();
    for (int i = 0; i < localGame1.numPlayers; i++) {
      playerList.changeMultiColour(localGame1.player[i].name, ACTIVE_PLAYER_COLOUR);
    }
    playerList.repaint();
    Game localGame2 = getClientGame(clientName, paramString1);
    if (localGame2 != null)
    {
      setMode(MODE_IN_PROGRESS);
      return localGame2;
    }
    setMode(mode);
    return null;
  }
  
  public void toggleSound()
  {
    boolean bool = !frontEnd.getSoundMode();
    frontEnd.setSoundMode(bool);
    if (bool)
    {
      frontEnd.play("soundon.au");
      post("Sound is on. Hit <CTRL>-<S> again to turn it off.", PLAIN_COLOUR);
    }
    else
    {
      post("Sound is off.", PLAIN_COLOUR);
    }
  }
  
  public void watchGame(String paramString1, String paramString2)
  {
    super.watchGame(paramString1, paramString2);
  }
  
  static class MainPanel
    extends JPanel
  {
    Dimension d;
    
    public MainPanel(Dimension paramDimension)
    {
      super();
      enableEvents(8L);
      d = paramDimension;
    }
    
    public Dimension getPreferredSize()
    {
      return d;
    }
    
    public boolean isFocusTraversable()
    {
      return true;
    }
  }
}