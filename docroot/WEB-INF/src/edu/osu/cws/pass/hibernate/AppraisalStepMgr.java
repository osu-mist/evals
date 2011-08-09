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
        String key;
        Session session = HibernateUtil.getCurrentSession();
        try {
            List<AppraisalStep> results = (List<AppraisalStep>) this.list(session);
            results.size();

            for (AppraisalStep step : results) {
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
