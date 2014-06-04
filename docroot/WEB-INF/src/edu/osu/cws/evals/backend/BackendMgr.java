package edu.osu.cws.evals.backend;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.inject.Inject;
import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.*;
import edu.osu.cws.util.Logger;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.*;
import java.util.*;

public class BackendMgr {
    private int totalErrorCount = 0;  //how many errors we have so far?
    private int createCount = 0;
    private int updateCount = 0;
    private int followupEmailCount = 0;
    List<String> bcEmailsSent = new ArrayList<String>(); // BCs that we sent emails to
    List<String> reportEmailsSent = new ArrayList<String>(); // BCs that we sent late report emails to
    private int openCount = 0; // # of open appraisals
    private int archivedCount = 0;

    private StringBuffer errorMsg = new StringBuffer();  //to hold all the errors
    private MailerInterface mailer;
    private Map<String, Configuration> configMap;
    private Map<String, EmailType> emailTypeMap;
    private LoggingInterface logger;

    private Date startTime = null;

    // We are not sending individual emails to supervisor.
    //We are only sending one email to each supervisor at each run.
    //So we are saving them all till the end and send them all.
    //assumes supervisor is the only receiver of the email. True to this release. May change later.
    Map<Integer, StringBuffer> supervisorEmailMessages = new HashMap<Integer, StringBuffer>();

    // List of supervisor pidms that had at least 1 appraisal overdue classified IT action.
    // It's used so that we know what supervisor emails to append IT no increase warning
    List<Integer> supervisorITNoIncreaseWarn = new ArrayList<Integer>();

    // Keep track of Email objects for each supervisor pidm
    Map<Integer, List<Email>> supervisorEmails = new HashMap<Integer, List<Email>>();

    private List<String> dataErrors = new ArrayList<String>();

    private Session session = null;
    private Transaction tx;

    /**
     * Constructor method that is automatically called by google guice.
     *
     * @param logger
     * @param mailer
     * @param configMap
     * @param emailTypeMap
     */
    @Inject
    BackendMgr(LoggingInterface logger, MailerInterface mailer, Map<String, Configuration> configMap,
               Map<String, EmailType> emailTypeMap) {
        this.logger = logger;
        this.mailer = mailer;
        this.configMap = configMap;
        this.emailTypeMap = emailTypeMap;
    }

    /**
     * /**This is the high level method that puts everything together.
     * 1. initiate all the instance variables
     * 2. update appraisal status
     * 3. create new appraisal records
     * 4. send out emails to supervisors
     * 5. send out email to business center reviewers
     * @throws Exception
     */
    public void process() throws Exception
    {
       startTime = new Date();
       try
       {
          updateAppraisals();
          createAppraisals();
          createProfessionalFacultyAnnual();
          archiveAppraisals();
          emailSupervisors();
          emailReviewers();
          sendLateEvaluationsReport();
       } catch(Exception e)
       {
         if (totalErrorCount >= Constants.MAX_ERROR_COUNT)
         {
            logger.log(Logger.CRITICAL, "Cron aborted", "Max number of errors reached");
            System.out.println("Max number of errors reached.  Cron aborting.");
         }
         else
         {
             //error from setup
             System.out.println("Cron aborting due to issues in setup.");
             logger.log(Logger.CRITICAL, "backend aborted", e);

         }
       }
       finally
       {
         if (session != null && session.isOpen())
             session.close();
         printSummary();
       }
    }

    private void printSummary()
    {
        // # of supervisor emails sent
        int supervisorEmailsSent = supervisorEmailMessages.size();

        Date exitTime = new Date();
        int timeElapsed = (int) (exitTime.getTime() - startTime.getTime() ) / (60*1000);

        System.out.println("\nData Errors:\n");
        System.out.println(getDataErrors());
        System.out.println("=====================================================");

        if (errorMsg.length() > 0)
        {
            System.out.println("\nSummary of errors\n" + errorMsg.toString());
        }
        System.out.println("=====================================================\n");


        System.out.println("Number of appraisals in db: ................" + openCount);
        System.out.println("Number of appraisals created: .............." + createCount);
        System.out.println("Number of appraisals updated: .............." + updateCount);
        System.out.println("Number of appraisals archived: ............." + archivedCount);
        System.out.println("Number of followup emails sent: ............" + followupEmailCount);
        System.out.println("Number of supervisor emails sent: .........." + supervisorEmailsSent);
        System.out.println("Email sent to BCs: ........................." + bcEmailsSent);
        System.out.println("Late Report email sent to BCs: ............." + reportEmailsSent);
        System.out.println("Number of errors occurred:..................." + totalErrorCount);
        System.out.println("Job starting at ............................" + startTime);
        System.out.println("Job exiting at ............................." + exitTime);
        System.out.println("Total process time in minutes:.............." + timeElapsed);
    }

    /** Creates new trial and annual appraisals that are due for creation.
     *
     * @throws Exception
     */
    private void createAppraisals() throws Exception
    {
        Job job = null;

        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();

        // specify list of appointment types
        ArrayList<String> appointmentTypes = new ArrayList<String>();
        appointmentTypes.add(AppointmentType.CLASSIFIED);
        appointmentTypes.add(AppointmentType.CLASSIFIED_IT);

        //get a list of all the active classified jobs
        List<Job> shortJobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        tx.commit();

        for (Job shortJob : shortJobs)
        {
            try
            {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                System.out.print("Working with job...");
                System.out.println(shortJob.getSignature());
                job = JobMgr.getJob(shortJob.getEmployee().getId(),
                                    shortJob.getPositionNumber(), shortJob.getSuffix());
                System.out.print("Got job.. ");
                System.out.println(job.getSignature());

                //is it in trial period?
                if (job.withinTrialPeriod())
                {
                    System.out.println("Job in trial period.");
                    handleTrialCreation(job);
                    tx.commit();
                    continue;   //done for this job as it is still in trial period.
                }

                //If we get here, the job is not in trial period.
                // Do we need to create an annual appraisal today?
                handleAnnualCreation(job);
                tx.commit();
            }catch(Exception e)
            {
               if (session != null && session.isOpen())
                   session.close();
               String msg = "Failed to create new Appraisal for " + shortJob.getSignature();
               logDataError(msg);
               System.out.println(msg);
               if (errorMsg.length() > 0)
                   errorMsg.append("\n");
               errorMsg.append(msg);
               log_error(msg, e);
            }
        }//for loop
     }

    /** Creates new annual appraisals for professional faculty that are due for creation.
     *
     * @throws Exception
     */
    private void createProfessionalFacultyAnnual() throws Exception {
        Job job = null;
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();

        // specify list of appointment types
        ArrayList<String> appointmentTypes = new ArrayList<String>();
        appointmentTypes.add(AppointmentType.PROFESSIONAL_FACULTY);

        //get a list of all the active classified jobs
        List<Job> shortJobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        DateTime createForDate = getCreateForDate(AppointmentType.PROFESSIONAL_FACULTY);
        tx.commit();

        for (Job shortJob : shortJobs) {
            try {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                System.out.print("Working with job...");
                System.out.println(shortJob.getSignature());

                Appraisal lastShortAppraisal = AppraisalMgr.getLastAppraisalByJob(shortJob);
                if (lastShortAppraisal != null) {
                    System.out.println("Last appraisal - id: " + lastShortAppraisal.getId() + ", startDate: "
                            + lastShortAppraisal.getStartDate());
                    DateTime lastStartDate = new DateTime(lastShortAppraisal.getStartDate());
                    DateTime appraisalStartDate = lastStartDate.plusYears(1);

                    // check if we need to create new annual evaluation
                    if (createForDate.isAfter(appraisalStartDate) ) {
                        // Get the full job to check if appraisal exists and/or to create a new one.
                        job = JobMgr.getJob(shortJob.getEmployee().getId(),
                                shortJob.getPositionNumber(), shortJob.getSuffix());
                        if (!AppraisalMgr.appraisalExists(job, appraisalStartDate, Appraisal.TYPE_ANNUAL)) {
                            String msg = "creating " + Appraisal.TYPE_ANNUAL + " appraisal for " + shortJob.getSignature();
                            System.out.println(msg);
                            logger.log(Logger.INFORMATIONAL, msg, "");
                            createAppraisal(job, appraisalStartDate, Appraisal.TYPE_ANNUAL);
                        }
                    } else {
                        System.out.println("not time to create new annual evaluation. appraisalStartDate = " + appraisalStartDate);
                    }
                } else {
                    System.out.println("Professional Faculty job hasn't been initialized in EvalS yet.");
                }
                tx.commit();
            } catch(Exception e) {
                if (session != null && session.isOpen()) {
                    session.close();
                }
                String msg = "Failed to create new Appraisal for " + shortJob.getSignature();
                logDataError(msg);
                System.out.println(msg);
                if (errorMsg.length() > 0) {
                    errorMsg.append("\n");
                }
                errorMsg.append(msg);
                log_error(msg, e);
            }
        }//for loop
     }

    public void archiveAppraisals() throws Exception {
        Configuration daysBeforeArchive = ConfigurationMgr.getConfiguration(configMap, "daysBeforeArchive", "");
        int[] idsToArchive = AppraisalMgr.getIdsToArchive(daysBeforeArchive.getIntValue());
        if(idsToArchive != null && idsToArchive.length > 0) {
            archivedCount = AppraisalMgr.archive(idsToArchive);
        }

        // Log archived appraisal ids
        String msg = "[" + DateTime.now().toString() + "] " +
                archivedCount + " appraisals archived";
        if(idsToArchive != null && idsToArchive.length > 0) {
            msg += " with id in (" +
                StringUtils.join(ArrayUtils.toObject(idsToArchive), ", ") + ")";
        }
        System.out.println(msg);
        logger.log(Logger.INFORMATIONAL, msg, "");
    }


    public boolean handleTrialCreation(Job job) throws Exception
    {
       //is there a trial record created for the job?
       if (!AppraisalMgr.trialAppraisalExists(job)) //create only if it is not already created.
       {
           DateTime startDate = job.getTrialStartDate();
           if (startDate == null || AppraisalMgr.trialAppraisalExists(job)) {
               return false;
           }

           String msg = "creating trial appraisal for " + job.getSignature();
           System.out.println(msg);
           createAppraisal(job, startDate, Appraisal.TYPE_TRIAL);
           System.out.println("Created trial aprpaisal for " + job.getSignature());
           return true;
       }
        return false;
    }

    public boolean handleAnnualCreation(Job job) throws Exception
    {
        // don't create annual evaluation if the job is in trial period
        if (job.withinTrialPeriod()) {
            return false;
        }

        //Need to create appraisal records for appraisals whose periods start on or before this date
        // if they haven't been created.
        DateTime createForDate = getCreateForDate(job.getAppointmentType());
        String msg;

        if (job.getAnnualInd() == 0) //No annual appraisal for this job.
               return false;

        //If we get here, we are doing annual appraisal for this job.
        String type = Appraisal.TYPE_ANNUAL;

        DateTime appraisalStartDate = getAppraisalStartDate(job, createForDate);
        System.out.println("appraisalStartDate = " + appraisalStartDate);

        if (appraisalStartDate == null) //no need to create appraisal at this time.
             return false;

        //Is this the first annual appraisal?
        if (appraisalStartDate.equals(job.getInitialEvalStartDate()))
        {
            //don't create it if the job's trial appraisal is still open.
            // The first annual will be created by the web app when the trial one is closed or completed.
            if (AppraisalMgr.openTrialAppraisalExists(job))
            {
                System.out.println("Open trial record exists, not creating annual.");
                return false;
            }
            //if we get here, this is the initial annual, and there is no open trial appraisal.
            //An annual record id needed for this job
            type = Appraisal.TYPE_INITIAL;
        }

        // If we get here, we need to create an appraisal record for this job if it does not exist.
        // Check to see if it does. If not, create one. The appraisalExists method checks to see
        // if there's an evaluation created for a start date within 6 months of the give one.
        if (!AppraisalMgr.appraisalExists(job, appraisalStartDate, Appraisal.TYPE_ANNUAL)) {
            msg = "creating " + type + " appraisal for " + job.getSignature();
            System.out.println(msg);
            logger.log(Logger.INFORMATIONAL, msg, "");
            createAppraisal(job, appraisalStartDate, type);
            return true;
        }

        return false;
    }

    /** createForDate: This is an awkward name, can't think of a better one for now.
     * Significance of createForDate:  If an appraisal has a period start date on or
     * before this date, then there should be an appraisal record for it.
     * If there isn't one, we will need to create on.
     *
     * @param appointmentType
     * @return: createForDate   DateTime object
     */
    private DateTime getCreateForDate(String appointmentType)
    {
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "firstGoalDueReminder", appointmentType);
        int intVal = config.getIntValue(); //# of days before appraisal start date to create appraisal

        //Need to create appraisal records start on or before this calendar date
        // if they haven't been created.
        DateTime createForDate = EvalsUtil.getToday().plusDays(intVal);

        System.out.println("create for date = " + createForDate.toString());
        return createForDate;
    }

    /**
     * This is the start date of the appraisal record we are about to create.
     * If we are more than 2 months away from the appraisal due date, return null as we are not creating one.
     * If we are within 2 months from the appraisal date, then return the start date of the appraisal period.
     * @param job
     * @param createForDate: (DateTime) If the appraisalStartDate is after this date, then do not create the appraisal record.
     * @return: start date (DateTime) of the appraisal record we are to create, or null if we don't need to create one now.
     * @throws Exception
     */
    private DateTime getAppraisalStartDate(Job job, DateTime createForDate) throws Exception
    {
        DateTime startDate = job.getNewAnnualStartDate(); //appraisal startDate for current calendar year.
        System.out.println("at beginning, startCat = " + startDate);

        //If createForDate is next year, then we need to get the appraisal start date for next year.
        DateTime lastDateOfYear = EvalsUtil.getToday().withMonthOfYear(12).withDayOfMonth(31);

        if (createForDate.isAfter(lastDateOfYear))
          {
             //Also need to distinguish between initial annual and other annual,
             // as duration of initial can be different.
             if (startDate.equals(job.getInitialEvalStartDate()))  //initial appraisal
                startDate = startDate.plusMonths(job.getAnnualInd());
             else
               //Length of other appraisals is always 12 months.
                 startDate = startDate.plusMonths(12);
          }

          System.out.println("at 2nd, startCat = " + startDate);

          if (startDate.isAfter(createForDate)) //appraisal period start date is later than createForDate.
          {
               // No need to create one for this year yet,
               // but do we need to create one for last year?
            if (startDate.equals(job.getInitialEvalStartDate()))  //initial appraisal
                 return null;
            else
               //Length of other appraisals is always 12 months.
                startDate = startDate.minusMonths(12);
          }

        return startDate;
    }

    /**
     * Create a new appraisal and send email.
     * @param job
     * @param startDate     DateTime object
     * @param type
     * @return
     * @throws Exception
     */
    private Appraisal createAppraisal(Job job, DateTime startDate, String type) throws Exception {
        Appraisal appraisal = null;

        if (type.equals(Appraisal.TYPE_INITIAL)) {
            Appraisal trialAppraisal = AppraisalMgr.getTrialAppraisal(job);
            if (trialAppraisal != null) {
                appraisal = AppraisalMgr.createFirstAnnualAppraisal(trialAppraisal);
            }
        }

        if (appraisal == null) {
            appraisal = AppraisalMgr.createAppraisal(job, startDate, type);
        }

        if (appraisal != null)
        {
            // create salary object if needed
            if (appraisal.getIsSalaryUsed()) {
                AppraisalMgr.createOrUpdateSalary(appraisal, configMap);
            }

            logger.log(Logger.INFORMATIONAL,
                    "created " + type + " appraisal for " + job.getSignature(), "");

            createCount++;
            sendMail(appraisal);
        }
        return appraisal;
    }

    private boolean shouldUpdateSalaryInfo(Appraisal appraisal) throws Exception{
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "appraisalDue",
                appraisal.getAppointmentType());
        DateTime dueDate = EvalsUtil.getDueDate(appraisal, config);
        //If the the current date is before or on the appraisal due date, returns true.
        return EvalsUtil.getToday().compareTo(dueDate) <= 0;
    }

    /**
     * This methods updates status of appraisals, and sends out notification emails.
     * Status of appraisals change with time.  For example, the status of goalsDue
     * changes to goalsOverdue, goalsApproved to resutlsDue.
     * @throws Exception: This methods handles it's own Exceptions by logging them.
     *                    It throws an Exception when totalErrorCount is greater than
     *                    MAZ_ERROR_COUNt.  The thrown exception is caught by the
     *                    process method, which logs it and abort.
     */

    private void updateAppraisals() throws Exception
    {
        //get a lists of all IDs of the appraisals that are still open.
        //Not completed, closed, or archived.

        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        int[] ids = AppraisalMgr.getOpenIDs();
        openCount = ids.length;
        tx.commit();
        System.out.println("Number of appraisal records: " + ids.length);
        Appraisal appraisal = null;
        String newStatus;
        DateTime createForDate = getCreateForDate(AppointmentType.PROFESSIONAL_FACULTY);

        for (int i = 0; i < ids.length; i++)
        {
            try
            {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                System.out.println("Processing appraisal " + ids[i]);
                appraisal = AppraisalMgr.getAppraisal(ids[i]);

                // if the status contains "Overdue", calculate appraisal.overdue and save it
                if (appraisal.getStatus().contains(Appraisal.OVERDUE)) {
                    appraisal.updateOverdue(configMap);
                    AppraisalMgr.saveOverdue(appraisal);
                }

                // update salary for classified IT evals
                if (appraisal.getIsSalaryUsed()) {
                    boolean salaryNeedsUpdate = shouldUpdateSalaryInfo(appraisal);
                    if (salaryNeedsUpdate) {
                        AppraisalMgr.createOrUpdateSalary(appraisal, configMap);
                    }

                    if (shouldSendITWithHoldWarningEmail(appraisal)) {
                        sendNoIncreaseReminder(appraisal);
                    }
                }

                //Do we need to change status?
                String status = appraisal.getStatus();
                newStatus = appraisal.getNewStatus(configMap);

                // Update appraisal status
                if (newStatus != null) {
                   System.out.println("Need to update status for " + appraisal.getId() +
                           " from " + appraisal.getStatus() + " to " + newStatus);
                    updateAppraisal(appraisal, newStatus);
                } else {
                    // check if we need to send follow up email
                    checkFrequencyAndSendMail(appraisal, createForDate, status);
                }

                tx.commit();
            }catch(Exception e)
            {
                if (session != null && session.isOpen())
                    session.close();
                String msg = "Failed to update appraisal " + ids[i];

                // Log data error and try to provide employee pidm
                String message = "Data error with appraisal " + ids[i];
                if (appraisal != null && appraisal.getJob() != null &&
                        appraisal.getJob().getEmployee() != null) {
                    message += " employee " + appraisal.getJob().getEmployee().getId();
                }
                logDataError(message);

                System.out.println(msg);
                errorMsg.append("\n" + msg);
                log_error(msg, e);
            }
        }
    }

    /**
     * Checks if the email needs be sent via the frequency configuration and then calls the method to
     * send it if it is needed.
     *
     * @param appraisal
     * @param createForDate
     * @param status
     * @throws Exception
     */
    private void checkFrequencyAndSendMail(Appraisal appraisal, DateTime createForDate, String status)
            throws Exception {
        // Do we need to send additional reminder email?
        Configuration frequencyConfig = getFrequencyConfig(appraisal);
        if (!isEmailFrequencyEnabled(frequencyConfig)) {   //May need to send followup email
            return;
        }

        Email lastEmail = EmailMgr.getLastEmail(appraisal.getId(), status);

        boolean notTimeToSendReminder = lastEmail != null &&
                !EvalsUtil.anotherEmail(lastEmail, frequencyConfig); //not time yet
        if (notTimeToSendReminder || timeToSendFirstStatusEmail(appraisal, createForDate)) {
            return;
        }

        System.out.println("need to send another email for status of " + status
                + " for appraisal " + appraisal.getId());
        sendMail(appraisal);
        System.out.println("Sent email for " + status);
        followupEmailCount++;

        // commit to db so that future email checks detect email has already been sent.
        tx.commit();
        tx = session.beginTransaction();

    }

    /**
     * Returns the frequency configuration for the given appraisal's status + appointment type.
     *
     * @param appraisal
     * @return
     */
    private Configuration getFrequencyConfig(Appraisal appraisal) {
        String key = appraisal.getStatus() + "Frequency";
        return ConfigurationMgr.getConfiguration(configMap, key,
                appraisal.getAppointmentType());
    }

    /**
     * Whether or not the email frequency has been disabled. If the frequency configuration value is set to "-1", it is
     * considered to be disabled. No follow up emails are sent.
     *
     * @param frequencyConfig
     * @return
     */
    private boolean isEmailFrequencyEnabled(Configuration frequencyConfig) {
        return frequencyConfig != null && !frequencyConfig.getValue().equals("-1");
    }

    private void log_error(String title, Exception e)throws Exception
    {
        totalErrorCount++;
        logger.log(Logger.ERROR, title, e);
        if (totalErrorCount >= Constants.MAX_ERROR_COUNT)
        {
            throw e;    //handled by the process method.
        }
    }

    private void logDataError(String message) {
        dataErrors.add(message);
    }

    public String getDataErrors() {
        return "* " + StringUtils.join(dataErrors, "\n* ");
    }

    /**
     *
     * @param appraisal             Appraisal object to update
     * @param newStatus             New status for the given appraisal
     * @throws Exception
     */
   public void updateAppraisal(Appraisal appraisal, String newStatus) throws Exception
    {
        // only update the status if we have a non-null value to use
        if (newStatus == null) {
            return;
        }

        String status = appraisal.getStatus();
        boolean newGoalsTimedOut = newStatus.equals(Appraisal.STATUS_GOALS_APPROVED);

        String shortMsg = "updating appraisal " + appraisal.getId();
        String longMsg = "Set status to " + newStatus + " from " + status + ".";
        logger.log(Logger.INFORMATIONAL, shortMsg , longMsg);
        appraisal.setOriginalStatus(status);
        appraisal.setStatus(newStatus);
        AppraisalMgr.updateAppraisalStatus(appraisal);

        if (newGoalsTimedOut) {
            timeOutGoalsReactivation(appraisal);
        } else {
            Configuration frequencyConfig = getFrequencyConfig(appraisal);
            if (isEmailFrequencyEnabled(frequencyConfig)) {
                sendMail(appraisal);
            }
        }
        updateCount++;
    }


    /**
     * Sends to employee, and adds the emails to supervisor to the supervisorEmails to send later.
     * We are not sending individual emails to supervisor. We are only sending one email to each
     * supervisor at each run. So we are saving them all till the end and send them all. Assumes
     * supervisor is the only receiver of the email. True to this release. May change later.
     *
     * @param appraisal: the appraisal record we are dealing with
     * @throws Exception
     */
    public boolean sendMail(Appraisal appraisal) throws Exception {
        if (appraisal == null) {
            return false;
        }

        String status = appraisal.getStatus();
        EmailType emailType = emailTypeMap.get(status);
        // Don't send email is the job is not active or if email is not needed abort
        if (!appraisal.isEmployeeJobActive() || emailType == null) {
            return false;
        }

        boolean goalReactivationReminder = status.equals(Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED) ||
                        status.equals(Appraisal.STATUS_GOALS_REACTIVATED);
        Job employeeJob = appraisal.getJob();
        if (emailType.getMailTo().contains("employee") || goalReactivationReminder) {
            if (!mailer.sendMail(appraisal, emailType)) {
                int employeeId = employeeJob.getEmployee().getId();
                int supervisorId = employeeJob.getSupervisor().getEmployee().getId();
                logDataError("Employee " + employeeId + " or supervisor " +  supervisorId +
                        " does not have a valid email address.");
                return false;
            }
            return true;
        } else if (emailType.getMailTo().contains("supervisor")) {
            if (employeeJob.getSupervisor() == null) { //no supervisor
                logDataError("Job " + employeeJob.getSignature() + " has no supervisor.");
                return false;
            }

            // get supervisor email string buffer, append to it.
            Employee supervisor = employeeJob.getSupervisor().getEmployee();
            StringBuffer sb = getSupervisorSb(supervisor);
            sb.append("<p>").append(mailer.getStatusMsg(appraisal, emailType));

            // get supervisor email list and add email to it.
            List<Email> emailList = getSupervisorEmailList(supervisor);
            emailList.add(new Email(appraisal.getId(), status));

            // check whether or not the supervisor needs to get IT warning from appraisal overdue
            if (status.equals(Appraisal.STATUS_APPRAISAL_OVERDUE) &&
                    appraisal.getJob().getAppointmentType().equals(AppointmentType.CLASSIFIED_IT) &&
                    !supervisorITNoIncreaseWarn.contains(supervisor.getId())) {
                supervisorITNoIncreaseWarn.add(supervisor.getId());
            }
            return true;
        }
        return false;
    }

    /**
     * Get supervisor email string buffer. If it hasn't been created, it initializes it.
     *
     * @param supervisor        Employee object
     * @return
     */
    public StringBuffer getSupervisorSb(Employee supervisor) {
        if (supervisor == null) {
            return null;
        }

        int key = supervisor.getId();
        if (!supervisorEmailMessages.containsKey(key)) {
            supervisorEmailMessages.put(key, new StringBuffer());
        }
        return supervisorEmailMessages.get(key);
    }

    /**
     * Gets a supervisor's email list. If it hasn't been created, it initializes it.
     *
     * @param supervisor        Employee object
     * @return
     */
    public List<Email> getSupervisorEmailList(Employee supervisor) {
        int key = supervisor.getId();
        if (!supervisorEmails.containsKey(key)) {
            supervisorEmails.put(key, new ArrayList<Email>());
        }
        return supervisorEmails.get(key);
    }

    /**
     *
     * @throws Exception
     */
    public void emailSupervisors() throws Exception
    {
        Set<Integer> keySet = supervisorEmailMessages.keySet();
        System.out.println("keyset size = " + keySet.size());

        for (Integer id: keySet)
        {
            try
            {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                Employee supervisor = EmployeeMgr.findById(id, null);

                StringBuffer messageBuffer = supervisorEmailMessages.get(id);
                String message = messageBuffer.toString();
                List<Email> emailList = supervisorEmails.get(id);

                // If the supervisor had at least 1 Classified IT appraisal overdue, include warning
                if (supervisorITNoIncreaseWarn.contains(id)) {
                   message += mailer.getAppraisalOverdueITWarning();
                }
                mailer.sendSupervisorMail(supervisor, message, emailList);
                tx.commit();
            } catch(Exception e)
            {
                if (session != null && session.isOpen())
                    session.close();
                String msg = "Error sending supervisor email to " + id;
                logDataError(msg);
                errorMsg.append("\n" + msg);
                log_error(msg, e);
            }
        }
    }

    /**
     *
     * @throws Exception
     */
    public void emailReviewers()  throws Exception
    {
       session = HibernateUtil.getCurrentSession();
       tx = session.beginTransaction();
       List<BusinessCenter> bcList = BusinessCenterMgr.list();
       tx.commit();

      for (BusinessCenter bc: bcList)
       {
           try
           {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                String bcName = bc.getName();
                int dueCount = AppraisalMgr.getReviewDueCount(bcName);
                int overdueCount = AppraisalMgr.getReviewOvedDueCount(bcName);

                if (dueCount == 0 && overdueCount == 0) { //nothing to review for this bc.
                    tx.commit();
                    continue;
                }

                String[] emailAddresses = getReviewersEmails(bcName);
                if (emailAddresses == null) { // no email addresses for this bc.
                    tx.commit();
                    continue;
                }

                System.out.println("Numbers: " + dueCount + ", " + overdueCount);
                mailer.sendReviewerMail(emailAddresses, dueCount, overdueCount);
                tx.commit();
                System.out.println("Done with " + bcName);

               // log the bc names that get emails sent.
               if (emailAddresses.length != 0 && emailAddresses[0] != null) {
                    bcEmailsSent.add(bcName);
               }
           }catch(Exception e)
           {
               if (session != null & session.isOpen())
                   session.close();
               String msg = "Error sending reviewer email to " + bc;
               logDataError(msg);
               errorMsg.append("\n" + msg);
               log_error(msg, e);
           }
       }
    }

    /**
     * Returns an array with emails for the reviewers of a BC.
     *
     * @param bcName
     * @return
     * @throws Exception
     */
    private String[] getReviewersEmails(String bcName) throws Exception {
        ArrayList<String> emailAddresses = new ArrayList<String>();
        List<Reviewer> reviewers = ReviewerMgr.getReviewers(bcName);

        if (reviewers.size() == 0) {
            logDataError("No reviewers in BC: " + bcName);
            return null;
        } else {
            for (Reviewer reviewer : reviewers) {
                emailAddresses.add(reviewer.getEmployee().getEmail());
            }
        }

        System.out.println("There are " + emailAddresses.size() + " reviewers.");
        System.out.println("first email address = " + emailAddresses.get(0) + ". BC name = " + bcName);
        return emailAddresses.toArray(new String[emailAddresses.size()]);
    }

    /**
     * Returns an array with emails for all the admin users.
     *
     * @return
     * @throws Exception
     */
    private String[] getAdminEmails() throws Exception {
        ArrayList<String> emailAddresses = new ArrayList<String>();
        List<Admin> admins = AdminMgr.list();
        if (admins.size() == 0) {
            logDataError("No admin users");
            return null;
        } else {
            for (Admin admin : admins) {
                emailAddresses.add(admin.getEmployee().getEmail());
            }
        }

        System.out.println("There are " + emailAddresses.size() + " admins.");
        System.out.println("first email address = " + emailAddresses.get(0));
        return emailAddresses.toArray(new String[emailAddresses.size()]);
    }

    /**
     * This method sends Classified IT no increase reminder.
     *
     * @param appraisal
     */
    private void sendNoIncreaseReminder(Appraisal appraisal) {
        // Send IT email warning supervisors extra process to withhold increase if desired.
        EmailType emailType = emailTypeMap.get("classifiedITNoIncrease");
        // Don't send email is the job is not active or if email is not needed abort
        if (!appraisal.isEmployeeJobActive() || emailType == null) {
            return;
        }

        mailer.sendMail(appraisal, emailType);
    }

    /**
     * Whether we should be sending the warning email about withholding increase for classified IT
     * evaluations.
     *
     * @param appraisal
     * @return
     */
    private boolean shouldSendITWithHoldWarningEmail(Appraisal appraisal) throws Exception {
        //Do not send email if the rating is set and the value is 1 or 2
        if(appraisal.getRating() != null &&
                (appraisal.getRating() == 1 || appraisal.getRating() == 2)){
            return false;
        }

        DateTime endDate = new DateTime(appraisal.getEndDate()).withTimeAtStartOfDay();
        int daysUntilEndDate = Days.daysBetween(EvalsUtil.getToday(), endDate).getDays();

        String appointmentType = appraisal.getJob().getAppointmentType();
        Configuration config = ConfigurationMgr.getConfiguration(configMap, "IT-increase-withhold-warn1-days",
                appointmentType);
        int daysBeforeWarning1 = config.getIntValue();
        config = ConfigurationMgr.getConfiguration(configMap, "IT-increase-withhold-warn2-days", appointmentType);
        int daysBeforeWarning2 = config.getIntValue();

        int warningEmailCount = EmailMgr.getEmailCount(appraisal.getId(), "classifiedITNoIncrease");

        return (daysUntilEndDate <= daysBeforeWarning2 && warningEmailCount <= 1) ||
                (daysUntilEndDate <= daysBeforeWarning1 && warningEmailCount == 0);
    }

    /**
     * Sends the user an email notification of timeout and sets the timeout fields in the goal
     * version.
     *
     * @param appraisal
     */
    public void timeOutGoalsReactivation(Appraisal appraisal) {
        // get the correct goal version to timeout.
        GoalVersion goalVersion = appraisal.getReactivatedGoalVersion();
        if (goalVersion == null) {
            return; // shouldn't have gotten here.
        }

        goalVersion.setRequestDecision(false);
        // leave decision_pidm as null to know it's backend doing timeout.
        goalVersion.setTimedOutAt(appraisal.getOriginalStatus());

        // notify employee of time out
        EmailType emailType = emailTypeMap.get(appraisal.getOriginalStatus() + "Timeout");
        if (emailType != null) {
            mailer.sendMail(appraisal, emailType);
        }
     }

    public int getDataErrorCount() {
        return dataErrors.size();
    }

    public LoggingInterface getLogger() {
        return logger;
    }

    public MailerInterface getMailer() {
        return mailer;
    }

    public Map<Integer, StringBuffer> getSupervisorEmailMessages() {
        return supervisorEmailMessages;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    /**
     * Checks whether or not it's time to send the email reminder. It's used to prevent the
     * first goals due email for the first professional faculty evaluation from being sent before
     * the review period begins. Currently, the supervisor could create the first evaluations 12+ months
     * before the evaluation begins.
     *
     * @param appraisal
     * @param createForDate
     * @return
     */
    public static boolean timeToSendFirstStatusEmail(Appraisal appraisal, DateTime createForDate) {
        // If the appointment type is other than prof. faculty always send email right away
        if (!appraisal.getAppointmentType().equals(AppointmentType.PROFESSIONAL_FACULTY)) {
            return false;
        }

        String status = appraisal.getStatus();
        if (!status.equals(Appraisal.STATUS_GOALS_DUE) && !status.equals(Appraisal.STATUS_GOALS_OVERDUE)) {
            return false;
        }

        // if the create for date is after the review cycle, don't wait to send emails
        if (createForDate.isAfter(new DateTime(appraisal.getStartDate()))) {
            return false;
        }

        // at this point, only the prof. faculty 1st evaluation that whose review cycle hasn't
        // started is the one we block the emails from being sent.
        return true;
    }

    /**
     * Sends csv reports of late evaluations that haven't been completed to the various BC and admin users.
     *
     * @throws Exception
     */
    private void sendLateEvaluationsReport() throws Exception {
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();

        // If there's no need to send any reports, exit the method
        List<String> bcNames = bcNamesThatNeedLateReports();
        if (bcNames.isEmpty()) {
            return;
        }

        // get the late evaluation information only for the BCs that need the late report
        List<Object[]> lateEvaluations = ReportMgr.getLateEvaluations(bcNames);
        writeLateEvalsCSVFiles(lateEvaluations);
        tx.commit();

        for (String bcName : bcNames) {
            try {
                session = HibernateUtil.getCurrentSession();
                tx = session.beginTransaction();
                String filePath = getLateReportFilePath(bcName);

                // check that the report was written successfully before continuing with the BC
                if (!new File(filePath).exists()) {
                    continue;
                }

                String[] emailAddresses;
                if (bcName.equals("admins")) {
                    emailAddresses = getAdminEmails();
                } else {
                    emailAddresses = getReviewersEmails(bcName);
                }

                if (emailAddresses == null) { // no email addresses for the report
                    tx.commit();
                    continue;
                }

                mailer.sendLateReport(emailAddresses, filePath, bcName);
                tx.commit();
                System.out.println("Done with report: " + bcName);

                // log the bc names that get emails sent.
                if (emailAddresses.length != 0 && emailAddresses[0] != null) {
                    reportEmailsSent.add(bcName);
                }

                // delete csv report
                File report = new File(filePath);
                if (!report.delete()) {
                    logDataError("\n" + "Failed to delete late evaluations report: " + filePath);
                }
            } catch(Exception e) {
                if (session != null & session.isOpen()) {
                    session.close();
                }

                String msg = "Error sending late report email to " + bcName;
                logDataError(msg);
                errorMsg.append("\n" + msg);
                log_error(msg, e);
            }
        }
    }

    /**
     * Checks if there's at least one business center that we need to send a late report to.
     *
     * @return
     * @throws Exception
     */
    private List<String> bcNamesThatNeedLateReports() throws Exception {
        List<String> bcNames = new ArrayList<String>();
        List<BusinessCenter> businessCenters = BusinessCenterMgr.list();
        // add a fake bc to check if we need to send report to admin users
        BusinessCenter admin = new BusinessCenter();
        admin.setName("admins");
        businessCenters.add(admin);

        for (BusinessCenter businessCenter : businessCenters) {
            if (shouldSendLateReportEmail(businessCenter.getName())) {
                bcNames.add(businessCenter.getName());
            }
        }
        return bcNames;
    }

    /**
     * Checks whether or not it is time to send the late evaluation report for this month.
     *
     * @param bcName
     * @return
     * @throws Exception
     */
    private boolean shouldSendLateReportEmail(String bcName) throws Exception {
        Email lastEmail = EmailMgr.getLastEmail(0, "lateReport" + bcName);
        if (lastEmail == null) {
            return true;
        }

        DateTime lastEmailSentDate = new DateTime(lastEmail.getSentDate());
        DateTime firstDayOfMonth = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay();
        return firstDayOfMonth.isAfter(lastEmailSentDate);
    }

    /**
     * Writes to disk the csv files with the list of late evaluations for each bc. Files are written to
     * Constants.TMP_DIR_REPORT_CSV + bcName + .csv
     *
     * @param lateEvaluations           List<Object[]> hibernate sql result with late evaluations
     * @return lateEvalCSVFiles         A map with the path to each one of the csv files generated.
     * @throws IOException
     */
    private void writeLateEvalsCSVFiles(List<Object[]> lateEvaluations) throws IOException {
        StringWriter stringWriter = new StringWriter();
        StringWriter adminStringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter);
        CSVWriter adminWriter = new CSVWriter(adminStringWriter);
        String headerRow = "\"Appraisal ID\",\"Employee\",\"OSU ID\",\"Position Number\",\"Supervisor\"," +
                "\"Status\",\"Appointment Type\",\"Start Date\",\"End Date\",\"Overdue Days\"," +
                "\"Business Center\"\n";

        for (int i = 0; i < lateEvaluations.size(); i++) {
            Object[] lateEval = lateEvaluations.get(i);
            String bcName = lateEval[10].toString();
            writeLateCSVRow(writer, adminWriter, lateEval);

            // Either end of input row, or the next row belongs to a different BC. Need to write the buffer to the
            // bc file and start anew.
            if (i == lateEvaluations.size() -1 || !bcName.equals(lateEvaluations.get(i + 1)[10].toString())) {
                // specify filename for the new BC so we can write string buffer to file
                String filename = getLateReportFilePath(bcName);
                PrintWriter out = new PrintWriter(filename);

                StringBuffer buffer = stringWriter.getBuffer();
                // write string buffer for the previous bc now that we are processing a different bc
                buffer.insert(0, headerRow);
                String bcLateString = buffer.toString();
                out.print(bcLateString);
                out.close();

                // clear out string buffer so that data string buffer is clean to process next BC's data
                buffer.setLength(0);
            }
        }
        writer.close();

        // Write the admin file.
        StringBuffer adminBuffer = adminStringWriter.getBuffer();
        adminBuffer.insert(0, headerRow); // insert header row for admin
        PrintWriter adminOut = new PrintWriter(getLateReportFilePath("admins"));
        adminOut.print(adminBuffer.toString());
        adminOut.close();
        adminWriter.close();
    }

    private String getLateReportFilePath(String bcName) {
        return Constants.TMP_DIR_REPORT_CSV + bcName + ".csv";
    }

    /**
     * Converts an object array, which contains the late evaluation data into a string array.
     * It then writes the row into the CSVWriter.
     *
     * @param writer            CSVWriter object to write the row into
     * @param adminWriter       CSVWriter object for the admin report
     * @param lateEval
     */
    private static void writeLateCSVRow(CSVWriter writer, CSVWriter adminWriter, Object[] lateEval) {
        ArrayList<String> row = new ArrayList<String>();
        for (Object column : lateEval) {
            if (column != null) {
                row.add(column.toString());
            } else {
                row.add(null);
            }
        }
        writer.writeNext(row.toArray(new String[row.size()]));
        adminWriter.writeNext(row.toArray(new String[row.size()]));
    }

}
