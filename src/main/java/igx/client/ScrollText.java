package igx.client;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Vector;

public class ScrollText
  extends Canvas
{
  public static final int MARGIN = 1;
  int fontSize;
  int fontHeight;
  Font font;
  FontMetrics fm;
  Vector lines;
  int numLines;
  int width;
  int currentLine;
  int lineWidth;
  int spaceWidth;
  int numElements;
  int lineID;
  int height;
  int topMargin;
  
  public ScrollText(int paramInt1, Toolkit paramToolkit, int paramInt2, int paramInt3)
  {
    width = (paramInt2 - 1);
    height = paramInt3;
    font = new Font("SansSerif", 0, paramInt1);
    fm = paramToolkit.getFontMetrics(font);
    fontHeight = (fm.getAscent() + 1);
    spaceWidth = fm.charWidth(' ');
    numLines = ((paramInt3 - 1) / fontHeight);
    lines = new Vector(numLines + 1);
    lines.addElement(new Vector());
    currentLine = (this.lineWidth = this.lineID = 0);
  }
  
  public void addText(CText paramCText)
  {
    if (paramCText.text == null)
    {
      lineWidth = 0;
      currentLine += 1;
      lines.addElement(new Vector());
    }
    else
    {
      Vector localVector2 = getWords(paramCText.text);
      CText localCText = new CText("", paramCText.color);
      for (int j = 0; j < localVector2.size(); j++)
      {
        String str = (String)localVector2.elementAt(j);
        int i = fm.stringWidth(str) + spaceWidth;
        if (i + lineWidth < width)
        {
          paramCText.text += str;
          lineWidth += i;
        }
        else
        {
            Vector localVector1 = new Vector();
          localVector1 = (Vector)lines.elementAt(currentLine);
          localCText.setWidth(fm.stringWidth(paramCText.text));
          localVector1.addElement(localCText);
          currentLine += 1;
          lineWidth = (i + spaceWidth);
          localVector1 = new Vector();
          lines.addElement(localVector1);
          localCText = new CText(" " + str, paramCText.color);
        }
      }
      Vector localVector1 = (Vector)lines.elementAt(currentLine);
      localCText.setWidth(fm.stringWidth(paramCText.text));
      localVector1.addElement(localCText);
    }
    while (currentLine >= numLines)
    {
      lines.removeElementAt(0);
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
  
  public Vector getWords(String paramString)
  {
    Vector localVector = new Vector();
    int j;
    int i;
    for (i = 0; (j = paramString.indexOf(' ', i)) != -1; i = j + 1) {
      localVector.addElement(paramString.substring(i, j + 1));
    }
    localVector.addElement(paramString.substring(i, paramString.length()));
    return localVector;
  }
  
  public void newLine()
  {
    addText(new CText(null, null));
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setFont(font);
    int j = fontHeight + 1;
    for (int m = 0; m < currentLine; m++)
    {
      Vector localVector = (Vector)lines.elementAt(m);
      int i = 1;
      int k = localVector.size();
      for (int n = 0; n < k; n++)
      {
        CText localCText = (CText)localVector.elementAt(n);
        paramGraphics.setColor(localCText.color);
        paramGraphics.drawString(localCText.text, i, j);
        i += width;
      }
      j += fontHeight;
    }
  }
}