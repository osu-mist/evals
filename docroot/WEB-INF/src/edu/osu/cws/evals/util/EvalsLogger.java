package edu.osu.cws.evals.util;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 7/23/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 * This class wraps the edu.osu.ces.util.Logger class to provide some PASS specific fields.
 * It provides a way to be consistent across the application.
 * All PASS logging should be done by this class, not the native Logger class.
 */

import com.google.inject.Inject;
import edu.osu.cws.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class EvalsLogger implements LoggingInterface {

    private Logger logger;
    private String appName = "CWS-EvalS";

    private Map<String, String> fields = new HashMap<String, String>();


    @Inject
    public EvalsLogger(String serverName, String environment)
    {
        logger = new Logger(serverName, appName, environment);
    }

    public void log(String level, String shortMessage, String longMessage,
                    Map<String,String> myFields) throws Exception
    {
        fields.putAll(myFields);
        logger.log(level, shortMessage, longMessage, fields);
    }


    public void log(String level, String shortMessage, String longMessage) throws Exception
    {
        logger.log(level, shortMessage, longMessage, fields);
    }

    public void log(String level, String shortMessage, Exception exception,
                    Map<String,String> myFields)  throws Exception
    {
        fields.putAll(myFields);
        logger.log(level, shortMessage, exception, fields);
    }

    public void log(String level, String shortMessage, Exception exception)  throws Exception
    {
        log(level, shortMessage, exception, fields);
    }

}
