/**
 * Portlet class for PASS project, used to handle the processAction and doView
 * requests. It relies heavily on the Actions class to figure out which classes
 * to deleage the work to.
 */

package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.util.*;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.portlet.*;

/**
 * <a href="PASSPortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class PASSPortlet extends GenericPortlet {

    /**
     * String used to store the view jsp used by the
     * doView method.
     */
	protected String viewJSP;

    /**
     * Name of default properties file that is loaded the first time the
     * portlet is loaded.
     */
    private static final String defaultProperties = "default.properties";

    private PermissionRules permissionRules = new PermissionRules();
    private AppraisalSteps appraisalSteps = new AppraisalSteps();
    private Admins admins = new Admins();
    private Reviewers reviewers = new Reviewers();

    /**
     * Specifies whether or not we skip doView method. This is set to true, when the
     * processAction method has been called.
     */
    public boolean skipDoView = false;

    /**
     * Helper Liferay object to store error messages into the server's log file
     */
	private static Log _log = LogFactoryUtil.getLog(PASSPortlet.class);

    /**
     * The actions class
     */
    private Actions actionClass = new Actions();

    public void init() throws PortletException {
		viewJSP = getInitParameter("home-jsp");
	}

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

        portletSetup(renderRequest);
        _log.error("doView called action = " + ParamUtil.getString(renderRequest, "action"));


        // The skip-delegate parameter is set by processAction. This allow us to set the jsp
        // path and all the logic/data we need in processAction without being overwritten by
        // doView.
        if (!skipDoView) {
            delegate(renderRequest, renderResponse);
        }
        skipDoView = false;

        include(viewJSP, renderRequest, renderResponse);
	}

	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {
        portletSetup(actionRequest);

        _log.error("processAction called action = " + ParamUtil.getString(actionRequest, "action"));
        // We set the skipDoView property, to tell the doView that the processing is done
        // by the processAction method.
        skipDoView = true;
        delegate(actionRequest, actionResponse);
	}


    /**
     * Delegate method will call the respective method in the Actions class and pass the request
     * and response objects. The method called in Actions is based on the "action" parameter value.
     * The delegated methods in Actions class return the path to the jsp file that should be loaded.
     * Those methods also set any parameters needed by the jsp files.
     *
     * @param request
     * @param response
     */
    public void delegate(PortletRequest request, PortletResponse response) {
        actionClass.setPortletContext(getPortletContext());
        Method actionMethod;
        String action;
        viewJSP = getInitParameter("home-jsp");

        // The portlet action can be set by the action/renderURLs using "action" as the parameter
        // name
        action =  ParamUtil.getString(request, "action", "displayHomeView");

        if (!action.equals("")) {
            try {
                actionMethod = Actions.class.getDeclaredMethod(action, PortletRequest.class,
                        PortletResponse.class, PASSPortlet.class);
                viewJSP = (String) actionMethod.invoke(actionClass, request, response, this);
                // The action methods return the init-param of the path, we then need to grab the value
                viewJSP = getInitParameter(viewJSP);
            } catch (NoSuchMethodException e) {
                _log.error("action method: " + action + " not found" + stackTraceString(e));
            } catch (InvocationTargetException e) {
                _log.error("failed to call method: " + action + stackTraceString(e));
            } catch (IllegalAccessException e) {
                _log.error("failed to call method: " + action + stackTraceString(e));
            }
        }

        _log.debug("viewJSP in delegate: "+viewJSP);
    }

    /**
     * Takes care of loading the resource bundle Language.properties into the
     * portletContext.
     */
    private void loadResourceBundle() {
        ResourceBundle resources;
        try {
            resources = ResourceBundle.getBundle("edu.osu.cws.pass.portlet.Language");
            getPortletContext().setAttribute("resourceBundle", resources);
        } catch (MissingResourceException e) {
            _log.error("failed to load resource bundle" + stackTraceString(e));
        }
    }

    /**
     * Simple method that takes in an exception and returns the stacktrace as a string. We
     * need this so that if we get an exception, we can send it to the luminis log.
     *
     * @param e     Exception
     * @return  String stack trace
     */
    public static String stackTraceString(Exception e) {
        StringWriter writerStr = new StringWriter();
        PrintWriter myPrinter = new PrintWriter(writerStr);
        e.printStackTrace(myPrinter);
        return writerStr.toString();
    }

    /**
     * Takes care of initializing portlet variables and storing them in the portletContext.
     * Some of these variables are: permissionRules, appraisalSteps, reviewers, admins and
     * environment properties. This method is called everytime a doView, processAction or
     * serveResource are called, but the code inside only executes the first time.
     *
     * @param request
     */
    private void portletSetup(PortletRequest request) {
        try {
            if (getPortletContext().getAttribute("environmentProp") == null) {
                loadEnvironmentProperties(request);
                getPortletContext().setAttribute("permissionRules", permissionRules.list());
                getPortletContext().setAttribute("appraisalSteps", appraisalSteps.list());
                getPortletContext().setAttribute("reviewers", reviewers.list());
                getPortletContext().setAttribute("admins", admins.list());
                loadResourceBundle();
            }
        } catch (Exception e) {
            _log.error("failed run portletSetup");
        }
    }

    /**
     * Loads default.properties and then overrides properties by trying to load
     * properties file: hostname.properties. The config object is then stored
     * in the portletContext.
     *
     * @param request
     */
    private void loadEnvironmentProperties(PortletRequest request) {
        String propertyFile = request.getServerName() +".properties";
        CompositeConfiguration config = new CompositeConfiguration();

        // First load hostname.properties. Then try to load default.properties
        try {
            config.addConfiguration(new PropertiesConfiguration(propertyFile));
            _log.error(propertyFile + " - loaded");
        } catch (ConfigurationException e) {
            _log.error("Failed to load server specific properties file - " + propertyFile);
        } finally {
            try {
                config.addConfiguration(new PropertiesConfiguration(defaultProperties));
                _log.error(defaultProperties + " - loaded");

                // Set the Hibernate config file and store properties in portletContext
                _log.error("using hibernate cfg file - "+config.getString("hibernate-cfg-file"));
                HibernateUtil.setConfig(config.getString("hibernate-cfg-file"));
                getPortletContext().setAttribute("environmentProp", config);
            } catch (ConfigurationException e) {
                _log.error("failed to load default properties file - " + defaultProperties);
            }
        }
    }

    protected void include(
			String path, RenderRequest renderRequest,
			RenderResponse renderResponse)
		throws IOException, PortletException {

		PortletRequestDispatcher portletRequestDispatcher =
			getPortletContext().getRequestDispatcher(path);

		if (portletRequestDispatcher == null) {
			_log.error(path + " is not a valid include");
		}
		else {
			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}
}