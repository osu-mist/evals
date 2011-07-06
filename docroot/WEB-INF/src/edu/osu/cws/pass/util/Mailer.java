package edu.osu.cws.pass.util;

/**
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * Date: 6/24/11
 */
import java.util.*;
import javax.activation.MimeType;
import javax.mail.*;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.util.*;
import edu.osu.cws.pass.hibernate.AppraisalMgr;
import sun.misc.Resource;
import sun.net.www.protocol.mailto.MailToURLConnection;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.NewsAddress;
import javax.portlet.*;

public class Mailer {

    /**
     * Body and subject from resource Bundle
	 * body a dynamic message
     * Instantiate an Email object
     * and call its send function
     *
     */
    private ResourceBundle emailBundle;
	private Mail email;

 	/** gets the text from the emailResources, format it, and call the send function of
        * of the Email class.
        */

    public Mailer(ResourceBundle emailBundle, Mail email) {}

    public void sendMail(Appraisal appraisal, EmailType emailType) throws MessagingException {

        Message message = email.getMessage();

        //get mailTo from emailType   //mail to is comma separated list of roles, like "employee, supervisor"

        String mailTo = emailType.getMailTo();

        if (mailTo != null) {
            Address recipient = getEvaluatorEmail(mailTo, appraisal);
            message.setRecipient(Message.RecipientType.TO, recipient);
		    //message.setRecipient(recipient);
        }

/*
        String cc = emailType.getCc();

	    if (cc != null) {
		    getEmailAddress(appraisal, cc);
		    set the cc in the message
        }

        get bcc from emailType
	    if bcc is not null
		    bcc = getEmailAddress(appraisal, bcc)
		    set the bcc in the message
	    get the subject from the resource bundle
	    get the body from the get body method.  Maybe we should do it the way Jose in the delegate method in the portlet.

	    Transport.send(message);
	    */
   }

	//get the appropriate resourceBundle resource
	//format and return it.
    private String getGoalsDueBody(Appraisal appraisal) throws MessagingException {

        String body = emailBundle.getString("goalsDue");
        // replace body
        return body;
    }

    private String getGoalsPastDueBody(Appraisal appraisal) throws MessagingException {
       return "done";
    }

    private String getGoalsRequireModificationBody(Appraisal appraisal) throws MessagingException {
       return "done";
    }

    private Address getEvaluatorEmail(String mailTo, Appraisal appraisal) throws MessagingException {
        Employee contact = appraisal.getEvaluator();
        String address = contact.getEmail();
        Address recipient = email.stringToAddress(address);
        return recipient;
    }
}
