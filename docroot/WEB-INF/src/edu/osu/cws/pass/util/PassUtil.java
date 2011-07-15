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
     * @param config
     * @return
     * @throws ModelException
     */
    public static Date getDueDate(Appraisal appraisal, Configuration config) throws ModelException
    {

        Date refDate= appraisal.getEndDate();

        if (config.getReferencePoint().equals("start"))
            refDate = appraisal.getStartDate();

        Calendar cal = Calendar.getInstance();
        cal.setTime(refDate);

        int offset = config.getIntValue();
        if (config.getAction().equals("subtract"))
            offset = offset * (-1);

        cal.add(Calendar.DAY_OF_MONTH, offset); //This assumes all the days are based days, not months.
        return cal.getTime();
    }


   /**
     * Calculate the due date respect to the status and figures out if it's due.
     * For example, if status is "goals-due", then calculate the due day for goals,
     * compare to the current day to see if it's due or pass due.
     * @@@Need to rethink this.  Maybe move to the PASSUtil class
     * @param appraisal
     * @param status
     * @return <0 < overdue, 0 due, >0 not due
     * @throws Exception
     */
    public static int isDue(Appraisal appraisal, String status, Configuration config) throws Exception
    {
        int offset = config.getIntValue();
        if (config.getAction().equals("subtract"))
            offset = offset * (-1);

        Calendar dueDay = Calendar.getInstance();

        if (config.getReferencePoint().equals("start"))
            dueDay.setTime(appraisal.getStartDate());
        else
            dueDay.setTime(appraisal.getEndDate());

        dueDay.add(Calendar.DAY_OF_MONTH, offset);

        return (CWSUtil.daysBetween(dueDay.getTime(), new Date()));  //@@@Need to check direction
    }

    /**
      * Figures out if additional reminder email needs to be sent.
      * @param lastEmail
      * @param conf
      * @return
      */


     public static boolean anotherEmail(Email lastEmail, Configuration conf)
     {
         int frequency = conf.getIntValue();
         int daysPassed = CWSUtil.daysBetween(new Date(), lastEmail.getSentDate());
         return (daysPassed >= frequency);
     }

    /**
     * Formats date object with the standard dd/MM/yy used throughout the application.
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return MessageFormat.format("{0,date,dd/MM/yy}",new Object[]{date});
    }

}
