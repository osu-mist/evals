package edu.osu.cws.evals.util;

/**
 * Mailer: send email to the appropriate user/users give an appraisal and emailType
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * @date: 6/24/11
 */

import java.lang.reflect.Method;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.ReviewerMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.util.*;
import java.text.MessageFormat;

public class Mailer {

    private ResourceBundle emailBundle;
	private Mail email;
    private String linkURL;
    private String mimeType;
    private Map<String, Configuration> configMap;
    private String logHost;
    private Address[] replyTo = new Address[1];
    private EvalsLogger logger;

    Map<String, String> logFields = new HashMap<String, String>();

    public Mailer(ResourceBundle resources, Mail mail,
                  String linkURL, String mimeType, Map<String, Configuration> map, EvalsLogger logger, Address replyTo) {
        this.email = mail;
        this.emailBundle = resources;
        this.linkURL = linkURL;
        this.mimeType = mimeType;
        configMap = map;
        this.logger = logger;
        this.replyTo[0] = replyTo;
        //@todo make replyTo an address here, take a string in the constructor

        logFields.put("sub-facility","Mailer");
    }

    /**
     * Sends an email
     * @param appraisal - an Appraisal object
     * @param emailType - an EmailType
     * @throws Exception
     */
    public void sendMail(Appraisal appraisal, EmailType emailType) throws Exception {

        String logShortMessage = "";
        String logLongMessage = "";

        if (!(appraisal.getJob().getStatus().equals("A"))) {
            logShortMessage = "Email not sent";
            logLongMessage = "Appraisal " + appraisal.getId() +
                    " not available, job " + appraisal.getJob().getPositionNumber() +
                    " is not active.";
            logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
            return;
        }

        Message msg = email.getMessage();
        String mailTo = emailType.getMailTo();
        String mailCC = emailType.getCc();
        String mailBCC = emailType.getBcc();

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

        String addressee = getAddressee(appraisal,mailTo);

        String body = getBody(appraisal, emailType, addressee);

        msg.setContent(body, mimeType);
   
        msg.setReplyTo(replyTo);

        String subject = emailBundle.getString( "email_" + emailType.getType() + "_subject");

        msg.setSubject(subject);
        Transport.send(msg);

        Email email = new Email(appraisal.getId(), emailType.getType());
        EmailMgr.add(email);

        String recipientString = InternetAddress.toString(msg.getAllRecipients());

        logShortMessage = emailType.getType() + " email sent for appraisal " + appraisal.getId();
        logLongMessage = "email of type " + emailType.getType() + " addressed to " + addressee
                            + " sent to " + mailTo + " (" + recipientString + ")"
                            + " regarding appraisal " + appraisal.getId()
                            + " for " + getJobTitle(appraisal) + " " + getEmployeeName(appraisal);
        logger.log(Logger.INFORMATIONAL, logShortMessage, logLongMessage, logFields);
   }

    /**
     * get the recipients of a particular email
     * @param mailTo
     * @param appraisal
     * @return
     * @throws MessagingException
     *
     */
    private Address[] getRecipients(String mailTo, Appraisal appraisal) throws Exception {
        String[] mailToArray = mailTo.split(",");
        ArrayList recipients = new ArrayList();
        String logShortMessage = "";
        String logLongMessage = "";

        int i = 0;
        for (String recipient : mailToArray) {
            Job job = appraisal.getJob();
            if (recipient.equals("employee")) {
                if (job == null) {
                    logShortMessage = "Employee email not sent";
                    logLongMessage = "Job for appraisal " + appraisal.getId() + " is null";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    recipients.add(job.getEmployee().getEmail());
                }
            }

            if (recipient.equals("supervisor")) {
                if (job == null) {
                    logShortMessage = "Supervisor email not sent";
                    logLongMessage = "Job for appraisal " + appraisal.getId() + " is null";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    Job supervisorJob = job.getSupervisor();
                    if (supervisorJob == null) {
                        logShortMessage = "Supervisor email not sent";
                        logLongMessage = "Supervisor for appraisal " + appraisal.getId() + " is null";
                        logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                    } else {
                        recipients.add(supervisorJob.getEmployee().getEmail());
                    }
                }
            }

            if (recipient.equals("reviewer")) {
                if (job == null) {
                    logShortMessage = "Reviewer email not sent";
                    logLongMessage = "Job for appraisal " + appraisal.getId() + " is null";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    String bcName = job.getBusinessCenterName();
                    List<Reviewer> reviewers = ReviewerMgr.getReviewers(bcName);
                    if (reviewers == null) {
                        logShortMessage = "Reviewer email not sent";
                        logLongMessage = "Reviewers for appraisal " + appraisal.getId() + " is null";
                        logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                    } else {
                        for (Reviewer reviewer : reviewers) {
                            recipients.add(reviewer.getEmployee().getEmail());
                        }
                    }
                }
            }

        }

        //@todo: For testing purposes. Remove when testing is done.
        recipients = new ArrayList();
        recipients.add("joan.lu@oregonstate.edu");


        Address[] recipientsArray = new Address[recipients.size()];
        for (Object address : recipients) {
            recipientsArray[i++] = email.stringToAddress((String) address);
        }

        return recipientsArray;
    }

    /**
     * fetch the standard parts of the email body, and then delegate a specific method
     * to retrieve the body for that specific emailType
     * @param appraisal
     * @param emailType
     * @return
     * @throws Exception
     */
    private String getBody(Appraisal appraisal, EmailType emailType, String addressee) throws Exception {
        String bodyWrapper = emailBundle.getString("email_body");
        String bodyContent = getStatusMsg(appraisal, emailType);
        return MessageFormat.format(bodyWrapper, addressee,
                bodyContent, getBusinessCenterDescriptor(appraisal), linkURL, linkURL);
    }


    /**
     * gets the status message
     * @param appraisal
     * @param emailType
     * @return
     * @throws Exception
     */
    public String getStatusMsg(Appraisal appraisal, EmailType emailType) throws Exception {
        String statusMsg = "";
        String bodyMethodId = emailType.getType() + "Body";
        Method bodyMethod;

        if (!bodyMethodId.equals("Body")) {
            bodyMethod = Mailer.class.getDeclaredMethod(bodyMethodId, Appraisal.class);
            statusMsg = (String) bodyMethod.invoke(this, appraisal);
        }
        return statusMsg;
    }

    /**
     * Send batched email to a supervisor
     * @param supervisor
     * @param middleBody
     * @param emailList
     * @throws Exception
     */
    public void sendSupervisorMail(Employee supervisor,String middleBody,
                                   List<Email> emailList) throws Exception {

        String bcName = JobMgr.getBusinessCenter(supervisor.getId());
        //System.out.println("bcname = " +bcName);
        if (bcName == null) //supervisor has no job, Error
        {
            String shortMsg = "From sendSupervisorMail: supervisor has no active job";
            String longMsg = "Supervisor " + supervisor.getName() + ", " +
                    supervisor.getId() + ", has no active job.";
            logger.log(Logger.ERROR, shortMsg, longMsg);
            return;
        }

        String bcKey = "businesscenter_" + bcName + "_descriptor";
        String bcDescritor = emailBundle.getString(bcKey);

        String supervisorName = supervisor.getConventionName();
        String emailAddress = supervisor.getEmail();
        //@todo: take this out after testing:
        emailAddress = "joan.lu@oregonstate.edu";

        String bodyWrapper = emailBundle.getString("email_body");

        String body = MessageFormat.format(bodyWrapper, supervisorName,
                    middleBody, bcDescritor, linkURL, linkURL);
        Message msg = email.getMessage();

        Address to = email.stringToAddress(emailAddress);
        String supervisorSubject = emailBundle.getString("email_supervisor_subject");

        msg.addRecipient(Message.RecipientType.TO, to);
        msg.setContent(body, mimeType);
        msg.setSubject(supervisorSubject);

        Transport.send(msg);
        EmailMgr.add(emailList);

        for (Email email : emailList) {
            Integer appraisalId = email.getAppraisalId();
            String emailType = email.getEmailType();
            email.getEmailType();
            String logStatus = Logger.INFORMATIONAL;
            String logShortMessage = emailType + " email sent to for appraisal " + appraisalId;
            String logLongMessage = emailType + " mail sent to " + emailAddress + " for appraisal " + appraisalId; 

            logger.log(logStatus, logShortMessage, logLongMessage);
        }
    }

    /**
     * Send email to reviewers of one business center
     * @param emailAddresses
     * @param dueCount
     * @param OverDueCount
     * @throws Exception
     */
    public void sendReviewerMail(String[] emailAddresses, int dueCount, int OverDueCount)
            throws Exception {
        String bodyString = emailBundle.getString("email_reviewers");
        String msgs = emailBundle.getString("email_reviewDue_body");
        String middleBody = MessageFormat.format(msgs, dueCount, OverDueCount);
        String body = MessageFormat.format(bodyString, middleBody, linkURL, linkURL);
        Message msg = email.getMessage();
        String reviewerSubject = emailBundle.getString("email_reviewer_subject");

        Address[] recipients = new Address[emailAddresses.length];
        int i = 0;
        for (String recipient : emailAddresses) {
            recipients[i++] = email.stringToAddress(recipient);
        }

        msg.addRecipients(Message.RecipientType.TO, recipients);
        msg.setContent(body, mimeType);
        msg.setSubject(reviewerSubject);
        Transport.send(msg);

        logger.log("INFORMATION", "Email sent to reviewers", "");

    }

    /**
     * Fetch the body for a particular emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

    /**
     * Fetch the body for a particular emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for goalsRequiredModification emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsRequiredModificationBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_goalsRequiredModification_body");
    }

    /**
     * Fetch the body for goalsApprovalDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsApprovalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for goalsApprovalOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsApprovalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApprovalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for goalsApproved emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsApprovedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApproved_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for a goalsReactivated emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivated_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for a resultsDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String resultsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

    /**
     * Fetch the body for a resultsOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String resultsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for appraisalDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String appraisalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal),
                appraisal.getReviewPeriod(), getDaysRemaining(appraisal));
    }

    /**
     * Fetch the body for appraisalOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String appraisalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for releaseDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String reviewDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_reviewDue_body");
                String businessCenterName = appraisal.getJob().getBusinessCenterName();
        int dueCount = AppraisalMgr.getReviewDueCount(businessCenterName);
        return MessageFormat.format(bodyString, dueCount);
    }

    /**
     * Fetch the body for releaseOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String reviewOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_reviewOverdue_body");
        String businessCenterName = appraisal.getJob().getBusinessCenterName();
        //AppraisalMgr.getReviewDueCount(businessCenterName);
        int dueCount = AppraisalMgr.getReviewOvedDueCount(businessCenterName);
        return MessageFormat.format(bodyString, dueCount);
    }

    /**
     * Fetch the body for releaseDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String releaseDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for releaseOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String releaseOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_releaseOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for signatureDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String signatureDueBody(Appraisal appraisal) throws Exception {
         return emailBundle.getString("email_signatureDue_body");
    }

    /**
     * Fetch the body for signatureOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String signatureOverdueBody(Appraisal appraisal) throws Exception {
        return emailBundle.getString("email_signatureOverdue_body");
    }

    /**
     * Fetch the body for rebuttalReadDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalRead_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for rebuttalReadDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadDue_body");
        String osuid = appraisal.getJob().getEmployee().getOsuid();

        return MessageFormat.format(bodyString, getEmployeeName(appraisal),
                osuid, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for rebuttalReadOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for completed emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String completedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_completed_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for closed emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String closedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_closed_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for closed emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String jobTerminatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_jobTerminated_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal));
    }

    /**
     * Fetch the business center descriptor
     * @param appraisal
     * @return
     */
    private String getBusinessCenterDescriptor(Appraisal appraisal) {
        Job job = appraisal.getJob();
        String bcName = job.getBusinessCenterName();
        String bcKey = "businesscenter_" + bcName + "_descriptor";
        return emailBundle.getString(bcKey);
    }

    /**
     * Fetch the title of the job for a specific appraisal
     * @param appraisal
     * @return
     */
    private String getJobTitle(Appraisal appraisal) {
        return (appraisal.getJob().getJobTitle());
    }

    /**
     * Fetch the full name of the employee for a particular appraisal
     * @param appraisal
     * @return
     */
    private String getEmployeeName(Appraisal appraisal) {
        Job job = appraisal.getJob();
        Employee employee = job.getEmployee();
        return employee.getConventionName();
    }

    /**
     * Fetch the days remaining to respond to a particular action
     * @param appraisal
     * @return
     * @throws Exception
     */
    private int getDaysRemaining(Appraisal appraisal) throws Exception {
        String status = appraisal.getStatus();
        Configuration config = configMap.get(status);
        Date dueDay = EvalsUtil.getDueDate(appraisal, config);
        return CWSUtil.getRemainDays(dueDay);
    }

    /**
     * Get the correct address name used in the "Dear ..." line
     * @param appraisal
     * @param mailTo
     * @return
     * @throws Exception
     */
    private String getAddressee(Appraisal appraisal, String mailTo) throws Exception {
        if (mailTo.indexOf("employee") > -1) {
            return appraisal.getJob().getEmployee().getConventionName();
        }
        if (mailTo.indexOf("supervisor") > -1) {
            return appraisal.getJob().getSupervisor().getEmployee().getConventionName();
        }
        if (mailTo.indexOf("reviewer") > -1) {
            return "Reviewer";
        }
        return "";
    }
}
