package edu.osu.cws.evals.util;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/1/11
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */

import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.util.CWSUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.osu.cws.evals.models.*;

public class EvalsUtil {
    /**
     *
     * @param appraisal
     * @param config: a appropriate configuration object. It's the caller's responsibility to evals in
     * the correct configuration.  For example, if you want to figure out if goals are due, the
     * name of the config should be "goalsDue".
     * @return a Date object presenting the date a certain status is due.
     */
    public static Date getDueDate(Appraisal appraisal, Configuration config) throws Exception {
        if (config == null)  //Should not need to do this.
            return null;

        int offset = config.getIntValue();
        if (config.getAction().equals("subtract"))
            offset = offset * (-1);

        Date refDate = appraisal.getStartDate();
        Calendar dueDay = Calendar.getInstance();
        String ref = config.getReferencePoint();

        //System.out.println("reference point = " + ref);

        if (ref.equals("end"))
            refDate = appraisal.getEndDate();
        else if (ref.equals("GOALS_REQUIRED_MOD_DATE"))
        {
            refDate = appraisal.getGoalsRequiredModificationDate();
            //System.out.println("reference date = " + refDate);
        }
        else if (ref.equals("employee_signed_date"))
            refDate = appraisal.getEmployeeSignedDate();
        else if (ref.equals("firstEmailSentDate"))
            refDate = EmailMgr.getFirstEmail(appraisal.getId(), "jobTerminated").getSentDate();

        if (refDate == null) //error
            return null;

        dueDay.setTime(refDate);
        dueDay.add(Calendar.DAY_OF_MONTH, offset);  //Assumes the offset type is Calendar.DAY_OF_MONTH
        return dueDay.getTime();
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
        Date dueDate = getDueDate(appraisal, config);
        //System.out.println("due date = " + dueDate);
        return (CWSUtil.daysBetween(dueDate, new Date()));  //@@@Need to check direction
    }

    /**
      * Figures out if additional reminder email needs to be sent.
      * @param lastEmail
      * @param config a appropriate configuration object. It's the caller's responsibility to evals in
      * the correct configuration.  For example, if you want to figure out if goals are due, the
      * name of the config should be "goalsDue".
      * @return  true if need to send another email, false otherwise.
      */
     public static boolean anotherEmail(Email lastEmail, Configuration config)
     {
         int frequency = config.getIntValue();
         int daysPassed = CWSUtil.daysBetween(new Date(), lastEmail.getSentDate());
         return (daysPassed > frequency);
     }

    /**
     * Formats date object with the standard MM/dd/yy used throughout the application.
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return MessageFormat.format("{0,date,MM/dd/yy}",new Object[]{date});
    }


    /**
     * This method assumes the existence of the following files:
     *  WEB-INF/src/backend_ecs_dev.properties
     *  WEB-INF/src/backend_ecs_prod.properties
     *  WEB-INF/src/ecs_dev.properties
     *  WEB-INF/src/ecs_prod.properties
     * @param env:  Only valid values are: "web" from the web environment, "backend" from backend cron.
     * @portletRoot: Root directory of the running portlet
     * @return: name of the configuration file specific to the hosting environment.
     * If a properties file matches the hostname exists, it returns that.
     * Else, it figures out if it's the ECS's development or production environment, and returns the appropriate filename.
     * Else, it returns null.
     */
    public static String getSpecificConfigFile(String env, String portletRoot)
    {
        if (!env.equals("web") && !env.equals("backend")) //invalid
           return null;

        String hostname = CWSUtil.getLocalHostname();
        System.out.println("hostname is " + hostname);

        String filenameHead = portletRoot + Constants.ROOT_DIR;

        if (env.equals("backend"))
            filenameHead = filenameHead + "backend_";

        String specificPropFile = filenameHead + hostname + ".properties";
        //System.out.println("specificProfFile is " + specificPropFile);
        File specificFile = new File(specificPropFile);
        if (specificFile.exists())
           return specificPropFile;

        //If we get here, the specific config file based on hostname does not exist.
        //Check for the ECS environment
        specificPropFile = null;
        if (hostname.indexOf("ucsadm") > 0) //ECS environment
        {
           if (hostname.indexOf("dev.") > 0) //ECS dev env
              specificPropFile = filenameHead + "ecs_dev"  +  ".properties";
            else  //production enviornment
              specificPropFile = filenameHead + "ecs_prod"  +  ".properties";
        }

        System.out.println("specificPropFile = " + specificPropFile);
        specificFile = new File(specificPropFile);
        if (specificFile.exists())
           return specificPropFile;
        return null;
    }

    public static int daysBeforeAppraisalDue(Job job, Date appraisalStartDate, String appraisalType,
                                             Map<String, Configuration> configMap) {
        Configuration appraisalDueConfig = configMap.get(Appraisal.STATUS_APPRAISAL_DUE);
        int offset = appraisalDueConfig.getIntValue();    //number of days before end date of appraisal

        //determine appraisal due date.
        Date appraisalEndDate = job.getEndEvalDate(appraisalStartDate, appraisalType);

        Calendar appraisalDueCal = Calendar.getInstance();
        appraisalDueCal.setTime(appraisalEndDate);
        appraisalDueCal.add(Calendar.DAY_OF_MONTH, -offset);
        Date appraisalDueDate = appraisalDueCal.getTime();
        System.out.println("appraisalDueDate = " + appraisalDueDate);
        return CWSUtil.daysBetween(appraisalDueDate, new Date());
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
            Date dueDate = EvalsUtil.getDueDate(appraisal, config);
            return -1 * CWSUtil.getRemainDays(dueDate);
        }

        return 0;
    }
}
