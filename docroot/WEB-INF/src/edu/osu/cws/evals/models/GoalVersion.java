package edu.osu.cws.evals.models;

import java.util.*;

public class GoalVersion implements Comparable<GoalVersion> {
    private int id;

    private Appraisal appraisal;

    private Integer approverPidm;

    private Date createDate;

    private Date approvedDate;

    private Boolean requestApproved;

    private Integer decisionPidm;

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

    public Integer getApproverPidm() {
        return approverPidm;
    }

    public void setApproverPidm(Integer approverPidm) {
        this.approverPidm = approverPidm;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Boolean getRequestApproved() {
        return requestApproved;
    }

    public void setRequestApproved(Boolean requestApproved) {
        this.requestApproved = requestApproved;
    }

    public Integer getDecisionPidm() {
        return decisionPidm;
    }

    public void setDecisionPidm(Integer decisionPidm) {
        this.decisionPidm = decisionPidm;
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

    /**
     * Whether or not the supervisor has approved the goals submitted by the employee.
     * @return
     */
    public boolean areGoalsApproved() {
        return approvedDate != null;
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
            return BEFORE;
        }

        if (this.createDate.getTime() < otherGoalVersion.createDate.getTime()) {
            return AFTER;
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
        goalVersion.setApproverPidm(trialGoalVersion.getApproverPidm());
        goalVersion.setApprovedDate(trialGoalVersion.getApprovedDate());
        goalVersion.setRequestApproved(trialGoalVersion.getRequestApproved());
        goalVersion.setDecisionPidm(trialGoalVersion.getDecisionPidm());
        goalVersion.setTimedOutAt(trialGoalVersion.getTimedOutAt());

        return goalVersion;
    }

    /**
     * Whether or not the reactivation request for this goal version is pending or approved.
     *
     * @return
     */
    public boolean goalReactivationPendingOrApproved() {
        boolean approvedGoalReactivation = requestApproved != null && requestApproved;

        return approvedGoalReactivation || requestApproved == null;
    }
}
