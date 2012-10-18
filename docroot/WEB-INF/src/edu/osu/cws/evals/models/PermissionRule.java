package edu.osu.cws.evals.models;

public class PermissionRule extends Evals implements Cloneable {
    private int id;

    private String status;

    private String role;

    private String goals;

    private String newGoals;

    private String goalComments;

    private String results;

    private String supervisorResults;

    private String evaluation;

    private String review;

    private String employeeResponse;

    private String rebuttalRead;

    private String saveDraft;

    private String requireModification;

    private String submit;

    private String actionRequired;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public PermissionRule() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getNewGoals() {
        return newGoals;
    }

    public void setNewGoals(String newGoals) {
        this.newGoals = newGoals;
    }

    public String getGoalComments() {
        return goalComments;
    }

    public void setGoalComments(String goalComments) {
        this.goalComments = goalComments;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getSupervisorResults() {
        return supervisorResults;
    }

    public void setSupervisorResults(String supervisorResults) {
        this.supervisorResults = supervisorResults;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getSaveDraft() {
        return saveDraft;
    }

    public void setSaveDraft(String saveDraft) {
        this.saveDraft = saveDraft;
    }

    public String getEmployeeResponse() {
        return employeeResponse;
    }

    public String getRebuttalRead() {
        return rebuttalRead;
    }

    public void setRebuttalRead(String rebuttalRead) {
        this.rebuttalRead = rebuttalRead;
    }

    public void setEmployeeResponse(String employeeResponse) {
        this.employeeResponse = employeeResponse;
    }

    public String getRequireModification() {
        return requireModification;
    }

    public void setRequireModification(String requireModification) {
        this.requireModification = requireModification;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }
}
