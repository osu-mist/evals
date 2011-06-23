package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

public class AppraisalMgr {
    private Employee loggedInUser;

    private Appraisal appraisal = new Appraisal();
    private CriteriaMgr criteriaMgr = new CriteriaMgr();
    private JobMgr jobMgr = new JobMgr();
    private HashMap appraisalSteps;
    private HashMap permissionRules;

    // Holds a list of appraisal status that are hidden from the employee and
    // instead we display in-review
    private ArrayList<String> statusHiddenFromEmployee = new ArrayList<String>();

    public AppraisalMgr() {
        statusHiddenFromEmployee.add("appraisal-due");
        statusHiddenFromEmployee.add("appraisal-past-due");
        statusHiddenFromEmployee.add("review-due");
        statusHiddenFromEmployee.add("review-past-due");
        statusHiddenFromEmployee.add("release-due");
        statusHiddenFromEmployee.add("release-past-due");
    }

    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job   Job for this appraisal
     * @param type  Type of appraisal: trial, annual or special
     * @return appraisal.id
     * @throws Exception
     */
    public int createAppraisal(Job job, String type) throws Exception {
        CriterionDetail detail;
        Assessment assessment;

        appraisal.setJob(job);
        appraisal.setStatus("goals-due");
        //@todo: how do we define the start and end date ?
        appraisal.setStartDate(new Date());
        appraisal.setEndDate(new Date());
        appraisal.setCreateDate(new Date());
        appraisal.setType(type);

        if (appraisal.validate()) {
            String appointmentType = job.getAppointmentType();
            List<CriterionArea> criteriaList = criteriaMgr.list(appointmentType);
            Session session = HibernateUtil.getCurrentSession();
            try {
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
            } catch (Exception e){
                session.close();
                throw e;
            }
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
        String updatedGoalTextGoalText;
        String originalNewGoalText;
        String updatedNewGoalText;
        GoalLog goalLog;

        // Validate the data first before we try to save anything
        modifiedAppraisal.validate();
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.validate();
        }

        // Try to save the data
        Session session = HibernateUtil.getCurrentSession();
        session.saveOrUpdate(modifiedAppraisal);

        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setModifiedDate(new Date());
            session.saveOrUpdate(assessment);

            // Create new assessment log if necessary
            originalGoalText = assessment.getLastGoalLog(GoalLog.DEFAULT_GOAL_TYPE).getContent();
            updatedGoalTextGoalText = assessment.getGoal();
            //@todo: use a hash instead of comparing these two long text fields
            if (!originalGoalText.equals(updatedGoalTextGoalText) && updatedGoalTextGoalText != null) {
                goalLog = new GoalLog();
                goalLog.setCreateDate(new Date());
                goalLog.setAuthor(loggedInUser);
                goalLog.setContent(updatedGoalTextGoalText);
                assessment.addAssessmentLog(goalLog);
                session.save(goalLog);
            }


            originalNewGoalText = assessment.getLastGoalLog(GoalLog.NEW_GOAL_TYPE).getContent();
            updatedNewGoalText = assessment.getNewGoals();
            //@todo: use a hash instead of comparing these two long text fields
            if (!originalNewGoalText.equals(updatedNewGoalText) && updatedNewGoalText != null) {
                goalLog = new GoalLog();
                goalLog.setCreateDate(new Date());
                goalLog.setAuthor(loggedInUser);
                goalLog.setContent(updatedNewGoalText);
                goalLog.setType(GoalLog.NEW_GOAL_TYPE);
                assessment.addAssessmentLog(goalLog);
                session.save(goalLog);
            }
        }
        return true;
    }


    /**
     * Processes the processAction request (Map) and tries to save the appraisal. This method
     * moves the appraisal to the next appraisal step.
     *
     * @param request
     * @param id
     * @return
     * @throws Exception
     */
    public boolean processUpdateRequest(Map request, int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
            PermissionRule permRule = getAppraisalPermissionRule(appraisal);

            // Check to see if the logged in user has permission to access the appraisal
            if (permRule == null) {
                throw new ModelException("You do  not have permission to view the appraisal");
            }
            // update appraisal & assessment fields based on permission rules
            setAppraisalFields(request, appraisal, permRule);


            //@todo: validate appraisal

            // save changes to db
            updateAppraisal(appraisal);
            tx.commit();

            // If appraisalStep.getEmailType() is not null
                // Send the email  //design in another module, not for the June demo.
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return false;
    }

        /**
     * Handles updating the appraisal fields in the appraisal and assessment objects.
     *
     * @param request
     * @param appraisal
     * @param permRule
     */
    public void setAppraisalFields(Map<String, String[]> request, Appraisal appraisal, PermissionRule permRule)
            throws Exception{
        String parameterKey = "";

        // Save Goals
        if (permRule.getGoals() != null && permRule.getGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "appraisal.goal." + assessmentID;
                if (request.get(parameterKey) != null) {
                    assessment.setGoal(request.get(parameterKey)[0]);
                }
            }
            if (request.get("submit-goals") != null) {
                appraisal.setGoalsSubmitDate(new Date());
            }
            if (request.get("approve-goals") != null) {
                appraisal.setGoalApprovedDate(new Date());
                appraisal.setGoalsApprover(loggedInUser);
            }
        }
        // Save newGoals
        if (permRule.getNewGoals() != null && permRule.getNewGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "appraisal.newGoal." + assessmentID;
                assessment.setNewGoals(request.get(parameterKey)[0]);
            }
        }
        // Save goalComments
        if (permRule.getGoalComments() != null && permRule.getGoalComments().equals("e")) {
            if (request.get("appraisal.goalsComments") != null) {
                appraisal.setGoalsComments(request.get("appraisal.goalsComments")[0]);
            }
        }
        // Save employee results
        if (permRule.getResults() != null && permRule.getResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "assessment.employeeResult." + assessmentID;
                if (request.get(parameterKey) != null) {
                    assessment.setEmployeeResult(request.get(parameterKey)[0]);
                }
            }
        }
        // Save Supervisor Results
        if (permRule.getSupervisorResults() != null && permRule.getSupervisorResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                String assessmentID = Integer.toString(assessment.getId());
                parameterKey = "assessment.supervisorResult." + assessmentID;
                if (request.get(parameterKey) != null) {
                    assessment.setSupervisorResult(request.get(parameterKey)[0]);
                }
            }
        }
        if (request.get("submit-results") != null) {
            appraisal.setResultSubmitDate(new Date());
        }
        // Save evaluation
        if (permRule.getEvaluation() != null && permRule.getEvaluation().equals("e")) {
            if (request.get("appraisal.evaluation") != null) {
                appraisal.setEvaluation(request.get("appraisal.evaluation")[0]);
            }
            if (request.get("appraisal.rating") != null) {
                appraisal.setRating(Integer.parseInt(request.get("appraisal.rating")[0]));
            }
            if (request.get(permRule.getSubmit()) != null) {
                appraisal.setEvaluationSubmitDate(new Date());
                appraisal.setEvaluator(loggedInUser);
            }
        }
        // Save review
        if (permRule.getReview() != null && permRule.getReview().equals("e")) {
            if (request.get("appraisal.review") != null) {
                appraisal.setReview(request.get("appraisal.review")[0]);
            }
            if (request.get("submit-appraisal") != null) {
                appraisal.setReviewer(loggedInUser);
                appraisal.setReviewSubmitDate(new Date());
            }
        }
        if (request.get("sign-appraisal") != null) {
            appraisal.setEmployeeSignedDate(new Date());
        }
        if (request.get("release-appraisal") != null) {
            appraisal.setSignatureRequestedDate(new Date());
        }
        // Save employee response
        if (permRule.getEmployeeResponse() != null && permRule.getEmployeeResponse().equals("e")) {
            appraisal.setRebuttal(request.get("appraisal.rebuttal")[0]);
            if (request.get("submit-response") != null) {
                appraisal.setRespondedDate(new Date());
            }
        }

        // If the appraisalStep object has a new status, update the appraisal object
        String appointmentType = appraisal.getJob().getAppointmentType();
        AppraisalStep appraisalStep = getAppraisalStepKey(request, appointmentType, permRule);
        String newStatus = appraisalStep.getNewStatus();
        if (newStatus != null && !newStatus.equals(appraisal.getStatus())) {
//            _log.error("found appraisalStep "+appraisalStep.toString());
            appraisal.setStatus(newStatus);
            String employeeResponse = appraisal.getRebuttal();
            if (request.get("sign-appraisal") != null &&
                    employeeResponse != null && !employeeResponse.equals("")) {
                appraisal.setStatus("rebuttal-submitted");
            }
        }
    }


    /**
     * Figures out the appraisal step key for the button that the user pressed when the appraisal
     * form was submitted.
     *
     * @param request
     * @param appointmentType
     * @param permRule
     * @return
     */
    private AppraisalStep getAppraisalStepKey(Map request, String appointmentType,
                                              PermissionRule permRule) {
        AppraisalStep appraisalStep;
        String appraisalStepKey;
        ArrayList<String> appraisalButtons = new ArrayList<String>();
        if (permRule.getSaveDraft() != null) {
            appraisalButtons.add(permRule.getSaveDraft());
        }
        if (permRule.getRequireModification() != null) {
            appraisalButtons.add(permRule.getRequireModification());
        }
        if (permRule.getSubmit() != null) {
            appraisalButtons.add(permRule.getSubmit());
        }

        for (String button : appraisalButtons) {
            // If this button is the one the user clicked, use it to look up the
            // appraisalStepKey
            if (request.get(button) != null) {
                appraisalStepKey = button + "-" + appointmentType;
                appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
//                _log.error("appraisalStepKey = "+appraisalStepKey);
                if (appraisalStep != null) {
                    return appraisalStep;
                }
            }
        }

        return new AppraisalStep();
    }


    /**
     * Figures out the current user role in the appraisal and returns the respective permission
     * rule for that user role and action in the appraisal.
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    public PermissionRule getAppraisalPermissionRule(Appraisal appraisal) throws Exception {
        String permissionKey = "";
        String role = getRole(appraisal, loggedInUser.getId());
        permissionKey = appraisal.getStatus()+"-"+ role;

        return (PermissionRule) permissionRules.get(permissionKey);
    }

    /**
     * Wrapper method for getAppraisalPermissionRule(appraisal). It starts a session and
     * transaction and calls that method.
     *
     * @param appraisal
     * @param startSession
     * @return
     * @throws Exception
     */
    public PermissionRule getAppraisalPermissionRule(Appraisal appraisal, boolean startSession)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        PermissionRule permissionRule;
        try {
            Transaction tx = session.beginTransaction();
            permissionRule = getAppraisalPermissionRule(appraisal);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return permissionRule;
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
     * @throws Exception
     */
    public ArrayList<HashMap> getAllMyActiveAppraisals(int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<HashMap> myActiveAppraisals;
        try {
            myActiveAppraisals = this.getAllMyActiveAppraisals(pidm, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return myActiveAppraisals;
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
    public List<HashMap> getMyTeamsActiveAppraisals(Integer pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<HashMap> teamActiveAppraisals;
        try {
            teamActiveAppraisals = this.getMyTeamsActiveAppraisals(pidm, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return teamActiveAppraisals;
    }

    /**
     * Returns a list of hashmap that includes the job title, employee name, status,
     * start/end date and appointment type of the jobs' the employee supervises.
     *
     * @param pidm      Supervisor's pidm.
     * @param session
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
     * @throws Exception
     */
    public String getRole(Appraisal appraisal, int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Job supervisor;
        if (pidm == appraisal.getJob().getEmployee().getId()) {
            return "employee";
        }

        supervisor = jobMgr.getSupervisor(appraisal.getJob());
        if (supervisor != null && pidm == supervisor.getEmployee().getId()) {
            return "supervisor";
        }

        String query = "from edu.osu.cws.pass.models.Reviewer where " +
                "businessCenterName = :businessCenterName and employee.id = :pidm " +
                "and employee.status = 'A'";
        List reviewerList = session.createQuery(query)
                .setString("businessCenterName", appraisal.getJob().getBusinessCenterName())
                .setInteger("pidm", pidm)
                .list();

        if (reviewerList.size() != 0) {
            return "reviewer";
        }

        if (jobMgr.isUpperSupervisor(appraisal.getJob(), pidm)) {
            return "upper-supervisor";
        }

        return "";
    }

    /**
     * This method is just a wrapper for session.get. It returns the appraisal that
     * matches the id. It also adds the currentSupervisor to the appraisal object.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public Appraisal getAppraisal(int id) throws Exception {
        int userID = loggedInUser.getId();
        String userRole;
        String appraisalStatus;

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            appraisal = getAppraisal(id, session);
            userRole = getRole(appraisal, userID);
            appraisalStatus = appraisal.getStatus();
            tx.commit();

            Job supervisorJob = jobMgr.getSupervisor(appraisal.getJob());
            appraisal.getJob().setCurrentSupervisor(supervisorJob);

            if (userRole.equals("employee") && statusHiddenFromEmployee.contains(appraisalStatus)) {
                appraisal.setRoleBasedStatus("in-review");
            } else {
                appraisal.setRoleBasedStatus(appraisal.getStatus());
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }


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
        appraisal = (Appraisal) session.get(Appraisal.class, id);
        return appraisal;
    }

    public ArrayList<HashMap> getReviews(String businessCenterName) throws Exception {
        ArrayList<HashMap> reviewList;
        Session session = HibernateUtil.getCurrentSession();
        try {
            reviewList = getReviews(businessCenterName, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return reviewList;
    }


    /**
     * Returns an ArrayList of HashMaps which contain data about appraisals pending
     * review. This method is used to display a list of pending reviews in the displayReview
     * actions method.
     *
     * @param businessCenterName
     * @param session
     * @return
     */
    private ArrayList<HashMap> getReviews(String businessCenterName, Session session) {
        Transaction tx = session.beginTransaction();
        String query = "select new map(id as id, job.jobTitle as jobTitle, " +
                "concat(job.employee.lastName, ', ', job.employee.firstName) as employeeName, " +
                "evaluationSubmitDate as evaluationSubmitDate, status as status) " +
                "from edu.osu.cws.pass.models.Appraisal where job.businessCenterName = :bc " +
                "and status in ('review-due', 'review-past-due') and job.endDate is NULL";

        ArrayList<HashMap> result =  (ArrayList<HashMap>) session.createQuery(query)
                .setString("bc", businessCenterName).list();
        tx.commit();
        return result;
    }

    /**
     * Returns a count of the pending reviews for a given business center.
     *
     * @param businessCenterName
     * @return
     * @throws Exception
     */
    public int getReviewCount(String businessCenterName) throws Exception {
        int reviewCount = 0;
        Session session = HibernateUtil.getCurrentSession();
        try {
            reviewCount = getReviewCount(businessCenterName, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return reviewCount;
    }

    private int getReviewCount(String businessCenterName, Session session) {
        Transaction tx = session.beginTransaction();
        int count = session.createQuery("from edu.osu.cws.pass.models.Appraisal " +
                "where status in ('review-due', 'review-past-due') " +
                "and job.businessCenterName = :bcName and job.endDate is NULL")
                .setString("bcName", businessCenterName)
                .list().size();
        tx.commit();
        return count;
    }

    public void setLoggedInUser(Employee loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void setAppraisalSteps(HashMap appraisalSteps) {
        this.appraisalSteps = appraisalSteps;
    }

    public void setPermissionRules(HashMap permissionRules) {
        this.permissionRules = permissionRules;
    }
}

