package edu.osu.cws.pass.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    /**
     * Static variable used to keep track of what hibernate config file the java
     * class should be using.
     */
    private static short environment = 0;

    /**
     * Constant used to define the environment variable. The development constant
     * tells this class to use hibernate-dev.cfg.xml as the configuration file.
     */
    public static final short DEVELOPMENT = 0;

     /**
     * Constant used to define the environment variable. The development constant
     * tells this class to use hibernate-test.cfg.xml as the configuration file.
     */
    public static final short TESTING = 1;

    /**
     * @todo: We need to figure out how we are going to deal with the hibernate configuration
     * in production environment.
     */
    public static final short PRODUCTION = 2;


    /**
     * Method used to create the Hibernate session. This method is private to ensure
     * it is only called once during the initialization of the sessionFactory property.
     *
     * @return          Hibenate's session
     */
    private static SessionFactory buildSessionFactory() {
        String configUsed;
        String developmentConfig = "hibernate-dev.cfg.xml";
        String testConfig = "hibernate-test.cfg.xml";

        // Determine which config environment we are in and use the respective config file
        switch (environment) {
            case TESTING:
                configUsed = testConfig;
                break;
            case DEVELOPMENT:
            default:
                configUsed = developmentConfig;
                break;
        }

        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration().configure(configUsed).buildSessionFactory();

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
     * Use this method to set the environment which determines what hibernate config file
     * to use. Possible variables are: HibernateUtil.DEVELOPMENT, HibernateUtil.TESTING and
     * HibernateUtil.PRODUCTION.
     *
     * @param env   The environment to use
     */
    public static void setEnvironment(short env) {
        environment = env;
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