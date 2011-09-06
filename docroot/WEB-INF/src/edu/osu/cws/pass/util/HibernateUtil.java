package edu.osu.cws.pass.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.portlet.*;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    /**
     * Config value - name of hibernate test xml config file.
     */

    public static final String TEST_CONFIG = "hibernate-test.cfg.xml";
    public static final String DEV_CONFIG = "hibernate-luminis-dev.cfg.xml";

    /**
     * Config value - path to hibernate xml config files from the root of the
     * portlet. This value is used by DBUnit.java
     */
    public static final String CONFIG_PATH = "docroot/WEB-INF/src/";


    /**
     * Static variable used to keep track of what hibernate config file the java
     * class should be using. By default it uses the test configuration db.
     */
    private static String config = DEV_CONFIG;

    /**
     * Method used to create the Hibernate session. This method is private to ensure
     * it is only called once during the initialization of the sessionFactory property.
     *
     * @return          Hibenate's session
     */
    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure(config).buildSessionFactory();

        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * This is a getter method that returns the hibernate session used by the application.
     *
     * @return  sessionFactory
     */
    private static SessionFactory getSessionFactory() {
        try {
            if (sessionFactory == null) {
                sessionFactory = buildSessionFactory();
            }
            return sessionFactory;
        }
        catch (Throwable e) {
            System.err.println("Initial getSessionFactory failed." + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Use this method to set the name of the hibernate configuration file to load. This
     * method is called by PASSPortlet.portletSetup.
     *
     * @param configName
     */
    public static void setConfig(String configName) {
        config = configName;
    }

    /**
     * This method returns the current Hibernate Session. It relies on the
     * private getSessionFactory method.
     *
     * @return
     */
    public static Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();

    }
    

}