package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.HibernateUtil;
import edu.osu.cws.pass.util.Mailer;
import edu.osu.cws.pass.util.PassUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.*;

public class AppraisalMgr {
    private Employee loggedInUser;

    private Appraisal appraisal = new Appraisal();
    private CriteriaMgr criteriaMgr = new CriteriaMgr();
    private JobMgr jobMgr = new JobMgr();
    private EmployeeMgr employeeMgr = new EmployeeMgr();
    private HashMap appraisalSteps;
    private HashMap permissionRules;
    private HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();

    // Holds a list of appraisal status that are hidden from the employee and
    // instead we display in-review
    private ArrayList<String> statusHiddenFromEmployee = new ArrayList<String>();

    public AppraisalMgr() {
        statusHiddenFromEmployee.add("appraisalDue");
        statusHiddenFromEmployee.add("appraisalOverdue");
        statusHiddenFromEmployee.add("reviewDue");
        statusHiddenFromEmployee.add("reviewOverdue");
        statusHiddenFromEmployee.add("releaseDue");
        statusHiddenFromEmployee.add("releaseOverdue");
    }

    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job   Job for this appraisal
     * @param type: trial, annual, initial
     * @param goalsDueConfig: Configuration object of goalsDue or resultsDue
     * @param startDate: starting date of appraisal period.
     * @return appraisal.id
     * @throws Exception
     */
    public static Appraisal createAppraisal(Job job, Date startDate, String type,
                                            Configuration goalsDueConfig) throws Exception {
        CriteriaMgr criteriaMgr = new CriteriaMgr();
        Appraisal appraisal = new Appraisal();
        CriterionDetail detail;
        Assessment assessment;

        if (!type.equals(Appraisal.TYPE_TRIAL) && !type.equals(Appraisal.TYPE_ANNUAL) &&
                !type.equals(Appraisal.TYPE_INITIAL)) {
            throw new ModelException("Invalid appraisal type : " + type);
        }

        appraisal.setJob(job);
        appraisal.setStartDate(startDate);
        appraisal.setCreateDate(new Date());

        // In the db, we only store: annual or trial.
        String dbType = type;
        if (type.equals(Appraisal.TYPE_INITIAL)) {
            dbType = Appraisal.TYPE_ANNUAL;
        }
        appraisal.setType(dbType);

        Date endDate = job.getEndEvalDate(startDate, type);
        appraisal.setEndDate(endDate);

        createAppraisalStatus(startDate, goalsDueConfig, appraisal);

        if (appraisal.validate()) {
            String appointmentType = job.getAppointmentType();
            List<CriterionArea> criteriaList = criteriaMgr.list(appointmentType);
            Session session = HibernateUtil.getCurrentSession();
            try {
                Transaction tx = session.beginTransaction();
                session.save(appraisal);///441

                // Create assessment and associate it to appraisal
                for (CriterionArea criterion : criteriaList) {
                    detail = criterion.getCurrentDetail();
                    assessment = new Assessment();
                    assessment.setCriterionDetail(detail);
                    assessment.setAppraisal(appraisal);
                    assessment.setCreateDate(new Date());
                    assessment.setModifiedDate(new Date());
                    session.save(assessment);

                }
                tx.commit();
            } catch (Exception e){
                session.close();
                throw e;
            }
        }

        return appraisal;
    }

    /**
     * Sets the status of the appraisal. If the startDate of the appraisal is before Nov 1st, 2011, we set the
     * status to appraisalDue, else if
     *
     * @param startDate
     * @param goalsDueConfig
     * @param appraisal
     * @throws Exception
     */
    private static void createAppraisalStatus(Date startDate, Configuration goalsDueConfig,
                                              Appraisal appraisal) throws Exception {
        //@@todo: Need to change this back.
        String Nov1st2011 = "06/01/2011";
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        Date startPointDate = fmt.parse(Nov1st2011);
        if (startDate.before(startPointDate)) {
            appraisal.setStatus("appraisalDue");
        } else if (PassUtil.isDue(appraisal, goalsDueConfig) < 0) {
            appraisal.setStatus("goalsOverdue");
        } else {
            appraisal.setStatus("goalsDue");
        }
    }


    public static List<Appraisal>  getActiveAppraisals(Job job)
    {
        return new ArrayList();
    }

    /**
     * creates an appraisal record and returns it
     * @param job:  the job the appraisal record is created against
     * @param Type:  possible values are trials, annual and initial.
     * @             Initial is annual except the length of the period is defined by annual_eval_ind
     * @return:  the appraisal record created
     * @throws Exception
     */
   /* public static Appraisal createAppraisal(Job job, String Type) throws Exception
    {
        return new Appraisal();
    }
    */

    /**
     * This method is called upon completion or closure of an trial appraisal to create the
     * initial appraisal record.
     * It copies the jobpidm, position#, subfix, all the fields related to goals,
     * the assessment records from the trial records.
     * Depends on the times, it either sets the status to goalsApproved, resultsDue
     * or results over due.
     * startDate of the appraisal record is the first day of the month on or after
     * the eval_date or job_start_date.
     * end_date of the appraisal record is the number of month indicated by
     * PYVPASJ.annual_eval_ind.
     *
     * @param  trialAppraisal: this is the newly closed or completed trial appraisal
     * @return the newly created appraisal
     *
     */
    public static Appraisal createInitialAppraisalAfterTrial(Appraisal trialAppraisal,
                                                             Configuration resultsDueConfig) throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setType(Appraisal.TYPE_ANNUAL);
        appraisal.setJob(trialAppraisal.getJob());
        appraisal.setCreateDate(new Date());
        Date initialEvalStartDate = appraisal.getJob().getInitialEvalStartDate();
        appraisal.setStartDate(initialEvalStartDate);
        appraisal.setGoalsSubmitDate(trialAppraisal.getGoalsSubmitDate());
        appraisal.setGoalsApprover(trialAppraisal.getGoalsApprover());
        appraisal.setGoalApprovedDate(trialAppraisal.getGoalApprovedDate());

        Date endDate = appraisal.getJob().getEndEvalDate(appraisal.getStartDate(), Appraisal.TYPE_INITIAL);
        appraisal.setEndDate(endDate);

        int resultsDue = PassUtil.isDue(appraisal, resultsDueConfig);
        if (resultsDue == 0) {
            appraisal.setStatus("resultsDue");
        } else if (resultsDue < 0) {
            appraisal.setStatus("resultsOverdue");
        } else {
            appraisal.setStatus("goalsApproved");
        }

        if (appraisal.validate()) {
            Session session = HibernateUtil.getCurrentSession();
            try {
                session.save(appraisal);

                Assessment newAssessment;
                for (Assessment origAssesment: trialAppraisal.getAssessments()) {
                    newAssessment = new Assessment();
                    newAssessment.setCriterionDetail(origAssesment.getCriterionDetail());
                    newAssessment.setNewGoals(origAssesment.getNewGoals());
                    newAssessment.setGoal(origAssesment.getGoal());
                    newAssessment.setAppraisal(appraisal);
                    newAssessment.setCreateDate(new Date());
                    newAssessment.setEmployeeResult(origAssesment.getEmployeeResult());
                    newAssessment.setSupervisorResult(origAssesment.getSupervisorResult());
                    session.save(newAssessment);
                }
            } catch (Exception e){
                session.close();
                throw e;
            }
        }

        return appraisal;
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
                if (updatedGoalTextGoalText.equals("")) {
                    updatedGoalTextGoalText = "empty";
                }
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
                if (updatedNewGoalText.equals("")) {
                    updatedNewGoalText = "empty";
                }
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
     * moves the appraisal to the next appraisal step and sends any emails if necessary.
     *
     * @param request
     * @param id                id of appraisal
     * @param resultsDueConfig  Configuration object specifying when the results are due
     * @param mailer            Mailer object to send emails to supervisor and employee
     * @return appraisal        Appraisal object after processing the update request
     * @throws Exception
     */
    public Appraisal processUpdateRequest(Map request, int id, Configuration resultsDueConfig, Mailer mailer)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
            Job supervisorJob = jobMgr.getSupervisor(appraisal.getJob());
            appraisal.getJob().setCurrentSupervisor(supervisorJob);
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

            // Creates the first annual appraisal if needed
            String action = "";
            if (request.get("sign-appraisal") != null) {
                action = "sign-appraisal";
            }
            createFirstAnnualAppraisal(appraisal, resultsDueConfig, action);
            tx.commit();

            // Send email if needed
            String appointmentType = appraisal.getJob().getAppointmentType();
            AppraisalStep appraisalStep;
            String employeeResponse = appraisal.getRebuttal();

            // If the employee signs and provides a rebuttal, we want to use a different
            // appraisal step so that we can send an email to the reviewer.
            if (submittedRebuttal(request, employeeResponse)) {
                String appraisalStepKey = "submit-response-" + appointmentType;
                appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
            } else {
                appraisalStep = getAppraisalStepKey(request, appointmentType, permRule);
            }

            EmailType emailType = appraisalStep.getEmailType();
            if (emailType != null) {
                mailer.sendMail(appraisal, emailType);
            }

            return appraisal;
        } catch (Exception e) {
            if (session.isOpen()) {
                session.close();
            }
            throw e;
        }
    }

    /**
     * Creates the first annual appraisal if needed. The first annual appraisal is created if:
     *  1) The current appraisal is of type: trial
     *  2) the action is sign-appraisal
     *  3) The job annual_indicator != 0
     *
     * @param appraisal
     * @param resultsDueConfig
     * @param action
     * @throws Exception
     */
    private void createFirstAnnualAppraisal(Appraisal appraisal, Configuration resultsDueConfig, String action)
            throws Exception {
        Job job = appraisal.getJob();

        if (!appraisal.getType().equals(Appraisal.TYPE_TRIAL)) {
            return;
        }
        if (!action.equals("sign-appraisal")) {
            return;
        }
        if (job.getAnnualInd() == 0) {
            return;
        }
        AppraisalMgr.createInitialAppraisalAfterTrial(appraisal, resultsDueConfig);
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
            if (request.get(permRule.getSubmit()) != null) {
                appraisal.setReviewer(loggedInUser);
                appraisal.setReviewSubmitDate(new Date());
            }
        }
        if (request.get("sign-appraisal") != null) {
            appraisal.setEmployeeSignedDate(new Date());
        }
        if (request.get("release-appraisal") != null) {
            appraisal.setReleaseDate(new Date());
        }
        // Save employee response
        if (permRule.getEmployeeResponse() != null && permRule.getEmployeeResponse().equals("e")) {
            appraisal.setRebuttal(request.get("appraisal.rebuttal")[0]);
            String employeeResponse = appraisal.getRebuttal();
            if (submittedRebuttal(request, employeeResponse)) {
                appraisal.setRebuttalDate(new Date());
            }
        }
        // Save supervisor rebuttal read
        if (permRule.getRebuttalRead() != null && permRule.getRebuttalRead().equals("e")
                && request.get("read-appraisal-rebuttal") != null) {
            appraisal.setSupervisorRebuttalRead(new Date());
        }

        // If the appraisalStep object has a new status, update the appraisal object
        String appointmentType = appraisal.getJob().getAppointmentType();
        AppraisalStep appraisalStep = getAppraisalStepKey(request, appointmentType, permRule);
        String newStatus = appraisalStep.getNewStatus();
        if (newStatus != null && !newStatus.equals(appraisal.getStatus())) {
            appraisal.setStatus(newStatus);
            String employeeResponse = appraisal.getRebuttal();
            if (submittedRebuttal(request, employeeResponse)) {
                appraisal.setStatus("rebuttalReadDue");
            }
        }
        if (appraisal.getStatus().equals("goalsRequiredModification")) {
            appraisal.setGoalsRequiredModificationDate(new Date());
        }
    }

    /**
     * Specifies whether or not the employee submitted a rebuttal when the appraisal was signed.
     *
     * @param request
     * @param employeeResponse
     * @return
     */
    private boolean submittedRebuttal(Map<String, String[]> request, String employeeResponse) {
        return request.get("sign-appraisal") != null &&
                employeeResponse != null && !employeeResponse.equals("");
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
     *      id
     *      Job.jobTitle
     *      startDate
     *      endDate
     *      status;
     *
     * @param pidm
     * @return
     * @throws Exception
     */
    public ArrayList<Appraisal> getAllMyActiveAppraisals(int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Appraisal> myActiveAppraisals;
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
    private ArrayList<Appraisal> getAllMyActiveAppraisals(int pidm, Session session) {
        Transaction tx = session.beginTransaction();
        //@todo: the query below should have a where clause => job.employee.id = pidm
        String query = "select new edu.osu.cws.pass.models.Appraisal(id, job.jobTitle, startDate, endDate, status)" +
                " from edu.osu.cws.pass.models.Appraisal where " +
                " job.employee.id = :pidm and status not in ('archived')";
        ArrayList<Appraisal> result = (ArrayList<Appraisal>) session.createQuery(query)
                .setInteger("pidm", pidm)
                .list();
        tx.commit();

        // Check the status of the appraisal and check to see if it needs to be replaced
        for(Appraisal appraisal : result) {
            setAppraisalStatus(appraisal, "employee");
        }
        return result;
    }


    /**
     * Returns a List of team's active appraisals for the given supervisor's pidm.
     *
     * @param pidm
     * @param onlyActive    Whether or not to include only the active appraisals
     * @return
     */
    public List<Appraisal> getMyTeamsAppraisals(Integer pidm, boolean onlyActive) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<Appraisal> teamActiveAppraisals;
        try {
            teamActiveAppraisals = this.getMyTeamsAppraisals(pidm, onlyActive, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return teamActiveAppraisals;
    }

    /**
     * Returns a list of appraisals with limited attributes set: id, job title, employee name,
     * job appointment type, start date, end date, status, goalsRequiredModification and
     * employeSignedDate.
     *
     * @param pidm      Supervisor's pidm.
     * @param onlyActive    Whether or not to include only the active appraisals
     * @param session
     * @return List of Hashmaps that contains the jobs this employee supervises.
     */
    private List<Appraisal> getMyTeamsAppraisals(Integer pidm, boolean onlyActive, Session session) {
        Transaction tx = session.beginTransaction();
        String query = "select new edu.osu.cws.pass.models.Appraisal(id, job.jobTitle, job.employee.lastName, " +
                "job.employee.firstName, job.appointmentType, startDate, endDate, status, " +
                "goalsRequiredModificationDate, employeeSignedDate) " +
                "from edu.osu.cws.pass.models.Appraisal where job.supervisor.employee.id = :pidm ";

        if (onlyActive) {
            query += "and status not in ('archived') ";
        }

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

        //@todo: the admin role below needs tests
        if (admins.containsKey(pidm)) {
            return "admin";
        }

        return "";
    }

    /**
     * Returns the role (employee, supervisor, immediate supervisor or reviewer) of the pidm
     * in the given appraisal. Return empty string if the pidm does not have any role on the
     * appraisal. This method creates a new session and calls the getRole method.
     *
     * @param appraisal     appraisal to check role in
     * @param pidm          pidm of the user to check
     * @return role
     * @throws Exception
     */
    public String getRoleAndSession(Appraisal appraisal, int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String role;

        try {
            Transaction tx = session.beginTransaction();
            role = getRole(appraisal, pidm);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return role;
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
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            appraisal = getAppraisal(id, session);
            tx.commit();

            Job supervisorJob = jobMgr.getSupervisor(appraisal.getJob());
            appraisal.getJob().setCurrentSupervisor(supervisorJob);
        } catch (Exception e) {
            if (session.isOpen()) {
                session.close();
            }
            throw e;
        }


        return appraisal;
    }

    /**
     * Checks the appraisal status and if we need to change the status based on the user role, the status
     * is changed. Right now, if the supervisor submitted the appraisal or hr submitted comments, the status
     * displayed to the user is in review. If the status contains rebuttalRead, we set the status to
     * completed.
     *
     * @param appraisal
     * @param role
     */
    public void setAppraisalStatus(Appraisal appraisal, String role) {
        if (role.equals("employee") && statusHiddenFromEmployee.contains(appraisal.getStatus())) {
            appraisal.setStatus("inReview");
        }

        // Whenever the status is rebuttalReadDue or rebuttalReadOverdue, we set it as completed.
        if (appraisal.getStatus().contains("rebuttalRead")) {
            appraisal.setStatus("completed");
        }
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

    /**
     * Returns an ArrayList of Appraisal which contain data about appraisals pending
     * review. This method is used to display a list of pending reviews in the displayReview
     * actions method. If maxResults > 0, it will limit the number of results.
     *
     * @param businessCenterName
     * @param maxResults
     * @return
     * @throws Exception
     */
    public ArrayList<Appraisal> getReviews(String businessCenterName, int maxResults) throws Exception {
        ArrayList<Appraisal> reviewList;
        Session session = HibernateUtil.getCurrentSession();
        try {
            reviewList = getReviews(businessCenterName, session, maxResults);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return reviewList;
    }


    /**
     * Returns an ArrayList of Appraisal which contain data about appraisals pending
     * review. This method is used to display a list of pending reviews in the displayReview
     * actions method. If maxResults > 0, it will limit the number of results.
     *
     * @param businessCenterName
     * @param session
     * @param maxResults
     * @return
     */
    private ArrayList<Appraisal> getReviews(String businessCenterName, Session session, int maxResults) {
        Transaction tx = session.beginTransaction();
        String query = "select new edu.osu.cws.pass.models.Appraisal(id, job.jobTitle, job.positionNumber, " +
                "startDate, endDate, type, job.employee.id, job.employee.lastName, job.employee.firstName, " +
                "evaluationSubmitDate, status, job.businessCenterName, job.orgCodeDescription, job.suffix) " +
                "from edu.osu.cws.pass.models.Appraisal where job.businessCenterName = :bc " +
                "and status in ('reviewDue', 'reviewOverdue') and job.endDate is NULL";

        Query hibernateQuery = session.createQuery(query)
                .setString("bc", businessCenterName);
        if (maxResults > 0) {
            hibernateQuery.setMaxResults(maxResults);
        }
        ArrayList<Appraisal> result =  (ArrayList<Appraisal>) hibernateQuery.list();
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
                "where status in ('reviewDue', 'reviewOverdue') " +
                "and job.businessCenterName = :bcName")
                .setString("bcName", businessCenterName)
                .list().size();
        tx.commit();
        return count;
    }

    /**
     * Returns a list of appraisals. It searches for all the appraisals of the employee using the
     * osuid. The parameters specify if the logged in user is: admin, supervisor of the business
     * center the logged in user belongs to. It also sets the status of the each appraisal based
     * on the user role.
     *
     * @param osuid OSU ID to use when searching appraisals
     * @param pidm  Pidm of currently logged in user
     * @param isAdmin   Whether or not the logged in user is admin
     * @param isSupervisor Whether or not the logged in user is a supervisor
     * @param bcName Business Center Name of logged in user reviewer
     * @return
     * @throws Exception
     */
    public List<Appraisal> search(int osuid, int pidm, boolean isAdmin, boolean isSupervisor, String bcName)
            throws Exception {
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        Employee employee = employeeMgr.findByOsuid(osuid);
        Job appraisalJob;
        boolean isUpperSupervisorOfAJob = false;
        boolean isUpperSupervisor;
        ArrayList<Job> jobsWhiteList = new ArrayList<Job>();
        List<Appraisal> appraisalsCopy = new ArrayList<Appraisal>();

        if (employee == null) {
            return appraisals;
        }

        if (!isAdmin && isSupervisor) {
            for (Job job : (Set<Job>) employee.getJobs()) {
                isUpperSupervisor = jobMgr.isUpperSupervisor(job, pidm);
                isUpperSupervisorOfAJob = isUpperSupervisorOfAJob || isUpperSupervisor;

                // Keep track of the jobs the pidm user is upper supervisor, so that we can use them to filter out
                // appraisals
                if (isUpperSupervisor || job.getBusinessCenterName().equals(bcName)) {
                    jobsWhiteList.add(job);
                }
            }
        }

        Session session = HibernateUtil.getCurrentSession();
        try {
            appraisals = search(osuid, pidm, isAdmin, isUpperSupervisorOfAJob, bcName, session);
            if (!isAdmin && isSupervisor) {
                // Iterate over the appraisals and add only the ones with jobs in the jobsWhiteList
                for (Appraisal appraisal : appraisals) {
                    for (Job accessibleJobs : jobsWhiteList) {
                        appraisalJob = appraisal.getJob();
                        if (accessibleJobs.getPositionNumber().equals(appraisalJob.getPositionNumber()) &&
                                accessibleJobs.getSuffix().equals(appraisalJob.getSuffix()) &&
                                accessibleJobs.getEmployee().getId() == appraisalJob.getEmployee().getId()
                                ) {
                            appraisalsCopy.add(appraisal);
                        }
                    }
                }
            } else {
                appraisalsCopy = appraisals;
            }

        } catch (Exception e){
            session.close();
            throw e;
        }
        setAppraisalsStatusByRole(pidm, appraisalsCopy);

        return appraisalsCopy;
    }

    /**
     * Iterates over list of appraisal and sets the status based on the role of the user (pidm).
     *
     * @param pidm
     * @param appraisals
     * @throws Exception
     */
    private void setAppraisalsStatusByRole(int pidm, List<Appraisal> appraisals) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Appraisal tempAppraisal;
        try {
            Transaction tx = session.beginTransaction();
            for (Appraisal appraisal : appraisals) {
                tempAppraisal = (Appraisal) session.load(Appraisal.class, appraisal.getId());
                String userRole = getRole(tempAppraisal, pidm);
                setAppraisalStatus(appraisal, userRole);
            }
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

    /**
     * Returns a list of appraisals. It searches for all the appraisals of the employee using the
     * osuid. The parameters specify if the logged in user is: admin, supervisor of the business
     * center the logged in user belongs to.
     *
     * @param osuid
     * @param pidm
     * @param isAdmin
     * @param isUpperSupervisor
     * @param bcName
     * @param session
     * @return
     * @throws Exception
     */
    public List<Appraisal> search(int osuid, int pidm, boolean isAdmin, boolean isUpperSupervisor, String bcName,
                                  Session session) throws Exception {
        List<Appraisal> result = new ArrayList<Appraisal>();
        ArrayList<String> conditions = new ArrayList<String>();

        String query = "select new edu.osu.cws.pass.models.Appraisal (id, job.jobTitle, job.positionNumber, " +
                "startDate, endDate, type, job.employee.id, job.employee.lastName, job.employee.firstName, " +
                "evaluationSubmitDate, status, job.businessCenterName, job.orgCodeDescription, job.suffix) " +
                "from edu.osu.cws.pass.models.Appraisal where job.employee.osuid = :osuid";

        if (!isAdmin) {
            if (bcName != null && !bcName.equals("")) {
                conditions.add("job.businessCenterName = :bcName");
            }
            if (!conditions.isEmpty()) {
                query += " AND ( " + StringUtils.join(conditions, " OR ") + " )";
            }
        }

        Transaction tx = session.beginTransaction();
        Query hibernateQuery = session.createQuery(query).setString("osuid", Integer.toString(osuid));
        if (!conditions.isEmpty()) {
            hibernateQuery.setString("bcName", bcName);
        }

        result =  (ArrayList<Appraisal>) hibernateQuery.list();
        tx.commit();

        return result;
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are due for review for a business center.
     */
    public static int getReviewDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, "reviewDue");
    }

    /**
     * Returns the count of reviews due for a specific business center based on the status.
     *
     * @param bcName
     * @param status
     * @return
     * @throws Exception
     */
    private static int getReviewCountByStatus(String bcName, String status) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int count = 0;
        try {
            Transaction tx = session.beginTransaction();
            String query = "select count(*) "+
                    "from edu.osu.cws.pass.models.Appraisal where job.businessCenterName = :bc " +
                    "and status in (:status) and job.endDate is NULL";

            Object countObj = session.createQuery(query)
                    .setString("bc", bcName)
                    .setString("status", status)
                    .list().get(0);
            count =   Integer.parseInt(countObj.toString());
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
        return count;
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are overdue for review for a business center.
     */
    public static int getReviewOvedDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, "reviewOverdue");
    }

    /**
     * Updates the status of the given appraisal. This does not check permissions or
     * the status String. This is just for demo purposes.
     *
     * @param id
     * @param status
     * @throws Exception
     */
    public void updateAppraisalStatus(int id, String status) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<HashMap> myActiveAppraisals;
        try {
            this.updateAppraisalStatus(id, status, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
    }

    /**
     * Updates the status of the given appraisal. This does not check permissions or
     * the status String. This is just for demo purposes.
     *
     * @param id
     * @param status
     * @param session
     * @throws Exception
     */
    private void updateAppraisalStatus(int id, String status, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
        appraisal.setStatus(status);
        session.update(appraisal);
        tx.commit();
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

    public void setAdmins(HashMap<Integer, Admin> admins) {
        this.admins = admins;
    }

    /**
     * select id from appraisals where status is not completed or closed.
     * @return an array of int containing the appraisalID's of
     * all the appraisal records whose status are not "completed", "closed"
     * or "archived".
     * It now assumes all appraisal records are classified appraisal records.
     * @throws Exception
     */
    public static int[] getOpenIDs() throws Exception {
        int[] ids;
        List result;
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            String query = "select appraisal.id from edu.osu.cws.pass.models.Appraisal appraisal " +
                    "where status not in ('completed', 'closed', 'archived')";

            result =  session.createQuery(query).list();
            ids = new int[result.size()];
            for (int i = 0; i < result.size(); i++) {
                ids[i] = (Integer) result.get(i);
            }
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return ids;
    }

    /**
     *
     * @param job
     * @return  true is a trial appraisal exist for the job, false otherwise
     */
    public static boolean trialAppraisalExists(Job job) throws Exception {
        Date startDate = job.getTrialStartDate();
        return appraisalExists(job, startDate,  Appraisal.TYPE_TRIAL);
    }

    /** select count(*) from appraisals where job.... and startDAte = startDate and type = type
     * @param job: job against which the appraisal was create
     * @param startDate: start date of appraisal period
     * @param type: "trial" or "annual".
     * @return true if an appraisal exist for job and startDate and type, false otherwise
     */
    public static boolean appraisalExists(Job job, Date startDate, String type) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        try {
            Transaction tx = session.beginTransaction();
            String query = "select count(*) from edu.osu.cws.pass.models.Appraisal appraisal " +
                    "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                    "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                    "and appraisal.startDate = :startDate";

            Iterator resultMapIter = session.createQuery(query)
                    .setInteger("pidm", job.getEmployee().getId())
                    .setString("positionNumber", job.getPositionNumber())
                    .setString("suffix", job.getSuffix())
                    .setString("type", type)
                    .setDate("startDate", startDate)
                    .setMaxResults(1)
                    .list().iterator();

            int appraisalCount = 0;
            if (resultMapIter.hasNext()) {
                appraisalCount =  Integer.parseInt(resultMapIter.next().toString());
            }
            tx.commit();
            if (appraisalCount > 0) {
                return true;
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return false;
    }

    /**  @@todo: Jose to implement this
     * @param job
     * @return true if there is a trial appraisal not in the status of "closed", "completed"
     *          or "archived". false otherwise.
     * @throws Exception
     */
    public static boolean openTrialAppraisalExists(Job job) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        try {
            Transaction tx = session.beginTransaction();
            String query = "select count(*) from edu.osu.cws.pass.models.Appraisal appraisal " +
                    "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                    "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                    "and (appraisal.status != 'closed' or  appraisal.status != 'completed' or " +
                    "appraisal.status != 'archived')";

            Iterator resultMapIter = session.createQuery(query)
                    .setInteger("pidm", job.getEmployee().getId())
                    .setString("positionNumber", job.getPositionNumber())
                    .setString("suffix", job.getSuffix())
                    .setString("type", Appraisal.TYPE_TRIAL)
                    .setMaxResults(1)
                    .list().iterator();

            int appraisalCount = 0;
            if (resultMapIter.hasNext()) {
                appraisalCount =  Integer.parseInt(resultMapIter.next().toString());
            }
            tx.commit();
            if (appraisalCount > 0) {
                return true;
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return false;
    }


    /**
     * Updates the appraisal status and originalStatus using the id of the appraisal and hsql query.
     *
     * @param appraisal
     */
    public static void updateAppraisalStatus(Appraisal appraisal) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            String query = "update edu.osu.cws.pass.models.Appraisal appraisal set status = :status, " +
                    "originalStatus = :origStatus where id = :id";
            session.createQuery(query).setString("status", appraisal.getStatus())
                    .setString("origStatus", appraisal.getOriginalStatus())
                    .setInteger("id", appraisal.getId())
                    .executeUpdate();
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
    }
}
