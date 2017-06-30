package igx.client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

public class ButtonCanvas
  extends Canvas
  implements MouseListener
{
  protected Vector buttons = new Vector();
  protected SoftButton[] button;
  protected int numButtons;
  protected Font buttonFont;
  protected FontMetrics fm;
  public int buttonFontHeight;
  public int buttonFontDescent;
  public int buttonSpaceWidth;
  public int width;
  public int height;
  public int buttonHeight;
  ButtonListener listener = null;
  int currentButton = -1;
  
  public ButtonCanvas(int paramInt1, Toolkit paramToolkit, int paramInt2, int paramInt3)
  {
    width = paramInt2;
    height = paramInt3;
    setBackground(Color.gray);
    buttonFont = new Font("SansSerif", 0, paramInt1);
    fm = paramToolkit.getFontMetrics(buttonFont);
    buttonFontDescent = fm.getDescent();
    buttonFontHeight = (fm.getAscent() + buttonFontDescent + 1);
    buttonSpaceWidth = fm.charWidth(' ');
    addMouseListener(this);
    buttonHeight = (buttonFontHeight + 4);
  }
  
  public void addButton(int paramInt1, int paramInt2, String paramString)
  {
    Dimension localDimension = buttonDimensions(paramString);
    Rectangle localRectangle = new Rectangle(paramInt1, paramInt2, width, height);
    buttons.addElement(new SoftButton(localRectangle, paramString, buttonFontDescent));
  }
  
  public Dimension buttonDimensions(String paramString)
  {
    return new Dimension(4 + fm.stringWidth(paramString), buttonFontHeight + 4);
  }
  
  public void buttonPressed(int paramInt)
  {
    if (listener != null) {
      listener.buttonPressed(button[paramInt].getText());
    }
  }
  
  public int getButton(Point paramPoint)
  {
    for (int i = 0; i < numButtons; i++) {
      if (button[i].contains(paramPoint)) {
        return i;
      }
    }
    return -1;
  }
  
  public static void main(String[] paramArrayOfString)
  {
    Frame localFrame = new Frame("Beej's Buttons");
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    ButtonCanvas localButtonCanvas = new ButtonCanvas(14, localToolkit, 400, 400);
    localButtonCanvas.addButton(10, 10, "(S)tart Game");
    localButtonCanvas.addButton(50, 50, "Add (C)omputer Player");
    localButtonCanvas.addButton(100, 100, "Sound On/Off (F2)");
    localButtonCanvas.addButton(200, 200, "(Q)uit");
    localButtonCanvas.prepareButtons();
    localFrame.add(localButtonCanvas);
    localFrame.pack();
    localFrame.show();
    localFrame.setSize(400, 400);
    localFrame.validate();
  }
  
  public void mouseClicked(MouseEvent paramMouseEvent) {}
  
  public void mouseEntered(MouseEvent paramMouseEvent) {}
  
  public void mouseExited(MouseEvent paramMouseEvent)
  {
    if ((currentButton != -1) && (button[currentButton] != null))
    {
      button[currentButton].setHighlight(false);
      redrawButton(currentButton);
    }
    currentButton = -1;
  }
  
  public void mousePressed(MouseEvent paramMouseEvent)
  {
    int i = getButton(paramMouseEvent.getPoint());
    if (i != -1)
    {
      currentButton = i;
      button[i].setHighlight(true);
      redrawButton(currentButton);
    }
  }
  
  public void mouseReleased(MouseEvent paramMouseEvent)
  {
    if (currentButton != -1)
    {
      if (button[currentButton].contains(paramMouseEvent.getPoint())) {
        buttonPressed(currentButton);
      }
      button[currentButton].setHighlight(false);
      redrawButton(currentButton);
      currentButton = -1;
    }
  }
  
  public void paint(Graphics paramGraphics)
  {
    super.paint(paramGraphics);
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(0, 0, width, height);
    paramGraphics.setFont(buttonFont);
    for (int i = 0; i < numButtons; i++) {
      button[i].paint(paramGraphics);
    }
  }
  
  public void prepareButtons()
  {
    numButtons = buttons.size();
    button = new SoftButton[numButtons];
    for (int i = 0; i < numButtons; i++) {
      button[i] = ((SoftButton)buttons.elementAt(i));
    }
    buttons = new Vector();
  }
  
  public void redrawButton(int paramInt)
  {
    Graphics localGraphics = getGraphics();
    localGraphics.setFont(buttonFont);
    if (localGraphics == null)
    {
      repaint();
      return;
    }
    button[paramInt].paint(localGraphics);
  }
  
  public void setButtonListener(ButtonListener paramButtonListener)
  {
    listener = paramButtonListener;
  }
}