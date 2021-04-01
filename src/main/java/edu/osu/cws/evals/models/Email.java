package edu.osu.cws.evals.models;

import java.util.Date;

public class Email extends Evals
{
    private int id;
    private int appraisalId;
    private String emailType;
    private Date sentDate;

    public Email(int appraisalID, String emailType) {
        this.emailType = emailType;
        this.appraisalId = appraisalID;
        sentDate = new Date();
    }

    public Email() {} //@@@May not this.  Put it in to compile the code for now.

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }


    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public int getId() {

        return id;
    }



    public int getAppraisasId() {
        return appraisalId;
    }

    public String getEmailType() {
        return emailType;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAppraisalId(int appraisalId) {
        this.appraisalId = appraisalId;
    }

    public int getAppraisalId() {

        return appraisalId;
    }
}
