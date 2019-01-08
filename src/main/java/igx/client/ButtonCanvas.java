package igx.client;

// ButtonCanvas.java  

import igx.shared.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ButtonCanvas extends JPanel implements MouseListener
{
  protected Vector buttons = new Vector();
  protected SoftButton[] button;
  protected int numButtons;
  protected Font buttonFont;
  protected FontMetrics fm;
  public int buttonFontHeight, buttonFontDescent, buttonSpaceWidth;
  public int width, height;
  public int buttonHeight;
  ButtonListener listener = null;

  int currentButton = -1;

  public ButtonCanvas(int fontSize, Toolkit toolkit, int width, int height) {
	super();
	this.width = width;
	this.height = height;
	setBackground(Color.gray);
	buttonFont = new Font("SansSerif", Font.PLAIN, fontSize);
	fm = toolkit.getFontMetrics(buttonFont);
	buttonFontDescent = fm.getDescent();
	buttonFontHeight = fm.getAscent() + buttonFontDescent + 1;
	buttonSpaceWidth = fm.charWidth(' ');
	addMouseListener(this);
	buttonHeight = buttonFontHeight + SoftButton.BUTTON_MARGIN * 2;
        setPreferredSize( new Dimension( width, height ) );
	// numLines = (height) / fontHeight;
	// lines = new Vector(numLines + 1);
	// lines.addElement(new Vector());
	// currentLine = lineWidth = lineID = 0;
  }  
  public void addButton (int x, int y, String text) {
	Dimension dim = buttonDimensions(text);
	Rectangle r = new Rectangle(x, y, dim.width, dim.height);
	buttons.addElement(new SoftButton(r, text, buttonFontDescent));
  }  
  public Dimension buttonDimensions (String text) {
	return new Dimension(SoftButton.BUTTON_MARGIN * 2 + fm.stringWidth(text), buttonFontHeight + SoftButton.BUTTON_MARGIN * 2);
  }  
  public void buttonPressed (int buttonNum) {
	// System.out.println("Button \"" + button[buttonNum].getText() + "\" clicked.");
	if (listener != null)
	  listener.buttonPressed(button[buttonNum].getText());
  }  
  public int getButton (Point p) {
	for (int i = 0; i < numButtons; i++) 
	  if (button[i].contains(p))
	return i;
	return -1;
  }  
  public static void main (String[] args) {
	JFrame f = new JFrame("Beej's Buttons");
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	ButtonCanvas bc = new ButtonCanvas(14, toolkit, 400, 400);
	bc.addButton(10, 10, "(S)tart Game");
	bc.addButton(50, 50, "Add (C)omputer Player");
	bc.addButton(100, 100, "Sound On/Off (F2)");
	bc.addButton(200, 200, "(Q)uit");
	bc.prepareButtons();
	f.add(bc);
	f.pack();
	f.show();
	f.setSize(400,400); 
	f.validate();
  }  
  public void mouseClicked (MouseEvent e) {}  
  public void mouseEntered (MouseEvent e) {}  
  public void mouseExited(MouseEvent e) {
	if (currentButton != -1)
	  if (button[currentButton] != null) {
	button[currentButton].setHighlight(false);
	redrawButton(currentButton);
	  }
	currentButton = -1;
  }  
  public void mousePressed (MouseEvent e) {
	int buttonNum = getButton(e.getPoint());
	if (buttonNum != -1) {
	  currentButton = buttonNum;
	  button[buttonNum].setHighlight(true);
	  redrawButton(currentButton);
	}
  }  
  public void mouseReleased(MouseEvent e) {
	if (currentButton != -1) {
	  if (button[currentButton].contains(e.getPoint()))
	buttonPressed(currentButton);
	  button[currentButton].setHighlight(false);
	  redrawButton(currentButton);
	  currentButton = -1;
	}
  }  
  public void paint (Graphics g) {
	super.paint(g);
	g.setColor(Color.black);
	g.fillRect(0,0,width,height);
	g.setFont(buttonFont);
	for (int i = 0; i < numButtons; i++)
	  button[i].paint(g);
  }  
  public void prepareButtons () {
	numButtons = buttons.size();
	button = new SoftButton[numButtons];
	for (int i = 0; i < numButtons; i++) 
	  button[i] = (SoftButton)(buttons.elementAt(i));
	buttons = new Vector();
  }  
  public void redrawButton (int buttonNum) {
	Graphics g = getGraphics();
	g.setFont(buttonFont);
	if (g == null) {
	  repaint();
	  return;
	}
	button[buttonNum].paint(g);
  }  
  public void setButtonListener (ButtonListener listener) {
	this.listener = listener;
  }  
}