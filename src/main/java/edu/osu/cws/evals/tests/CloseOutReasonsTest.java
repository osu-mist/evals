package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.CloseOutReasonMgr;
import edu.osu.cws.evals.models.CloseOutReason;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Test
public class CloseOutReasonsTest {

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

    public void shouldListNotDeletedCloseOutReasons() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(false);
        tx.commit();
        for (CloseOutReason reason : reasonList) {
            assert reason.getDeleteDate() == null : "Reason should not be deleted";
        }
    }

    public void shouldListDeletedCloseOutReasons() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(true);
        tx.commit();
        int deletedReasons = 0;
        for (CloseOutReason reason : reasonList) {
            if (reason.getDeleteDate() != null) {
                deletedReasons++;
            }
        }
        assert deletedReasons > 0 : "Deleted reasons should be included";
    }

    public void shouldListReasonsSortedAlphabetically() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(false);
        tx.commit();
        assert reasonList.get(0).getId() == 4 : "List not sorted alphabetically. First element should be id 4";
        assert reasonList.get(1).getId() == 1 : "List not sorted alphabetically. First element should be id 1";
        assert reasonList.get(2).getId() == 2 : "List not sorted alphabetically. First element should be id 2";
    }

    public void shouldDeleteAReason() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        CloseOutReason reason = (CloseOutReason) session.load(CloseOutReason.class, 1);
        assert reason.getDeleteDate() == null : "Starting reason should not be deleted";
        assert CloseOutReasonMgr.delete(1) : "One row should have been updated";
        tx.commit();

        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        reason = (CloseOutReason) session.load(CloseOutReason.class, 1);
        assert reason.getDeleteDate() != null : "Reason should have the deleted date set";
        tx.commit();
    }

    public void shouldAddNewReason() throws Exception {
        Employee loggedInUser = new Employee(12345);
        String reasonText = "won lottery and quit!";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        String verifyQuery = "from edu.osu.cws.evals.models.CloseOutReason where reason = ?";
        CloseOutReason reason = (CloseOutReason) session.createQuery(verifyQuery)
                .setString(0, reasonText)
                .uniqueResult();
        assert reason == null : "This reason shouldn't exist in the db before adding it";

        CloseOutReasonMgr.add(reasonText, loggedInUser);
        session.flush();
        reason = (CloseOutReason) session.createQuery(verifyQuery)
                .setString(0, reasonText)
                .uniqueResult();
        assert reason != null : "This reason should exist after adding it";
        tx.commit();
    }

    public void shouldUndeleteWhenAddingDeletedReason() throws Exception {
        Employee loggedInUser = new Employee(12345);
        String reasonText = "Lateral move does not require eval";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        String verifyQuery = "from edu.osu.cws.evals.models.CloseOutReason where reason = ?";
        int reasonCount = session.createQuery(verifyQuery)
                .setString(0, reasonText)
                .list().size();
        assert reasonCount == 1 : "This reason should already exist";

        CloseOutReasonMgr.add(reasonText, loggedInUser);
        session.flush();
        reasonCount = session.createQuery(verifyQuery)
                .setString(0, reasonText)
                .list().size();
        assert reasonCount == 1 : "No new reason should have been added";
        CloseOutReason reason = (CloseOutReason) session.load(CloseOutReason.class, 3);
        assert reason.getDeleteDate() == null : "Reason should have been un-deleted";
        tx.commit();
    }
}
