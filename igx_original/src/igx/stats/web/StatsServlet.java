package igx.stats.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import net.sf.hibernate.Session;
import igx.stats.Player;
import igx.stats.*;
import com.jrefinery.data.*;
import com.jrefinery.chart.*;
import java.awt.image.BufferedImage;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;

/**
 * <p>Title: IGX</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Matt Hall, John Watkinson
 * @version 1.0
 */

public class StatsServlet extends BaseStatsServlet {
    public static final String CONTENT_TYPE = "text/html";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";

    public static final String PARAM_ACTION = "action";

    public static final String PARAM_PLAYER_NAME = "player_name";
    public static final String PARAM_GAMEID = "gameId";
    public static final String PARAM_TURN = "turn";
    public static final String PARAM_GALAXY_SIZE = "size";

    public static final String ACTION_PLAYER_LIST = "playerlist";
    public static final String ACTION_VIEW_PLAYER = "viewplayer";
    public static final String ACTION_VIEW_GAME = "viewgame";
    public static final String ACTION_LAST_GAMES = "lastgames";
    public static final String ACTION_SAVE_GAME = "savegame";
    public static final String ACTION_SCREENSHOT = "screenshot";

    public static final String ATTRIB_PLAYERS = "player_list";
    public static final String ATTRIB_GAMES = "games_list";
    public static final String ATTRIB_CHART = "chart";
    public static final String ATTRIB_FINAL_STATES = "final_states";
    public static final String ATTRIB_LAST_TURN = "last_turn";

    public static final String JSP_PLAYER_LIST = "playerList.jsp";
    public static final String JSP_PLAYER_VIEW = "playerView.jsp";
    public static final String JSP_GAME_VIEW = "gameView.jsp";
    public static final String JSP_GAME_LIST = "gameList.jsp";
    public static final String JSP_SCREENSHOT = "screenshot.jsp";

    //Process the HTTP Get request
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        String action = request.getParameter(PARAM_ACTION);
        boolean forwardToJsp = true;

        try {
            String jspName = "error.jsp";

            if (ACTION_PLAYER_LIST.equals(action)) {
                List players = StatsHelper.findAllPlayers();
                request.setAttribute(ATTRIB_PLAYERS, players);
                jspName = JSP_PLAYER_LIST;
            } else if (ACTION_VIEW_PLAYER.equals(action)) {
                String playerName = (String) request.getParameter(PARAM_PLAYER_NAME);
                Player player = StatsHelper.findPlayer(playerName);
                request.setAttribute(ATTRIB_GAMES,player.getGames());
                request.setAttribute(ATTRIB_PLAYERS,player);
                jspName = JSP_PLAYER_VIEW;
            } else if (ACTION_VIEW_GAME.equals(action)) {
                Long gameId = new Long(request.getParameter(PARAM_GAMEID));
                Game game = (Game) SessionTable.getSessionTable().getSession().load(Game.class, gameId);
                List finalStates = (List) StatsHelper.getFinalGameTimeStateList(game);

                request.setAttribute(ATTRIB_FINAL_STATES, finalStates);
                request.setAttribute(ATTRIB_GAMES, game);
                jspName = JSP_GAME_VIEW;
            } else if (ACTION_LAST_GAMES.equals(action)) {
                List games = StatsHelper.getLast24HoursGames();

                request.setAttribute(ATTRIB_GAMES, games);
                jspName = JSP_GAME_LIST;
            } else if (ACTION_SAVE_GAME.equals(action)) {
                Long gameId = new Long(request.getParameter(PARAM_GAMEID));
                Game game = (Game) SessionTable.getSessionTable().getSession().load(Game.class, gameId);

                game.setSave(true);

                List finalStates = (List) StatsHelper.getFinalGameTimeStateList(game);

                request.setAttribute(ATTRIB_FINAL_STATES, finalStates);
                request.setAttribute(ATTRIB_GAMES, game);

                SessionTable.getSessionTable().getSession().flush();

                jspName = JSP_GAME_VIEW;
            } else if (ACTION_SCREENSHOT.equals(action)) {
                Long gameId = new Long(request.getParameter(PARAM_GAMEID));
                Game game = (Game) SessionTable.getSessionTable().getSession().load(Game.class, gameId);
                int lastTurn = StatsHelper.getLastTurnNumberForGame(game);
                request.setAttribute(ATTRIB_GAMES, game);
                request.setAttribute(ATTRIB_LAST_TURN, new Integer(lastTurn));

                jspName = JSP_SCREENSHOT;
            }
            if (forwardToJsp) {
                request.getRequestDispatcher(jspName).forward(request, response);
            }
            SessionTable.getSessionTable().getSession().close();
            SessionTable.getSessionTable().clearSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

