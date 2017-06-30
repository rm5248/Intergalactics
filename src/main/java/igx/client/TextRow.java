package igx.client;

import java.awt.Color;
import java.util.Vector;

public class TextRow
{
  Vector elements;
  boolean underlined = false;
  Color underlineColour;
  boolean selected = false;
  boolean centered = false;
  int numberOfElements;
  
  public TextRow(Vector paramVector)
  {
    elements = paramVector;
    numberOfElements = paramVector.size();
  }
  
  public void center()
  {
    centered = true;
  }
  
  public void setSelect(boolean paramBoolean)
  {
    selected = paramBoolean;
  }
  
  public void underline(Color paramColor)
  {
    underlineColour = paramColor;
    underlined = true;
  }
}