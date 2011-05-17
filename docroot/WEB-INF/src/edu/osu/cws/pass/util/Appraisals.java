package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

public class Appraisals {
    private Employee loggedInUser;

    private Appraisal appraisal = new Appraisal();
    private CriterionArea criterionArea = new CriterionArea();
    private Criteria criteria = new Criteria();

    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job
     * @return appraisal.id
     * @throws ModelException
     */
    public int createAppraisal(Job job) throws ModelException {
        CriterionDetail detail;
        Assessment assessment;

        appraisal.setJob(job);
        appraisal.setStatus("goals-due");
        //@todo: how do we define the start and end date ?
        appraisal.setStartDate(new Date());
        appraisal.setEndDate(new Date());
        appraisal.setCreateDate(new Date());

        if (appraisal.validate()) {
            List<CriterionArea> criteriaList = criteria.list(job.getAppointmentType().getId());
            Session session = HibernateUtil.getCurrentSession();
            Transaction tx = session.beginTransaction();
            session.save(appraisal);

            // Create assessment and associate it to appraisal
            for (CriterionArea criterion : criteriaList) {
                detail = criterion.getCurrentDetail();
                assessment = new Assessment();
                assessment.setCriterionDetail(detail);
                assessment.setAppraisal(appraisal);
                assessment.setCreateDate(new Date());
                session.save(assessment);

            }
            tx.commit();
        }

        return appraisal.getId();
    }

    /**
     * Updates the appraisal object along with the assessment object. If the goals have been
     * modified, a new record is inserted in the assessments_logs table.
     *
     * @param modifiedAppraisal
     * @return
     * @throws ModelException
     */
    public boolean updateAppraisal(Appraisal modifiedAppraisal) throws ModelException {
        String originalGoalText;
        String newGoalText;
        AssessmentLog assessmentLog;

        // Validate the data first before we try to save anything
        modifiedAppraisal.validate();
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.validate();
        }

        // Try to save the data
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        modifiedAppraisal.setModifiedDate(new Date());
        int originalAppraisalID = modifiedAppraisal.getId();
        session.saveOrUpdate(modifiedAppraisal);

        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setModifiedDate(new Date());
            session.saveOrUpdate(assessment);

            // Create new assessment log if necessary
            originalGoalText = assessment.getLastAssessmentLog().getContent();
            newGoalText = assessment.getGoal();
            if (!originalGoalText.equals(newGoalText) && newGoalText != null) {
                assessmentLog = new AssessmentLog();
                assessmentLog.setCreateDate(new Date());
                assessmentLog.setAuthor(loggedInUser);
                assessmentLog.setContent(newGoalText);
                assessment.addAssessmentLog(assessmentLog);
                session.save(assessmentLog);
            }
        }
        tx.commit();
        return true;
    }

    /**
     * Returns a list of active appraisals for all the jobs that the current pidm holds.
     * The fields that are returned in the appraisal are:
     *      appraisalID
     *      positionTitle
     *      startDate
     *      endDate
     *      status;
     *
     * @param pidm
     * @return
     */
    public ArrayList<HashMap> getAllMyActiveAppraisals(int pidm) {
        Session session = HibernateUtil.getCurrentSession();
        return this.getAllMyActiveAppraisals(pidm, session);
    }

    /**
     * Returns a HashMap
     * @param pidm
     * @param session
     * @return
     */
    private ArrayList<HashMap> getAllMyActiveAppraisals(int pidm, Session session) {
        Transaction tx = session.beginTransaction();
        //@todo: the query below should have a where clause => job.employee.id = pidm
        String query = "select new map( id as id, job.positionTitle as positionTitle, " +
                "startDate as startDate, endDate as endDate, status as status)" +
                " from edu.osu.cws.pass.models.Appraisal where " +
                " job.employeePidm.id = :pidm and status not in ('completed', 'closed')";
        ArrayList<HashMap> result = (ArrayList<HashMap>) session.createQuery(query)
                .setInteger("pidm", pidm)
                .list();
        tx.commit();
        return result;
    }

    public void setLoggedInUser(Employee loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}

