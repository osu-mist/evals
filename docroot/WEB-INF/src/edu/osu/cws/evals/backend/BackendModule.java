package edu.osu.cws.evals.backend;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.AppraisalStepMgr;
import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.hibernate.EmailTypeMgr;
import edu.osu.cws.evals.models.AppraisalStep;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.util.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Map;

public class BackendModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LoggingInterface.class).to(EvalsLogger.class);
    }

    /**
     * The various provider methods below are automatically called by google guice when it needs
     * to create a new instance of the BackendMgr class. The singleton annotation makes it so that
     * there is only one instance of the object being returned even if the provider method is called
     * more than once.
     */

    @Provides @Singleton
    MailerInterface provideMailer() {
        MailerInterface mailer = null;
        try {
            mailer = EvalsUtil.createMailer(getConfig(),
                                            provideMapStringConfiguration(),
                                            EvalsUtil.createLogger(getConfig()));
        } catch (Exception e) {
            // do something
        }

        return mailer;
    }

    @Provides @Singleton
    public Map<String, Configuration> provideMapStringConfiguration() {
        PropertiesConfiguration config = getConfig();
        String hibernateConfig = config.getString("hibernate-cfg-file");
        HibernateUtil.setHibernateConfig(hibernateConfig, "",
                config.getString("extra-properties-path") +
                config.getString("extra-properties-file"));
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Map<String, Configuration> configMap = null;

        try {
            configMap = ConfigurationMgr.mapByName();
        } catch (Exception e) {
            // do something
        }
        tx.commit();
        return configMap;
    }

    @Provides @Singleton
    public Map<String, EmailType> provideMailStringEmailType() {
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

    private PropertiesConfiguration getConfig() {
        PropertiesConfiguration config = null;
        try {
            config = EvalsUtil.loadEvalsConfig(null);
        } catch (Exception e) {
            // need to log exception
        }
        return config;
    }
}
