package edu.osu.cws.evals.portlet;

import java.util.Map;

/**
 * This class is meant to hold the structured json data that the evaluation form submits. This class
 * makes it easier to iterate and access the data without having to do a lot of casting or toString
 * method calls.
 */
public class AppraisalJSON {
    private Integer id;
    private String goalsComments;
    private String evaluation;
    private Integer rating;
    private String salaryRecommendation;
    private String review;
    private String rebuttal;
    private String buttonClicked;
    Map<Integer, AssessmentJSON> assessments;

    AppraisalJSON(Integer id, String goalsComments, String evaluation, Integer rating, String salaryRecommendation, String review, String rebuttal, String buttonClicked, Map<Integer, AssessmentJSON> assessments) {
        this.id = id;
        this.goalsComments = goalsComments;
        this.evaluation = evaluation;
        this.rating = rating;
        this.salaryRecommendation = salaryRecommendation;
        this.review = review;
        this.rebuttal = rebuttal;
        this.buttonClicked = buttonClicked;
        this.assessments = assessments;
    }

    public Integer getId() {
        return id;
    }

    public String getGoalsComments() {
        return goalsComments;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public Integer getRating() {
        return rating;
    }

    public String getSalaryRecommendation() {
        return salaryRecommendation;
    }

    public String getReview() {
        return review;
    }

    public String getRebuttal() {
        return rebuttal;
    }

    public String getButtonClicked() {
        return buttonClicked;
    }

    public Map<Integer, AssessmentJSON> getAssessments() {
        return assessments;
    }

}
