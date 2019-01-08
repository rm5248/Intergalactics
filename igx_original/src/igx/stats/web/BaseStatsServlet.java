package igx.stats.web;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Session;

/**
 * <p>Title: IGX</p>
 * <p>Description: Basic stats servlet to provide services to other extended stats servlets</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Matt Hall
 * @version 1.0
 */

public class BaseStatsServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html";
    private SessionFactory factory = null;

    //Initialize global variables
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        // The hib session factory gets put in there by InitWebApp
        factory = (SessionFactory) context.getAttribute("sessions");
    }

    protected Session getSession() {
        try {
            return factory.openSession();
        } catch (Exception e) {
            System.out.println("Base servlet couldn't provide hib session: ");
            e.printStackTrace();
        }
        return null;
    }
}