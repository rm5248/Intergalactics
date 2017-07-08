package igx.client;

import igx.shared.Params;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.swing.JFrame;

public class DialogCanvas
  extends ButtonCanvas
  implements KeyListener
{
  public static final Color BORDER_COLOUR = Color.gray;
  public static final Color DIALOG_COLOUR = Color.lightGray;
  public static final Color ERROR_COLOUR = Color.red;
  public static final Color USER_COLOUR = Color.white;
  public static final char CURSOR_CHAR = '_';
  public static final int BORDER_WIDTH = 10;
  public static final String OKAY = "Okay";
  public static final String CANCEL = "Cancel";
  public static final int ROWS = 7;
  int fontSize;
  int fontHeight;
  int fontDescent;
  int cursorWidth;
  int borderLeft;
  int borderRight;
  Font font;
  String dialogText;
  String errorText;
  String userTextOne;
  String userTextTwo;
  int buttonLeft;
  int buttonRight;
  boolean dialogOn = false;
  boolean passwordMode = false;
  boolean messageMode = false;
  int count = 0;
  
  public DialogCanvas(int paramInt1, int paramInt2, Toolkit paramToolkit)
  {
    super(paramInt2, paramToolkit, paramInt1, 0);
    if (buttonFontHeight % 2 == 1) {
      buttonFontHeight += 1;
    }
    fontDescent = fm.getDescent();
    fontHeight = (buttonFontHeight + fontDescent);
    height = (fontHeight * 7);
    fontSize = paramInt2;
    cursorWidth = fm.charWidth('_');
    borderLeft = (10 * buttonSpaceWidth);
    borderRight = (paramInt1 - 10 * buttonSpaceWidth - 1);
    font = buttonFont;
    dialogText = "";
    errorText = "";
    userTextOne = "";
    userTextTwo = "";
    Dimension localDimension1 = buttonDimensions("Okay");
    Dimension localDimension2 = buttonDimensions("Cancel");
    int i = width * 2 + width;
    buttonLeft = ((paramInt1 - i) / 2);
    buttonRight = ((paramInt1 + i) / 2 - width);
    addButton(buttonLeft, height - height - fontDescent, "Okay");
    addButton(buttonRight, height - height - fontDescent, "Cancel");
  }
  
  public void addChar(char paramChar)
  {
    int i = fm.charWidth(paramChar);
    int j = 2;
    String str;
    if (!userTextTwo.equals(""))
    {
      str = userTextTwo;
    }
    else if (fm.stringWidth(userTextOne + paramChar) > borderRight - borderLeft - 2 * cursorWidth)
    {
      str = userTextTwo;
    }
    else
    {
      str = userTextOne;
      j = 1;
    }
    if (fm.stringWidth(str + paramChar) > borderRight - borderLeft - 2 * cursorWidth) {
      return;
    }
    if (j == 1)
    {
      userTextOne += paramChar;
      redrawRow(3);
    }
    else
    {
      userTextTwo += paramChar;
      redrawRow(3);
      redrawRow(4);
    }
  }
  
  public void clearDialog()
  {
    dialogOn = false;
    passwordMode = false;
    messageMode = false;
    dialogText = "";
    errorText = "";
    userTextOne = "";
    userTextTwo = "";
    repaint();
  }
  
  public void clearUserText()
  {
    userTextOne = "";
    userTextTwo = "";
    redrawRow(3);
    redrawRow(4);
  }
  
  protected void drawText(Graphics paramGraphics, int paramInt, Color paramColor, String paramString)
  {
    paramGraphics.setColor(paramColor);
    paramGraphics.drawString(paramString, borderLeft + cursorWidth, (paramInt + 1) * fontHeight - fontDescent);
  }
  
  protected void eraseText(Graphics paramGraphics, int paramInt)
  {
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(borderLeft, paramInt * fontHeight + 1, borderRight - borderLeft + 1, fontHeight + 1);
  }
  
  public String getText()
  {
    if (userTextTwo == null) {
      return userTextOne;
    }
    return userTextOne + userTextTwo;
  }
  
  public boolean isMessageChar(char paramChar)
  {
    if ((Character.isDigit(paramChar)) || (Character.isLetter(paramChar))) {
      return true;
    }
    for (int i = 0; i < Params.MESSAGE_KEYS.length; i++) {
      if (Params.MESSAGE_KEYS[i] == paramChar) {
        return true;
      }
    }
    return false;
  }
  
  public void paste()
  {
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    Clipboard localClipboard = localToolkit.getSystemClipboard();
    Transferable localTransferable = localClipboard.getContents(this);
    if (localTransferable != null) {
      try
      {
        String str = (String)localTransferable.getTransferData(DataFlavor.stringFlavor);
        if (str != null)
        {
          int i = str.length();
          for (int j = 0; j < i; j++)
          {
            char c = str.charAt(j);
            if (isMessageChar(c)) {
              addChar(c);
            }
          }
        }
      }
      catch (IOException localIOException) {}
      catch (UnsupportedFlavorException localUnsupportedFlavorException) {}
    }
  }
  
  public void keyPressed(KeyEvent paramKeyEvent)
  {
    char c = paramKeyEvent.getKeyChar();
    int i = paramKeyEvent.getKeyCode();
    if (i == 8) {
      removeChar();
    }
    if ((i == 86) && (paramKeyEvent.isControlDown())) {
      paste();
    } else if (isMessageChar(c)) {
      addChar(c);
    }
  }
  
  public void keyReleased(KeyEvent paramKeyEvent) {}
  
  public void keyTyped(KeyEvent paramKeyEvent) {}
  
  public static void main(String[] paramArrayOfString)
  {
    JFrame localFrame = new JFrame("Know Your Role");
    Toolkit localToolkit = Toolkit.getDefaultToolkit();
    DialogCanvas localDialogCanvas = new DialogCanvas(400, 16, localToolkit);
    localDialogCanvas.setDialogText("Enter you alias");
    localDialogCanvas.addKeyListener(localDialogCanvas);
    localFrame.add(localDialogCanvas);
    localFrame.setVisible( true );
    localFrame.setSize(400, localDialogCanvas.height + 20);
    localFrame.validate();
  }
  
  public String makeStars(int paramInt)
  {
    String str = "";
    for (int i = 0; i < paramInt; i++) {
      str = str + "*";
    }
    return str;
  }
  
  public void paint(Graphics paramGraphics)
  {
    paramGraphics.setColor(Color.black);
    paramGraphics.fillRect(0, 0, width, height);
    if (messageMode)
    {
      paramGraphics.setFont(font);
      paramGraphics.setColor(ERROR_COLOUR);
      int i = fm.stringWidth(dialogText);
      int j = (width - i) / 2;
      int k = 4 * fontHeight;
      paramGraphics.drawString(dialogText, j, k);
    }
    else if (dialogOn)
    {
      super.paint(paramGraphics);
    }
    paramGraphics.setFont(font);
    paramGraphics.setColor(BORDER_COLOUR);
    paramGraphics.drawRect(0, 0, width - 1, height - 1);
    int i = fontHeight / 2;
    for (int j = 0; j < 7; j++)
    {
      paramGraphics.drawLine(0, fontHeight * j, borderLeft - 1, fontHeight * j);
      paramGraphics.drawLine(0, fontHeight * j + i, borderLeft - 1, fontHeight * j + i);
      paramGraphics.drawLine(borderRight + 1, fontHeight * j, width, fontHeight * j);
      paramGraphics.drawLine(borderRight + 1, fontHeight * j + i, width, fontHeight * j + i);
    }
    if (dialogOn)
    {
      drawText(paramGraphics, 1, DIALOG_COLOUR, dialogText);
      drawText(paramGraphics, 2, ERROR_COLOUR, "  " + errorText);
      if (userTextTwo.equals(""))
      {
        if (passwordMode) {
          drawText(paramGraphics, 3, USER_COLOUR, makeStars(userTextOne.length()) + '_');
        } else {
          drawText(paramGraphics, 3, USER_COLOUR, userTextOne + '_');
        }
      }
      else if (passwordMode)
      {
        drawText(paramGraphics, 3, USER_COLOUR, makeStars(userTextOne.length()));
        drawText(paramGraphics, 4, USER_COLOUR, makeStars(userTextTwo.length()) + '_');
      }
      else
      {
        drawText(paramGraphics, 3, USER_COLOUR, userTextOne);
        drawText(paramGraphics, 4, USER_COLOUR, userTextTwo + '_');
      }
    }
  }
  
  public void redrawRow(int paramInt)
  {
    Graphics localGraphics = getGraphics();
    if (localGraphics == null)
    {
      repaint();
    }
    else
    {
      localGraphics.setFont(font);
      eraseText(localGraphics, paramInt);
      switch (paramInt)
      {
      case 1: 
        drawText(localGraphics, 1, DIALOG_COLOUR, dialogText);
        break;
      case 2: 
        drawText(localGraphics, 2, ERROR_COLOUR, "  " + errorText);
        break;
      case 3: 
        if (userTextTwo.equals(""))
        {
          if (passwordMode) {
            drawText(localGraphics, 3, USER_COLOUR, makeStars(userTextOne.length()) + '_');
          } else {
            drawText(localGraphics, 3, USER_COLOUR, userTextOne + '_');
          }
        }
        else if (passwordMode) {
          drawText(localGraphics, 3, USER_COLOUR, makeStars(userTextOne.length()));
        } else {
          drawText(localGraphics, 3, USER_COLOUR, userTextOne);
        }
        break;
      case 4: 
        if (!userTextTwo.equals("")) {
          if (passwordMode) {
            drawText(localGraphics, 4, USER_COLOUR, makeStars(userTextTwo.length()) + '_');
          } else {
            drawText(localGraphics, 4, USER_COLOUR, userTextTwo + '_');
          }
        }
        break;
      }
    }
  }
  
  public void removeChar()
  {
    if (!userTextTwo.equals(""))
    {
      userTextTwo = userTextTwo.substring(0, userTextTwo.length() - 1);
      redrawRow(4);
    }
    else if (!userTextOne.equals(""))
    {
      userTextOne = userTextOne.substring(0, userTextOne.length() - 1);
      redrawRow(3);
    }
  }
  
  public void setDialogText(String paramString)
  {
    dialogOn = true;
    dialogText = paramString;
    repaint();
  }
  
  public void setErrorText(String paramString)
  {
    errorText = paramString;
    repaint();
  }
  
  public void setMessageText(String paramString)
  {
    messageMode = true;
    dialogText = paramString;
    repaint();
  }
}