// RobotLadder.java
package igx.bots.arena;

import igx.bots.*;
import java.util.*;
import java.io.*;

public class RobotLadder {

    public static final String BOT_FILE_NAME = "botWars.txt";
    public static final String TABLE_NAME = "robotRankings.html";
    public static final String TABLE_HEADER = "robotHeader.html";
    public static final String TABLE_MIDDER = "robotMidder.html";
    public static final String TABLE_FOOTER = "robotFooter.html";
    public static final int NUMBER_OF_EACH_BOT = 1;

    static Robot[] robotList;
    static int maxPlayers = 2;
    static int segments = 800;
    static int numRobots;
    static int numberOfGames = 0;
    static long seed;

    public static void complain(String complaint) {
        System.out.println(complaint);
        System.out.println("Usage:");
        System.out.println("  java igx.bots.arena.RobotLadder players segments");
        System.out.println("(Where 'players' is the number of players in the largest game)");
        System.out.println("(and 'segments' is the number of segments per game)");
        System.exit(1);
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the intergalactics robot ladder...");
        if (args.length != 2) {
            complain("2 args required.");
        }
        try {
            maxPlayers = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            complain("Integer expected.");
        }
        try {
            segments = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            complain("Integer expected.");
        }
        robotList = generateBotList();
        numRobots = robotList.length;
        if (numRobots < maxPlayers) {
            complain("You need enough bots to fill the biggest game!");
        }
        while (true) {
            for (int i = 2; i <= maxPlayers; i++) {
                int[] spot = new int[i];
                for (int j = 0; j < i; j++) {
                    spot[j] = -1;
                }
                setSeed();
                playGames(spot, 0, 0, 0);
            }
            System.out.println("Total games played: " + numberOfGames);
            for (int i = 0; i < numRobots; i++) {
                System.out.println(robotList[i].getName() + " - " + robotList[i].makeStats(numberOfGames));
            }
            makeTable();
        }
        //System.out.println(robotList[i].getName() + " - " + robotList[i].totalWins + " wins.");
    }

    static void setSeed() {
        System.out.println("New Game");
        seed = System.currentTimeMillis();
    }

    // Gets bot class and makes sure it's cool...
    protected static Class getBotClass(String className) {
        Class robotClass = null;
        try {
            robotClass = Class.forName(className);
            // Try to instantiate as a robot
            Bot robot = (Bot) robotClass.newInstance();
        } catch (ClassNotFoundException e) {
            complain("'bot class name: " + className + " not found.");
        } catch (ClassCastException e) {
            complain("This... thing... is not a 'bot: " + className + "!");
        } catch (Exception e) {
            complain("Problem with your 'bot, " + className + ". " + e + ".");
        }
        return robotClass;
    }

    protected static Robot[] generateBotList() {
        BufferedReader br;
        Vector bots = new Vector();
        int numBots = 0;
        try {
            br = new BufferedReader(new FileReader(BOT_FILE_NAME));
            String botType = br.readLine();
            String bot = br.readLine();
            while (bot != null) {
                Class botClass = getBotClass(bot);
                Bot botInstance = null;
                try {
                    botInstance = (Bot) botClass.newInstance();
                } catch (Exception f) {
                    return new Robot[0];
                }
                int n = botInstance.numberOfBots();
                if (n >= NUMBER_OF_EACH_BOT) {
                    n = NUMBER_OF_EACH_BOT;
                }
                for (int i = 0; i < n; i++) {
                    String botName = botInstance.createName(i);
                    Robot robot = new Robot(botName, botType, botClass, numBots++, i, maxPlayers);
                    bots.addElement(robot);
                }
                botType = br.readLine();
                bot = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Problem with 'bot file: " + e);
            return new Robot[0];
        }
        int n = bots.size();
        Robot[] robotList = new Robot[n];
        for (int i = 0; i < n; i++) {
            Robot r = (Robot) (bots.elementAt(i));
            r.setNumberOfRobots(n);
            robotList[i] = r;
        }
        return robotList;
    }

    static void playGames(int[] spot, int p, int skips, int players) {
        // System.out.println("Spot: " + spot + " P: " + p + " Skips: " + skips + " Players: " + players);
        int n = spot.length;
        if (players == n) {
            playGame(spot);
            return;
        }
        for (int i = 0; i < n; i++) {
            if (spot[i] == -1) {
                spot[i] = p;
                playGames(spot, p + 1, skips, players + 1);
                spot[i] = -1;
            }
        }
        if (skips < (numRobots - n)) {
            playGames(spot, p + 1, skips + 1, players);
        }
    }

    static void playGame(int[] spot) {
        numberOfGames++;
        int n = spot.length;
        for (int i = 0; i < n; i++) {
            System.out.print(" " + spot[i]);
        }
        RobotArena arena = new RobotArena();
        for (int i = 0; i < n; i++) {
            arena.addRobot(robotList[spot[i]].getBotClass(), robotList[spot[i]].getSkill());
        }
        arena.seed = seed;
        arena.numSegments = segments;
        arena.reportEvents = false;
        Statistics stats = arena.runGame();
        int winner = spot[stats.winner];
        System.out.println(" winner: " + winner);
        for (int i = 0; i < n; i++) {
            robotList[spot[i]].playedGame(n);
            if (spot[i] != winner) {
                robotList[winner].beat(spot[i]);
            }
        }
        robotList[winner].wonGame(n);
        // GC
        Runtime.getRuntime().gc();
    }

    public static void setRankings() {
        for (int i = 0; i < numRobots; i++) {
            int rank = 1;
            for (int o = 0; o < numRobots; o++) {
                if (robotList[o].totalWins > robotList[i].totalWins) {
                    rank += 1;
                }
            }
            robotList[i].rank = rank;
        }
    }

    public static final String DATA_START = "<TD ALIGN=\"center\"><FONT SIZE=-1 FACE=\"Verdana, Helvetica\">";
    public static final String DATA_STOP = "</FONT></TD>";
    public static final String BOLD_DATA_START = "<TD ALIGN=\"center\"><FONT SIZE=-1 COLOR=\"#FFCF51\" FACE=\"Verdana, Helvetica\">";
    public static final String BOT_DATA_START = "<TD ALIGN=\"center\"><FONT SIZE=-1 COLOR=\"#FFFFFF\" FACE=\"Verdana, Helvetica\">";

    public static String perCent(int wins, int max) {
        return new Integer(wins * 100 / max).toString();
    }

    public static void makeTable() {
        BufferedWriter output;
        BufferedReader input;
        setRankings();
        try {
            output = new BufferedWriter(new FileWriter(TABLE_NAME));
            input = new BufferedReader(new FileReader(TABLE_HEADER));
            String line = "<!-- Machine Generated Rankings File -->";
            do {
                output.write(line);
                output.newLine();
                line = input.readLine();
            } while (line != null);
            input.close();
            // Rowspan of game size %
            output.write("<TD ALIGN=\"center\" COLSPAN=" + (maxPlayers - 1)
                    + "><FONT SIZE=-1 COLOR=\"#FFFFFF\" FACE=\"Verdana, Helvetica\">");
            output.newLine();
            output.write("Game Size %");
            output.newLine();
            output.write("</FONT></TD>");
            output.newLine();
            // Write out vertical bot columns
            for (int i = 0; i < numRobots; i++) {
                output.write("    <TD BGCOLOR=\"#202020\" ALIGN=\"center\" VALIGN=\"bottom\" ROWSPAN=2><FONT SIZE=-1 COLOR=\"#FFFFFF\" FACE=\"Verdana, Helvetica\">");
                output.newLine();
                String name = robotList[i].getName();
                for (int j = 0; j < name.length(); j++) {
                    output.write("" + name.charAt(j) + "<BR>");
                }
                output.write("</FONT></TD>");
                output.newLine();
            }
            // Write out middle part
            input = new BufferedReader(new FileReader(TABLE_MIDDER));
            line = "<!-- Middle -->";
            do {
                output.write(line);
                output.newLine();
                line = input.readLine();
            } while (line != null);
            input.close();
            // Write out column for each game size
            for (int i = 2; i <= maxPlayers; i++) {
                output.write("    <TD VALIGN=\"bottom\" ALIGN=\"center\"><FONT SIZE=-1 COLOR=\"#FFFFFF\" FACE=\"Verdana, Helvetica\">");
                output.newLine();
                output.write("" + i);
                output.newLine();
                output.write("</FONT></TD>");
                output.newLine();
            }
            output.write("</TR>");
            output.newLine();
            // Write out rankings for each robot
            int i = 0;
            while (i < numRobots) {
                String type = robotList[i].getType();
                // Find bots with same type
                int k = i;
                while ((k < numRobots) && (robotList[k].getType().equals(type))) {
                    k++;
                }
                // Write Bot class data
                output.write("<TR BGCOLOR=\"#202020\">");
                output.newLine();
                output.write("<TD ALIGN=\"center\" ROWSPAN=" + (k - i)
                        + "><FONT SIZE=-1 FACE=\"Verdana, Helvetica\">");
                output.newLine();
                output.write(type);
                output.newLine();
                output.write("</FONT></TD>");
                output.newLine();
                for (int j = i; j < k; j++) {
                    // If it isn't the first bot of this type, then start a new row.
                    if (j > i) {
                        output.write("</TR>");
                        output.newLine();
                        output.write("<TR BGCOLOR=\"#202020\">");
                        output.newLine();
                    }
                    Robot r = robotList[j];
                    // Write name
                    if (r.rank == 1) {
                        output.write(BOLD_DATA_START);
                    } else {
                        output.write(BOT_DATA_START);
                    }
                    output.write(r.getName());
                    output.write(DATA_STOP);
                    output.newLine();
                    // Write rank
                    if (r.rank == 1) {
                        output.write(BOLD_DATA_START);
                    } else {
                        output.write(DATA_START);
                    }
                    output.write(new Integer(r.rank).toString());
                    output.write(DATA_STOP);
                    output.newLine();
                    // Write win per cent
                    if (r.rank == 1) {
                        output.write(BOLD_DATA_START);
                    } else {
                        output.write(DATA_START);
                    }
                    output.write(perCent(r.totalWins, r.numberOfGames));
                    output.write(DATA_STOP);
                    output.newLine();
                    // Write out per cents for game sizes
                    for (int l = 2; l <= maxPlayers; l++) {
                        output.write(DATA_START);
                        output.write(perCent(r.wins[l - 2], r.numGames[l - 2]));
                        output.write(DATA_STOP);
                        output.newLine();
                    }
                    // Write out per cents against each robot
                    for (int l = 0; l < numRobots; l++) {
                        output.write(DATA_START);
                        if (j == l) {
                            output.write("X");
                        } else {
                            output.write(new Integer(r.robotWins[l] * 100 / (r.robotWins[l] + robotList[l].robotWins[j])).toString());
                        }
                        output.write(DATA_STOP);
                        output.newLine();
                    }
                }
                output.write("</TR>");
                output.newLine();
                i = k;
            }
            // Write bottom of table
            output.write("<TR BGCOLOR=\"#464646\">");
            output.newLine();
            for (int l = 0; l < 4 + (maxPlayers - 1) + numRobots; l++) {
                output.write(DATA_START + "&nbsp;" + DATA_STOP);
            }
            // Write stats stuff...
            output.write("</TR>");
            output.write("</TABLE>");
            output.write("<!-- Stats section -->");
            output.write("<TABLE WIDTH=550>");
            output.write("<TR><TD>");
            output.write("<FONT SIZE=-1 FACE=\"Verdana, Helvetica\">");
            output.newLine();
            output.write("<P>");
            output.write("Last Updated: <B>" + new Date() + "</B> ");
            output.newLine();
            output.write("<BR>Total Games Played: <B>" + numberOfGames + "</B> ");
            output.newLine();
            output.write("<BR>Turns per Game: <B>" + segments / 20 + "</B>");
            // Write footer info
            input = new BufferedReader(new FileReader(TABLE_FOOTER));
            line = "<!-- Footer -->";
            do {
                output.write(line);
                output.newLine();
                line = input.readLine();
            } while (line != null);
            input.close();
            output.flush();
            output.close();
        } catch (IOException e) {
        }
    }
}
