package edu.osu.cws.pass.util;

/**
 * Mailer: send email to the appropriate user/users give an appraisal and emailType
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * Date: 6/24/11
 */
import java.lang.reflect.Method;
import javax.mail.*;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.util.*;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public class Mailer {

    private ResourceBundle emailBundle;
	private Mail email;
    private String linkURL;
    private String mimeType;
    private Map<String, Configuration> configMap;

    public Mailer(ResourceBundle resources, Mail mail,
                  String linkURL, String mimeType, Map<String, Configuration> map) {
        this.email = mail;
        this.emailBundle = resources;
        this.linkURL = linkURL;
        this.mimeType = mimeType;
        configMap = map;
    }

/* send an email
 * @param appraisal - an Appriasal object
 * @param emailType - an EmailType
 *
 */
    public void sendMail(Appraisal appraisal, EmailType emailType) throws Exception {

        Message msg = email.getMessage();
        String mailTo = emailType.getMailTo();
        String mailCC = emailType.getCc();
        String mailBCC = emailType.getBcc();
        String bodyMethodId = emailType.getType() + "Body";

        if (mailTo != null && !mailTo.equals("")) {
            Address[] to = getRecipients(mailTo, appraisal);
            msg.addRecipients(Message.RecipientType.TO, to);
        }

	    if (mailCC != null && !mailCC.equals("")) {
            Address[] cc = getRecipients(mailCC, appraisal);
            msg.addRecipients(Message.RecipientType.CC, cc);
        }

	    if (mailBCC != null && !mailBCC.equals("")) {
            Address[] bcc = getRecipients(mailBCC, appraisal);
            msg.addRecipients(Message.RecipientType.BCC, bcc);
        }

        String body = getBody(appraisal, bodyMethodId);

        msg. setContent(body, "text/html");

        String subject = emailBundle.getString( "email_" + emailType.getType() + "_subject");

        msg.setSubject(subject);
        Transport.send(msg);
   }

/* get the recipients of a particular email
 * @param appraisal - an Appriasal object
 * @param emailType - an EmailType
 * @return array of recipient addresses
 */
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

/* fetch the standard parts of the email body, and then delegate a specific method
 * to retrieve the body for that specific emailType
 * @param appraisal - an Appriasal object
 * @param bodyMethodId - the name of the method to call
 * @return compelete email body
 */
    private String getBody(Appraisal appraisal, String bodyMethodId) throws Exception {
        String bodyWrapper = emailBundle.getString("email_body");
        Method bodyMethod;
        String bodyContent = "";

        if (!bodyMethodId.equals("")) {
            bodyMethod = Mailer.class.getDeclaredMethod(bodyMethodId, String.class,
                    String.class, String.class, Integer.class);

            bodyContent = (String) bodyMethod.invoke(this, appraisal);
        }

        return MessageFormat.format(bodyWrapper, getEmployeeName(appraisal),
                bodyContent, getBusinessCenterDescriptor(appraisal), linkURL);
    }

/* Fetch the body for a particular emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

/* Fetch the body for a particular emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

/* Fetch the body for goalsApproved emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsApprovedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApproved_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

/* Fetch the body for goalsRequiredModification emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsRequiredModificationBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_RequiredModification_body");
    }

/* Fetch the body for a goalsReactivated emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsReactivatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivated_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod());
    }

/* Fetch the body for a resultsDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String resultsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

/* Fetch the body for a resultsOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String resultsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

/* Fetch the body for goalsApprovalDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsApprovalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for goalsApprovalOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String goalsApprovalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for appraisalDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String appraisalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

/* Fetch the body for appraisalOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String appraisalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), appraisal.getReviewPeriod());
    }

/* Fetch the body for releaseDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String releaseDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for releaseOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String releaseOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for signatureDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String signatureDueBody(Appraisal appraisal) throws Exception {
         return emailBundle.getString("email_signatureDue_body");
    }

/* Fetch the body for signatureOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String signatureOverdueBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_signatureOverdue_body");
    }

/* Fetch the body for rebuttalReadDue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String rebuttalReadDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for rebuttalReadOverdue emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String rebuttalReadOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

/* Fetch the body for completed emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String completedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_completed_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod());
    }

/* Fetch the body for closed emailType
 * @param appraisal - an Appriasal object
 * @return emailType specific email content
 */
    private String closedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_closed_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

/* Fetch the business center descriptor
 * @param appraisal - an Appriasal object
 * @return full name of business center
 */
    private String getBusinessCenterDescriptor(Appraisal appraisal) {
        Job job = appraisal.getJob();
        String bcName = job.getBusinessCenterName();
        String bcKey = "businesscenter_" + bcName + "_descriptor";
        return emailBundle.getString(bcKey);
    }

/* Fetch the title of the job for a specific appraisal
 * @param appraisal - an Appriasal object
 * @return job title
 */
    private String getJobTitle(Appraisal appraisal) {
        return appraisal.getJob().getJobTitle();
    }

/* Fetch the full name of the employee for a particular appraisal
 * @param appraisal - an Appriasal object
 * @return name
 */
    private String getEmployeeName(Appraisal appraisal) {
        Job job = appraisal.getJob();
        Employee employee = job.getEmployee();
        return employee.getConventionName();
    }

/* Fetch the days remaining to respond to a particular action
 * @param appraisal - an Appriasal object
 * @return number of days
 */
    private Integer getDaysRemaining(Appraisal appraisal) throws Exception {
        Configuration config = configMap.get(appraisal.getStatus());
        Date dueDay = PassUtil.getDueDate(appraisal, config);
        return CWSUtil.daysBetween(new Date(), dueDay);
    }

}
