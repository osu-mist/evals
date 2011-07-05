package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.AppraisalStep;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AppraisalStepMgr {

    /**
     * Uses list(session) method to grab a list of AppraisalStep. Then
     * it creates a map of appraisal steps using "action"-"appointmentType"-
     * "originalStatus" as the hashmap key.
     *
     * @return ruleMap
     */
    public HashMap list() throws Exception {
        HashMap stepsMap = new HashMap();
        AppraisalStep step;
        String key;
        Session session = HibernateUtil.getCurrentSession();
        try {
            Iterator stepIterator = this.list(session).iterator();

            while (stepIterator.hasNext()) {
                step = (AppraisalStep) stepIterator.next();
                key = step.getAction()+"-"+step.getAppointmentType();
                stepsMap.put(key, step);
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return stepsMap;
    }

    /**
     * Retrieves a list of AppraisalStep from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    public List list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("from edu.osu.cws.pass.models.AppraisalStep").list();
        tx.commit();
        return result;
    }
}