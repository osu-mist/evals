package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.AppraisalStep;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Admins {

    /**
     * Uses list(session) method to grab a list of AppraisalStep. Then
     * it creates a map of appraisal steps using "action"-"appointmentType"-
     * "originalStatus" as the hashmap key.
     *
     * @return ruleMap
     */
    public List list() {
        Session session = HibernateUtil.getCurrentSession();
        return this.list(session);
    }

    /**
     * Retrieves a list of PermissionRule from the database.
     *
     * @param session
     * @return
     * @throws org.hibernate.HibernateException
     */
    public List list(Session session) throws HibernateException {
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("from edu.osu.cws.pass.models.Admin").list();
        tx.commit();
        return result;
    }
}
