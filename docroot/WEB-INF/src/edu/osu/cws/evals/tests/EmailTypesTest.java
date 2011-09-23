package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.EmailTypeMgr;
import edu.osu.cws.evals.models.EmailType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class EmailTypesTest {
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

    public void shouldReturnAMapWithTypeAsTheKey() throws Exception {
        HashMap<String, EmailType> results = (HashMap<String, EmailType>) EmailTypeMgr.getMap();
        assert results.containsKey("goals-submitted");
        assert results.containsKey("goals-require-modification");
    }
}
