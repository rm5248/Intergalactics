package igx.client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.Vector;

public class ListCanvas
  extends Canvas
{
  public static final int MARGIN_FACTOR = 1;
  int width;
  int height;
  int fontHeight;
  int fontDescent;
  int rows;
  int maxWidth;
  Vector strings = new Vector();
  Font font;
  FontMetrics fm;
  
  public ListCanvas(int paramInt1, int paramInt2, int paramInt3, Toolkit paramToolkit)
  {
    width = paramInt1;
    height = paramInt2;
    font = new Font("Helvetica", 0, paramInt3);
    fm = paramToolkit.getFontMetrics(font);
    fontDescent = fm.getDescent();
    fontHeight = (fm.getAscent() + 1 + fontDescent);
    rows = (paramInt2 / fontHeight);
    setBackground(Color.black);
  }
  
  public void addText(CText paramCText)
  {
    strings.addElement(paramCText);
    paramCText.setWidth(fm.stringWidth(paramCText.text));
    if (width > maxWidth) {
      maxWidth = width;
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
    Frame localFrame = new Frame("Toothless Joe Ward");
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
    localFrame.pack();
    localFrame.show();
    localFrame.setSize(400, localListCanvas.height + 20);
    localFrame.validate();
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setFont(font);
    int i = strings.size();
    for (int j = 0; j < i; j++)
    {
      int k = j / rows;
      int m = (maxWidth + fontHeight * 1) * k + fontHeight * 1;
      int n = (j % rows + 1) * fontHeight - fontDescent;
      CText localCText = (CText)strings.elementAt(j);
      paramGraphics.setColor(localCText.color);
      paramGraphics.drawString(localCText.text, m, n);
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
      } else if (width > maxWidth) {
        maxWidth = width;
      }
    }
    repaint();
  }
}