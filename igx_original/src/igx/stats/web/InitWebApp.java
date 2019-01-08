package igx.stats.web;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.*;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.Configuration;
import org.apache.commons.logging.*;

import igx.stats.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class InitWebApp implements ServletContextListener {

    /**
     * Create the Hibernate SessionFactory and place it in the servlet context.
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        SessionFactory sessions = null;
        Configuration cfg = null;
        try {
            // The link to the database
            cfg = new Configuration()
                .addClass(Attack.class)
                .addClass(Event.class)
                .addClass(Game.class)
                .addClass(PlanetUpdate.class)
                .addClass(Player.class)
                .addClass(TimeState.class);

            // The source of persistence sessions
            sessions = cfg.buildSessionFactory();

            // Configure the source of persistence sessions
            sessions = cfg.buildSessionFactory();
            SessionTable.initSessionTable(sessions);
            context.setAttribute("sessions", sessions);
        } catch (Exception e) {
            System.out.println("Could not initialize webapp:");
            e.printStackTrace();
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // To Do: Roll back any lingering sessions (probably not necessary).
    }
}