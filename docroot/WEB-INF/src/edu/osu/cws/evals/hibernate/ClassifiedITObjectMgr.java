package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.ClassifiedITObject;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 10/4/12
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassifiedITObjectMgr {


    /**
     * Returns a list of ClassifiedITObject contains limited attributes set: employee name, review Period
     *
     * @param pidm          Supervisor's pidm.
     * @return List of Appraisal that contains the ClassifiedITObject this supervisor relate to.
     */
    public static ArrayList<ClassifiedITObject> getMyClassifiedITAppraisals (Integer pidm)
            throws Exception {
        Session hibSession = HibernateUtil.getCurrentSession();
        Criteria criteria = hibSession.createCriteria(Job.class);
        ArrayList<ClassifiedITObject> myTeamClassifiedITObject = new ArrayList<ClassifiedITObject>();
        String reviewPeriod = "";
        String name = "";
        criteria.add(Restrictions.eq("supervisor.employee.id", pidm)).add(Restrictions.eq("status", "A")).
                add(Restrictions.like("appointmentType", AppointmentType.CLASSIFIED_IT));
        List result = criteria.list();
        if (result.isEmpty()) {
            return myTeamClassifiedITObject;
        }
        for (Object jResult : result) {
            Job job = (Job) jResult;

            job.setAnnualInd(Constants.ANNUAL_IND);
            Date startDate, endDate;
            Calendar startCal = job.getNewAnnualStartDate();
            startDate = startCal.getTime();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (startDate.before(EvalsUtil.getEvalsStartDate())) {
                startCal.set(Calendar.YEAR, currentYear);
                startDate = startCal.getTime();
            }
            endDate = job.getEndEvalDate(startDate, "annual");
            name = job.getEmployee().getName();
            reviewPeriod = getReviewPeriod(startDate, endDate);
            ClassifiedITObject classifiedITObject = new ClassifiedITObject(name, reviewPeriod);
            myTeamClassifiedITObject.add(classifiedITObject);

        }
        return myTeamClassifiedITObject;
    }

    public static String getReviewPeriod(Date startDate,Date endDate) {
        if (startDate == null) {
            startDate = new Date();
        }
        if (endDate == null) {
            startDate = new Date();
        }

        return MessageFormat.format("{0,date,MM/dd/yy} - {1,date,MM/dd/yy}",
                new Object[]{startDate, endDate});
    }
}
