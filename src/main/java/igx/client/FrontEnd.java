package igx.client;

// FrontEnd.java 
import igx.shared.*;
import java.awt.*;

public interface FrontEnd {
    // Get the main container for the frontend

    public Container getContainer();
    // Get interior dimensions

    public Dimension getDimensions();
    // Get sound state

    public boolean getSoundMode();
    // Player starts game
    // public void beginGame (NewGame game, String playerName);
    // Play sound file

    public void play(String file);
    // Play sound

    public void playSound(String file);
    // Player quits game
    // public void quitGame ();
    // Player quits program

    public void quitProgram();
    // Toggle sound

    public void setSoundMode(boolean onOff);

    /**
     * This method was created in VisualAge.
     *
     * @param serverVersion java.lang.String
     */
    void versionProblem(String serverVersion);
}
