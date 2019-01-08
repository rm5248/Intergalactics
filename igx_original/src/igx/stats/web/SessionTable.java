package igx.stats.web;

import java.util.*;
import net.sf.hibernate.*;
import java.sql.*;

/**
 * Maintains a map from thread to Hibernate session so that a session can easily be accessed.
 */
public class SessionTable {

    private Map table;

    private Map transactions;

    private SessionFactory factory;

    private static SessionTable singleton;

    /**
     * Creates a new session table. Requires a pre-initialized session factory.
     * @param sessionFactory the preinitialized SessionFactory
     */
    public SessionTable(SessionFactory sessionFactory) {
        table = new HashMap();
        transactions = new HashMap();
        factory = sessionFactory;
    }

    public static void initSessionTable(SessionFactory factory) {
        singleton = new SessionTable(factory);
    }

    public static SessionTable getSessionTable() {
        return singleton;
    }

    /**
     * Gets the session for the currently executing thread, or creates one if necessary.
     * @return a Session, possibly a newly opened one.
     */
    public Session getSession() throws SQLException, HibernateException {
        Thread current = Thread.currentThread();
        Session s = (Session)table.get(current);
        if (s == null) {
            // Create new session
            s = factory.openSession();
            table.put(current, s);
        }
        return s;
    }

    public Transaction getTransaction() throws HibernateException {
        Thread current = Thread.currentThread();
        Session s = (Session)table.get(current);
        if (s != null) {
            Transaction t = (Transaction)transactions.get(s);
            if (t == null) {
                t = s.beginTransaction();
                transactions.put(s, t);
            }
            return t;
        }
        return null;
    }

    /**
     * Checks to see if this thread has a session, useful if the caller isn't sure whether a flush is required.
     * @return true if the thread has a session, false otherwise.
     */
    public boolean hasSession() {
        Thread current = Thread.currentThread();
        return table.containsKey(current);
    }

    public boolean hasTransaction() {
        Thread current = Thread.currentThread();
        Object s = table.get(current);
        return transactions.containsKey(s);
    }

    public void clearTransaction() {
        Thread current = Thread.currentThread();
        Object s = table.get(current);
        if (s != null) {
            transactions.remove(s);
        }
    }

    /**
     * Clears out the session for a given thread. The assumption is that session has already been closed.
     */
    public void clearSession() {
        Thread current = Thread.currentThread();
        Object s = table.get(current);
        if (s != null) {
            transactions.remove(s);
        }
        table.remove(current);
    }
}