package igx.stats.web;

import net.sf.hibernate.Session;
import igx.stats.Game;
import com.jrefinery.chart.JFreeChart;
import java.util.*;
import igx.stats.*;
import com.jrefinery.data.*;
import com.jrefinery.chart.*;
import com.jrefinery.chart.plot.*;
import igx.bots.Constants;
import com.jrefinery.chart.renderer.XYItemRenderer;
import java.awt.Paint;
import java.awt.Color;
import igx.shared.Params;

/**
 *  <p>
 *
 *  Title: IGX</p> <p>
 *
 *  Description: Used as a one stop place to generate charts. One stop is a
 *  stupid thing to say, I apologize.</p> <p>
 *
 *  Copyright: Copyright (c) 2003</p> <p>
 *
 *  Company: </p>
 *
 *@author     Matt Hall, John Watkinson
 *@created    March 8, 2003
 *@version    1.0
 */

public class ChartHelper {

    /**
     *  Constructor for the ChartHelper object
     */
    public ChartHelper() { }


    /**
     *  Generate a basic score vs. time graph for a game
     *
     *@param  data         Description of the Parameter
     *@param  gameToGraph  Description of the Parameter
     *@return              The gameScoreGraph value
     */
    public static JFreeChart getGameScoreGraph(Game game) {
        // Make the data series objects for each player
        Iterator players = game.getPlayers().iterator();
        HashMap playerSeries = new HashMap();
        while (players.hasNext()) {
            Player player = (Player) players.next();
            XYSeries series = new XYSeries(player.getName());
            playerSeries.put(player.getName(), series);
        }

        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        Set timeStateSet = game.getTimeStates();
        Iterator iter = timeStateSet.iterator();
        double maxTime = 0;
        while (iter.hasNext()) {
            TimeState tState = (TimeState) iter.next();
            XYSeries series = (XYSeries) playerSeries.get(tState.getPlayer().getName());
            double time = new Double(tState.getTime()).doubleValue();
            series.add(time, new Double(tState.getScore()).doubleValue());
            if (time > maxTime) {
                maxTime = time;
            }
        }

        // Put the players in the dataset in the order the database loaded them
        ArrayList series = new ArrayList();
        players = game.getPlayers().iterator();
        while (players.hasNext()) {
            Player player = (Player) players.next();
            series.add(playerSeries.get(player.getName()));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        for (int i = 0; i < series.size(); i++) {
            dataset.addSeries((XYSeries) series.get(i));
        }
        JFreeChart chart = ChartFactory.createLineXYChart("Game " + game.getTime(), "Time", "Score",
                dataset, true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.getDomainAxis().setMaximumAxisValue(maxTime);
        // plot.setForegroundAlpha(0.5f);

        XYItemRenderer render = plot.getRenderer();
        int numSeries = playerSeries.size();
        for (int i=0; i<numSeries; i++) {
            render.setSeriesPaint(i, (Paint) Params.PLAYERCOLOR[i]);
        }

        return chart;
    }

    public static JFreeChart getNumberOfPlayersPieChart(Player player) {
        Iterator games = player.getGames().iterator();
        DefaultPieDataset dataset = new DefaultPieDataset();
        while (games.hasNext()) {
            Game game = (Game) games.next();
            Set players = game.getPlayers();
            String label = players.size() + " Players";
            Number currval = dataset.getValue(label);
            if (currval == null) {
                dataset.setValue(label, 1d);
            } else {
                dataset.setValue(label, currval.doubleValue() + 1d);
            }
        }

        JFreeChart chart = ChartFactory.createPie3DChart("Number of Players", dataset,
                true, true, false);

        Pie3DPlot plot = (Pie3DPlot) chart.getPlot();
        plot.setForegroundAlpha(0.60f);
        plot.setInteriorGap(0.4);

        return chart;
    }

    public static JFreeChart getNumberOfHumanPlayersPieChart(Player player) {
        Iterator games = player.getGames().iterator();
        DefaultPieDataset dataset = new DefaultPieDataset();
        while (games.hasNext()) {
            Game game = (Game) games.next();
            Iterator players = game.getPlayers().iterator();
            int humanCount = 0;
            while (players.hasNext()) {
                Player p = (Player) players.next();
                if (!p.isRobot()) {
                    humanCount++;
                }
            }
            if (!player.isRobot()) {
                humanCount--;
            }
            String label = humanCount + " Humans";
            Number currval = dataset.getValue(label);
            if (currval == null) {
                dataset.setValue(label, 1d);
            } else {
                dataset.setValue(label, currval.doubleValue() + 1d);
            }
        }

        JFreeChart chart = ChartFactory.createPie3DChart("Human Players in Games", dataset,
                true, true, false);

        Pie3DPlot plot = (Pie3DPlot) chart.getPlot();
        plot.setForegroundAlpha(0.60f);
        plot.setInteriorGap(0.4);

        return chart;
    }

    public static JFreeChart getFinalGamePieChart(Game game, String pieType) {
        String graphTitle = "";
        if (ChartServlet.PIE_PLANETS.equals(pieType)) {
            graphTitle = "Planets Owned";
        } else if (ChartServlet.PIE_SHIPS.equals(pieType)) {
            graphTitle = "Fleet Size";
        }
        List finalStates = StatsHelper.getFinalGameTimeStateList(game);
        DefaultPieDataset dataset = new DefaultPieDataset();
        HashMap timeHash = new HashMap();
        for (int i=0; i<finalStates.size(); i++) {
            TimeState state = (TimeState) finalStates.get(i);
            // Put the timestates in a hash so I can get them out in the correct
            // order later
            timeHash.put(state.getPlayer(), state);
        }

        Iterator players = game.getPlayers().iterator();
        while (players.hasNext()) {
            Player player = (Player) players.next();
            TimeState state = (TimeState) timeHash.get(player);

            if (ChartServlet.PIE_PLANETS.equals(pieType)) {
                dataset.setValue(state.getPlayer().getName(), new Double(state.getNumPlanets()));
            } else if (ChartServlet.PIE_SHIPS.equals(pieType)) {
                dataset.setValue(state.getPlayer().getName(), new Double(state.getNumShips()));
            }
        }

        JFreeChart chart = ChartFactory.createPie3DChart(graphTitle, dataset,
                true, true, false);

        Pie3DPlot plot = (Pie3DPlot) chart.getPlot();
        plot.setForegroundAlpha(0.60f);
        plot.setInteriorGap(0.4);
        for (int i=0; i<dataset.getItemCount(); i++) {
            plot.setPaint(i, (Paint) Params.PLAYERCOLOR[i]);
        }

        return chart;
    }

    public static JFreeChart getPlayerWinsStackedChart(Player player) {
        Iterator games = player.getGames().iterator();
        double data[][] = new double[Constants.MAXIMUM_PLAYERS][Constants.MAXIMUM_PLAYERS];
        for (int i=0; i<Constants.MAXIMUM_PLAYERS; i++) {
            for (int j=0; j<Constants.MAXIMUM_PLAYERS; j++) {
                data[i][j] = 0;
            }
        }

        /** Add the player placement data to the array for the chart.
         *  However, if two players have the same score then make them have
         *  the same placement in the game.  (All 0 score players should come
         *  in the same place in the game.
         */

        while (games.hasNext()) {
            int place = -1;
            int lastScore = Integer.MAX_VALUE;
            Game game = (Game) games.next();
            Set players = game.getPlayers();
            List finalStates = StatsHelper.getFinalGameTimeStateList(game);
            for (int i=0; i<finalStates.size(); i++) {
                TimeState ts = (TimeState) finalStates.get(i);
                if (ts.getScore() < lastScore) {
                    // If this score is lower than the last one, then this is a
                    // lower placed player
                    place++;
                }
                lastScore = ts.getScore();

                if (ts.getPlayer() == player) {
                    data[place][players.size()-1]++;
                    break;
                }
            }

        }

        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
                "", "", data);

        JFreeChart chart = ChartFactory.createVerticalBarChart3D(
                                                  "Player Record",  // chart title
                                                  "# Players in Game",      // domain axis label
                                                  "Wins",         // range axis label
                                                  dataset,         // data
                                                  true,            // include legend
                                                  true,            // tooltips
                                                  false            // urls
                                              );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setForegroundAlpha(0.80f);

        return chart;
    }
}
