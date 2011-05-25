package edu.osu.cws.pass.models;

public class PermissionRule extends Pass {
    private int id;

    private String status;

    private String role;

    private String goals;

    private String newGoals;

    private String requiredModification;

    private String results;

    private String resultComments;

    private String evaluation;

    private String review;

    private String employeeResponse;

    private String saveDraft;

    private String requireModification;

    private String submit;

    private String actionRequired;

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

    public String getRequiredModification() {
        return requiredModification;
    }

    public void setRequiredModification(String requiredModification) {
        this.requiredModification = requiredModification;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getResultComments() {
        return resultComments;
    }

    public void setResultComments(String resultComments) {
        this.resultComments = resultComments;
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
