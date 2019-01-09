package igx.server;

// Rank.java
public class Rank {

    public String name;
    public int rank;
    public int gamesPlayed;
    public int gamesWon;
    public int gamesPlayedHumans;
    public int gamesWonHumans;
    public int offset;

    public Rank(String name,
            int rank,
            int gamesPlayed,
            int gamesWon,
            int gamesPlayedHumans,
            int gamesWonHumans) {
        this.name = name;
        this.rank = rank;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.gamesPlayedHumans = gamesPlayedHumans;
        this.gamesWonHumans = gamesWonHumans;
    }
}
