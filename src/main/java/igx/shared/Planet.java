package igx.shared;

// Planet.java
// Class Planet
import java.util.*;
import java.awt.*;

public class Planet {
    // Constants

    public static final int NO_ATTACKERS = -1;
    public static final int MULTIPLE_ATTACKERS = -2;

    // Data fields
    public int x, y;
    public Player owner = Player.NEUTRAL;
    public int production, ratio, ships, defenceRatio;
    public int prodTurns = Params.SEGMENTS;
    public Vector attackers = new Vector();
    public int[] attacker = new int[Params.MAXPLAYERS];
    public int attackingPlayer = NO_ATTACKERS;
    public int totalAttackingShips = 0;
    public GameInstance Game;
    public char planetChar;
    public int planetSize; // % size
    public int planetShade; // % shade
    public boolean blackHole = false;

    public Planet(int number, GameInstance Game) {
        this.Game = Game;
        planetChar = num2char(number);
        planetShade = Game.pseudo(24, 48);
        planetSize = Game.pseudo(25, 90); // Per Cent
    }
    // Constructor

    public Planet(GameInstance Game, int number) {
        this.Game = Game;
        ships = Game.pseudo(Params.MINNEUTRALSHIPS, Params.MAXNEUTRALSHIPS);
        production = Game.pseudo(Params.MINPROD, Params.MAXPROD);
        ratio = Game.pseudo(Params.MINRATIO, Params.MAXRATIO);
        defenceRatio = ratio + Game.pseudo(Params.MINDEFENCEBONUS, Params.MAXDEFENCEBONUS);
        planetChar = num2char(number);
        planetShade = Game.pseudo(24, 48);
        planetSize = Game.pseudo(25, 90); // Per Cent
    }

    public static int char2num(char c) {
        if (Character.isDigit(c)) {
            return Character.digit(c, 10) + 26;
        } else {
            return Character.digit(c, 36) - 10;
        }
    }
    // Converter between number and symbol, and vice versa

    public static char num2char(int n) {
        if (n > Params.PLANETS) {
            return '\u0000';
        } else if (n < 26) {
            return (char) ('\u0041' + n);
        } else {
            return (char) ('\u0030' + n - 26);
        }
    }
// UPDATE - Called every segment to update events on the planet

    public int update() {
        if (!attackers.isEmpty()) // Attackers exist, defend
        {
            Game.dirty[char2num(planetChar)] = true;
            int attackingShips = 0;
            for (int i = 0; i < attackers.size(); i++) {
                attackingShips += ((Fleet) attackers.elementAt(i)).ships;
            }
            // PHASING THIS OUT - MAKES 1 SHIP FLEETS TOO POWERFUL
            /*
		int attackNumber = ships * Params.DEFENDSTRENGTH / 100;
		if ((ships * Params.DEFENDSTRENGTH) % 100 > 0)
			// Ceiling
			attackNumber++;
             */
            int attackNumber = ships;
            for (int i = 0; i < attackNumber; i++) {
                if (Game.pseudo(0, 99) < Params.DEFENDSTRENGTH) {
                    if ((Game.pseudo(0, 99) < defenceRatio) && (attackingShips > 0)) {
                        int j = 0;
                        int attackedShip = Game.pseudo(0, attackingShips - 1);
                        while ((attackedShip -= ((Fleet) attackers.elementAt(j)).ships) >= 0) {
                            j++;
                        }
                        Fleet target = ((Fleet) attackers.elementAt(j));
                        attackingShips--;
                        attacker[target.owner.number]--;
                        if ((--target.ships) == 0) {
                            Game.ui.postRepulsion(target.owner, this);
                            Game.fleets.remove(target);
                            attackers.removeElementAt(j);
                        }
                    }
                }
            }
        }
        attackingPlayer = NO_ATTACKERS;
        totalAttackingShips = 0;
        for (int i = 0; i < Game.players; i++) {
            if (attacker[i] != 0) {
                if (attackingPlayer == NO_ATTACKERS) {
                    attackingPlayer = i;
                } else {
                    attackingPlayer = MULTIPLE_ATTACKERS;
                }
                totalAttackingShips += attacker[i];
            }
        }
        attackers = new Vector();
        if (owner != Player.NEUTRAL) {
            prodTurns -= 1;
            if (prodTurns == 0) // Produce ships and change defence bonus
            {
                Game.dirty[char2num(planetChar)] = true;
                prodTurns = Params.SEGMENTS;
                ships += production;
                defenceRatio = ratio + Game.pseudo(Params.MINDEFENCEBONUS, Params.MAXDEFENCEBONUS);
            }
        }
        return ships;
    }
}
