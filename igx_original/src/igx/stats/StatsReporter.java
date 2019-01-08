package igx.stats;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.Timestamp;

public interface StatsReporter extends Remote {
	public void reportGame (Timestamp time,
			    int winnerID,
			    String[] players,
			    Vector timeStates,
			    Vector attacks,
			    Vector events,
			    Vector updates) throws RemoteException;
}
