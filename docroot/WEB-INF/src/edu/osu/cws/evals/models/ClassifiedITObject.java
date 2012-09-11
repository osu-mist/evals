package edu.osu.cws.evals.models;

/**
 * Created with IntelliJ IDEA.
 * User: wanghuay
 * Date: 8/31/12
 * Time: 4:47 PM
 * To change this template use File | Settings | File Templates.
 */
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
