package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.Mailer;
import edu.osu.cws.evals.portlet.Actions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AppraisalMgr {
    //Need to change this back to 11/01/2011 after testing.
    private static final String FULL_GOALS_DATE = "11/01/2011";
    private static Date fullGoalsDate;
    private Employee loggedInUser;

    private Appraisal appraisal = new Appraisal();
    private CriteriaMgr criteriaMgr = new CriteriaMgr();
    private JobMgr jobMgr = new JobMgr();
    private EmployeeMgr employeeMgr = new EmployeeMgr();
    private HashMap appraisalSteps;
    private HashMap permissionRules;
    private HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();
    private HashMap<Integer, Reviewer> reviewers = new HashMap<Integer, Reviewer>();
    private Mailer mailer;
    Map<String, Configuration> configurationMap;
    private PermissionRule permissionRule;



    public AppraisalMgr() {
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        try {
            fullGoalsDate = fmt.parse(FULL_GOALS_DATE);
        } catch (Exception e) {
            //Should not get here
        }
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
        appraisal.setRating(0);

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
        if (startDate.before(fullGoalsDate)) {
            appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        } else if (EvalsUtil.isDue(appraisal, goalsDueConfig) < 0) {
            appraisal.setStatus(Appraisal.STATUS_GOALS_OVERDUE);
        } else {
            appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
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
     * @param resultsDueConfig
     * @return the newly created appraisal
     * @throws Exception
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
        appraisal.setRating(0);

        Date endDate = appraisal.getJob().getEndEvalDate(appraisal.getStartDate(), Appraisal.TYPE_INITIAL);
        appraisal.setEndDate(endDate);

        int resultsDue = EvalsUtil.isDue(appraisal, resultsDueConfig);
        if (appraisal.getStartDate().before(fullGoalsDate)) {
            appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        } else if (resultsDue == 0) {
            appraisal.setStatus(Appraisal.STATUS_RESULTS_DUE);
        } else if (resultsDue < 0) {
            appraisal.setStatus(Appraisal.STATUS_RESULTS_OVERDUE);
        } else {
            appraisal.setStatus(Appraisal.STATUS_GOALS_APPROVED);
        }

        if (appraisal.validate()) {
            Session session = HibernateUtil.getCurrentSession();
            session.save(appraisal);

            Assessment newAssessment;
            for (Assessment origAssesment: trialAppraisal.getAssessments()) {
                newAssessment = new Assessment();
                newAssessment.setCriterionDetail(origAssesment.getCriterionDetail());
                newAssessment.setNewGoals(origAssesment.getNewGoals()); //@todo: Joan: no need to set newGoals.
                newAssessment.setGoal(origAssesment.getGoal());
                newAssessment.setAppraisal(appraisal);
                newAssessment.setCreateDate(new Date());
                newAssessment.setEmployeeResult(origAssesment.getEmployeeResult());
                newAssessment.setSupervisorResult(origAssesment.getSupervisorResult());
                newAssessment.setModifiedDate(new Date());
                session.save(newAssessment);
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
            session.saveOrUpdate(assessment);  //@todo: joan: Do we need to do this everytime?

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
     * @param appraisal
     * @param permRule
     * @throws Exception
     */
    public void processUpdateRequest(Map request, Appraisal appraisal, PermissionRule permRule)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Configuration resultsDueConfig = configurationMap.get(Appraisal.STATUS_RESULTS_DUE);

        Job supervisorJob = appraisal.getJob().getSupervisor();

        // update appraisal & assessment fields based on permission rules
        setAppraisalFields(request, appraisal, permRule);

        //@todo: validate appraisal

        // save changes to db
        updateAppraisal(appraisal);

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
    }

    /**
     * Creates the first annual appraisal if needed. The first annual appraisal is created if:
     *  1) The current appraisal is of type: trial
     *  2) the action is sign-appraisal
     *  3) The job annual_indicator != 0
     *
     * @param appraisal
     * @param configurationMap
     * @param action
     * @throws Exception
     * @return appraisal    The first annual appraisal created, null otherwise
     */
    public Appraisal createFirstAnnualAppraisal(Appraisal appraisal,
                                                Map<String, Configuration>  configurationMap, String action)
            throws Exception {
        Job job = appraisal.getJob();
        Configuration resultsDueConfig = configurationMap.get(Appraisal.STATUS_RESULTS_DUE);

        if (!appraisal.getType().equals(Appraisal.TYPE_TRIAL)) {
            return null;
        }
        if (!action.equals("sign-appraisal")) {
            return null;
        }
        if (job.getAnnualInd() == 0) {
            return null;
        }
        Date startDate = appraisal.getStartDate();
        int daysBeforeAppraisalDue = EvalsUtil.daysBeforeAppraisalDue(job, startDate, Appraisal.TYPE_ANNUAL,
                configurationMap);
        if (startDate.before(fullGoalsDate)  &&
                daysBeforeAppraisalDue > Constants.DAYS_BEFORE_APPRAISAL_DUE_To_CREATE) {
            return null;
        }
        return AppraisalMgr.createInitialAppraisalAfterTrial(appraisal, resultsDueConfig);
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

        // Save the close out reason
        if (appraisal.getRole().equals(Actions.ROLE_REVIEWER) || appraisal.getRole().equals("admin")) {
            if (request.get("appraisal.closeOutReasonId") != null) {
                int closeOutReasonId = Integer.parseInt(request.get("appraisal.closeOutReasonId")[0]);
                CloseOutReason reason = CloseOutReasonMgr.get(closeOutReasonId);

                appraisal.setCloseOutBy(loggedInUser);
                appraisal.setCloseOutDate(new Date());
                appraisal.setCloseOutReason(reason);
                appraisal.setOriginalStatus(appraisal.getStatus());
            }
        }

        // If the appraisalStep object has a new status, update the appraisal object
        String appointmentType = appraisal.getJob().getAppointmentType();
        AppraisalStep appraisalStep = getAppraisalStepKey(request, appointmentType, permRule);
        String newStatus = appraisalStep.getNewStatus();
        if (newStatus != null && !newStatus.equals(appraisal.getStatus())) {
            appraisal.setStatus(newStatus);
            String employeeResponse = appraisal.getRebuttal();
            if (submittedRebuttal(request, employeeResponse)) {
                appraisal.setStatus(Appraisal.STATUS_REBUTTAL_READ_DUE);
            }
        }
        if (appraisal.getStatus().equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)) {
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
        // close out button
        appraisalButtons.add("close-appraisal");

        for (String button : appraisalButtons) {
            // If this button is the one the user clicked, use it to look up the
            // appraisalStepKey
            if (request.get(button) != null) {
                appraisalStepKey = button + "-" + appointmentType;
                appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
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

        PermissionRule permissionRule = (PermissionRule) permissionRules.get(permissionKey);
        if (permissionRule != null &&  appraisal.getStartDate().before(fullGoalsDate)) {
            permissionRule.setGoals(null);
            permissionRule.setResults(null);
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
        return this.getAllMyActiveAppraisals(pidm, session);
    }

    /**
     * Returns a HashMap
     * @param pidm
     * @param session
     * @return
     */
    private ArrayList<Appraisal> getAllMyActiveAppraisals(int pidm, Session session) {
        //@todo: the query below should have a where clause => job.employee.id = pidm
        String query = "select new edu.osu.cws.evals.models.Appraisal(id, job.jobTitle, startDate, endDate, status)" +
                " from edu.osu.cws.evals.models.Appraisal where " +
                " job.employee.id = :pidm and status not in ('archived')";
        ArrayList<Appraisal> result = (ArrayList<Appraisal>) session.createQuery(query)
                .setInteger("pidm", pidm)
                .list();

        // Check the status of the appraisal and check to see if it needs to be replaced
        for(Appraisal appraisal : result) {
            appraisal.setRole("employee");
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
        return this.getMyTeamsAppraisals(pidm, onlyActive, session);
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
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        List<Integer> pidms = new ArrayList<Integer>();

        String query = "select ap.ID, jobs.PYVPASJ_DESC, jobs.PYVPASJ_APPOINTMENT_TYPE, " +
                "ap.START_DATE, ap.END_DATE, ap.STATUS, ap.GOALS_REQUIRED_MOD_DATE, " +
                "ap.EMPLOYEE_SIGNED_DATE, jobs.PYVPASJ_PIDM " +
                "FROM appraisals ap, PYVPASJ jobs " +
                "WHERE ap.JOB_PIDM=jobs.PYVPASJ_PIDM AND ap.POSITION_NUMBER=jobs.PYVPASJ_POSN " +
                "AND ap.JOB_SUFFIX=jobs.PYVPASJ_SUFF AND jobs.PYVPASJ_SUPERVISOR_PIDM=:pidm ";

        if (onlyActive) {
            query += "AND status NOT IN ('archived') ";
        }

        List<Object[]> result =  session.createSQLQuery(query)
                .addScalar("ID", StandardBasicTypes.INTEGER)
                .addScalar("PYVPASJ_DESC", StandardBasicTypes.STRING)
                .addScalar("PYVPASJ_APPOINTMENT_TYPE", StandardBasicTypes.STRING)
                .addScalar("START_DATE", StandardBasicTypes.DATE)
                .addScalar("END_DATE", StandardBasicTypes.DATE)
                .addScalar("STATUS", StandardBasicTypes.STRING)
                .addScalar("GOALS_REQUIRED_MOD_DATE", StandardBasicTypes.DATE)
                .addScalar("EMPLOYEE_SIGNED_DATE", StandardBasicTypes.DATE)
                .addScalar("PYVPASJ_PIDM", StandardBasicTypes.INTEGER)
                .setInteger("pidm", pidm).list();

        if (result.isEmpty()) {
            return appraisals;
        }

        // Build list of appraisals from sql results
        for (Object[] aResult : result) {
            Integer id = (Integer) aResult[0];
            String jobTitle = (String) aResult[1];
            String appointmentType = (String) aResult[2];
            Date startDate = (Date) aResult[3];
            Date endDate = (Date) aResult[4];
            String status = (String) aResult[5];
            Date goalsReqModDate = (Date) aResult[6];
            Date employeeSignDate = (Date) aResult[7];
            Integer employeePidm = (Integer) aResult[8];

            appraisal = new Appraisal(id, jobTitle, null, null, appointmentType,
                    startDate, endDate, status, goalsReqModDate, employeeSignDate, employeePidm);
            appraisals.add(appraisal);
            pidms.add(employeePidm);
        }

        List<Employee> employees = session.getNamedQuery("employee.firstAndLastNameByPidm")
                .setParameterList("ids", pidms).list();
        HashMap<Integer, Employee> employeeHashMap = new HashMap<Integer, Employee>();
        for (Employee employee : employees) {
            employeeHashMap.put(employee.getId(), employee);
        }

        for (Appraisal ap : appraisals) {
            Integer employeePidm = ap.getJob().getEmployee().getId();
            Employee employee = employeeHashMap.get(employeePidm);
            ap.getJob().setEmployee(employee);
        }
        return appraisals;
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
        if (appraisal.getRole() != null && !appraisal.getRole().equals("")) {
            return appraisal.getRole();
        }

        Session session = HibernateUtil.getCurrentSession();
        Job supervisor;
        if (pidm == appraisal.getJob().getEmployee().getId()) {
            appraisal.setRole("employee");
            return appraisal.getRole();
        }

        supervisor = appraisal.getJob().getSupervisor();
        if (supervisor != null && pidm == supervisor.getEmployee().getId()) {
            appraisal.setRole("supervisor");
            return appraisal.getRole();
        }

        String query = "from edu.osu.cws.evals.models.Reviewer where " +
                "businessCenterName = :businessCenterName and employee.id = :pidm " +
                "and employee.status = 'A'";
        List reviewerList = session.createQuery(query)
                .setString("businessCenterName", appraisal.getJob().getBusinessCenterName())
                .setInteger("pidm", pidm)
                .list();

        if (reviewerList.size() != 0) {
            appraisal.setRole("reviewer");
            return appraisal.getRole();
        }

        if (jobMgr.isUpperSupervisor(appraisal.getJob(), pidm)) {
            appraisal.setRole("upper-supervisor");
            return appraisal.getRole();
        }

        //@todo: the admin role below needs tests
        if (admins.containsKey(pidm)) {
            appraisal.setRole("admin");
            return appraisal.getRole();
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
        Session session = HibernateUtil.getCurrentSession();
        appraisal = getAppraisal(id, session);

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
        Session session = HibernateUtil.getCurrentSession();
        return getReviews(businessCenterName, session, maxResults);
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
    //@todo: Joan: don't do anything with job.endDate, that's not reliable.
    private ArrayList<Appraisal> getReviews(String businessCenterName, Session session, int maxResults) {
        Query hibernateQuery = session.getNamedQuery("appraisal.getReviews")
                .setString("bc", businessCenterName);
        //@todo: Joan: can we set the maxResults before querying the database?
        if (maxResults > 0) {
            hibernateQuery.setMaxResults(maxResults);
        }
        ArrayList<Appraisal> result =  (ArrayList<Appraisal>) hibernateQuery.list();
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
        Session session = HibernateUtil.getCurrentSession();
        return getReviewCount(businessCenterName, session);
    }

    private int getReviewCount(String businessCenterName, Session session) {
        int reviewCount = 0;

        List results = session.getNamedQuery("appraisal.reviewCount")
                .setString("bcName", businessCenterName)
                .list();
        if (!results.isEmpty()) {
            reviewCount = Integer.parseInt(results.get(0).toString());
        }
        return reviewCount;
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are due for review for a business center.
     */
    public static int getReviewDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, Appraisal.STATUS_REVIEW_DUE);
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
        String query = "select count(*) "+
                "from edu.osu.cws.evals.models.Appraisal where job.businessCenterName = :bc " +
                "and status in (:status) and job.endDate is NULL";

        Object countObj = session.createQuery(query)
                .setString("bc", bcName)
                .setString("status", status)
                .list().get(0);

        return Integer.parseInt(countObj.toString());
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are overdue for review for a business center.
     */
    public static int getReviewOvedDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, Appraisal.STATUS_REVIEW_OVERDUE);
    }

    /**
     * Updates the status of the given appraisal. This does not check permissions or
     * the status String.
     *
     * @param id
     * @param status
     * @throws Exception
     */
    public void updateAppraisalStatus(int id, String status) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<HashMap> myActiveAppraisals;
        this.updateAppraisalStatus(id, status, session);
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
        Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
        appraisal.setStatus(status);
        session.update(appraisal);
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

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public void setConfigurationMap(Map<String, Configuration> configurationMap) {
        this.configurationMap = configurationMap;
    }

    public void setReviewers(HashMap<Integer, Reviewer> reviewers) {
        this.reviewers = reviewers;
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
        String query = "select appraisal.id from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where status not in ('completed', 'closed', 'archived')";

        result =  session.createQuery(query).list();
        ids = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            ids[i] = (Integer) result.get(i);
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

        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal " +
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
        return (appraisalCount > 0);
    }

    /**  @@todo: Jose to implement this
     * @param job
     * @return true if there is a trial appraisal not in the status of "closed", "completed"
     *          or "archived". false otherwise.
     * @throws Exception
     */
    public static boolean openTrialAppraisalExists(Job job) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                "and (appraisal.status != 'closed' AND  appraisal.status != 'completed' AND " +
                "appraisal.status != 'archived')";

        Iterator resultMapIter = session.createQuery(query)
                .setInteger("pidm", job.getEmployee().getId())
                .setString("positionNumber", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .setString("type", Appraisal.TYPE_TRIAL)   //@todo: Joan: it's always going to be trial, right?
                .setMaxResults(1)  //@todo: Joan: No need to set maxResuls?
                .list().iterator();

        int appraisalCount = 0;
        if (resultMapIter.hasNext()) {
            appraisalCount =  Integer.parseInt(resultMapIter.next().toString());
        }
        return (appraisalCount > 0);
    }

    /**
     * Updates the appraisal status and originalStatus using the id of the appraisal and hsql query.
     *
     * @param appraisal
     */
    public static void updateAppraisalStatus(Appraisal appraisal) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "update edu.osu.cws.evals.models.Appraisal appraisal set status = :status, " +
                "originalStatus = :origStatus where id = :id";
        session.createQuery(query).setString("status", appraisal.getStatus())
                .setString("origStatus", appraisal.getOriginalStatus())
                .setInteger("id", appraisal.getId()) //@todo: Joan: No need to set ID.
                .executeUpdate();

    }


    /**
     * Get all the appraisals first using job-pidm, and then filter through to remove
     * extra appraisals for reviewer and supervisor.
     * @param osuid     osu id of the employee's appraisals we are searching
     * @param pidm      pidm of the logged in user
     * @param isAdmin
     * @param isSupervisor
     * @param bcName
     * @return
     * @throws Exception
     */
    public List<Appraisal> search(int osuid, int pidm, boolean isAdmin, boolean isSupervisor, String bcName)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        Set<Job> notBelongJobs = new HashSet<Job>();//jobs the logged in user has no access
        Set<Job> belongJobs = new HashSet<Job>(); //jobs the logged in user has access

        Employee employee = employeeMgr.findByOsuid(osuid);
        if (employee == null) {
            return appraisals;
        }

        int searchUserID = employee.getId(); //pidm of the employee we are searching for.
        Query hibernateQuery = session.getNamedQuery("appraisal.search")
                .setInteger("pidm", searchUserID);

        List<Appraisal> appraisalsTemp =  (ArrayList<Appraisal>) hibernateQuery.list();

        if (isAdmin) //Admin gets to see all, so no filtering needed
            return appraisalsTemp;

        for (Appraisal appraisal : appraisalsTemp) {
            Job appJob = appraisal.getJob();
            if (belongJobs.contains(appJob) || notBelongJobs.contains(appJob)) {
                if (belongJobs.contains(appJob)) {
                    appraisals.add(appraisal);
                }
                continue;
            }

            //If we get here, we haven't checked this job yet.
            if (bcName != null && !bcName.equals("")) { //reviewer
               if (appJob.getBusinessCenterName().equals(bcName)) {
                  appraisals.add(appraisal);
                  belongJobs.add(appJob);
               }
               else {
                  notBelongJobs.add(appJob);
               }
            } else if (isSupervisor) {
                // Fetch the appraisal job from the db because the isSupervisor method needs to
                // traverse up the supervising chain.
                //Employee employee1 = new Employee(appJob.getEmployee().getId());
                //Job dbJob = new Job(employee, appJob.getPositionNumber(), appJob.getSuffix());
                Job job = (Job) session.load(Job.class, appJob);
                if (jobMgr.isUpperSupervisor(job, pidm)) {
                    appraisals.add(appraisal);
                    belongJobs.add(appJob);
                } else {
                    notBelongJobs.add(appJob);
                }
            }
        }

        return appraisals;

    }
}