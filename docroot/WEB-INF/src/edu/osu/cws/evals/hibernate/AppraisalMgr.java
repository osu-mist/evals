package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.Mailer;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;

public class AppraisalMgr {

    // used to sort the list of evaluations displayed during searches
    private static final String LIST_ORDER = " order by job.employee.lastName, " +
            "job.employee.firstName, startDate";

    // Constants that specify appraisal list for report
    private static final String REPORT_LIST_SELECT = "select new edu.osu.cws.evals.models.Appraisal " +
            "(id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
            " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
            " from edu.osu.cws.evals.models.Appraisal ";
    private static final String REPORT_LIST_WHERE = " where status not in ('completed', 'archived', " +
            "'closed') ";

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

    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job   Job for this appraisal
     * @param type: trial, annual, initial
     * @param startDate: (DateTime) starting date of appraisal period.
     * @return appraisal.id
     * @throws Exception
     */
    public static Appraisal createAppraisal(Job job, DateTime startDate, String type)
            throws Exception {
        CriteriaMgr criteriaMgr = new CriteriaMgr();
        Appraisal appraisal = new Appraisal();
        CriterionDetail detail;
        Assessment assessment;

        if (!type.equals(Appraisal.TYPE_TRIAL) && !type.equals(Appraisal.TYPE_ANNUAL) &&
                !type.equals(Appraisal.TYPE_INITIAL)) {
            throw new ModelException("Invalid appraisal type : " + type);
        }

        appraisal.setJob(job);
        appraisal.setStartDate(startDate.toDate());
        appraisal.setCreateDate(new Date());
        appraisal.setRating(0);
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);

        // In the db, we only store: annual or trial.
        String dbType = type;
        if (type.equals(Appraisal.TYPE_INITIAL)) {
            dbType = Appraisal.TYPE_ANNUAL;
        }
        appraisal.setType(dbType);

        DateTime endDate = job.getEndEvalDate(startDate, type);
        appraisal.setEndDate(CWSUtil.toDate(endDate));

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
     * @param startDate         DateTime object
     * @param goalsDueConfig
     * @param appraisal
     * @throws Exception
     */
    private static void createAppraisalStatus(DateTime startDate, Configuration goalsDueConfig,
                                              Appraisal appraisal) throws Exception {
        if (EvalsUtil.isDue(appraisal, goalsDueConfig) < 0) {
            appraisal.setStatus(Appraisal.STATUS_GOALS_OVERDUE);
        } else {
            appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        }
    }

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
        Date initialEvalStartDate = appraisal.getJob().getInitialEvalStartDate().toDate();
        appraisal.setStartDate(initialEvalStartDate);
        appraisal.setGoalsSubmitDate(trialAppraisal.getGoalsSubmitDate());
        appraisal.setGoalsApprover(trialAppraisal.getGoalsApprover());
        appraisal.setGoalApprovedDate(trialAppraisal.getGoalApprovedDate());
        appraisal.setRating(0);

        DateTime startDate = new DateTime(appraisal.getStartDate());
        DateTime endDate = appraisal.getJob().getEndEvalDate(startDate, Appraisal.TYPE_INITIAL);
        appraisal.setEndDate(CWSUtil.toDate(endDate));

        int resultsDue = EvalsUtil.isDue(appraisal, resultsDueConfig);
        if (resultsDue == 0) {
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

        // set the overdue value before updating the status
        String beforeUpdateStatus = appraisal.getStatus();
        // calculate overdue value & set the appraisal.overdue value
        AppraisalMgr.updateOverdue(appraisal, configurationMap);
        int oldOverdue = appraisal.getOverdue();

        // update appraisal & assessment fields based on permission rules
        setAppraisalFields(request, appraisal, permRule);

        boolean statusChanged = !appraisal.getStatus().equals(beforeUpdateStatus);
        if (statusChanged) {
            // Using the old status value call setStatusOverdue()
            String overdueMethod = StringUtils.capitalize(beforeUpdateStatus);
            overdueMethod = "set" + overdueMethod.replace("Due", "Overdue");
            try {
                // call setStageOverdue method
                Method controllerMethod = appraisal.getClass().getDeclaredMethod(overdueMethod,
                        Integer.class);
                controllerMethod.invoke(appraisal, oldOverdue);
            } catch (NoSuchMethodException e) {
                // don't do anything since some methods might not exist.
            }

            // Assign the new status based on configuration values
            String status = appraisal.getStatus();
            String newStatus = AppraisalMgr.getNewStatus(status, appraisal, configurationMap);
            if (newStatus != null) {
                appraisal.setStatus(newStatus);
            }

            // If the new status is valid for overdue, refresh the overdue value
            if (appraisal.getStatus().contains(Appraisal.OVERDUE)) {
                AppraisalMgr.updateOverdue(appraisal, configurationMap);
            } else {
                appraisal.setOverdue(-999);
            }
        }


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
     *  2) The job annual_indicator != 0
     *
     * @param trialAppraisal
     * @param configurationMap
     * @throws Exception
     * @return appraisal    The first annual appraisal created, null otherwise
     */
    public static Appraisal createFirstAnnualAppraisal(Appraisal trialAppraisal,
                                                Map<String, Configuration>  configurationMap)
            throws Exception {
        Job job = trialAppraisal.getJob();
        Configuration resultsDueConfig = configurationMap.get(Appraisal.STATUS_RESULTS_DUE);

        if (!trialAppraisal.getType().equals(Appraisal.TYPE_TRIAL)) {
            return null;
        }
        if (job.getAnnualInd() == 0) {
            return null;
        }
        return AppraisalMgr.createInitialAppraisalAfterTrial(trialAppraisal, resultsDueConfig);
    }

    /**
     * Returns the first trial appraisal for the given job.
     *
     * @param job
     * @return trialAppraisal
     */
    public static Appraisal getTrialAppraisal(Job job) {
        Session session = HibernateUtil.getCurrentSession();

        Appraisal trialAppraisal = (Appraisal) session.getNamedQuery("appraisal.getTrialAppraisal")
                .setInteger("pidm", job.getEmployee().getId())
                .setString("posno", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .uniqueResult();
        return trialAppraisal;
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
        if (appraisal.getRole().equals(ActionHelper.ROLE_REVIEWER) || appraisal.getRole().equals("admin")) {
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

        return permissionRule;
    }

    /**
     * Returns a list of active appraisals for all the jobs that the current pidm holds. If the
     * posno and suffix are provided, the evaluations are specific to the job.
     * The fields that are returned in the appraisal are:
     *      id
     *      Job.jobTitle
     *      startDate
     *      endDate
     *      status
     *      overdue
     *
     * @return
     * @param pidm
     * @param posno
     * @param suffix
     */
    public static ArrayList<Appraisal> getAllMyActiveAppraisals(int pidm, String posno,
                                                                String suffix) {
        Session session = HibernateUtil.getCurrentSession();
        String query = "select new edu.osu.cws.evals.models.Appraisal(id, job.jobTitle, startDate, " +
                "endDate, status, overdue)" +
                " from edu.osu.cws.evals.models.Appraisal where " +
                " job.employee.id = :pidm and status not in ('archived')";

        if (posno != null && suffix != null) {
            query += " and job.positionNumber = :posno and job.suffix = :suffix";
        }

        Query hibQuery = session.createQuery(query).setInteger("pidm", pidm);
        if (posno != null && suffix != null) {
            hibQuery.setString("posno", posno).setString("suffix", suffix);
        }

        ArrayList<Appraisal> result = (ArrayList<Appraisal>) hibQuery.list();

        // Check the status of the appraisal and check to see if it needs to be replaced
        for(Appraisal appraisal : result) {
            appraisal.setRole("employee");
        }
        return result;
    }

    /**
     * Returns a list of appraisals with limited attributes set: id, job title, employee name,
     * job appointment type, start date, end date, status, goalsRequiredModification and
     * employeSignedDate. If the posno and suffix are specific, the team appraisals are
     * specific to that supervising job.
     *
     * @param pidm          Supervisor's pidm.
     * @param onlyActive    Whether or not to include only the active appraisals
     * @param posno         Supervisor's posno
     * @param suffix        Supervisor's suffix
     * @return List of Appraisal that contains the jobs this employee supervises.
     */
    public static ArrayList<Appraisal> getMyTeamsAppraisals(Integer pidm, boolean onlyActive,
                                                 String posno, String suffix) {
        ArrayList<Appraisal> appraisals = new ArrayList<Appraisal>();
        List<Integer> pidms = new ArrayList<Integer>();
        Session session = HibernateUtil.getCurrentSession();

        String query = "select ap.ID, jobs.PYVPASJ_DESC, jobs.PYVPASJ_APPOINTMENT_TYPE, " +
                "ap.START_DATE, ap.END_DATE, ap.STATUS, ap.GOALS_REQUIRED_MOD_DATE, " +
                "ap.EMPLOYEE_SIGNED_DATE, jobs.PYVPASJ_PIDM, ap.OVERDUE " +
                "FROM appraisals ap, PYVPASJ jobs " +
                "WHERE ap.JOB_PIDM=jobs.PYVPASJ_PIDM AND ap.POSITION_NUMBER=jobs.PYVPASJ_POSN " +
                "AND ap.JOB_SUFFIX=jobs.PYVPASJ_SUFF AND jobs.PYVPASJ_SUPERVISOR_PIDM=:pidm ";

        if (!StringUtils.isEmpty(posno) && !StringUtils.isEmpty(suffix)) {
            query += "AND jobs.PYVPASJ_SUPERVISOR_POSN=:posno " +
                    "AND jobs.PYVPASJ_SUPERVISOR_SUFF = :suffix ";
        }

        if (onlyActive) {
            query += "AND status NOT IN ('archived') ";
        }

        Query hibQuery = session.createSQLQuery(query)
                .addScalar("ID", StandardBasicTypes.INTEGER)
                .addScalar("PYVPASJ_DESC", StandardBasicTypes.STRING)
                .addScalar("PYVPASJ_APPOINTMENT_TYPE", StandardBasicTypes.STRING)
                .addScalar("START_DATE", StandardBasicTypes.DATE)
                .addScalar("END_DATE", StandardBasicTypes.DATE)
                .addScalar("STATUS", StandardBasicTypes.STRING)
                .addScalar("GOALS_REQUIRED_MOD_DATE", StandardBasicTypes.DATE)
                .addScalar("EMPLOYEE_SIGNED_DATE", StandardBasicTypes.DATE)
                .addScalar("PYVPASJ_PIDM", StandardBasicTypes.INTEGER)
                .addScalar("OVERDUE", StandardBasicTypes.INTEGER)
                .setInteger("pidm", pidm);
        if (!StringUtils.isEmpty(posno) && !StringUtils.isEmpty(suffix)) {
            hibQuery.setString("posno", posno).setString("suffix", suffix);
        }
        List<Object[]> result =  hibQuery.list();

        if (result.isEmpty()) {
            return appraisals;
        }

        // Build list of appraisals from sql results
        for (Object[] aResult : result) {
           Appraisal appraisal;
            Integer id = (Integer) aResult[0];
            String jobTitle = (String) aResult[1];
            String appointmentType = (String) aResult[2];
            Date startDate = (Date) aResult[3];
            Date endDate = (Date) aResult[4];
            String status = (String) aResult[5];
            Date goalsReqModDate = (Date) aResult[6];
            Date employeeSignDate = (Date) aResult[7];
            Integer employeePidm = (Integer) aResult[8];
            Integer overdue = (Integer) aResult[9];

            appraisal = new Appraisal(id, jobTitle, null, null, appointmentType,
                    startDate, endDate, status, goalsReqModDate, employeeSignDate, employeePidm,
                    overdue);
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

        ArrayList<Appraisal> myTeamAppraisals = new ArrayList<Appraisal>();
        if (appraisals != null) {
            for (Appraisal appraisal : appraisals) {
                appraisal.setRole("supervisor");
                myTeamAppraisals.add(appraisal);
            }
        }

        return myTeamAppraisals;
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
     * It returns the appraisal that matches the id.
     * It also adds the currentSupervisor to the appraisal object.
     *
     * @param id
     * @return Appraisal
     * @throws Exception
     */
    public Appraisal getAppraisal(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
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
    //@todo: Joan: don't do anything with job.endDate, that's not reliable.
    public ArrayList<Appraisal> getReviews(String businessCenterName, int maxResults) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
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
        int reviewCount = 0;
        Session session = HibernateUtil.getCurrentSession();
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
        DateTime startDate = job.getTrialStartDate();
        if (startDate == null) {
            return false;
        }
        return appraisalExists(job, startDate,  Appraisal.TYPE_TRIAL);
    }

    /** select count(*) from appraisals where job.... and startDAte = startDate and type = type
     * @param job: job against which the appraisal was create
     * @param startDate: (DateTime) start date of appraisal period
     * @param type: "trial" or "annual".
     * @return true if an appraisal exist for job and startDate and type, false otherwise
     */
    public static boolean appraisalExists(Job job, DateTime startDate, String type) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        startDate = startDate.minusMonths(6);
        Date beginDate = startDate.toDate();
        Date endDate = startDate.plusMonths(12).toDate();

        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                "and appraisal.startDate >= :beginDate and appraisal.startDate <= :endDate";

        Iterator resultMapIter = session.createQuery(query)
                .setInteger("pidm", job.getEmployee().getId())
                .setString("positionNumber", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .setString("type", type)
                .setDate("beginDate", beginDate)
                .setDate("endDate", endDate)
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
     * @param job
     * @param appraisalStartDate    DateTime object
     * @return
     * @throws Exception
     */
    public static boolean AnnualExists(Job job, DateTime appraisalStartDate) throws Exception
    {
        if (AppraisalMgr.appraisalExists(job, appraisalStartDate, Appraisal.TYPE_ANNUAL))
            return true;

        //If we get here, there is no record for the job for appraisalStartDate
        //It's possible that someone added a value for Pyvpasj_eval_date after we created
        //the previous appraisal, so need to check that.
        int thisYear = appraisalStartDate.getYear();
        DateTime startDateBasedOnJobBeginDate = job.getAnnualStartDateBasedOnJobBeginDate(thisYear);

        return !startDateBasedOnJobBeginDate.equals(appraisalStartDate) &&
                AppraisalMgr.appraisalExists(job, startDateBasedOnJobBeginDate, Appraisal.TYPE_ANNUAL);
    }


    /**
     * Updates the appraisal status and originalStatus using the id of the appraisal and hsql query.
     *
     * @param appraisal
     * @throws Exception
     */
    public static void updateAppraisalStatus(Appraisal appraisal) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "update edu.osu.cws.evals.models.Appraisal appraisal " +
                "set status = :status, originalStatus = :origStatus where id = :id";

        session.createQuery(query)
                .setString("status", appraisal.getStatus())
                .setString("origStatus", appraisal.getOriginalStatus())
                .setInteger("id", appraisal.getId())
                .executeUpdate();
    }


    /**
     * First get the jobs that match the search criteria. Then we get the list of appraisals
     * using the jobs in the where clause.
     *
     * @param searchTerm    osu id of or name the employee's appraisals we are searching
     * @param pidm          pidm of the logged in user
     * @param isAdmin
     * @param isSupervisor
     * @param bcName
     * @return
     * @throws Exception
     */
    public List<Appraisal> search(String searchTerm, int pidm, boolean isAdmin,
                                  boolean isSupervisor, String bcName) throws Exception {
        List<Job> jobs;

        if (!isAdmin) {
            int supervisorPidm = 0;
            if (isSupervisor) {
                supervisorPidm = pidm;
            }
            jobs = JobMgr.search(searchTerm, bcName, supervisorPidm);
        } else {
            jobs = JobMgr.search(searchTerm, "", 0);
        }

        String hql = "select new edu.osu.cws.evals.models.Appraisal ( " +
            "id, job.jobTitle, job.positionNumber, startDate, endDate, type, " +
            "job.employee.id, job.employee.lastName, job.employee.firstName, " +
            "evaluationSubmitDate, status, job.businessCenterName, " +
            "job.orgCodeDescription, job.suffix, overdue) from " +
            "edu.osu.cws.evals.models.Appraisal ";

        return getAppraisalsByJobs(jobs, hql, null);
    }

    /**
     * Returns a list of appraisals that are retrieved based on the jobs. These are used by
     * by appraisal.search and report search of employee appraisals.
     *
     * @param jobs
     * @param hql
     * @param conditions
     * @return
     * @throws Exception
     */
    private static List<Appraisal> getAppraisalsByJobs(List<Job> jobs, String hql,
                                                       List<String> conditions)
            throws Exception {
        // The list of jobs contains the pidm, posno and suff that match the search criteria and
        // the permission level of the logged in user.
        if (jobs == null || jobs.isEmpty()) {
            return new ArrayList<Appraisal>();
        }

        Session session = HibernateUtil.getCurrentSession();
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        if (conditions == null) {
            conditions = new ArrayList<String>();
        }

        List<String> jobConditions = new ArrayList<String>();
        for (int i = 0; i < jobs.size(); i++) {
            String cond = "(job.employee.id = :pidm" + i + " and job.positionNumber = :posno" + i +
                    " and job.suffix = :suff"+i+")";
            jobConditions.add(cond);
        }

        // add the job pidm, posno and suffix to the hql query to only get the appraisals we need.
        if (!jobConditions.isEmpty()) {
            String jobJoinClause = StringUtils.join(jobConditions, " or ");
            conditions.add(jobJoinClause);
        }

        if (!conditions.isEmpty()) {
            hql += " where " + StringUtils.join(conditions, " and ") + " ";
        }

        hql += LIST_ORDER;
        Query query = session.createQuery(hql);

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            query.setInteger("pidm"+i, job.getEmployee().getId())
                    .setString("posno"+i, job.getPositionNumber())
                    .setString("suff"+i, job.getSuffix());
        }

        List<Appraisal> temp =  (ArrayList<Appraisal>) query.list();

        appraisals = addSupervisorToAppraisals(temp);
        return appraisals;
    }

    /**
     * Add the supervisor to the list of appraisal pojo. This is so that the jsp is able
     * to handle with job's not having supervisors.
     *
     * @param source
     * @return
     * @throws Exception
     */
    private static List<Appraisal> addSupervisorToAppraisals(List<Appraisal> source)
            throws Exception {
        List<Appraisal> destination = new ArrayList<Appraisal>();

        // Add supervisor pojo to the appraisal list
        for (Appraisal appraisal : source) {
            Job job = appraisal.getJob();
            Job tempJob = JobMgr.addSupervisorToJob(job);
            appraisal.setJob(tempJob);

            // Copy the appraisal pojo and fields to handle missing employee/job
            Appraisal nonDbAppraisal = new Appraisal(appraisal);
            destination.add(nonDbAppraisal);
        }

        return destination;
    }


    /**
     * This method is used by ReportsAction to display the list of evaluations for a given set of
     * jobs.
     *
     * @param jobs
     * @return
     */
    public static List<Appraisal> getEmployeeAppraisalList(List<Job> jobs) throws Exception {
        ArrayList<String> conditions = new ArrayList<String>();
        String hql = REPORT_LIST_SELECT;
        conditions.add("status not in ('completed', 'archived', 'closed')");

        return getAppraisalsByJobs(jobs, hql, conditions);
    }


    /**
     * Returns the data needed to generate the google charts and nothing more.
     *
     * @param paramMap
     * @param directSupervisors
     * @param inLeafSupervisor
     * @return
     */
    public static List<Appraisal> getReportListData(HashMap paramMap, List<Job> directSupervisors,
                                                    boolean inLeafSupervisor) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        List<Appraisal> results = new ArrayList<Appraisal>();
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = (String) paramMap.get(Constants.BC_NAME);
        boolean addBC = !StringUtils.isEmpty(bcName);

        String hqlQuery = getReportListHQL(scope, report, addBC);
        Query listQuery = session.createQuery(hqlQuery)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);

        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            listQuery.setParameter("tsOrgCode", scopeValue);

            // only set the bcName if it's not empty. if an admin searches, the bcName is empty
            if (addBC) {
                listQuery.setParameter("bcName", bcName);
            }

            results = (ArrayList<Appraisal>) listQuery.list();
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            List<Integer> appraisalIds = getAppraisalIdsForSupervisorReport(directSupervisors,
                    inLeafSupervisor);

            if (!appraisalIds.isEmpty()) {
                results = (ArrayList<Appraisal>) listQuery
                        .setParameterList("appraisalIds", appraisalIds)
                        .list();
            }
        } else {
            results = (ArrayList<Appraisal>) listQuery
                    .list();
        }

        return AppraisalMgr.addSupervisorToAppraisals(results);
    }

    /**
     * Returns the hql needed to display the list of evaluation records in the reports.
     * The data is sorted by lastName, firstName
     *
     * @param scope
     * @param reportType
     * @param addBC         Whether the BC should be added to the where clause
     * @return
     */
    public static String getReportListHQL(String scope, String reportType, boolean addBC) {
        String where = REPORT_LIST_WHERE + " and job.appointmentType in :appointmentTypes ";

        //@todo: do we need to do bc filtering when the scope is supervisor?
        if (!scope.equals(ReportsAction.DEFAULT_SCOPE) &&
                !scope.equals(ReportsAction.SCOPE_SUPERVISOR) && addBC) {
            where += " and job.businessCenterName = :bcName";
        }

        if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            where += " and job.orgCodeDescription LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            where += " and job.tsOrgCode = :tsOrgCode";
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            where += " and id in (:appraisalIds)";
        }

        if (ReportMgr.isWayOverdueReport(reportType)) {
            where += " and overdue > 30";
        } else if (ReportMgr.isOverdueReport(reportType)) {
            where += " and overdue > 0";
        }

        return REPORT_LIST_SELECT + where + LIST_ORDER;
    }

    /**
     * Returns the list of all the appraisals that match the current supervising chain all the
     * way down to the leaf.
     *
     * @param directSupervisors
     * @param inLeafSupervisor
     * @return
     */
    public static List<Integer> getAppraisalIdsForSupervisorReport(List<Job> directSupervisors,
                                                                   boolean inLeafSupervisor) {
        if (directSupervisors == null) {
            return null;
        }

        Session session = HibernateUtil.getCurrentSession();
        List<Integer> ids = new ArrayList<Integer>();

        String select = "SELECT appraisals.id FROM pyvpasj " +
                "LEFT JOIN appraisals ON (appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) ";
        String where = "WHERE pyvpasj_status = 'A' " +
                "AND PYVPASJ_APPOINTMENT_TYPE in (:appointmentTypes) " +
                "and appraisals.status not in ('completed', 'archived', 'closed')";
        String startWith = EvalsUtil.getStartWithClause(directSupervisors.size());

        if (!inLeafSupervisor) {
            where += " AND level > 1 ";
        }
        String sql = select + where + startWith + Constants.CONNECT_BY;

        Query query = session.createSQLQuery(sql)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);
        EvalsUtil.setStartWithParameters(directSupervisors, query);
        List<BigDecimal> result = query.list();

        for (BigDecimal id : result) {
            ids.add(Integer.parseInt(id.toString()));
        }

        return ids;
    }


    /**
     * Calculates the overdue value for the appraisal object and updates the value in the object.
     * It does not update the db.
     *
     * @param appraisal
     * @param configurationMap
     * @throws Exception
     */
    public static void updateOverdue(Appraisal appraisal, Map<String, Configuration> configurationMap)
            throws Exception {
        int overdue = EvalsUtil.getOverdue(appraisal, configurationMap);
        appraisal.setOverdue(overdue);
    }

    /**
     * Saves the overdue value by itself in the appraisal record.
     *
     * @param appraisal
     */
    public static void saveOverdue(Appraisal appraisal) {
        Session session = HibernateUtil.getCurrentSession();
        session.getNamedQuery("appraisal.saveOverdue")
                .setInteger("id", appraisal.getId())
                .setInteger("overdue", appraisal.getOverdue())
                .executeUpdate();
    }

    /**
     * Calculates what should be the new status of a given appraisal. It looks at the
     * configuration values to see whether the status is due or overdue.
     * @todo: handle: STATUS_GOALS_REACTIVATED in next release
     *
     * @param status
     * @param appraisal
     * @param configMap
     * @return
     * @throws Exception
     */
    public static String getNewStatus(String status, Appraisal appraisal,
                                      Map<String, Configuration> configMap) throws Exception {
        String newStatus = null;
        Configuration config = configMap.get(status); //config object of this status

        if (status.contains(Appraisal.DUE) && EvalsUtil.isDue(appraisal, config) <= 0) {
            newStatus = status.replace(Appraisal.DUE, Appraisal.OVERDUE); //new status is overdue
        } else if (status.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)
                &&  isGoalsReqModOverDue(appraisal, configMap)) {
            //goalsRequiredModification is not overdue.
            newStatus = Appraisal.STATUS_GOALS_OVERDUE;
        } else if (status.equals(Appraisal.STATUS_GOALS_APPROVED)) {
            //Need to check to see if it's time to change the status to results due
            Configuration reminderConfig = configMap.get("firstResultDueReminder");
            if (EvalsUtil.isDue(appraisal, reminderConfig) < 0) {
                newStatus = Appraisal.STATUS_RESULTS_DUE;
            }
        }
        return newStatus;
    }

    /**
     * If goals are not due yet, then no
     * If goals are due, check to see if goalsRequiredModification is overdue
     * Goals modifications due date is a configuration parameter which
     * defines how many days after requiredModification is submitted before they are due.
     * If goals modification is over due, then yes.
     * @param appraisal
     * @param configMap
     * @return true if both goals are overdue and goalsRequiredModification is overdue. Otherwise false.
     * @throws Exception
     */
    private static boolean isGoalsReqModOverDue(Appraisal appraisal,
                                                Map<String, Configuration> configMap) throws Exception
    {
        Configuration goalsDueConfig = configMap.get(Appraisal.STATUS_GOALS_DUE); //this config exists

        if (EvalsUtil.isDue(appraisal, goalsDueConfig) <= 0) { //goals due or overdue
            System.out.println(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION + ", goals overdue");
            //goals is due or overdue.  Is goalsRequiredModification overdue?
            Configuration modConfig = configMap.get("goalsRequiredModificationDue");

            if (EvalsUtil.isDue(appraisal, modConfig) < 0) {  // requiredModification is over due.
               return true;
            }
        }

        return false;
    }
}
