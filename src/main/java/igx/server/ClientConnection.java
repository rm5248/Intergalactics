package igx.server;

import igx.shared.Forum;
import igx.shared.Message;
import igx.shared.Params;
import igx.shared.Player;
import igx.shared.Robot;
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
     * Represents the stage of the protocol that we are at.
     */
    private enum ProtocolState {
        READING_VERSION,
        READING_NAME,
        READING_PASSWORD,
        IN_LOBBY
    }

    /**
     * The client can add different things. Each is prefaced by a '+'
     */
    private enum ThingToAdd {
        MESSAGE,
        ROBOT,
        NEW_GAME
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
    private boolean m_adding;
    private ThingToAdd m_thingToAdd;
    private ServerForum m_forum;
    private Player m_us;

    ClientConnection(ReadableByteChannel readable,
            WritableByteChannel writable,
            ServerForum f) throws IOException {
        m_read = readable;
        m_write = writable;
        //m_readBuffer = ByteBuffer.allocate( 64 );
        m_writeBuffer = ByteBuffer.allocate(256);
        m_isClosed = false;
        m_scanner = new Scanner(m_read);
        m_numberFailedReads = 0;
        m_adding = false;
        m_forum = f;

        //the first state for the protocol is to read if the client accepts the version
        m_protocolState = ProtocolState.READING_VERSION;

        //Whenever a new client connects, tell it the version
        writeStringWithNewline(PROTOCOL_VERSION);
        writeBuffer();
        logger.debug("Done creating new connection");
    }

    boolean isClosed() {
        return m_isClosed;
    }

    public Player getPlayer() {
        return m_us;
    }

    void sendMessage(Message msg) {
        switch (msg.getType()) {

        }
    }

    /**
     * Read all of the data in the buffer and act on it appropriately.
     */
    void parseData() throws IOException {

        if (!m_scanner.hasNextLine()) {
            m_numberFailedReads++;

            if (!m_read.isOpen()) {
                logger.debug("Connection is no longer open: not parsing");
                return;
            }

            if (m_numberFailedReads > 2) {
                //too much data without a newline, close this connection
                //because it's probably invalid at this point.
                //(insecure protocol, could be an attack)
                logger.warn("Closing connection, too much data without a newline");
                m_isClosed = true;
            }

            return;
        }

        m_numberFailedReads = 0;

        while (m_scanner.hasNextLine()) {
            if (!m_read.isOpen()) {
                logger.debug("Connection is no longer open: not parsing");
                return;
            }

            String line = m_scanner.nextLine();
            logger.debug("Incoming data: {}", line);
            if (m_protocolState == ProtocolState.READING_VERSION) {

                if (line.compareTo( Params.ACK ) == 0) {
                    logger.debug("Good on the protocol!");
                    m_protocolState = ProtocolState.READING_NAME;
                } else {
                    logger.debug("bad protocol!");
                    m_isClosed = true;
                }
            } else if (m_protocolState == ProtocolState.READING_NAME) {
                if( !isAliasValid( line ) ){
                    writeStringWithNewline( Params.UPDATE );
//                    m_protocolState = ProtocolState.IN_LOBBY;
//                    m_alias = line;
                    continue;
                }
                
                if( m_forum.getRobot( line ) != null ){
                    writeStringWithNewline( Params.ADDCOMPUTERPLAYER );
//                    m_protocolState = ProtocolState.IN_LOBBY;
//                    m_alias = line;
                    continue;
                }
                m_alias = line;
                logger.debug("Client alias is {}", m_alias);

                //alias read OK
                m_protocolState = ProtocolState.READING_PASSWORD;
                
                //If this user already exists, tell them to enter their password.
                //if this user does not exist, thell them to create a password
                if( m_forum.userAlreadyExists(m_alias) ){
                    writeStringWithNewline( Params.OLD_ALIAS );
                }else{
                    writeStringWithNewline( Params.NEW_ALIAS );
                }
                writeBuffer();
            }else if (m_protocolState == ProtocolState.READING_PASSWORD) {
                logger.debug("Password is {}", line);
                if( !m_forum.userAlreadyExists( m_alias ) ){
                    m_forum.createNewUser(m_alias, line);
                }

                if( m_forum.checkUserPassword( m_alias, line ) ){
                     //correct password
                    writeStringWithNewline( Params.ACK );
                    writeBuffer();
                }else{
                    writeStringWithNewline( Params.NACK );
                    writeBuffer();
                    m_protocolState = ProtocolState.READING_NAME;
                    continue;
                }
               

                m_us = m_forum.addPlayer(m_alias);

                //Send all of the bulletins
                for( String bulletin : m_forum.getBulletins() ){
                    writeStringWithNewline( bulletin );
                }
                writeStringWithNewline( Params.ENDTRANSMISSION );
                writeBuffer();

                //tell the client any robot information
                for( Robot bot : m_forum.getBots() ){
                    writeStringWithNewline( bot.botType );
                    writeStringWithNewline( bot.name );
                    writeStringWithNewline( bot.ranking + "" );
                }
                writeStringWithNewline( Params.ENDTRANSMISSION );
                writeBuffer();

                //tell the client any player information
                writeStringWithNewline(m_alias);
                writeStringWithNewline("1");
                writeStringWithNewline("User2");
                writeStringWithNewline("1");
                writeStringWithNewline("~");

                //tell the client any game information
                writeStringWithNewline("GameName"); //name of game
                writeStringWithNewline("["); //in progress = [, not in progress = ]
                writeStringWithNewline(1 + ""); //number of users
                writeStringWithNewline("User2"); //creator
                writeStringWithNewline("1");
                writeStringWithNewline("~");

                writeStringWithNewline("]"); //we're not in-game?
                writeBuffer();

                m_protocolState = ProtocolState.IN_LOBBY;
            } else if (m_protocolState == ProtocolState.IN_LOBBY) {
                //We are in the lobby.  We can do several different commands here,
                //such as chat message, create game, etc.
                if (line.equals("+")) {
                    m_adding = true;
                    continue;
                }

                if (line.equals("@")) {
                    m_thingToAdd = ThingToAdd.MESSAGE;
                    continue;
                } else if (line.equals("+")) {
                    m_thingToAdd = ThingToAdd.NEW_GAME;
                    continue;
                }

                if (m_adding) {
                    switch (m_thingToAdd) {
                        case MESSAGE:
                            logger.debug("incoming message {}", line);
                            break;
                    }
                }
            }

        }
    }

    private void writeString(String str) {
        m_writeBuffer.put(str.getBytes());
    }

    /**
     * Put a string, followed by a newline character into our buffer
     *
     * @param str
     */
    private void writeStringWithNewline(String str) {
        m_writeBuffer.put(str.getBytes());
        m_writeBuffer.put("\n".getBytes());
    }

    /**
     * Write out our buffer
     */
    private void writeBuffer() throws IOException {
        m_writeBuffer.flip();
        while (m_writeBuffer.hasRemaining()) {
            m_write.write(m_writeBuffer);
        }
        m_writeBuffer.flip();
        m_writeBuffer.clear();
    }

    private boolean isAliasValid(String alias) {
        if (alias.length() == 0) {
            return false;
        }
        char[] c = alias.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (Character.isLetter(c[i])
                    || Character.isDigit(c[i])
                    || (c[i] == '_')
                    || (c[i] == ' ')) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
