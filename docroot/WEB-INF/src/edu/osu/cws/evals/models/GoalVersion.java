package edu.osu.cws.evals.models;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: zhangsh
 * Date: 3/5/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoalVersion {
    private int id;

    private int appraisalID;

    private int approverPidm;

    private Date createDate;

    private Date approvedDate;

    public GoalVersion() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppraisalID() {
        return appraisalID;
    }

    public void setAppraisalID(int appraisalID) {
        this.appraisalID = appraisalID;
    }

    public int getApproverPidm() {
        return approverPidm;
    }

    public void setApproverPidm(int approverPidm) {
        this.approverPidm = approverPidm;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Date approvedDate) {
        this.approvedDate = approvedDate;
    }
}
