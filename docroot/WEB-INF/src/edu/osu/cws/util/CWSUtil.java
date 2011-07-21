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
     * @param start: start of the period
     * @param end: end of the period
     * @param target: The date in question
     * @return true is target is the same as start or end, or target is between start and end.  False otherwise.
     */
    public static boolean isWithinPeriod(Date start, Date end, Date target)
    {

        if (target.equals(start) || target.equals(end))   //on the boundary
            return true;

        return (target.after(start) && target.before(end));
    }

    /**
     *
     * @param start
     * @param end
     * @return true is the current time is between start and end, false otherwise.
     */
    public static boolean isWithinPeriod(Date start, Date end)
    {
        Date now = new Date();
        return isWithinPeriod(start, end, now);
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
     * @param date
     * @return if date is the first date of the month, return date, else return the first day of next month
     */
   public static Date firstDayOfMonth(Date date)
   {
       Calendar cal = Calendar.getInstance();
       cal.setTime(date);
       if (cal.get(Calendar.DAY_OF_MONTH) > 1)
       {
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
       }
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
