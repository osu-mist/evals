/**
 * Portlet class for PASS project, used to handle the processAction and doView
 * requests. It relies heavily on the Actions class to figure out which classes
 * to deleage the work to.
 */

package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.util.HibernateUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.portlet.*;

/**
 * <a href="JSPPortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class JSPPortlet extends GenericPortlet {

	protected String viewJSP;

    /**
     * Specifies whether or not we skip doView method. This is set to true, when the
     * processAction method has been called.
     */
    public boolean skipDoView = false;

    /**
     * Helper Liferay object to store error messages into the server's log file
     */
	private static Log _log = LogFactoryUtil.getLog(Actions.class);

    /**
     * The actions class
     */
    private Actions actionClass = new Actions();

    public void init() throws PortletException {
        //@todo: copy the line below to store the config paths to jsp files
        //editJSP = getInitParameter("edit-jsp");

		viewJSP = getInitParameter("home-jsp");
        HibernateUtil.setEnvironment(HibernateUtil.DEVELOPMENT);
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

        _log.error("doView called action = "+ ParamUtil.getString(renderRequest, "action"));

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
        Method actionMethod;
        String action;
        viewJSP = getInitParameter("home-jsp");

        // The portlet action can be set by the action/renderURLs using "action" as the parameter
        // name
        action =  ParamUtil.getString(request, "action");

        if (!action.equals("")) {
            try {
                actionMethod = Actions.class.getDeclaredMethod(action, PortletRequest.class,
                        PortletResponse.class, JSPPortlet.class);
                viewJSP = (String) actionMethod.invoke(actionClass, request, response, this);
                // The action methods return the init-param of the path, we then need to grab the value
                viewJSP = getInitParameter(viewJSP);
            } catch (NoSuchMethodException e) {
                StringWriter writerStr = new StringWriter();
                PrintWriter myPrinter = new PrintWriter(writerStr);
                e.printStackTrace(myPrinter);
                String stackTraceStr = writerStr.toString();
                _log.error("action method: " + action + " not found" + stackTraceStr);
            } catch (InvocationTargetException e) {
                StringWriter writerStr = new StringWriter();
                PrintWriter myPrinter = new PrintWriter(writerStr);
                e.printStackTrace(myPrinter);
                String stackTraceStr = writerStr.toString();
                _log.error("failed to call method: " + action + stackTraceStr);
            } catch (IllegalAccessException e) {
                StringWriter writerStr = new StringWriter();
                PrintWriter myPrinter = new PrintWriter(writerStr);
                e.printStackTrace(myPrinter);
                String stackTraceStr = writerStr.toString();
                _log.error("failed to call method: " + action + stackTraceStr);
            }
        }

        _log.debug("viewJSP in delegate: "+viewJSP);

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