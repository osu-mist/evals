package edu.osu.cws.pass.models;

import javax.jnlp.IntegrationService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * The end of the date period for the appraisal.
     */
    private Date endDate;

    /**
     * The employee's supervisor that approves the employee's goals
     */
    private Employee goalApprover;

    private Date goalApprovedDate;

    /**
     * Comments entered by the supervisor regarding the employee's goals
     */
    private String goalComments;

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
    private Employee hrApprover;

    private Date hrApprovedDate;

    private String reviewStatusID;

    /**
     * Comments/feedback that business center provides to supervisor's
     * evaluation.
     */
    private String hrComments;

    private Date createDate;

    private Date modifiedDate;

    private String employeeComments;

    private Date employeeSignedDate;

    private String emailType;

    private Date emailDate;

    private Integer emailCount;

    private Date closeOutDate;

    private Employee closeOutBy;

    private String closeOutReason;

    private Set<Assessment> assessments = new HashSet<Assessment>();

    private static final String jobRequired =
            "Please provide a valid job";

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

    public Employee getGoalApprover() {
        return goalApprover;
    }

    public void setGoalApprover(Employee goalApprover) {
        this.goalApprover = goalApprover;
    }

    public Date getGoalApprovedDate() {
        return goalApprovedDate;
    }

    public void setGoalApprovedDate(Date goalApprovedDate) {
        this.goalApprovedDate = goalApprovedDate;
    }

    public String getGoalComments() {
        return goalComments;
    }

    public void setGoalComments(String goalComments) {
        this.goalComments = goalComments;
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

    public Employee getHrApprover() {
        return hrApprover;
    }

    public void setHrApprover(Employee hrApprover) {
        this.hrApprover = hrApprover;
    }

    public Date getHrApprovedDate() {
        return hrApprovedDate;
    }

    public void setHrApprovedDate(Date hrApprovedDate) {
        this.hrApprovedDate = hrApprovedDate;
    }

    public String getReviewStatusID() {
        return reviewStatusID;
    }

    public void setReviewStatusID(String reviewStatusID) {
        this.reviewStatusID = reviewStatusID;
    }

    public String getHrComments() {
        return hrComments;
    }

    public void setHrComments(String hrComments) {
        this.hrComments = hrComments;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getEmployeeComments() {
        return employeeComments;
    }

    public void setEmployeeComments(String employeeComments) {
        this.employeeComments = employeeComments;
    }

    public Date getEmployeeSignedDate() {
        return employeeSignedDate;
    }

    public void setEmployeeSignedDate(Date employeeSignedDate) {
        this.employeeSignedDate = employeeSignedDate;
    }

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public Date getEmailDate() {
        return emailDate;
    }

    public void setEmailDate(Date emailDate) {
        this.emailDate = emailDate;
    }

    public Integer getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(Integer emailCount) {
        this.emailCount = emailCount;
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
}
