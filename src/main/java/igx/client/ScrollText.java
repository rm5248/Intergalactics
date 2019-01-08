package igx.client;

// ScrollText.java 

import java.awt.*;
import java.util.*;

public class ScrollText extends Canvas
{
  // Constants
  public static final int MARGIN = 1;
  // Data fields
  int fontSize, fontHeight;
  Font font;
  FontMetrics fm;
  Vector lines;
  int numLines, width, currentLine, lineWidth, spaceWidth, numElements, lineID;
  int height;
  int topMargin;

  public ScrollText (int fontSize, Toolkit toolkit, int width, int height) {
	/*    Dimension dimQ = buttonDimensions("(Q)uit");
	Dimension dimM = buttonDimensions("Message (F1)");
	Dimension dimS = buttonDimensions("(S)ound Off");    
	addButton(2, 2, "Message (F1)");
	addButton(2, 5 + dimQ.height, "(F)orum");
	addButton(width - 2 - dimS.width, 2, "(S)ound Off");
	addButton(width - 2 - dimS.width, 5 + dimQ.height, "(Q)uit");
	prepareButtons();
	topMargin = 5 + 2 * dimQ.height;
	*/
	this.width = width - MARGIN;
	this.height = height;
	font = new Font("SansSerif", Font.PLAIN, fontSize);
	fm = toolkit.getFontMetrics(font);
	fontHeight = fm.getAscent() + 1;
	spaceWidth = fm.charWidth(' ');
	numLines = (height - MARGIN) / fontHeight;
	lines = new Vector(numLines + 1);
	lines.addElement(new Vector());
	currentLine = lineWidth = lineID = 0;
  }  
  public void addText (CText text) {
	Vector thisLine;
	CText currentText;
	if (text.text == null) { // Newline
	  lineWidth = 0;
	  currentLine++;      
	  lines.addElement(new Vector());
	} else {
	  Vector words = getWords(text.text);
	  String word;
	  int wordWidth;
	  currentText = new CText("", text.color);
	  for (int i = 0; i < words.size(); i++) {
	word = (String)words.elementAt(i);
	wordWidth = fm.stringWidth(word) + spaceWidth;
	if (wordWidth + lineWidth < width) {
	  currentText.text += word; // + " ";
	  lineWidth += wordWidth;
 	} else {
	  thisLine = (Vector)lines.elementAt(currentLine);
	  currentText.setWidth(fm.stringWidth(currentText.text));
	  thisLine.addElement(currentText);
	  currentLine++;
	  lineWidth = wordWidth + spaceWidth;
	  thisLine = new Vector();
	  lines.addElement(thisLine);
	  currentText = new CText(" " + word/* + " "*/, text.color);
	}
	  }
	  thisLine = (Vector)lines.elementAt(currentLine);
	  currentText.setWidth(fm.stringWidth(currentText.text));
	  // -- Remove trailing space
	  // currentText.text = currentText.text.substring(0, currentText.text.length() - 1);
	  thisLine.addElement(currentText);
	}
	while (currentLine >= numLines) {
	  lines.removeElementAt(0);
	  currentLine--;
	}
	repaint();
  }  
  public Dimension getMinimumSize () {
	return new Dimension(width, height);
  }  
  public Dimension getPreferredSize () {
	return new Dimension(width, height);
  }  
  public Vector getWords (String text) {
	Vector words = new Vector();
	int lastIndex = 0;
	int index;
	while ((index = text.indexOf(' ', lastIndex)) != -1) {
	  words.addElement(text.substring(lastIndex, index+1));
	  lastIndex = index + 1;
	}
	words.addElement(text.substring(lastIndex, text.length()));
	return words;
  }  
  public void newLine () {
	addText(new CText(null, null));
  }  
  public void paint (Graphics g) {
	/*    super.paint(g);
	g.setColor(Color.gray);
	g.drawLine(0, topMargin+3, width, topMargin+3);
	*/
	g.setFont(font);
	Vector currentLineOfText;
	CText text;
	int x, y, numPhrases;
	y = fontHeight + MARGIN;
	for (int i = 0; i < currentLine; i++) {
	  currentLineOfText = (Vector)lines.elementAt(i);
	  x = 1;
	  numPhrases = currentLineOfText.size();
	  for (int o = 0; o < numPhrases; o++) {
	text = (CText)currentLineOfText.elementAt(o);
	g.setColor(text.color);
	g.drawString(text.text, x, y);
	x += text.width;
	  }
	  y += fontHeight;
	}
  }  
}