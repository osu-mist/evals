package edu.osu.cws.pass.util;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/1/11
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */

import edu.osu.cws.pass.hibernate.EmailMgr;
import edu.osu.cws.util.CWSUtil;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import edu.osu.cws.pass.models.*;
import java.util.Map;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.hibernate.JobMgr;
import edu.osu.cws.pass.hibernate.AppraisalMgr;


public class PassUtil {


    /**
     *
     * @param appraisal
     * @param config: a appropriate configuration object. It's the caller's responsibility to pass in
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

        System.out.println("reference point = " + ref);

        if (ref.equals("end"))
            refDate = appraisal.getEndDate();
        else if (ref.equals("GOALS_REQUIRED_MOD_DATE"))
        {
            refDate = appraisal.getGoalsRequiredModificationDate();
            System.out.println("reference date = " + refDate);
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
     * compare to the current day to see if it's due or pass due.
     * @@@Need to rethink this.  Maybe move to the PASSUtil class
     * @param appraisal: the appraisal record of interest
     * @param config a appropriate configuration object. It's the caller's responsibility to pass in
     * the correct configuration.  For example, if you want to figure out if goals are due, the
     * name of the config should be "goalsDue".
     * @return <0 < overdue, 0 due, >0 due day in the future, or not due yet.
     */
    public static int isDue(Appraisal appraisal, Configuration config) throws Exception {
        Date dueDate = getDueDate(appraisal, config);
        System.out.println("due date = " + dueDate);
        return (CWSUtil.daysBetween(dueDate, new Date()));  //@@@Need to check direction
    }

    /**
      * Figures out if additional reminder email needs to be sent.
      * @param lastEmail
      * @param config a appropriate configuration object. It's the caller's responsibility to pass in
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

}
