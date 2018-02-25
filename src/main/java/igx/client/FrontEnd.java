package igx.client;

import java.awt.Container;
import java.awt.Dimension;

public abstract interface FrontEnd
{
  public abstract Container getContainer();
  
  public abstract Dimension getSize();
  
  public abstract boolean getSoundMode();
  
  public abstract void play(String paramString);
  
  public abstract void playSound(String paramString);
  
  public abstract void quitProgram();
  
  public abstract void setSoundMode(boolean paramBoolean);
  
  public abstract void versionProblem(String paramString);
}