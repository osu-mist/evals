package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AppraisalStepMgr;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

public class AppraisalStepsTest {

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
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        HashMap steps = AppraisalStepMgr.list();
        tx.commit();
        //@todo: does the test below make sense without the original status in the appraisal_step
        // table?
        assert steps.containsKey("submit-Classified") : "Missing step in hashmap";
        assert steps.containsKey("require-modification-Classified") : "Missing step in hashmap";
        assert steps.size() == 2 :
                "AppraisalStepMgr.list() should find all appraisalSteps";
    }
}
