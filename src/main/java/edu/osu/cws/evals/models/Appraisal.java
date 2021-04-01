package edu.osu.cws.evals.models;

import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;

import java.text.MessageFormat;
import java.util.*;

public class Appraisal extends Evals implements Comparable<Appraisal> {

    public static final String TYPE_ANNUAL = "annual";

    public static final String TYPE_INITIAL = "initial";

    public static final String TYPE_TRIAL = "trial";

    public static final String STATUS_APPRAISAL_DUE = "appraisalDue";
    public static final String STATUS_APPRAISAL_OVERDUE = "appraisalOverdue";
    public static final String STATUS_ARCHIVED_CLOSED = "archivedClosed";
    public static final String STATUS_ARCHIVED_COMPLETED = "archivedCompleted";
    public static final String STATUS_BACK_ORIG_STATUS = "backToOriginalStatus";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_GOALS_APPROVAL_DUE = "goalsApprovalDue";
    public static final String STATUS_GOALS_APPROVAL_OVERDUE = "goalsApprovalOverdue";
    public static final String STATUS_GOALS_APPROVED = "goalsApproved";
    public static final String STATUS_GOALS_DUE = "goalsDue";
    public static final String STATUS_GOALS_OVERDUE = "goalsOverdue";
    public static final String STATUS_GOALS_REACTIVATED = "goalsReactivated";
    public static final String STATUS_GOALS_REACTIVATION_REQUESTED = "goalsReactivationRequested";
    public static final String STATUS_GOALS_REQUIRED_MODIFICATION = "goalsRequiredModification";
    public static final String STATUS_REBUTTAL_READ_DUE = "rebuttalReadDue";
    public static final String STATUS_REBUTTAL_READ_OVERDUE = "rebuttalReadOverdue";
    public static final String STATUS_RELEASE_DUE = "releaseDue";
    public static final String STATUS_RELEASE_OVERDUE = "releaseOverdue";
    public static final String STATUS_RESULTS_DUE = "resultsDue";
    public static final String STATUS_RESULTS_OVERDUE = "resultsOverdue";
    public static final String STATUS_REVIEW_DUE = "reviewDue";
    public static final String STATUS_REVIEW_OVERDUE = "reviewOverdue";
    public static final String STATUS_SIGNATURE_DUE = "signatureDue";
    public static final String STATUS_SIGNATURE_OVERDUE = "signatureOverdue";
    public static final String STATUS_IN_REVIEW = "inReview";
    public static final String STATUS_EMPLOYEE_REVIEW_DUE = "employeeReviewDue";

    public static final String STAGE_GOALS = "goals";
    public static final String STAGE_RESULTS = "results";
    public static final String STAGE_APPRAISAL = "appraisal";
    public static final String STAGE_EVALUATION = "evaluation";
    public static final String STAGE_RELEASE = "release";
    public static final String STAGE_SIGNATURE = "signature";
    public static final String STAGE_COMPLETED = "completed";
    public static final String STAGE_REBUTTAL = "rebuttal";
    public static final String STAGE_CLOSED = "closed";
    public static final String STAGE_ARCHIVED = "archived";

    public static final String DUE = "Due";
    public static final String OVERDUE = "Overdue";

    private static final Map<String, String> nextStatus;
    static {
        Map<String, String> tempMap = new HashMap<String, String>();
        tempMap.put(STATUS_GOALS_REACTIVATION_REQUESTED, STATUS_GOALS_APPROVED);
        tempMap.put(STATUS_GOALS_REACTIVATED, STATUS_GOALS_APPROVED);
        tempMap.put(STATUS_EMPLOYEE_REVIEW_DUE, STATUS_RELEASE_DUE);
        tempMap.put(STATUS_GOALS_APPROVED, STATUS_RESULTS_DUE);
        tempMap.put(STATUS_GOALS_REQUIRED_MODIFICATION, STATUS_GOALS_OVERDUE);
        nextStatus = Collections.unmodifiableMap(tempMap);
    }

    private static final List<String> statusToExpire = Arrays.asList(
            STATUS_GOALS_REACTIVATION_REQUESTED,
            STATUS_GOALS_REACTIVATED,
            STATUS_EMPLOYEE_REVIEW_DUE
    );

    private int id;

    /**
     * Appraisals are related to a job instead of the employee because an
     * employee may hold multiple jobs.
     */
    private Job job;

    private String status;

    /**
     * The beginning of the date period for the appraisal.
     */
    private Date startDate;

    /**
     * The end of the date period for the appraisal.
     */
    private Date endDate;

    /**
     * The employee's supervisor that approves the employee's goals
     */
    private Employee evaluator;

    private Date evaluationSubmitDate;

    private Date resultSubmitDate;

    /**
     * Appraisal evaluation filled out by the supervisor and reviewed
     * by business center reviewer.
     */
    private String evaluation;

    /**
     * Rating given to the employee during the evaluation step
     */
    private Integer rating;

    /**
     * Business center employee approving the supervisor's evaluation
     */
    private Employee reviewer;

    private Date reviewSubmitDate;

    private String reviewStatusID;

    /**
     * Comments/feedback that business center provides to supervisor's
     * evaluation.
     */
    private String review;

    private Date createDate;

    private String rebuttal;

    private Date rebuttalDate;

    private Date employeeSignedDate;

    private Date releaseDate;

    private Date supervisorRebuttalRead;

    private String type;

    private Date closeOutDate;

    private Employee closeOutBy;

    private CloseOutReason closeOutReason;

    private String originalStatus;

    private Integer overdue;

    private Set<GoalVersion> goalVersions = new HashSet<GoalVersion>();

    private Set<Salary> salaries = new HashSet<Salary>();

    private Integer goalsOverdue;

    private Integer goalsApprovalOverdue;

    private Integer resultsOverdue;

    private Integer appraisalOverdue;

    private Integer reviewOverdue;

    private Integer releaseOverdue;

    private Integer signatureOverdue;

    private Integer rebuttalReadOverdue;

    /**
     * Read only propety not stored in the db. It is the role of the logged in user with
     * regards to this appraisal.
     */
    private String role;

    private PermissionRule permissionRule;

    private ArrayList<String> statusHiddenFromEmployee = new ArrayList<String>();

    public Appraisal() { }

    /**
     * Constructor used by AppraisalMgr to fetch only a limited set of attributes. Used to
     * display information in my status section and supervisor report (evaluations of current
     * supervisor).
     *
     * @param id
     * @param jobTitle
     * @param startDate
     * @param endDate
     * @param status
     * @param overdue
     * @param appointmentType
     */
    public Appraisal(int id, String jobTitle, Date startDate, Date endDate, String status,
                     Integer overdue, String appointmentType) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.job = new Job();
        this.job.setJobTitle(jobTitle);
        this.job.setAppointmentType(appointmentType);

        if (overdue == null) {
            this.overdue = -999;
        } else {
            this.overdue = overdue;
        }
    }

    /**
     * Constructor used by AppraisalMgr to fetch only a limited set of attributes. Used to display
     * information in my team section.
     *
     * @param id
     * @param jobTitle
     * @param jobTitle
     * @param lastName
     * @param firstName
     * @param appointmentType
     * @param startDate
     * @param endDate
     * @param status
     * @param employeeSignedDate
     * @param employeeId
     * @param overdue
     */
    public Appraisal(Integer id, String jobTitle, String lastName, String firstName, String appointmentType,
                     Date startDate, Date endDate, String status, Date employeeSignedDate,
                     int employeeId, Integer overdue) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setLastName(lastName);
        employee.setFirstName(firstName);
        if (id != null) {
            this.id = id;
        }
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.job = new Job();
        this.job.setJobTitle(jobTitle);
        this.job.setAppointmentType(appointmentType);
        this.job.setEmployee(employee);
        this.employeeSignedDate = employeeSignedDate;

        if (overdue == null) {
            this.overdue = -999;
        } else {
            this.overdue = overdue;
        }
    }

    /**
     * Constructor used by the getReviews and search method in appraisal mgr to fetch a list
     * of appraisal objects
     *
     * @param id
     * @param jobTitle
     * @param positionNumber
     * @param startDate
     * @param endDate
     * @param type
     * @param employeeId
     * @param lastName
     * @param firstName
     * @param evaluationSubmitDate
     * @param status
     * @param orgCodeDescription
     */
    public Appraisal(int id, String jobTitle, String positionNumber, Date startDate, Date endDate,
                     String type, int employeeId, String lastName, String firstName, Date evaluationSubmitDate,
                     String status, String bcName, String orgCodeDescription, String suffix,
                     Integer overdue) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setLastName(lastName);
        employee.setFirstName(firstName);

        Job tempJob = new Job();
        tempJob.setJobTitle(jobTitle);
        tempJob.setOrgCodeDescription(orgCodeDescription);
        tempJob.setEmployee(employee);
        tempJob.setPositionNumber(positionNumber);
        tempJob.setBusinessCenterName(bcName);
        tempJob.setSuffix(suffix);

        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.evaluationSubmitDate = evaluationSubmitDate;
        this.status = status;
        this.job = tempJob;

        if (overdue == null) {
            this.overdue = -999;
        } else {
            this.overdue = overdue;
        }
    }

    /**
     * Constructor used by ReportMgr.getReportListHQL. It only fetches the data that it needs. The
     * employee.id and job's pidm, posno and suffix are dummy data since they are only needed
     * to construct the object.
     *
     * @param id
     * @param firstName
     * @param lastName
     * @param startDate
     * @param endDate
     * @param status
     * @param overdue
     * @param employeeId
     * @param positionNumber
     * @param suffix
     */
    public Appraisal(int id, String firstName, String lastName, Date startDate, Date endDate,
                     String status, Integer overdue, int employeeId, String positionNumber, String suffix) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setLastName(lastName);
        employee.setFirstName(firstName);

        Job tempJob = new Job();
        tempJob.setPositionNumber(positionNumber);
        tempJob.setSuffix(suffix);
        tempJob.setEmployee(employee);

        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.job = tempJob;

        if (overdue == null) {
            this.overdue = -999;
        } else {
            this.overdue = overdue;
        }
    }

    public Appraisal(int id, Date startDate, String status) {
        this.id = id;
        this.startDate = startDate;
        this.status = status;
    }

    /**
     * Used to copy an appraisal object. This is used by appraisal search or report list appraisal.
     * This is done so that the jsp files don't complain about missing employee or job records in
     * the db.
     *
     * @param appraisal
     */
    public Appraisal(Appraisal appraisal) {
        this.id = appraisal.getId();
        if (appraisal.getStartDate() != null) {
            setStartDate(appraisal.getStartDate());
        }
        if (appraisal.getEndDate() != null) {
            setEndDate(appraisal.getEndDate());
        }
        if (appraisal.getType() != null) {
            setType(appraisal.getType());
        }
        if (appraisal.getEvaluationSubmitDate() != null) {
            setEvaluationSubmitDate(appraisal.getEvaluationSubmitDate());
        }
        if (appraisal.getStatus() != null) {
            setStatus(appraisal.getStatus());
        }
        if (appraisal.getOverdue() != null) {
            setOverdue(appraisal.getOverdue());
        }
        if (appraisal.getStatus() != null) {
            setStatus(appraisal.getStatus());
        }
        if (appraisal.getStatus() != null) {
            setStatus(appraisal.getStatus());
        }
        setJob(appraisal.getJob());
    }

    /**
     * Uses the start date and end date to generate the review period.
     *
     * @return
     */
    public String getReviewPeriod() {
        if (startDate == null) {
            startDate = new Date();
        }
        if (endDate == null) {
            endDate = new Date();
        }

        return MessageFormat.format("{0,date,MM/dd/yy} - {1,date,MM/dd/yy}",
                new Object[]{getStartDate(), getEndDate()});
    }

    /**
     * Compares various date fields of the appraisal object to figure out when was the last modified
     * date of the appraisal. The fields that are compared are: evaluationSubmitDate, resultSubmitDate,
     * reviewSubmitDate, rebuttalDate, employeeSignedDate, releaseDate, supervisorRebuttalRead,
     * closeOutDate.
     *
     * @return lastModified
     */
    public Date getLastModified() {
        Date lastModified = createDate;
        Date fieldsToCompare[] = {evaluationSubmitDate, resultSubmitDate, reviewSubmitDate,
                rebuttalDate, employeeSignedDate, releaseDate, supervisorRebuttalRead,
                closeOutDate};

        for (Date appraisalDate : fieldsToCompare) {
            if (appraisalDate != null && appraisalDate.after(lastModified)) {
                lastModified = appraisalDate;
            }
        }

        return lastModified;
    }

    /**
     *  Checks the appraisal status and if we need to change the status based on the user role, the status
     * is changed. Right now, if the supervisor submitted the appraisal or hr submitted comments, the status
     * displayed to the user is in review. If the status contains rebuttalRead, we set the status to
     * completed.
     *
     * @return status
     */
    public String getViewStatus() {
        String viewStatus = status;

        statusHiddenFromEmployee.add(STATUS_APPRAISAL_DUE);
        statusHiddenFromEmployee.add(STATUS_APPRAISAL_OVERDUE);
        statusHiddenFromEmployee.add(STATUS_REVIEW_DUE);
        statusHiddenFromEmployee.add(STATUS_REVIEW_OVERDUE);
        statusHiddenFromEmployee.add(STATUS_RELEASE_DUE);
        statusHiddenFromEmployee.add(STATUS_RELEASE_OVERDUE);

        if (getRole().equals("employee") &&  statusHiddenFromEmployee.contains(viewStatus)) {
            viewStatus = STATUS_IN_REVIEW;
        }

        // Whenever the status is rebuttalReadDue or rebuttalReadOverdue, we set it as completed.
        if (viewStatus.contains("rebuttalRead")) {
            viewStatus = STATUS_COMPLETED;
        }

        return viewStatus;
    }

    /**
     * This method is named as a getter so that it can be called from the jsp view.
     *
     * @return
     */
    public boolean getIsOpen() {
        String viewStatus = getViewStatus();
        return !status.equals(STATUS_CLOSED) && !viewStatus.equals(STATUS_COMPLETED)
                && !status.equals(STATUS_ARCHIVED_CLOSED)
                && !status.equals(STATUS_ARCHIVED_COMPLETED);
    }


    /**
     * Given a status, it returns the respective stage.
     *
     * @param status
     * @return
     */
    public static String getStage(String status) {
        String stage;
        if (status.equals(Appraisal.STATUS_GOALS_APPROVED) ) {
            stage = Appraisal.STAGE_RESULTS;
        } else if (status.contains("goals")) {
            stage = Appraisal.STAGE_GOALS;
        } else {
            stage = status.replace("Due", "").replace("Overdue", "");
        }

        if (stage.equals("rebuttalRead")) {
            stage = Appraisal.STAGE_REBUTTAL;
        }

        return stage;
    }

    /**
     * Used by the report list data. If the object is not overdue, we display - .
     *
     * @return
     */
    public String getViewOverdue() {
        if (overdue < 1) {
            return "-";
        }
        return overdue.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Employee getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Employee evaluator) {
        this.evaluator = evaluator;
    }

    public Date getEvaluationSubmitDate() {
        return evaluationSubmitDate;
    }

    public void setEvaluationSubmitDate(Date evaluationSubmitDate) {
        this.evaluationSubmitDate = evaluationSubmitDate;
    }

    public Date getResultSubmitDate() {
        return resultSubmitDate;
    }

    public void setResultSubmitDate(Date resultSubmitDate) {
        this.resultSubmitDate = resultSubmitDate;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Employee getReviewer() {
        return reviewer;
    }

    public void setReviewer(Employee reviewer) {
        this.reviewer = reviewer;
    }

    public Date getReviewSubmitDate() {
        return reviewSubmitDate;
    }

    public void setReviewSubmitDate(Date reviewSubmitDate) {
        this.reviewSubmitDate = reviewSubmitDate;
    }

    public String getReviewStatusID() {
        return reviewStatusID;
    }

    public void setReviewStatusID(String reviewStatusID) {
        this.reviewStatusID = reviewStatusID;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getRebuttal() {
        return rebuttal;
    }

    public void setRebuttal(String rebuttal) {
        this.rebuttal = rebuttal;
    }

    public Date getRebuttalDate() {
        return rebuttalDate;
    }

    public void setRebuttalDate(Date rebuttalDate) {
        this.rebuttalDate = rebuttalDate;
    }

    public Date getEmployeeSignedDate() {
        return employeeSignedDate;
    }

    public void setEmployeeSignedDate(Date employeeSignedDate) {
        this.employeeSignedDate = employeeSignedDate;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getSupervisorRebuttalRead() {
        return supervisorRebuttalRead;
    }

    public void setSupervisorRebuttalRead(Date supervisorRebuttalRead) {
        this.supervisorRebuttalRead = supervisorRebuttalRead;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCloseOutDate() {
        return closeOutDate;
    }

    public void setCloseOutDate(Date closeOutDate) {
        this.closeOutDate = closeOutDate;
    }

    public Employee getCloseOutBy() {
        return closeOutBy;
    }

    public void setCloseOutBy(Employee closeOutBy) {
        this.closeOutBy = closeOutBy;
    }

    public CloseOutReason getCloseOutReason() {
        return closeOutReason;
    }

    public void setCloseOutReason(CloseOutReason closeOutReason) {
        this.closeOutReason = closeOutReason;
    }

    public Set<GoalVersion> getGoalVersions() {
        return goalVersions;
    }

    public void setGoalVersions(Set<GoalVersion> goalVersions) {
        this.goalVersions = goalVersions;
    }

    public String getOriginalStatus() {
        return originalStatus;
    }

    public void setOriginalStatus(String originalStatus) {
        this.originalStatus = originalStatus;
    }

    public Integer getOverdue() {
        return overdue;
    }

    public void setOverdue(Integer overdue) {
        this.overdue = overdue;
    }

    public Integer getGoalsOverdue() {
        return goalsOverdue;
    }

    public void setGoalsOverdue(Integer goalsOverdue) {
        this.goalsOverdue = goalsOverdue;
    }

    public Integer getGoalsApprovalOverdue() {
        return goalsApprovalOverdue;
    }

    public void setGoalsApprovalOverdue(Integer goalsApprovalOverdue) {
        this.goalsApprovalOverdue = goalsApprovalOverdue;
    }

    public Integer getResultsOverdue() {
        return resultsOverdue;
    }

    public void setResultsOverdue(Integer resultsOverdue) {
        this.resultsOverdue = resultsOverdue;
    }

    public Integer getAppraisalOverdue() {
        return appraisalOverdue;
    }

    public void setAppraisalOverdue(Integer appraisalOverdue) {
        this.appraisalOverdue = appraisalOverdue;
    }

    public Integer getReviewOverdue() {
        return reviewOverdue;
    }

    public void setReviewOverdue(Integer reviewOverdue) {
        this.reviewOverdue = reviewOverdue;
    }

    public Integer getReleaseOverdue() {
        return releaseOverdue;
    }

    public void setReleaseOverdue(Integer releaseOverdue) {
        this.releaseOverdue = releaseOverdue;
    }

    public Integer getSignatureOverdue() {
        return signatureOverdue;
    }

    public void setSignatureOverdue(Integer signatureOverdue) {
        this.signatureOverdue = signatureOverdue;
    }

    public Integer getRebuttalReadOverdue() {
        return rebuttalReadOverdue;
    }

    public void setRebuttalReadOverdue(Integer rebuttalReadOverdue) {
        this.rebuttalReadOverdue = rebuttalReadOverdue;
    }

    public void addGoalVersion(GoalVersion goalVersion) {
        goalVersion.setAppraisal(this);
        goalVersions.add(goalVersion);
    }

    public Set<Salary> getSalaries() {
        return salaries;
    }

    public void setSalaries(Set<Salary> salaries) {
        this.salaries = salaries;
    }

    public Salary getSalary() {
        if (salaries.isEmpty()) {
            return null;
        }

        // There should only be 1 object, return it
        return salaries.iterator().next();
    }
    /**
     * Role of the logged in user for this appraisal. This is used
     * in the getViewStatus() method.
     *
     * @return
     */
    public String getRole() {
        if(role == null) {
            role = "";
        }
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PermissionRule getPermissionRule() {
        return permissionRule;
    }

    public void setPermissionRule(PermissionRule permissionRule) {
        this.permissionRule = permissionRule;
    }

    public boolean isEmployeeJobActive() {
        return getJob().getStatus().equalsIgnoreCase("A");
    }

    /**
     * Return the most recently created goal version where the goals haven't been approved by the
     * supervisor and the goals reactivation request hasn't been denied.
     *
     * @return
     */
    public GoalVersion getReactivatedGoalVersion() {
        for (GoalVersion goalVersion : goalVersions) {
            if (goalVersion.inActivatedState()) {
                return goalVersion;
            }
        }

        return null;
    }

    /**
     * Walk through goalVersions and return the ones that are approved sorted by approvedDate asc.
     * An approved GoalVersion has approvedDate not set to null.
     * When there are no approved GoalVersions, return an empty list.
     * @return
     */
    public List<GoalVersion> getApprovedGoalsVersions() {
        List<GoalVersion> approvedGoalsVersions = new ArrayList<GoalVersion>();
        for (GoalVersion goalVersion : goalVersions) {
            if (goalVersion.getGoalsApprovedDate() != null) {
                approvedGoalsVersions.add(goalVersion);
            }
        }

        Collections.sort(approvedGoalsVersions);
        return approvedGoalsVersions;
    }

    /**
     * This returns the goals version whose request has been approved but goals haven't.
     */
    public GoalVersion getUnapprovedGoalsVersion() {
        for (GoalVersion goalVersion : goalVersions) {
            boolean reactivateRequestApproved = goalVersion.getRequestDecision() != null
                    && goalVersion.getRequestDecision();
            if (reactivateRequestApproved && goalVersion.getGoalsApprovedDate() == null) {
                return goalVersion;
            }
        }

        return null;
    }

    /**
     * This method the goals version that is pending request approval.
     */
    public GoalVersion getRequestPendingGoalsVersion() {
        for (GoalVersion goalVersion : goalVersions) {
            if (goalVersion.getRequestDecision() == null) {
                return goalVersion;
            }
        }

        return null;
    }

    /**
     * Returns the goal version that was most recently timed out at a given status. This is
     * used when sending emails from the backend.
     *
     * @param timedOutStatus
     * @return
     */
    public GoalVersion getLastTimedOutGoalVersion(String timedOutStatus) {
        List<GoalVersion> timedOutGoalVersions = new ArrayList<GoalVersion>();
        for (GoalVersion goalVersion : goalVersions) {
            String timedOutAt = goalVersion.getTimedOutAt();
            if (timedOutAt != null && timedOutAt.equals(timedOutStatus)) {
                timedOutGoalVersions.add(goalVersion);
            }
        }

        Collections.sort(timedOutGoalVersions);
        if (timedOutGoalVersions.isEmpty()) {
            return null;
        }

        // Return the most recently timed out goal version
        return timedOutGoalVersions.get(timedOutGoalVersions.size() -1);
    }

    /**
     * Loads lazy associations. This is needed because Hibernate doesn't load all the
     * needed object associations by default. If the first time, they are used is in the
     * jsp, it will throw an error since the db session will have been closed at that
     * point.
     */
    public void loadLazyAssociations() {
        job.toString();
        Job supervisor = job.getSupervisor();
        if (supervisor != null && supervisor.getEmployee() != null) {
            supervisor.getEmployee().toString();
        }
        if (job.getEmployee() != null) {
            job.getEmployee().toString();
        }
        if (getCloseOutReason() != null) {
            getCloseOutReason().getReason();
        }
        if (evaluator != null) {
            evaluator.getName();
        }

        // iterate over goalVersions to load data
        for (GoalVersion goalVersion : goalVersions) {
            for (Assessment assessment : goalVersion.getSortedAssessments()) {
                for (AssessmentCriteria assessmentCriteria : assessment.getAssessmentCriteria()) {
                    assessmentCriteria.getCriteriaArea().getName();
                }
            }
        }

        // load salary if available. This applies only to IT
        if (this.getSalary() != null) {
            this.getSalary().toString();
        }
    }

    /**
     * Returns a new Appraisal pojo with the properties copied over from the trialAppraisal. It
     * also calls the copyPropertiesFromTrial method recursively over the associations.
     *
     * @param trialAppraisal
     * @return
     */
    public static Appraisal createFirstAnnual(Appraisal trialAppraisal) throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setType(Appraisal.TYPE_ANNUAL);
        appraisal.setJob(trialAppraisal.getJob());
        appraisal.setCreateDate(new Date());
        appraisal.setStartDate(trialAppraisal.getJob().getInitialEvalStartDate().toDate());
        appraisal.setRating(0);

        // set the status. The cron job will update the status if needed
        appraisal.setStatus(Appraisal.STATUS_GOALS_APPROVED);
        List<GoalVersion> approvedGoalsVersions = trialAppraisal.getApprovedGoalsVersions();
        if (approvedGoalsVersions == null || approvedGoalsVersions.isEmpty()) {
            appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        }

        // calculate & set the end date
        DateTime startDate = new DateTime(appraisal.getStartDate()).withTimeAtStartOfDay();
        DateTime endDate = appraisal.getJob().getEndEvalDate(startDate, Appraisal.TYPE_INITIAL);
        appraisal.setEndDate(CWSUtil.toDate(endDate));

        // get list of goal versions to copy
        List<GoalVersion> goalVersionsToCopy = new ArrayList<GoalVersion>();
        for (GoalVersion goalVersion : trialAppraisal.getGoalVersions()) {
            if (goalVersion != null && goalVersion.getRequestDecision()) {
                goalVersionsToCopy.add(goalVersion);
            }
        }

        if (goalVersionsToCopy == null || goalVersionsToCopy.isEmpty()) {
            throw new Exception("GoalVersions to copy shouldn't be empty");
        }

        for (GoalVersion trialGoalVersion : goalVersionsToCopy) {
            GoalVersion goalVersion = GoalVersion.copyPropertiesFromTrial(trialGoalVersion, appraisal);
            appraisal.addGoalVersion(goalVersion);

            // copy assessment objects
            for (Assessment oldAssessment: trialGoalVersion.getAssessments()) {
                Assessment newAssessment = Assessment.copyPropertiesFromTrial(oldAssessment);
                goalVersion.addAssessment(newAssessment);
            }
        }

        return appraisal;
    }

    /*
     * Calculates the overdue value for the appraisal object and updates the value in the object.
     * It does not update the db.
     *
     * @param configurationMap
     * @throws Exception
     */
    public void updateOverdue(Map<String, Configuration> configurationMap)
            throws Exception {
        overdue = EvalsUtil.getOverdue(this, configurationMap);
    }

    /**
     * Calculates what should be the new status of a given appraisal. It looks at the
     * configuration values to see whether the status is due or overdue.
     *
     * @param configMap
     * @return          The new status of the given appraisal. The status is a string.
     * @throws Exception
     */
    public String getNewStatus(Map<String, Configuration> configMap)
            throws Exception {
        String status = getStatus();
        //config object of this status
        Configuration config = ConfigurationMgr.getConfiguration(configMap, status, getAppointmentType());

        // the employee review due status does not become overdue
        if (status.contains(Appraisal.DUE) && EvalsUtil.isOverdue(this, config) &&
                !status.equals(Appraisal.STATUS_EMPLOYEE_REVIEW_DUE)) {
            return status.replace(Appraisal.DUE, Appraisal.OVERDUE); //new status is overdue
        }

        if (status.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)
                && isGoalsReqModOverDue(configMap) && !areGoalsReactivated()) {
            //goalsRequiredModification is not overdue.
            return nextStatus.get(status);
        }

        if (status.equals(Appraisal.STATUS_GOALS_APPROVED)) {
            // set correct config to check if it's time to change the status to results due
            config = ConfigurationMgr.getConfiguration(configMap, "firstResultDueReminder",
                    getAppointmentType());
        } else if (statusToExpire.contains(status)) {
            // get configuration to check if the status status needs to be expired
            config = ConfigurationMgr.getConfiguration(configMap, status + "Expiration", getAppointmentType());
        }

        if (EvalsUtil.isOverdue(this, config)) {
            return nextStatus.get(status);
        }

        return null;
    }

    /**
     * If goals are not due yet, then no
     * If goals are due, check to see if goalsRequiredModification is overdue
     * Goals modifications due date is a configuration parameter which
     * defines how many days after requiredModification is submitted before they are due.
     * If goals modification is over due, then yes.
     *
     * @param configMap
     * @return true if both goals are overdue and goalsRequiredModification is overdue. Otherwise false.
     * @throws Exception
     */
    private boolean isGoalsReqModOverDue(Map<String, Configuration> configMap)
            throws Exception {

        Configuration goalsDueConfig = ConfigurationMgr.getConfiguration(configMap, Appraisal.STATUS_GOALS_DUE,
                getAppointmentType()); //this config exists

        if (EvalsUtil.isDue(this, goalsDueConfig) <= 0) { //goals due or overdue
            System.out.println(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION + ", goals overdue");
            //goals is due or overdue.  Is goalsRequiredModification overdue?
            Configuration modConfig = ConfigurationMgr.getConfiguration(configMap, "goalsRequiredModification",
                    getAppointmentType());

            if (EvalsUtil.isDue(this, modConfig) < 0) {  // requiredModification is over due.
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a Date object which contains the salary eligibility date for the
     * given appraisal.
     *
     * @return
     */
    public Date getSalaryEligibilityDate() {
        DateTime sed = new DateTime(this.startDate).withTimeAtStartOfDay();
        sed.withYear(EvalsUtil.getToday().getYear());

        return sed.toDate();
    }

    /**
     * Figure out if the appraisal is currently in goals reactivated loop.
     * Basically if we are not in approvalDue status or later and there's more than 1
     * goal version.
     */
    public boolean areGoalsReactivated() {
        boolean hasMultipleGoalVersions = goalVersions.size() > 1;

        String[] goalsReactivatedStatus = {
            STATUS_GOALS_APPROVAL_DUE,
            STATUS_GOALS_APPROVAL_OVERDUE,
            STATUS_GOALS_REACTIVATED,
            STATUS_GOALS_REACTIVATION_REQUESTED,
            STATUS_GOALS_REQUIRED_MODIFICATION
        };

        return hasMultipleGoalVersions && ArrayUtils.contains(goalsReactivatedStatus, status);
    }

    public Map<String, Assessment> getAssessmentMap() {
        HashMap<String, Assessment> assessmentMap = new HashMap<String, Assessment>();
        for (GoalVersion goalVersion : goalVersions) {
            for (Assessment assessment : goalVersion.getAssessments()) {
                if (!assessment.isDeleted()) {
                    assessmentMap.put(assessment.getId().toString(), assessment);
                }
            }
        }
        return assessmentMap;
    }

    public String getAppointmentType() {
        return job.getAppointmentType();
    }

    /**
     * Whether or not this appraisal object should use the salary increase section.
     *
     * @return
     */
    public Boolean getIsSalaryUsed() {
        if (getAppointmentType().equals(AppointmentType.CLASSIFIED_IT)) {
            if (type.equals(Appraisal.TYPE_ANNUAL)) {
                // annual IT evals always get salary piece
                return true;
            } else if (getJob().getAnnualInd() == 18) {
                // if the annual ind is 18, the IT trial eval gets salary piece
                return true;
            }
        }

        return false;
    }

    /**
     * Appraisal objects are sorted first by start date and then employee last name.
     *
     * @param otherAppraisal
     * @return
     */
    public int compareTo(Appraisal otherAppraisal) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        Employee employee = this.job.getEmployee();
        Job otherJob = otherAppraisal.getJob();
        Employee otherEmployee = otherJob.getEmployee();
        // Check for nulls
        if (this.startDate == null || otherAppraisal.getStartDate() == null || this.job == null ||
                employee == null || employee.getLastName() == null || otherJob != null || otherEmployee == null
                || otherEmployee.getLastName() == null) {
            return AFTER;
        }

        if (this.startDate.getTime()  > otherAppraisal.getStartDate().getTime()) {
            return AFTER;
        }

        if (this.startDate.getTime() < otherAppraisal.getStartDate().getTime()) {
            return BEFORE;
        }

        return this.getJob().getEmployee().getLastName().compareTo(otherEmployee.getLastName());
    }

    public boolean isRated() {
        Integer rating = getRating();
        String aptType = getAppointmentType();
        if(rating == null) {
            return false;
        }
        boolean classifiedNotRated = aptType.equals(AppointmentType.CLASSIFIED) && rating == 4;
        boolean profFacultyNotRated = getJob().isUnclassified() && rating == 6;
        return !(classifiedNotRated || profFacultyNotRated);
    }
}
