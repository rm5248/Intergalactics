package igx.client;

// igx.java 
import igx.shared.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class I extends JFrame implements FrontEnd {

    Dimension size = null;
    static Dimension customSize = null;
    boolean soundOnOff = false;
    Server server;
    SoundManager player;
    String host;
    AuPlayer au;
    private JComponent container;

    public I(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        au = new AuPlayer();
        player = new SoundManager(this);
        container = new JPanel();
        setLayout(new BorderLayout());
        add(container);
        container.setSize(1440, 880);
    }

    public Container getContainer() {
        return container;
    }

    public Dimension getDimensions() {
        return size;
    }

    public boolean getSoundMode() {
        return soundOnOff;
    }

    public static void main(String[] args) {
        showIntro();
        if ((args.length != 1) && (args.length != 4)) {
            showUsage();
            System.exit(1);
        }
        int x = -1, y = -1;
        String host = null;
        if (args.length == 4) {
            try {
                if (args[0].equals("-s")) {
                    x = Integer.parseInt(args[1]);
                    y = Integer.parseInt(args[2]);
                    host = args[3];
                } else if (args[1].equals("-s")) {
                    x = Integer.parseInt(args[2]);
                    y = Integer.parseInt(args[3]);
                    host = args[0];
                } else {
                    showUsage();
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                showUsage();
                System.exit(1);
            }
            if (x != -1) {
                customSize = new Dimension(x, y);
            }
        } else {
            host = args[0];
        }
        I frame = new I(Params.NAME + " - " + host);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.black);
        frame.preGame(host, true);
        frame.host = host;
    }

    public void play(String sound) {
        if (soundOnOff) {
            player.play(sound);
        }
    }

    public void playSound(String sound) {
        au.play(sound);
    }

    public void preGame(String host, boolean firstTime) {
        InetAddress location = null;
        try {
            location = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host.");
            System.exit(1);
        }
        try {
            server = new Server(new Socket(location, Params.PORTNUM));
        } catch (IOException e) {
            System.out.println("Unknown host.");
            System.exit(1);
        }
        if (!server.isConnected()) {
            throw new NullPointerException("Socket unexpectedly closed!");
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        if (customSize != null) {
            screenSize = customSize;
        }
        setSize(screenSize);
        setVisible(true);
        size = new Dimension(screenSize.width, screenSize.height);
        Insets i = getInsets();
        size.width = size.width - i.left - i.right;
        size.height = size.height - i.top - i.bottom - 20;
        setSize(size);
        container.setSize(size);
        ClientForum cf = new ClientForum(this, Params.SERVER_NAME, toolkit, server);
        server.setForum(cf);
        Image icon = toolkit.getImage("hive.gif");
        setIconImage(icon);
        //pack();
        cf.setToPreferredSize();
        validate();
        server.start();
    }

    public void quitProgram() {
        System.exit(0);
    }

    public void setSoundMode(boolean mode) {
        soundOnOff = mode;
    }

    public static void showIntro() {
        System.out.println(Params.NAME + " v" + Params.VERSION);
        System.out.println("Copyright 1999 HiVE Software");
    }

    public static void showUsage() {
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("  intergalactics [ie] <server>");
        System.out.println("");
        System.out.println("See manual.html for instructions.");
    }

    /**
     * This method was created in VisualAge.
     *
     * @param serverVersion java.lang.String
     */
    public void versionProblem(String serverVersion) {
        System.out.println("This version is out of date! The server is using version " + serverVersion + ".");
        System.out.println("Please get an updated client from the " + Params.NAME + " homepage.");
        System.exit(0);
    }
}
