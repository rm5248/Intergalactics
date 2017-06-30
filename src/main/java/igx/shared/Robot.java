package igx.shared;

public class Robot
{
  public String botType;
  public String name;
  public Class botClass = null;
  public int ranking;
  public int skill;
  
  public Robot(String paramString1, String paramString2, int paramInt)
  {
    botType = paramString1;
    name = paramString2;
    ranking = paramInt;
  }
  
  public void setClass(Class paramClass)
  {
    botClass = paramClass;
  }
  
  public void setSkill(int paramInt)
  {
    skill = paramInt;
  }
  
  public Player toPlayer()
  {
    Player localPlayer = new Player(name);
    //isHuman = false;
    return localPlayer;
  }
}