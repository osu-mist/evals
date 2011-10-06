package edu.osu.cws.evals.hibernate;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/2/11
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */

import edu.osu.cws.evals.models.Email;
import org.hibernate.Transaction;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;


public class EmailMgr {

    /**
     * @param appraisalID
     * @param emailType
     * @return the latest email for the appraisal and emailType or null if none exists.
     * @throws Exception
     */
    public static Email getLastEmail(int appraisalID, String emailType) throws Exception {
        return getEmailBySortOrder(appraisalID, emailType, true);
    }

    /**
     * Returns the first email sent for a given appraisal and emailType. It allows sorting of the
     * email list asc or desc using sentDate. This method is used by getLastEmail and getFirstEmail.
     *
     * @param appraisalID
     * @param emailType
     * @param sortDesc
     * @return
     * @throws Exception
     */
    private static Email getEmailBySortOrder(int appraisalID, String emailType, boolean sortDesc) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        try {
            String query = "from edu.osu.cws.evals.models.Email email " +
                    "where email.appraisalId = :appraisalId and email.emailType = :emailType " +
                    "order by  sentDate ";
            if (sortDesc) {
                query += "desc";
            } else {
                query += "asc";
            }

            List<Email> emails = session.createQuery(query)
                    .setInteger("appraisalId", appraisalID)
                    .setString("emailType", emailType)
                    .setMaxResults(1)
                    .list();

            if (!emails.isEmpty()) {
                return emails.get(0);
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }
     return null;
}

    /**
     * @param appraisalID
     * @param emailType
     * @return the first email for the appraisal and emailType or null if none exists.
     * @throws Exception
     */
    public static Email getFirstEmail(int appraisalID, String emailType) throws Exception {
        return getEmailBySortOrder(appraisalID, emailType, false);
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
            email.validate();
            session.save(email);
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

    /**
     * Iterates over the emails in the list and saves them to the db all
     * in one transaction.
     *
     * @param emails
     * @throws Exception
     */
    public static void add(List<Email> emails) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            for (Email email : emails) {
                email.validate();
                session.save(email);
            }
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

}
