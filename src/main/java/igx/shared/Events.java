package igx.shared;

// Events.java
// Class Events - Handles random events
import java.awt.Color;

public class Events
{
  public static final Color COLOUR = Color.white;
  // Data fields
  GameInstance Game;
  boolean crazyEvent = false;

  // Constructor
  public Events (GameInstance Game)
  {
	this.Game = Game;
  }  
/**
 * This method was created in VisualAge.
 * @return java.awt.Color
 * @param p igx.shared.Planet
 */
public Color getColor(Planet p) {
	return Params.PLAYERCOLOR[p.owner.number];
}
/**
 * This method was created in VisualAge.
 * @return java.awt.Color
 * @param p igx.shared.Planet
 */
public Color getColor(Player p) {
	return Params.PLAYERCOLOR[p.number];
}
  private int getWorstPlanet () {
	int worst = 0;
	int numBlackHoles = 0;
	for (int i = 0; i < Params.PLANETS; i++) {
	  if (Game.planet[i].blackHole) {
	numBlackHoles++;
	continue;
	  }
	  if (Game.planet[i].production < Game.planet[worst].production)
	worst = i;
	  else if (Game.planet[i].production == Game.planet[worst].production) {
	if (Game.planet[i].ratio < Game.planet[worst].ratio)
	  worst = i;
	else if ((Game.planet[i].ratio == Game.planet[worst].ratio) &&
		 (Game.planet[i].ships < Game.planet[worst].ships))
	  worst = i;
	  }
	}
	if (numBlackHoles == Params.PLANETS - 1)
	  return -1;
	else
	  return worst;
  }  
public void update() {
	// First, consider black hole situations
	if ((Game.turn > Params.BLACKHOLETURN) && (Game.turn % Params.BLACKHOLEPERIOD == 0)) {
		int worstNum = getWorstPlanet();
		if (worstNum != -1) {
			Planet worst = Game.planet[worstNum];
			worst.production = Params.SPECIALPRODMAX + 1;
			worst.ratio = 0;
			worst.owner = Player.NEUTRAL;
			worst.planetShade = 0;
			worst.ships = 0;
			worst.blackHole = true;
			Game.dirty[worstNum] = true;
			String[] text = {"The star that planet ", new Character(worst.planetChar).toString(), " is orbiting goes supernova and becomes a black hole!"};
			Color[] color = {COLOUR, getColor(worst), COLOUR};
			Game.ui.postSpecial(text, color);
		}
		return;
	} else
		if (Game.turn == Params.BLACKHOLETURN) {
			String[] text = {"Something is not right in the Galaxy... the end is near!"};
			Color[] color = {COLOUR};
			Game.ui.postSpecial(text, color);
			return;
		}
	int targetNum = Game.pseudo(0, Params.PLANETS - 1);
	Game.dirty[targetNum] = true;
	Planet target = Game.planet[targetNum];
	if ((target.owner == Player.NEUTRAL) || (Game.pseudo(0, 99) >= Params.EVENTCHANCE))
		return;
	int event = Game.pseudo(0, 999);
	if ((event -= Params.PRODINCREASE) < 0) {
		target.production = Math.min(target.production + Game.pseudo(Params.MINPRODUP, Params.MAXPRODUP), Params.SPECIALPRODMAX);
		String[] text = {"Production on ", new Character(target.planetChar).toString(), " increases to " + new Integer(target.production).toString() + "."};
		Color[] color = {COLOUR, getColor(target), COLOUR};
		Game.ui.postSpecial(text, color);
	} else
		if ((event -= Params.RATIOINCREASE) < 0) {
			target.ratio = Math.min(target.ratio + Game.pseudo(Params.MINRATIOUP, Params.MAXRATIOUP), Params.SPECIALRATIOMAX);
			String[] text = {"Ratio on ", new Character(target.planetChar).toString(), " increases to " + new Integer(target.ratio).toString() + "%."};
			Color[] color = {COLOUR, getColor(target), COLOUR};
			Game.ui.postSpecial(text, color);
		} else
			if ((event -= Params.SPECIALPRODINCREASE) < 0) {
				if (target.production != Params.SPECIALPRODMAX) {
					target.production = Params.SPECIALPRODMAX;
					String[] text = {"Production on ", new Character(target.planetChar).toString(), " sky-rockets to " + new Integer(target.production).toString() + "."};
					Color[] color = {COLOUR, getColor(target), COLOUR};
					Game.ui.postSpecial(text, color);
				}
			} else
				if ((event -= Params.SPECIALRATIOINCREASE) < 0) {
					if (target.ratio != Params.SPECIALRATIOMAX) {
						target.ratio = Params.SPECIALRATIOMAX;
						String[] text = {"Ratio on ", new Character(target.planetChar).toString(), " leaps to " + new Integer(target.ratio).toString() + "%."};
						Color[] color = {COLOUR, getColor(target), COLOUR};
						Game.ui.postSpecial(text, color);
					}
				} else
					if ((event -= Params.PRODDECREASE) < 0) {
						target.production = Math.max(target.production - Game.pseudo(Params.MINPRODDOWN, Params.MAXPRODDOWN), Params.SPECIALPRODMIN);
						String[] text = {"Production on ", new Character(target.planetChar).toString(), " decreases to " + new Integer(target.production).toString() + "."};
						Color[] color = {COLOUR, getColor(target), COLOUR};
						Game.ui.postSpecial(text, color);
					} else
						if ((event -= Params.RATIODECREASE) < 0) {
							target.ratio = Math.max(target.ratio - Game.pseudo(Params.MINRATIODOWN, Params.MAXRATIODOWN), Params.SPECIALRATIOMIN);
							String[] text = {"Ratio on planet ", new Character(target.planetChar).toString(), " decreases to " + new Integer(target.ratio).toString() + "%."};
							Color[] color = {COLOUR, getColor(target), COLOUR};
							Game.ui.postSpecial(text, color);
						} else
							if ((event -= Params.SPECIALPRODDECREASE) < 0) {
								if (target.production != Params.SPECIALPRODMIN) {
									target.production = Params.SPECIALPRODMIN;
									String[] text = {"Production on ", new Character(target.planetChar).toString(), " plummets to " + new Integer(target.production).toString() + "."};
									Color[] color = {COLOUR, getColor(target), COLOUR};
									Game.ui.postSpecial(text, color);
								}
							} else
								if ((event -= Params.SPECIALRATIODECREASE) < 0) {
									if (target.ratio != Params.SPECIALRATIOMIN) {
										target.ratio = Params.SPECIALRATIOMIN;
										String[] text = {"Ratio on planet ", new Character(target.planetChar).toString(), " drops to a shameful " + new Integer(target.ratio).toString() + "%."};
										Color[] color = {COLOUR, getColor(target), COLOUR};
										Game.ui.postSpecial(text, color);
									}
								} else
									if (((event -= Params.REVOLT) < 0) && (Game.players > 1)) {
										int revoltee;
										while ((revoltee = Game.pseudo(0, Game.players - 1)) == target.owner.number);
										String[] text = {"The people of ", new Character(target.planetChar).toString(), " rebel against ", target.owner.name, ".", null, "  They ally with ", Game.player[revoltee].name, "."};
										Color[] color = {COLOUR, getColor(target), COLOUR, getColor(target), COLOUR, null, COLOUR, getColor(Game.player[revoltee]), COLOUR};
										Game.ui.postSpecial(text, color);
										target.owner = Game.player[revoltee];
									} else
										if ((event -= Params.RELOCATE) < 0) {
											crazyEvent = true; // So a redraw will happen.
											int x, y;
											int oldX = target.x;
											int oldY = target.y;
											do {
												x = Game.pseudo(0, Params.MAPX - 1);
												y = Game.pseudo(0, Params.MAPY - 1);
											} while (Game.map[x][y] != Params.SPACE);
											Game.map[target.x][target.y] = Params.SPACE;
											target.x = x;
											target.y = y;
											Game.map[x][y] = Planet.num2char(targetNum);
											String[] text = {"Planet ", new Character(target.planetChar).toString(), " warps to a new location!"};
											Color[] color = {COLOUR, getColor(target), COLOUR};
											Game.ui.postSpecial(text, color);
											Game.ui.postPlanetMove(oldX, oldY, target);
										}
										// This must be the last event
										else
											if ((event -= Params.CRAZY) < 0) {
												event = Game.pseudo(0, 99);
												crazyEvent = true;
												if ((event -= Params.ALLFLEETSARRIVE) < 0) {
													Game.resolveAllConflicts = true;
													String[] text = {"TIME WARP! All fleets suddenly arrive!"};
													Color[] color = {COLOUR};
													Game.ui.postSpecial(text, color);
												} else
													if ((event -= Params.PLANETSRELOCATE) < 0) {
														for (int i = 0; i < Params.PLANETS; i++) {
															int x, y;
															target = Game.planet[i];
															do {
																x = Game.pseudo(0, Params.MAPX - 1);
																y = Game.pseudo(0, Params.MAPY - 1);
															} while (Game.map[x][y] != Params.SPACE);
															Game.map[target.x][target.y] = Params.SPACE;
															target.x = x;
															target.y = y;
															Game.map[x][y] = Planet.num2char(i);
															Game.dirty[i] = true;
														}
														Game.ui.postRedrawGalaxy();
														String[] text = {"REALITY SHIFT! All planets warp to new locations!"};
														Color[] color = {COLOUR};
														Game.ui.postSpecial(text, color);
													} else
														if ((event -= Params.REVERSEPRODUCTION) < 0) {
															for (int i = 0; i < Params.PLANETS; i++) {
																target = Game.planet[i];
																target.production = (target.production > Params.MAXPROD) ? Params.MINPROD : Params.MAXPROD - target.production;
																Game.dirty[i] = true;
															}
															String[] text = {"JUDGEMENT DAY! The meek shall be rewarded, the proud shall be punished..."};
															Color[] color = {COLOUR};
															Game.ui.postSpecial(text, color);
														} else
															if ((event -= Params.ALLFLEETSDESTROYED) < 0) {
																Game.fleets.first = null;
																Game.ui.postRedrawGalaxy();
																String[] text = {"COSMIC HURRICANE! All ships in transit and all attacking ships are destroyed!"};
																Color[] color = {COLOUR};
																Game.ui.postSpecial(text, color);
															} else
																if ((event -= Params.ALLDOCKEDDESTROYED) < 0) {
																	for (int i = 0; i < Params.PLANETS; i++)
																		if (Game.planet[i].owner != Player.NEUTRAL) {
																			Game.planet[i].ships = 0;
																			Game.dirty[i] = true;
																		}
																	String[] text = {"GALAXY-WIDE TERRORISM! All ships docked at planets are destroyed!"};
																	Color[] color = {COLOUR};
																	Game.ui.postSpecial(text, color);
																} else
																	if ((event -= Params.REROLLPRODUCTION) < 0) {
																		for (int i = 0; i < Params.PLANETS; i++)
																			if (Game.planet[i].owner != Player.NEUTRAL) {
																				Game.planet[i].production = Game.pseudo(Params.MINPROD, Params.MAXPROD);
																				Game.planet[i].ratio = Game.pseudo(Params.MINRATIO, Params.MAXRATIO);
																				Game.dirty[i] = true;
																			}
																		String[] text = {"ALTERNATE UNIVERSE! Similar, but different!"};
																		Color[] color = {COLOUR};
																		Game.ui.postSpecial(text, color);
																	} else
																		if ((event -= Params.BABYDUCK) < 0) {
																			String[] text = {"Look to Baby Duck for inspiration..."};
																			Color[] color = {Color.yellow};
																			Game.ui.postSpecial(text, color);
																		} else
																			if ((event -= Params.ARMAGEDDON) < 0) {
																				for (int i = 0; i < Params.PLANETS; i++)
																					if (Game.planet[i] != target) {
																						Planet current = Game.planet[i];
																						current.production = 0;
																						current.ratio = Params.SPECIALRATIOMIN;
																						if ((current.owner != Player.NEUTRAL) && (current.ships > 0)) {
																							Game.fleets.insert(new Fleet(Game, current, target, current.ships));
																							current.ships = 0;
																						}
																						Game.dirty[i] = true;
																					}
																				target.production = 666;
																				target.ratio = Params.SPECIALRATIOMIN;
																				String[] text = {"ARMAGEDDON! Let God sort 'em out at ", new Character(target.planetChar).toString(), "!"};
																				Color[] color = {COLOUR, getColor(target), COLOUR};
																				Game.ui.postSpecial(text, color);
																			}
											}
}
}