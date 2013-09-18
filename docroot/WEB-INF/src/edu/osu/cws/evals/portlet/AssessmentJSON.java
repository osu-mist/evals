package edu.osu.cws.evals.portlet;

import java.util.Map;

/**
 * This class is meant to hold the structured json data that the evaluation form submits. This class
 * makes it easier to iterate and access the data without having to do a lot of casting or toString
 * method calls.
 */
public class AssessmentJSON {
    private String goal;
    private String employeeResult;
    private String supervisorResult;
    private String deleted;
    private Map<Integer, Boolean> criteria;
    private Integer id;

    AssessmentJSON(String goal, String employeeResult, String supervisorResult, String deleted,
                   Map<Integer, Boolean> criteria, Integer id) {
        this.goal = goal;
        this.employeeResult = employeeResult;
        this.supervisorResult = supervisorResult;
        this.deleted = deleted;
        this.criteria = criteria;
        this.id = id;
    }

    public String getGoal() {
        return goal;
    }

    public String getEmployeeResult() {
        return employeeResult;
    }

    public String getSupervisorResult() {
        return supervisorResult;
    }

    public String getDeleted() {
        return deleted;
    }

    public Map<Integer, Boolean> getCriteria() {
        return criteria;
    }

    public Integer getId() {
        return id;
    }
}
