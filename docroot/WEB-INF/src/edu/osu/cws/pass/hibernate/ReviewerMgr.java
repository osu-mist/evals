package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Reviewer;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.List;

public class ReviewerMgr {
    /**
     * Uses list(session) method to grab a list of reviewers. Then
     * it creates a map of admins using "pidm" as the key and the
     * admin object as the value.
     *
     * @return ruleMap
     */
    public HashMap<Integer, Reviewer> list() throws Exception {
        HashMap<Integer, Reviewer> reviewers = new HashMap<Integer, Reviewer>();
        Session session = HibernateUtil.getCurrentSession();
        try {
            for (Reviewer reviewer : this.list(session)) {
                reviewers.put(reviewer.getEmployee().getId(), reviewer);
            }
        } catch (Exception e){
            session.close();
            throw e;
        }

        return reviewers;
    }

    /**
     * Retrieves a list of Reviewer from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    public List<Reviewer> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Reviewer> result = session.createQuery("from edu.osu.cws.pass.models.Reviewer").list();
        tx.commit();
        return result;
    }
}
