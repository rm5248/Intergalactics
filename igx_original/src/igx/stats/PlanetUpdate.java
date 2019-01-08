package igx.stats;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class PlanetUpdate implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private byte production;

    /** nullable persistent field */
    private byte planetID;

    /** nullable persistent field */
    private short time;

    /** nullable persistent field */
    private byte ratio;

    /** nullable persistent field */
    private short ships;

    /** nullable persistent field */
    private byte x;

    /** nullable persistent field */
    private byte y;

    /** nullable persistent field */
    private igx.stats.Player player;

    /** nullable persistent field */
    private igx.stats.Game game;

    /** full constructor */
    public PlanetUpdate(byte production, byte planetID, short time, byte ratio, short ships, byte x, byte y, igx.stats.Player player, igx.stats.Game game) {
        this.production = production;
        this.planetID = planetID;
        this.time = time;
        this.ratio = ratio;
        this.ships = ships;
        this.x = x;
        this.y = y;
        this.player = player;
        this.game = game;
    }

    /** default constructor */
    public PlanetUpdate() {
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public byte getProduction() {
        return this.production;
    }

    public void setProduction(byte production) {
        this.production = production;
    }

    public byte getPlanetID() {
        return this.planetID;
    }

    public void setPlanetID(byte planetID) {
        this.planetID = planetID;
    }

    public short getTime() {
        return this.time;
    }

    public void setTime(short time) {
        this.time = time;
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

    public byte getX() {
        return this.x;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public byte getY() {
        return this.y;
    }

    public void setY(byte y) {
        this.y = y;
    }

    public igx.stats.Player getPlayer() {
        return this.player;
    }

    public void setPlayer(igx.stats.Player player) {
        this.player = player;
    }

    public igx.stats.Game getGame() {
        return this.game;
    }

    public void setGame(igx.stats.Game game) {
        this.game = game;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof PlanetUpdate) ) return false;
        PlanetUpdate castOther = (PlanetUpdate) other;
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
