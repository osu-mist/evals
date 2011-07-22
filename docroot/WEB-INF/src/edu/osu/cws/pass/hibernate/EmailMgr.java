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
     * todo: Insert the email object into the emails table
     * @param email
     * @return
     * @throws Exception
     */
    public static int add(Email email)  throws Exception
    {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        //@@@Do I really need a transaction?
        email.validate();
        session.save(email);
        tx.commit();
        return 1; //@@@ Need to look at this
    }

    /**
     * @todo: Inserts all the email objects passed in to the emails table.
     * All in one transaction
     * @param emails
     * @return: Number of records inserted.
     */
    public static int add(Email[] emails)
    {
        return 1;
    }

}
