package igx.client;

// TextElement.java 
import java.util.*;
import java.awt.*;

public class TextElement {

    public static final int SEQUENTIAL = -1;

    public String text;
    public Color colour;
    public int column;

    public TextElement(String text, Color colour, int column) {
        this.text = text;
        this.colour = colour;
        this.column = column;
    }

    public int getWidth(FontMetrics fm) {
        return fm.stringWidth(text);
    }
}
