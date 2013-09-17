package edu.osu.cws.evals.models;

import java.util.*;

public class Assessment extends Evals implements Comparable<Assessment> {

    private int id;

    private String goal;

    private String employeeResult;

    private String supervisorResult;

    private Date createDate;

    private Date modifiedDate;

    private GoalVersion goalVersion;

    /**
     * The sequence represents the order in which the objects are displayed in the evaluation form.
     * The sequence numbers should be unique for all assessments that belong to a single goal version.
     * In other words, some sequence #s will correspond to deleted assessments that are not displayed
     * to the user.
     */
    private int sequence;

    private Integer deleterPidm;

    private Date deleteDate;

    private Set<GoalLog> goalLogs = new HashSet<GoalLog>();

    private Set<AssessmentCriteria> assessmentCriteria = new HashSet<AssessmentCriteria>();

    public Assessment() { }

    /**
     * This method returns the last GoalLog for either a regular
     * goal entered during the goals due/overdue period or the goals
     * that are appended afterwards: "new".
     *
     * @param type Either GoalLog.DEFAULT_GOAL_TYPE or GoalLog.NEW_GOAL_TYPE
     * @return
     */
    public GoalLog getLastGoalLog(String type) {
        for (GoalLog goalLog : goalLogs) {
            if ((goalLog.getType() == null && type == null) ||
                    (goalLog.getType() != null && goalLog.getType().equals(type))) {
                return goalLog;
            }
        }
        return new GoalLog();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getEmployeeResult() {
        return employeeResult;
    }

    public void setEmployeeResult(String employeeResult) {
        this.employeeResult = employeeResult;
    }

    public String getSupervisorResult() {
        return supervisorResult;
    }

    public void setSupervisorResult(String supervisorResult) {
        this.supervisorResult = supervisorResult;
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

    public Integer getDeleterPidm() {
        return deleterPidm;
    }

    public void setDeleterPidm(Integer deleterPidm) {
        this.deleterPidm = deleterPidm;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    public Set<GoalLog> getGoalLogs() {
        return goalLogs;
    }

    public void setGoalLogs(Set<GoalLog> goalLogs) {
        this.goalLogs = goalLogs;
    }

    public void addAssessmentLog(GoalLog goalLog) {
        goalLog.setAssessment(this);
        this.goalLogs.add(goalLog);
    }

    public GoalVersion getGoalVersion() {
        return goalVersion;
    }

    public void setGoalVersion(GoalVersion goalVersion) {
        this.goalVersion = goalVersion;
    }

    public Set<AssessmentCriteria> getAssessmentCriteria() {
        return assessmentCriteria;
    }

    public void setAssessmentCriteria(Set<AssessmentCriteria> assessmentCriteria) {
        this.assessmentCriteria = assessmentCriteria;
    }

    public void addAssessmentCriteria(AssessmentCriteria assessmentCriterion) {
        assessmentCriterion.setAssessment(this);
        assessmentCriteria.add(assessmentCriterion);
    }

    public int compareTo(Assessment otherAssessment) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this.sequence < otherAssessment.sequence) {
            return BEFORE;
        }

        if (this.sequence > otherAssessment.sequence) {
            return AFTER;
        }

        return EQUAL;
    }

    /**
     * Returns a sorted list of AssessmentCriteria by the name of CriteriaArea.name.
     *
     * @return
     */
    public List<AssessmentCriteria> getSortedAssessmentCriteria() {
        List<AssessmentCriteria> sortedAssessmentCriteria = new ArrayList(assessmentCriteria);
        Collections.sort(sortedAssessmentCriteria);
        return sortedAssessmentCriteria;
    }

    /**
     * Whether or not the Assessment has been deleted.
     *
     * @return
     */
    public Boolean isDeleted() {
        return deleterPidm != null && deleteDate != null;
    }

    /**
     * Returns a new Assessment object with the trialAssessment properties set as well as the
     * goalVersion association setup.
     *
     * @param trialAssessment
     * @return
     */
    public static Assessment copyPropertiesFromTrial(Assessment trialAssessment) {
        Assessment newAssessment = new Assessment();
        newAssessment.setGoal(trialAssessment.getGoal());
        newAssessment.setCreateDate(new Date());
        newAssessment.setEmployeeResult(trialAssessment.getEmployeeResult());
        newAssessment.setSupervisorResult(trialAssessment.getSupervisorResult());
        newAssessment.setModifiedDate(new Date());
        newAssessment.setSequence(trialAssessment.getSequence());

        // copy assessment criteria objects for this new assessment
        for (AssessmentCriteria trialAssessmentCriteria : trialAssessment.getAssessmentCriteria()) {
            AssessmentCriteria newAssessmentCriteria =
                    AssessmentCriteria.copyPropertiesFromTrial(trialAssessmentCriteria);
            newAssessment.addAssessmentCriteria(newAssessmentCriteria);
        }

        return newAssessment;
    }

    /**
     * Whether or not this assessment should be considered a new goal as far as permission rules are
     * concerned.
     */
    public boolean isNewGoal() {
        return goalVersion.inActivatedState();
    }
}
