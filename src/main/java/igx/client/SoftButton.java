package igx.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class SoftButton
{
  public static final Color BUTTON_FOREGROUND = new Color(0, 192, 0);
  public static final Color BUTTON_HIGHLIGHT = new Color(64, 255, 0);
  public static final Color BUTTON_BORDER = Color.lightGray;
  public static final int BUTTON_MARGIN = 2;
  Rectangle r;
  String text;
  boolean highlight = false;
  int descent;
  
  public SoftButton(Rectangle paramRectangle, String paramString)
  {
    r = paramRectangle;
    text = paramString;
    descent = descent;
  }
  
  public SoftButton(Rectangle paramRectangle, String paramString, int paramInt)
  {
    r = paramRectangle;
    text = paramString;
  }
  
  public boolean contains(Point paramPoint)
  {
    return r.contains(paramPoint);
  }
  
  public String getText()
  {
    return text;
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setColor(BUTTON_BORDER);
    paramGraphics.drawRoundRect(r.x, r.y, r.width, r.height, 2, 2);
    if (highlight) {
      paramGraphics.setColor(BUTTON_HIGHLIGHT);
    } else {
      paramGraphics.setColor(BUTTON_FOREGROUND);
    }
    paramGraphics.drawString(text, r.x + 2, r.y + r.height - 4 - 1 - descent);
  }
  
  public void setHighlight(boolean paramBoolean)
  {
    highlight = paramBoolean;
  }
}