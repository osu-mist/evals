package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.NolijCopy;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;

public class NolijCopies {

    /**
     * Handles adding a new nolij_copy entry to the nolij_copies table.
     *
     * @param appraisalId
     * @param filename
     * @return
     * @throws Exception
     */
    public static boolean add(int appraisalId, String filename) throws Exception {
        NolijCopy nolijCopy = new NolijCopy();
        nolijCopy.setAppraisalId(appraisalId);
        nolijCopy.setFilename(filename);
        nolijCopy.setSubmitDate(new Date());

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            nolijCopy.validate();
            session.save(nolijCopy);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return true;

    }

}
