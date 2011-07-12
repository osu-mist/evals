package edu.osu.cws.pass.util;

/**
 * @author Kenneth Lett <kenneth.lett@oregonstate.edu>
 * @copyright Copyright 2011, Central Web Services, Oregon State University
 * Date: 6/24/11
 */
import java.lang.reflect.Method;
import javax.mail.*;
import javax.mail.internet.NewsAddress;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.util.*;
import edu.osu.cws.pass.util.PassUtil;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.text.MessageFormat;

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
    private String linkURL;
    private String mimeType;
    private Map<String, Configuration> configMap;
    
 	/** gets the text from the emailResources, format it, and call the send function of
        * of the Email class.
        */

    public Mailer(ResourceBundle resources, Mail mail,
                  String linkURL, String mimeType, Map<String, Configuration> map) {
        this.email = mail;
        this.emailBundle = resources;
        this.linkURL = linkURL;
        this.mimeType = mimeType;
        configMap = map;

    }

    public void sendMail(Appraisal appraisal, EmailType emailType) throws Exception {

        Message msg = email.getMessage();
        String mailTo = emailType.getMailTo();

        if (mailTo != null) {
            Address[] to = getRecipients(mailTo, appraisal);
            msg.addRecipients(Message.RecipientType.TO, to);
        }

        String mailCC = emailType.getCc();

	    if (mailCC != null) {
            Address[] cc = getRecipients(mailCC, appraisal);
            msg.addRecipients(Message.RecipientType.CC, cc);
        }

        String mailBCC = emailType.getBcc();

	    if (mailBCC != null) {
            Address[] bcc = getRecipients(mailBCC, appraisal);
            msg.addRecipients(Message.RecipientType.BCC, bcc);
        }

        String body = getBody(appraisal, emailType);

        msg. setContent(body, "text/html");
        String subjectResourceKey = "email_" + emailType.getType() + "_subject";
        String subject = emailBundle.getString(subjectResourceKey);

        msg.setSubject(subject);
        Transport.send(msg);
   }

    private Address[] getRecipients(String mailTo, Appraisal appraisal) throws MessagingException {
        String[] mailToArray = mailTo.split(",");
        Address[] recipients = new Address[mailToArray.length];
        String contact = "";
        int i = 0;
        for (String recipient : mailToArray) {
            if (recipient.equals("employee")) {
                contact = appraisal.getJob().getEmployee().getEmail();
            }
            if (recipient.equals("supervisor")) {
                contact = appraisal.getJob().getSupervisor().getEmployee().getEmail();
            }

            recipients[i++] = email.stringToAddress(contact);
        }
        return recipients;
    }

    private String getBody(Appraisal appraisal, EmailType emailType) throws Exception {

        String body = emailBundle.getString("email_body");
        Job job = appraisal.getJob();
        Employee employee = job.getEmployee();
        String bcDescriptor = getBusinessCenterDescriptor(job);
        String jobTitle = job.getJobTitle();
        String firstName =  employee.getFirstName();
        String lastName = employee.getLastName();
        String name = firstName + " " + lastName;
        Method bodyMethod;

        // evaluation period
        // Integer days = job.
        // evaluation time

        String bodyResourceKey = "email_" + emailType.getType() + "_body";
        String bodyString = emailBundle.getString(bodyResourceKey);
        String bodyMethodId = emailType.getType() + "Body";

        if (!bodyMethodId.equals("")) {
            bodyMethod = Mailer.class.getDeclaredMethod(bodyMethodId, String.class, String.class, String.class, Integer.class);
            bodyString = (String) bodyMethod.invoke(this, bodyString, jobTitle, "now til then", 30);
        }

        body = MessageFormat.format(body, name, bodyString, bcDescriptor, linkURL);
        return body;
    }

    private String goalsDueBody(Appraisal appraisal, String resourceKey) throws Exception {
        Configuration config = configMap.get(appraisal.getStatus());
        Date dueDay = PassUtil.getDueDate(appraisal, config);
        int days = CWSUtil.daysBetween(new Date(), dueDay);
        String jobTitle = getJobTitle(appraisal);
        
        String bodyString = emailBundle.getString(resourceKey);

        String body = MessageFormat.format(bodyString, jobTitle, "evaluationPeriod", days);
        return body;
    }
    
    private String getBusinessCenterDescriptor(Job job) {
        String bcName = job.getBusinessCenterName();
        String bcKey = "businesscenter_" + bcName + "_descriptor";
        String bcDescriptor = emailBundle.getString(bcKey);
        return bcDescriptor;
    }


    private String getJobTitle(Appraisal appraisal) {
        return appraisal.getJob().getJobTitle();
    }
    private String getJob(Appraisal appraisal) {
        return appraisal.getJob().getJobTitle();
    }
}
