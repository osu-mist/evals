/**
 * Portlet class for PASS project, used to handle the processAction and doView
 * requests. It relies heavily on the Actions class to figure out which classes
 * to delegate the work to.
 */

package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalStepMgr;
import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.util.*;
import edu.osu.cws.util.CWSUtil;
import edu.osu.cws.util.Logger;
import edu.osu.cws.util.Mail;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.portlet.*;

/**
 * <a href="EvalsPortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class EvalsPortlet extends GenericPortlet {

    public static final String CONTEXT_CACHE_TIMESTAMP = "contextCacheTimestamp";
    /**
     * String used to store the view jsp used by the
     * doView method.
     */
	protected String viewJSP = null;

    /**
     * Name of default properties file that is loaded the first time the
     * portlet is loaded.
     */
    private static final String defaultProperties = "default.properties";

    private PermissionRuleMgr permissionRuleMgr = new PermissionRuleMgr();
    private AppraisalStepMgr appraisalStepMgr = new AppraisalStepMgr();

    /**
     * Helper Liferay object to store error messages into the server's log file
     */
	private static Log _log = LogFactoryUtil.getLog(EvalsPortlet.class);

    /**
     * The actions class
     */
    private Actions actionClass = new Actions();

    public void doDispatch(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		String jspPage = renderRequest.getParameter("jspPage");

		if (jspPage != null) {
			include(jspPage, renderRequest, renderResponse);
		}
		else {
			super.doDispatch(renderRequest, renderResponse);
		}
	}

    /**
     * This method expects an "action" as a renderRequest parameter. If there is one, it tries
     * to call Actions.action method. If that method does not exist it calls the default home
     * action.
     *
     * @param renderRequest     Portlet RenderRequest object
     * @param renderResponse    Portlet RenderResponse object
     * @throws IOException
     * @throws PortletException
     */
	public void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

        // If processAction's delegate method was called, it set the viewJSP property to some
        // jsp value, if viewJSP is null, it means processAction was not called and we need to
        // call delegate
        if (viewJSP == null) {
            delegate(renderRequest, renderResponse);
        }
        actionClass.setRequestAttributes(renderRequest);

        include(viewJSP, renderRequest, renderResponse);
        viewJSP = null;
	}

    public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {

        delegate(actionRequest, actionResponse);
	}

    public void serveResource(ResourceRequest request, ResourceResponse response)
            throws PortletException, IOException {
        String result = "";
        Method actionMethod;
        String resourceID;
        Session hibSession = null;
        actionClass.setPortletContext(getPortletContext());

        // The logic below is similar to delegate method, but instead we
        // need to return the value we get from Actions method instead of
        // assign it to viewJSP
        resourceID = request.getResourceID();
        if (resourceID != null) {
            try {
                hibSession = HibernateUtil.getCurrentSession();
                Transaction tx = hibSession.beginTransaction();
                actionMethod = Actions.class.getDeclaredMethod(resourceID, PortletRequest.class,
                        PortletResponse.class);

                // The resourceID methods return the init-param of the path
                result = (String) actionMethod.invoke(actionClass, request, response);
                tx.commit();

                // If there was no string returned by the Action method, we return immediately
                if (result == null) {
                    return;
                }
            } catch (Exception e) {
                if (hibSession != null && hibSession.isOpen()) {
                    hibSession.close();
                }
                handlePASSException(e, "Error in serveResource", Logger.ERROR);
                result="There was an error performing your request";
            }
        } else {
            result="There was an error performing your request";
        }

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.print(result);
    }

    /**
     * Delegate method will call the respective method in the Actions class and pass the request
     * and response objects. The method called in Actions is based on the "action" parameter value.
     * The delegated methods in Actions class return the path to the jsp file that should be loaded.
     * Those methods also set any parameters needed by the jsp files.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @throws Exception
     */
    public void delegate(PortletRequest request, PortletResponse response) {
        Method actionMethod;
        String action = "delegate";
        viewJSP = Constants_JSP.HOME;
        Session hibSession = null;

        try {
            actionClass.setPortletContext(getPortletContext());
            portletSetup(request);
            hibSession = HibernateUtil.getCurrentSession();
            Transaction tx = hibSession.beginTransaction();
            actionClass.setUpUserPermissionInSession(request, false);

            // The portlet action can be set by the action/renderURLs using "action" as the parameter
            // name
            action =  ParamUtil.getString(request, "action", "displayHomeView");

            if (!action.equals("")) {
                actionMethod = Actions.class.getDeclaredMethod(action, PortletRequest.class, PortletResponse.class);
                viewJSP = (String) actionMethod.invoke(actionClass, request, response);
            }
            tx.commit();
        } catch (Exception e) {
            if (hibSession != null && hibSession.isOpen()) {
                hibSession.close();
            }
            handlePASSException(e, action, Logger.ERROR);
        }
    }

    /**
     * Takes care of loading the resource bundle Language.properties into the
     * portletContext.
     * @throws java.util.MissingResourceException
     */
    private void loadResourceBundle() throws MissingResourceException{
        ResourceBundle resources = ResourceBundle.getBundle("edu.osu.cws.evals.portlet.Language");
        getPortletContext().setAttribute("resourceBundle", resources);
    }

    /**
     * Takes care of initializing portlet variables and storing them in the portletContext.
     * Some of these variables are: permissionRuleMgr, appraisalStepMgr, reviewers, admins and
     * environment properties. It also creates a EvalsLogger and Mailer instances and stores them
     * in portletContext. This method is called everytime a doView, processAction or
     * serveResource are called, but the code inside only executes the first time.
     *
     * @param request
     * @throws Exception
     */
    private void portletSetup(PortletRequest request) throws Exception {
        if (getPortletContext().getAttribute("environmentProp") == null) {
            Session hibSession = null;
            actionClass.setPortletContext(getPortletContext());
            String message = loadEnvironmentProperties(request);
            createLogger();

            try {
                hibSession = HibernateUtil.getCurrentSession();
                Transaction tx = hibSession.beginTransaction();
                actionClass.setEvalsConfiguration(false);
                message += "Stored Configuration Map and List in portlet context\n";
                createMailer();
                message += "Mailer setup successfully\n";
                getPortletContext().setAttribute("permissionRules", permissionRuleMgr.list());
                message += "Stored Permission Rules in portlet context\n";
                getPortletContext().setAttribute("appraisalSteps", appraisalStepMgr.list());
                message += "Stored Appraisal Steps in portlet context\n";
                loadResourceBundle();
                message += "Stored resource bundle Language.properties in portlet context\n";
                Date currentTimestamp = new Date();
                getPortletContext().setAttribute(CONTEXT_CACHE_TIMESTAMP, currentTimestamp);
                message += "Stored contextCacheTimestamp of " + currentTimestamp.toString() + "\n";

                actionClass.setEvalsAdmins(false);
                actionClass.setEvalsReviewers(false);
                tx.commit();
                EvalsLogger logger =  getLog();
                if (logger != null) {
                    logger.log(Logger.INFORMATIONAL, "Portlet Setup Success", message);
                }
            } catch (Exception e) {
                if (hibSession != null && hibSession.isOpen()) {
                    hibSession.close();
                }
                EvalsLogger logger =  getLog();
                if (logger != null) {
                    logger.log(Logger.ERROR, "Portlet Setup Failed", message);
                }
                throw e;
            }

        }
    }

    /**
     * Creates a Mailer instance and stores it in the portlet context. It fetches the mail properties from
     * the environmentProp attribute in portlet context that comes from the properties files.
     *
     * @throws Exception
     */
    private void createMailer() throws Exception {
        ResourceBundle resources = ResourceBundle.getBundle("edu.osu.cws.evals.portlet.Email");
        CompositeConfiguration config = (CompositeConfiguration) getPortletContext().getAttribute("environmentProp");
        String hostname = config.getString("mail.hostname");
        Address from = new InternetAddress(config.getString("mail.fromAddress"));
        Address replyTo = new InternetAddress(config.getString("mail.replyToAddress"));
        String linkUrl = config.getString("mail.linkUrl");
        String mimeType = config.getString("mail.mimeType");
        Map<String, Configuration> configurationMap = (Map<String, Configuration>)
                getPortletContext().getAttribute("configurations");
        Mail mail = new Mail(hostname, from);
        Mailer mailer = new Mailer(resources, mail, linkUrl, mimeType, configurationMap, getLog(),replyTo);
        getPortletContext().setAttribute("mailer", mailer);
    }

    /**
     * Creates an instance of EvalsLogger, grabs the properties from the properties file and stores the
     * log instance in the portletContext.
     */
    private void createLogger() {
        CompositeConfiguration config = (CompositeConfiguration) getPortletContext().getAttribute("environmentProp");
        String serverName = config.getString("log.serverName");
        String clientHost = config.getString("log.clientHost");
        EvalsLogger evalsLogger = new EvalsLogger(serverName, clientHost);
        getPortletContext().setAttribute("log", evalsLogger);
    }

    /**
     * Returns a EvalsLogger instance stored in the portletContext.
     *
     * @return EvalsLogger
     * @throws Exception
     */
    private EvalsLogger getLog() throws Exception {
        return (EvalsLogger) getPortletContext().getAttribute("log");
        /* if (log == null) {
            throw new Exception("Could not get instance of EvalsLogger from portletContext");
        }
        */
       // return log;
    }

    /**
     * Loads default.properties and then overrides properties by trying to load
     * properties file: hostname.properties. The config object is then stored
     * in the portletContext.
     *
     * @param request
     * @throws Exception
     * @return message  Information logging to specify which files were loaded
     */
    private String loadEnvironmentProperties(PortletRequest request) throws Exception {
        String message = "";
        String infoMsg = "";
        CompositeConfiguration config = new CompositeConfiguration();
        String portletRoot = getPortletContext().getRealPath("/");
        String propertyFile = EvalsUtil.getSpecificConfigFile("web", portletRoot);

        // First load hostname.properties. Then try to load default.properties
        if (propertyFile != null) {
            PropertiesConfiguration propConfig =  new PropertiesConfiguration(propertyFile);
            config.addConfiguration(propConfig);
            infoMsg = propertyFile + " - loaded";
        } else {
            infoMsg = propertyFile + " - not found";
        }
        message += infoMsg + "\n";

        config.addConfiguration(new PropertiesConfiguration(defaultProperties));
        infoMsg = defaultProperties + " - loaded";
        message += infoMsg + "\n";

        // Set the Hibernate config file and store properties in portletContext
        infoMsg = "using hibernate cfg file - " + config.getString("hibernate-cfg-file");
        message += infoMsg + "\n";
        HibernateUtil.setConfig(config.getString("hibernate-cfg-file"));
        getPortletContext().setAttribute("environmentProp", config);

        return message;
    }

    /**
     * Takes care of handling portletSetup errors. This method is called by doView,
     * processAction and delegate's catch block.
     *
     * @param e Exception
     */
    private void handlePASSException(Exception e, String shortMessage, String level) {
        try {
            //getLog().log(level, shortMessage, e);
            EvalsLogger logger = getLog();
            if (logger != null)
            {
                logger.log(level, shortMessage,e);
            }
        } catch (Exception exception) {
            _log.error(CWSUtil.stackTraceString(e));
            _log.error(CWSUtil.stackTraceString(exception));
        }

        viewJSP = Constants_JSP.ERROR;
    }

    protected void include(
			String path, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher =
			getPortletContext().getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
            try {
                getLog().log(Logger.ERROR, path + " is not a valid include", "");
                //@todo: temporary fix for the null dispatcher issue.
                // Will come back to revisit next release.
                portletRequestDispatcher =
			        getPortletContext().getRequestDispatcher(Constants_JSP.HOME);
                portletRequestDispatcher.include(renderRequest, renderResponse);
            } catch (Exception e) {
                _log.error(path + " is not a valid include");
                _log.error(CWSUtil.stackTraceString(e));

            }
		}
		else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}
}
