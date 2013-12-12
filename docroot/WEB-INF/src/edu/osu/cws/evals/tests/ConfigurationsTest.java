package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Test
public class ConfigurationsTest {

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldSortConfigurationsBySectionThenBySequence() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ArrayList<Configuration> configs = (ArrayList<Configuration>) ConfigurationMgr.list();
        tx.commit();

        assert configs.get(0).getId() == 1;
        assert configs.get(1).getId() == 2;
        assert configs.get(2).getId() == 4;
        assert configs.get(3).getId() == 3;
        assert configs.get(5).getId() == 9;
        assert configs.get(6).getId() == 10;
        assert configs.get(7).getId() == 11;
    }

    public void shouldProvideMapUsingNameAsKey() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        HashMap<String, Configuration> configs = (HashMap<String, Configuration>) ConfigurationMgr.mapByName();
        tx.commit();

        assert configs.containsKey("configurations-goal-max-char");
        assert configs.containsKey("configurations-result-max-char");
        assert configs.containsKey("configurations-goal-min-char");
        assert configs.containsKey("configurations-result-min-char");
        assert configs.containsKey("configurations-frequency-terminate-emails");
        assert configs.containsKey("configurations-employee-result-notifications");
        assert configs.containsKey("configurations-review-due-notifications");
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldThrowErrorWhenConfigurationIsNotFound() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ConfigurationMgr.edit(50, "should not save because 10 is not a valid id");
        tx.commit();
    }

    public void shouldSaveConfigurationOnEdit() throws Exception {
        String newConfigValue = "should save value to db";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        assert ConfigurationMgr.edit(1, newConfigValue);
        Configuration config = (Configuration) session.get(Configuration.class, 1);
        assert config.getValue().equals(newConfigValue);
        tx.commit();
    }

    public void shouldReturnContextLastUpdate() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        assert ConfigurationMgr.getContextLastUpdate() != null;
        tx.commit();
    }

    public void shouldUpdateContextTimestamp() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ConfigurationMgr.updateContextTimestamp();
        tx.commit();
    }
}
