package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.MailerInterface;

import java.util.List;

public class MockMailer implements MailerInterface {
    // used to check what type of email was sent
    private EmailType emailType;

    // used to change return value of sendMail()
    private boolean sendMailReturnValue = false;

    public boolean sendMail(Appraisal appraisal, EmailType emailType) {
        this.emailType = emailType;
        return sendMailReturnValue;
    }

    public String getStatusMsg(Appraisal appraisal, EmailType emailType) throws Exception {
        return "";
    }

    public void sendSupervisorMail(Employee supervisor, String middleBody,
                            List<Email> emailList) {
        return;
    }

    public void sendReviewerMail(String[] emailAddresses, int dueCount, int OverDueCount) {
        return;
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setSendMailReturnValue(boolean sendMailReturnValue) {
        this.sendMailReturnValue = sendMailReturnValue;
    }
}
