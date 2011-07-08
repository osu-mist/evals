package edu.osu.cws.util;

/**
 * Created by IntelliJ IDEA.
 * User: luf
 * Date: 6/30/11
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */

import javax.mail.Address;
import javax.mail.Message;
import java.util.Properties;


public class Mail {
    Address from;
	Properties prop;  //this is the properties that holds the mailHost and is used to create a session.

	public Mail(String hostname, String from)  throws Exception
    {

    }
	/*
    public Message getMessage() throws Exception
    {
      return new Message();
    }

	private Address[] StringToAddress(String[] emailAddresses) throws Exception
    {

    }

	private Address stringToAddress(String emailAddress) throws Exception
    {

    }
    */

}
