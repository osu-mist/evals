package edu.osu.cws.util;

/**
 * Created by IntelliJ IDEA.
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * Date: 6/23/11
 */

import org.hibernate.stat.SessionStatisticsImpl;
import sun.applet.resources.MsgAppletViewer;
import sun.nio.cs.ext.MacIceland;

import javax.mail.*;
import javax.mail.internet.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Date;
import java.util.Properties;

public class Mail {
    Address from;
	Properties props = new Properties();

	public Mail(String hostname, Address from) {
         props.setProperty("mail.smtp.host", hostname);
         this.from = from;
    }

	public Message getMessage() throws MessagingException {
        Session session = Session.getInstance(props, null);
        Message message = new MimeMessage(session);
        message.setFrom(from);
        return message;
    }

	public Address[] StringToAddress(String[] emailAddresses) throws AddressException {
        Address address = new InternetAddress();
        Address[] addresses = InternetAddress.parse(java.util.Arrays.toString(emailAddresses));
        return addresses;
    }

	public InternetAddress stringToAddress(String emailAddress) throws AddressException {
        InternetAddress address = new InternetAddress(emailAddress);
        return address;
    }
}
