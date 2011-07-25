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

import java.util.Date;

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
        Email[] emails = new Email[2];
        Email email = new Email();
        email.setAppraisalId(1);
        email.setEmailType("goals-submitted");
        email.setSentDate(new Date());
        emails[0] = email;

        email.setAppraisalId(2);
        email.setEmailType("goals-required-modification");
        emails[1] = email;

        EmailMgr.add(emails);
    }
}