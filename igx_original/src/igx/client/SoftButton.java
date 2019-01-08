package igx.client;

// SoftButton.java 

import java.awt.*;

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
public SoftButton(Rectangle r, String text) {
	this.r = r;
	this.text = text;
	this.descent = descent;
}
  public SoftButton (Rectangle r, String text, int descent) {
	this.r = r;
	this.text = text;
  }  
  public boolean contains(Point p) {
	return r.contains(p);
  }  
  public String getText () {
	return text;
  }  
  public void paint (Graphics g) {
	g.setColor(BUTTON_BORDER);
	g.drawRoundRect(r.x, r.y, r.width, r.height, BUTTON_MARGIN, BUTTON_MARGIN);
	if (highlight)
	  g.setColor(BUTTON_HIGHLIGHT);
	else
	  g.setColor(BUTTON_FOREGROUND);
	g.drawString(text, r.x + BUTTON_MARGIN, r.y + r.height - 2*BUTTON_MARGIN - 1 - descent);
  }  
  public void setHighlight (boolean h) {
	highlight = h;
  }  
}