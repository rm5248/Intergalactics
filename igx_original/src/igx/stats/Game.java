package igx.stats;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class Game implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private int numPlayers;

    /** nullable persistent field */
    private java.util.Date time;

    /** nullable persistent field */
    private boolean save;

    /** nullable persistent field */
    private igx.stats.Player winner;

    /** nullable persistent field */
    private igx.stats.Player neutral;

    /** persistent field */
    private Set players;

    /** persistent field */
    private Set timeStates;

    /** full constructor */
    public Game(int numPlayers, java.util.Date time, boolean save, igx.stats.Player winner, igx.stats.Player neutral, Set players, Set timeStates) {
        this.numPlayers = numPlayers;
        this.time = time;
        this.save = save;
        this.winner = winner;
        this.neutral = neutral;
        this.players = players;
        this.timeStates = timeStates;
    }

    /** default constructor */
    public Game() {
    }

    /** minimal constructor */
    public Game(Set players, Set timeStates) {
        this.players = players;
        this.timeStates = timeStates;
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public int getNumPlayers() {
        return this.numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public java.util.Date getTime() {
        return this.time;
    }

    public void setTime(java.util.Date time) {
        this.time = time;
    }

    public boolean isSave() {
        return this.save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public igx.stats.Player getWinner() {
        return this.winner;
    }

    public void setWinner(igx.stats.Player winner) {
        this.winner = winner;
    }

    public igx.stats.Player getNeutral() {
        return this.neutral;
    }

    public void setNeutral(igx.stats.Player neutral) {
        this.neutral = neutral;
    }

    public java.util.Set getPlayers() {
        return this.players;
    }

    public void setPlayers(java.util.Set players) {
        this.players = players;
    }

    public java.util.Set getTimeStates() {
        return this.timeStates;
    }

    public void setTimeStates(java.util.Set timeStates) {
        this.timeStates = timeStates;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Game) ) return false;
        Game castOther = (Game) other;
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
