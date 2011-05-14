package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.util.AppraisalSteps;
import edu.osu.cws.pass.util.HibernateUtil;
import edu.osu.cws.pass.util.PermissionRules;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

public class AppraisalStepsTest {
    AppraisalSteps appraisalSteps = new AppraisalSteps();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeClass
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        try {
            dbunit.seedDatabase();
        } catch (Exception e) {}
    }

    @Test(groups = {"unittest"})
    public void shouldListAllSteps() {
        HashMap steps = appraisalSteps.list();
        assert steps.containsKey("submit-Classified-goals-due") : "Missing step in hashmap";
        assert steps.containsKey("require-modification-Classified-goals-submitted") : "Missing step in hashmap";
        assert steps.size() == 2 :
                "AppraisalSteps.list() should find all appraisalSteps";

    }
}
