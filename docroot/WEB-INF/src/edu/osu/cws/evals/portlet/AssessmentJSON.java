package edu.osu.cws.evals.portlet;

import java.util.LinkedHashMap;
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
        if (shouldResetCriteriaIndex()) {
            resetCriteriaIndex();
        }

        return criteria;
    }

    public Integer getId() {
        return id;
    }

    private boolean shouldResetCriteriaIndex() {
        return id < 500;
    }

    /**
     * The assessments added via js have the criteria ids in
     * sequence:
     * goal1 criteria ids: 1, 2, 3, 4
     * goal2 criteria ids: 5, 6, 7, 8
     *
     * The ids need to be unique since that's a requirement by html
     */
    private void resetCriteriaIndex() {
        Map<Integer, Boolean> newCriteria = new LinkedHashMap<Integer, Boolean>();
        for (Integer key : criteria.keySet()) {
            Boolean value = criteria.get(key);

            // the expected keys start at 1 for new js criteria
            Integer newKey = (key % criteria.size()) + 1;
            newCriteria.put(newKey, value);
        }

        criteria = newCriteria;
    }
}
