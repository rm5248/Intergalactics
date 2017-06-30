package igx.client;

import java.awt.Color;
import java.awt.FontMetrics;

public class TextElement
{
  public static final int SEQUENTIAL = -1;
  public String text;
  public Color colour;
  public int column;
  
  public TextElement(String paramString, Color paramColor, int paramInt)
  {
    text = paramString;
    colour = paramColor;
    column = paramInt;
  }
  
  public int getWidth(FontMetrics paramFontMetrics)
  {
    return paramFontMetrics.stringWidth(text);
  }
}