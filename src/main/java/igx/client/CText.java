package igx.client;

import java.awt.Color;

public class CText
{
  public String text;
  public Color color;
  public int width;
  
  public CText(String paramString, Color paramColor)
  {
    text = paramString;
    color = paramColor;
  }
  
  public void setWidth(int paramInt)
  {
    width = paramInt;
  }
}