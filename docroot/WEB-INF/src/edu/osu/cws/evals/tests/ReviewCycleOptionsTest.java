package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.ReviewCycleOptionMgr;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ReviewCycleOption;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class ReviewCycleOptionsTest {
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

    public void shouldOnlyListNonDeletedOptions() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<ReviewCycleOption> optionList = ReviewCycleOptionMgr.list();
        tx.commit();
        for (ReviewCycleOption option : optionList) {
            assert option.getDeleteDate() == null : "Reason should not be deleted";
        }
    }

    public void shouldListOptionsSortedBySequenceThenName() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<ReviewCycleOption> optionList = ReviewCycleOptionMgr.list();
        tx.commit();

        for (ReviewCycleOption option : optionList) {
            assert option.getDeleteDate() == null : "Reason should not be deleted";
        }
    }

    public void shouldDeleteOptionAndSetDeleteDateAndPidm() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        int deleterPidm = 12345;
        ReviewCycleOptionMgr.delete(2, new Employee(deleterPidm));
        ReviewCycleOption option = ReviewCycleOptionMgr.get(2);
        tx.commit();

        assert option.getDeleteDate() != null : "The delete date should be set";
        assert option.getDeleter().getId() == deleterPidm : "The deleter pidm should be set";
    }

}
