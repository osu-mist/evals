package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.util.HibernateUtil;
import edu.osu.cws.pass.util.Reviewers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class ReviewersTest {
    Reviewers reviewers = new Reviewers();

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
    public void shouldListReviewers() {
        HashMap reviewersList = reviewers.list();
        assert reviewersList.size() == 2 : "Invalid list of reviewers";
        assert reviewersList.containsKey(12345) : "Missing reviewer from list";
        assert reviewersList.containsKey(12467) : "Missing reviewer from list";

    }
}