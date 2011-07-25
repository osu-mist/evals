package edu.osu.cws.pass.hibernate;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/2/11
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */

import edu.osu.cws.pass.models.Email;
import org.hibernate.Transaction;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;


public class EmailMgr {

    /**
     * @todo:
     * from emails where apparaisalID, emailType ordered by sentDate Desc limit 1
     * @param appraisalID
     * @param emailType
     * @return: the latest email for the appraisal and emailType
     *          or null if none exists.
     */
    public static Email getLastEmail(int appraisalID, String emailType)
    {
        return new Email();
    }

    /**
     * @todo:
     * @param appraisalID
     * @param emailType
     * @return the latest email for the appraisal and emailType
     *          or null if none exists.
     */
    public static Email getFirstEmail(int appraisalID, String emailType)
    {
        return new Email();
    }


    /** @todo: Don't need this for now.  Just leave it as a stub.
     * select count(*) from emails where apparaisalID, emailType
     * @param apparaisalID
     * @param emailType
     * @return an integer indicating the number of emails sent for appraisalId and emailType.
     */
    public static int getEmailCount(int apparaisalID, String emailType)
    {
        return 1;
    }


    /**
     * Saves the email pojo to the db.
     *
     * @param email
     * @return
     * @throws Exception
     */
    public static void add(Email email)  throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            email.validate();
            session.save(email);
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

    /**
     * Iterates over the emails in the array and saves them to the db all
     * in one transaction.
     *
     * @param emails
     * @throws Exception
     */
    public static void add(Email[] emails) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            for (Email email : emails) {
                email.validate();
                session.save(email);
            }
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

}
