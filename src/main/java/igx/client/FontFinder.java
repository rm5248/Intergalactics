package igx.client;

// FontFinder.java
import igx.shared.*;
import java.awt.*;

public class FontFinder {

    public static final int INIT_SIZE = 30;

    public static Font getFont(Toolkit toolkit,
            String face,
            int rows,
            int height) {
        int size = INIT_SIZE + 1;
        Font font;
        int thisHeight;
        FontMetrics fm;
        do {
            size--;
            font = new Font(face, Font.PLAIN, size);
            fm = toolkit.getFontMetrics(font);
            thisHeight = (fm.getAscent() + fm.getDescent()) * rows;
        } while ((thisHeight >= height) || (size == 0));
        return font;
    }
}
