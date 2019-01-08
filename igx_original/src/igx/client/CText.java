package igx.client;

// CText.java
// A class that holds a string and an associated colour

import java.awt.*;

public class CText
{
  public String text;
  public Color color;
  public int width;

  public CText (String text, Color color)
  {
	this.text = text;
	this.color = color;
  }  
  public void setWidth (int width)
  {
	this.width = width;
  }  
}