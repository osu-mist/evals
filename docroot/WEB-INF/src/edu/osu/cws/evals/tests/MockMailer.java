package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.MailerInterface;

import java.util.ArrayList;
import java.util.List;

public class MockMailer implements MailerInterface {
    // used to check what type of email was sent
    private EmailType emailType;

    // used to change return value of sendMail()
    private boolean sendMailReturnValue = false;

    // used to check whether or not send supervisor was called and the parameters
    private ArrayList<Integer> supervisorIds = new ArrayList<Integer>();

    // used to keep track of email count sent to bc reviewers.
    private int reviewOverdueCount = 0;
    private int reviewDueCount = 0;
    private String[] reviewerEmails;
    private int sendReviewerCallsCount = 0;

    public boolean sendMail(Appraisal appraisal, EmailType emailType) {
        this.emailType = emailType;
        return sendMailReturnValue;
    }

    public String getStatusMsg(Appraisal appraisal, EmailType emailType) throws Exception {
        return "";
    }

    public void sendSupervisorMail(Employee supervisor, String middleBody,
                            List<Email> emailList) {
        this.supervisorIds.add(supervisor.getId());
    }

    public void sendReviewerMail(String[] emailAddresses, int dueCount, int OverDueCount) {
        sendReviewerCallsCount++;
        reviewDueCount = dueCount;
        reviewOverdueCount = OverDueCount;
    }


    public String getAppraisalOverdueITWarning() throws Exception {
        return "";
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public int getSendReviewerCallsCount() {
        return sendReviewerCallsCount;
    }

    public int getReviewOverdueCount() {
        return reviewOverdueCount;
    }

    public int getReviewDueCount() {
        return reviewDueCount;
    }

    public void setSendMailReturnValue(boolean sendMailReturnValue) {
        this.sendMailReturnValue = sendMailReturnValue;
    }

    public ArrayList<Integer> getSupervisorIds() {
        return supervisorIds;
    }
}
