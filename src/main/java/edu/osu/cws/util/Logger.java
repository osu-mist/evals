package edu.osu.cws.util;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Logger
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @date: 7/14/11
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 */
public class Logger {
    public static final String EMERGENCY = "0";
    public static final String ALERT = "1";
    public static final String CRITICAL = "2";
    public static final String ERROR = "3";
    public static final String WARNING = "4";
    public static final String NOTICE = "5";
    public static final String INFORMATIONAL = "6";
    public static final String DEBUG = "7";

    private String logHost;  //the logging server
    private String clientHost;     //client requesting logging.
    private String facilityName;   // name of the app
    private String environment;

    public Logger(String logHost, String facilityName, String environment) {
        this.logHost = logHost;
        this.clientHost = CWSUtil.getLocalHostname();
        this.facilityName = facilityName;
        this.environment = environment;
    }

    /**
     * generate and log a simple GELF message
     * @param level
     * @param shortMessage
     * @param longMessage
     * @throws Exception
     */
    public void log(String level, String shortMessage, String longMessage) throws Exception {
        System.out.println(longMessage);
    }

    /**
     * generate and log a GELF message with a map of additional fields
     * @param level
     * @param shortMessage
     * @param longMessage
     * @param fields
     * @throws Exception
     */
    public void log(String level, String shortMessage, String longMessage, Map<String,String> fields) throws Exception {
        System.out.println(longMessage);
    }

    /**
     * create a log message for an exception with custom fields
     * @param level
     * @param shortMessage
     * @param exception
     * @param fields
     * @throws Exception
     */
    public void log(String level, String shortMessage, Exception exception,
                    Map<String,String> fields)  throws Exception {
        String longMessage = CWSUtil.stackTraceString(exception);
        System.out.println(longMessage);
    }

    /**
     * create a log message for an exception with no additional fields
     * @param level
     * @param shortMessage
     * @param exception
     * @throws Exception
     */
    public void log(String level, String shortMessage, Exception exception)  throws Exception {
        String longMessage = CWSUtil.stackTraceString(exception);
        System.out.println(longMessage);
    }
}
