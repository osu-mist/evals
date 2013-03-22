package edu.osu.cws.evals.backend;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.hibernate.EmailTypeMgr;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.util.EvalsLogger;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.Mailer;
import edu.osu.cws.util.Mail;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.util.Map;
import java.util.ResourceBundle
import edu.osu.cws.evals.portlet.Constants;

public class BackendModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    /**
     * The various provider methods below are automatically called by google guice when it needs
     * to create a new instance of the BackendMgr class. The singleton annotation makes it so that
     * there is only one instance of the object being returned even if the provider method is called
     * more than once.
     */

    @Provides
    EvalsLogger provideEvalsLogger() {
        CompositeConfiguration config = provideCompositeConfiguration();
        String serverName = config.getString("log.serverName");
        String environment = config.getString("log.environment");

        return new EvalsLogger(serverName, environment);
    }

    @Provides @Singleton
    Mailer provideMailer() {
        Mailer mailer = null;
        try {
            CompositeConfiguration config = provideCompositeConfiguration();
            String mailHost = config.getString("mail.hostname");
            System.out.println("mailHost: " + mailHost);
            String mailFrom = config.getString("mail.fromAddress");
            String replyTo = config.getString("mail.replyToAddress");
            String linkUrl = config.getString("mail.linkUrl");
            String helpLinkUrl = config.getString("helpfulLinks.url");

            ResourceBundle emailBundle = ResourceBundle.getBundle(BackendMgr.EMAIL_BUNDLE_FILE);
            EvalsLogger logger = provideEvalsLogger();
            Map<String, Configuration> configMap = provideMapStringConfiguration();

            mailer = new Mailer(emailBundle, mailHost, mailFrom, linkUrl, helpLinkUrl, configMap,
                    logger, replyTo);
        } catch (Exception e) {
            // do something
        }

        return mailer;
    }

    @Provides @Singleton
    CompositeConfiguration provideCompositeConfiguration() {
        CompositeConfiguration config = new CompositeConfiguration();
        try {
            String specificPropFile = EvalsUtil.getSpecificConfigFile("backend", "");
            config.addConfiguration(new PropertiesConfiguration(specificPropFile));
            config.addConfiguration(new PropertiesConfiguration(Constants.DEFAULT_PROPERTIES_FILE));
        } catch (Exception e) {
            // need to log exception
        }
        return  config;
    }

    @Provides @Singleton
    Map<String, Configuration> provideMapStringConfiguration() {
        CompositeConfiguration config = provideCompositeConfiguration();
        HibernateUtil.setConfig(config.getString("hibernate-cfg-file"));
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Map<String, Configuration> configMap = null;
        ConfigurationMgr confMgr = new ConfigurationMgr();

        try {
            configMap = confMgr.mapByName();
        } catch (Exception e) {
            // do something
        }
        tx.commit();
        return configMap;
    }

    @Provides @Singleton
    Map<String, EmailType> provideMailStringEmailType() {
        Map<String, EmailType> emailTypeMap = null;
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        try {
            emailTypeMap = EmailTypeMgr.getMap();
        } catch (Exception e) {
            // do something
        }
        tx.commit();
        return emailTypeMap;
    }
}