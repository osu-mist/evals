package edu.osu.cws.util;

import com.liferay.portal.kernel.log.Log;
import org.apache.commons.configuration.CompositeConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class ExceptionHandler {

    /**
     * Exception we are handling
     */
    private Exception e;

    private Log log;

    /**
     * Properties containing email information to send error reports
     */
    private CompositeConfiguration props;

    /**
     * Name of the application
     */
    private String appName;

    public ExceptionHandler(Exception e, Log log, CompositeConfiguration props, String appName) {
        this.e = e;
        this.log = log;
        this.props = props;
        this.appName = appName;
    }

    /**
     * Sends email to developer and logs the error.
     */
    public void handleException() {
        emailDeveloper();
        log.error(appName + " - " + stackTraceString(e));
    }

    /**
     * Sends email to developer
     */
    private void emailDeveloper() {
        //@todo: still have to implement
        //@todo: do we want to use liferay provided classes or the built-in java mail class?
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
}
