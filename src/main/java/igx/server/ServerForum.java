package igx.server;

import igx.shared.Forum;
import igx.shared.Player;
import igx.shared.Robot;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a forum on the server.
 */
public class ServerForum extends Forum {
    
    private List<ClientConnection> m_clients;
    
    public ServerForum(Robot[] paramArrayOfRobot) {
        super(paramArrayOfRobot);
        
        m_clients = new ArrayList<>();
    }
 
    protected void message(String playerName, String messageText, int paramInt) {
        
    }
    
    public Player addPlayer( String alias ){
        Player p = super.addPlayer( alias );
        
        return p;
    }
}
