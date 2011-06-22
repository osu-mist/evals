package edu.osu.cws.pass.models;

import java.util.*;

public class Appraisal extends Pass {
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

    private Date respondedDate;

    private Date createDate;

    private String rebuttal;

    private Date rebuttalDate;

    private Date employeeSignedDate;

    private Date signatureRequestedDate;

    private Date supervisorRebuttalRead;

    private String type;

    private Date evaluation2Date;

    private Date review2Date;

    private Employee evaluator2;

    private Employee reviewer2;

    private Date closeOutDate;

    private Employee closeOutBy;

    private String closeOutReason;

    private Employee reopenedBy;

    private Date reopenedDate;

    private String reopenReason;

    private String originalStatus;

    private Set<Assessment> assessments = new HashSet<Assessment>();

    /**
     * Property that holds the appraisal status displayed based on the user status
     * For example, an employee gets to see in-review vs supervisor sees appraisal
     * submitted.
     */
    private String roleBasedStatus;

    private static final String jobRequired =
            "Please provide a valid job";

    /**
     * Validation error message for signature is public because the appraisal.jsp
     * needs to access this static variable in order to do js validation
     */
    public static final String signatureRequired =
            "Please click the box to acknowledge that you have read the appraisal";

    public static final String TYPE_ANNUAL = "annual";

    public static final String TYPE_TRIAL = "trial";

    public static final String TYPE_SPECIAL = "special";

    public Appraisal() { }

    public boolean validateJob() {
        ArrayList<String> jobErrors = new ArrayList<String>();

        // If there were any previous validation errors remove them
        this.errors.remove("job");

        if (this.job == null || this.job.getId() == 0) {
            jobErrors.add(jobRequired);
        }

        if (jobErrors.size() > 0) {
            this.errors.put("job", jobErrors);
            return false;
        }
        return true;
    }

    /**
     * Returns a sorted list of assessments. The assessment pojo class
     * implements comparable interface which makes this easy.
     *
     * @return
     */
    public List getSortedAssessments() {
        List sortedAssessments = new ArrayList(assessments);
        Collections.sort(sortedAssessments);
        return sortedAssessments;
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

    public Date getRespondedDate() {
        return respondedDate;
    }

    public void setRespondedDate(Date respondedDate) {
        this.respondedDate = respondedDate;
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

    public Date getSignatureRequestedDate() {
        return signatureRequestedDate;
    }

    public void setSignatureRequestedDate(Date signatureRequestedDate) {
        this.signatureRequestedDate = signatureRequestedDate;
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

    public Date getEvaluation2Date() {
        return evaluation2Date;
    }

    public void setEvaluation2Date(Date evaluation2Date) {
        this.evaluation2Date = evaluation2Date;
    }

    public Date getReview2Date() {
        return review2Date;
    }

    public void setReview2Date(Date review2Date) {
        this.review2Date = review2Date;
    }

    public Employee getEvaluator2() {
        return evaluator2;
    }

    public void setEvaluator2(Employee evaluator2) {
        this.evaluator2 = evaluator2;
    }

    public Employee getReviewer2() {
        return reviewer2;
    }

    public void setReviewer2(Employee reviewer2) {
        this.reviewer2 = reviewer2;
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

    public String getCloseOutReason() {
        return closeOutReason;
    }

    public void setCloseOutReason(String closeOutReason) {
        this.closeOutReason = closeOutReason;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<Assessment> assessments) {
        this.assessments = assessments;
    }

    public void addAssessment(Assessment assessment) {
        assessment.setAppraisal(this);
        this.assessments.add(assessment);
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

    public String getRoleBasedStatus() {
        return roleBasedStatus;
    }

    public void setRoleBasedStatus(String roleBasedStatus) {
        this.roleBasedStatus = roleBasedStatus;
    }
}
