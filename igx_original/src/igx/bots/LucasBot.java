// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LucasBot.java

package igx.bots;


// Referenced classes of package igx.bots:
//            Bot, Constants, GameState, Planet, 
//            Message

public class LucasBot extends Bot
{

    public void newGame(GameState gamestate, int i)
    {
        myNumber = getNumber();
        switch(i)
	    {
	    case 0: // '\0'
		explore = 11;
		aggression = 35;
		return;

	    case 1: // '\001'
		explore = 12;
		aggression = 30;
		return;

	    case 2: // '\002'
		explore = 13;
		aggression = 25;
		return;
	    }
        explore = 11;
        aggression = 35;
    }

    public String createName(int i)
    {
        switch(i)
	    {
	    case 0: // '\0'
		return "C3PO";

	    case 1: // '\001'
		return "R2D2";

	    case 2: // '\002'
		return "IG88";
	    }
        i = 0;
        return "C3PO";
    }

    public int bestPlanetOfMine(GameState gamestate)
    {
        int i = 0;
        int j = -1;
        int k = -1;
        int l = -1;
        for(int i1 = 0; i1 < 36; i1++)
	    {
		Planet planet = gamestate.getPlanet(i1);
		int j1 = planet.getShips();
		int k1 = planet.getRatio();
		int l1 = planet.getProduction();
		int i2 = (j1 * (k1 + 75) * 100) / (10 + l1);
		int j2 = planet.getTotalAttackers();
		if(j2 > 0)
		    {
			if(j2 > 2 * j1)
			    {
				j = i1;
				k = j1;
			    }
		    } else
			if(planet.getOwner() == myNumber && i2 > i)
			    {
				i = i2;
				l = i1;
			    }
	    }

        if(j != -1 && l != -1)
            sendFleet(j, l, k);
        return l;
    }

    public int bestEnemyPlanet(GameState gamestate, int i)
    {
        int j = -1;
        int k = -1;
        for(int l = 0; l < 36; l++)
	    {
		Planet planet = gamestate.getPlanet(l);
		int i1 = planet.getProduction();
		if(planet.isNeutral())
		    i1 = explore;
		double d = gamestate.getDistance(i, l);
		i1 = (int)((double)(i1 * productionValue) / (d * d));
		if(planet.getOwner() != myNumber && i1 > j)
		    {
			j = i1;
			k = l;
		    }
	    }

        return k;
    }

    public void update(GameState gamestate, GameState gamestate1, Message amessage[])
    {
        int i = bestPlanetOfMine(gamestate);
        if(i == -1)
            return;
        Planet planet = gamestate.getPlanet(i);
        int j = (planet.getShips() - 5) * (planet.getRatio() + 75);
        int k = bestEnemyPlanet(gamestate, i);
        if(k == -1)
            return;
        Planet planet1 = gamestate.getPlanet(k);
        Planet planet2 = gamestate.getPlanet(i);
        if(2 * planet2.getShips() < planet2.getProduction() && !planet1.isNeutral())
            return;
        int l = (int)((double)planet1.getShips() + ((double)planet1.getProduction() * (gamestate.getDistance(i, k) / 10D) + 1.0D)) * (planet1.getRatio() + 75);
        if(planet1.isNeutral())
            l = 10 * (30 - 2 * explore);
        if(j * 100 > l * (100 + aggression))
            if(planet1.isNeutral())
                sendFleet(i, k, planet.getShips());
            else
                sendFleet(i, k, (l / planet.getRatio()) * ((100 + aggression) / 100));
        if(Bot.random(0, 39) == 0)
	    {
		for(int i1 = 0; i1 < 36; i1++)
		    {
			Planet planet3 = gamestate.getPlanet(i1);
			if(planet3.getOwner() == myNumber)
			    {
				int k1 = planet3.getShips();
				for(int l1 = 0; l1 < 36; l1++)
				    {
					Planet planet4 = gamestate.getPlanet(l1);
					double d = 8D;
					double d1 = gamestate.getDistance(i1, l1);
					if(planet4.getOwner() == myNumber && d1 <= d && 3 * planet3.getRatio() < 2 * planet4.getRatio() && k1 > 6)
					    {
						sendFleet(i1, l1, k1 / 2);
						l1 = 36;
					    }
				    }

			    }
		    }

	    }
        int j1 = Bot.random(0, 250);
        switch(j1)
	    {
	    default:
		break;

	    case 0: // '\0'
		if(getSkillLevel() == 0)
		    sendMessage(9, "You'll never get me in one of those horrible starships.");
		if(getSkillLevel() == 1)
		    {
			sendMessage(9, "BWEEEboooWEEdwoooop");
			return;
		    }
		break;

	    case 1: // '\001'
		if(getSkillLevel() == 0)
		    sendMessage(9, "I am fluent in over 3 million forms of communication.");
		if(getSkillLevel() == 1)
		    {
			sendMessage(9, "woooDEEEleep");
			return;
		    }
		break;

	    case 2: // '\002'
		if(getSkillLevel() == 0)
		    sendMessage(9, "SIR!  That was a breach of protocol.");
		if(getSkillLevel() == 1)
		    {
			sendMessage(9, "WAAAAAAAAAAAAAAAAAAAAAAAAAAH");
			return;
		    }
		break;
	    }
    }

    public LucasBot()
    {
        productionValue = 0x989680;
    }

    private int myNumber;
    private int productionValue;
    private int explore;
    private int aggression;
}
