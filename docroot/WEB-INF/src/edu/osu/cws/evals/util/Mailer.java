package edu.osu.cws.evals.util;

/**
 * Mailer: send email to the appropriate user/users give an appraisal and emailType
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * @date: 6/24/11
 */

import java.lang.reflect.Method;
import javax.mail.*;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.ReviewerMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
import edu.osu.cws.evals.models.Email;
import org.apache.commons.mail.*;

import java.text.MessageFormat;

public class Mailer {
    private ResourceBundle emailBundle;
    private String hostName;
    private String from;
    private String linkURL;
    private String helpLinkURL;
    private Map<String, Configuration> configMap;
    private String replyTo;
    private EvalsLogger logger;

    Map<String, String> logFields = new HashMap<String, String>();

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
                  String helpLinkURL, Map<String, Configuration> map, EvalsLogger logger,
                  String replyTo) {
        this.emailBundle = resources;
        this.from = from;
        this.hostName = hostName;
        this.linkURL = linkURL;
        this.helpLinkURL = helpLinkURL;
        configMap = map;
        this.logger = logger;
        this.replyTo = replyTo;
    }

    /**
     * Sends an email
     * @param appraisal - an Appraisal object
     * @param emailType - an EmailType
     * @throws Exception
     */
    public void sendMail(Appraisal appraisal, EmailType emailType) {
        String logShortMessage = "";
        String logLongMessage = "";

        try {
            if (!(appraisal.getJob().getStatus().equals("A"))) {
                logShortMessage = "Email not sent";
                logLongMessage = "Appraisal " + appraisal.getId() +
                        " not available, job is not active.";
                logger.log(Logger.NOTICE,logShortMessage,logLongMessage,logFields);
                return;
            }

            HtmlEmail email = getHtmlEmail();
            String mailTo = emailType.getMailTo();
            String mailCC = emailType.getCc();
            String mailBCC = emailType.getBcc();
            boolean hasRecipients = false;

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

            if (!hasRecipients) {
                return;
            }

            String addressee = getAddressee(appraisal,mailTo);
            String body = getBody(appraisal, emailType, addressee);
            email.setHtmlMsg(body);

            String subject = emailBundle.getString( "email_" + emailType.getType() + "_subject");
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
        }
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
    public void sendSupervisorMail(Employee supervisor,String middleBody,
                                   List<Email> emailList) {

        try {
            String bcName = JobMgr.getBusinessCenter(supervisor.getId());
            if (bcName == null) //supervisor has no job, Error
            {
                String shortMsg = "From sendSupervisorMail: supervisor has no active job";
                String longMsg = "Supervisor PIDM: " + supervisor.getId() +
                        ", has no active job.";
                logger.log(Logger.ERROR, shortMsg, longMsg);
                return;
            }

            String bcKey = "businesscenter_" + bcName + "_descriptor";
            String bcDescritor = emailBundle.getString(bcKey);

            String supervisorName = supervisor.getConventionName();
            String emailAddress = supervisor.getEmail();

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
     * Fetch the body for a particular emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsDueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsDue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal),
                appraisal.getReviewPeriod(), getDueDate(appraisal), getDaysRemaining(appraisal));
    }

    /**
     * Fetch the body for a particular emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsOverdueBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsOverdue_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(), getDueDate(appraisal));
    }

    /**
     * Fetch the body for goalsRequiredModification emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsRequiredModificationBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsRequiredModification_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(), getDueDate(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal));
    }

    /**
     * Fetch the body for goalsApproved emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsApprovedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsApproved_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod(),
                getDueDate(appraisal));
    }

    /**
     * Fetch the body for a goalsReactivated emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String goalsReactivatedBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_goalsReactivated_body");
        return MessageFormat.format(bodyString, appraisal.getReviewPeriod(), getDueDate(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal), getDaysRemaining(appraisal));
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
                getDueDate(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal), getDaysRemaining(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal));
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
                appraisal.getReviewPeriod(),getDueDate(appraisal));
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
                appraisal.getReviewPeriod(),getDueDate(appraisal));
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
                getDueDate(appraisal));
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
                getDueDate((appraisal)));
    }

    /**
     * Fetch the body for rebuttalReadDue emailType
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String rebuttalReadBody(Appraisal appraisal) throws Exception {
        String bodyString = emailBundle.getString("email_rebuttalRead_body");
        return MessageFormat.format(bodyString, getJobTitle(appraisal), appraisal.getReviewPeriod());
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
                osuid, getJobTitle(appraisal), appraisal.getReviewPeriod(), getDueDate(appraisal));
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
                appraisal.getReviewPeriod(), getDueDate(appraisal));
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
     * Fetch the due day
     * @param appraisal
     * @return
     * @throws Exception
     */
    private String getDueDate(Appraisal appraisal) throws Exception {
        String status = appraisal.getStatus();
        if(status.contains("Overdue")) {
            status = status.replace("Overdue", "Due");
        }
        Configuration config = configMap.get(status);
        DateTime dueDay = EvalsUtil.getDueDate(appraisal, config);
        return dueDay.toString(Constants.DATE_FORMAT);
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
        DateTime dueDay = EvalsUtil.getDueDate(appraisal, config);
        return Days.daysBetween(new DateTime(), dueDay).getDays();
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
