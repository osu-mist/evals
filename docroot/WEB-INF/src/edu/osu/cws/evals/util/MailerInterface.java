package edu.osu.cws.evals.util;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.models.EmailType;
import edu.osu.cws.evals.models.Employee;

import java.util.List;

public interface MailerInterface {
    boolean sendMail(Appraisal appraisal, EmailType emailType);

    String getStatusMsg(Appraisal appraisal, EmailType emailType) throws Exception;

    void sendSupervisorMail(Employee supervisor, String middleBody,
                            List<Email> emailList);

    void sendReviewerMail(String[] emailAddresses, int dueCount, int OverDueCount);

    void sendLateReport(String[] emailAddresses, String filePath, String bcName);

    String getAppraisalOverdueITWarning() throws Exception;
}
