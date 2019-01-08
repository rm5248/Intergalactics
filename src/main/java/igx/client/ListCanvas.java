package igx.client;

// ListCanvas.java 

import igx.shared.*;
import java.awt.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ListCanvas extends JPanel
{
  public static final int MARGIN_FACTOR = 1;

  int width, height, fontHeight, fontDescent, rows, maxWidth;
  Vector strings = new Vector();
  Font font;
  FontMetrics fm;

  public ListCanvas (int width, int height, int fontSize, Toolkit toolkit) {
	this.width = width;
	this.height = height;
	font = new Font(Params.DEFAULT_FONT, Font.PLAIN, fontSize);
	fm = toolkit.getFontMetrics(font);
	fontDescent = fm.getDescent();
	fontHeight = fm.getAscent() + 1 + fontDescent;
	rows = height / fontHeight;
	setBackground(Color.black);
        
        Dimension d = new Dimension( width, height );
        setMinimumSize(d);
  }  
  public void addText (CText text) {
	strings.addElement(text);
	text.setWidth(fm.stringWidth(text.text));
	if (text.width > maxWidth)
	  maxWidth = text.width;
	repaint();
  }  
  public void addText (String text, Color colour) {
	addText(new CText(text, colour));
  }  
  public void changeColour (String key, Color color) {
	changeMultiColour(key, color);
	repaint();
  }  
  public void changeMultiColour (String key, Color color) {
	for (int i = 0; i < strings.size(); i++) {
	  CText text = (CText)(strings.elementAt(i));
	  if (text.text.equals(key))
	text.color = color;
	}
  }  
  public static void main (String[] args) {
	JFrame f = new JFrame("Toothless Joe Ward");
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	ListCanvas lc = new ListCanvas(400, 400, 20, toolkit);
	lc.addText("A", Color.blue);
	lc.addText("B", Color.blue);
	lc.addText("C", Color.blue);
	lc.addText("D", Color.blue);
	lc.addText("E", Color.blue);
	lc.addText("Sweet", Color.red);
	lc.addText("F", Color.blue);
	lc.addText("G", Color.blue);
	lc.addText("H", Color.blue);
	lc.addText("I", Color.blue);
	lc.addText("J", Color.blue);
	lc.addText("K", Color.blue);
	lc.addText("L", Color.blue);
	lc.addText("M", Color.blue);
	lc.addText("N", Color.blue);
	lc.addText("Game", Color.red);
	lc.addText("O", Color.blue);
	lc.addText("P", Color.blue);
	lc.addText("Q", Color.blue);
	lc.addText("R", Color.blue);
	f.add(lc);
	f.pack();
	f.setVisible( true );
	f.setSize(400, lc.height + 20); 
	f.validate();
  }  
  public void paint (Graphics g) {
      super.paint(g);
	g.setFont(font);
	int n = strings.size();
	for (int i = 0; i < n; i++) {
	  int rowNum = i / rows;
	  int x = (maxWidth + fontHeight * MARGIN_FACTOR) * rowNum + fontHeight * MARGIN_FACTOR;
	  int y = ((i % rows) + 1) * fontHeight - fontDescent;
	  CText text = (CText)(strings.elementAt(i));
	  g.setColor(text.color);
	  g.drawString(text.text, x, y);
	}
  }  
  public void removeText (String key) {
	maxWidth = 0;
	for (int i = 0; i < strings.size(); i++) {
	  CText text = (CText)(strings.elementAt(i));
	  if (text.text.equals(key)) 
	strings.removeElementAt(i);
	  else if (text.width > maxWidth)
	maxWidth = text.width;
	}
	repaint();
  }  
}