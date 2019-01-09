package igx.client;

// TextRow.java 
import java.util.*;
import java.awt.*;

public class TextRow {

    Vector elements;
    boolean underlined = false;
    Color underlineColour;
    boolean selected = false;
    boolean centered = false;
    int numberOfElements;

    public TextRow(Vector elements) {
        this.elements = elements;
        numberOfElements = elements.size();
    }

    public void center() {
        centered = true;
    }

    public void setSelect(boolean val) {
        selected = val;
    }

    public void underline(Color colour) {
        underlineColour = colour;
        underlined = true;
    }
}
