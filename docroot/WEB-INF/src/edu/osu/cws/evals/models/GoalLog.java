package edu.osu.cws.evals.models;

import java.util.Date;

public class GoalLog extends Evals {
    private int id;

    private String content = "";

    private Employee author;

    private Assessment assessment;

    private Date createDate;

    /**
     * Defaults to null, which will be for normal goals. the value is new for new
     * goals, used for goalsReactivated.
     */
    private String type;

    public static final String NEW_GOAL_TYPE = "new";

    public static final String DEFAULT_GOAL_TYPE = null;

    public GoalLog() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Employee getAuthor() {
        return author;
    }

    public void setAuthor(Employee author) {
        this.author = author;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
