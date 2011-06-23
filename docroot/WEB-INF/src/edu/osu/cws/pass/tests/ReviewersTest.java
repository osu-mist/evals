package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.ReviewerMgr;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class ReviewersTest {
    ReviewerMgr reviewerMgr = new ReviewerMgr();

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
    public void shouldListReviewers() throws Exception {
        HashMap reviewersList = reviewerMgr.list();
        assert reviewersList.size() == 2 : "Invalid list of reviewers";
        assert reviewersList.containsKey(787812) : "Missing reviewer from list";
        assert reviewersList.containsKey(8712359) : "Missing reviewer from list";

    }
}
