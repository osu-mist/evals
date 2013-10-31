package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.ClassifiedITObject;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class ClassifiedITObjectMgr {


    /**
     * Returns a list of ClassifiedITObject contains limited attributes set: employee name, review Period
     *
     * @param pidm          Supervisor's pidm.
     * @return List of Appraisal that contains the ClassifiedITObject this supervisor relate to.
     */
    public static ArrayList<ClassifiedITObject> getMyClassifiedITAppraisals (Integer pidm)
            throws Exception {
        Session Session = HibernateUtil.getCurrentSession();
        Criteria criteria = Session.createCriteria(Job.class);
        ArrayList<ClassifiedITObject> myTeamClassifiedITObject = new ArrayList<ClassifiedITObject>();
        criteria.add(Restrictions.eq("supervisor.employee.id", pidm)).add(Restrictions.eq("status", "A")).
                add(Restrictions.like("appointmentType", AppointmentType.CLASSIFIED_IT));
        List result = criteria.list();
        if (result.isEmpty()) {
            return myTeamClassifiedITObject;
        }
        for (Object jResult : result) {
            Job job = (Job) jResult;

            job.setAnnualInd(Constants.ANNUAL_IND);
            DateTime startDate, endDate;
            startDate = job.getNewAnnualStartDate();
            if(startDate.isAfterNow()) {
                startDate = startDate.minusMonths(12);
            }
            endDate = job.getEndEvalDate(startDate, "annual");
            String name = job.getEmployee().getName();
            String reviewPeriod = getReviewPeriod(startDate, endDate);
            ClassifiedITObject classifiedITObject = new ClassifiedITObject(name, reviewPeriod);
            myTeamClassifiedITObject.add(classifiedITObject);

        }
        return myTeamClassifiedITObject;
    }

    public static String getReviewPeriod(DateTime startDate, DateTime endDate) {
        if (startDate == null) {
            startDate = EvalsUtil.getToday();
        }
        if (endDate == null) {
            endDate = EvalsUtil.getToday();
        }

        return startDate.toString(Constants.DATE_FORMAT) + " - " +
                endDate.toString(Constants.DATE_FORMAT);
    }
}
