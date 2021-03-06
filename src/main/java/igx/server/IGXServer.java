package igx.server;

import igx.shared.Forum;
import igx.shared.Game;
import igx.shared.Params;
import igx.shared.Robot;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.sqlite.SQLiteConfig;

public class IGXServer {

    private int m_portNum;
    private ServerSocketChannel m_serverSocket;
    private List<ClientConnection> m_clients;
    private Map<SocketChannel, ClientConnection> m_socketToClient;
    private ServerForum m_mainForum;
    private DSLContext m_context;
    private Connection m_sqlConnection;

    private static final Logger logger = LogManager.getLogger();

    IGXServer( Properties props ) throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setPragma(SQLiteConfig.Pragma.FOREIGN_KEYS, "on" );
        m_sqlConnection = DriverManager.getConnection( props.getProperty( "db.url" ), config.toProperties() );
        Flyway flyway = Flyway.configure().dataSource(props.getProperty( "db.url" ), null, null).load();
        flyway.migrate();
        
        m_context = DSL.using( m_sqlConnection );
        //m_portNum = portnum;
        m_clients = new ArrayList<ClientConnection>();
        m_socketToClient = new HashMap<SocketChannel, ClientConnection>();
        m_mainForum = new ServerForum(null, null, m_context);

        logger.debug("Creating new server on port {}", m_portNum);
    }

    void run() throws IOException {
        Selector selector = Selector.open();

        m_serverSocket = ServerSocketChannel.open();
        m_serverSocket.socket().bind(new InetSocketAddress(Params.PORTNUM));
        m_serverSocket.configureBlocking(false);

        SelectionKey serverSelectionKey = m_serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int readyChannels = selector.select();

            if (readyChannels == 0) {
                logger.trace("No channels to select");
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key == serverSelectionKey) {
                    //create our new client and add to our internal 
                    //list of clients
                    SocketChannel socket = m_serverSocket.accept();
                    socket.configureBlocking(false);
                    logger.debug("New connection from {}", socket.getRemoteAddress());
                    ClientConnection client = null;

                    try {
                        client = new ClientConnection(socket, socket, m_mainForum);
                    } catch (IOException ex) {
                        logger.error("Unable to create new client: ", ex);
                        continue;
                    }
                    m_clients.add(client);
                    m_socketToClient.put(socket, client);

                    //create a new selection key for this client, so that when
                    //there is data to read we will add it to the appropriate
                    //client
                    socket.register(selector, SelectionKey.OP_READ, socket);
                    continue;
                }

                if (key.isReadable()) {
                    SocketChannel socket = (SocketChannel) key.attachment();
                    ClientConnection client = m_socketToClient.get(socket);

                    if (client == null) {
                        logger.error("Readable key, but no client?!");
                        continue;
                    }

//                    if( socket.isOpen() ){
//                        logger.debug( "isopen" );
//                    }else{
//                        logger.debug( "not open" );
//                    }
//                    
//                    if( socket.isConnected() ){
//                        logger.debug( "connected" );
//                    }else{
//                        logger.debug( "not connected" );
//                    }
                    client.parseData();
                    if (client.isClosed()) {
                        socket.close();
                        logger.debug("Client has exited");
                        m_socketToClient.remove(socket);
                        m_clients.remove(client);
                        key.cancel();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Properties props;

        props = new Properties();
        props.setProperty( "db.url", "jdbc:sqlite:file:./igx.db" );

        try {
            IGXServer ix = new IGXServer( props );
            ix.run();
        } catch (IOException ex) {
            logger.fatal("Exiting due to IO exception: ", ex);
        } catch (SQLException ex) {
            logger.fatal("SQL Exception: ", ex );
        }
        
        System.exit( 1 );
    }
}
