package edu.osu.cws.evals.models;

import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.util.CWSUtil;
import org.joda.time.DateTime;

import java.text.MessageFormat;
import java.util.*;

public class Appraisal extends Evals {

    public static final String TYPE_ANNUAL = "annual";

    public static final String TYPE_INITIAL = "initial";

    public static final String TYPE_TRIAL = "trial";

    public static final String STATUS_APPRAISAL_DUE = "appraisalDue";
    public static final String STATUS_APPRAISAL_OVERDUE = "appraisalOverdue";
    public static final String STATUS_ARCHIVED = "archived";
    public static final String STATUS_BACK_ORIG_STATUS = "backToOriginalStatus";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_GOALS_APPROVAL_DUE = "goalsApprovalDue";
    public static final String STATUS_GOALS_APPROVAL_OVERDUE = "goalsApprovalOverdue";
    public static final String STATUS_GOALS_APPROVED = "goalsApproved";
    public static final String STATUS_GOALS_DUE = "goalsDue";
    public static final String STATUS_GOALS_OVERDUE = "goalsOverdue";
    public static final String STATUS_GOALS_REACTIVATED = "goalsReactivated";
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

    private Date goalsSubmitDate;

    /**
     * The end of the date period for the appraisal.
     */
    private Date endDate;

    /**
     * The employee's supervisor that approves the employee's goals
     */
    private Employee evaluator;

    private Date evaluationSubmitDate;

    private Date goalApprovedDate;

    private Employee goalsApprover;

    /**
     * Comments entered by the supervisor regarding the employee's goals
     */
    private String goalsComments;

    private Date goalsRequiredModificationDate;

    public void setGoalsRequiredModificationDate(Date goalsRequiredModificationDate) {
        this.goalsRequiredModificationDate = goalsRequiredModificationDate;
    }

    public Date getGoalsRequiredModificationDate() {
        return goalsRequiredModificationDate;
    }

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

    private Employee reopenedBy;

    private Date reopenedDate;

    private String reopenReason;

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
     */
    public Appraisal(int id, String jobTitle, Date startDate, Date endDate, String status,
                     Integer overdue) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.job = new Job();
        this.job.setJobTitle(jobTitle);

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
     * @param goalsRequiredModificationDate
     * @param employeeSignedDate
     * @param employeeId
     * @param overdue
     */
    public Appraisal(int id, String jobTitle, String lastName, String firstName, String appointmentType,
                     Date startDate, Date endDate, String status, Date goalsRequiredModificationDate,
                     Date employeeSignedDate, int employeeId, Integer overdue) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setLastName(lastName);
        employee.setFirstName(firstName);
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.job = new Job();
        this.job.setJobTitle(jobTitle);
        this.job.setAppointmentType(appointmentType);
        this.job.setEmployee(employee);
        this.employeeSignedDate = employeeSignedDate;
        this.goalsRequiredModificationDate = goalsRequiredModificationDate;

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
     * date of the appraisal. The fields that are compared are: evaluationSubmitDate, goalsSubmitDate,
     * goalApprovedDate, resultSubmitDate, reviewSubmitDate, rebuttalDate, employeeSignedDate,
     * releaseDate, supervisorRebuttalRead, closeOutDate, reopenedDate
     *
     * @return lastModified
     */
    public Date getLastModified() {
        Date lastModified = createDate;
        Date fieldsToCompare[] = {evaluationSubmitDate, goalsSubmitDate, goalApprovedDate,
                resultSubmitDate, reviewSubmitDate, rebuttalDate, employeeSignedDate,
                releaseDate, supervisorRebuttalRead, closeOutDate, reopenedDate};

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

    public boolean isOpen() {
        String viewStatus = getViewStatus();
        return !status.equals(STATUS_CLOSED) && !viewStatus.equals(STATUS_COMPLETED)
                && !status.equals(STATUS_ARCHIVED);
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

    public Date getGoalsSubmitDate() {
        return goalsSubmitDate;
    }

    public void setGoalsSubmitDate(Date goalsSubmitDate) {
        this.goalsSubmitDate = goalsSubmitDate;
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

    public Date getGoalApprovedDate() {
        return goalApprovedDate;
    }

    public void setGoalApprovedDate(Date goalApprovedDate) {
        this.goalApprovedDate = goalApprovedDate;
    }

    public Employee getGoalsApprover() {
        return goalsApprover;
    }

    public void setGoalsApprover(Employee goalsApprover) {
        this.goalsApprover = goalsApprover;
    }

    public String getGoalsComments() {
        return goalsComments;
    }

    public void setGoalsComments(String goalsComments) {
        this.goalsComments = goalsComments;
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

    public Employee getReopenedBy() {
        return reopenedBy;
    }

    public void setReopenedBy(Employee reopenedBy) {
        this.reopenedBy = reopenedBy;
    }

    public Date getReopenedDate() {
        return reopenedDate;
    }

    public void setReopenedDate(Date reopenedDate) {
        this.reopenedDate = reopenedDate;
    }

    public String getReopenReason() {
        return reopenReason;
    }

    public void setReopenReason(String reopenReason) {
        this.reopenReason = reopenReason;
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
     * Returns the currently active goal version. In this release, we only support 1 goal version
     * per Appraisal. In the future, when we allow goals reactivation, we'll allow more than one.
     *
     * @return
     */
    public GoalVersion getCurrentGoalVersion() {
        if (goalVersions != null && !goalVersions.isEmpty()) {
            return (GoalVersion) goalVersions.toArray()[0];
        }

        return null;
    }

    /**
     * Walk through goalVersions and return the ones that are approved sorted by approvedDate asc.
     * An approved GoalVersion has approvedDate not set to null.
     * When there are no approved GoalVersions, return an empty list.
     * @return
     */
    public List getApprovedGoalsVersions() {
        List<GoalVersion> approvedGoalsVersions = new ArrayList<GoalVersion>();
        for (GoalVersion goalVersion : goalVersions) {
            if (goalVersion.isApproved()) {
                approvedGoalsVersions.add(goalVersion);
            }
        }

        Collections.sort(approvedGoalsVersions);
        return approvedGoalsVersions;
    }

    /**
     * Walk through goalVersions and return a single unapproved GoalVersion.
     * An unapproved GoalVersion has requestApproved set to true and approvedDate set to null
     * When there isn't an unapproved goals version, the method returns null. According to the
     * business requirements, there should only be 1 unapproved GoalVersion at a single time.
     */
    public GoalVersion getUnapprovedGoalsVersion() {
        for (GoalVersion goalVersion : goalVersions) {
            if (goalVersion.isUnapproved()) {
                return goalVersion;
            }
        }

        return null;
    }

    /**
     * Loads lazy associations
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
    public static Appraisal createFirstAnnual(Appraisal trialAppraisal) {
        Appraisal appraisal = new Appraisal();
        appraisal.setType(Appraisal.TYPE_ANNUAL);
        appraisal.setJob(trialAppraisal.getJob());
        appraisal.setCreateDate(new Date());
        appraisal.setStartDate(trialAppraisal.getJob().getInitialEvalStartDate().toDate());
        appraisal.setGoalsSubmitDate(trialAppraisal.getGoalsSubmitDate());
        appraisal.setGoalsApprover(trialAppraisal.getGoalsApprover());
        appraisal.setGoalApprovedDate(trialAppraisal.getGoalApprovedDate());
        appraisal.setRating(0);

        // set the status. The cron job will update the status if needed
        appraisal.setStatus(Appraisal.STATUS_GOALS_APPROVED);

        // calculate & set the end date
        DateTime startDate = new DateTime(appraisal.getStartDate());
        DateTime endDate = appraisal.getJob().getEndEvalDate(startDate, Appraisal.TYPE_INITIAL);
        appraisal.setEndDate(CWSUtil.toDate(endDate));

        // copy over goal version
        for (GoalVersion trialGoalVersion : trialAppraisal.getGoalVersions()) {
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
     * @todo: handle: STATUS_GOALS_REACTIVATED in next release
     *
     * @param configMap
     * @return
     * @throws Exception
     */
    public String getNewStatus(Map<String, Configuration> configMap)
            throws Exception {

        String newStatus = null;
        String status = getStatus();
        Configuration config = configMap.get(status); //config object of this status

        if (status.contains(Appraisal.DUE) && EvalsUtil.isDue(this, config) <= 0) {
            newStatus = status.replace(Appraisal.DUE, Appraisal.OVERDUE); //new status is overdue
        } else if (status.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)
                && isGoalsReqModOverDue(configMap)) {
            //goalsRequiredModification is not overdue.
            newStatus = Appraisal.STATUS_GOALS_OVERDUE;
        } else if (status.equals(Appraisal.STATUS_GOALS_APPROVED)) {
            //Need to check to see if it's time to change the status to results due
            Configuration reminderConfig = configMap.get("firstResultDueReminder");
            if (EvalsUtil.isDue(this, reminderConfig) < 0) {
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
     *
     * @param configMap
     * @return true if both goals are overdue and goalsRequiredModification is overdue. Otherwise false.
     * @throws Exception
     */
    private boolean isGoalsReqModOverDue(Map<String, Configuration> configMap)
            throws Exception {

        Configuration goalsDueConfig = configMap.get(Appraisal.STATUS_GOALS_DUE); //this config exists

        if (EvalsUtil.isDue(this, goalsDueConfig) <= 0) { //goals due or overdue
            System.out.println(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION + ", goals overdue");
            //goals is due or overdue.  Is goalsRequiredModification overdue?
            Configuration modConfig = configMap.get("goalsRequiredModification");

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
        DateTime sed = new DateTime(this.startDate);
        sed.withYear(new DateTime().getYear());

        return sed.toDate();
    }
}
