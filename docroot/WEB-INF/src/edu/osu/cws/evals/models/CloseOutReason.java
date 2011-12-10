package edu.osu.cws.evals.models;

import java.util.Date;

public class CloseOutReason extends Evals {
    private int id;

    private String reason;

    private Employee creator;

    private Date createDate;

    private Date deleteDate;

    public static final String validReasonRequired = "Please enter a valid close out reason";

    public CloseOutReason() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Employee getCreator() {
        return creator;
    }

    public void setCreator(Employee creator) {
        this.creator = creator;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
}
