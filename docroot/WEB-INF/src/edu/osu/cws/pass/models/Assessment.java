package edu.osu.cws.pass.models;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Assessment extends Pass {
    private int id;

    private Appraisal appraisal;

    private CriterionDetail criterionDetail;

    private String goal;

    private String newGoals;

    private String employeeResult;

    private String supervisorResult;

    private Date createDate;

    private Date modifiedDate;

    private Set<GoalLog> goalLogs = new HashSet<GoalLog>();

    public Assessment() { }

    /**
     * This method returns the last GoalLog for either a regular
     * goal entered during the goals due/past due period or the goals
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

    public Appraisal getAppraisal() {
        return appraisal;
    }

    public void setAppraisal(Appraisal appraisal) {
        this.appraisal = appraisal;
    }

    public CriterionDetail getCriterionDetail() {
        return criterionDetail;
    }

    public void setCriterionDetail(CriterionDetail criterionDetail) {
        this.criterionDetail = criterionDetail;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getNewGoals() {
        return newGoals;
    }

    public void setNewGoals(String newGoals) {
        this.newGoals = newGoals;
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
}
