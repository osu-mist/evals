package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.ConfigurationMgr2;
import edu.osu.cws.pass.models.Configuration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

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
        int i = 1;
    }

    @Test
    public void shouldSortConfigurationsBySectionThenBySequence() throws Exception {
        ConfigurationMgr2 configurationMgr = new ConfigurationMgr2();
        ArrayList<Configuration> configs = (ArrayList<Configuration>) configurationMgr.list();

        assert configs.get(0).getId() == 1;
        assert configs.get(1).getId() == 2;
        assert configs.get(2).getId() == 4;
        assert configs.get(3).getId() == 3;
        assert configs.get(4).getId() == 7;
        assert configs.get(5).getId() == 6;
        assert configs.get(6).getId() == 5;
    }
}
