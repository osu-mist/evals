package edu.osu.cws.util;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 6/30/11
 * Time: 4:33 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 */
public class CWSUtil
{
    /**
     * Adds or subtracts the offset amount of time to the given calendar field
     * @param date: reference point
     * @param offset: offset from reference point
     *                positive Number means addition, negative means subtraction.
     * @param offsetType:  Calendar.MONTH or Calendar.DAY_OF_MONTH
     * @return true if the resulting date is on or after the current time.
     */
   public static boolean onOrAfterNow(Date date, int offset, int offsetType)
    {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);

	    if ((offsetType != Calendar.MONTH) && (offsetType != Calendar.DAY_OF_MONTH))
            //@@@invalid offsetType, not sure this is the right way to handle it.
		    return false;

		cal.add(offsetType, offset);
	    return onOrAfterNow(cal);
    }

    /**
     *
     * @param cal
     * @return true is the time represented by cal is on or after the current time.
     */
    public static boolean onOrAfterNow(Calendar cal)
    {
  	    Calendar today = Calendar.getInstance();
        if (cal.equals(today))
            return true;
        return cal.after(today);
    }

    /**
     *
     * @param d1
     * @param d2
     * @return the number of days between the 2 date object.
     *          positive number is d1 is later than d2, negative number otherwise.
     */
    public static int daysBetween(Date d1, Date d2)
    {
	    //returns positive number if dueDate is in the future
	    //returns negative number if dueDate is in the past.
	    return (int)( (d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     *
     * @param d1
     * @param d2
     * @return: true if the current time is in between d1 and d2, including d1 and d2.
     */
    public static boolean isWithinPeriod(Date d1, Date d2)
    {
        Date now = new Date();
        if (now.equals(d1) || now.equals(d2))   //on the boundary
            return true;

        Date begin = d1;
        Date end = d2;
        if (d1.after(d2))
        {
            begin = d2;
            end = d1;
        }
        return (now.after(begin) && now.before(end));
    }

    /**
     * @@@This seems to be a duplicate of getDueDate.  Need to think!
     * @param startDate: reference point
     * @param offset: amount of time from reference point, positive or negative
     * @param offsetType, either Calendar.MONTH or Calendar.DAY_OF_MONTH
     * @return a new date by adding offset to startDate
     */
   public static Date getEndDate(Date startDate, int offset, int offsetType)
   {
      if ((offsetType != Calendar.MONTH) && (offsetType != Calendar.DAY_OF_MONTH))
          //invalid input
          return null;

      Calendar cal = Calendar.getInstance();
      cal.setTime(startDate);
      cal.add(offsetType, offset);
      return cal.getTime();
   }

    /**
     *
     * @param d
     * @return a date representing 1st day of the month after the month of the input date
     */
   public static Date firstDateNextMonth(Date d)
   {
       Calendar cal = Calendar.getInstance();
       cal.setTime(d);
       cal.add(Calendar.MONTH, 1);
       cal.set(Calendar.DAY_OF_MONTH, 1);
       return cal.getTime();
   }


    /**
     *
     * @param dueDate
     * @return Number of day between today and dueDate.
     * Positive # indicates dueDate is in the future
     * Negative number indicate dueDate is in the past (overdue)
     */
   public static int getRemainDays(Date dueDate)
   {
	    Date now = new Date();
	    return CWSUtil.daysBetween(now, dueDate);
   }


    /**
     *
     * @param str: the string to be converted to array
     * @param delimiter: delimiter to use for splitting the string
     * @return:
     */
    public static String[] stringToArray(String str, String delimiter)
    {
       if (str == null)
           return null;

       StringTokenizer st = new StringTokenizer(str, delimiter);
       String[] tokens = new String[st.countTokens()];
       int index = 0;

      while (st.hasMoreTokens()) {
         tokens[index++] = st.nextToken().trim();
      }
       return  tokens;
    }

    /**
     * Calls   stringToArray(String str, String delimiter) assuming space as the delimiter
     * @param str
     * @return
     */
    public static String[] stringToArray(String str)
    {
        return  stringToArray(str, " ");
    }
}
