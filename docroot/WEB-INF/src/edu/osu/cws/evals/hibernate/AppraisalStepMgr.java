package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppraisalStep;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashMap;
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
        List<AppraisalStep> results = (List<AppraisalStep>) this.list(session);
        results.size();

        for (AppraisalStep step : results) {
            key = step.getAction()+"-"+step.getAppointmentType();
            stepsMap.put(key, step);
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
        List result = session.createQuery("from edu.osu.cws.evals.models.AppraisalStep").list();
        return result;
    }
}
