package edu.osu.cws.evals.models;

public class AssessmentCriteria implements Comparable<AssessmentCriteria> {
    private int id;

    private CriterionArea criteriaArea;
    private Assessment assessment;
    private Boolean checked;

    public AssessmentCriteria() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CriterionArea getCriteriaArea() {
        return criteriaArea;
    }

    public void setCriteriaArea(CriterionArea criteriaArea) {
        this.criteriaArea = criteriaArea;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    /**
     * Assessment Criteria objects are sorted based on the name of the criteria area associated.
     *
     * @param otherAssessmentCriteria
     * @return
     */
    public int compareTo(AssessmentCriteria otherAssessmentCriteria) {
        String thisName = this.criteriaArea.getName();
        String otherName = otherAssessmentCriteria.criteriaArea.getName();
        return thisName.compareTo(otherName);
    }
}
