package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.util.LoggingInterface;

import java.util.HashMap;
import java.util.Map;

public class MockLogger implements LoggingInterface {

    private String appName = "CWS-EvalS";

    private Integer loggedErrorCalls = 0;

    private Map<String, String> fields = new HashMap<String, String>();

    public MockLogger() {}

    public MockLogger(String serverName, String environment)
    {
        return;
    }

    public void log(String level, String shortMessage, String longMessage,
                    Map<String,String> myFields) throws Exception
    {
        loggedErrorCalls++;
        return;
    }


    public void log(String level, String shortMessage, String longMessage) throws Exception
    {
        loggedErrorCalls++;
        return;
    }

    public void log(String level, String shortMessage, Exception exception,
                    Map<String,String> myFields)  throws Exception
    {
        loggedErrorCalls++;
        return;
    }

    public void log(String level, String shortMessage, Exception exception)  throws Exception
    {
        loggedErrorCalls++;
        return;
    }

    public Integer getLoggedErrorCalls() {
        return loggedErrorCalls;
    }
}
