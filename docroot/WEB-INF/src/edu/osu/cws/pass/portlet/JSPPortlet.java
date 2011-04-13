/**
 * Portlet class for PASS project, used to handle the processAction and doView
 * requests. It relies heavily on the Actions class to figure out which classes
 * to deleage the work to.
 */

package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import edu.osu.cws.pass.util.HibernateUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * <a href="JSPPortlet.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class JSPPortlet extends GenericPortlet {

    public void init() throws PortletException {
		editJSP = getInitParameter("edit-jsp");
		helpJSP = getInitParameter("help-jsp");
		viewJSP = JSP_DEFAULT_HOME;
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

	public void doEdit(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		if (renderRequest.getPreferences() == null) {
			super.doEdit(renderRequest, renderResponse);
		}
		else {
			include(editJSP, renderRequest, renderResponse);
		}
	}

	public void doHelp(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException, PortletException {

		include(helpJSP, renderRequest, renderResponse);
        HibernateUtil.getSessionFactory().close();
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

        viewJSP = JSP_DEFAULT_HOME;
        Method actionMethod;
        String action = renderRequest.getParameter("action");
        if (action != null) {
            try {
                actionMethod = Actions.class.getDeclaredMethod(action, RenderRequest.class, RenderResponse.class);
                viewJSP = (String) actionMethod.invoke(actionClass, renderRequest, renderResponse);
            } catch (NoSuchMethodException e) {
                StringWriter writerStr = new StringWriter();
                    PrintWriter myPrinter = new PrintWriter(writerStr);
                    e.printStackTrace(myPrinter);
                    String stackTraceStr = writerStr.toString();
                _log.error("action method: " + action + " not found" + stackTraceStr);
            } catch (InvocationTargetException e) {
                _log.error("failed to call method: " + action);
            } catch (IllegalAccessException e) {
                _log.error("failed to call method: " + action);
            }
        }

        _log.debug("viewJSP in doView: "+viewJSP);
        include(viewJSP, renderRequest, renderResponse);
	}

	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws IOException, PortletException {
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

	protected String editJSP;
	protected String helpJSP;
	protected String viewJSP;

    private static final String JSP_DEFAULT_HOME = "/jsp/home/start.jsp";

	private static Log _log = LogFactoryUtil.getLog(JSPPortlet.class);
    private Actions actionClass = new Actions();

}