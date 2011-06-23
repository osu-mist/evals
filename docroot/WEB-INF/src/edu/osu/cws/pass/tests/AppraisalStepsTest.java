package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AppraisalStepMgr;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

public class AppraisalStepsTest {
    AppraisalStepMgr appraisalStepMgr = new AppraisalStepMgr();

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

    @Test(groups = {"unittest"})
    public void shouldListAllSteps() throws Exception {
        HashMap steps = appraisalStepMgr.list();
        //@todo: does the test below make sense without the original status in the appraisal_step
        // table?
        assert steps.containsKey("submit-Classified") : "Missing step in hashmap";
        assert steps.containsKey("require-modification-Classified") : "Missing step in hashmap";
        assert steps.size() == 2 :
                "AppraisalStepMgr.list() should find all appraisalSteps";

    }
}
