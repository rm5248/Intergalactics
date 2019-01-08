package igx.stats.web;

// Galaxy.java

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import igx.shared.Planet;
import igx.shared.Params;
import igx.stats.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;

public class GalaxyPainter extends Canvas
{
    //// Constants
    // Thresholds and colours on ratio chart
    final static int LOWRATIO = Params.SPECIALRATIOMAX / 2;
    final static int MIDRATIO = 5 * Params.SPECIALRATIOMAX / 6;
    final static int HIGHRATIO = Params.SPECIALRATIOMAX;
    final static Color HIGHCOLOUR = Color.red;
    final static Color MIDCOLOUR = Color.yellow;
    final static Color LOWCOLOUR = Color.green;
    final static int MESSAGE_FONT_RATIO = 32;
    // the background planet colour
    final static Color PLANETCOLOUR = new Color(64, 64, 64);
    // Ratio between map height and ratio bar width
    final static int ATTACK_WIDTH_RATIO = 250;
    public final static int SCROLLBAR_WIDTH_RATIO = 48;
    public final static int SCROLLBAR_HEIGHT_RATIO = 16;

    int planetSize;
    int size;
    Font font;
    FontMetrics fontMetric;
    int fontHeight, ratioWidth;
    int zeroRatioY, lowRatioY, midRatioY, highRatioY;
    int highRatioHeight, midRatioHeight, lowRatioHeight;
    int sliderWidth, sliderHeight, sliderMax;
    // message stuff
    int messageFontSize, messageFontHeight, messageFontDescent, messageY;
    Toolkit toolkit;

    private PlanetUpdate[] planets;
    private Map planetToAttacks;
    private Player[] players;

    public GalaxyPainter (PlanetUpdate[] planets, Attack[] attacks, Player[] players, int size) {
        super();
        this.planets = planets;
        this.players = players;
        this.size = size;
        toolkit = Toolkit.getDefaultToolkit();
        int fontSize = 12 * size / 800;
        font = new Font("SansSerif", Font.PLAIN, fontSize);
        planetSize = size / Params.MAPX;
        FontMetrics fm = toolkit.getFontMetrics(font);
        fontMetric = fm;
        fontHeight = fm.getAscent();
        ratioWidth = size / ATTACK_WIDTH_RATIO;
        zeroRatioY = planetSize - 3;
        highRatioY = 3;
        lowRatioY = (zeroRatioY - highRatioY) / 2 + highRatioY;
        midRatioY = (zeroRatioY - highRatioY) / 6 + highRatioY;
        highRatioHeight = (zeroRatioY - highRatioY) / 6 + 1;
        lowRatioHeight = (zeroRatioY - highRatioY) / 2;
        midRatioHeight = (zeroRatioY - highRatioY) / 3 + 1;
        setBackground(Color.black);
        mapAttacks(planets, attacks);
    }

    public BufferedImage getImage() {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        paint(image.createGraphics());
        /*
        try {
            FileOutputStream out = new FileOutputStream(new File("test.jpg"));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
            param.setQuality(1f,true);
            encoder.encode(image, param);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return image;
    }

    private void mapAttacks(PlanetUpdate[] planets, Attack[] attacks) {
        planetToAttacks = new HashMap();
        for (int i = 0; i < attacks.length; i++) {
            Attack a = attacks[i];
            int planetID = a.getPlanetID();
            PlanetUpdate p = planets[planetID];
            ArrayList l = (ArrayList)planetToAttacks.get(p);
            if (l == null) {
                l = new ArrayList();
                planetToAttacks.put(p, l);
            }
            l.add(a);
        }
    }

    public Graphics getDefaultGraphics () {
        Graphics g = getGraphics();
        g.setFont(font);
        g.setPaintMode();
        return g;
    }

    public void paint (Graphics g) {
        g.setFont(font);
        g.setPaintMode();
        g.setColor(PLANETCOLOUR);
        g.drawLine(0, 0, 0, size-1);
        g.drawLine(0, 0, size-1, 0);
        g.drawLine(0, size-1, size-1, size-1);
        g.drawLine(size-1, 0, size-1, size-1);
        for (int i = 0; i < Params.PLANETS; i++) {
            repaintPlanet(g, i);
        }
    }

    private int getPlayerNumber(Player p) {
        for (int i = 0; i < players.length; i++) {
            if (players[i].equals(p)) {
                return i;
            }
        }
        return Params.NEUTRAL;
    }

    private char getPlanetChar(int planetNum) {
        return igx.shared.Planet.num2char(planetNum);
    }

    public void repaintPlanet (Graphics g, int planetNum) {
        PlanetUpdate planet = planets[planetNum];
        int x = planetSize*planet.getX()+1;
        int y = planetSize*planet.getY()+1;
        int rightX = x + planetSize - 1;
        int bottomY = y + planetSize - 1;
        // Clear background and draw background planet
        int planetWidth = planetSize*2/3;
        int ownerNum = getPlayerNumber(planet.getPlayer());
        g.setColor(Color.black);
        g.fillRect(x, y, planetSize, planetSize);
        g.setColor(PLANETCOLOUR);
        g.fillOval((x + planetSize / 2) - planetWidth / 2, (y + planetSize / 2) - planetWidth /2,
                   planetWidth, planetWidth);
        // Set to owner's colour
        g.setColor(Params.PLAYERCOLOR[ownerNum]);
        // If neutral just draw planet
        if (ownerNum == Params.NEUTRAL)
            g.drawString(new Character(getPlanetChar(planetNum)).toString(), x + 2, y + fontHeight - 1);
        else {
            // Draw planet and production
            g.drawString(getPlanetChar(planetNum) + "  " + planet.getProduction(), x + 2, y + fontHeight - 1);
            // Draw number of ships
            g.drawString(new Integer(planet.getShips()).toString(), x + 2, y + 2*fontHeight - 1);
        }
        // Draw number of attacking ships
        ArrayList attacks = (ArrayList)planetToAttacks.get(planet);
        if (attacks != null) {
            if (attacks.size() > 1) {
                // How about a little ANDing with a rectangle?
                int totalShips = 0;
                for (int i = 0; i < attacks.size(); i++) {
                    Attack a = (Attack)attacks.get(i);
                    totalShips += a.getShips();
                }
                String text = new Integer(totalShips).toString();
                int textWidth = fontMetric.stringWidth(text);
                int rectX = 0;
                g.setXORMode(Color.white);
                for (int i = 0; i < attacks.size(); i++) {
                    Attack a = (Attack)attacks.get(i);
                    Color c = Params.PLAYERCOLOR[getPlayerNumber(a.getPlayer())];
                    int rectWidth = a.getShips() * textWidth / totalShips;
                    g.setColor(c);
                    g.fillRect(x+2+rectX, y + 2*fontHeight - 1, rectWidth, fontHeight);
                    rectX += rectWidth;
                }
                g.setPaintMode();
                g.setColor(Color.white);
                g.drawString(text, x + 2, y + 3*fontHeight - 1);
                g.setXORMode(Color.white);
                rectX = 0;
                for (int i = 0; i < attacks.size(); i++) {
                    Attack a = (Attack)attacks.get(i);
                    Color c = Params.PLAYERCOLOR[getPlayerNumber(a.getPlayer())];
                    int rectWidth = a.getShips() * textWidth / totalShips;
                    g.setColor(c);
                    g.fillRect(x+2+rectX, y + 2*fontHeight - 1, rectWidth, fontHeight);
                    rectX += rectWidth;
                }
                g.setPaintMode();
            } else {
                Attack a = (Attack)attacks.get(0);
                g.setColor(Params.PLAYERCOLOR[getPlayerNumber(a.getPlayer())]);
                g.drawString(new Integer(a.getShips()).toString(), x + 2, y + 3*fontHeight - 1);
            }
        }
        if (ownerNum != Params.NEUTRAL) {
            // Draw ratio graph
            int ratio = planet.getRatio();
            int ratioHeight = (HIGHRATIO - planet.getRatio()) * (zeroRatioY - highRatioY) / HIGHRATIO;
            int ratioX = rightX - 2 - ratioWidth;
            g.setColor(LOWCOLOUR);
            g.fillRect(ratioX, lowRatioY + y, ratioWidth, lowRatioHeight);
            g.setColor(MIDCOLOUR);
            g.fillRect(ratioX, midRatioY + y, ratioWidth, midRatioHeight);
            g.setColor(HIGHCOLOUR);
            g.fillRect(ratioX, highRatioY + y, ratioWidth, highRatioHeight);
            g.setColor(Color.black);
            g.fillRect(ratioX, highRatioY + y, ratioWidth, ratioHeight);
        }
    }
}