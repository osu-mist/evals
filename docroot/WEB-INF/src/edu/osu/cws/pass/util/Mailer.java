package edu.osu.cws.pass.util;

/**
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * Date: 6/24/11
 */
import java.lang.reflect.Method;
import javax.mail.*;
import javax.mail.internet.NewsAddress;
import javax.xml.stream.events.StartDocument;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.util.*;
import edu.osu.cws.pass.util.PassUtil;
import org.hibernate.loader.custom.Return;

import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public class Mailer {

    /**
     * Body and subject from resource Bundle
	 * body a dynamic message
     * Instantiate an Email object
     * and call its send function
     *
     */
    private ResourceBundle emailBundle;
	private Mail email;
    private String linkURL;
    private String mimeType;
    private Map<String, Configuration> configMap;
    
 	/** gets the text from the emailResources, format it, and call the send function of
        * of the Email class.
        */

    public Mailer(ResourceBundle resources, Mail mail,
                  String linkURL, String mimeType, Map<String, Configuration> map) {
        this.email = mail;
        this.emailBundle = resources;
        this.linkURL = linkURL;
        this.mimeType = mimeType;
        configMap = map;

    }

    public void sendMail(Appraisal appraisal, EmailType emailType) throws Exception {

        Message msg = email.getMessage();
        String mailTo = emailType.getMailTo();

        if (mailTo != null && !mailTo.equals("")) {
            Address[] to = getRecipients(mailTo, appraisal);
            msg.addRecipients(Message.RecipientType.TO, to);
        }

        String mailCC = emailType.getCc();

	    if (mailCC != null) {
            Address[] cc = getRecipients(mailCC, appraisal);
            msg.addRecipients(Message.RecipientType.CC, cc);
        }

        String mailBCC = emailType.getBcc();

	    if (mailBCC != null) {
            Address[] bcc = getRecipients(mailBCC, appraisal);
            msg.addRecipients(Message.RecipientType.BCC, bcc);
        }

        String body = getBody(appraisal, emailType);

        msg. setContent(body, "text/html");
        String subjectResourceKey = "email_" + emailType.getType() + "_subject";
        String subject = emailBundle.getString(subjectResourceKey);

        msg.setSubject(subject);
        Transport.send(msg);
   }

    private Address[] getRecipients(String mailTo, Appraisal appraisal) throws MessagingException {
        String[] mailToArray = mailTo.split(",");
        Address[] recipients = new Address[mailToArray.length];
        String contact = "";
        int i = 0;
        for (String recipient : mailToArray) {
            if (recipient.equals("employee")) {
                contact = appraisal.getJob().getEmployee().getEmail();
            }
            if (recipient.equals("supervisor")) {
                contact = appraisal.getJob().getSupervisor().getEmployee().getEmail();
            }

            recipients[i++] = email.stringToAddress(contact);
        }
        return recipients;
    }

    private String getBody(Appraisal appraisal, EmailType emailType) throws Exception {
        String bodyWrapper = emailBundle.getString("email_body");
        Method bodyMethod;
        String bodyContent = "";
        String bodyMethodId = emailType.getType() + "Body";

        if (!bodyMethodId.equals("")) {
            bodyMethod = Mailer.class.getDeclaredMethod(bodyMethodId, String.class, String.class, String.class, Integer.class);
            bodyContent = (String) bodyMethod.invoke(this, appraisal);
        }

        return MessageFormat.format(bodyWrapper, getEmployeeName(appraisal), bodyContent, getBusinessCenterDescriptor(appraisal), linkURL);

    }

    private String goalsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                getEvaluationPeriod(appraisal), getDaysRemaining(appraisal));
    }

    private String goalsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), getEvaluationPeriod(appraisal));
    }

    private String goalsApprovedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApproved_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), getEvaluationPeriod(appraisal));
    }

    private String goalsRequiredModificationBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_RequiredModification_body");
    }

    private String goalsReactivatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivated_body");
        return MessageFormat.format(bodyString, getEvaluationPeriod(appraisal));
    }

    private String resultsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                getEvaluationPeriod(appraisal), getDaysRemaining(appraisal));
    }

    private String resultsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), getEvaluationPeriod(appraisal));
    }

    private String goalsApprovalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String goalsApprovalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String appraisalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal),
                getEvaluationPeriod(appraisal), getDaysRemaining(appraisal));
    }

    private String appraisalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getEvaluationPeriod(appraisal));
    }

    private String releaseDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String releaseOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String signatureDueBody(Appraisal appraisal) throws Exception {
         return emailBundle.getString("email_signatureDue_body");
    }

    private String signatureOverdueBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_signatureOverdue_body");
    }

    private String rebuttalReadDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String rebuttalReadOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                getEvaluationPeriod(appraisal));
    }

    private String completedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_completed_body");
        return MessageFormat.format(bodyString, getEvaluationPeriod(appraisal));
    }

    private String closedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_closed_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), getEvaluationPeriod(appraisal));
    }
    
    private String getBusinessCenterDescriptor(Appraisal appraisal) {
        Job job = appraisal.getJob();
        String bcName = job.getBusinessCenterName();
        String bcKey = "businesscenter_" + bcName + "_descriptor";
        return emailBundle.getString(bcKey);
    }

    private String getJobTitle(Appraisal appraisal) {
        return appraisal.getJob().getJobTitle();
    }

    private String getEmployeeName(Appraisal appraisal) {
        Job job = appraisal.getJob();
        Employee employee = job.getEmployee();
        return employee.getConventionName();
    }
    
    private Integer getDaysRemaining(Appraisal appraisal) throws Exception {
        Configuration config = configMap.get(appraisal.getStatus());
        Date dueDay = PassUtil.getDueDate(appraisal, config);
        return CWSUtil.daysBetween(new Date(), dueDay);
    }

    private String getEvaluationPeriod(Appraisal appraisal) {
        Date start = appraisal.getStartDate();
        Date end = appraisal.getEndDate();
        return start.toString() + " to " + end.toString();
    }

}
