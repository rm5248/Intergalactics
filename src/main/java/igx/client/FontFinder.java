package igx.client;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;

public class FontFinder
{
  public static final int INIT_SIZE = 30;
  
  public FontFinder() {}
  
  public static Font getFont(Toolkit paramToolkit, String paramString, int paramInt1, int paramInt2)
  {
    int i = 31;
    Font localFont;
    int j;
    do
    {
      i--;
      localFont = new Font(paramString, 0, i);
      FontMetrics localFontMetrics = paramToolkit.getFontMetrics(localFont);
      j = (localFontMetrics.getAscent() + localFontMetrics.getDescent()) * paramInt1;
    } while ((j >= paramInt2) || (i == 0));
    return localFont;
  }
}