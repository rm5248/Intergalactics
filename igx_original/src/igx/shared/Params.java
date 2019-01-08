package igx.shared;

// Params.java
// Params class : Global constants
// v3 Beta version -- PORT 1542

import java.awt.*;

public class Params
{
  // Name of Game
  public static final String NAME = "intergalactics";

  // Server name
  public static final String SERVER_NAME = "the HiVE";

  // Version Number
  public static final String VERSION = "3.8";

  // Major version sig digits
  public static final int MAJOR_VERSION_LENGTH = 3;

  // Debug Mode - Hardly used
  public static final boolean DEBUG = false;

  // ICON for igx
  public static final String ICON = "hive.gif";

  // Size of map
  public static final int MAPX = 16;
  public static final int MAPY = MAPX;

  // Size of font used on Map, Intel and Console windows

  public static final int MAPFONTSIZE     = 12;
  public static final int INTELFONTSIZE   = 12;
  public static final int CONSOLEFONTSIZE = 14;
  public static final int NEWSFONTSIZE    = 12;

  // Size of main display in start frame
  public static final Dimension STARTFRAMESIZE = new Dimension(380,150);

  // Specifications of News window
  // Number of visible rows
  public static final int NEWSROWS = 10;
  // Number of columns
  public static final int NEWSCOLUMNS = 40;

  // Number of Planets
  public static final int PLANETS = 36;

  public static final int SQUARED_HOME_PLANET_DISTANCE_TOLERANCE = 16;

  // Default font face
  public static final String DEFAULT_FONT = "Helvetica";

  // Maximum number of games in the forum
  public static final int MAX_GAMES = 8;

  // Characters representing the stars (REDUNDANT : Planet has static methods)
  public static final char[] PLANETCHAR =
	{'A','B','C','D','E','F','G','H','I','J','K','L','M',
	 'N','o','P','Q','R','S','T','U','V','W','X','Y','Z',
	 '0','1','2','3','4','5','6','7','8','9'};

  // Empty space character
  public static final char SPACE = '.';

  // Maximum number of players and neutral player number
  public static final int MAXPLAYERS = 9;
  public static final int NEUTRAL = MAXPLAYERS;
  public static final int MESSAGE_TO_ALL = MAXPLAYERS;
  public static final int MESSAGE_TO_FORUM = MAXPLAYERS+1;

  // Maximum length of name for each player
  public static final int MAXNAME = 8;

  // Number of turns before a game is ranked
  public static final int MINGAMELENGTH = 10;

  // Colour of each player, last color is neutral
  public static final Color[] PLAYERCOLOR =
	{new Color(255,  0,  0),
	 new Color(120,255,  0),
	 new Color( 64, 64,255),
	 new Color(255,255,  0),
	 new Color(  0,193,154),
	 new Color(186,134, 75),
	 new Color(154, 21,225),
	 new Color(255, 0, 255),
	 new Color(160,255,160),
	 Color.gray};

  // Neutral colour, here for convenience
  public static final Color NEUTRALCOLOR = PLAYERCOLOR[MAXPLAYERS];

  // Empty space colour on map
  public static final Color EMPTYSPACECOLOR = Color.darkGray;

  // Colour of other text
  public static final Color TEXTCOLOR = Color.lightGray;

  // Quit signal
  public static final int QUIT_SIGNAL = 0;
  // Ready to Quit signal
  public static final int READY_SIGNAL = 1;
  // Don't Quit signal
  public static final int DONT_SIGNAL = 2;

  // Min/Max initial production on a planet
  public static final int MINPROD =  0;
  public static final int MAXPROD = 10;

  // Min/Max initial attack ratio of a planet
  public static final int MINRATIO = 20;
  public static final int MAXRATIO = 49;

  // Min/Max defence bonus
  public static final int MINDEFENCEBONUS =  0;
  public static final int MAXDEFENCEBONUS = 30;

  // Per Cent of attacking/defending ships that attack
  public static final int ATTACKSTRENGTH = 25;
  public static final int DEFENDSTRENGTH = 25;

  // Min/Max initial ships defending neutral planets
  public static final int MINNEUTRALSHIPS =  1;
  public static final int MAXNEUTRALSHIPS = 10;

  // Inital production, ratio and initial ships on home planets
  public static final int HOMEPLANETPROD  = 10;
  public static final int HOMEPLANETRATIO = 40;
  public static final int HOMEPLANETSHIPS = 10;

  // Chance of special events
  public static final int EVENTCHANCE = 50;

  // Weighting of special events -- make these add to 1000 --
  public static final int PRODINCREASE         = 141; // Increase production
  public static final int RATIOINCREASE        = 141; // Increase ratio
  public static final int SPECIALPRODINCREASE  = 51; // Special production increase
  public static final int SPECIALRATIOINCREASE = 51; // Special ratio increase
  public static final int PRODDECREASE         = 141; // Decrease production
  public static final int RATIODECREASE        = 141; // Decrease ratio
  public static final int SPECIALPRODDECREASE  = 51; // Special production decrease
  public static final int SPECIALRATIODECREASE = 51; // Special ratio decrease
    public static final int REVOLT               = 61; // Revolt
    public static final int RELOCATE             = 170; // Planet relocation
  public static final int CRAZY                = 1; // Crazy Special Event

  // Weighting of crazy events -- make these add to 100 --
  public static final int ALLFLEETSARRIVE      = 15;
  public static final int PLANETSRELOCATE      = 23;
  public static final int REVERSEPRODUCTION    = 15;
  public static final int ALLFLEETSDESTROYED   = 15;
  public static final int ALLDOCKEDDESTROYED   = 15;
  public static final int REROLLPRODUCTION     = 15;
  public static final int ARMAGEDDON           = 1;
  public static final int BABYDUCK             = 1;

  // End-game planet disappearance
  public static final int BLACKHOLETURN = 100;
  public static final int BLACKHOLEPERIOD = 5;

  // Results of special events
	// Min/Max production increase
	public static final int MINPRODUP       = 1;
	public static final int MAXPRODUP       = 5;
	// Min/Max ratio increase
	public static final int MINRATIOUP      = 6;
	public static final int MAXRATIOUP      = 25;
	// Special production max
	public static final int SPECIALPRODMAX  = 15;
	// Special ratio max
	public static final int SPECIALRATIOMAX = 60;
	// Min/Max production decrease
	public static final int MINPRODDOWN     = 1;
	public static final int MAXPRODDOWN     = 5;
	// Min/Max ratio decrease
	public static final int MINRATIODOWN    = 6;
	public static final int MAXRATIODOWN    = 25;
	// Special production min
	public static final int SPECIALPRODMIN  = 0;
	// Special ratio min
	public static final int SPECIALRATIOMIN = 1;

  // Number of segments per turn (Planets produce every turn)
  public static final int SEGMENTS = 20;

  // Time in milliseconds between segments
  public static int TIMESLICE = 2500;

  // Time delay before game begins (milliseconds)
  public static final int STARTDELAY = 5000;

  // Distance a fleet moves in one segment
  public static final float FLEETSPEED = 0.100001f;

  // Distance a fleet moves in one turn (synchronize with above FLEETSPEED)
  public static final int TURNSPEED = 2;

  // Generic yes/no strings
  public static final String ACK  = "[";
  public static final String NACK = "]";

  public static final String NEW_ALIAS = "{";
  public static final String OLD_ALIAS = "}";

  // Pregame
  public static final String FORUM = "+";

  // Turn update string
  public static final String UPDATE = "|";

  // Communication terminating string
  public static final String ENDTRANSMISSION = "~";

  // Special string indicating that a player is quitting
  public static final String PLAYERQUITTING = "!";

  // Special string indicating a message
  public static final String SENDMESSAGE = "@";

  // Special string indicating new game
  public static final String NEWGAME = "#";

  // Special string indicating join game
  public static final String JOINGAME = "$";

  // Special string indicating abandon game
  public static final String ABANDONGAME = "%";

  // Special string indicating a new player has arrived
  public static final String PLAYERARRIVED = "^";

  // Start game string
  public static final String STARTGAME = "&";

  // Add computer player
  public static final String ADDCOMPUTERPLAYER = "*";

  // Remove computer player
  public static final String REMOVECOMPUTERPLAYER = "\\";

  // Game ended
  public static final String GAMEENDED = "(";

  // Watch Game
  public static final String WATCHGAME = ")";

  // Add Custom Computer Player
  public static final String ADDCUSTOMCOMPUTERPLAYER = "?";

   // Random map
   public static final String RANDOMMAP = "<";

   // Custom map
   public static final String CUSTOMMAP = ">";

  // Port number over which communication occurs (1024 <= PORTNUM <2048)
  public static final int PORTNUM = 1542;

  // Non-letter, non-digit keys allowable in a message
  public static final char[] MESSAGE_KEYS =
  {'`', '~', '\'', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '/',
   '=', '+', '|', '\\', '[', ']', '{', '}', ';', ':', '"', ',', '<', '.', '>', '?', ' '};

  // AUDIO FILENAMES

  // PreGame sounds
  public static final String AU_SOUNDON = "soundon.au";//"crazy.au";
  public static final String AU_ARRIVED = "arrive.au";
  //**New Sounds**
  /*  public static final String AU_PLAYER_STARTING = "youstart.au";
	  public static final String AU_GAME_STARTING = "gamestart.au";
	  public static final String AU_CREATE = "create.au";
	  public static final String AU_JOIN = "join.au";
	  public static final String AU_GAME_ABANDON = "gamegone.au";
  public static final String AU_PLAYER_ABANDON = "noplayer.au";
  */
  // Game sounds
  //public static final String AU_ATTACK = "enemyCombat.au";
  public static final String AU_REPELLED = "repelled.au";
  public static final String AU_INVADE = "victory.au";
  public static final String AU_EVENT = "event.au";
  public static final String AU_CRAZY = "crazy.au";
  public static final String AU_QUIT = "quit.au";
  public static final String AU_MESSAGE = "message.au";
  public static final String AU_ATTACK_YOU = "attack.au";
  public static final String AU_YOU_ATTACK = "combat.au";
  public static final String AU_YOU_LOSE = "lose.au";
  public static final String AU_YOU_INVADED = "invaded.au";
  public static final String AU_REINFORCE = "reinforce.au";
  public static final String AU_YOU_QUIT = "youquit.au";
  // public static final String AU_RESOLVED = "resolve.au";
  public static final String AU_BLACKHOLE = "nothing.au";
  public static final String AU_PLAYER_LEFT = "left.au";

  // Computer player names
  public static final int NUMBER_OF_COMPUTER_NAMES = 10;

  public static final String[] COMPUTER_NAMES = {
	"Worker",
	"Drone",
	"Queen",
	"Larva",
	"Pupa",
	"Honey",
	"Stinger",
	"Bumble",
	"Wasp",
	"Hornet"};

  // Max number of Bulletins on bulletin board
  public static final int BULLETINS = 50;

  // Bulletin File
  public static final String BULLETIN_FILE = "bulletin.txt";

  // Scoring System
  public static final int PRODUCTION_VALUE = 10;
  public static final int SHIP_VALUE = 3;
  public static final int RATIO_VALUE = 1;

  // Ranking System
  public static final String RANKING_FILE = "rankings.dat";
  public static final int STANDARD_RANKING = 1500;
  public static final int MAX_DIFFERENCE = 350;
  public static final int MAX_SCORE = 32;
  public static final int MIN_GAMES_BEFORE_RANKED = 5;
}
