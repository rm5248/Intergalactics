package igx.bots.smart;

// PlanetFilter.java

import igx.bots.*;
import java.util.*;

public abstract class PlanetFilter
{
  protected GameState g;

  public PlanetFilter (GameState g) {
	this.g = g;
  }  
  public abstract int compare (int a, int b);  
  public int[] getValidPlanets () {
	Vector pList = new Vector();
	for (int i = 0; i < Constants.PLANETS; i++) {
	  if (isValid(i))
	pList.addElement(new Integer(i));
	}
	int n = pList.size();
	int[] result = new int[n];
	for (int i = 0; i < n; i++)
	  result[i] = ((Integer)pList.elementAt(i)).intValue();
	return result;
  }  
  public abstract boolean isValid (int p);  
}