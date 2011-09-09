package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AdminMgr;
import edu.osu.cws.pass.hibernate.EmailMgr;
import edu.osu.cws.pass.models.Email;
import edu.osu.cws.pass.models.EmailType;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
        email.setAppraisalId(1);
        email.setEmailType("goals-submitted");
        email.setSentDate(new Date());

        EmailMgr.add(email);
    }

    public void shouldAddAnArrayOfEmails()  throws Exception{
        //@todo: test add;
        List<Email> emails = new ArrayList<Email>();
        Email email = new Email();
        email.setAppraisalId(1);
        email.setEmailType("goals-submitted");
        email.setSentDate(new Date());
        emails.add(email);

        email.setAppraisalId(2);
        email.setEmailType("goals-required-modification");
        emails.add(email);

        EmailMgr.add(emails);
    }

    public void shouldReturnFirstEmail() throws Exception {
        Email email = EmailMgr.getFirstEmail(1, "goals-submitted");
        assert email.getId() == 1 : "The first sent email should have been returned";
    }

    public void shouldReturnNullIfThereIsNoFirstEmail() throws Exception {
        Email email = EmailMgr.getFirstEmail(1, "goals-due");
        assert email == null : "The return value should be null because there is no row in the db";
    }

    public void shouldReturnLastEmail() throws Exception {
        Email email = EmailMgr.getLastEmail(1, "goals-submitted");
        assert email.getId() == 3 : "The first sent email should have been returned";
    }

    public void shouldReturnNullIfThereIsNoLastEmail() throws Exception {
        Email email = EmailMgr.getLastEmail(1, "goals-due");
        assert email == null : "The return value should be null because there is no row in the db";

    }
}