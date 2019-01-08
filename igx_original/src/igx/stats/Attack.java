package igx.stats;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class Attack implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private short time;

    /** nullable persistent field */
    private boolean newAttack;

    /** nullable persistent field */
    private byte ratio;

    /** nullable persistent field */
    private short ships;

    /** nullable persistent field */
    private byte planetID;

    /** nullable persistent field */
    private igx.stats.Game game;

    /** nullable persistent field */
    private igx.stats.Player player;

    /** full constructor */
    public Attack(short time, boolean newAttack, byte ratio, short ships, byte planetID, igx.stats.Game game, igx.stats.Player player) {
        this.time = time;
        this.newAttack = newAttack;
        this.ratio = ratio;
        this.ships = ships;
        this.planetID = planetID;
        this.game = game;
        this.player = player;
    }

    /** default constructor */
    public Attack() {
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public short getTime() {
        return this.time;
    }

    public void setTime(short time) {
        this.time = time;
    }

    public boolean isNewAttack() {
        return this.newAttack;
    }

    public void setNewAttack(boolean newAttack) {
        this.newAttack = newAttack;
    }

    public byte getRatio() {
        return this.ratio;
    }

    public void setRatio(byte ratio) {
        this.ratio = ratio;
    }

    public short getShips() {
        return this.ships;
    }

    public void setShips(short ships) {
        this.ships = ships;
    }

    public byte getPlanetID() {
        return this.planetID;
    }

    public void setPlanetID(byte planetID) {
        this.planetID = planetID;
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
        if ( !(other instanceof Attack) ) return false;
        Attack castOther = (Attack) other;
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
