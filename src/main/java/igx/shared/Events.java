package igx.shared;

import java.awt.Color;

public class Events
{
  public static final Color COLOUR = Color.white;
  GameInstance Game;
  boolean crazyEvent = false;
  
  public Events(GameInstance paramGameInstance)
  {
    Game = paramGameInstance;
  }
  
  public Color getColor(Planet paramPlanet)
  {
      
    return Params.PLAYERCOLOR[paramPlanet.owner.number];
  }
  
  public Color getColor(Player paramPlayer)
  {
    return Params.PLAYERCOLOR[paramPlayer.number];
  }
  
  private int getWorstPlanet()
  {
    int i = 0;
    int j = 0;
    for (int k = 0; k < 36; k++) {
      if (Game.planet[k].blackHole) {
        j++;
      } else if (Game.planet[k].production < Game.planet[i].production) {
        i = k;
      } else if (Game.planet[k].production == Game.planet[i].production) {
        if (Game.planet[k].ratio < Game.planet[i].ratio) {
          i = k;
        } else if ((Game.planet[k].ratio == Game.planet[i].ratio) && (Game.planet[k].ships < Game.planet[i].ships)) {
          i = k;
        }
      }
    }
    if (j == 35) {
      return -1;
    }
    return i;
  }
  
  public void update()
  {
    Object localObject2;
    if ((Game.turn > 100) && (Game.turn % 5 == 0))
    {
      int i = getWorstPlanet();
      if (i != -1)
      {
        Planet localObject1 = Game.planet[i];
        localObject1.production = 16;
        localObject1.ratio = 0;
        localObject1.owner = Player.NEUTRAL;
        localObject1.planetShade = 0;
        localObject1.ships = 0;
        localObject1.blackHole = true;
        Game.dirty[i] = true;
        String[] arrayOfString2 = { "The star that planet ", new Character(localObject1.planetChar).toString(), " is orbiting goes supernova and becomes a black hole!" };
        localObject2 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
        Game.ui.postSpecial(arrayOfString2, (Color[])localObject2);
      }
      return;
    }
    if (Game.turn == 100)
    {
      String[] arrayOfString1 = { "Something is not right in the Galaxy... the end is near!" };
      Color[] localObject1 = new Color[] { COLOUR };
      Game.ui.postSpecial(arrayOfString1, (Color[])localObject1);
      return;
    }
    int j = Game.pseudo(0, 35);
    Game.dirty[j] = true;
    Planet localObject1 = Game.planet[j];
    if ((localObject1.owner == Player.NEUTRAL) || (Game.pseudo(0, 99) >= 50)) {
      return;
    }
    int k = Game.pseudo(0, 999);
    Object localObject3;
    //What should these ratios be?
    if (k < 100)
    {
      int production = Math.min(localObject1.production + Game.pseudo(1, 5), 15);
      localObject2 = new String[] { "Production on ", new Character(localObject1.planetChar).toString(), " increases to " + new Integer(production).toString() + "." };
      localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
      Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
    }
    else if ( k > 100 && k < 200 )
    {
      int ratio = Math.min(localObject1.ratio + Game.pseudo(6, 25), 60);
      localObject2 = new String[] { "Ratio on ", new Character(localObject1.planetChar).toString(), " increases to " + new Integer(ratio).toString() + "%." };
      localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
      Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
    }
    else
    {
      k -= 51;
      if (k < 0)
      {
        if (localObject1.production != 15)
        {
          localObject1.production = 15;
          localObject2 = new String[] { "Production on ", new Character(localObject1.planetChar).toString(), " sky-rockets to " + new Integer(localObject1.production).toString() + "." };
          localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
          Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
        }
      }
      else
      {
        k -= 51;
        if (k < 0)
        {
          if (localObject1.ratio != 60)
          {
            localObject1.ratio = 60;
            localObject2 = new String[] { "Ratio on ", new Character(localObject1.planetChar).toString(), " leaps to " + new Integer(localObject1.ratio).toString() + "%." };
            localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
            Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
          }
        }
        else if (k - 141 < 0)
        {
          localObject1.production = Math.max(localObject1.production - Game.pseudo(1, 5), 0);
          localObject2 = new String[] { "Production on ", new Character(localObject1.planetChar).toString(), " decreases to " + new Integer(localObject1.production).toString() + "." };
          localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
          Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
        }
        else if (k - 141 < 0)
        {
          localObject1.ratio = Math.max(localObject1.ratio - Game.pseudo(6, 25), 1);
          localObject2 = new String[] { "Ratio on planet ", new Character(localObject1.planetChar).toString(), " decreases to " + new Integer(localObject1.ratio).toString() + "%." };
          localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
          Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
        }
        else
        {
          k -= 51;
          if (k < 0)
          {
            if (localObject1.production != 0)
            {
              localObject1.production = 0;
              localObject2 = new String[] { "Production on ", new Character(localObject1.planetChar).toString(), " plummets to " + new Integer(localObject1.production).toString() + "." };
              localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
              Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
            }
          }
          else
          {
            k -= 51;
            if (k < 0)
            {
              if (localObject1.ratio != 1)
              {
                localObject1.ratio = 1;
                localObject2 = new String[] { "Ratio on planet ", new Character(localObject1.planetChar).toString(), " drops to a shameful " + new Integer(localObject1.ratio).toString() + "%." };
                localObject3 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
                Game.ui.postSpecial((String[])localObject2, (Color[])localObject3);
              }
            }
            else
            {
              k -= 61;
              int m;
              if ((k < 0) && (Game.players > 1))
              {
                while ((m = Game.pseudo(0, Game.players - 1)) == localObject1.owner.number) {}
                localObject3 = new String[] { "The people of ", new Character(localObject1.planetChar).toString(), " rebel against ", localObject1.owner.name, ".", null, "  They ally with ", Game.player[m].name, "." };
                Color[] arrayOfColor2 = { COLOUR, getColor((Planet)localObject1), COLOUR, getColor((Planet)localObject1), COLOUR, null, COLOUR, getColor(Game.player[m]), COLOUR };
                Game.ui.postSpecial((String[])localObject3, arrayOfColor2);
                localObject1.owner = Game.player[m];
              }
              else
              {
                int i7;
                if (k - 170 < 0)
                {
                  crazyEvent = true;
                  i7 = localObject1.x;
                  int i8 = localObject1.y;
                  int i5;
                  do
                  {
                    m = Game.pseudo(0, 15);
                    i5 = Game.pseudo(0, 15);
                  } while (Game.map[m][i5] != '.');
                  Game.map[localObject1.x][localObject1.y] = '.';
                  localObject1.x = m;
                  localObject1.y = i5;
                  Game.map[m][i5] = Planet.num2char(j);
                  String[] arrayOfString9 = { "Planet ", new Character(localObject1.planetChar).toString(), " warps to a new location!" };
                  Color[] arrayOfColor3 = { COLOUR, getColor((Planet)localObject1), COLOUR };
                  Game.ui.postSpecial(arrayOfString9, arrayOfColor3);
                  Game.ui.postPlanetMove(i7, i8, (Planet)localObject1);
                }
                else
                {
                  k--;
                  if (k < 0)
                  {
                    k = Game.pseudo(0, 99);
                    crazyEvent = true;
                    k -= 15;
                    if (k < 0)
                    {
                      Game.resolveAllConflicts = true;
                      String[] arrayOfString3 = { "TIME WARP! All fleets suddenly arrive!" };
                      Color[] arrayOfColor1 = { COLOUR };
                      Game.ui.postSpecial(arrayOfString3, arrayOfColor1);
                    }
                    else
                    {
                      k -= 23;
                      Object localObject4;
                      if (k < 0)
                      {
                        for (int n = 0; n < 36; n++)
                        {
                          localObject1 = Game.planet[n];
                          int i6;
                          do
                          {
                            i6 = Game.pseudo(0, 15);
                            i7 = Game.pseudo(0, 15);
                          } while (Game.map[i6][i7] != '.');
                          Game.map[localObject1.x][localObject1.y] = '.';
                          localObject1.x = i6;
                          localObject1.y = i7;
                          Game.map[i6][i7] = Planet.num2char(n);
                          Game.dirty[n] = true;
                        }
                        Game.ui.postRedrawGalaxy();
                        String[] arrayOfString4 = { "REALITY SHIFT! All planets warp to new locations!" };
                        localObject4 = new Color[] { COLOUR };
                        Game.ui.postSpecial(arrayOfString4, (Color[])localObject4);
                      }
                      else
                      {
                        k -= 15;
                        String[] arrayOfString5;
                        if (k < 0)
                        {
                          for (int i1 = 0; i1 < 36; i1++)
                          {
                            localObject1 = Game.planet[i1];
                            localObject1.production = (localObject1.production > 10 ? 0 : 10 - localObject1.production);
                            Game.dirty[i1] = true;
                          }
                          arrayOfString5 = new String[] { "JUDGEMENT DAY! The meek shall be rewarded, the proud shall be punished..." };
                          localObject4 = new Color[] { COLOUR };
                          Game.ui.postSpecial(arrayOfString5, (Color[])localObject4);
                        }
                        else
                        {
                          k -= 15;
                          if (k < 0)
                          {
                            Game.fleets.first = null;
                            Game.ui.postRedrawGalaxy();
                            arrayOfString5 = new String[] { "COSMIC HURRICANE! All ships in transit and all attacking ships are destroyed!" };
                            localObject4 = new Color[] { COLOUR };
                            Game.ui.postSpecial(arrayOfString5, (Color[])localObject4);
                          }
                          else
                          {
                            k -= 15;
                            if (k < 0)
                            {
                              for (int i2 = 0; i2 < 36; i2++) {
                                if (Game.planet[i2].owner != Player.NEUTRAL)
                                {
                                  Game.planet[i2].ships = 0;
                                  Game.dirty[i2] = true;
                                }
                              }
                              String[] arrayOfString6 = { "GALAXY-WIDE TERRORISM! All ships docked at planets are destroyed!" };
                              localObject4 = new Color[] { COLOUR };
                              Game.ui.postSpecial(arrayOfString6, (Color[])localObject4);
                            }
                            else
                            {
                              k -= 15;
                              String[] arrayOfString7;
                              if (k < 0)
                              {
                                for (int i3 = 0; i3 < 36; i3++) {
                                  if (Game.planet[i3].owner != Player.NEUTRAL)
                                  {
                                    Game.planet[i3].production = Game.pseudo(0, 10);
                                    Game.planet[i3].ratio = Game.pseudo(20, 49);
                                    Game.dirty[i3] = true;
                                  }
                                }
                                arrayOfString7 = new String[] { "ALTERNATE UNIVERSE! Similar, but different!" };
                                localObject4 = new Color[] { COLOUR };
                                Game.ui.postSpecial(arrayOfString7, (Color[])localObject4);
                              }
                              else
                              {
                                k--;
                                if (k < 0)
                                {
                                  arrayOfString7 = new String[] { "Look to Baby Duck for inspiration..." };
                                  localObject4 = new Color[] { Color.yellow };
                                  Game.ui.postSpecial(arrayOfString7, (Color[])localObject4);
                                }
                                else
                                {
                                  k--;
                                  if (k < 0)
                                  {
                                    for (int i4 = 0; i4 < 36; i4++) {
                                      if (Game.planet[i4] != localObject1)
                                      {
                                        localObject4 = Game.planet[i4];
                                        localObject1.production = 0;
                                        localObject1.ratio = 1;
                                        if ((localObject1.owner != Player.NEUTRAL) && (localObject1.ships > 0))
                                        {
                                          Game.fleets.insert(new Fleet(Game, (Planet)localObject4, (Planet)localObject1, localObject1.ships));
                                          localObject1.ships = 0;
                                        }
                                        Game.dirty[i4] = true;
                                      }
                                    }
                                    localObject1.production = 666;
                                    localObject1.ratio = 1;
                                    String[] arrayOfString8 = { "ARMAGEDDON! Let God sort 'em out at ", new Character(localObject1.planetChar).toString(), "!" };
                                    localObject4 = new Color[] { COLOUR, getColor((Planet)localObject1), COLOUR };
                                    Game.ui.postSpecial(arrayOfString8, (Color[])localObject4);
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}