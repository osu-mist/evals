package edu.osu.cws.evals.models;

public class AssessmentCriteria {
    private int id;

    private int assessmentID;

    private int criteriaAreaID;

    private boolean checked;

    public AssessmentCriteria() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssessmentID() {
        return assessmentID;
    }

    public void setAssessmentID(int assessmentID) {
        this.assessmentID = assessmentID;
    }

    public int getCriteriaAreaID() {
        return criteriaAreaID;
    }

    public void setCriteriaAreaID(int criteriaAreaID) {
        this.criteriaAreaID = criteriaAreaID;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
