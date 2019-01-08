package igx.client;

// DialogCanvas.java 

import igx.shared.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.IOException;

public class DialogCanvas extends ButtonCanvas implements KeyListener
{
  public static final Color BORDER_COLOUR = Color.gray;
  public static final Color DIALOG_COLOUR = Color.lightGray;
  public static final Color ERROR_COLOUR = Color.red;
  public static final Color USER_COLOUR = Color.white;
  public static final char CURSOR_CHAR = '_';
  public static final int BORDER_WIDTH = 10;
  public static final String OKAY = "Okay";
  public static final String CANCEL = "Cancel";
  public static final int ROWS = 7;

  int fontSize, fontHeight, fontDescent, cursorWidth, borderLeft, borderRight;
  Font font;
  String dialogText, errorText, userTextOne, userTextTwo;
  int buttonLeft, buttonRight;
  boolean dialogOn = false;
  boolean passwordMode = false;
  boolean messageMode = false;

  //// Temp stuff ////

  int count = 0;

  public DialogCanvas (int width, int fontSize, Toolkit toolkit) {
	super(fontSize, toolkit, width, 0);
	if (buttonFontHeight % 2 == 1)
	  buttonFontHeight++;
	fontDescent = fm.getDescent();
	fontHeight = buttonFontHeight + fontDescent; 
	height = fontHeight * ROWS;
	this.fontSize = fontSize;
	cursorWidth = fm.charWidth(CURSOR_CHAR);
	borderLeft = BORDER_WIDTH * buttonSpaceWidth;
	borderRight = width - BORDER_WIDTH * buttonSpaceWidth - 1;
	font = buttonFont;
	dialogText = "";
	errorText = "";
	userTextOne = "";
	userTextTwo = "";
	Dimension okayDimension = buttonDimensions (OKAY);
	Dimension cancelDimension = buttonDimensions (CANCEL);
	int buttonsWidth = okayDimension.width * 2 + cancelDimension.width;
	buttonLeft = (width - buttonsWidth) / 2;
	buttonRight = (width + buttonsWidth) / 2 - cancelDimension.width;
	addButton(buttonLeft, height - okayDimension.height - fontDescent, OKAY);
	addButton(buttonRight, height - cancelDimension.height - fontDescent, CANCEL);
	prepareButtons();
  }  
  public void addChar (char c) {
	String text;
	int charWidth = fm.charWidth(c);
	int whichRow = 2;
	if (!userTextTwo.equals(""))
	  text = userTextTwo;
	else {
	  if (fm.stringWidth(userTextOne + c) > (borderRight - borderLeft - 2 * cursorWidth))
	text = userTextTwo;
	  else { 
	text = userTextOne;
	whichRow = 1;
	  }
	}
	if (fm.stringWidth(text + c) > (borderRight - borderLeft - 2 * cursorWidth))
	  return;
	else {
	  if (whichRow == 1) {
	userTextOne += c;
	redrawRow(3);
	  } else {
	userTextTwo += c;
	redrawRow(3);
	redrawRow(4);
	  }
	}
  }  
  public void clearDialog () {
	dialogOn = false;
	passwordMode = false;
	messageMode = false;
	dialogText = "";
	errorText = "";
	userTextOne = "";
	userTextTwo = "";
	repaint();
  }  
  public void clearUserText () {
	userTextOne = "";
	userTextTwo = "";
	redrawRow(3);
	redrawRow(4);
  }  
  protected void drawText (Graphics g, int row, Color color, String text) {
	g.setColor(color);
	g.drawString(text, borderLeft + cursorWidth, (row + 1) * fontHeight - fontDescent);
  }  
  protected void eraseText (Graphics g, int row) {
	g.setColor(Color.black);
	g.fillRect(borderLeft, row * fontHeight + 1, (borderRight - borderLeft + 1), fontHeight + 1);
  }  
  public String getText () {
	if (userTextTwo == null)
	  return userTextOne;
	else
	  return userTextOne + userTextTwo;
  }  
  /*  public void buttonPressed (int buttonNum) {
	super.buttonPressed(buttonNum);
	count++;
	switch (count) {
	case 1:
	  setErrorText("(You made a big error)");
	  clearUserText();
	  break;
	case 2:
	  setErrorText("");
	  setDialogText("Enter your password");
	  clearUserText();
	  break;
	case 3:
	  clearUserText();
	  setDialogText("Enter your alias");
	  count = 0;
	  break;
	}
  }
  */
  public boolean isMessageChar(char key) {
	if (Character.isDigit(key) || Character.isLetter(key))
	  return true;
	for (int i = 0; i < Params.MESSAGE_KEYS.length; i++)
	  if (Params.MESSAGE_KEYS[i] == key)
	return true;
	return false;
  }  

  public void paste () {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Clipboard clip = toolkit.getSystemClipboard();
    Transferable trans = clip.getContents(this);
    if (trans != null) {
      try {
	String data = (String)trans.getTransferData(DataFlavor.stringFlavor);
	if (data != null) {
	  int n = data.length();
	  for (int i = 0; i < n; i++) {
	    char c = data.charAt(i);
	    if (isMessageChar(c))
	      addChar(c);
	  }
	}
      } catch (IOException e) {
      } catch (UnsupportedFlavorException f) {}
    }
  }

  public void keyPressed (KeyEvent e) {
    char c = e.getKeyChar();
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_BACK_SPACE)
      removeChar();
    if ((code == KeyEvent.VK_V) && (e.isControlDown()))
      paste();
    else if (isMessageChar(c))
      addChar(c);
  }  
  public void keyReleased (KeyEvent e) {}  
  public void keyTyped (KeyEvent e) {}  
  public static void main (String[] args) {
	Frame f = new Frame("Know Your Role");
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	DialogCanvas dc = new DialogCanvas(400, 16, toolkit);
	dc.setDialogText("Enter you alias");
	dc.addKeyListener(dc);
	f.add(dc);
	f.pack();
	f.show();
	f.setSize(400, dc.height + 20); 
	f.validate();
  }  
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 * @param num int
 */
public String makeStars(int num) {
	String val = "";
	for (int i = 0; i < num; i++) 
		val += "*";
	return val;
}
public void paint(Graphics g) {
	g.setColor(Color.black);
	g.fillRect(0, 0, width, height);
	if (messageMode) {
		g.setFont(font);
		g.setColor(ERROR_COLOUR);
		int messageWidth = fm.stringWidth(dialogText);
		int x = (width - messageWidth) / 2;
		int y = (ROWS / 2 + 1) * fontHeight;
		g.drawString(dialogText, x, y);
	} else if (dialogOn)
		super.paint(g);
	// Init
	g.setFont(font);
	// Draw Borders
	g.setColor(BORDER_COLOUR);
	g.drawRect(0, 0, width - 1, height - 1);
	int halfHeight = fontHeight / 2;
	for (int i = 0; i < ROWS; i++) {
		g.drawLine(0, fontHeight * i, borderLeft - 1, fontHeight * i);
		g.drawLine(0, fontHeight * i + halfHeight, borderLeft - 1, fontHeight * i + halfHeight);
		g.drawLine(borderRight + 1, fontHeight * i, width, fontHeight * i);
		g.drawLine(borderRight + 1, fontHeight * i + halfHeight, width, fontHeight * i + halfHeight);
	}
	// Draw each line of text
	if (dialogOn) {
		drawText(g, 1, DIALOG_COLOUR, dialogText);
		drawText(g, 2, ERROR_COLOUR, "  " + errorText);
		if (userTextTwo.equals("")) {
			if (passwordMode)
				drawText(g, 3, USER_COLOUR, makeStars(userTextOne.length()) + CURSOR_CHAR);
			else			
				drawText(g, 3, USER_COLOUR, userTextOne + CURSOR_CHAR);
		} else {
			if (passwordMode) {
				drawText(g, 3, USER_COLOUR, makeStars(userTextOne.length()));
				drawText(g, 4, USER_COLOUR, makeStars(userTextTwo.length()) + CURSOR_CHAR);
			} else {
				drawText(g, 3, USER_COLOUR, userTextOne);
				drawText(g, 4, USER_COLOUR, userTextTwo + CURSOR_CHAR);
			}
		}
	}
}
public void redrawRow(int row) {
	Graphics g = getGraphics();
	if (g == null)
		repaint();
	else {
		g.setFont(font);
		eraseText(g, row);
		switch (row) {
			case 1 :
				drawText(g, 1, DIALOG_COLOUR, dialogText);
				break;
			case 2 :
				drawText(g, 2, ERROR_COLOUR, "  " + errorText);
				break;
			case 3 :
				if (userTextTwo.equals("")) {
					if (passwordMode)
						drawText(g, 3, USER_COLOUR, makeStars(userTextOne.length()) + CURSOR_CHAR);
					else				
						drawText(g, 3, USER_COLOUR, userTextOne + CURSOR_CHAR);
				} else {
					if (passwordMode)
						drawText(g, 3, USER_COLOUR, makeStars(userTextOne.length()));
					else
						drawText(g, 3, USER_COLOUR, userTextOne);
				}
				break;
			case 4 :
				if (!userTextTwo.equals("")) {
					if (passwordMode)
						drawText(g, 4, USER_COLOUR, makeStars(userTextTwo.length()) + CURSOR_CHAR);
					else
						drawText(g, 4, USER_COLOUR, userTextTwo + CURSOR_CHAR);
				}
				break;
		}
	}
}
  public void removeChar () {
	if (!userTextTwo.equals("")) {
	  userTextTwo = userTextTwo.substring(0, userTextTwo.length() - 1);
	  redrawRow(4);
	} else if (!userTextOne.equals("")) {
	  userTextOne = userTextOne.substring(0, userTextOne.length() - 1);
	  redrawRow(3);
	}
  }  
  public void setDialogText (String text) {
	dialogOn = true;
	dialogText = text;
	repaint();
  }  
  public void setErrorText (String text) {
	errorText = text;
	repaint();
  }  
  public void setMessageText (String text) {
	messageMode = true;
	dialogText = text;
	repaint();
  }  
}
