package igx.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a single client connection to the server
 */
class ClientConnection {
    private static final Logger logger = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "3.8";
    
    ClientConnection(){
        
    }
    
    /**
     * Data is coming in from the client
     * 
     * @param data 
     */
    void incomingData( byte[] data ){
        String str = new String( data );
        
        logger.debug( "Incoming data: {}", str );
    }
    
}
