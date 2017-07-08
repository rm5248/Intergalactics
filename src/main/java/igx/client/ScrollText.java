package igx.client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScrollText
  extends Canvas
{
  public static final int MARGIN = 1;
  int fontSize;
  int fontHeight;
  Font font;
  FontMetrics fm;
  private List<List<CText>> lines;
  int numLines;
  int width;
  int currentLine;
  int lineWidth;
  int spaceWidth;
  int numElements;
  int lineID;
  int height;
  int topMargin;
  
  private static final Logger logger = LogManager.getLogger();
  
  public ScrollText(int paramInt1, Toolkit paramToolkit, int paramInt2, int paramInt3)
  {
      logger.debug( "Scroll text size {}", paramInt1 );
    width = (paramInt2 - 1);
    height = paramInt3;
    font = new Font("SansSerif", 0, paramInt1);
    fm = paramToolkit.getFontMetrics(font);
    fontHeight = (fm.getAscent() + 1);
    spaceWidth = fm.charWidth(' ');
    numLines = ((paramInt3 - 1) / fontHeight);
    logger.debug( "Number of lines is {}", numLines );
    lines = new Vector(numLines + 1);
    lines.add( new ArrayList<CText>() );
    currentLine = (this.lineWidth = this.lineID = 0);
      setBackground( Color.ORANGE );
  }
  
  public void addText(CText paramCText)
  {
      logger.debug( "Adding text {}", paramCText.text );
    if (paramCText.text == null)
    {
      lineWidth = 0;
      currentLine += 1;
      lines.add(new ArrayList<CText>());
    }
    else
    {
      String[] wordsList = paramCText.text.split( " " );
      CText localCText = new CText("", paramCText.color);
      for (int j = 0; j < wordsList.length; j++)
      {
        String str = wordsList[ j ];
        int i = fm.stringWidth(str) + spaceWidth;
        if (i + lineWidth < width)
        {
          paramCText.text += str;
          lineWidth += i;
        }
        else
        {
            List<CText> currentLineList = lines.get( currentLine );
//            Vector localVector1 = new Vector();
//          localVector1 = (Vector)lines.elementAt(currentLine);
          localCText.setWidth(fm.stringWidth(paramCText.text));
          currentLineList.add(localCText);
          currentLine += 1;
          lineWidth = (i + spaceWidth);
          //localVector1 = new Vector();
          //lines.addElement(localVector1);
          localCText = new CText(" " + str, paramCText.color);
        }
      }
      List<CText> current = lines.get( currentLine );
      localCText.setWidth(fm.stringWidth(paramCText.text));
      current.add( localCText );
    }
    
    while (currentLine >= numLines)
    {
      lines.remove( 0 );
      currentLine -= 1;
    }
    
    repaint();
  }
  
  public Dimension getMinimumSize()
  {
    return new Dimension(width, height);
  }
  
  public Dimension getPreferredSize()
  {
    return new Dimension(width, height);
  }
  
//  private String[] getWords(String paramString)
//  {
////    Vector localVector = new Vector();
////    int j;
////    int i;
////    for (i = 0; (j = paramString.indexOf(' ', i)) != -1; i = j + 1) {
////      localVector.addElement(paramString.substring(i, j + 1));
////    }
////    localVector.addElement(paramString.substring(i, paramString.length()));
////    return localVector;
//  }
  
  public void newLine()
  {
    addText(new CText(null, null));
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setFont(font);
    int yLocation = fontHeight + 1;
    for (int m = 0; m < currentLine; m++)
    {
      List<CText> currentText = lines.get( m );
      int xLocation = 1;
      for (CText text : currentText )
      {
        paramGraphics.setColor(text.color);
        paramGraphics.drawString(text.text, xLocation, yLocation);
        xLocation += width;
      }
      
      yLocation += fontHeight;
    }
  }
}