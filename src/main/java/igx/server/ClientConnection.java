package igx.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a single client connection to the server.
 * 
 * See documentation/protocol.md for information on the protocol
 */
class ClientConnection {
    private static final Logger logger = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "3.8";
    
    /**
     * Represents the stage of the protocl that we are at.
     */
    private enum ProtocolState{
        READING_VERSION,
        READING_NAME,
        READING_PASSWORD
    }
    
    private ReadableByteChannel m_read;
    private WritableByteChannel m_write;
    //private ByteBuffer m_readBuffer;
    private ByteBuffer m_writeBuffer;
    private boolean m_isClosed;
    private ProtocolState m_protocolState;
    private Scanner m_scanner;
    private String m_alias;
    private int m_numberFailedReads;
    
    ClientConnection( ReadableByteChannel readable, WritableByteChannel writable ) throws IOException {
        m_read = readable;
        m_write = writable;
        //m_readBuffer = ByteBuffer.allocate( 64 );
        m_writeBuffer = ByteBuffer.allocate( 256 );
        m_isClosed = false;
        m_scanner = new Scanner( m_read );
        m_numberFailedReads = 0;
        
        //the first state for the protocol is to read if the client accepts the version
        m_protocolState = ProtocolState.READING_VERSION;
        
        //Whenever a new client connects, tell it the version
        writeStringWithNewline( PROTOCOL_VERSION );
        writeBuffer();
        logger.debug( "Done creating new connection" );
    }
    
    boolean isClosed(){
        return m_isClosed;
    }
    
    /**
     * Read all of the data in the buffer and act on it appropriately.
     */
    void parseData() throws IOException {
//        int length = m_read.read( m_readBuffer );
//        if( length == -1 ){
//            m_isClosed = true;
//            return;
//        }
//        logger.debug( "Incoming data: {}", m_readBuffer.array() );
//        
//        m_readBuffer.flip();
        
        if( !m_scanner.hasNextLine() ){
            m_numberFailedReads++;
            
            if( m_numberFailedReads > 2 ){
                //too much data without a newline, close this connection
                //because it's probably invalid at this point.
                //(insecure protocol, could be an attack)
                logger.warn( "Closing connection, too much data without a newline" );
                m_isClosed = true;
            }
            
            return;
        }
        
        m_numberFailedReads = 0;

        String line = m_scanner.nextLine();
        logger.debug( "Incoming data: {}", line );
        if( m_protocolState == ProtocolState.READING_VERSION ){

            if( line.compareTo( "[" ) == 0 ){
                logger.debug( "Good on the protocol!" );
                m_protocolState = ProtocolState.READING_NAME;
            }else{
                logger.debug( "bad protocol!" );
                m_isClosed = true;
            }
        }else if( m_protocolState == ProtocolState.READING_NAME ){
            m_alias = line;
            logger.debug( "Client alias is {}", m_alias );
            
            //alias read OK
            m_protocolState = ProtocolState.READING_PASSWORD;
            writeStringWithNewline( "}" );
            writeBuffer();
        }else if( m_protocolState == ProtocolState.READING_PASSWORD ){
            logger.debug( "Password is {}", line );
            
            //correct password
            writeStringWithNewline( "[" );
            writeBuffer();
            
            //tell the client server information
            writeString( "Welcome to Intergalactics, " );
            writeString( m_alias );
            writeStringWithNewline( "!" );
            writeStringWithNewline( "This server is <SERVER-NAME>" );
            writeStringWithNewline( "~" );
            writeBuffer();
            
            //tell the client any robot information
            writeStringWithNewline( "~" );
        }
    }
    
    private void writeString( String str ){
        m_writeBuffer.put( str.getBytes() );
    }
    
    /**
     * Put a string, followed by a newline character into our buffer
     * @param str 
     */
    private void writeStringWithNewline( String str ){
        m_writeBuffer.put( str.getBytes() );
        m_writeBuffer.put( "\n".getBytes() );
    }
    
    /**
     * Write out our buffer
     */
    private void writeBuffer() throws IOException {
        m_writeBuffer.flip();
        while( m_writeBuffer.hasRemaining() ){
            m_write.write( m_writeBuffer );
        }
        m_writeBuffer.flip();
        m_writeBuffer.clear();
    }
    
}
