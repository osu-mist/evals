package edu.osu.cws.evals.util;

/**
 * Mailer: send email to the appropriate user/users give an appraisal and emailType
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * @date: 6/24/11
 */

import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.CWSUtil;
import edu.osu.cws.util.Logger;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joda.time.DateTime;
import org.joda.time.Days;

import javax.mail.MessagingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

public class Mailer implements MailerInterface {
    private ResourceBundle emailBundle;
    private String hostName;
    private String from;
    private String linkURL;
    private String helpLinkURL;
    private Map<String, Configuration> configMap;
    private String replyTo;
    private LoggingInterface logger;
    private String testMailToAddress;

    Map<String, String> logFields = new HashMap<String, String>();

    private static final List<String> commentTypeEmails = Arrays.asList(
            "rebuttalReadDue",
            "rebuttalRead"
    );

    /**
     * Constructors that sets the object parameters and initializes the email start date.
     *
     * @param resources
     * @param hostName
     * @param from
     * @param linkURL
     * @param helpLinkURL
     * @param map
     * @param logger
     * @param replyTo
     */
    public Mailer(ResourceBundle resources, String hostName, String from, String linkURL,
                  String helpLinkURL, Map<String, Configuration> map, LoggingInterface logger,
                  String replyTo, String testMailToAddress) {
        this.emailBundle = resources;
        this.from = from;
        this.hostName = hostName;
        this.linkURL = linkURL;
        this.helpLinkURL = helpLinkURL;
        configMap = map;
        this.logger = logger;
        this.replyTo = replyTo;
        this.testMailToAddress = testMailToAddress;
    }

    /**
     * Sends an email
     * @param appraisal - an Appraisal object
     * @param emailType - an EmailType
     * @throws Exception
     */
    public boolean sendMail(Appraisal appraisal, EmailType emailType) {
        String logShortMessage = "";
        String logLongMessage = "";

        try {
            if (!appraisal.isEmployeeJobActive()) {
                logShortMessage = "Email not sent";
                logLongMessage = "Appraisal " + appraisal.getId() +
                        " not available, job is not active.";
                logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                return false;
            }

            HtmlEmail email = getHtmlEmail();
            if (!setEmailRecipients(appraisal, email, emailType)) {
                return false;
            }

            String addressee = getAddressee(appraisal, emailType.getMailTo());
            String body = getBody(appraisal, emailType, addressee);
            email.setHtmlMsg(body);

            String subject = emailBundle.getString("email_" + emailType.getType() + "_subject");
            if (commentTypeEmails.contains(emailType.getType())) {
                subject = MessageFormat.format(subject, getCommentType(appraisal));
            }

            email.setSubject(subject);
            email.send();

            Email evalsEmail = new Email(appraisal.getId(), emailType.getType());
            EmailMgr.add(evalsEmail);

            logShortMessage = emailType.getType() + " email sent for appraisal " + appraisal.getId();
            logLongMessage = "email of type " + emailType.getType() + " sent regarding appraisal " + appraisal.getId();
            logger.log(Logger.INFORMATIONAL, logShortMessage, logLongMessage, logFields);
        } catch (Exception e) {
            try {
                logShortMessage = "Email not sent";
                String stackTrace = replaceEmails(CWSUtil.stackTraceString(e), "email address removed");
                logLongMessage = "Error encountered when sending mail for appraisal = " +
                        appraisal.getId() + "\n" + stackTrace;
                logger.log(Logger.ERROR,logShortMessage,logLongMessage);
            } catch (Exception logError) { }
            return false;
        }

        return true;
    }

    /**
     * Figures out if the email text should say rebuttal or comment based on the appointment type.
     *
     * @param appraisal
     * @return
     */
    private String getCommentType(Appraisal appraisal) {
        String commentType = "rebuttal";
        if (appraisal.getJob().isUnclassified()) {
            commentType = "response";
        }
        return commentType;
    }

    /**
     * Looks at the email type object and sets the cc, bcc and to fields in the email object.
     * If there were no recipients to add, it returns false.
     *
     * @param appraisal
     * @param email
     * @param emailType
     * @return
     * @throws Exception
     */
    private boolean setEmailRecipients(Appraisal appraisal, HtmlEmail email, EmailType emailType)
            throws Exception {
        boolean hasRecipients = false;
        String mailTo = emailType.getMailTo();
        String mailCC = emailType.getCc();
        String mailBCC = emailType.getBcc();

        // Modifying the CC recipient for the signature emails to include Classified. This was the simplest
        // solution. Other things to consider in the future is: 1) use a different appraisal step + email type
        // or 2) allow email_types to have an appointment type column
        if (appraisal.getAppointmentType().equals(AppointmentType.CLASSIFIED_IT) &&
                emailType.getType().contains("signature")) {
            mailCC = "reviewer";
        }

        if (mailTo != null && !mailTo.equals("")) {
            String[] to = getRecipients(mailTo, appraisal);
            if (to != null && to.length != 0) {
                email.addTo(to);
                hasRecipients = true;
            }
        }

        if (mailCC != null && !mailCC.equals("")) {
            String[] cc = getRecipients(mailCC, appraisal);
            if (cc != null && cc.length != 0) {
                email.addCc(cc);
                hasRecipients = true;
            }
        }

        if (mailBCC != null && !mailBCC.equals("")) {
            String[] bcc = getRecipients(mailBCC, appraisal);
            if (bcc != null && bcc.length != 0) {
                email.addBcc(bcc);
                hasRecipients = true;
            }
        }
        return hasRecipients;
    }

    private HtmlEmail getHtmlEmail() throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(hostName);
        email.setFrom(from);
        email.addReplyTo(replyTo);
        return email;
    }

    /**
     * get the recipients of a particular email
     * @param mailTo
     * @param appraisal
     * @return
     * @throws MessagingException
     *
     */
    private String[] getRecipients(String mailTo, Appraisal appraisal) throws Exception {
        String[] mailToArray = mailTo.split(",");
        ArrayList<String> recipients = new ArrayList<String>();
        String logShortMessage = "";
        String logLongMessage = "";

        // Get the appraisal job and check that it's valid.
        Job job = appraisal.getJob();
        if (job == null) {
            logShortMessage = "Email not sent";
            logLongMessage = "Job for appraisal " + appraisal.getId() + " is null";
            logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
            return null;
        }

        if(testMailToAddress != null && !testMailToAddress.equals("")){
           mailToArray[0] = testMailToAddress;
           return mailToArray;
        }

        for (String recipient : mailToArray) {
            if (recipient.equals("employee")) {
                addToEmailList(job.getEmployee(), recipients);
            }

            if (recipient.equals("supervisor")) {
                Job supervisorJob = job.getSupervisor();
                if (supervisorJob == null) {
                    logShortMessage = "Supervisor email not sent";
                    logLongMessage = "Supervisor for appraisal " + appraisal.getId() + " is null";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    addToEmailList(supervisorJob.getEmployee(), recipients);
                }
            }

            if(recipient.equals("upper supervisor")) {
                Job upperSupervisorJob = job.getSupervisor().getSupervisor();
                if (upperSupervisorJob == null) {
                    logShortMessage = "Upper Supervisor email not sent";
                    logLongMessage = "Upper Supervisor for appraisal " + appraisal.getId() + " is null";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    addToEmailList(upperSupervisorJob.getEmployee(), recipients);
                }
            }

            if (recipient.equals("reviewer")) {
                String bcName = job.getBusinessCenterName();
                List<Reviewer> reviewers = ReviewerMgr.getReviewers(bcName);
                if (reviewers == null) {
                    logShortMessage = "Reviewer email not sent";
                    logLongMessage = "No reviewers were found for the business center " +
                            bcName + " for which appraisal " + appraisal.getId() + " belongs to.";
                    logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                } else {
                    for (Reviewer reviewer : reviewers) {
                        addToEmailList(reviewer.getEmployee(), recipients);
                    }
                }
            }
        }

        return recipients.isEmpty()? null :  recipients.toArray(new String[recipients.size()]);
    }

    /**
     * Handles logging employees that have a null email address.
     *
     * @param employee  Employee with invalid email address
     * @throws Exception
     */
    private void logNullEmail(Employee employee) throws Exception {
        String logShortMessage;
        String logLongMessage;
        logShortMessage = employee.getId() + " has a blank or null email address";
        logLongMessage = " PIDM = " + employee.getId() +" does not have a valid email address";
        logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
    }

    /**
     * Adds an employee's email to an array list. If the email is null or empty string it logs
     * the data error.
     *
     * @param employee      Employee/Reviewer/Supervisor that we're trying to email
     * @param recipients    List of recipient email addresses
     * @throws Exception
     */
    private void addToEmailList(Employee employee, List<String> recipients)
            throws Exception {
        String email = employee.getEmail();
        if (email == null || email.equals("")) {
            logNullEmail(employee);
        } else {
            recipients.add(email);
        }
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
                bodyContent, getBusinessCenterDescriptor(appraisal), linkURL, linkURL, helpLinkURL, helpLinkURL);
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
    public void sendSupervisorMail(Employee supervisor, String middleBody,
                                   List<Email> emailList) {
        try {
            String bcDescritor = getBCDescriptor(supervisor);
            if (bcDescritor == null) {
                return;
            }

            String supervisorName = supervisor.getConventionName();
            String emailAddress = supervisor.getEmail();

            if(testMailToAddress != null && !testMailToAddress.equals("")){
                emailAddress = testMailToAddress;
            }

            if (emailAddress == null || emailAddress.equals("")) {
                logNullEmail(supervisor);
                return;
            }

            String bodyWrapper = emailBundle.getString("email_body");
            String body = MessageFormat.format(bodyWrapper, supervisorName,
                        middleBody, bcDescritor, linkURL, linkURL, helpLinkURL, helpLinkURL);

            HtmlEmail email = getHtmlEmail();
            email.addTo(emailAddress);
            email.setHtmlMsg(body);
            email.setSubject(emailBundle.getString("email_supervisor_subject"));
            email.send();
            EmailMgr.add(emailList);

            for (Email evalsEmail : emailList) {
                Integer appraisalId = evalsEmail.getAppraisalId();
                String emailType = evalsEmail.getEmailType();
                String logStatus = Logger.INFORMATIONAL;
                String logShortMessage = emailType + " email sent for appraisal " + appraisalId;
                String logLongMessage = emailType + " mail sent to supervisor with PIDM: " +
                        supervisor.getId() + " for appraisal " + appraisalId;

                logger.log(logStatus, logShortMessage, logLongMessage);
            }
        } catch (Exception e) {
            String logLongMessage = "";
            String shortMessage = "Error in sendSupervisorMail";
            try {
                logLongMessage = "Error encountered when sending mail to supervisor with PIDM: " +
                        supervisor.getId() + "\n" + CWSUtil.stackTraceString(e);
                logger.log(Logger.ERROR, shortMessage, logLongMessage);
            } catch (Exception logError) { }
        }
    }

    /**
     * Checks that the supervisor job exists and returns the BC descriptor for the
     * supervisor's job bc.
     *
     * @param supervisor
     * @return
     * @throws Exception
     */
    private String getBCDescriptor(Employee supervisor) throws Exception {
        String bcName = JobMgr.getBusinessCenter(supervisor.getId());
        if (bcName == null) { //supervisor has no job, Error
            String shortMsg = "From sendSupervisorMail: supervisor has no active job";
            String longMsg = "Supervisor PIDM: " + supervisor.getId() +
                    ", has no active job.";
            logger.log(Logger.ERROR, shortMsg, longMsg);
            return null;
        }

        return emailBundle.getString("businesscenter_" + bcName + "_descriptor");
    }

    /**
     * Send email to reviewers of one business center
     * @param emailAddresses
     * @param dueCount
     * @param OverDueCount
     * @throws Exception
     */
    public void sendReviewerMail(String[] emailAddresses, int dueCount, int OverDueCount) {
        try {
            String bodyString = emailBundle.getString("email_reviewers");
            String msgs = emailBundle.getString("email_reviewDue_body");
            String middleBody = MessageFormat.format(msgs, dueCount, OverDueCount);
            String body = MessageFormat.format(bodyString, middleBody, linkURL, linkURL);

            HtmlEmail email = getHtmlEmail();

            if(testMailToAddress != null && !testMailToAddress.equals("")){
                for(int i = 0; i < emailAddresses.length; i ++){
                    emailAddresses[i] = testMailToAddress;
                }
            }

            email.addTo(emailAddresses);
            email.setHtmlMsg(body);
            email.setSubject(emailBundle.getString("email_reviewer_subject"));
            email.send();

            String longMsg = "Emails sent to: various reviewers";
            logger.log(Logger.INFORMATIONAL, "Reviewer emails sent", longMsg);
        } catch (Exception e) {
            String logLongMessage = "";
            String shortMessage = "Error in sendReviewerMail";
            try {
                logLongMessage = "Error encountered when sending mail to reviewers" +
                        "\n" + CWSUtil.stackTraceString(e);
                logger.log(Logger.ERROR, shortMessage, logLongMessage);
            } catch (Exception logError) { }
        }

    }

    /**
     * Sends email to either admin/bc users with attachment of late evaluations csv report.
     *
     * @param emailAddresses
     * @param filePath
     * @param bcName                    BC name that the report is being sent to.
     */
    public void sendLateReport(String[] emailAddresses, String filePath, String bcName) {
        try {
            String body = emailBundle.getString("email_lateReport_body");
            HtmlEmail email = getHtmlEmail();
            String month = new DateTime().monthOfYear().getAsText();

            if(testMailToAddress != null && !testMailToAddress.equals("")){
                for(int i = 0; i < emailAddresses.length; i ++) {
                    emailAddresses[i] = testMailToAddress;
                }
            }

            // Create the attachment
            String filename = "EvalS-lateReport-" + bcName + "-" + month + ".csv";
            EmailAttachment attachment = new EmailAttachment();
            attachment.setPath(filePath);
            attachment.setDisposition(EmailAttachment.ATTACHMENT);
            attachment.setDescription(filename);
            attachment.setName(filename);

            email.attach(attachment);
            email.addTo(emailAddresses);
            email.setHtmlMsg(body);
            email.setSubject(emailBundle.getString("email_lateReport_subject"));
            email.send();

            String longMsg = "Late report emails sent to: various reviewers";
            logger.log(Logger.INFORMATIONAL, "Late Report email sent", longMsg);

            Email evalsEmail = new Email(0, "lateReport" + bcName);
            EmailMgr.add(evalsEmail);
        } catch (Exception e) {
            String logLongMessage = "";
            String shortMessage = "Error in sendLateReport";
            try {
                logLongMessage = "Error encountered when sending mail to reviewers" +
                        "\n" + CWSUtil.stackTraceString(e);
                logger.log(Logger.ERROR, shortMessage, logLongMessage);
            } catch (Exception logError) { }
        }
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
                appraisal.getReviewPeriod(), getDueDate(appraisal, null), getDaysRemaining(appraisal, false));
    }

    /**
     * Fetch the body for a particular emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for goalsRequiredModification emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsRequiredModificationBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsRequiredModification_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDaysRemaining(appraisal, false));
    }

    /**
     * Fetch the body for goalsReactivatedTimeout emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivatedTimeoutBody(Appraisal appraisal) throws Exception {
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "goalsReactivatedExpiration",
                appraisal.getAppointmentType());
        int daysToSubmit = config.getIntValue();
        String bodyString = emailBundle.getString("email_goalsReactivatedTimeout_body");
        return MessageFormat.format(bodyString, daysToSubmit, getJobTitle(appraisal),
                appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for goalsReactivationRequestedTimeout emailType
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivationRequestedTimeoutBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivationRequestedTimeout_body");
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "goalsReactivationRequestedExpiration",
                appraisal.getAppointmentType());
        int daysToSubmit = config.getIntValue();

        GoalVersion lastTimedOutGoalVersion = appraisal.getLastTimedOutGoalVersion(Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED);
        DateTime submitDate;
        if (lastTimedOutGoalVersion  == null) {
            // if the reactivated goal version is null it means that it hasn't been saved to the db
            // and the request has just been submitted by the employee.
            submitDate = EvalsUtil.getToday();
        } else {
            submitDate = new DateTime(lastTimedOutGoalVersion.getCreateDate()).withTimeAtStartOfDay();
        }

        String requestSubmitDate = submitDate.toString(Constants.DATE_FORMAT);
        return MessageFormat.format(bodyString, requestSubmitDate, daysToSubmit);
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
                appraisal.getReviewPeriod(), getDueDate(appraisal, null), getDaysRemaining(appraisal, false));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
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
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getStatusExpirationDate(appraisal));
    }

    /**
     * Fetch the body for a goalsReactivationRequested emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivationRequestedBody(Appraisal appraisal) throws Exception {
        //# {0} = submit date, {1} = employee name, {2} = job title, {3} = review period, {4} = due date
        String bodyString = emailBundle.getString("email_goalsReactivationRequested_body");
        DateTime submitDate;
        GoalVersion reactivatedGoalVersion = appraisal.getReactivatedGoalVersion();

        if (reactivatedGoalVersion == null) {
            // if the reactivated goal version is null it means that it hasn't been saved to the db
            // and the request has just been submitted by the employee.
            submitDate = EvalsUtil.getToday();
        } else {
            submitDate = new DateTime(reactivatedGoalVersion.getCreateDate()).withTimeAtStartOfDay();
        }
        String requestSubmitDate = submitDate.toString(Constants.DATE_FORMAT);

        // Use today as the submit date since this email is sent right after the request is submitted
        return MessageFormat.format(bodyString, requestSubmitDate, getEmployeeName(appraisal),
                getJobTitle(appraisal), appraisal.getReviewPeriod(), getStatusExpirationDate(appraisal));
    }

    /**
     * Fetch the body for a goalsReactivationRequested emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivationDeniedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivationDenied_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
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
                appraisal.getReviewPeriod(), getDueDate(appraisal, null), getDaysRemaining(appraisal, false));
    }

    /**
     * Fetch the body for a resultsOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String resultsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_resultsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for appraisalDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String appraisalDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalDue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for appraisalOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String appraisalOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_appraisalOverdue_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
    }

    /**
     * Fetch the warning for appraisalOverdue emailType
     * @return
     * @throws Exception
     */
    public String getAppraisalOverdueITWarning() throws Exception {
        // extra message for classified IT as required by bargaining agreement
        return emailBundle.getString("email_appraisalOverdue_IT_body");
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
     * Fetch the body for employeeReviewDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String employeeReviewDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_employeeReviewDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDaysRemaining(appraisal, true));
    }

    /**
     * Fetch the body for employeeReviewExpired emailType
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String employeeReviewExpiredBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_employeeReviewExpired_body");
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal, "signatureDue"));
    }

    /**
     * Fetch the body for releaseDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String releaseDueBody(Appraisal appraisal) throws Exception {
        String key = "email_releaseDue_body";
        if (appraisal.getJob().isUnclassified()) {
            key = "email_releaseDueProfFaculty_body";
        }
        String bodyString = emailBundle.getString(key);
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for signatureDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String signatureDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_signatureDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),appraisal.getReviewPeriod(),
                getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for signatureDueNotRated emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String signatureDueNotRatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_signatureDueNotRated_body");
        return MessageFormat.format(bodyString, appraisal.getJob().getSupervisor().getEmployee().getConventionName(),
                getEmployeeName(appraisal), getJobTitle(appraisal));
    }


    /**
     * Fetch the body for signatureOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String signatureOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_signatureOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),appraisal.getReviewPeriod(),
                getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for rebuttalRead emailType
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalRead_body");
        return MessageFormat.format(bodyString, getCommentType(appraisal), getJobTitle(appraisal),
                appraisal.getReviewPeriod());
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
                osuid, getCommentType(appraisal), getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for rebuttalReadOverdue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalReadOverdue_body");
        String osuid = appraisal.getJob().getEmployee().getOsuid();
        return MessageFormat.format(bodyString, getEmployeeName(appraisal), osuid, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal, null));
    }

    /**
     * Fetch the body for completed emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String completedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_completed_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    /**
     * Fetch the body for completion reminder emailType
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String firstCompletionReminderBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_completionReminder_body");
        int daysAfterEndOfCycle = Math.abs(getDaysRemaining(appraisal, "end"));
        return MessageFormat.format(bodyString, daysAfterEndOfCycle, getEmployeeName(appraisal),
                getJobTitle(appraisal), appraisal.getReviewPeriod());
    }

    private String secondCompletionReminderBody(Appraisal appraisal) throws Exception {
        return firstCompletionReminderBody(appraisal);
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
     * Fetch the body for closed emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String classifiedITNoIncreaseBody(Appraisal appraisal) throws Exception {
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "IT-increase-withhold-warn2-days",
                appraisal.getAppointmentType());
        int daysToNotifyEmployee = config.getIntValue();

        Employee employee = appraisal.getJob().getEmployee();
        String employeeName = employee.getConventionName();
        String bodyString = emailBundle.getString("email_classifiedITNoIncrease_body");
        String sed = new DateTime(appraisal.getSalaryEligibilityDate()).toString("MM/dd");
        return MessageFormat.format(bodyString, employeeName, sed, daysToNotifyEmployee);
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
     * Fetch the due day. If there's a status specified in the parameter, we use it instead of the appraisal's
     * status.
     *
     * @param appraisal
     * @param status                Status to override the appraisal status when checking due date
     * @return
     * @throws Exception
     */
    private String getDueDate(Appraisal appraisal, String status) throws Exception {
        if (status == null) {
            status = appraisal.getStatus();
        }
        if(status.contains("Overdue")) {
            status = status.replace("Overdue", "Due");
        }
        Configuration config = ConfigurationMgr.getConfiguration(configMap, status, appraisal.getAppointmentType());
        DateTime dueDay = EvalsUtil.getDueDate(appraisal, config);
        return dueDay.toString(Constants.DATE_FORMAT);
    }

    /**
     * Returns a String that represents when the given status is expired for the goals reactivation
     * and employee review due workflow.
     *
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String getStatusExpirationDate(Appraisal appraisal) throws Exception {
        String status = appraisal.getStatus() + "Expiration";
        Configuration config = ConfigurationMgr.getConfiguration(configMap, status, appraisal.getAppointmentType());
        DateTime dueDay = EvalsUtil.getDueDate(appraisal, config);
        return dueDay.toString(Constants.DATE_FORMAT);
    }

    /**
     * Fetch the days remaining to respond to a particular action or before the action expires.
     *
     * @param appraisal
     * @param expirationStatus              Whether or not to use statusExpiration to calculate days remaining
     * @return
     * @throws Exception
     */
    private int getDaysRemaining(Appraisal appraisal, Boolean expirationStatus) throws Exception {
        String name = appraisal.getStatus();
        if (expirationStatus != null && expirationStatus) {
            name += "Expiration";
        }
        Configuration config = ConfigurationMgr.getConfiguration(configMap, name, appraisal.getAppointmentType());
        DateTime dueDay = EvalsUtil.getDueDate(appraisal, config);
        return Days.daysBetween(EvalsUtil.getToday(), dueDay).getDays();
    }

    /**
     * Fetch the days remaining to respond to a particular action or before the action expires. Only
     * supports the "end" date as reference.
     *
     * @param appraisal                     Appraisal object
     * @param reference                     Reference point in appraisal stage to use when calculating days
     * @return                              Number of days remaining until the reference point of this appraisal
     * @throws Exception
     */
    public static int getDaysRemaining(Appraisal appraisal, String reference) throws Exception {
        if (!reference.equals("end")) {
            return -1;
        }

        DateTime dueDay = new DateTime(appraisal.getEndDate());
        return Days.daysBetween(EvalsUtil.getToday(), dueDay).getDays();
    }

    /**
     * Get the correct address name used in the "Dear ..." line
     * @param appraisal
     * @param mailTo
     * @return
     * @throws Exception
     */
    private String getAddressee(Appraisal appraisal, String mailTo) throws Exception {
        if (mailTo.contains("employee")) {
            return appraisal.getJob().getEmployee().getConventionName();
        }
        if (mailTo.contains("upper supervisor")) {
            return appraisal.getJob().getSupervisor().getSupervisor().getEmployee().getConventionName();
        }
        if (mailTo.contains("supervisor")) {
            return appraisal.getJob().getSupervisor().getEmployee().getConventionName();
        }
        if (mailTo.contains("reviewer")) {
            return "Reviewer";
        }
        return "";
    }

    /**
     * Strips e-mail addresses from text and replaces with given replacement text
     *
     * @param originalText
     * @return
     */
    private String replaceEmails(String originalText, String replacementStr) {
        String EMAIL_PATTERN = "\\b[a-zA-Z0-9._%-+]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b";
        return originalText.replaceAll(EMAIL_PATTERN, replacementStr); // Replace emails
    }
}
