package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.hibernate.ReviewerMgr;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.models.Reviewer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        HashMap reviewersList = reviewerMgr.mapByEmployeeId();
        assert reviewersList.size() == 2 : "Invalid list of reviewers";
        assert reviewersList.containsKey(787812) : "Missing reviewer from list";
        assert reviewersList.containsKey(8712359) : "Missing reviewer from list";
    }

    public void shouldListSortedReviewersByBusinessCenterAndThenByLastName() throws Exception {
        ArrayList<Reviewer> reviewers = (ArrayList<Reviewer>) reviewerMgr.list();
        assert reviewers.get(0).getBusinessCenterName().equals("AABC") : "Incorrect sorting by BC";
        assert reviewers.get(0).getEmployee().getId() == 8712359 : "Incorrect sorting by name";

        assert reviewers.get(1).getBusinessCenterName().equals("AABC") : "Incorrect sorting by BC";
        assert reviewers.get(1).getEmployee().getId() == 990871 : "Incorrect sorting by name";

        assert reviewers.get(2).getBusinessCenterName().equals("UABC") : "Incorrect sorting by BC";
        assert reviewers.get(2).getEmployee().getId() == 787812 : "Incorrect sorting by name";

    }

    public void shouldDeleteReviewer() throws Exception {
        ArrayList<Reviewer> oldReviewersList = (ArrayList<Reviewer>) reviewerMgr.list();
        reviewerMgr.delete(1);
        ArrayList<Reviewer> newReviewerList = (ArrayList<Reviewer>) reviewerMgr.list();

        assert oldReviewersList.size() == newReviewerList.size() + 1;
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddReviewerWhoIsNotActive() throws Exception {
        reviewerMgr.add("testing", "UABC");
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddReviewerWhoIsAlreadyReviewerForThatBusinessCenter() throws Exception {
        reviewerMgr.add("barlowc3", "AABC");
    }

    public void shouldAddReviewerWhoIsAReviewerForDifferentBusinessCenter() throws Exception {
        ArrayList<Reviewer> oldReviewersList = (ArrayList<Reviewer>) reviewerMgr.list();
        reviewerMgr.add("barlowc3", "UABC");
        ArrayList<Reviewer> newReviewerList = (ArrayList<Reviewer>) reviewerMgr.list();
        assert oldReviewersList.size() == newReviewerList.size() - 1;
    }

    public void shouldAddReviewer() throws Exception {
        ArrayList<Reviewer> oldReviewersList = (ArrayList<Reviewer>) reviewerMgr.list();
        reviewerMgr.add("cedenoj", "UABC");
        ArrayList<Reviewer> newReviewerList = (ArrayList<Reviewer>) reviewerMgr.list();
        assert oldReviewersList.size() == newReviewerList.size() - 1;
    }

    public void shouldListReviewersByBC() throws Exception {
        List<Reviewer> results = ReviewerMgr.getReviewers("UABC");

        assert results.size() == 1 : "Invalid count of reviewers in UABC";
        assert results.get(0).getEmployee().getId() == 787812 : "Invalid employee in reviewers list";

        results = ReviewerMgr.getReviewers("AABC");

        assert results.size() == 2 : "Invalid count of reviewers in AABC";
        assert results.get(0).getEmployee().getId() == 8712359 : "Invalid employee in reviewers list";
        assert results.get(1).getEmployee().getId() == 990871 : "Invalid employee in reviewers list";

    }

}