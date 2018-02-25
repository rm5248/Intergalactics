package igx.client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForumCanvas
  extends JPanel
  implements MouseListener, RowListener
{
  public static final Color SELECT_COLOUR = Color.red;
  private RowListener listener = null;
  private int fontSize;
  private int rows;
  private Font font;
  protected FontMetrics fm;
  protected int fontHeight;
  int fontDescent;
  int spaceWidth;
  public TextRow[] row;
  int selectedRow = -1;
  
  private static final Logger logger = LogManager.getLogger();
  
  public ForumCanvas(int paramWidth, int paramHeight, int paramFontSize, Toolkit paramToolkit)
  {
      setPreferredSize( new Dimension( paramWidth, paramHeight ) );
    fontSize = paramFontSize;
    font = new Font("Helvetica", 0, paramFontSize);
    fm = paramToolkit.getFontMetrics(font);
    fontDescent = fm.getDescent();
    fontHeight = (fm.getAscent() + fontDescent);
    if (fontHeight % 2 == 0) {
      fontHeight += 1;
    }
    spaceWidth = fm.charWidth(' ');
    rows = (paramHeight / fontHeight);
    row = new TextRow[rows];
    setBackground(Color.black);
    addMouseListener(this);
  }
  
  public static int computeHeight(int paramInt1, int paramInt2, Toolkit paramToolkit)
  {
    Font localFont = new Font("Helvetica", 0, paramInt1);
    FontMetrics localFontMetrics = paramToolkit.getFontMetrics(localFont);
    int i = localFontMetrics.getAscent() + localFontMetrics.getDescent();
    return paramInt2 * i;
  }
  
  public static void main(String[] paramArrayOfString)
  {
    JFrame localFrame = new JFrame("Rooty Poo");
    localFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    ForumCanvas localForumCanvas = new ForumCanvas(400, 400, 14, localToolkit);
    int i = localForumCanvas.rows;
    Vector localVector = new Vector();
    TextElement localTextElement = new TextElement("The ", Color.gray, -1);
    localVector.addElement(localTextElement);
    localTextElement = new TextElement("" + (i - 1), Color.red, -1);
    localVector.addElement(localTextElement);
    localTextElement = new TextElement(" rows.", Color.gray, -1);
    localVector.addElement(localTextElement);
    TextRow localTextRow = new TextRow(localVector);
    localTextRow.center();
    localTextRow.underline(Color.blue);
    localForumCanvas.row[0] = localTextRow;
    for (int j = 1; j < i; j++)
    {
      localVector = new Vector();
      localTextElement = new TextElement("Row ", Color.white, 20);
      localVector.addElement(localTextElement);
      localTextElement = new TextElement("" + j, Color.green, 30);
      localVector.addElement(localTextElement);
      localTextRow = new TextRow(localVector);
      if (j == 10) {
        localTextRow.setSelect(true);
      }
      localForumCanvas.row[j] = localTextRow;
    }
    localForumCanvas.selectedRow = 10;
    localForumCanvas.setRowListener(localForumCanvas);
    localFrame.add(localForumCanvas);
    localFrame.pack();
    localFrame.setSize(400, 400);
    localFrame.validate();
    localFrame.setVisible( true );
  }
  
  public void mouseClicked(MouseEvent paramMouseEvent) {}
  
  public void mouseEntered(MouseEvent paramMouseEvent) {}
  
  public void mouseExited(MouseEvent paramMouseEvent) {}
  
  public void mousePressed(MouseEvent paramMouseEvent)
  {
    if (listener != null)
    {
      Point localPoint = paramMouseEvent.getPoint();
      int i = localPoint.y / fontHeight;
      if (i < rows) {
        listener.rowSelected(i);
      }
    }
  }
  
  public void mouseReleased(MouseEvent paramMouseEvent) {}
  
  public void paint(Graphics paramGraphics)
  {
      super.paint( paramGraphics );
    paramGraphics.setColor(Color.black);
    Dimension size = getSize();
    paramGraphics.fillRect(0, 0, size.width, size.height );
    for (int i = 0; i < rows; i++) {
      if (row[i] != null) {
        paintRow(paramGraphics, i);
      }
    }
    logger.debug( "size is {}", size );
  }
  
  protected void paintRow(Graphics paramGraphics, int paramInt)
  {
    paramGraphics.setFont(font);
    TextRow localTextRow = row[paramInt];
    int i = 1 + fontHeight;
    int j = fontHeight * (paramInt + 1);
    TextElement localTextElement = null;
    int width = this.getSize().width;
    
    if (localTextRow.centered)
    {
      int textWidth = 0;
      for (int elementNumber = 0; elementNumber < localTextRow.numberOfElements; elementNumber++)
      {
        localTextElement = (TextElement)localTextRow.elements.elementAt(elementNumber);
        textWidth += localTextElement.getWidth(fm);
      }
      i = (width - textWidth) / 2;
    }
    
    for (int elementNumber = 0; elementNumber < localTextRow.numberOfElements; elementNumber++)
    {
      localTextElement = (TextElement)localTextRow.elements.elementAt(elementNumber);
      int column = localTextElement.column;
      if( column != -1 ){
          i = column;
      }
      paramGraphics.setColor(localTextElement.colour);
      paramGraphics.drawString(localTextElement.text, i, j - 1 - fontDescent);
      i += localTextElement.getWidth(fm);
    }
    
    if (localTextRow.underlined)
    {
      paramGraphics.setColor(localTextRow.underlineColour);
      paramGraphics.drawLine(0, j, width, j);
    }
    
    if (localTextRow.selected)
    {
      paramGraphics.setColor(SELECT_COLOUR);
      int[] arrayOfInt1 = { 1, fontHeight / 2, 1 };
      int[] arrayOfInt2 = { j - fontHeight + 2, j - fontHeight + 1 + fontHeight / 2, j - 1 };
      paramGraphics.drawPolygon(arrayOfInt1, arrayOfInt2, 3);
      paramGraphics.fillPolygon(arrayOfInt1, arrayOfInt2, 3);
      int[] arrayOfInt3 = { width - 2, width - 1 - fontHeight / 2, width - 2 };
      paramGraphics.drawPolygon(arrayOfInt3, arrayOfInt2, 3);
      paramGraphics.fillPolygon(arrayOfInt3, arrayOfInt2, 3);
    }
  }
  
  public void redrawRow(int paramInt)
  {
      repaint();
  }
  
  public void rowSelected(int paramInt)
  {
    selectRow(paramInt);
  }
  
  public void selectRow(int paramInt)
  {
    if ((selectedRow != -1) && (row[selectedRow] != null))
    {
      row[selectedRow].setSelect(false);
      redrawRow(selectedRow);
    }
    selectedRow = paramInt;
    if ((selectedRow != -1) && (row[selectedRow] != null))
    {
      row[selectedRow].setSelect(true);
      redrawRow(selectedRow);
    }
  }
  
  public void setRowListener(RowListener paramRowListener)
  {
    listener = paramRowListener;
  }
}