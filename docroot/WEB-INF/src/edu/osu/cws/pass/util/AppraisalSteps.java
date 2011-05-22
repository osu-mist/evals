package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.AppraisalStep;
import edu.osu.cws.pass.models.PermissionRule;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AppraisalSteps {

    /**
     * Uses list(session) method to grab a list of AppraisalStep. Then
     * it creates a map of appraisal steps using "action"-"appointmentType"-
     * "originalStatus" as the hashmap key.
     *
     * @return ruleMap
     */
    public HashMap list() {
        HashMap stepsMap = new HashMap();
        AppraisalStep step;
        String key;
        Session session = HibernateUtil.getCurrentSession();
        Iterator stepIterator = this.list(session).iterator();

        while (stepIterator.hasNext()) {
            step = (AppraisalStep) stepIterator.next();
            key = step.getAction()+"-"+step.getAppointmentType()+"-"+
                    step.getOriginalStatus();
            stepsMap.put(key, step);
        }

        return stepsMap;
    }

    /**
     * Retrieves a list of AppraisalStep from the database.
     *
     * @param session
     * @return
     * @throws org.hibernate.HibernateException
     */
    public List list(Session session) throws HibernateException {
        Transaction tx = session.beginTransaction();
        List result = session.createQuery("from edu.osu.cws.pass.models.AppraisalStep").list();
        tx.commit();
        return result;
    }
}
