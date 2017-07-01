package igx.server;

import igx.shared.Params;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IGXServer {
    private int m_portNum;
    private ServerSocket m_serverSocket;
    
    IGXServer( int portnum ){
        m_portNum = portnum;
    }
    
    public void run() {
        try{
            m_serverSocket = new ServerSocket( m_portNum );
        }catch( IOException ex ){
            System.out.println( "Can't listen" );
            return;
        }
        
        while( true ){
            try{
                Socket s = m_serverSocket.accept();
            }catch( IOException ex ){
                System.out.println( "Can't accept" );
            }
        }
    }
    
    public static void main( String[] args ){
        IGXServer ix = new IGXServer( Params.PORTNUM );
        ix.run();
    }
}
