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

        int previousSequence = 0;
        for (ReviewCycleOption option : optionList) {
            assert previousSequence <= option.getSequence() : "Sequence should be in ascending order";
            previousSequence = option.getSequence();
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

    public void shouldEditOptionWithFormValues() throws Exception {
        int id = 1;
        int pidm = 12345;
        int sequence = 88;
        int value = 8;
        String name = "Updated Category";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ReviewCycleOptionMgr.add(name, value, sequence, new Employee(pidm), id);
        ReviewCycleOption option = ReviewCycleOptionMgr.get(name);
        tx.commit();

        assert option.getCreator().getId() == pidm;
        assert option.getSequence() == sequence;
        assert option.getValue() == value;
        assert option.getId() == id;
    }

    public void shouldAddOptionWithFormValues() throws Exception {
        int pidm = 12345;
        int sequence = 88;
        int value = 8;
        String name = "Testing Add";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ReviewCycleOptionMgr.add(name, value, sequence, new Employee(pidm), null);
        ReviewCycleOption option = ReviewCycleOptionMgr.get(name);
        tx.commit();

        assert option.getCreator().getId() == pidm;
        assert option.getSequence() == sequence;
        assert option.getValue() == value;
        assert option.getId() != null;
    }

    public void shouldUndoDeleteWhenAddingExistingOption() throws Exception {
        int pidm = 12345;
        int sequence = 88;
        int value = 8;
        String name = "Deleted option";

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ReviewCycleOptionMgr.add(name, value, sequence, new Employee(pidm), null);
        ReviewCycleOption option = ReviewCycleOptionMgr.get(name);
        tx.commit();

        assert option.getCreator().getId() == pidm;
        assert option.getSequence() == sequence;
        assert option.getValue() == value;
        assert option.getId() == 3 : "It should use id of previous deleted option";
        assert option.getDeleteDate() == null : "It should clear out deleted values";
        assert option.getDeleter() == null : "It should clear out deleted values";
    }

}
