package edu.osu.cws.util;

import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Logger
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * Date: 7/14/11
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

    private String logHost;

    public Logger(String logHost) {
        this.logHost = logHost;
    }

    public void log(GelfMe`ssage message) throws Exception {
        GelfSender gelfSender = new GelfSender(logHost);
        message.setHost("Portal");
        if (message.isValid()) {
            gelfSender.sendMessage(message);
        }
    }

    public void log(String level, String shortMessage, String longMessage) throws Exception {
        GelfMessage message = new GelfMessage(shortMessage, longMessage, new Date(), level);
        log(message);
    }

    public void log(String level, String shortMessage, String longMessage, Map<String,String> fields) throws Exception {
        GelfMessage message = new GelfMessage(shortMessage, longMessage, new Date(), level);
        Set<String> keys = fields.keySet();
        for (String key : keys) {
            message.addField(key, fields.get(key));
        }
        log(message);
    }
}