package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppraisalStep;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;

public class AppraisalStepMgr {

    /**
     * Grabs a list of AppraisalStep.
     * Then it creates a map of appraisal steps using "action"-"appointmentType"-
     * "originalStatus" as the hashmap key.
     *
     * @return ruleMap
     */
    public static HashMap list() throws Exception {
        HashMap stepsMap = new HashMap();
        Session session = HibernateUtil.getCurrentSession();
        List<AppraisalStep> results = session.createQuery("from edu.osu.cws.evals.models.AppraisalStep").list();

        for (AppraisalStep step : results) {
            String key = step.getAction()+"-"+step.getAppointmentType();
            if (step.getEmailType() != null) {
                step.getEmailType().toString();
            }
            stepsMap.put(key, step);
        }

        return stepsMap;
    }
}
