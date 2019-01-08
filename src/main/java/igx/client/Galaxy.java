package igx.client;

// Galaxy.java

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import igx.shared.*;
import javax.swing.JPanel;

public class Galaxy extends JPanel
{
    //// Constants
    // Number of message rows
    final static int MESSAGE_ROWS = 3;
    // Thresholds and colours on ratio chart
    final static int LOWRATIO = Params.SPECIALRATIOMAX / 2;
    final static int MIDRATIO = 5 * Params.SPECIALRATIOMAX / 6;
    final static int HIGHRATIO = Params.SPECIALRATIOMAX;
    final static Color HIGHCOLOUR = Color.red;
    final static Color MIDCOLOUR = Color.yellow;
    final static Color LOWCOLOUR = Color.green;
    final static int MESSAGE_FONT_RATIO = 32;
    // the background planet colour
    final static Color PLANETCOLOUR = new Color(32, 32, 32);
    // Ratio between map height and ratio bar width
    final static int ATTACK_WIDTH_RATIO = 250;
    public final static int SCROLLBAR_WIDTH_RATIO = 48;
    public final static int SCROLLBAR_HEIGHT_RATIO = 16;
    // Colours
    public final static Color CONNECTION_COLOUR = Color.yellow;

    public String[] messageRow = new String[MESSAGE_ROWS];
    public int sliderValue = -1;
    int planetSize;
    int size;
    Font font;
    FontMetrics fontMetric;
    int fontHeight, ratioWidth;
    int zeroRatioY, lowRatioY, midRatioY, highRatioY;
    int highRatioHeight, midRatioHeight, lowRatioHeight;
    GameInstance game;
    int selectedPlanet = -1;
    int targetPlanet = -1;
    int sliderWidth, sliderHeight, sliderMax;
    // UI Monitor
    Monitor redrawMonitor;
    // message stuff
    Font messageFont;
    FontMetrics messageFM;
    int messageFontSize, messageFontHeight, messageFontDescent, messageY;
    String message = "";
    int messageInitialX;
    int[] messageX = new int[MESSAGE_ROWS];
    Toolkit toolkit;
    Player me = null;

    public Galaxy (GameInstance game, int size, int fontSize, Toolkit toolkit, Monitor redrawMonitor) {
        super();
        this.redrawMonitor = redrawMonitor;
        this.game = game;
        this.size = size;
        this.toolkit = toolkit;
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
        sliderHeight = size / SCROLLBAR_HEIGHT_RATIO;
        sliderWidth = size / SCROLLBAR_WIDTH_RATIO;
        sliderMax = size;
        messageFontSize = size / MESSAGE_FONT_RATIO;
        messageFont = new Font("SansSerif", Font.PLAIN, messageFontSize);
        messageFM = toolkit.getFontMetrics(messageFont);
        messageFontDescent = messageFM.getDescent();
        messageFontHeight = messageFM.getAscent() + messageFontDescent + 1;
        messageY = (planetSize - messageFontHeight) / 2 + messageFontHeight;
        messageInitialX = (planetSize - messageFontHeight) / 2;
        for (int i = 0; i < MESSAGE_ROWS; i++) {
            messageRow[i] = "";
            messageX[i] = messageInitialX;
        }
        setBackground(Color.black);
    }

    public void setMe (Player value) {
        me = value;
    }
    public void abortOrder () {
        Graphics g = getDefaultGraphics();
        if (targetPlanet != -1) {
            connectPlanets(g, selectedPlanet, targetPlanet);
            selectPlanet(g, targetPlanet);
            targetPlanet = -1;
            if (sliderValue != -1)
                eraseSlider(g);
            sliderValue = -1;
        }
        if (selectedPlanet != -1) {
            selectPlanet(g, selectedPlanet);
            selectedPlanet = -1;
        }
    }
    public void addMessageChar (char key) {
        Graphics g = getDefaultGraphics();
        // Find spot for key
        int rowNum = 0;
        while ((rowNum < MESSAGE_ROWS) && (messageRow[rowNum].length() > 0))
            rowNum++;
        if (rowNum != 0)
            rowNum--;
        int charWidth = messageFM.charWidth(key);
        if ((messageX[rowNum] + charWidth) > size)
            rowNum++;
        if (rowNum == MESSAGE_ROWS)
            return;
        else {
            messageRow[rowNum] += key;
            g.setColor(Color.white);
            g.setFont(messageFont);
            plotMessageChar(g, key, rowNum);
            messageX[rowNum] += charWidth;
            g.setFont(font);
        }
    }
    public void choosePlanet (int planetNum) {
        Graphics g = getDefaultGraphics();
        if (selectedPlanet != -1)
            selectPlanet(g, selectedPlanet);
        selectedPlanet = planetNum;
        selectPlanet(g, selectedPlanet);
    }
    public boolean choosePlanet (int mouseX, int mouseY) {
        int targetSquare = getPlanet(mouseX, mouseY);
        if (targetSquare == -1)
            return false;
        else {
            choosePlanet(targetSquare);
            return true;
        }
    }
    public void chooseTargetPlanet (int target) {
        Graphics g = getDefaultGraphics();
        targetPlanet = target;
        connectPlanets(g, selectedPlanet, targetPlanet);
        selectPlanet(g, targetPlanet);
    }
    public boolean chooseTargetPlanet (int mouseX, int mouseY) {
        int targetSquare = getPlanet(mouseX, mouseY);
        if ((targetSquare == -1) || (targetSquare == selectedPlanet))
            return false;
        else {
            chooseTargetPlanet(targetSquare);
            return true;
        }
    }
    public void connectPlanets(Graphics g, int selectedPlanet, int targetPlanet) {
        g.setXORMode(Color.black);
        Planet fromPlanet = game.planet[selectedPlanet];
        Planet toPlanet = game.planet[targetPlanet];
        int deltaX = toPlanet.x - fromPlanet.x;
        int deltaY = toPlanet.y - fromPlanet.y;
        float dX = deltaX;
        float dY = deltaY;
        int fromX, fromY, toX, toY;
        int intersectPoint;
        int planetLength = planetSize - 1;
        if (Math.abs(deltaY) > Math.abs(deltaX)) {
            if (deltaY < 0) {
                fromY = planetSize * fromPlanet.y - 1;
                toY = planetSize * toPlanet.y + planetSize;
                deltaY = -deltaY;
            } else {
                fromY = planetSize * fromPlanet.y + planetSize;
                toY = planetSize * toPlanet.y - 1;
            }
            intersectPoint = planetLength * (planetSize + planetSize * deltaX / deltaY) / 2 / planetSize;
            fromX = planetSize * fromPlanet.x + intersectPoint;
            toX = planetSize * toPlanet.x + planetSize - 1 - intersectPoint;
        } else {
            if (deltaX < 0) {
                fromX = planetSize * fromPlanet.x - 1;
                toX = planetSize * toPlanet.x + planetSize;
                deltaX = -deltaX;
            } else {
                fromX = planetSize * fromPlanet.x + planetSize;
                toX = planetSize * toPlanet.x - 1;
            }
            intersectPoint = planetLength * (planetSize + planetSize * deltaY / deltaX) / 2 / planetSize;
            fromY = planetSize * fromPlanet.y + intersectPoint;
            toY = planetSize * toPlanet.y + planetSize - 1 - intersectPoint;
        }
        g.setColor(CONNECTION_COLOUR);
        g.drawLine(toX, toY, fromX, fromY);
        // Do up arrow
        float arrowLeftX =  dY - dX;
        float arrowLeftY = -dX - dY;
        float arrowRightX = -dY - dX;
        float arrowRightY = dX - dY;
        float arrowLength = (float)Math.sqrt(arrowRightX * arrowRightX + arrowRightY * arrowRightY);
        arrowLeftX  *= planetSize / arrowLength / 3;
        arrowLeftY  *= planetSize / arrowLength / 3;
        arrowRightX *= planetSize / arrowLength / 3;
        arrowRightY *= planetSize / arrowLength / 3;
        g.drawLine(toX, toY, (toX + (int)arrowLeftX),  (toY + (int)arrowLeftY));
        g.drawLine(toX, toY, (toX + (int)arrowRightX), (toY + (int)arrowRightY));
        g.setPaintMode();
    }
    public void drawSlider (Graphics g) {
        if (sliderValue == -1)
            return;
        g.setColor(Color.gray);
        g.drawRect(Params.MAPX * planetSize, sliderValue - sliderHeight, sliderWidth - 1, sliderHeight);
        g.setColor(Color.black);
        g.drawLine(Params.MAPX * planetSize + sliderWidth / 2 - 1, sliderValue + 1 - sliderHeight,
                   Params.MAPX * planetSize + sliderWidth / 2 - 1, sliderValue - 1);
    }
    public void drawSliderBar (Graphics g) {
        g.setColor(Color.gray);
        g.drawLine(Params.MAPX * planetSize + sliderWidth / 2 - 1, 0,
                   Params.MAPX * planetSize + sliderWidth / 2 - 1, size);
    }
    public String endMessage () {
        repaintMessageRow();
        String message = "";
        for (int i = 0; i < MESSAGE_ROWS; i++) {
            message += messageRow[i];
            messageRow[i] = "";
            messageX[i] = messageInitialX;
        }
        return message;
    }
    public void eraseMessageChar () {
        for (int i = MESSAGE_ROWS - 1; i >= 0; i--)
            if (messageRow[i].length() > 0) {
        repaintMessageRow();
        messageRow[i] = messageRow[i].substring(0, messageRow[i].length() - 1);
        messageX[i] = messageFM.stringWidth(messageRow[i]) + messageInitialX;
        paintMessage();
        return;
            }
    }
    public void eraseSlider (Graphics g) {
        g.setColor(Color.black);
        g.fillRect(Params.MAPX * planetSize, sliderValue - sliderHeight, sliderWidth, sliderHeight+1);
        g.setColor(Color.gray);
        g.drawLine(Params.MAPX * planetSize + sliderWidth / 2 - 1, sliderValue - sliderHeight,
                   Params.MAPX * planetSize + sliderWidth / 2 - 1, sliderValue);
    }
    public Graphics getDefaultGraphics () {
        Graphics g = getGraphics();
        g.setFont(font);
        g.setPaintMode();
        return g;
    }
    int getPlanet (int x, int y) {
        int mapX = x / planetSize;
        int mapY = y / planetSize;
        if ((mapX >= Params.MAPX) || (mapY >= Params.MAPY))
            return -1;
        int targetSquare = game.map[mapX][mapY];
        if (targetSquare == Params.SPACE)
            return -1;
        else
            return(Planet.char2num((char)targetSquare));
    }
    public void movePlanet (int oldX, int oldY, Planet planet) {
        Graphics g = getDefaultGraphics();
        int x = planetSize * oldX;
        int y = planetSize * oldY;
        g.setColor(Color.black);
        g.fillRect(x, y, planetSize, planetSize);
        repaintPlanet(g, Planet.char2num(planet.planetChar));
    }
    public void paint (Graphics g) {
        super.paint(g);
        // LOCK
        redrawMonitor.lock();
        g.setFont(font);
        g.setPaintMode();
        drawSliderBar(g);
        for (int i = 0; i < Params.PLANETS; i++)
            repaintPlanet(g, i);
        if (selectedPlanet != -1) {
            selectPlanet(g, selectedPlanet);
            if (targetPlanet != -1) {
                selectPlanet(g, targetPlanet);
                connectPlanets(g, selectedPlanet, targetPlanet);
                drawSlider(g);
            }
        }
        g.setXORMode(Color.black);
        drawOldFleets(g);
        g.setPaintMode();
        if (message.length() != 0)
            paintMessage();
        // UNLOCK
        redrawMonitor.unlock();
    }
    void paintMessage () {
        Graphics g = getDefaultGraphics();
        g.setColor(Color.white);
        g.setFont(messageFont);
        for (int i = 0; i < MESSAGE_ROWS; i++)
            if (messageRow[i].length() > 0)
                g.drawString(messageRow[i], messageInitialX, messageY + messageFontHeight * i - messageFontDescent);
    }
    /*  public static void main (String[] args) {
 Frame frame = new Frame("igx Part Deux");
 Toolkit toolkit = Toolkit.getDefaultToolkit();
 Dimension screenSize = toolkit.getScreenSize();
    // screenSize = new Dimension(640,480);
 Insets in = frame.getInsets();
 screenSize.width -= (in.left + in.right);
 screenSize.height -= (in.bottom + in.top + 18);
 screenSize.height = screenSize.height / 16 * 16;
 UI ui = new igx.server.ServerUI();
 Player beej = new Player("Beej", 0);
 Player john = new Player("John", 1);
 Player[] players = {beej, john};
 GameInstance theGame = new GameInstance(500, 2, players);
 theGame.registerUI(ui);
 theGame.planet[4].ratio = 60;
 Galaxy galaxy = new Galaxy(theGame, screenSize.height, 10, toolkit);
 galaxy.selectedPlanet = 0;
 galaxy.targetPlanet = 9;
 ScrollText news = new ScrollText(12, toolkit, screenSize.width - screenSize.height, screenSize.height);
 frame.add(galaxy, BorderLayout.WEST);
 frame.add(news, BorderLayout.EAST);
 frame.pack();
 frame.show();
 frame.setSize(screenSize);
 news.setSize(screenSize.width - screenSize.height - screenSize.height / SCROLLBAR_WIDTH_RATIO, screenSize.height);
 galaxy.setSize(screenSize.height + screenSize.height / SCROLLBAR_WIDTH_RATIO, screenSize.height);
 CText text1 = new CText("Christmas Chortles", Color.red);
 CText text2 = new CText("BJ can't", Color.gray);
 CText text3 = new CText("program", Color.blue);
 CText text4 = new CText("worth beans. In fact, he once managed an infinite", Color.gray);
 CText text5 = new CText("loop", Color.red);
 CText text6 = new CText("on a", Color.gray);
 CText text7 = new CText("non-programmable", Color.yellow);
 CText text8 = new CText("calculator.", Color.gray);
 while (true) {
   news.addText(text1);
   news.newLine();
   news.addText(text2);
   news.addText(text3);
   news.addText(text4);
   news.addText(text5);
   news.addText(text6);
   news.addText(text7);
   news.addText(text8);
   news.newLine();
   try {
 Thread.sleep(3000);
   } catch (InterruptedException e) {}
    // galaxy.targetPlanets(theGame.pseudo(0, 35), theGame.pseudo(0, 35));
 }
  }*/

    public void plotMessageChar (Graphics g, char key, int i) {
        g.drawString(new Character(key).toString(), messageX[i], messageY + messageFontHeight * i - messageFontDescent);
    }
    public void redrawPlanet (int planetNum) {
        Graphics g = getDefaultGraphics();
        repaintPlanet(g, planetNum);
    }
    void repaintMessageRow() {
        Graphics g = getDefaultGraphics();
        int rowNum = 0;
        while ((rowNum < MESSAGE_ROWS) && (messageRow[rowNum].length() > 0))
            rowNum++;
        if (--rowNum == -1)
            return;
        int galaxyRows = ((rowNum + 1) * messageFontHeight + messageY) / planetSize + 1;
        for (int j = 0; j < galaxyRows; j++)
            for (int i = 0; i < Params.MAPX; i++) {
        if (game.map[i][j] == Params.SPACE) {
            g.setColor(Color.black);
            g.fillRect(i * planetSize, j * planetSize, planetSize, planetSize);
        } else
            repaintPlanet(g, Planet.char2num(game.map[i][j]));
            }
    }
    public void repaintPlanet (Graphics g, int planetNum) {
        Planet planet = game.planet[planetNum];
        int x = planetSize*planet.x;
        int y = planetSize*planet.y;
        int rightX = x + planetSize - 1;
        int bottomY = y + planetSize - 1;
        // Clear background and draw background planet
        int planetWidth = planetSize * planet.planetSize / 100;
        g.setColor(Color.black);
        g.fillRect(x, y, planetSize, planetSize);
        g.setColor(new Color(planet.planetShade, planet.planetShade, planet.planetShade));
        g.fillOval((x + planetSize / 2) - planetWidth / 2, (y + planetSize / 2) - planetWidth /2,
                   planetWidth, planetWidth);
        // Set to owner's colour
        g.setColor(Params.PLAYERCOLOR[planet.owner.number]);
 /*g.drawLine(x, y, rightX, y);
   g.drawLine(rightX, y, rightX, bottomY);
   g.drawLine(rightX, bottomY, x, bottomY);
   g.drawLine(x, bottomY, x, y);*/
        // If neutral just draw planet
        if (planet.owner.number == Params.NEUTRAL)
            g.drawString(new Character(planet.planetChar).toString(), x + 2, y + fontHeight - 1);
        else {
            // Draw planet and production
            g.drawString(planet.planetChar + "  " + planet.production, x + 2, y + fontHeight - 1);
            // Draw number of ships
            g.drawString(new Integer(planet.ships).toString(), x + 2, y + 2*fontHeight - 1);
        }
        // Draw number of attacking ships
        if (planet.attackingPlayer != Planet.NO_ATTACKERS) {
            if (planet.attackingPlayer == Planet.MULTIPLE_ATTACKERS) {
                // How about a little ANDing with a rectangle?
                String text = new Integer(planet.totalAttackingShips).toString();
                int textWidth = fontMetric.stringWidth(text);
                int rectX = 0;
                g.setXORMode(Color.white);
                for (int i = 0; i < game.players; i++) {
                    if (planet.attacker[i] == 0)
                        continue;
                    Color c = Params.PLAYERCOLOR[i];
                    int rectWidth = planet.attacker[i] * textWidth / planet.totalAttackingShips;
                    g.setColor(c);
                    g.fillRect(x+2+rectX, y + 2*fontHeight - 1, rectWidth, fontHeight);
                    rectX += rectWidth;
                }
                g.setPaintMode();
                g.setColor(Color.white);
                g.drawString(text, x + 2, y + 3*fontHeight - 1);
                g.setXORMode(Color.white);
                rectX = 0;
                for (int i = 0; i < game.players; i++) {
                    if (planet.attacker[i] == 0)
                        continue;
                    Color c = Params.PLAYERCOLOR[i];
                    int rectWidth = planet.attacker[i] * textWidth / planet.totalAttackingShips;
                    g.setColor(c);
                    g.fillRect(x+2+rectX, y + 2*fontHeight - 1, rectWidth, fontHeight);
                    rectX += rectWidth;
                }
                g.setPaintMode();
            } else {
                g.setColor(Params.PLAYERCOLOR[planet.attackingPlayer]);
                g.drawString(new Integer(planet.totalAttackingShips).toString(), x + 2, y + 3*fontHeight - 1);
            }
        }
        if (planet.owner.number != Params.NEUTRAL) {
            // Draw ratio graph
            int ratio = planet.ratio;
            int ratioHeight = (HIGHRATIO - planet.ratio) * (zeroRatioY - highRatioY) / HIGHRATIO;
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

    Vector oldFleets = new Vector();

    public void drawFleet (Graphics g, int x, int y, int color) {
        // Draw center
        g.setColor(Color.white);
        g.drawLine(x,y,x,y);
        // Draw top and bottom
        g.setColor(Color.gray);
        g.drawLine(x-1,y-1,x+1,y-1);
        g.drawLine(x-1,y+1,x+1,y+1);
        // Draw sides
        g.setColor(Color.lightGray);
        g.drawLine(x-2,y,x-2,y);
        g.drawLine(x+2,y,x+2,y);
        // Draw coloured part
        g.setColor(Params.PLAYERCOLOR[color]);
        g.drawLine(x-1,y,x-1,y);
        g.drawLine(x+1,y,x+1,y);
    }

    public void drawOldFleets (Graphics g) {
        g.setXORMode(Color.black);
        g.setColor(Color.white);
        int n = oldFleets.size();
        for (int i = 0; i < n; i++) {
            Point p = (Point)(oldFleets.elementAt(i));
            drawFleet(g, p.x, p.y, me.number);
        }
        g.setPaintMode();
    }

    public void drawFleets () {
        Graphics g = getDefaultGraphics();
        drawOldFleets(g);
        g.setXORMode(Color.black);
        //g.setPaintMode();
        oldFleets = new Vector();
        g.setColor(Color.white);
        Fleet f = game.fleets.first;
        while (f != null) {
            if ((f.owner == me) && (f.distance > 0)) {
                // Draw this damn fleet
                int startX = f.source.x * planetSize + planetSize/2;
                int startY = f.source.y * planetSize + planetSize/2;
                int stopX = f.destination.x * planetSize + planetSize/2;
                int stopY = f.destination.y * planetSize + planetSize/2;
                float totalDistance = (float)Math.sqrt((f.destination.x - f.source.x) * (f.destination.x - f.source.x) +
                        (f.destination.y - f.source.y) * (f.destination.y - f.source.y));
                float progress = f.distance / totalDistance;
                int x = stopX + (int)((startX - stopX) * progress);
                int y = stopY + (int)((startY - stopY) * progress);
                Point p = new Point(x,y);
                oldFleets.addElement(p);
                drawFleet(g, p.x, p.y, me.number);
            }
            f = f.next;
        }
        g.setPaintMode();
    }

    public void repaintXORs () {
        Graphics g = getDefaultGraphics();
        if (targetPlanet != -1) {
            connectPlanets(g, selectedPlanet, targetPlanet);
            selectPlanet(g, targetPlanet);
        }
        if (selectedPlanet != -1)
            selectPlanet(g, selectedPlanet);
        drawOldFleets(g);
    }
    public void selectPlanet (Graphics g, int planetNum) {
        g.setXORMode(Color.black);
        Planet planet = game.planet[planetNum];
        int x = planetSize*planet.x;
        int y = planetSize*planet.y;
        int rightX = x + planetSize - 1;
        int bottomY = y + planetSize - 1;
        g.setColor(Params.PLAYERCOLOR[planet.owner.number]);
        g.drawLine(x, y, rightX, y);
        g.drawLine(rightX, y, rightX, bottomY);
        g.drawLine(rightX, bottomY, x, bottomY);
        g.drawLine(x, bottomY, x, y);
        g.setPaintMode();
    }
    public int sendShips (int mouseY) {
        int max = size - sliderHeight;
        mouseY -= sliderHeight;
        if (mouseY > max)
            mouseY = max;
        if (mouseY < 0)
            mouseY = 0;
        if (game.planet[selectedPlanet].owner.number == Params.NEUTRAL)
            return 0;
        else
            return (max - mouseY) * game.planet[selectedPlanet].ships / max;
    }
    public void setShipSlider (int y) {
        Graphics g = getDefaultGraphics();
        if (sliderValue != -1)
            eraseSlider(g);
        if (y > sliderMax)
            sliderValue = sliderMax;
        else if (y < sliderHeight)
            sliderValue = sliderHeight;
        else
            sliderValue = y;
        drawSlider(g);
    }
}