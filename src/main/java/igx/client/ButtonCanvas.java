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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ButtonCanvas
  extends JPanel
  implements MouseListener
{
  protected List<SoftButton> buttons = new ArrayList<SoftButton>();
  protected int numButtons;
  protected Font buttonFont;
  protected FontMetrics fm;
  public int buttonFontHeight;
  public int buttonFontDescent;
  public int buttonSpaceWidth;
  public int buttonHeight;
  ButtonListener listener = null;
  private SoftButton currentButton;
  
  private static final Logger logger = LogManager.getLogger();
  
  public ButtonCanvas(int fontSize, Toolkit paramToolkit, int wid, int heigt)
  {
      setSize( wid, heigt );
    setBackground(Color.gray);
    buttonFont = new Font("SansSerif", 0, fontSize);
    fm = paramToolkit.getFontMetrics(buttonFont);
    buttonFontDescent = fm.getDescent();
    buttonFontHeight = (fm.getAscent() + buttonFontDescent + 1);
    buttonSpaceWidth = fm.charWidth(' ');
    addMouseListener(this);
    buttonHeight = (buttonFontHeight + 4);
  }
  
  public void addButton(int xLocation, int yLocation, String buttonText)
  {
    Dimension localDimension = buttonDimensions(buttonText);
    logger.debug("adding button {} at {},{}", buttonText, xLocation, yLocation );
    Rectangle localRectangle = new Rectangle(xLocation, yLocation, 
            localDimension.width, 
            localDimension.height);
    buttons.add(new SoftButton(localRectangle, buttonText, buttonFontDescent));
  }
  
  public Dimension buttonDimensions(String paramString)
  {
    return new Dimension(4 + fm.stringWidth(paramString), buttonFontHeight + 4);
  }
  
  private void currentButtonPressed()
  {
    if (listener != null) {
      listener.buttonPressed(currentButton.getText());
    }
  }
  
  /**
   * Get the button that is contained in the point, 
   * or null if not found.
   * 
   * @param paramPoint
   * @return 
   */
  private SoftButton getButton(Point paramPoint)
  { 
    for( SoftButton sf : buttons ){
        if( sf.contains( paramPoint ) ){
            return sf;
        }
    }
    
    return null;
  }
  
  public static void main(String[] paramArrayOfString)
  {
    JFrame localFrame = new JFrame("Beej's Buttons");
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    ButtonCanvas localButtonCanvas = new ButtonCanvas(14, localToolkit, 400, 400);
    localButtonCanvas.addButton(10, 10, "(S)tart Game");
    localButtonCanvas.addButton(50, 50, "Add (C)omputer Player");
    localButtonCanvas.addButton(100, 100, "Sound On/Off (F2)");
    localButtonCanvas.addButton(200, 200, "(Q)uit");
    localFrame.add(localButtonCanvas);
    //localFrame.pack();
    //localFrame.show();
    localFrame.setVisible( true );
    localFrame.setSize(400, 400);
    localFrame.validate();
  }
  
  public void mouseClicked(MouseEvent paramMouseEvent) {}
  
  public void mouseEntered(MouseEvent paramMouseEvent) {}
  
  public void mouseExited(MouseEvent paramMouseEvent)
  {
    if ((currentButton != null))
    {
        currentButton.setHighlight( false );
      redrawButton(currentButton);
    }
    currentButton = null;
  }
  
  public void mousePressed(MouseEvent paramMouseEvent)
  {
    SoftButton sf = getButton( paramMouseEvent.getPoint() );
    if( sf != null )
    {
        sf.setHighlight( true );
        redrawButton( sf );
      currentButton = sf;
      redrawButton(currentButton);
    }
  }
  
  public void mouseReleased(MouseEvent paramMouseEvent)
  {
    if (currentButton != null)
    {
        if( currentButton.contains( paramMouseEvent.getPoint() ) ){
            
        currentButtonPressed();
      }
        currentButton.setHighlight( false );
      redrawButton(currentButton);
      currentButton = null;
    }
  }
  
  public void paint(Graphics paramGraphics)
  {
    super.paint(paramGraphics);
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(0, 0, getSize().width, getSize().height);
    paramGraphics.setFont(buttonFont);
    for( SoftButton sf : buttons ){
        sf.paint( paramGraphics );
    }
  }
  
  private void redrawButton(SoftButton sf)
  {
    Graphics localGraphics = getGraphics();
    localGraphics.setFont(buttonFont);
    if (localGraphics == null)
    {
      repaint();
      return;
    }
    sf.paint(localGraphics);
  }
  
  public void setButtonListener(ButtonListener paramButtonListener)
  {
    listener = paramButtonListener;
  }
}