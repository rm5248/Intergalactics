package igx.bots;

// Constants.java 

import igx.shared.Params;

/**
 * Contains all the constants in igx that could be relevant to your 'bot. These constants will synchronize
 * with the server's constants, so don't go using any magic numbers.
 * <P>
 * If any of these constants seem confusing, then maybe you don't fully understand the igx game engine.
 * Is it the case? Well, then if not for your own sake, but for the sake of your poor, unenlightened 'bot,
 * read up on it in the igx
 * <A HREF="http://www.cs.utoronto.ca/~jw/iCONv2/manual.html">Player's Guide</A>.
 * Any further inquiries regarding the igx game engine can be directed to 
 * <A HREF="mailto:john.watkinson@utoronto.ca">us</A> here at HiVE Software. 
 */

public class Constants
{
  /**
   * Robot API version.
   */
  public static final String VERSION = "1.1";

  /**
   * Map width.
   */
  public static final int MAP_WIDTH = Params.MAPX;

  /**
   * Map height.
   */
  public static final int MAP_HEIGHT = Params.MAPY;

  /**
   * Maximum number of players.
   */
  public static final int MAXIMUM_PLAYERS = Params.MAXPLAYERS;

  /**
   * Neutral player's number.
   */
  public static final int NEUTRAL = MAXIMUM_PLAYERS;

  /**
   * Sending a message to this player number will result in the message being sent to ALL players.
   */
  public static final int MESSAGE_TO_ALL = NEUTRAL;

  /**
   * Number of planets.
   */
  public static final int PLANETS = Params.PLANETS;

  /**
   * Number of segments per turn.
   */
  public static final int SEGMENTS = Params.SEGMENTS;

  /**
   * Distance travelled by fleets per segment.
   */
  public static final float FLEET_SPEED = Params.FLEETSPEED;

  /**
   * Empty space character on the map.
   */
  public static final int EMPTY_SPACE = -1;

  /**
   * Minimum initial production on a planet.
   */
  public static final int MIN_PRODUCTION =  Params.MINPROD;

  /**
   * Maximum initial production on a planet.
   */
  public static final int MAX_PRODUCTION = Params.MAXPROD;

  /**
   * Minimum initial attack ratio of a planet.
   */
  public static final int MIN_RATIO = Params.MINRATIO;

  /**
   * Maximum initial attack ratio of a planet.
   */
  public static final int MAX_RATIO = Params.MAXRATIO;

  /**
   * Minimum defence bonus a planet receives when being attacked.
   */
  public static final int MIN_DEFENCE_BONUS = Params.MINDEFENCEBONUS;

  /**
   * Maximum defence bonus a planet receives when being attacked.
   */   
  public static final int MAX_DEFENCE_BONUS = Params.MAXDEFENCEBONUS;

  /**
   * Minimum initial ships defending neutral planets.
   */
  public static final int MIN_NEUTRAL_SHIPS = Params.MINNEUTRALSHIPS;

  /**
   * Maximum initial ships defending neutral planets.
   */
  public static final int MAX_NEUTRAL_SHIPS = Params.MAXNEUTRALSHIPS;

  /**
   * Inital production on home planets.
   */
  public static final int HOME_PRODUCTION = Params.HOMEPLANETPROD;

  /**
   * Inital ratio on home planets.
   */
  public static final int HOME_RATIO = Params.HOMEPLANETRATIO;

  /**
   * Initial ships on home planets.
   */
  public static final int HOME_SHIPS = Params.HOMEPLANETSHIPS;

  /**
   * Special production maximum. This is the highest production a planet will ever achieve 
   * due to random events.
   */
  public static final int SPECIAL_MAX_PRODUCTION = Params.SPECIALPRODMAX;

  /**
   * Special ratio maximum. This is the highest ratio a planet will ever achieve due to random events.
   */
  public static final int SPECIAL_MAX_RATIO = Params.SPECIALRATIOMAX;

}