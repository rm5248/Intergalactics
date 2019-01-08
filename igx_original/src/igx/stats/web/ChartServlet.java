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
import com.jrefinery.chart.entity.EntityCollection;
import com.jrefinery.chart.entity.StandardEntityCollection;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;

/**
 * <p>Title: IGX</p>
 * <p>Description: Provides charts as images</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Matt Hall, John Watkinson
 * @version 1.0
 */

public class ChartServlet extends BaseStatsServlet {

    public static final String ACTION_CREATE_CHART = "creategame";
    public static final String ACTION_CREATE_OPPONENT_CHART = "opponentchart";
    public static final String ACTION_CREATE_HUMAN_CHART = "humanchart";
    public static final String ACTION_CREATE_GAME_PIE = "game_pie";
    public static final String ACTION_CREATE_WINS_CHART = "wins_stack";
    public static final String ACTION_CREATE_SCREENSHOT = "screenshot";
    public static final String ACTION_CREATE_GAME_IMAGEMAP = "game_imagemap";

    public static final String PIE_TYPE = "pie_type";
    public static final String PIE_PLANETS = "planet_pie";
    public static final String PIE_SHIPS = "ships_pie";

    //Process the HTTP Get request
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(StatsServlet.CONTENT_TYPE);
        String action = request.getParameter(StatsServlet.PARAM_ACTION);
        boolean forwardToJsp = true;

        try {
            Session data = SessionTable.getSessionTable().getSession();
            String jspName = "error.jsp";

            if (ACTION_CREATE_CHART.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Long gameId = new Long(request.getParameter(StatsServlet.PARAM_GAMEID));
                Game game = (Game) data.load(Game.class, gameId);

                JFreeChart chart = ChartHelper.getGameScoreGraph(game);
                BufferedImage chartImage = chart.createBufferedImage(525, 525);

                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, chartImage);

                forwardToJsp = false;
            } else if (ACTION_CREATE_OPPONENT_CHART.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Player player = StatsHelper.findPlayer(request.getParameter(StatsServlet.PARAM_PLAYER_NAME));

                JFreeChart chart = ChartHelper.getNumberOfPlayersPieChart(player);
                BufferedImage chartImage = chart.createBufferedImage(250, 250);

                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, chartImage);

                forwardToJsp = false;
            } else if (ACTION_CREATE_HUMAN_CHART.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Player player = StatsHelper.findPlayer(request.getParameter(StatsServlet.PARAM_PLAYER_NAME));

                JFreeChart chart = ChartHelper.getNumberOfHumanPlayersPieChart(player);
                BufferedImage chartImage = chart.createBufferedImage(250, 250);

                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, chartImage);

                forwardToJsp = false;
            } else if (ACTION_CREATE_GAME_PIE.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Long gameId = new Long(request.getParameter(StatsServlet.PARAM_GAMEID));
                Game game = (Game) data.load(Game.class, gameId);
                String pieType = (String) request.getParameter(PIE_TYPE);

                JFreeChart chart = ChartHelper.getFinalGamePieChart(game, pieType);
                BufferedImage chartImage = chart.createBufferedImage(250, 250);

                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, chartImage);

                forwardToJsp = false;
            } else if (ACTION_CREATE_WINS_CHART.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Player player = StatsHelper.findPlayer(request.getParameter(StatsServlet.PARAM_PLAYER_NAME));

                JFreeChart chart = ChartHelper.getPlayerWinsStackedChart(player);
                BufferedImage chartImage = chart.createBufferedImage(504, 250);

                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, chartImage);

                forwardToJsp = false;
            } else if (ACTION_CREATE_SCREENSHOT.equals(action)) {
                response.setContentType(StatsServlet.CONTENT_TYPE_JPEG);
                Long gameId = new Long(request.getParameter(StatsServlet.PARAM_GAMEID));
                int turn = Integer.parseInt(request.getParameter(StatsServlet.PARAM_TURN));
                int galaxySize = Integer.parseInt(request.getParameter(StatsServlet.PARAM_GALAXY_SIZE));
                Game game = (Game) SessionTable.getSessionTable().getSession().load(Game.class, gameId);
                List planetList = StatsHelper.getPlanetUpdatesForGameAndTurn(game, turn);
                List attackList = StatsHelper.getAttacksForGameAndTurn(game, turn);
                Set playerSet = game.getPlayers();
                PlanetUpdate[] pupdates = (PlanetUpdate[])planetList.toArray(new PlanetUpdate[0]);
                Attack[] attacks = (Attack[])attackList.toArray(new Attack[0]);
                Player[] players = (Player[])playerSet.toArray(new Player[0]);
                GalaxyPainter painter = new GalaxyPainter(pupdates, attacks, players, galaxySize);
                BufferedImage image = painter.getImage();
                ServletOutputStream sos = response.getOutputStream();
                ChartUtilities.writeBufferedImageAsPNG(sos, image);

                forwardToJsp = false;
            } else if (ACTION_CREATE_GAME_IMAGEMAP.equals(action)) {

                Long gameId = new Long(request.getParameter(StatsServlet.PARAM_GAMEID));
                Game game = (Game) SessionTable.getSessionTable().getSession().load(Game.class, gameId);

                JFreeChart chart = ChartHelper.getGameScoreGraph(game);
                PrintWriter pw = response.getWriter();
                double maxH = chart.getXYPlot().getDomainAxis().getMaximumAxisValue();
                double maxV = chart.getXYPlot().getRangeAxis().getMaximumAxisValue();
                int minHdisp = 60;  int maxHdisp = 515;
                int minVdisp = 36;  int maxVdisp = 460;
                double currTurn = 0;
                double incAmt = maxH/(maxHdisp - minHdisp);

                pw.write("<map name='gameMap'>");
                pw.write("<area shape='default' nohref>");
                for (int i=minHdisp; i<maxHdisp; i++) {
                    int turn = Math.round(Math.round(currTurn));
                    String coords = i+","+minVdisp+","+(i+1)+","+maxVdisp;
                    String rect = "<area SHAPE=\"RECT\" COORDS=\""+coords+"\" href=\"/stats?action=screenshot&gameId="+game.getId()+"&size=600&turn="+turn+"\">";
                    currTurn += incAmt;
                    pw.write(rect);
                }
                // pw.write("<area SHAPE=\"RECT\" COORDS=\"100,100,500,500\" href=\"/stats?action=screenshot&gameId="+game.getId()+"&size=600&turn="+0+"\" alt=\"SKLF\">");
                pw.write("</map>");

                /*EntityCollection collection = new StandardEntityCollection();
                ChartRenderingInfo rInfo = new ChartRenderingInfo();
                chart.getXYPlot().draw(chart.createBufferedImage(500,500).createGraphics(),
                                            new Rectangle(500,500), rInfo);

                ChartUtilities.writeImageMap(sos, "gameMap", rInfo);*/
                forwardToJsp = false;
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

