package igx.client;

// StatusBar.java 

import java.awt.*;
import igx.shared.*;

public class StatusBar extends Canvas
{
  // Constants
  public final static int MODE_NORMAL     = 0;
  public final static int MODE_SELECT     = 1;
  public final static int MODE_SHIPS      = 2;
  public final static int MODE_MESSAGE    = 3;
  public final static int MODE_QUIT       = 4;
  public final static int MARGIN          = 1;
  public final static int MAX_SHIP_DIGITS = 6;

  int fontSize, fontHeight, fontStart, spaceWidth;
  public int width, height;
  int numPlayers;
  int mode = MODE_NORMAL;
  int messageTo = -1;
  int numShips = -1;
  int turn, segment;
  Planet selectedPlanet;
  Planet targetedPlanet;
  Font font;
  FontMetrics fm;
  GameInstance game;
  int playerNum;
  String playerName;
  public StatusBar (GameInstance game, int fontSize, Toolkit toolkit, int width, int playerNum, String playerName) {
	super();
	this.game = game;
	this.width = width;
	this.playerNum = playerNum;
	this.playerName = playerName;
	setBackground(Color.black);
	font = new Font("SansSerif", Font.PLAIN, fontSize);
	fm = toolkit.getFontMetrics(font);
	spaceWidth = fm.charWidth(' ');
	fontHeight = fm.getAscent() + fm.getDescent() + 2;
	fontStart = fm.getAscent();
	numPlayers = game.players;
	height = (numPlayers + 1 + 4) * fontHeight + MARGIN + 1; // Four lines of status info
  }  
  public void addShips (int digit) {
	String ships;
	if (numShips == -1)
	  ships = "";
	else
	  ships = new Integer(numShips).toString();
	ships += new Integer(digit).toString();
	setShips(Integer.parseInt(ships));
  }  
  public void clearLine (Graphics g, int lineNumber) {
	Color oldColour = g.getColor();
	g.setColor(Color.black);
	g.fillRect(0, lineNumber*fontHeight + MARGIN, width, fontHeight - 1);
	g.setColor(oldColour);
  }  
  Graphics clearMode () {
	Graphics g = getDefaultGraphics();
	mode = MODE_NORMAL;
	messageTo = -1;
	numShips = -1;
	selectedPlanet = targetedPlanet = null;
	clearStatus(g);
	return g;
  }  
  public void clearStatus (Graphics g) {
	for (int i = numPlayers + 1; i < numPlayers + 5; i++)
	  clearLine(g, i);
  }  
  public void drawDistanceInfo (Graphics g) {
	CText[] text = new CText[5];
	text[0] = new CText("ETA:", Color.lightGray);
	text[1] = new CText(" " + turn, Color.white);
	text[2] = new CText(":", Color.lightGray);
	text[3] = new CText(new Integer(segment).toString(), Color.white);
	text[4] = new CText(".", Color.lightGray);
	drawLine(g, text, numPlayers+3);
  }  
  public void drawLine (Graphics g, CText[] text, int lineNumber) {
	int y = (lineNumber) * fontHeight + MARGIN + fontStart;
	int x = 1;
	for (int i = 0; i < text.length; i++) {
	  g.setColor(text[i].color);
	  if ((text[i].text.length() > 0) && (text[i].text.charAt(0) == ' '))
	x+= spaceWidth;
	  g.drawString(text[i].text, x, y);
	  x += fm.stringWidth(text[i].text);
	}
  }  
  public void drawMessageInfo (Graphics g) {
	CText[] text = new CText[2];
	text[0] = new CText("Message to", Color.lightGray);
	if (messageTo == -1)
	  text[1] = new CText(" ", Color.lightGray);
	else if (messageTo == Player.NEUTRAL.number + 1)
		text[1] = new CText(" Forum", Color.white);
	else if (messageTo == Player.NEUTRAL.number) 
	  text[1] = new CText(" Game", Color.white);
	else 
	  text[1] = new CText(" " + game.player[messageTo].name, game.player[messageTo].getColor());
	drawLine(g, text, numPlayers+1);
	if (messageTo != -1) {
	  text = new CText[1];
	  text[0] = new CText("Type message", Color.lightGray);
	  drawLine(g, text, numPlayers+2);
	  text = new CText[3];
	  text[0] = new CText("Click", Color.lightGray);
	  text[1] = new CText(" MESSAGE", Color.white);
	  text[2] = new CText(" button to send", Color.lightGray);
	  drawLine(g, text, numPlayers+3);
	}
  }  
  public void drawPlanetInfo (Graphics g, Planet planet, int lineNumber) {
	CText[] text = new CText[6];
	text[0] = new CText(new Character(planet.planetChar).toString(), planet.owner.getColor());
	if (planet.owner == Player.NEUTRAL) {
	  text[1] = new CText(" (", Color.lightGray);
	  if (planet.blackHole)
	text[2] = new CText("Black Hole", Params.NEUTRALCOLOR);
	  else
	text[2] = new CText("Neutral", Params.NEUTRALCOLOR);
	  text[3] = new CText(")", Color.lightGray);
	  text[4] = text[5] = new CText("", Color.black);
	} else {
	  text[1] = new CText(" (", Color.lightGray);
	  text[2] = new CText(new Integer(planet.ships).toString(), Color.white);
	  text[3] = new CText(" ships,", Color.lightGray);
	  text[4] = new CText(" " + new Integer(planet.ratio).toString(), Color.white);
	  text[5] = new CText("% ratio)", Color.lightGray);
	}
	drawLine(g, text, lineNumber);
  }  
public void drawQuitInfo(Graphics g) {
	CText[] text = new CText[3];
	text[0] = new CText("Press <", Color.lightGray);
	text[1] = new CText("R", Color.white);
	text[2] = new CText("> for \"ready to quit\" or", Color.lightGray);
	drawLine(g, text, numPlayers + 2);
	text[0] = new CText("press <", Color.lightGray);
	text[1] = new CText("Y", Color.white);
	text[2] = new CText("> to quit.", Color.lightGray);
	drawLine(g, text, numPlayers + 3);
}
public void drawScoreInfo(Graphics g) {
	// Sort these players... and makes sure it takes n x n time, too.
	Player[] player = new Player[numPlayers];
	for (int i = 0; i < numPlayers; i++) {
		int rank = 0; // Number of players who are BETTER than this one
		for (int j = 0; j < numPlayers; j++) {
			if (i == j)
				continue;
			if (game.player[j].score > game.player[i].score)
				rank++;
			else
				if ((game.player[j].score == game.player[i].score) && (j < i))
					rank++;
		}
		player[rank] = game.player[i];
	}
	for (int i = 0; i < numPlayers; i++) {
		String name = "(" + (player[i].number+1) + ") " +player[i].name;
		g.setColor(player[i].getColor());
		if (!player[i].isHuman)
			name += "(robot)";
		else if (!player[i].isActive)
			name += "(quit)";
		else if (!player[i].isPresent)
			name += "(missing)";
		else if (player[i].status == Params.READY_SIGNAL)
			name += "(ready to quit)";
		clearLine(g, i);
		g.drawString(name, 1, fontHeight * i + MARGIN + fontStart);
		String score = new Integer(player[i].score).toString();
		if (player[i].isActive)
			g.drawString(score, 3 * width / 4, fontHeight * i + MARGIN + fontStart);
	}
}
  public void drawSelectInfo (Graphics g) {
	drawPlanetInfo(g, selectedPlanet, numPlayers + 1);
  }  
  public void drawSendInfo (Graphics g) {
	drawPlanetInfo(g, selectedPlanet, numPlayers + 1);
	drawPlanetInfo(g, targetedPlanet, numPlayers + 2);
	drawDistanceInfo(g);
	if (numShips != -1)
	  drawShipInfo(g);
  }  
  public void drawShipInfo (Graphics g) {
	CText[] text = new CText[3];
	text[0] = new CText("Send", Color.lightGray);
	text[1] = new CText(" " + new Integer(numShips).toString(), Color.white);
	text[2] = new CText(" ships.", Color.lightGray);
	drawLine(g, text, numPlayers+4);
  }  
  public void drawTimeInfo (Graphics g) {
	CText[] text = new CText[6];
	text[0] = new CText("Time", Color.lightGray);
	text[1] = new CText(" " + game.turn, Color.white);
	text[2] = new CText(":", Color.lightGray);
	text[3] = new CText(new Integer(game.segment).toString(), Color.white);
	text[4] = new CText(" You: ", Color.lightGray);
	if ((playerNum != -1) && (game.player[playerNum].isActive))
		text[5] = new CText(game.player[playerNum].name, Params.PLAYERCOLOR[playerNum]);
	else
		text[5] = new CText(playerName, Color.white);
	drawLine(g, text, numPlayers);
	g.setColor(Color.gray);
	g.drawLine(0, (numPlayers+1) * fontHeight + MARGIN - 1, width, (numPlayers+1) * fontHeight + MARGIN - 1);
  }  
  public Graphics getDefaultGraphics () {
	Graphics g = getGraphics();
	g.setFont(font);
	return g;
  }  
  public int getShips () {
	return numShips;
  }  
  // Called by keyboard interface
  public void messageMode () {
	Graphics g = clearMode();
	mode = MODE_MESSAGE;
	drawMessageInfo(g);
  }  
  public void messageMode (int messageTo) {
	Graphics g = clearMode();
	this.messageTo = messageTo;
	mode = MODE_MESSAGE;
	drawMessageInfo(g);
  }  
  public void nextTurn () {
	Graphics g = getDefaultGraphics();
	clearLine(g, numPlayers);
	drawScoreInfo(g);
	drawTimeInfo(g);
	if (mode == MODE_SHIPS) {
	  planetDistance();
	  clearLine(g, numPlayers+3);
	  drawDistanceInfo(g);
	}
  }  
  public void normalMode () {
	clearMode();
  }  
  public void paint (Graphics g) {
	// Draw players
	g.setFont(font);
	drawScoreInfo(g);
	g.setColor(Color.gray);
	g.drawLine(0, fontHeight * numPlayers + MARGIN - 1, width, fontHeight * numPlayers + MARGIN - 1);
	drawTimeInfo(g);
	switch (mode) {
	case MODE_SELECT:
	  drawSelectInfo(g);
	  break;
	case MODE_SHIPS:
	  drawSendInfo(g);
	  break;
	case MODE_MESSAGE:
	  drawMessageInfo(g);
	  break;
	case MODE_QUIT:
	  drawQuitInfo(g);
	  break;
	}
	g.setColor(Color.gray);
	// g.drawLine(0, height - 1, width, height - 1);
  }  
  public void planetChanged (int planetNum) {
	Planet planet = game.planet[planetNum];
	if ((selectedPlanet != null) && (planet.planetChar == selectedPlanet.planetChar))
	  updateSelectedPlanet();
	else if ((targetedPlanet != null) && (planet.planetChar == targetedPlanet.planetChar))
	  updateTargetedPlanet();
  }  
  public void  planetDistance () {
	int deltaX = selectedPlanet.x - targetedPlanet.x;
	int deltaY = selectedPlanet.y - targetedPlanet.y;
	int distance = (int)(Math.sqrt(deltaX*deltaX + deltaY*deltaY)*Params.SEGMENTS/Params.TURNSPEED + 0.99999999);
	turn = game.turn + (distance / Params.SEGMENTS);
	segment = game.segment + (distance % Params.SEGMENTS);
	if (segment >= Params.SEGMENTS) {
	  segment -= Params.SEGMENTS;
	  turn++;
	}
  }  
  public void planetMoved (Planet movedPlanet) {
	if ((targetedPlanet != null) && ((movedPlanet.planetChar == selectedPlanet.planetChar) ||
				     (movedPlanet.planetChar == targetedPlanet.planetChar))) {
	  planetDistance();
	  Graphics g = getDefaultGraphics();
	  clearLine(g, numPlayers+3);
	  drawDistanceInfo(g);
	}
  }  
public void quitMode() {
	Graphics g = clearMode();
	mode = MODE_QUIT;
	drawQuitInfo(g);
}
  public void selectMode (Planet planet) {
	Graphics g = clearMode();
	mode = MODE_SELECT;
	selectedPlanet = planet;
	drawSelectInfo(g);
  }  
  public void sendShipsMode (Planet planet) {
	Planet oldSelect = selectedPlanet;
	Graphics g = clearMode();
	mode = MODE_SHIPS;
	selectedPlanet = oldSelect;
	targetedPlanet = planet;
	planetDistance();
	drawSendInfo(g);
  }  
  public void setShips (int ships) {
	Graphics g = getDefaultGraphics();
	clearLine(g, numPlayers+4);
	numShips = ships;
	drawShipInfo(g);
  }  
  public void updateSelectedPlanet () {
	Graphics g = getDefaultGraphics();
	clearLine(g, numPlayers+1);
	drawPlanetInfo(g, selectedPlanet, numPlayers+1);
  }  
  public void updateTargetedPlanet () {
	Graphics g = getDefaultGraphics();
	clearLine(g, numPlayers+2);
	drawPlanetInfo(g, targetedPlanet, numPlayers+2);
  }  
}
