package igx.server;

// RankingSystem.java
import igx.shared.*;
import java.util.*;
import java.io.*;

public class RankingSystem extends Thread {
    // Table constants

    public static final String TABLE_NAME = "rankings.html";
    public static final String TABLE_HEADER = "rankingsHeader.html";
    public static final String TABLE_FOOTER = "rankingsFooter.html";
    public static final String ROW_PREFIX = "   <TR BGCOLOR=\"#101010\">";
    public static final String ROW_SUFFIX = "   </TR>";
    public static final String TOP_PREFIX = "    <TD><FONT SIZE=-1 COLOR=\"#FFCF51\" FACE=\"Verdana, Helvetica\">";
    public static final String TOP_SUFFIX = "    </FONT></TD>";
    public static final String NORMAL_PREFIX = "    <TD><FONT SIZE=-1 FACE=\"Verdana, Helvetica\">";
    public static final String NORMAL_SUFFIX = "    </FONT></TD>";
    public static final String TOP_COLOUR = "FFCF51";
    public static final String STATS_URL = "http://allen.cs.toronto.edu:8080/stats?action=viewplayer&player_name=";
    // Maximum length of UTF encoding allowed
    protected static final int NAME_LENGTH = 40;
    protected static final int RECORD_LENGTH = NAME_LENGTH + 5 * 4;
    protected String rankFileName, path;
    protected RandomAccessFile rankFile = null;
    long fileLength = 0;
    protected Vector ranks;
    protected Hashtable nameToRank;
    // Fields set by server
    protected String winner;
    protected String[] losers;
    protected boolean againstHumans;

    public RankingSystem(String path, String rankFileName) {
        super("Ranking System");
        this.path = path;
        this.rankFileName = rankFileName;
        try {
            getRankings();
        } catch (IOException e) {
            System.out.println("Couldn't get rankings: " + e);
        }
        start();
    }

    protected int beat(Rank w, Rank l) {
        int winner = w.rank;
        int loser = l.rank;
        int gain = 0;
        if ((winner - loser) > Params.MAX_DIFFERENCE) {
            return 0;
        }
        if ((loser - winner) > Params.MAX_DIFFERENCE) {
            gain = Params.MAX_SCORE;
            l.rank -= Params.MAX_SCORE;
            return gain;
        }
        int delta = (loser - winner + 350) * Params.MAX_SCORE / (2 * Params.MAX_DIFFERENCE);
        gain = delta;
        l.rank -= delta;
        return gain;
    }

    /**
     * Reads in the ranking file and makes the adjustments for this game. Writes
     * out the ranking file.
     *
     * @param winner The name of the game winner.
     * @param losers An array of game losers.
     */
    public void doRankGame() {
        // PHASE 1: Adjust rankings file
        int n = 0;
        int m = losers.length;
        Rank winnerRank = (Rank) nameToRank.get(winner);
        Rank[] loserRank = new Rank[losers.length];
        try {
            rankFile = new RandomAccessFile(path + rankFileName, "rw");
            fileLength = rankFile.length();
            n = (int) (fileLength / RECORD_LENGTH);
        } catch (IOException e) {
            System.out.println("Couldn't open ranking file: " + e);
        }
        try {
            int insertPoint = n;
            if (winnerRank == null) {
                winnerRank = new Rank(winner, Params.STANDARD_RANKING, 0, 0, 0, 0);
                winnerRank.offset = insertPoint;
                writeRank(winnerRank, winnerRank.offset);
                ranks.addElement(winnerRank);
                nameToRank.put(winner, winnerRank);
                insertPoint++;
            }
            int pointsGained = 0;
            for (int i = 0; i < m; i++) {
                loserRank[i] = (Rank) nameToRank.get(losers[i]);
                if (loserRank[i] == null) {
                    loserRank[i] = new Rank(losers[i], Params.STANDARD_RANKING, 0, 0, 0, 0);
                    loserRank[i].offset = insertPoint;
                    ranks.addElement(loserRank[i]);
                    nameToRank.put(losers[i], loserRank[i]);
                    insertPoint++;
                }
                pointsGained += beat(winnerRank, loserRank[i]);
                loserRank[i].gamesPlayed++;
                if (againstHumans) {
                    loserRank[i].gamesPlayedHumans++;
                }
                writeRank(loserRank[i], loserRank[i].offset);
            }
            // Give winner points
            winnerRank.rank += pointsGained;
            winnerRank.gamesPlayed++;
            winnerRank.gamesWon++;
            if (againstHumans) {
                winnerRank.gamesPlayedHumans++;
                winnerRank.gamesWonHumans++;
            }
            // Write out winner record
            writeRank(winnerRank, winnerRank.offset);
        } catch (IOException e) {
            System.out.println("Problem reading rankings: " + e);
        } finally {
            try {
                if (rankFile != null) {
                    rankFile.close();
                }
            } catch (IOException e) {
            }
        }
        // PHASE 2: Generate table
        generateTable(ranks);
    }

    protected String realName(String name) {
        int botIndex = name.lastIndexOf("(robot)");
        if (botIndex != -1) {
            return name.substring(0, botIndex);
        } else {
            return name;
        }
    }

    protected void generateTable(Vector ranks) {
        int n = ranks.size();
        // Sort ranks using quicksort
        sort(ranks, 0, n - 1);
        // Generate the table
        BufferedWriter output;
        BufferedReader input;
        try {
            output = new BufferedWriter(new FileWriter(path + TABLE_NAME));
            input = new BufferedReader(new FileReader(path + TABLE_HEADER));
            String line = "<!-- Machine Generated Rankings File -->";
            do {
                output.write(line);
                output.newLine();
                line = input.readLine();
            } while (line != null);
            input.close();
            String name;
            int rating = 0;
            for (int i = 0; i < n; i++) {
                Rank rank = (Rank) ranks.elementAt(n - i - 1);
                if (i == 0) {
                    rating++;
                    output.write(ROW_PREFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + new Integer(rating).toString() + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + "<A HREF=\"" + STATS_URL + realName(rank.name) + "\"><FONT COLOR=\"" + TOP_COLOUR + "\">" + rank.name + "</FONT></A>" + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.rank + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesPlayed + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesWon + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesPlayedHumans + TOP_SUFFIX);
                    output.newLine();
                    output.write(TOP_PREFIX + rank.gamesWonHumans + TOP_SUFFIX);
                    output.newLine();
                    output.write(ROW_SUFFIX);
                    output.newLine();
                } else if (rank.gamesPlayed >= Params.MIN_GAMES_BEFORE_RANKED) {
                    rating++;
                    output.write(ROW_PREFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + new Integer(rating).toString() + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + "<A HREF=\"" + STATS_URL + realName(rank.name) + "\">" + rank.name + "</A>" + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.rank + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesPlayed + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesWon + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesPlayedHumans + NORMAL_SUFFIX);
                    output.newLine();
                    output.write(NORMAL_PREFIX + rank.gamesWonHumans + NORMAL_SUFFIX);
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

    /**
     * No docs available.
     *
     * @return igx.server.Rank
     * @param p igx.shared.Player
     */
    public Rank getRank(Player p) {
        String name = p.name;
        if (!p.isHuman) {
            name += "(robot)";
        }
        return (Rank) nameToRank.get(name);
    }

    /**
     * No docs available.
     *
     * @return igx.server.Rank
     * @param p igx.shared.Player
     */
    public Rank getRank(String name) {
        return (Rank) nameToRank.get(name);
    }

    /**
     * No docs available.
     *
     * @exception java.io.IOException The exception description.
     */
    protected void getRankings() throws IOException {
        int n = 0;
        try {
            rankFile = new RandomAccessFile(path + rankFileName, "rw");
            fileLength = rankFile.length();
            n = (int) (fileLength / RECORD_LENGTH);
            int size = (n == 0) ? 1 : n;
            ranks = new Vector(size);
            nameToRank = new Hashtable(size);
        } catch (IOException e) {
            System.out.println("Couldn't open ranking file: " + e);
        }
        try {
            for (int i = 0; i < n; i++) {
                Rank r = readRank(i);
                ranks.addElement(r);
                nameToRank.put(r.name, r);
            }
        } catch (IOException e) {
            System.out.println("Error reading ranking file: " + e);
        }
        try {
            rankFile.close();
        } catch (IOException e) {
            System.out.println("Couldn't close ranking file: " + e);
        }
    }

    protected String padString(String s) {
        int n = NAME_LENGTH - s.length();
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < n; i++) {
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Reads in the ranking file and makes the adjustments for this game. Writes
     * out the ranking file.
     *
     * @param winner The name of the game winner.
     * @param losers An array of game losers.
     */
    public synchronized void rankGame(String winner,
            String[] losers,
            boolean againstHumans) {
        this.winner = winner;
        this.losers = losers;
        this.againstHumans = againstHumans;
        notify();
    }

    protected Rank readRank(int i) throws IOException {
        long pos = i * RECORD_LENGTH;
        rankFile.seek(pos);
        String name = rankFile.readUTF();
        rankFile.seek(pos + NAME_LENGTH);
        int rank = rankFile.readInt();
        int gamesPlayed = rankFile.readInt();
        int gamesWon = rankFile.readInt();
        int gamesPlayedHumans = rankFile.readInt();
        int gamesWonHumans = rankFile.readInt();
        Rank r = new Rank(name, rank, gamesPlayed, gamesWon, gamesPlayedHumans, gamesWonHumans);
        r.offset = i;
        return r;
    }

    /**
     * No docs available.
     */
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
            doRankGame();
        }
    }

    protected static void sort(Vector ranks, int low0, int high0) {
        int low = low0;
        int high = high0;
        Rank mid;
        if (high0 > low0) {
            mid = (Rank) ranks.elementAt((low0 + high0) / 2);
            while (low <= high) {
                while ((low < high0) && (((Rank) ranks.elementAt(low)).rank < mid.rank)) {
                    low++;
                }
                while ((low0 < high) && (mid.rank < ((Rank) ranks.elementAt(high)).rank)) {
                    high--;
                }
                if (low <= high) {
                    Object o = ranks.elementAt(low);
                    ranks.setElementAt(ranks.elementAt(high), low);
                    ranks.setElementAt(o, high);
                    low++;
                    high--;
                }
            }
            if (low0 < high) {
                sort(ranks, low0, high);
            }
            if (low < high0) {
                sort(ranks, low, high0);
            }
        }
    }

    protected void writeRank(Rank r, int i) throws IOException {
        long pos = i * RECORD_LENGTH;
        rankFile.seek(pos);
        rankFile.writeUTF(r.name);
        rankFile.seek(pos);
        // Get length of UTF encoding
        int l = rankFile.readUnsignedShort();
        int n = NAME_LENGTH - l;
        if (n > 0) {
            rankFile.seek(pos + l + 2);
        }
        for (int j = 0; j < n; j++) {
            rankFile.writeByte(0);
        }
        rankFile.seek(pos + NAME_LENGTH);
        rankFile.writeInt(r.rank);
        rankFile.writeInt(r.gamesPlayed);
        rankFile.writeInt(r.gamesWon);
        rankFile.writeInt(r.gamesPlayedHumans);
        rankFile.writeInt(r.gamesWonHumans);
    }
}
