// Robot.java
package igx.bots.arena;

import igx.bots.*;

public class Robot {

    private Class botClass;
    private String name;
    private String type;
    private int number;
    private int skill;
    private int numRobots;
    private int maxPlayers;

    // Stats
    int rank;
    int totalWins = 0;
    int numberOfGames = 0;
    int[] wins;
    int[] numGames;
    int[] robotWins;

    public Robot(String name, String type, Class botClass, int number, int skill, int maxPlayers) {
        this.name = name;
        this.type = type;
        this.botClass = botClass;
        this.number = number;
        this.skill = skill;
        this.maxPlayers = maxPlayers;
        wins = new int[maxPlayers - 1];
        numGames = new int[maxPlayers - 1];
    }

    public void setNumberOfRobots(int numRobots) {
        this.numRobots = numRobots;
        robotWins = new int[numRobots];
    }

    public void wonGame(int numPlayers) {
        wins[numPlayers - 2] += 1;
        totalWins += 1; //(numPlayers - 1);
    }

    public void playedGame(int numPlayers) {
        numGames[numPlayers - 2] += 1;
        numberOfGames++;
    }

    public void beat(int robotNum) {
        robotWins[robotNum]++;
    }

    public String makeStats(int numGames) {
        String s = "";
        s += "Total Wins: " + totalWins;
        for (int i = 0; i < (maxPlayers - 1); i++) {
            s += " " + (i + 2) + "-player: " + wins[i];
        }
        for (int i = 0; i < numRobots; i++) {
            s += " vs. " + i + ": " + robotWins[i];
        }
        return s;
    }

    public String getName() {
        return name;
    }

    public int getSkill() {
        return skill;
    }

    public int getNumber() {
        return number;
    }

    public Class getBotClass() {
        return botClass;
    }

    public String getType() {
        return type;
    }
}
