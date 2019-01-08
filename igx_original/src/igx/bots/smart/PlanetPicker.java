package igx.bots.smart;

// PlanetPicker.java

import igx.bots.*;

public class PlanetPicker
{
  private GameState game;
  private PlanetFilter filter;
  boolean[] picked = new boolean[Constants.PLANETS];

  public PlanetPicker (GameState game, PlanetFilter filter) {
	this.filter = filter;
	this.game = game;
	reset();
  }  
  public int getNextPlanet () {
	int best = -1;
	Planet bestPlanet = null;
	for (int i = 0; i < Constants.PLANETS; i++) {
	  Planet p = game.getPlanet(i);
	  if (!picked[i] && filter.isValid(i)) {
	if (best == -1) {
	  best = i;
	  bestPlanet = p;
	} else {
	  if (filter.compare(i, best) > 0) {
	    best = i;
	    bestPlanet = p;
	  }
	}
	  }
	}
	if (best != -1)
	  picked[best] = true;
	return best;
  }  
  public void reset () {
	for (int i = 0; i < Constants.PLANETS; i++)
	  picked[i] = false;
  }  
}