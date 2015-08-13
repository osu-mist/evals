package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.Mailer;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Test
public class EmailsTest {

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

    public void shouldAddOneEmailToDB() throws Exception {
        //@todo: test add

        Email email = new Email();
        email.setAppraisalId(99);
        email.setEmailType("goals-submitted");
        email.setSentDate(new Date());

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        // Verify that record doesn't exist yet.
        String verifyQuery = "from edu.osu.cws.evals.models.Email where appraisalId = ? and " +
                "emailType = ?";
        boolean recordExists = session.createQuery(verifyQuery)
                .setInteger(0, 99)
                .setString(1, "goals-submitted")
                .list().size() == 1;
        assert !recordExists : "Email record shouldn't exist";

        EmailMgr.add(email);

        // Verify that record was saved
        recordExists = session.createQuery(verifyQuery)
                .setInteger(0, 99)
                .setString(1, "goals-submitted")
                .list().size() == 1;
        assert recordExists : "Email record wasn't saved";

        tx.commit();
    }

    public void shouldAddAnArrayOfEmails()  throws Exception{
        //@todo: test add;
        List<Email> emails = new ArrayList<Email>();
        Email email = new Email();
        email.setAppraisalId(1);
        email.setEmailType("goals-submitted");
        email.setSentDate(new Date());
        emails.add(email);

        email = new Email();
        email.setAppraisalId(2);
        email.setEmailType("goals-required-modification");
        email.setSentDate(new Date());
        emails.add(email);

        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        String verifyQuery = "from edu.osu.cws.evals.models.Email";
        Integer initialCount = session.createQuery(verifyQuery).list().size();

        EmailMgr.add(emails);
        session.flush();

        Integer afterAddCount = session.createQuery(verifyQuery).list().size();
        assert (afterAddCount - initialCount) == emails.size() : "emails were not added";

        tx.commit();
    }

    public void shouldReturnFirstEmail() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Email email = EmailMgr.getFirstEmail(1, "goals-submitted");
        tx.commit();
        assert email.getId() == 1 : "The first sent email should have been returned";
    }

    public void shouldReturnNullIfThereIsNoFirstEmail() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Email email = EmailMgr.getFirstEmail(1, "goals-due");
        tx.commit();
        assert email == null : "The return value should be null because there is no row in the db";
    }

    public void shouldReturnLastEmail() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Email email = EmailMgr.getLastEmail(1, "goals-submitted");
        tx.commit();
        assert email.getId() == 3 : "The first sent email should have been returned";
    }

    public void shouldReturnNullIfThereIsNoLastEmail() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Email email = EmailMgr.getLastEmail(1, "goals-due");
        tx.commit();
        assert email == null : "The return value should be null because there is no row in the db";

    }

    public void shouldCalculateDaysRemaining() throws Exception {
        Appraisal appraisal = new Appraisal();
        int offset = 60;
        DateTime endDate = new DateTime().plusDays(offset).withTimeAtStartOfDay();
        appraisal.setEndDate(endDate.toDate());
        assert Mailer.getDaysRemaining(appraisal, "end") == offset;

        assert Mailer.getDaysRemaining(appraisal, "foo") == -1 : "-1 should be returned for invalid reference";
        assert Mailer.getDaysRemaining(appraisal, "bar") == -1 : "-1 should be returned for invalid reference";
    }
}