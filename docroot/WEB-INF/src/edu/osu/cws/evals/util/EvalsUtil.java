package edu.osu.cws.evals.util;

import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.portlet.PortletContext;
import java.io.File;
import java.text.ParseException;
import java.util.*;

public class EvalsUtil {
    private static DateTime evalsStartDate = null;

    /**
     *
     * @param appraisal
     * @param config: a appropriate configuration object. It's the caller's responsibility to evals in
     * the correct configuration.  For example, if you want to figure out if goals are due, the
     * name of the config should be "goalsDue".
     * @return a Date object presenting the date a certain status is due.
     */
    public static DateTime getDueDate(Appraisal appraisal, Configuration config) throws Exception {
        if (config == null)  //Should not need to do this.
            return null;

        int offset = config.getIntValue();
        if (config.getAction().equals("subtract"))
            offset = offset * (-1);

        DateTime refDate = new DateTime(appraisal.getStartDate()).withTimeAtStartOfDay();
        String ref = config.getReferencePoint();

        //System.out.println("reference point = " + ref);

        if (ref.equals("end")) {
            refDate = new DateTime(appraisal.getEndDate()).withTimeAtStartOfDay();
        } else if (ref.equals("GOALS_REQUIRED_MOD_DATE")) {
            refDate = new DateTime(appraisal.getUnapprovedGoalsVersion().getGoalsRequiredModificationDate()).withTimeAtStartOfDay();
        } else if (ref.equals("employee_signed_date")) {
            refDate = new DateTime(appraisal.getEmployeeSignedDate()).withTimeAtStartOfDay();
        } else if (ref.equals("firstEmailSentDate")) {
            Email firstEmail = EmailMgr.getFirstEmail(appraisal.getId(), "jobTerminated");
            refDate = new DateTime(firstEmail.getSentDate()).withTimeAtStartOfDay();
        } else if (ref.equals("goal_reactivation_request")) {
            refDate = AppraisalMgr.getPendingRequestGoalVersionCreateDate(appraisal.getId());
        } else if (ref.equals("goal_reactivation_req_dec")) {
            refDate = AppraisalMgr.getUnapprovedGoalVersionRequestDecDate(appraisal.getId());
        }

        if (refDate == null) //error
            return null;

        return refDate.plusDays(offset).withTimeAtStartOfDay();  //Assumes the offset type is Calendar.DAY_OF_MONTH
    }


   /**
     * Calculate the due date respect to the status and figures out if it's due.
     * For example, if status is "goals-due", then calculate the due day for goals,
     * compare to the current day to see if it's due or evals due.
     * @@@Need to rethink this.  Maybe move to the PASSUtil class
     * @param appraisal: the appraisal record of interest
     * @param config a appropriate configuration object. It's the caller's responsibility to evals in
     * the correct configuration.  For example, if you want to figure out if goals are due, the
     * name of the config should be "goalsDue".
     * @return <0 < overdue, 0 due, >0 due day in the future, or not due yet.
     */
    public static int isDue(Appraisal appraisal, Configuration config) throws Exception {
        DateTime dueDate = getDueDate(appraisal, config);
        return Days.daysBetween(getToday(), dueDate).getDays();
    }

    /**
      * Figures out if additional reminder email needs to be sent.
      * @param lastEmail
      * @param config a appropriate configuration object. It's the caller's responsibility to evals in
      * the correct configuration.  For example, if you want to figure out if goals are due, the
      * name of the config should be "goalsDue".
      * @return  true if need to send another email, false otherwise.
      */
     public static boolean anotherEmail(Email lastEmail, Configuration config) throws Exception {
         DateTime sentDate = new DateTime(lastEmail.getSentDate()).withTimeAtStartOfDay();
         int offset = config.getIntValue();
         return sentDate.plusDays(offset).isBeforeNow();
     }

    /**
     * This method assumes the existence of the following file: WEB-INF/src/evals.properties
     *
     * @portletRoot: Root directory of the running portlet
     * @return: name of the configuration file specific to the hosting environment.
     */
    private static String getPropertyFileName(String portletRoot) {
        String hostname = CWSUtil.getLocalHostname();
        System.out.println("hostname is " + hostname);
        String filenameHead = portletRoot + Constants.getRootDir();
        String propertyFileName = filenameHead  + Constants.PROPERTIES_FILENAME;
        System.out.println("propertyFileName is " + propertyFileName);

        File propertyFile = new File(propertyFileName);
        if (propertyFile.exists() && propertyFile.canRead()) {
           return propertyFileName;
        }

        return null;
    }

    /**
     * Returns the evals.properties PropertiesConfiguration object. It figures out if it is
     * called from the web or backend based on whether or not the portletContext is null or not.
     *
     * @param context       PortletContext
     * @return
     * @throws Exception
     */
    public static PropertiesConfiguration loadEvalsConfig(PortletContext context)
            throws Exception {
        // If we have a portletContext object, we are called from the web and need to get the path
        String portletRoot = "";
        if (context != null) {
            portletRoot = context.getRealPath("/");
        }

        // Get the path and name of properties file to load
        String propertyFile = getPropertyFileName(portletRoot);
        if (propertyFile != null) {
            return overWriteDefaultConfigs(new PropertiesConfiguration(propertyFile));
        }

        return null;
    }

    /**
     * Parses through the properties defined in the configuration file. It tries to find if there
     * is a host/vm specific property that overwrites the value of each one of the properties. If it
     * finds a host property that takes precedence, it overwrites the default value with the host
     * specific one.
     *
     * @param config
     * @return
     */
    private static PropertiesConfiguration overWriteDefaultConfigs(PropertiesConfiguration config) {
        String hostPrefix = EvalsUtil.getPropertyPrefix();
        for (Iterator keys = config.getKeys(); keys.hasNext();) {
            String key = keys.next().toString();
            String hostBasedKey = hostPrefix + "." + key;
            if (config.containsKey(hostBasedKey)) {
                config.setProperty(key, config.getString(hostBasedKey));
            }
        }

        return config;
    }

    /**
     * Returns the property prefix to use when looking for properties overwritten in the
     * evals.properties file.
     * It figures out if it's the ECS's development or production environment, and returns either:
     * "ecs_dev" or "ecs_prod"
     * Else, it returns the hostname.
     *
     * @return String
     */
    private static String getPropertyPrefix() {
        String hostname = CWSUtil.getLocalHostname();

        if (hostname.contains("ucsadm")) { // ECS environment
            if (hostname.contains("dev.")) { //ECS dev env
                return "ecs_dev";
            } else {  //production environment
                return "ecs_prod";
            }
        }

        return  hostname;
    }

    /**
     * This method calculates the # of days an evaluation record is overdue. The are three
     * possible types of values:
     *   0 - due today
     * < 0 - x number of days before it's due
     * > 0 - overdue by x number of days
     */
    public static int getOverdue(Appraisal appraisal, Map<String, Configuration> configurationMap)
            throws Exception {
        int INVALID_PARAM = -999;
        String status = appraisal.getStatus();
        Configuration config = null;

        if (status.equals(Appraisal.STATUS_GOALS_REACTIVATED) || status.equals(Appraisal.STATUS_GOALS_APPROVED)) {
            return INVALID_PARAM;
        }

        if (status.equals(Appraisal.STATUS_COMPLETED) || status.equals(Appraisal.STATUS_CLOSED)) {
            return appraisal.getOverdue();
        }

        if (status.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)) {
            config = configurationMap.get(Appraisal.STATUS_GOALS_DUE);
        } else {
            if (status.contains("Overdue")) {
                status = status.replace("Overdue", "Due");
            }
            config = configurationMap.get(status);
        }

        if (config != null) {
            DateTime dueDate = EvalsUtil.getDueDate(appraisal, config);
            return Days.daysBetween(dueDate, EvalsUtil.getToday()).getDays();
        }

        return 0;
    }

    /**
     * Returns the start with clause so that we can get the oracle hierarchical data
     * for the current supervisor level.
     *
     * @return
     */
    public static String getStartWithClause(int directSupervisorCount) {
        ArrayList<String> startWithClause = new ArrayList<String>();
        for (int i = 0; i < directSupervisorCount; i++) {
            String jobClause = "(pyvpasj_pidm = :startWithPidm" + i +
                     " AND pyvpasj_posn = :startWithPosnNo" + i +
                    " AND pyvpasj_suff = :startWithSuffix" + i + ") ";
            startWithClause.add(jobClause);
        }
        String startWith = "START WITH ";
        startWith += StringUtils.join(startWithClause, " OR ");
        return startWith;
    }

    /**
     * Sets the pidm, posno and suffix parameters used by the start with
     * clause.
     *
     * @param directSupervisors
     * @param query
     */
    public static void setStartWithParameters(List<Job> directSupervisors, Query query) {
        int i = 0;
        for (Job directSupervisor : directSupervisors) {
            query.setInteger("startWithPidm"+i, directSupervisor.getEmployee().getId())
                    .setString("startWithPosnNo"+i, directSupervisor.getPositionNumber())
                    .setString("startWithSuffix"+i, directSupervisor.getSuffix());
            i++;
        }
    }

    /**
     * Parses the constant EVALS_START_DATE and stores a date object in evalsStartDate.
     *
     * @return  DateTime object
     * @throws ParseException
     */
    public static DateTime getEvalsStartDate() throws ParseException {
        if (evalsStartDate != null) {
            return evalsStartDate;
        } else {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_FORMAT_FULL);
            evalsStartDate = fmt.parseDateTime(Constants.EVALS_START_DATE);
        }

        return evalsStartDate;
    }

    /**
     * Creates an EvalsLogger instance
     *
     * @return EvalsLogger
     * @throws Exception
     */
    public static LoggingInterface createLogger(PropertiesConfiguration config) throws Exception{
        String serverName = config.getString("log.serverName");
        String environment = config.getString("log.environment");

        return new EvalsLogger(serverName, environment);
    }

    /**
     * Creates an Mailer instance
     *
     * @return Mailer
     * @throws Exception
     */
    public static MailerInterface createMailer(PropertiesConfiguration config,
                                      Map<String, Configuration> configurationMap,
                                      LoggingInterface logger) throws Exception {
        ResourceBundle resources = ResourceBundle.getBundle(Constants.EMAIL_BUNDLE_FILE);
        String hostname = config.getString("mail.hostname");
        String from = config.getString("mail.fromAddress");
        String replyTo = config.getString("mail.replyToAddress");
        String linkUrl = config.getString("mail.linkUrl");
        String helpLinkUrl = config.getString("helpfulLinks.url");
        String environment = config.getString("log.environment");
        String testMailToAddress = null;
        if (!environment.startsWith("prod")){
            testMailToAddress = config.getString("mail.testMailToAddress");
        }
        return new Mailer(resources, hostname, from, linkUrl,  helpLinkUrl, configurationMap,
                logger, replyTo, testMailToAddress);
    }

    public static DateTime getToday() {
        return new DateTime().withTimeAtStartOfDay();
    }
}
