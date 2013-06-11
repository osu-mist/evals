package edu.osu.cws.evals.util;

import java.util.Map;

public interface LoggingInterface {

    public void log(String level, String shortMessage, String longMessage,
                    Map<String,String> myFields) throws Exception;

    public void log(String level, String shortMessage, String longMessage) throws Exception;


    public void log(String level, String shortMessage, Exception exception,
                    Map<String,String> myFields)  throws Exception;

    public void log(String level, String shortMessage, Exception exception)  throws Exception;


}
