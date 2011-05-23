package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.util.AppraisalSteps;
import edu.osu.cws.pass.util.HibernateUtil;
import edu.osu.cws.pass.util.PermissionRules;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
    @BeforeMethod
    public void setUp() throws Exception {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    @Test(groups = {"unittest"})
    public void shouldListAllSteps() {
        HashMap steps = appraisalSteps.list();
        //@todo: does the test below make sense without the original status in the appraisal_step
        // table?
        assert steps.containsKey("submit-classified") : "Missing step in hashmap";
        assert steps.containsKey("require-modification-classified") : "Missing step in hashmap";
        assert steps.size() == 2 :
                "AppraisalSteps.list() should find all appraisalSteps";

    }
}
