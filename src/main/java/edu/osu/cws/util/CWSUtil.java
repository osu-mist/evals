package edu.osu.cws.util;

import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.EvalsUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.*;
import java.text.*;
import java.net.*;

/**
 *
 */
public class CWSUtil {


    /**
     *
     * @param start: (DateTime) start of the period
     * @param end: (DateTime) end of the period
     * @param target: (DateTime) The date in question
     * @return true is target is the same as start or end, or target is between start and end.  False otherwise.
     */
    public static boolean isWithinPeriod(DateTime start, DateTime end, DateTime target) {
        return target.equals(start) || target.equals(end) ||
                (target.isAfter(start) && target.isBefore(end));

    }

    /**
     *
     * @param start     DateTime object
     * @param end       DateTime object
     * @return true is the current time is between start and end, false otherwise.
     */
    public static boolean isWithinPeriod(DateTime start, DateTime end) {
        return isWithinPeriod(start, end, EvalsUtil.getToday());
    }

    /**
     *
     * @param dt: reference point
     * @return (DateTime) if date is the first date of the month, return the input date
     * else return the first day of next month
     */
   public static DateTime getFirstDayOfMonth(DateTime dt) {
       if (dt.getDayOfMonth() != 1) {
           dt = dt.plusMonths(1).withDayOfMonth(1);
       }
       return dt;
   }

    /**
     * Null safe way to convert Joda Datetime to Date.
     *
     * @param dt    DateTime object
     * @return
     */
    public static Date toDate(DateTime dt) {
        if (dt == null) {
            return null;
        }

        return dt.toDate();
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

    /**
     * Formats a given amount into a currency string.
     *
     * @param amount
     * @return
     */
    public static String formatCurrency(Double amount) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance();
        return fmt.format(amount);
    }
}
