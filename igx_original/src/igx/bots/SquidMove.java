package igx.bots;

public class SquidMove extends Object 
{
  public int source;
  public int dest;
  public int ships;
  public int launchTime;

	public SquidMove(int src, int dst, int shps, int tim) {
    super();

    source = src;
		dest = dst;
		ships = shps;
		launchTime = tim;
	}

  public String toString() {
    return Planet.planetChar(source) + " to " + Planet.planetChar(dest) + " : " + ships
      + " (at time " + launchTime + ")";
  }
}
