/**
 * Portlet class for PASS project, used to handle the processAction and doView
 * requests. It relies heavily on the ActionHelper class to figure out which classes
 * to delegate the work to.
 */

package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import edu.osu.cws.evals.hibernate.AppraisalStepMgr;
import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.util.*;
import edu.osu.cws.util.CWSUtil;
import edu.osu.cws.util.Logger;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import javax.portlet.*;

/**
 * <a href="EvalsPortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class EvalsPortlet extends GenericPortlet {

    public static final String CONTEXT_CACHE_TIMESTAMP = "contextCacheTimestamp";

    public static final String CONTEXT_LOAD_DATE = "contextLoadDate";

    /**
     * String used to store the view jsp used by the
     * doView method.
     */
	protected String viewJSP = null;

    private PermissionRuleMgr permissionRuleMgr = new PermissionRuleMgr();
    private AppraisalStepMgr appraisalStepMgr = new AppraisalStepMgr();

    /**
     * Helper Liferay object to store error messages into the server's log file
     */
	private static Log _log = LogFactoryUtil.getLog(EvalsPortlet.class);

    /**
     * The actions class
     */
    private ActionHelper actionHelper;

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
     * to call ActionHelper.action method. If that method does not exist it calls the default home
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
        actionHelper.setRequestAttributes(renderRequest);

        include(viewJSP, renderRequest, renderResponse);
        viewJSP = null;
        actionHelper.removeRequestMap();
	}

    public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {
        delegate(actionRequest, actionResponse);
	}

    public void serveResource(ResourceRequest request, ResourceResponse response)
            throws PortletException, IOException {
        String result = "";
        String resourceID;
        Session hibSession = null;

        // The logic below is similar to delegate method, but instead we
        // need to return the value we get from ActionHelper method instead of
        // assign it to viewJSP
        resourceID = request.getResourceID();
        String controllerClass =  ParamUtil.getString(request, "controller", "HomeAction");
        if (resourceID != null && controllerClass != null) {
            try {
                actionHelper = new ActionHelper(request, response, getPortletContext());
                controllerClass = "edu.osu.cws.evals.portlet." + controllerClass;
                ActionInterface controller = (ActionInterface) Class.forName(controllerClass).newInstance();
                ErrorHandler errorHandler = new ErrorHandler(actionHelper);
                controller.setActionHelper(actionHelper);
                controller.setErrorHandler(errorHandler);
                HomeAction homeAction = new HomeAction();
                homeAction.setActionHelper(actionHelper);
                homeAction.setErrorHandler(errorHandler);
                controller.setHomeAction(homeAction);

                hibSession = HibernateUtil.getCurrentSession();
                Transaction tx = hibSession.beginTransaction();
                Method controllerMethod = controller.getClass().getDeclaredMethod(
                        resourceID, PortletRequest.class, PortletResponse.class);

                // The resourceID methods return the init-param of the path
                result = (String) controllerMethod.invoke(controller, request, response);
                tx.commit();

                // If there was no string returned by the Action method, we return immediately
                if (result == null) {
                    return;
                }
            } catch (Exception e) {
                if (hibSession != null && hibSession.isOpen()) {
                    hibSession.close();
                }
                handleEvalsException(e, "Error in serveResource", Logger.ERROR, request);
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
     * Delegate method will call the respective method in the ActionHelper class and pass the request
     * and response objects. The method called in ActionHelper is based on the "action" parameter value.
     * The delegated methods in ActionHelper class return the path to the jsp file that should be loaded.
     * Those methods also set any parameters needed by the jsp files.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @throws Exception
     */
    public void delegate(PortletRequest request, PortletResponse response) {
        String action = "delegate";
        Session hibSession = null;

        try {
            portletSetup(request);
            hibSession = HibernateUtil.getCurrentSession();
            Transaction tx = hibSession.beginTransaction();
            actionHelper = new ActionHelper(request, response, getPortletContext());
            actionHelper.setUpUserPermission(false);
            if (actionHelper.isDemo()) {
                actionHelper.setupDemoSwitch();
            }
            actionHelper.addToRequestMap("isDemo", actionHelper.isDemo());

            // The portlet action can be set by the action/renderURLs using "action" as the parameter
            // name
            action =  ParamUtil.getString(request, "action", "display");
            String controllerClass =  ParamUtil.getString(request, "controller", "HomeAction");
            if (controllerClass != null && !action.equals("")) {
                controllerClass = "edu.osu.cws.evals.portlet." + controllerClass;

                ActionInterface controller = (ActionInterface) Class.forName(controllerClass).newInstance();
                controller.setActionHelper(actionHelper);
                ErrorHandler errorHandler = new ErrorHandler(actionHelper);
                controller.setErrorHandler(errorHandler);
                HomeAction homeAction = new HomeAction();
                homeAction.setActionHelper(actionHelper);
                homeAction.setErrorHandler(errorHandler);
                controller.setHomeAction(homeAction);

                Method controllerMethod = controller.getClass().getDeclaredMethod(
                        action, PortletRequest.class, PortletResponse.class);
                viewJSP = (String) controllerMethod.invoke(controller, request, response);
            }
            tx.commit();

        } catch (Exception e) {
            if (hibSession != null && hibSession.isOpen()) {
                hibSession.close();
            }
            handleEvalsException(e, action, Logger.ERROR, request);
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
        Boolean reload = false;
        Date loadDate = (Date) getPortletContext().getAttribute(CONTEXT_LOAD_DATE);
        if(loadDate == null){
            reload = true;
        }
        else if((new Date()).getTime() - loadDate.getTime() > Constants.PORTLET_RELOAD_FREQUENCY) {
            reload = true;
        }

        if (reload) {
            Session hibSession = null;
            String message = "";

            try {
                message += loadEnvironmentProperties();

                hibSession = HibernateUtil.getCurrentSession();
                Transaction tx = hibSession.beginTransaction();

                actionHelper = new ActionHelper(request, null, getPortletContext());

                createLogger();
                message += "Created logger object\n";
                createMailer();
                message += "Mailer setup successfully\n";
                getPortletContext().setAttribute("permissionRules", permissionRuleMgr.list());
                message += "Stored Permission Rules in portlet context\n";
                getPortletContext().setAttribute("appraisalSteps", appraisalStepMgr.list());
                message += "Stored Appraisal Steps in portlet context\n";
                loadResourceBundle();
                message += "Stored resource bundle Language.properties in portlet context\n";
                actionHelper.updateContextTimestamp();
                actionHelper.setAdminPortletData();
                tx.commit();

                EvalsLogger logger =  getLog();
                if (logger != null) {
                    logger.log(Logger.INFORMATIONAL, "Portlet Setup Success", message);
                }
                getPortletContext().setAttribute(CONTEXT_LOAD_DATE, new Date());
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
     * Requires the actionHelper private member to be instantiated.
     *
     * @throws Exception
     */
    private void createMailer() throws Exception {
        ResourceBundle resources = ResourceBundle.getBundle("edu.osu.cws.evals.portlet.Email");
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        String hostname = config.getString("mail.hostname");
        String from = config.getString("mail.fromAddress");
        String replyTo = config.getString("mail.replyToAddress");
        String linkUrl = config.getString("mail.linkUrl");
        String helpLinkUrl = config.getString("helpfulLinks.url");
        Map<String, Configuration> configurationMap = (Map<String, Configuration>)
                getPortletContext().getAttribute("configurations");
        Mailer mailer = new Mailer(resources, hostname, from, linkUrl,  helpLinkUrl,
                configurationMap, getLog(), replyTo);
        getPortletContext().setAttribute("mailer", mailer);
    }

    /**
     * Creates an instance of EvalsLogger, grabs the properties from the properties file and stores the
     * log instance in the portletContext. Requires actionHelper private member to be setup.
     */
    private void createLogger() {
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        String serverName = config.getString("log.serverName");
        String environment = config.getString("log.environment");
        EvalsLogger evalsLogger = new EvalsLogger(serverName, environment);
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
     * @throws Exception
     * @return message  Information logging to specify which files were loaded
     */
    private String loadEnvironmentProperties() throws Exception {
        String message = "";
        String infoMsg = "";

        // Load evals.properties
        PropertiesConfiguration config = EvalsUtil.loadEvalsConfig(getPortletContext());
        if (config != null) {
            infoMsg = " evals.properties - loaded";
        } else {
            infoMsg =  "evals.properties - not found";
        }
        message += infoMsg + "\n";

        // Set the Hibernate config file and store properties in portletContext
        String hibernateConfig = config.getString("hibernate-cfg-file");
        infoMsg = "using hibernate cfg file - " + hibernateConfig;
        message += infoMsg + "\n";
        HibernateUtil.setConfig(hibernateConfig);
        getPortletContext().setAttribute("environmentProp", config);

        return message;
    }

    /**
     * Takes care of handling portletSetup errors. This method is called by doView,
     * processAction and delegate's catch block.
     *
     * @param e Exception
     * @param shortMessage
     * @param level
     * @param request
     */
    private void handleEvalsException(Exception e, String shortMessage, String level, PortletRequest request) {
        try {
            String employee = "";
            Map<String, String> grayLogFields = new HashMap<String, String>();
            PortletSession session = request.getPortletSession(true);
            EvalsLogger logger = getLog();

            if (logger != null) {
                String currentURL = PortalUtil.getCurrentURL(request);
                Employee loggedOnUser = (Employee) session.getAttribute("loggedOnUser");
                String loggedOnUserId = "";
                if (loggedOnUser != null) {
                    loggedOnUserId = ((Integer) loggedOnUser.getId()).toString();
                }

                grayLogFields.put("logged-in-user", loggedOnUserId);
                grayLogFields.put("currentURL", currentURL);
                logger.log(level, shortMessage, e, grayLogFields);
            }
        } catch (Exception exception) {
            _log.error(CWSUtil.stackTraceString(e));
            _log.error(CWSUtil.stackTraceString(exception));
        }

        viewJSP = Constants.JSP_ERROR;
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
			        getPortletContext().getRequestDispatcher(Constants.JSP_HOME);
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
