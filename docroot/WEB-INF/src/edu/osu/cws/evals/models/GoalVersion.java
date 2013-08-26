package edu.osu.cws.evals.models;

import java.util.*;

public class GoalVersion {
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

    /**
     * Copies properties from the trialAppraisal's goalVersion and sets up association with
     * newAppraisal into a new instance of GoalVersion.
     *
     * @param trialAppraisal
     * @param newAppraisal
     * @return
     */
    public static GoalVersion copyPropertiesFromTrial(Appraisal trialAppraisal,
                                                      Appraisal newAppraisal) {
        //@todo: remove this method
        GoalVersion goalVersion = new GoalVersion();
        goalVersion.setAppraisal(newAppraisal);
        goalVersion.setCreateDate(new Date());

        return goalVersion;
    }

    /*
     * Returns the sequence of the last assessment in the collection.
     *
     * @return
     */
    public Integer getLastSequence() {
        Integer lastSequence = 0;
        for (Assessment assessment : assessments) {
            if (assessment.getSequence() > lastSequence) {
                lastSequence = assessment.getSequence();
            }
        }
        return lastSequence;
    }
}
