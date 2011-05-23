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
    private Jobs jobs = new Jobs();

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
            List<CriterionArea> criteriaList = criteria.list(job.getAppointmentType());
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
        GoalLog goalLog;

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
                goalLog = new GoalLog();
                goalLog.setCreateDate(new Date());
                goalLog.setAuthor(loggedInUser);
                goalLog.setContent(newGoalText);
                assessment.addAssessmentLog(goalLog);
                session.save(goalLog);
            }
        }
        tx.commit();
        return true;
    }

    /**
     * Returns a list of active appraisals for all the jobs that the current pidm holds.
     * The fields that are returned in the appraisal are:
     *      appraisalID
     *      jobTitle
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
        String query = "select new map( id as id, job.jobTitle as jobTitle, " +
                "startDate as startDate, endDate as endDate, status as status)" +
                " from edu.osu.cws.pass.models.Appraisal where " +
                " job.employee.id = :pidm and status not in ('completed', 'closed')";
        ArrayList<HashMap> result = (ArrayList<HashMap>) session.createQuery(query)
                .setInteger("pidm", pidm)
                .list();
        tx.commit();
        return result;
    }

    /**
     * Returns a List of team's active appraisals for the given supervisor's pidm.
     *
     * @param pidm
     * @return
     */
    public List<HashMap> getMyTeamsActiveAppraisals(Integer pidm) {
        Session session = HibernateUtil.getCurrentSession();
        return this.getMyTeamsActiveAppraisals(pidm, session);
    }

    /**
     * Returns a list of hashmap that includes the job title, employee name, status,
     * start/end date and appointment type of the jobs' the employee supervises.
     *
     * @param pidm      Supervisor's pidm.
     * @return List of Hashmaps that contains the jobs this employee supervises.
     */
    private List<HashMap> getMyTeamsActiveAppraisals(Integer pidm, Session session) {
        Transaction tx = session.beginTransaction();
        String query = "select new map(id as id, job.jobTitle as jobTitle, " +
                "concat(job.employee.lastName, ', ', job.employee.firstName) as employeeName, " +
                "job.appointmentType as appointmentType, startDate as startDate, " +
                "endDate as endDate, status as status) " +
                "from edu.osu.cws.pass.models.Appraisal where job.supervisor.employee.id = :pidm " +
                "and status not in ('completed', 'closed') ";
        List result =  session.createQuery(query).setInteger("pidm", pidm).list();

        tx.commit();
        return result;
    }

    /**
     * Returns the role (employee, supervisor, immediate supervisor or reviewer) of the pidm
     * in the given appraisal. Return empty string if the pidm does not have any role on the
     * appraisal.
     *
     * @param appraisal     appraisal to check role in
     * @param pidm          pidm of the user to check
     * @return role
     */
    public String getRole(Appraisal appraisal, int pidm) throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        Job supervisor;
        if (pidm == appraisal.getJob().getEmployee().getId()) {
            return "employee";
        }

        supervisor = jobs.getSupervisor(appraisal.getJob());
        if (supervisor != null && pidm == supervisor.getEmployee().getId()) {
            return "supervisor";
        }

        Transaction tx = session.beginTransaction();
        String query = "from edu.osu.cws.pass.models.Reviewer where " +
                "businessCenterName = :businessCenterName and employee.active = 1";
        List reviewerList = session.createQuery(query)
                .setString("businessCenterName", appraisal.getJob().getBusinessCenterName())
                .list();
        tx.commit();

        if (reviewerList.size() != 0) {
            return "reviewer";
        }

        if (jobs.isUpperSupervisor(appraisal.getJob(), pidm)) {
            return "upper-supervisor";
        }

        return "";
    }

    /**
     * This method is just a wrapper for session.get. It returns the appraisal that
     * matches the id.
     *
     * @param id
     * @return
     */
    public Appraisal getAppraisal(int id) throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        appraisal = getAppraisal(id, session);
        appraisal.getJob().setCurrentSupervisor(jobs.getSupervisor(appraisal.getJob()));
        return appraisal;
    }

    /**
     * This method is just a wrapper for getAppraisal(int id). It performs the hibernate
     * call to retrieve the appraisal.
     *
     * @param id
     * @param session
     * @return
     */
    private Appraisal getAppraisal(int id, Session session) {
        Transaction tx = session.beginTransaction();
        appraisal = (Appraisal) session.get(Appraisal.class, id);
        tx.commit();
        return appraisal;
    }

    public void setLoggedInUser(Employee loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}

