package igx.client;

// ClientForum.java
import igx.shared.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;

public class ClientForum extends Forum implements KeyListener, ButtonListener, ActionListener {
    // fontSize = height / FONT_RATIO

    public static final int FONT_RATIO = 49;
    public static final int FONT_PERCENT = 50;
    // Number of rows in status bar
    public static final int STATUS_ROWS = 3;
    // Max number of buttons in button bar
    public static final int MAX_BUTTONS = 7;
    // Spacing ratio between buttons... 0 means no spacing, 5 means 1/2 a button , 10 means whole button
    public static final int BUTTON_SPACING = 4;
    // Colours
    public static final Color BORDER_COLOUR = Color.gray;
    public static final Color BULLETIN_COLOUR = Color.orange;
    public static final Color STATUS_COLOUR = Color.white;
    public static final Color ACTIVE_PLAYER_COLOUR = Color.lightGray;
    public static final Color IDLE_PLAYER_COLOUR = Color.white;
    public static final Color BOLD_COLOUR = Color.white;
    public static final Color PLAIN_COLOUR = new Color(96, 96, 255);
    public static final Color MESSAGE_AUTHOR_COLOUR = Color.white;
    public static final Color MESSAGE_TEXT_COLOUR = Color.lightGray;
    // Dialog modes
    public static final int DIALOG_NONE = -1;
    public static final int DIALOG_ALIAS = 0;
    public static final int DIALOG_NEW_PASSWORD = 1;
    public static final int DIALOG_PASSWORD = 2;
    public static final int DIALOG_MESSAGE = 3;
    public static final int DIALOG_NEW_GAME = 4;
    public static final int DIALOG_CUSTOM_COMPUTER = 5;
    // Forum modes
    public static final int MODE_NO_GAMES = 0;
    public static final int MODE_NOT_IN_GAME = 1;
    public static final int MODE_IN_PROGRESS = 2;
    public static final int MODE_IN_NEW = 3;
    public static final int MODE_CREATED = 4;
    public static final int MODE_CREATED_WITH_ROBOTS = 5;
    // Buttons
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
    // public static final String BUTTON_SOUND_OFF = "(S)ound Off";
    public static final String BUTTON_QUIT = "(Q)uit";
    // Different cards
    public static final String CARD_FORUM = "forum";
    public static final String CARD_GAME = "game";

    GameListCanvas gameList;
    DialogCanvas dialog;
    ListCanvas playerList;
    ForumCanvas statusBar;
    ButtonCanvas buttonBar;
    ScrollText eventList;
    JPanel westContainer, eastContainer;
    MainPanel mainContainer;
    int width, height;
    int dialogMode = DIALOG_NONE;
    int mode = MODE_NO_GAMES;
    Point addRobotPoint, dropRobotPoint, startGamePoint;
    boolean soundOn = false;
    Game selectedGame = null;
    Server server;
    FrontEnd frontEnd;
    PopupMenu robotMenu, dropMenu;
    // The main card layout
    CardLayout card;
    // The UI of the current game
    ClientUI gameUI = null;
    String clientName;
    // Initial password for confirmation of new password
    String firstPassword = null;

    static class MainPanel extends JPanel {

        Dimension d;

        public MainPanel(Dimension d) {
            super(new BorderLayout());
            enableEvents(AWTEvent.KEY_EVENT_MASK);
            this.d = d;

            setBackground(Color.ORANGE);
        }

        public Dimension getPreferredSize() {
            return d;
        }

        public boolean isFocusTraversable() {
            return true;
        }
    }

    public ClientForum(FrontEnd frontEnd, String serverName, Toolkit toolkit, Server server) {
        super(null);
        this.frontEnd = frontEnd;
        this.server = server;
        Dimension dim = frontEnd.getDimensions();
        // Generate main container and card
        mainContainer = new MainPanel(dim);
        card = new CardLayout();
        frontEnd.getContainer().setLayout(card);
        // Preliminaries
        this.width = dim.width;
        this.height = dim.height; // - fakeHeight;
        mainContainer.setBackground(Color.black);
        int fontSize = height / FONT_RATIO;
        // gameList
        // We want a game list that takes up FONT_PERCENT the height of the screen.
        fontSize = FontFinder.getFont(toolkit,
                "SansSerif",
                Params.MAX_GAMES * 2 + GameListCanvas.HEADER_ROWS,
                FONT_PERCENT * height / 100).getSize();
        int gameListRows = Params.MAX_GAMES + GameListCanvas.HEADER_ROWS;
        gameList = new GameListCanvas(this, serverName, height, gameListRows, fontSize, toolkit);
        int gameListHeight = gameList.height;
        // dialog
        dialog = new DialogCanvas(height, fontSize, toolkit);
        int dialogHeight = dialog.height;
        dialog.setButtonListener(this);
        // playerList
        int playerListHeight = height - dialogHeight - gameListHeight;
        playerList = new ListCanvas(height, playerListHeight, fontSize, toolkit) {
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(BORDER_COLOUR);
                g.drawRect(0, -1, this.width - 1, this.height);
            }
        };
        playerList.addText("Players: ", Color.orange);
        // -- Right side width --
        int sideWidth = width - height;
        // statusBar
        int statusHeight = ForumCanvas.computeHeight(fontSize, STATUS_ROWS, toolkit);
        statusBar = new ForumCanvas(sideWidth, statusHeight, fontSize, toolkit);
        // buttonBar
        buttonBar = new ButtonCanvas(fontSize, toolkit, sideWidth, 0);
        buttonBar.height = ((10 + BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1) * MAX_BUTTONS;
        buttonBar.setButtonListener(this);
        // eventList
        int eventHeight = height - buttonBar.height - statusHeight;
        eventList = new ScrollText(fontSize, toolkit, sideWidth, eventHeight);
        // The two containers for the components
        westContainer = new JPanel(new BorderLayout());
        eastContainer = new JPanel(new BorderLayout());
        // Get them in there!
        westContainer.add(gameList, BorderLayout.NORTH);
        westContainer.add(dialog, BorderLayout.CENTER);
        westContainer.add(playerList, BorderLayout.SOUTH);
        eastContainer.add(statusBar, BorderLayout.NORTH);
        eastContainer.add(buttonBar, BorderLayout.CENTER);
        eastContainer.add(eventList, BorderLayout.SOUTH);

        JPanel gridWest = new JPanel(new GridLayout());
        gridWest.add(westContainer);
        mainContainer.add(gridWest, BorderLayout.WEST);
        mainContainer.add(eastContainer, BorderLayout.EAST);
//	mainContainer.add(westContainer, BorderLayout.WEST);
//	mainContainer.add(eastContainer, BorderLayout.EAST);
        //setDialog(ClientForum.DIALOG_ALIAS, "Enter your alias", null);
        frontEnd.getContainer().add(mainContainer, CARD_FORUM);
        card.show(frontEnd.getContainer(), CARD_FORUM);
        // Add listeners
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
    }

    public boolean abandonGame(String playerName) {
        Player p = getPlayer(playerName);
        Game g = p.game;
        boolean cont = true;
        if (g == null) {
            cont = false;
        } else {
            if (!g.inProgress) {
                cont = super.abandonGame(playerName);
            } else {
                p.game = null;
            }
        }
        if (cont) {
            int rowNum = getRow(g);
            gameList.setRow(rowNum, g);
            post(new CText(playerName, BOLD_COLOUR));
            if (g.inProgress) {
                post(new CText(" quits game ", PLAIN_COLOUR));
                playerList.changeColour(playerName, IDLE_PLAYER_COLOUR);
            } else {
                post(new CText(" abandons game ", PLAIN_COLOUR));
            }
            post(new CText(g.name, BOLD_COLOUR));
            post(new CText(".", PLAIN_COLOUR));
            newLine();
            if (isMe(playerName)) {
                setMode(MODE_NOT_IN_GAME);
            }
            if ((g.creator.equals(playerName)) && (!g.inProgress)) {
                post(new CText("Game ", PLAIN_COLOUR));
                post(new CText(g.name, BOLD_COLOUR));
                post(new CText(" evaporates.", PLAIN_COLOUR));
                newLine();
                games.removeElement(g);
                gameList.removeGame(g);
                for (int i = 0; i < g.numPlayers; i++) {
                    g.player[i].game = null;
                    if (g.player[i].name.equals(clientName)) {
                        selectGame(g.name);
                        setMode(MODE_NOT_IN_GAME);
                    }
                }
                if (games.size() == 0) {
                    setMode(MODE_NO_GAMES);
                    selectedGame = null;
                } else {
                    if (selectedGame == g) {
                        selectGame(g.name);
                        setMode(mode);
                    }
                }
            }
            setMode(mode);
            return true;
        } else {
            return false;
        }
    }

    public void acceptDialog() {
        String text = dialog.getText().trim();
        switch (dialogMode) {
            case DIALOG_ALIAS:
                if (text.length() > Params.MAXNAME) {
                    setDialog(DIALOG_ALIAS, "Enter your alias", "Must not exceed " + Params.MAXNAME + " characters.");
                } else if (text.length() < 2) {
                    setDialog(DIALOG_ALIAS, "Enter your alias", "Must have at least 2 characters.");
                } else if ((text.startsWith(" ")) || (text.startsWith("  "))) {
                    setDialog(DIALOG_ALIAS, "Enter your alias", "Must not begin with blank characters.");
                } else {
                    clientName = text;
                    clearDialog();
                    server.send(text);
                }
                break;
            case DIALOG_NEW_PASSWORD:
                firstPassword = text;
                setDialog(DIALOG_PASSWORD, "Confirm your new password", null);
                break;
            case DIALOG_PASSWORD:
                if ((firstPassword != null) && (!firstPassword.equals(text))) {
                    setDialog(DIALOG_NEW_PASSWORD, "If you are a new user, enter you password. Otherwise, hit <CANCEL> to re-enter your alias.", "Passwords did not match.");
                } else {
                    clearDialog();
                    server.send(text);
                }
                break;
            case DIALOG_MESSAGE:
                clearDialog();
                server.send(Params.FORUM);
                server.send(Params.SENDMESSAGE);
                server.send(text);
                break;
            case DIALOG_NEW_GAME:
                clearDialog();
                server.send(Params.FORUM);
                server.send(Params.NEWGAME);
                server.send(text);
                break;
            case DIALOG_CUSTOM_COMPUTER:
                clearDialog();
                server.send(Params.FORUM);
                server.send(Params.ADDCUSTOMCOMPUTERPLAYER);
                server.send(text);
                break;
        }
    }
// Robot menu selection

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        String name = command.substring(1);
        if (command.charAt(0) == '+') {
            server.send(Params.FORUM);
            server.send(Params.ADDCOMPUTERPLAYER);
            server.send(selectedGame.name);
            server.send(name);
        } else {
            if (command.charAt(0) == '-') {
                server.send(Params.FORUM);
                server.send(Params.REMOVECOMPUTERPLAYER);
                server.send(selectedGame.name);
                server.send(name);
            } else {
                if (command.charAt(0) == '!') {
                    setDialog(DIALOG_CUSTOM_COMPUTER,
                            "Enter URL, a '|', the robot class name, a '#' and then skill level of the custom robot.",
                            "(eg. http://www.cs.utoronto.ca/~jw|igx.bots.MoonBot.class#2)");
                } else if (command.charAt(0) == '?') {
                    server.send(Params.FORUM);
                    server.send(Params.ADDCOMPUTERPLAYER);
                    server.send(selectedGame.name);
                    server.send("?");
                } else if (command.charAt(0) == '<') {
                    server.send(Params.FORUM);
                    server.send(Params.STARTGAME);
                    server.send(name);
                }
            }
        }
    }
    // MODE SWITCHING

    public Point addButton(String text, int row) {
        Dimension d = buttonBar.buttonDimensions(text);
        Point p = new Point((buttonBar.width - d.width) / 2, row * ((10 + BUTTON_SPACING) * buttonBar.buttonHeight / 10 + 1));
        buttonBar.addButton(p.x, p.y, text);
        return p;
    }

    public boolean addCustomRobot(igx.shared.Robot r, String gameName) {
        String robotName = r.name;
        boolean retVal = super.addCustomRobot(r, gameName);
        Game g = getGame(gameName);
        gameList.setRow(getRow(g), g);
        post(new CText("Robot ", PLAIN_COLOUR));
        post(new CText(robotName, BOLD_COLOUR));
        post(new CText(" joins game ", PLAIN_COLOUR));
        post(new CText(gameName, BOLD_COLOUR));
        post(new CText(".", PLAIN_COLOUR));
        newLine();
        if (isMe(g.creator) && (mode == MODE_CREATED)) {
            setMode(MODE_CREATED_WITH_ROBOTS);
        }
        return retVal;
    }

    public Player addPlayer(String name) {
        Player p = null;
        boolean inAGame = false;
        Game g;
        // Look up a possible old player record for this player
        if (server.dispatch != null) {
            g = getClientGame(name, server.dispatch.name);
            if (g != null) {
                p = server.dispatch.Game.getPlayer(name);
                boolean active = p.isActive;
                super.addPlayer(p);
                if (active) {
                    p.isPresent = true;
                    p.game = g;
                    inAGame = true;
                } else {
                    p.game = null;
                    p.inGame = false;
                }
            }
        }
        if (p == null) {
            p = super.addPlayer(name);
            g = getPlayerGame(p);
            if (g != null) {
                inAGame = true;
            }
        }
        post(new CText(name, BOLD_COLOUR));
        post(new CText(" just showed up!", PLAIN_COLOUR));
        if (inAGame) {
            playerList.addText(name, ACTIVE_PLAYER_COLOUR);
        } else {
            playerList.addText(name, IDLE_PLAYER_COLOUR);
        }
        newLine();
        return p;
    }

    public boolean addRobot(String robotName, String gameName) {
        boolean retVal = super.addRobot(robotName, gameName);
        Game g = getGame(gameName);
        gameList.setRow(getRow(g), g);
        post(new CText("Robot ", PLAIN_COLOUR));
        post(new CText(robotName, BOLD_COLOUR));
        post(new CText(" joins game ", PLAIN_COLOUR));
        post(new CText(gameName, BOLD_COLOUR));
        post(new CText(".", PLAIN_COLOUR));
        newLine();
        if (isMe(g.creator)) {
            setMode(MODE_CREATED_WITH_ROBOTS);
        }
        return retVal;
    }
// Button pressing

    public void buttonPressed(String text) {
        if (text.equals(BUTTON_MESSAGE)) {
            if (dialogMode != DIALOG_MESSAGE) {
                setDialog(DIALOG_MESSAGE, "Enter Message Text", null);
            }
        } else {
            if (text.equals(BUTTON_SOUND_ON)) {
                toggleSound();
            } else {
                if (text.equals(BUTTON_QUIT)) {
                    frontEnd.quitProgram();
                } else {
                    if (text.equals(DialogCanvas.CANCEL)) {
                        if (dialogMode == DIALOG_ALIAS) {
                            frontEnd.quitProgram();
                        } else {
                            if ((dialogMode == DIALOG_PASSWORD) || (dialogMode == DIALOG_NEW_PASSWORD)) {
                                server.send("");
                            } else {
                                clearDialog();
                            }
                        }
                    } else {
                        if (text.equals(DialogCanvas.OKAY)) {
                            acceptDialog();
                        } else {
                            if (text.equals(BUTTON_CREATE_GAME)) {
                                setDialog(DIALOG_NEW_GAME, "Invent a name for this game", null);
                            } else {
                                if (text.equals(BUTTON_JOIN_GAME)) {
                                    sendJoinGame();
                                } else {
                                    if (text.equals(BUTTON_WATCH_GAME)) {
                                        sendWatchGame();
                                    } else {
                                        if (text.equals(BUTTON_ABANDON_GAME)) {
                                            sendAbandonGame();
                                        } else {
                                            if (text.equals(BUTTON_ABDICATE_GAME) || text.equals(BUTTON_QUIT_WATCHING)) {
                                                sendAbdicateGame();
                                            } else {
                                                if (text.equals(BUTTON_PLAY_GAME) || text.equals(BUTTON_CONTINUE_WATCHING_PLAY)) {
                                                    playGame();
                                                } else {
                                                    if (text.equals(BUTTON_ADD_ROBOT)) {
                                                        doAddRobot();
                                                    } else {
                                                        if (text.equals(BUTTON_START_GAME)) {
                                                            sendGetMaps();
                                                        } //sendStartGame();
                                                        else {
                                                            if (text.equals(BUTTON_DROP_ROBOT)) {
                                                                doDropRobot();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearDialog() {
        dialog.clearDialog();
        dialogMode = DIALOG_NONE;
    }

    public boolean createGame(String playerName, String gameName) {
        if (super.createGame(playerName, gameName)) {
            Game g = getGame(gameName);
            gameList.addGame(g);
            post(new CText(playerName, BOLD_COLOUR));
            post(new CText(" creates a new game, ", PLAIN_COLOUR));
            post(new CText(gameName, BOLD_COLOUR));
            post(new CText(".", PLAIN_COLOUR));
            newLine();
            if (isMe(playerName)) {
                selectGame(gameName);
            } else {
                if (mode == MODE_NO_GAMES) {
                    selectGame(gameName);
                    setMode(MODE_NOT_IN_GAME);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void displayForum(boolean quitGame) {
        if (server.dispatch != null) {
            selectGame(server.dispatch.name);
        }
        if (!quitGame) {
            setMode(MODE_IN_PROGRESS);
        } else {
            setMode(MODE_NOT_IN_GAME);
            server.dispatch = null;
        }
        card.show(frontEnd.getContainer(), CARD_FORUM);
        mainContainer.requestFocus();
    }
// Sets up a UI and displays a game

    public void displayGame(Dispatcher d, boolean watcher) {
        clearDialog();
        if (gameUI != null) {
            card.removeLayoutComponent(gameUI);
        }
        gameUI = new ClientUI(d, frontEnd, this, watcher);
        frontEnd.getContainer().add(gameUI, CARD_GAME);
        card.show(frontEnd.getContainer(), CARD_GAME);
        gameUI.setFocus();
    }

    public void doAddRobot() {
        robotMenu.show(buttonBar, addRobotPoint.x, addRobotPoint.y + buttonBar.buttonFontHeight);
    }

    public void doChooseMap(Vector maps) {
        PopupMenu mapMenu = new PopupMenu();
        buttonBar.add(mapMenu);
        // Add random robot option
        MenuItem item = new MenuItem("Randomized Map");
        item.setActionCommand("<" + Params.RANDOMMAP);
        item.addActionListener(this);
        mapMenu.add(item);
        mapMenu.add("-");
        int n = maps.size();
        for (int i = 0; i < n; i++) {
            String map = (String) (maps.elementAt(i));
            item = new MenuItem(map);
            item.setActionCommand("<" + map);
            item.addActionListener(this);
            mapMenu.add(item);
        }
        mapMenu.show(buttonBar, startGamePoint.x, startGamePoint.y + buttonBar.buttonFontHeight);
    }

    public void doDropRobot() {
        dropMenu = new PopupMenu();
        buttonBar.add(dropMenu);
        for (int i = 0; i < selectedGame.numPlayers; i++) {
            if (!(selectedGame.player[i].isHuman)) {
                igx.shared.Robot r = getRobot(selectedGame.player[i].name);
                MenuItem item = new MenuItem(r.name + "(" + r.ranking + ")");
                item.setActionCommand("-" + r.name);
                item.addActionListener(this);
                dropMenu.add(item);
            }
        }
        dropMenu.show(buttonBar, dropRobotPoint.x, dropRobotPoint.y + buttonBar.buttonFontHeight);
    }

    public void gameOver(String gameName, String winner) {
        Game g = getGame(gameName);
        if (g != null) {
            super.gameOver(gameName);
            post(new CText("Game ", PLAIN_COLOUR));
            post(new CText(gameName, BOLD_COLOUR));
            post(new CText(" ends.", PLAIN_COLOUR));
            newLine();
            post(new CText("Winner: ", PLAIN_COLOUR));
            post(new CText(winner, BOLD_COLOUR));
            post(new CText(".", PLAIN_COLOUR));
            newLine();
            for (int i = 0; i < g.numPlayers; i++) {
                playerList.changeMultiColour(g.player[i].name, IDLE_PLAYER_COLOUR);
            }
            playerList.repaint();
            gameList.removeGame(g);
            if (selectedGame == g) { // Must be watching!
                selectGame(g.name);
                Player me = getPlayer(clientName);
                me.isActive = false;
                me.inGame = false;
                me.game = null;
                displayForum(true);
                setMode(MODE_NOT_IN_GAME);
            }
            if (games.size() == 0) {
                setMode(MODE_NO_GAMES);
                selectedGame = null;
            }
        }
    }
    // GAME SELECTION

    public boolean gameSelected(int rowNum) {
        rowNum /= 2;
        if (rowNum >= games.size()) {
            return false;
        }
        if (rowNum < 0) {
            return false;
        }
        if (mode == MODE_NOT_IN_GAME) {
            selectedGame = (Game) (games.elementAt(games.size() - 1 - rowNum));
            setMode(mode);
            return true;
        }
        return false;
    }
    // Given a game, gets the game if the client is in it

    public Game getClientGame(String name, String gameName) {
        Game g = getGame(gameName);
        if (g == null) {
            return null;
        }
        if (g.getPlayer(name) == null) {
            return null;
        } else {
            return g;
        }
    }
// Finds the game that this player is in, null if it isn't in any

    public Game getPlayerGame(Player p) {
        int n = games.size();
        for (int i = 0; i < n; i++) {
            Game g = (Game) (games.elementAt(i));
            if (g.getPlayer(p.name) != null) {
                p.game = g;
                return g;
            }
        }
        return null;
    }
// Finds the game that this player is in, null if it isn't in any

    public Game getPlayerGame(String name) {
        int n = games.size();
        for (int i = 0; i < n; i++) {
            Game g = (Game) (games.elementAt(i));
            if (g.getPlayer(name) != null) {
                return g;
            }
        }
        return null;
    }

    public int getRow(Game g) {
        for (int i = 0; i < games.size(); i++) {
            Game game = (Game) (games.elementAt(i));
            if (game == g) {
                return (games.size() - i - 1) * 2;
            }
        }
        return -1;
    }
    // INCOMING EVENTS

    public boolean isMe(String name) {
        if (name.equals(clientName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMessageChar(char key) {
        if (Character.isDigit(key) || Character.isLetter(key)) {
            return true;
        }
        for (int i = 0; i < Params.MESSAGE_KEYS.length; i++) {
            if (Params.MESSAGE_KEYS[i] == key) {
                return true;
            }
        }
        return false;
    }

    public boolean joinGame(String playerName, String gameName) {
        if (super.joinGame(playerName, gameName)) {
            Game g = getGame(gameName);
            gameList.setRow(getRow(g), g);
            post(new CText(playerName, BOLD_COLOUR));
            post(new CText(" joins game ", PLAIN_COLOUR));
            post(new CText(gameName, BOLD_COLOUR));
            post(new CText(".", PLAIN_COLOUR));
            newLine();
            if (isMe(playerName)) {
                selectGame(gameName);
                if (isMe(g.creator)) {
                    setMode(MODE_CREATED);
                } else {
                    setMode(MODE_IN_NEW);
                }
            } else {
                setMode(mode);
            }
            return true;
        } else {
            return false;
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        char key = e.getKeyChar();
        if (dialogMode != DIALOG_NONE) {
            // Handle dialog box input
            switch (code) {
                case KeyEvent.VK_ESCAPE:
                    buttonPressed(DialogCanvas.CANCEL);
                    break;
                case KeyEvent.VK_ENTER:
                    acceptDialog();
                    break;
                default:
                    dialog.keyPressed(e);
            }
        } else // Non-dialog commands
        if (code == KeyEvent.VK_F1) {
            setDialog(DIALOG_MESSAGE, "Enter Message Text", null);
        } else {
            if (code == KeyEvent.VK_UP) {
                scroll(true);
            } else {
                if (code == KeyEvent.VK_DOWN) {
                    scroll(false);
                } else {
                    if (e.isAltDown()) {
                        switch (Character.toLowerCase(key)) {
                            case 'c':
                                setDialog(DIALOG_NEW_GAME, "Invent a name for this game", null);
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
                                //sendStartGame();
                                break;
                            case 'd':
                                doDropRobot();
                                break;
                            case 'm':
                                setDialog(DIALOG_MESSAGE, "Enter Message Text", null);
                                break;
                            case 's':
                                toggleSound();
                                break;
                            case 'q':
                                frontEnd.quitProgram();
                                break;
                        }
                    }
                }
            }
        }
        e.consume();
    }
    // KeyListener interface -- Ignore Released and Typed

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    // Testing

    public static void main(String[] args) {
        Frame f = new Frame("Inner Demons");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        f.pack();
        f.show();
        f.setSize(dim);
        Insets i = f.getInsets();
        int width = dim.width - i.left - i.right;
        int height = dim.height - i.top - i.bottom;
        igx.shared.Robot[] botList = new igx.shared.Robot[0];
        // ClientForum cf = new ClientForum(botList, "The HiVE", width, height, toolkit);
        // BROKEN TEST CODE
        ClientForum cf = null;
        f.add(cf.mainContainer);
        cf.setToPreferredSize();
        f.validate();
    }

    public void message(String playerName, String text) {
        super.message(playerName, text, Params.MESSAGE_TO_FORUM);
        post(new CText(playerName, MESSAGE_AUTHOR_COLOUR));
        post(new CText(": " + text, MESSAGE_TEXT_COLOUR));
        newLine();
    }

    public void newLine() {
        eventList.newLine();
    }

    public boolean playerQuit(String name) {
        if (isMe(name)) {
            return true;
        }
        super.removePlayer(name);
        playerList.removeText(name);
        post(new CText(name, BOLD_COLOUR));
        post(new CText(" called it a day!", PLAIN_COLOUR));
        newLine();
        return false;
    }

    public void playGame() {
        if (gameUI != null) {
            Player p = getPlayer(server.name);
            //System.out.println("isActive = " + p.isActive + ", game = " + p.game + ", inGame " + p.inGame);
            // Are we _REALLY_ in a game?
            if ((p != null) && p.isActive) {
                card.show(frontEnd.getContainer(), CARD_GAME);
                gameUI.requestFocus();
            }
        }
    }

    public void post(CText text) {
        eventList.addText(text);
    }

    public void post(String text, Color color) {
        post(new CText(text, color));
        newLine();
    }
// Client registration

    public void registerClient() throws IOException {
        Game joinedGame = null;
        server.name = clientName;
        String info = server.receive();
        while (!info.equals(Params.ENDTRANSMISSION)) {
            Player p = new Player(info);
            players.add(p);
            p.isActive = server.receiveBoolean();
            info = server.receive();
        }
        info = server.receive();
        while (!info.equals(Params.ENDTRANSMISSION)) {
            Game g = null;
            boolean inProgress;
            if (server.receive().equals(Params.ACK)) {
                inProgress = true;
            } else {
                inProgress = false;
            }
            int numPlayers = Integer.parseInt(server.receive());
            for (int i = 0; i < numPlayers; i++) {
                String name = server.receive();
                boolean isActive = server.receiveBoolean();
                if (i == 0) {
                    g = new Game(info, name);
                }
                Player p = getPlayer(name);
                if (p == null) { // Not in the forum
                    igx.shared.Robot r = getRobot(name);
                    if (r == null) {
                        p = new Player(name);
                        p.isPresent = false;
                    } else {
                        p = r.toPlayer();
                    }
                }
                p.game = g;
                g.player[i] = p;
            }
            g.inProgress = inProgress;
            g.numPlayers = numPlayers;
            games.addElement(g);
            gameList.addGame(g);
            info = server.receive();
        }
        // Get our game, if we're in one
        Player us = getPlayer(clientName);
        if (server.receive().equals(Params.ACK)) {
            Game g = getGame(server.receive());
            if (g != null) {
                joinedGame = g;
                us.game = joinedGame;
                us.inGame = true;
            }
        } else {
            joinedGame = null;
            us.game = null;
            us.inGame = false;
        }
        // Add players
        for( Player p : players ){
            if (p.isActive) {
                playerList.addText(p.name, ACTIVE_PLAYER_COLOUR);
            } else {
                playerList.addText(p.name, IDLE_PLAYER_COLOUR);
            }
        }
        // Set up status bar
        Vector rowVector = new Vector();
        int middle = (statusBar.width - statusBar.fm.stringWidth(clientName)) / 2;
        rowVector.addElement(new TextElement(clientName, STATUS_COLOUR, middle));
        statusBar.row[STATUS_ROWS / 2] = new TextRow(rowVector);
        statusBar.repaint();
        // Set initial mode
        if (joinedGame != null) {
            selectGame(joinedGame.name);
            if (joinedGame.inProgress) {
                setMode(MODE_IN_PROGRESS);
            } else {
                if (joinedGame.creator.equals(clientName)) {
                    boolean isRobots = false;
                    for (int i = 0; i < joinedGame.numPlayers; i++) {
                        if (!joinedGame.player[i].isHuman) {
                            isRobots = true;
                            break;
                        }
                    }
                    if (isRobots) {
                        setMode(MODE_CREATED_WITH_ROBOTS);
                    } else {
                        setMode(MODE_CREATED);
                    }
                } else {
                    setMode(MODE_IN_NEW);
                }
            }
        } else {
            if (games.size() == 0) {
                setMode(MODE_NO_GAMES);
            } else {
                gameList.manualSelectRow(GameListCanvas.HEADER_ROWS);
                Game latestGame = (Game) (games.elementAt(games.size() - 1));
                selectGame(latestGame.name);
                setMode(MODE_NOT_IN_GAME);
            }
        }
    }

    public boolean removeRobot(String robotName, String gameName) {
        boolean retVal = super.removeRobot(robotName, gameName);
        Game g = getGame(gameName);
        int rowNum = getRow(g);
        gameList.setRow(rowNum, g);
        post(new CText("Robot ", PLAIN_COLOUR));
        post(new CText(robotName, BOLD_COLOUR));
        post(new CText(" is unplugged from game ", PLAIN_COLOUR));
        post(new CText(g.name, BOLD_COLOUR));
        post(new CText(".", PLAIN_COLOUR));
        newLine();
        boolean isRobots = false;
        for (int i = 0; i < g.numPlayers; i++) {
            if (!g.player[i].isHuman) {
                isRobots = true;
                break;
            }
        }
        if (isMe(g.creator)) {
            if (!isRobots) {
                setMode(MODE_CREATED);
            } else {
                setMode(MODE_CREATED_WITH_ROBOTS);
            }
        }
        return retVal;
    }

    /**
     * This method was created in VisualAge.
     *
     * @param up boolean
     */
    public void scroll(boolean up) {
        int currentRow = gameList.selectedRow;
        if (up) {
            gameList.rowSelected(currentRow - 1);
        } else {
            gameList.rowSelected(currentRow + 1);
        }
    }
    // Does the best it can to select the right game

    public void selectGame(String gameName) {
        int n = games.size();
        for (int i = 0; i < n; i++) {
            Game g = (Game) (games.elementAt(i));
            if (g.name.equals(gameName)) {
                gameList.manualSelectRow((games.size() - i - 1) * 2 + GameListCanvas.HEADER_ROWS);
                selectedGame = g;
                return;
            }
        }
        if (n == 0) { // No games
            gameList.manualSelectRow(-1);
            selectedGame = null;
        } else { // Select first game
            gameList.manualSelectRow(GameListCanvas.HEADER_ROWS);
            selectedGame = (Game) (games.elementAt(games.size() - 1));
        }
    }

    public void sendAbandonGame() {
        server.send(Params.FORUM);
        server.send(Params.ABANDONGAME);
    }

    public void sendAbdicateGame() {
        // server.send(Params.FORUM);
        server.send(Params.PLAYERQUITTING);
        server.send(new Integer(Params.QUIT_SIGNAL).toString());
    }

    public void sendJoinGame() {
        server.send(Params.FORUM);
        server.send(Params.JOINGAME);
        server.send(selectedGame.name);
    }

    public void sendGetMaps() {
        if (selectedGame != null) {
            server.send(Params.FORUM);
            server.send(Params.CUSTOMMAP);
            server.send(new Integer(selectedGame.numPlayers).toString());
        } else {
            System.out.println("No selected game?");
        }
    }

    /*
  public void sendStartGame () {
	server.send(Params.FORUM);
	server.send(Params.STARTGAME);
  }
     */
    public void sendWatchGame() {
        server.send(Params.FORUM);
        server.send(Params.WATCHGAME);
        server.send(selectedGame.name);
    }

    public void setBotList(igx.shared.Robot[] botList) {
        this.botList = botList;
        robotMenu = new PopupMenu();
        buttonBar.add(robotMenu);
        // Add random robot option
        MenuItem randomItem = new MenuItem("Random Robot");
        randomItem.setActionCommand("?");
        randomItem.addActionListener(this);
        robotMenu.add(randomItem);
        robotMenu.add("-");
        // Add robot menus
        String type = "";
        Menu menu = new Menu("");
        for (int i = 0; i < botList.length; i++) {
            if (!botList[i].botType.equals(type)) {
                type = botList[i].botType;
                menu = new Menu(type);
                robotMenu.add(menu);
            }
            MenuItem item = new MenuItem(botList[i].name + " (" + botList[i].ranking + ")");
            item.setActionCommand("+" + botList[i].name);
            item.addActionListener(this);
            menu.add(item);
        }
        // Custom robot option
        robotMenu.add("-");
        MenuItem customItem = new MenuItem("Custom Robot...");
        customItem.setActionCommand("!");
        customItem.addActionListener(this);
        robotMenu.add(customItem);
    }

    public void setDialog(int mode, String text, String errorText) {
        dialog.clearDialog();
        dialog.setDialogText(text);
        if (errorText != null) {
            dialog.setErrorText(errorText);
        }
        dialogMode = mode;
        if ((dialogMode == DIALOG_PASSWORD) || (dialogMode == DIALOG_NEW_PASSWORD)) {
            dialog.passwordMode = true;
        }
        mainContainer.requestFocus();
    }

    public void setMode(int mode) {
        this.mode = mode;
        // Always add the standard buttons
        addButton(BUTTON_MESSAGE, 4);
        addButton(BUTTON_SOUND_ON, 5);
        addButton(BUTTON_QUIT, 6);
        // Add buttons based on mode
        switch (mode) {
            case MODE_NO_GAMES:
                addButton(BUTTON_CREATE_GAME, 0);
                break;
            case MODE_NOT_IN_GAME:
                addButton(BUTTON_CREATE_GAME, 0);
                if (selectedGame != null) {
                    if (selectedGame.inProgress) {
                        addButton(BUTTON_WATCH_GAME, 1);
                    } else {
                        addButton(BUTTON_JOIN_GAME, 1);
                    }
                }
                break;
            case MODE_IN_PROGRESS:
                Player p = selectedGame.getPlayer(clientName);
                if ((p != null) && (p.isActive)) {
                    addButton(BUTTON_PLAY_GAME, 0);
                    addButton(BUTTON_ABDICATE_GAME, 1);
                } else {
                    addButton(BUTTON_CONTINUE_WATCHING_PLAY, 0);
                    addButton(BUTTON_QUIT_WATCHING, 1);
                }
                break;
            case MODE_IN_NEW:
                addButton(BUTTON_ABANDON_GAME, 0);
                break;
            case MODE_CREATED:
                if (selectedGame.numPlayers < Params.MAXPLAYERS) {
                    addRobotPoint = addButton(BUTTON_ADD_ROBOT, 0);
                }
                if (selectedGame.numPlayers > 1) {
                    startGamePoint = addButton(BUTTON_START_GAME, 1);
                }
                addButton(BUTTON_ABANDON_GAME, 2);
                break;
            case MODE_CREATED_WITH_ROBOTS:
                if (selectedGame.numPlayers < Params.MAXPLAYERS) {
                    addRobotPoint = addButton(BUTTON_ADD_ROBOT, 0);
                }
                startGamePoint = addButton(BUTTON_START_GAME, 1);
                dropRobotPoint = addButton(BUTTON_DROP_ROBOT, 2);
                addButton(BUTTON_ABANDON_GAME, 3);
                break;
        }
        buttonBar.prepareButtons();
        buttonBar.repaint();
    }

    public void setToPreferredSize() {
        gameList.setSize(gameList.width, gameList.height);
        dialog.setSize(dialog.width, dialog.height);
        playerList.setSize(playerList.width, playerList.height);
        statusBar.setSize(statusBar.width, statusBar.height);
        buttonBar.setSize(buttonBar.width, buttonBar.height);
        eventList.setSize(eventList.width, eventList.height);
        westContainer.setSize(height, height);
        eastContainer.setSize(width - height, height);
        mainContainer.setSize(width, height);
    }

    public Game startNewGame(String gameName,
            String customMap) {
        super.startGame(gameName, customMap);
        Game g = getGame(gameName);
        gameList.setRow(getRow(g), g);
        post(new CText("Game ", PLAIN_COLOUR));
        post(new CText(gameName, BOLD_COLOUR));
        post(new CText(" begins.", PLAIN_COLOUR));
        newLine();
        for (int i = 0; i < g.numPlayers; i++) {
            playerList.changeMultiColour(g.player[i].name, ACTIVE_PLAYER_COLOUR);
        }
        playerList.repaint();
        Game cg = getClientGame(clientName, gameName);
        if (cg != null) {
            setMode(MODE_IN_PROGRESS);
            return cg;
        } else {
            setMode(mode);
            return null;
        }
    }
    // GAME EVENTS

    public void toggleSound() {
        boolean soundMode = !(frontEnd.getSoundMode());
        frontEnd.setSoundMode(soundMode);
        if (soundMode) {
            frontEnd.play(Params.AU_SOUNDON);
            post("Sound is on. Hit <CTRL>-<S> again to turn it off.", PLAIN_COLOUR);
        } else {
            post("Sound is off.", PLAIN_COLOUR);
        }
    }

    public void watchGame(String playerName, String gameName) {
        super.watchGame(playerName, gameName);
    }
}
