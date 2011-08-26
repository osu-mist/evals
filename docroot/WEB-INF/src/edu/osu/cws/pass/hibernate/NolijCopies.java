package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Appraisal;
import edu.osu.cws.pass.models.NolijCopy;
import edu.osu.cws.pass.util.HibernateUtil;
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

    /**
     * Specifies whether or not a PDF copy of the appraisal should be sent to NOLIJ. Basically we check that
     * the appraisal has an employee signature, but does not have a record in nolij_copies. This is used
     * by Actions.updateAppraisal.
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    public static boolean sendPDFToNolij(Appraisal appraisal) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            String query = "from edu.osu.cws.pass.models.NolijCopy where appraisalId = :id";
            NolijCopy nolijCopy = (NolijCopy) session.createQuery(query)
                    .setInteger("id", appraisal.getId()).uniqueResult();
            tx.commit();

            if (nolijCopy != null && nolijCopy.getId() != 0 && appraisal.getEmployeeSignedDate() != null) {
                return true;
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return false;
    }
}
