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
     * from emails where apparaisalID, emailType ordered by sentDate Desc limit 1
     * @param appraisalID
     * @param emailType
     * @return
     */
    public static Email getLastEmail(int appraisalID, String emailType)
    {
        return new Email();
    }


    public static Email getFirstEmail(int appraisalID, String emailType)
    {
        return new Email();
    }

    /**
     * select count(*) from emails where apparaisalID, emailType
     * @param apparaisalID
     * @param emailType
     * @return
     */
    public static int emailCount(int apparaisalID, String emailType)
    {
        return 1;
    }



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

}
