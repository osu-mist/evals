package edu.osu.cws.evals.tests;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import edu.osu.cws.evals.backend.BackendModule;
import edu.osu.cws.evals.models.AppraisalStep;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.LoggingInterface;
import edu.osu.cws.evals.util.MailerInterface;

import java.util.Map;

public class MockBackendModule extends AbstractModule{
    @Override
    protected void configure() {
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
    }

    @Provides @Singleton
    MailerInterface provideMailer() {
        MailerInterface mailer = null;
        try {
            mailer = new MockMailer();
        } catch (Exception e) {
            // do something
        }

        return mailer;
    }

    @Provides @Singleton
    Map<String, Configuration> provideMapStringConfiguration() {
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
        return new BackendModule().provideMapStringConfiguration();
    }

    @Provides @Singleton
    Map<String, EmailType> provideMailStringEmailType() {
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
        return new BackendModule().provideMailStringEmailType();
    }

    @Provides @Singleton
    Map<String, AppraisalStep> provideMailStringAppraisalStep() {
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
        return new BackendModule().provideMapStringAppraisalStep();
    }

    @Provides @Singleton
    LoggingInterface provideLoggingInterface() {
        return (LoggingInterface) new MockLogger();
    }
}
