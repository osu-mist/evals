package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Email;
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


    /**
     * select count(*) from emails where apparaisalID, emailType
     *
     * @param appraisalID
     * @param emailType
     * @return an integer indicating the number of emails sent for appraisalId and emailType.
     */
    public static int getEmailCount(int appraisalID, String emailType) {
        Session session = HibernateUtil.getCurrentSession();
        String query = "select count(*) from edu.osu.cws.evals.models.Email email " +
                "where email.appraisalId = :appraisalId and email.emailType = :emailType";

        Object countObj = session.createQuery(query)
                .setInteger("appraisalId", appraisalID)
                .setString("emailType", emailType)
                .list().get(0);

        return Integer.parseInt(countObj.toString());
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
        email.validate();
        session.save(email);
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
        for (Email email : emails) {
            email.validate();
            session.save(email);
        }
    }

    /**
     * Returns true if no emails have been sent for the *Due and *Overdue status of the appraisal.
     *
     * @param appraisalId
     * @param status
     * @return
     * @throws Exception
     */
    public static Boolean haveNotSentDueOrOverdueEmail(int appraisalId, String status) throws Exception {
        String dueStatus = status.replace(Appraisal.OVERDUE, Appraisal.DUE);
        Email lastDueEmail = EmailMgr.getLastEmail(appraisalId, dueStatus);
        Email lastOverDueEmail = EmailMgr.getLastEmail(appraisalId, status);

        // only allow overdue email to be sent if no due or *overdue email has been sent
        return lastDueEmail == null && lastOverDueEmail == null;
    }

}
