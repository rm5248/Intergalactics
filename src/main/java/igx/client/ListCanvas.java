package igx.client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ListCanvas
  extends JPanel
{
  public static final int MARGIN_FACTOR = 1;
  int fontHeight;
  int fontDescent;
  int rows;
  int maxWidth;
  Vector strings = new Vector();
  Font font;
  FontMetrics fm;
  
  private static final Logger logger = LogManager.getLogger();
  
  public ListCanvas(int paramWidth, int paramHeight, int fontSize, Toolkit paramToolkit)
  {
      setSize( paramWidth, paramHeight );
    font = new Font("Helvetica", 0, fontSize);
    fm = paramToolkit.getFontMetrics(font);
    fontDescent = fm.getDescent();
    fontHeight = (fm.getAscent() + 1 + fontDescent);
    rows = (paramHeight / fontHeight);
    setBackground(Color.black);
      setOpaque(true);
  }
  
  public void addText(CText paramCText)
  {
    strings.addElement(paramCText);
    paramCText.setWidth(fm.stringWidth(paramCText.text));
    if (getSize().width > maxWidth) {
      maxWidth = getSize().width;
    }
    repaint();
  }
  
  public void addText(String paramString, Color paramColor)
  {
    addText(new CText(paramString, paramColor));
  }
  
  public void changeColour(String paramString, Color paramColor)
  {
    changeMultiColour(paramString, paramColor);
    repaint();
  }
  
  public void changeMultiColour(String paramString, Color paramColor)
  {
    for (int i = 0; i < strings.size(); i++)
    {
      CText localCText = (CText)strings.elementAt(i);
      if (localCText.text.equals(paramString)) {
        localCText.color = paramColor;
      }
    }
  }
  
  public static void main(String[] paramArrayOfString)
  {
    JFrame localFrame = new JFrame("Toothless Joe Ward");
    localFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    ListCanvas localListCanvas = new ListCanvas(400, 400, 20, localToolkit);
    localListCanvas.addText("A", Color.blue);
    localListCanvas.addText("B", Color.blue);
    localListCanvas.addText("C", Color.blue);
    localListCanvas.addText("D", Color.blue);
    localListCanvas.addText("E", Color.blue);
    localListCanvas.addText("Sweet", Color.red);
    localListCanvas.addText("F", Color.blue);
    localListCanvas.addText("G", Color.blue);
    localListCanvas.addText("H", Color.blue);
    localListCanvas.addText("I", Color.blue);
    localListCanvas.addText("J", Color.blue);
    localListCanvas.addText("K", Color.blue);
    localListCanvas.addText("L", Color.blue);
    localListCanvas.addText("M", Color.blue);
    localListCanvas.addText("N", Color.blue);
    localListCanvas.addText("Game", Color.red);
    localListCanvas.addText("O", Color.blue);
    localListCanvas.addText("P", Color.blue);
    localListCanvas.addText("Q", Color.blue);
    localListCanvas.addText("R", Color.blue);
    localFrame.add(localListCanvas);
    //localFrame.pack();
    localFrame.setSize(400, localListCanvas.getSize().height + 20);
    localFrame.validate();
    localFrame.setVisible( true );
  }
  
  public void paint(Graphics paramGraphics)
  {
      super.paintComponents(paramGraphics);
    paramGraphics.setFont(font);
    logger.debug( "font height is {} descent is {}", fontHeight, fontDescent );
    for (int x = 0; x < strings.size(); x++)
    {
      int rowNumber = x / rows;
      int xLocation = (maxWidth + fontHeight * 1) * rowNumber + fontHeight * 1;
      int yLocation = (x % rows + 1) * fontHeight - fontDescent;
      CText localCText = (CText)strings.elementAt(x);
      paramGraphics.setColor(localCText.color);
      paramGraphics.drawString(localCText.text, xLocation, yLocation);
    }
  }
  
  public void removeText(String paramString)
  {
    maxWidth = 0;
    for (int i = 0; i < strings.size(); i++)
    {
      CText localCText = (CText)strings.elementAt(i);
      if (localCText.text.equals(paramString)) {
        strings.removeElementAt(i);
      } else if (getSize().width > maxWidth) {
        maxWidth = getSize().width;
      }
    }
    repaint();
  }
}