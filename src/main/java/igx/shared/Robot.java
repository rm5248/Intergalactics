package igx.shared;

// Robot.java 
public class Robot {

    public String botType;
    public String name;
    public Class botClass = null;
    public int ranking;
    public int skill;

    public Robot(String type, String name, int ranking) {
        this.botType = type;
        this.name = name;
        this.ranking = ranking;
    }

    public void setClass(Class botClass) {
        this.botClass = botClass;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public Player toPlayer() {
        Player p = new Player(name);
        p.isHuman = false;
        return p;
    }
}
