package igx.client;

// ForumCanvas.java 

import igx.shared.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ForumCanvas extends JPanel implements MouseListener, RowListener
{
  public static final Color SELECT_COLOUR = Color.red;
  
  RowListener listener = null;
  int fontSize, rows, width, height;
  Font font;
  FontMetrics fm;
  int fontHeight, fontDescent, spaceWidth;
  public TextRow[] row;
  int selectedRow = -1;
  
  public ForumCanvas (int width, int height, int fontSize, Toolkit toolkit) {
        setPreferredSize(new Dimension( width, height ));
	this.width = width;
	this.height = height;
	this.fontSize = fontSize;
	font = new Font(Params.DEFAULT_FONT, Font.PLAIN, fontSize);
	fm = toolkit.getFontMetrics(font);
	fontDescent = fm.getDescent();
	fontHeight = fm.getAscent() + fontDescent;
	if ((fontHeight % 2) == 0)
	  fontHeight++;
	spaceWidth = fm.charWidth(' ');
	rows = height / fontHeight;
	row = new TextRow[rows];
	setBackground(Color.black);
	addMouseListener(this);
  }  
  // Computes the required height of a ForumCanvas with a given number of rows
  public static int computeHeight (int fontSize, int rows, Toolkit toolkit) {
	Font font = new Font(Params.DEFAULT_FONT, Font.PLAIN, fontSize);
	FontMetrics fm = toolkit.getFontMetrics(font);
	int fontHeight = fm.getAscent() + fm.getDescent();
	return rows * fontHeight;
  }  
  public static void main (String[] args) {
	JFrame f = new JFrame("Rooty Poo");
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	ForumCanvas fc = new ForumCanvas(400, 400, 14, toolkit);
	int rows = fc.rows;
	Vector els = new Vector();
	TextElement e = new TextElement("The ", Color.gray, -1);
	els.addElement(e);
	e = new TextElement("" + (rows - 1) , Color.red, -1);
	els.addElement(e);
	e = new TextElement(" rows.", Color.gray, -1);
	els.addElement(e);
	TextRow row = new TextRow(els);
	row.center();
	row.underline(Color.blue);
	fc.row[0] = row;
	for (int i = 1; i < rows; i++) {
	  els = new Vector();
	  e = new TextElement("Row ", Color.white, 20);
	  els.addElement(e);
	  e = new TextElement("" + i, Color.green, 30);
	  els.addElement(e);
	  row = new TextRow(els);
	  if (i == 10)
	row.setSelect(true);
	  fc.row[i] = row;
	}
	fc.selectedRow = 10;
	fc.setRowListener(fc);
	f.add(fc);
	f.pack();
	f.show();
	f.setSize(400,400); 
	f.validate();
  }  
  public void mouseClicked (MouseEvent e) {}  
  public void mouseEntered (MouseEvent e) {}  
  public void mouseExited (MouseEvent e) {}  
  public void mousePressed (MouseEvent e) {
	if (listener != null) {
	  Point p = e.getPoint();
	  int rowNumber = p.y / fontHeight;
	  if (rowNumber < rows)
	listener.rowSelected(rowNumber);
	}
  }  
  public void mouseReleased (MouseEvent e) {}  
  public void paint (Graphics g) {
      super.paint(g);
	for (int i = 0; i < rows; i++)
	  if (row[i] != null)
	paintRow(g, i);
  }  
  protected void paintRow(Graphics g, int rowNumber) {
	g.setFont(font);
	TextRow r = row[rowNumber];
	TextElement e;
	int x = 1 + fontHeight;
	int y = fontHeight * (rowNumber + 1);
	if (r.centered) {
	  int totalWidth = 0;
	  for (int j = 0; j < r.numberOfElements; j++) {
	e = (TextElement) (r.elements.elementAt(j));
	totalWidth += e.getWidth(fm);
	  }
	  x = (width - totalWidth) / 2;
	}
	for (int j = 0; j < r.numberOfElements; j++) {
	  e = (TextElement) (r.elements.elementAt(j));
	  if (e.column != TextElement.SEQUENTIAL) {
	x = e.column;
	g.setColor(Color.black);
	g.fillRect(x, y - fontHeight + 1, width - 1 - x, fontHeight);
	  }
	  g.setColor(e.colour);
	  g.drawString(e.text, x, y - 1 - fontDescent);
	  x += e.getWidth(fm);
	}
	if (r.underlined) {
	  g.setColor(r.underlineColour);
	  g.drawLine(0, y, width, y);
	}
	if (r.selected) {
	  g.setColor(SELECT_COLOUR);
	  // g.drawRect(0, y - fontHeight + 1, width - 1, fontHeight - 1);
	  int[] pointX = {1, fontHeight / 2, 1};
	  int[] pointY = {y - fontHeight + 2, y - fontHeight + 1 + fontHeight / 2, y - 1};
	  g.drawPolygon(pointX, pointY, 3);
	  g.fillPolygon(pointX, pointY, 3);
	  int[] secondPointX = {width - 2, width - 1 - fontHeight / 2, width - 2};
	  g.drawPolygon(secondPointX, pointY, 3);
	  g.fillPolygon(secondPointX, pointY, 3);
	}
  }  
  public void redrawRow (int rowNumber) {
	Graphics g = getGraphics();
	if (g == null) {
	  repaint();
	  return;
	}
	int y = fontHeight * (rowNumber+1);
	g.setColor(Color.black);
	g.fillRect(1, y - fontHeight + 1, width - 2, fontHeight);
	if (row[rowNumber] != null)
	  paintRow(g, rowNumber);
  }  
  public void rowSelected (int rowNumber) {
	selectRow(rowNumber);
  }  
  public void selectRow (int rowNumber) {
	if ((selectedRow != -1) && (row[selectedRow] != null)) {
	  row[selectedRow].setSelect(false);
	  redrawRow(selectedRow);
	}
	selectedRow = rowNumber;
	if ((selectedRow != -1) && (row[selectedRow] != null)) {
	  row[selectedRow].setSelect(true);
	  redrawRow(selectedRow);
	}
  }  
  public void setRowListener (RowListener rl) {
	listener = rl;
  }  
}