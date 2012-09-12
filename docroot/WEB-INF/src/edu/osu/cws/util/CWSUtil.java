package edu.osu.cws.util;

import edu.osu.cws.evals.portlet.Constants;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.text.*;
import java.util.StringTokenizer;
import java.net.*;

/**
 *
 */
public class CWSUtil
{
    /**
     * Adds to or subtracts from date the offset amount of time, and return if the
     * resulting time is in the past or the future.
     * @param date: reference point
     * @param offset: offset from reference point
     *                positive Number means addition, negative means subtraction.
     * @param offsetType:  Calendar.MONTH or Calendar.DAY_OF_MONTH
     * @return true if the resulting date is on or after the current time, false otherwise.
     */
   public static boolean isOnOrAfterNow(Date date, int offset, int offsetType)
    {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);

	    if ((offsetType != Calendar.MONTH) && (offsetType != Calendar.DAY_OF_MONTH))
            //invalid offsetType
		    return false;

		cal.add(offsetType, offset);
        Calendar today = Calendar.getInstance();
        if (cal.equals(today))
            return true;
        return (cal.after(today));
    }

    /**
     *
     * @param cal
     * @return true is the time represented by cal is on or after the current time.
     */
    public static boolean isOnOrAfterNow(Calendar cal)
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
     *          positive if d1 is after d2, negative number otherwise.
     */
    public static int daysBetween(Date d1, Date d2)   throws Exception
    {
        Format format = new SimpleDateFormat("MM-dd-yy");
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");
        String s1 = format.format(d1);
        d1 = (Date)dateFormat.parse(s1);
        String s2 = format.format(d1);
        d2 = (Date)dateFormat.parse(s2);
        return (int) ((d1.getTime() - d2.getTime()) / (1000 * 60 * 60 * 24));
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
     * @param startDate: reference point
     * @param offset: amount of time from reference point, positive or negative
     * @param offsetType, either Calendar.MONTH or Calendar.DAY_OF_MONTH
     * @return a new date by adding offset to startDate
     */
   public static Date getNewDate(Date startDate, int offset, int offsetType)
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
     * @param date: reference point
     * @return if date is the first date of the month, return the input date
     * else return the first day of next month
     */
   public static Date getFirstDayOfMonth(Date date)
   {
       Calendar cal = Calendar.getInstance();
       cal.setTime(date);
       if (cal.get(Calendar.DAY_OF_MONTH) != 1)
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
     * Positive indicates dueDate is in the future
     * Negative indicates dueDate is in the past (overdue)
     */
   public static int getRemainDays(Date dueDate) throws Exception
   {
	    return CWSUtil.daysBetween(dueDate, new Date());
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

    /**
     *
     * @return the name of the local host.
     */
    public static String getLocalHostname()
    {
      try
      {
        InetAddress address = InetAddress.getLocalHost();
        return address.getHostName();
      } catch(UnknownHostException e)
      {
        return null;
      }
    }

    /**
     * Simple method that takes in an exception and returns the stacktrace as a string. We
     * need this so that if we get an exception, we can send it to the luminis log.
     *
     * @param e     Exception
     * @return  String stack trace
     */
    public static String stackTraceString(Exception e) {
        StringWriter writerStr = new StringWriter();
        PrintWriter myPrinter = new PrintWriter(writerStr);
        e.printStackTrace(myPrinter);
        return writerStr.toString();
    }

    /**
     * Simple method that escapes user data, and converts \n to <br />
     *
     * @param dirtyText
     * @return
     */
    public static String escapeHtml(String dirtyText) {
        String out = "";
        if (dirtyText != null) {
            out = dirtyText;
            out = StringEscapeUtils.escapeHtml(out);
            out = out.replaceAll("\n", "<br />");
        }

        return out;
    }

    /**
     * Accept as names: string separated by space, commas or dashes.
     * Does not accept symbols other than - or numbers.
     *
     * @param name
     * @return
     */
    public static boolean validateNameSearch(String name) {
        name = StringUtils.lowerCase(name);
        name = StringUtils.trim(name);

        if (StringUtils.isEmpty(name)) {
            return false;
        }

        String pattern = "([a-z,\\-\\s])+";
        return name.matches(pattern);
    }

    /**
     * We only accept numeric 9 digit string as a valid osu id.
     *
     * @param osuid
     * @return
     */
    public static boolean validateOsuid(String osuid) {
        return StringUtils.isNumeric(osuid) && osuid.length() == 9;
    }


    /**
     * A valid orgCode contains 6 digits and is numeric.
     *
     * @param orgCode
     * @return
     */
    public static boolean validateOrgCode(String orgCode) {
        return StringUtils.isNumeric(orgCode) && orgCode.length() == Constants.MAX_ORG_CODE_DIGITS;
    }
}
