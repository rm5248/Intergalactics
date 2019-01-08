package igx.stats;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class Event implements Serializable {

    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private int eventID;

    /** nullable persistent field */
    private String message;

    /** nullable persistent field */
    private int time;

    /** nullable persistent field */
    private igx.stats.Game game;

    /** full constructor */
    public Event(int eventID, java.lang.String message, int time, igx.stats.Game game) {
        this.eventID = eventID;
        this.message = message;
        this.time = time;
        this.game = game;
    }

    /** default constructor */
    public Event() {
    }

    public java.lang.Long getId() {
        return this.id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public int getEventID() {
        return this.eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public java.lang.String getMessage() {
        return this.message;
    }

    public void setMessage(java.lang.String message) {
        this.message = message;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
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
        if ( !(other instanceof Event) ) return false;
        Event castOther = (Event) other;
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
