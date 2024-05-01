package edu.osu.cws.evals.models;

import java.util.*;

public class GoalVersion implements Comparable<GoalVersion> {
    private int id;

    private Appraisal appraisal;

    private Integer goalsApproverPidm;

    private Date createDate;

    private Date goalsApprovedDate;

    private Date goalsSubmitDate;

    private Date goalsRequiredModificationDate;

    private String goalsComments;

    private Date requestDecisionDate;

    private Boolean requestDecision;

    private Integer requestDecisionPidm;

    private String timedOutAt;

    private Set<Assessment> assessments = new HashSet<Assessment>();

    public GoalVersion() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Appraisal getAppraisal() {
        return appraisal;
    }

    public void setAppraisal(Appraisal appraisal) {
        this.appraisal = appraisal;
    }

    public Integer getGoalsApproverPidm() {
        return goalsApproverPidm;
    }

    public void setGoalsApproverPidm(Integer goalsApproverPidm) {
        this.goalsApproverPidm = goalsApproverPidm;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getGoalsApprovedDate() {
        return goalsApprovedDate;
    }

    public void setGoalsApprovedDate(Date goalsApprovedDate) {
        this.goalsApprovedDate = goalsApprovedDate;
    }

    public Date getGoalsSubmitDate() {
        return goalsSubmitDate;
    }

    public void setGoalsSubmitDate(Date goalsSubmitDate) {
        this.goalsSubmitDate = goalsSubmitDate;
    }

    public void setGoalsRequiredModificationDate(Date goalsRequiredModificationDate) {
        this.goalsRequiredModificationDate = goalsRequiredModificationDate;
    }

    public Date getGoalsRequiredModificationDate() {
        return goalsRequiredModificationDate;
    }

    public String getGoalsComments() {
        return goalsComments;
    }

    public void setGoalsComments(String goalsComments) {
        this.goalsComments = goalsComments;
    }

    public Boolean getRequestDecision() {
        return requestDecision;
    }

    public void setRequestDecision(Boolean requestDecision) {
        this.requestDecision = requestDecision;
    }

    public Integer getRequestDecisionPidm() {
        return requestDecisionPidm;
    }

    public void setRequestDecisionPidm(Integer requestDecisionPidm) {
        this.requestDecisionPidm = requestDecisionPidm;
    }

    public String getTimedOutAt() {
        return timedOutAt;
    }

    public void setTimedOutAt(String timedOutAt) {
        this.timedOutAt = timedOutAt;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(Set<Assessment> assessments) {
        this.assessments = assessments;
    }

    public void addAssessment(Assessment assessment) {
        assessment.setGoalVersion(this);
        assessments.add(assessment);
    }

    public Date getRequestDecisionDate() {
        return requestDecisionDate;
    }

    public void setRequestDecisionDate(Date requestDecisionDate) {
        this.requestDecisionDate = requestDecisionDate;
    }

    /**
     * Returns a sorted list of assessments. The assessment pojo class
     * implements comparable interface which makes this easy. It removes deleted assessments from
     * the list.
     *
     * @return
     */
    public List<Assessment> getSortedAssessments() {
        List<Assessment> sortedAssessments = new ArrayList(assessments);
        Collections.sort(sortedAssessments);

        // Remove deleted assessments.
        // Using an iterator since otherwise we would get ConcurrentModificationException
        Iterator<Assessment> iterator = sortedAssessments.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isDeleted()) {
                iterator.remove();
            }
        }

        return sortedAssessments;
    }

    public int compareTo(GoalVersion otherGoalVersion) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this.createDate.getTime() > otherGoalVersion.createDate.getTime()) {
            return AFTER;
        }

        if (this.createDate.getTime() < otherGoalVersion.createDate.getTime()) {
            return BEFORE;
        }

        return EQUAL;
    }

    /**
     * Copies properties from the trialGoalVersion's and sets up association with
     * newAppraisal into a new instance of GoalVersion.
     *
     * @param trialGoalVersion
     * @param newAppraisal
     * @return
     */
    public static GoalVersion copyPropertiesFromTrial(GoalVersion trialGoalVersion,
                                                      Appraisal newAppraisal) {
        GoalVersion goalVersion = new GoalVersion();
        goalVersion.setAppraisal(newAppraisal);
        goalVersion.setCreateDate(new Date());
        goalVersion.setGoalsApproverPidm(trialGoalVersion.getGoalsApproverPidm());
        goalVersion.setGoalsApprovedDate(trialGoalVersion.getGoalsApprovedDate());
        goalVersion.setGoalsSubmitDate(trialGoalVersion.getGoalsSubmitDate());
        goalVersion.setRequestDecision(trialGoalVersion.getRequestDecision());
        goalVersion.setRequestDecisionPidm(trialGoalVersion.getRequestDecisionPidm());
        goalVersion.setTimedOutAt(trialGoalVersion.getTimedOutAt());

        return goalVersion;
    }

    /**
     * Whether or not the reactivation request for this goal version is pending or approved.
     * This method returns false if the goals reactivation request was denied or if the
     * goals reactivation request was approved, and the goals have been approved as well.
     *
     * @return
     */
    public boolean inActivatedState() {
        if (requestDecision == null) {
            return true; // request pending approval
        }

        if (requestDecision && requestDecisionPidm != null && goalsApprovedDate == null) {
            //pending goals approval, requestDecisionPidm is there to exclude the first version
            //of goals created by the cronjob.
            return true;
        }

        return false;
    }

    public void approveEmployeeGoals(Integer pidm) {
        setGoalsApprovedDate(new Date());
        setGoalsApproverPidm(pidm);
    }
}
