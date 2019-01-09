package igx.server;

// Ranking.java 
import igx.shared.*;
import java.util.*;
import java.io.*;

public class Ranking {
    // Constants

    public static final String POST_RANKING_DIR = "rankings";
    public static final String RANKING_LIST = "playerList";
    public static final String RANKING_EXT = ".player";
    public static final String TABLE_PATH = "";
    public static final String TABLE_NAME = "rankings.html";
    public static final String TABLE_HEADER = "rankingsHeader.html";
    public static final String TABLE_FOOTER = "rankingsFooter.html";
    public static final String ROW_PREFIX = "   <TR BGCOLOR=\"#101010\">";
    public static final String ROW_SUFFIX = "   </TR>";
    public static final String TOP_PREFIX = "    <TD><FONT SIZE=-1 COLOR=\"#FFCF51\" FACE=\"Verdana, Helvetica\">";
    public static final String TOP_SUFFIX = "    </FONT></TD>";
    public static final String NORMAL_PREFIX = "    <TD><FONT SIZE=-1 FACE=\"Verdana, Helvetica\">";
    public static final String NORMAL_SUFFIX = "    </FONT></TD>";

    public static final int MIN_GAMES_TO_GET_RANKED = 5;

    // Data fields
    public String playerName;
    public String fileName;
    public int ranking;
    public int newRanking;
    public int gamesPlayed;
    public int gamesWon;

    // Static fields
    public static Ranking COMPUTER_RANKING = new Ranking();
    public static String path = null;
    public static String RANKING_DIR = null;
    // Default constructor (for computer players)

    public Ranking() {
        playerName = "__Android";
        makeStandardRanking();
    }
// Constructor for human players

    public Ranking(String playerName) {
        BufferedReader rankingFile;
        File testFile;
        fileName = RANKING_DIR + File.separator + playerName + RANKING_EXT;
        try {
            testFile = new File(fileName);
            if (testFile.exists()) {
                rankingFile = new BufferedReader(new FileReader(testFile));
            } else {
                throw new FileNotFoundException("File not found");
            }
        } catch (FileNotFoundException e) {
            createNewRanking(playerName);
            try {
                rankingFile = new BufferedReader(new FileReader(fileName));
            } catch (FileNotFoundException f) {
                System.out.println("Problem creating ranking file " + fileName + ".");
                makeStandardRanking();
                return;
            }
        }
        try {
            String line;
            this.playerName = rankingFile.readLine();
            line = rankingFile.readLine();
            if (line == null) {
                throw new IOException("Empty line read in ranking file.");
            }
            ranking = newRanking = Integer.parseInt(line);
            line = rankingFile.readLine();
            if (line == null) {
                throw new IOException("Empty line read in ranking file.");
            }
            gamesPlayed = Integer.parseInt(line);
            line = rankingFile.readLine();
            if (line == null) {
                throw new IOException("Empty line read in ranking file.");
            }
            gamesWon = Integer.parseInt(line);
            rankingFile.close();
        } catch (IOException e) {
            System.out.println("Problem with ranking file: " + e);
            makeStandardRanking();
            return;
        } catch (NumberFormatException e) {
            System.out.println("Problem with ranking file: " + e);
            makeStandardRanking();
            return;
        }
    }

    public void beat(Ranking otherRanking) {
        int winner = ranking;
        int loser = otherRanking.ranking;
        if ((winner - loser) > Params.MAX_DIFFERENCE) {
            return;
        }
        if ((loser - winner) > Params.MAX_DIFFERENCE) {
            newRanking += Params.MAX_SCORE;
            otherRanking.newRanking -= Params.MAX_SCORE;
            return;
        }
        int delta = (loser - winner + 350) * Params.MAX_SCORE / (2 * Params.MAX_DIFFERENCE);
        newRanking += delta;
        otherRanking.newRanking -= delta;
    }

    public void createNewRanking(String playerName) {
        this.playerName = playerName;
        makeStandardRanking();
        writeRanking();
        BufferedWriter rankListFile;
        try {
            rankListFile = new BufferedWriter(new FileWriter(path + RANKING_LIST, true));
            rankListFile.write(playerName);
            rankListFile.newLine();
            rankListFile.close();
        } catch (IOException e) {
            System.out.println("Problem writing to player list: " + e);
        }
    }

    public void makeStandardRanking() {
        ranking = newRanking = Params.STANDARD_RANKING;
        gamesPlayed = 0;
        gamesWon = 0;
    }
// Makes the HTML table

    public static void makeTable(String path) {
        BufferedWriter output;
        BufferedReader input;
        BufferedReader rankingFile;
        BufferedReader playerList;
        Vector ranks = new Vector();
        try {
            output = new BufferedWriter(new FileWriter(path + TABLE_NAME));
            input = new BufferedReader(new FileReader(path + TABLE_HEADER));
            playerList = new BufferedReader(new FileReader(path + RANKING_LIST));
            String line = "<!-- Machine Generated Rankings File -->";
            do {
                output.write(line);
                output.newLine();
                line = input.readLine();
            } while (line != null);
            input.close();
            String name;
            do {
                name = playerList.readLine();
                if (name != null) {
                    Ranking rank = new Ranking();
                    String fileName = RANKING_DIR + File.separator + name + RANKING_EXT;
                    rankingFile = new BufferedReader(new FileReader(fileName));
                    rank.playerName = rankingFile.readLine();
                    line = rankingFile.readLine();
                    if (line == null) {
                        throw new IOException("Empty line read in ranking file.");
                    }
                    rank.ranking = rank.newRanking = Integer.parseInt(line);
                    line = rankingFile.readLine();
                    if (line == null) {
                        throw new IOException("Empty line read in ranking file.");
                    }
                    rank.gamesPlayed = Integer.parseInt(line);
                    line = rankingFile.readLine();
                    if (line == null) {
                        throw new IOException("Empty line read in ranking file.");
                    }
                    rank.gamesWon = Integer.parseInt(line);
                    rankingFile.close();
                    boolean ranked = false;
                    for (int i = 0; i < ranks.size(); i++) {
                        Ranking thisRank = (Ranking) ranks.elementAt(i);
                        if (rank.ranking > thisRank.ranking) {
                            ranks.insertElementAt(rank, i);
                            ranked = true;
                            break;
                        }
                    }
                    if (!ranked) {
                        ranks.addElement(rank);
                    }
                }
            } while (name != null);
            playerList.close();
            int rating = 0;
            for (int i = 0; i < ranks.size(); i++) {
                Ranking rank = (Ranking) ranks.elementAt(i);
                if (i == 0) {
                    rating++;
                    output.write(ROW_PREFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + new Integer(rating).toString() + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.playerName + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.ranking + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesPlayed + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesWon + TOP_SUFFIX);
                    output.newLine();
                    output.write(ROW_SUFFIX);
                    output.newLine();
                } else if (rank.gamesPlayed >= MIN_GAMES_TO_GET_RANKED) {
                    rating++;
                    output.write(ROW_PREFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + new Integer(rating).toString() + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.playerName + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.ranking + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesPlayed + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesWon + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(ROW_SUFFIX);
                    output.newLine();
                }
            }
            input = new BufferedReader(new FileReader(path + TABLE_FOOTER));
            do {
                line = input.readLine();
                if (line != null) {
                    output.write(line);
                    output.newLine();
                }
            } while (line != null);
            input.close();
            output.close();
        } catch (IOException e) {
            System.out.println("Problem while generating rankings file: " + e);
        }
    }

    public void playedGame() {
        gamesPlayed++;
    }

    /**
     * This method was created in VisualAge.
     *
     * @param path java.lang.String
     */
    public static void setPath(String thePath) {
        path = thePath;
        RANKING_DIR = path + POST_RANKING_DIR;
    }

    public void wonGame() {
        gamesWon++;
    }

    public void writeRanking() {
        if (playerName.equals("__Android")) {
            return;
        }
        BufferedWriter rankingFile;
        try {
            rankingFile = new BufferedWriter(new FileWriter(fileName));
            rankingFile.write(playerName);
            rankingFile.newLine();
            rankingFile.write(new Integer(newRanking).toString());
            rankingFile.newLine();
            rankingFile.write(new Integer(gamesPlayed).toString());
            rankingFile.newLine();
            rankingFile.write(new Integer(gamesWon).toString());
            rankingFile.newLine();
            rankingFile.close();
        } catch (IOException e) {
            System.out.println("Error writing ranking file " + fileName);
        }
    }
}
