package igx.stats;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class TimeState implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private int numShips;

    /** nullable persistent field */
    private short time;

    /** nullable persistent field */
    private short totalProduction;

    /** nullable persistent field */
    private int score;

    /** nullable persistent field */
    private byte numPlanets;

    /** nullable persistent field */
    private igx.stats.Game game;

    /** nullable persistent field */
    private igx.stats.Player player;

    /** full constructor */
    public TimeState(int numShips, short time, short totalProduction, int score, byte numPlanets, igx.stats.Game game, igx.stats.Player player) {
        this.numShips = numShips;
        this.time = time;
        this.totalProduction = totalProduction;
        this.score = score;
        this.numPlanets = numPlanets;
        this.game = game;
        this.player = player;
    }

    /** default constructor */
    public TimeState() {
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public int getNumShips() {
        return this.numShips;
    }

    public void setNumShips(int numShips) {
        this.numShips = numShips;
    }

    public short getTime() {
        return this.time;
    }

    public void setTime(short time) {
        this.time = time;
    }

    public short getTotalProduction() {
        return this.totalProduction;
    }

    public void setTotalProduction(short totalProduction) {
        this.totalProduction = totalProduction;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public byte getNumPlanets() {
        return this.numPlanets;
    }

    public void setNumPlanets(byte numPlanets) {
        this.numPlanets = numPlanets;
    }

    public igx.stats.Game getGame() {
        return this.game;
    }

    public void setGame(igx.stats.Game game) {
        this.game = game;
    }

    public igx.stats.Player getPlayer() {
        return this.player;
    }

    public void setPlayer(igx.stats.Player player) {
        this.player = player;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof TimeState) ) return false;
        TimeState castOther = (TimeState) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
