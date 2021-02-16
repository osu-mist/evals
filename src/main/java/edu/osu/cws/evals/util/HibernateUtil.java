package edu.osu.cws.evals.util;

import edu.osu.cws.evals.portlet.Constants;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    /**
     * Config value - name of hibernate test xml config file.
     */

    public static final String TEST_CONFIG = "hibernate-test.cfg.xml";

    /**
     * Config value - path to hibernate xml config files from the root of the
     * portlet. This value is used by DBUnit.java
     */
    public static final String CONFIG_PATH = "docroot/WEB-INF/src/";

    private static Configuration hibernateConfig = null;

    /**
     * Method used to set the Hibernate configuration.
     *
     * @param   configFileName
     * @param   hbmPathPrefix
     * @param   extraConfigFilePath
     */
    public static void setHibernateConfig(String configFileName, String hbmPathPrefix, String extraConfigFilePath)
    {
        String hbmDir = hbmPathPrefix + Constants.getRootDir() + "edu/osu/cws/evals/hbm";
        Properties extraProperties = getExtraProperties(extraConfigFilePath);
        // configFileName = "/opt/liferay/evals/hibernate.cfg.xml";
        System.out.println("----------------------------");
        System.out.println(hbmDir);
        System.out.println(configFileName);
        System.out.println("----------------------------");
        File configFile = new File("/opt/liferay/evals/hibernate.cfg.xml");
        hibernateConfig = new Configuration().configure(configFile);
        // hibernateConfig = new Configuration().configure(configFileName);
        hibernateConfig.addDirectory(new File("/opt/liferay/evals/hbm"));
        // hibernateConfig.addDirectory(new File(hbmDir));
        hibernateConfig.addProperties(extraProperties);
    }

    /**
     * Method used to create the Hibernate session. This method is private to ensure
     * it is only called once during the initialization of the sessionFactory property.
     *
     * @return  Hibernate's session
     */
    private static SessionFactory buildSessionFactory() {
        try {
            if (hibernateConfig == null) {
                System.out.println("HIBERNATE CONFIG IS NULL");
                //this should not be as caller is supposed to call setHibernateConfig first
                return null;
            }
            return hibernateConfig.buildSessionFactory();

        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Method used to get extra properties from a file.
     *
     * @param   filename
     */
    private static Properties getExtraProperties(String filename)
    {
        Properties prop = new Properties();
        try {
            FileInputStream in = new FileInputStream(filename);
            prop.load(in);
            in.close();
        } catch(IOException e) {
            System.err.println("File not found.");
        }
        return prop;
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
     * This method returns the current Hibernate Session. It relies on the
     * private getSessionFactory method.
     *
     * @return
     */
    public static Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();

    }
    

}
