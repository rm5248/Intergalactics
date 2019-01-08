package igx.stats;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class Player implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private boolean robot;

    /** persistent field */
    private Set games;

    /** full constructor */
    public Player(java.lang.String name, boolean robot, Set games) {
        this.name = name;
        this.robot = robot;
        this.games = games;
    }

    /** default constructor */
    public Player() {
    }

    /** minimal constructor */
    public Player(Set games) {
        this.games = games;
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public boolean isRobot() {
        return this.robot;
    }

    public void setRobot(boolean robot) {
        this.robot = robot;
    }

    public java.util.Set getGames() {
        return this.games;
    }

    public void setGames(java.util.Set games) {
        this.games = games;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Player) ) return false;
        Player castOther = (Player) other;
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
