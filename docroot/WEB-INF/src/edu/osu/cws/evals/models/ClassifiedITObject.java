package edu.osu.cws.evals.models;

/**
 * This is a special class to transport the data from a "fake" appraisal
 * of Classified IT employees' job and display in a table of myReport
 * portlet. This class is not mapping any database but just for the safety
 * of data transportation.
 * **/

 public class ClassifiedITObject {
    private String employeeName;

    private String reviewPeriod;

    public ClassifiedITObject(String employeeName, String reviewPeriod) {
        this.employeeName = employeeName;
        this.reviewPeriod = reviewPeriod;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getReviewPeriod() {
        return reviewPeriod;
    }

    public void setReviewPeriod(String reviewPeriod) {
        this.reviewPeriod = reviewPeriod;
    }
}
