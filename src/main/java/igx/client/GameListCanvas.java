package igx.client;

// GameListCanvas.java 
import igx.shared.*;
import java.awt.*;
import java.util.*;

public class GameListCanvas extends ForumCanvas {

    public static final int HEADER_ROWS = 3;
    public static final int GAME_WIDTH_RATIO = 10;
    public static final Color HEADING_COLOUR = Color.lightGray;
    public static final Color HEADING_UNDERLINE_COLOUR = Color.gray;
    public static final Color NEW_GAME_COLOUR = Color.white;
    public static final Color IN_PROGRESS_GAME_COLOUR = Color.gray;
    public static final Color COMMA_COLOUR = Color.lightGray;
    public static final Color TITLE_COLOUR = Color.orange;
    public static final Color SERVER_COLOUR = Color.orange;
    public static final Color BORDER_COLOUR = Color.gray;

    int statusColumn, gameColumn, playerColumn;
    TextRow heading0, heading1;
    Vector gameRows;
    int games = 0;
    String serverName;
    ClientForum forum;

    public GameListCanvas(ClientForum forum, String serverName, int width, int rows, int fontSize, Toolkit toolkit) {
        super(width, computeHeight(fontSize, rows * 2 + HEADER_ROWS, toolkit), fontSize, toolkit);
        this.serverName = serverName;
        this.forum = forum;
        statusColumn = fontHeight;
        gameColumn = statusColumn + fm.stringWidth("In Progress  ");
        playerColumn = gameColumn + GAME_WIDTH_RATIO * fontHeight;
        Vector headingElements = new Vector();
        // First heading row
        // Compute center
        String title = Params.NAME + " - " + serverName;
        int titleWidth = fm.stringWidth(title);
        headingElements.addElement(new TextElement(Params.NAME, TITLE_COLOUR, (width - titleWidth) / 2));
        headingElements.addElement(new TextElement(" - ", COMMA_COLOUR, TextElement.SEQUENTIAL));
        headingElements.addElement(new TextElement(serverName, SERVER_COLOUR, TextElement.SEQUENTIAL));
        heading0 = new TextRow(headingElements);
        row[0] = heading0;
        // Second heading row
        headingElements = new Vector();
        headingElements.addElement(new TextElement("Status", HEADING_COLOUR, statusColumn));
        headingElements.addElement(new TextElement("Game", HEADING_COLOUR, gameColumn));
        headingElements.addElement(new TextElement("Players", HEADING_COLOUR, playerColumn));
        heading1 = new TextRow(headingElements);
        heading1.underline(HEADING_UNDERLINE_COLOUR);
        row[2] = heading1;
        setRowListener(this);
    }

    public void addGame(Game game) {
        // Insert row
        for (int i = games * 2 + HEADER_ROWS - 1; i >= HEADER_ROWS; i--) {
            row[i + 2] = row[i];
        }
        games++;
        if (selectedRow != -1) {
            selectedRow += 2;
        }
        // Make sure first row isn't selected.
        setRow(0, game);
        row[HEADER_ROWS].setSelect(false);
        repaint();
    }

    public int getRowNum(Game game) {
        for (int i = HEADER_ROWS; i < games * 2 + HEADER_ROWS; i += 2) {
            String name = ((TextElement) row[i].elements.elementAt(1)).text;
            if (name.equals(game.name)) {
                return i;
            }
        }
        return -1;
    }
    // An outside force selects a row

    public void manualSelectRow(int rowNumber) {
        if ((rowNumber + HEADER_ROWS) % 2 == 0) {
            super.rowSelected(rowNumber);
        } else {
            super.rowSelected(rowNumber - 1);
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(BORDER_COLOUR);
        g.drawRect(0, 0, width - 1, height);
    }

    public void removeGame(Game game) {
        int rowNum = getRowNum(game);
        if (rowNum == -1) {
            return;
        }
        if (selectedRow >= rowNum) {
            selectedRow -= 2;
        }
        for (int i = rowNum; i < games * 2 + HEADER_ROWS; i++) {
            row[i] = row[i + 2];
        }
        games--;
        repaint();
    }

    public void rowSelected(int rowNumber) {
        if (rowNumber >= HEADER_ROWS) {
            if (forum.gameSelected(rowNumber - HEADER_ROWS)) {
                manualSelectRow(rowNumber);
            }
        }
    }

    public void setRow(int rowNum, Game game) {
        boolean wasSelected = false;
        if ((row[rowNum + HEADER_ROWS] != null) && (row[rowNum + HEADER_ROWS].selected)) {
            wasSelected = true;
        }
        Vector rowVector = new Vector();
        Vector nextRowVector = new Vector();
        if (game.inProgress) {
            rowVector.addElement(new TextElement("In Progress", IN_PROGRESS_GAME_COLOUR, statusColumn));
            rowVector.addElement(new TextElement(game.name, IN_PROGRESS_GAME_COLOUR, gameColumn));
        } else {
            rowVector.addElement(new TextElement("New", NEW_GAME_COLOUR, statusColumn));
            rowVector.addElement(new TextElement(game.name, NEW_GAME_COLOUR, gameColumn));
        }
        for (int i = 0; i < game.numPlayers; i++) {
            Vector v = null;
            if (i < Params.MAXPLAYERS / 2) {
                v = rowVector;
            } else {
                v = nextRowVector;
            }
            if ((i == 0) || (i == Params.MAXPLAYERS / 2)) {
                v.addElement(new TextElement(game.player[i].name, Params.PLAYERCOLOR[i], playerColumn));
            } else {
                v.addElement(new TextElement(game.player[i].name, Params.PLAYERCOLOR[i], TextElement.SEQUENTIAL));
            }
            if (i < game.numPlayers - 1) {
                v.addElement(new TextElement(", ", COMMA_COLOUR, TextElement.SEQUENTIAL));
            }
        }
        row[rowNum + HEADER_ROWS] = new TextRow(rowVector);
        row[rowNum + HEADER_ROWS + 1] = new TextRow(nextRowVector);
        if (wasSelected) {
            row[rowNum + HEADER_ROWS].setSelect(true);
        }
        redrawRow(rowNum + HEADER_ROWS);
        redrawRow(rowNum + HEADER_ROWS + 1);
    }
}
