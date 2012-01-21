package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.CloseOutReasonMgr;
import edu.osu.cws.evals.models.CloseOutReason;
import edu.osu.cws.evals.models.ModelException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.ArrayList;

public class CloseOutAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    /**
     * Handles listing the close out reasons.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        ArrayList<CloseOutReason> reasonsList = CloseOutReasonMgr.list(false);
        actionHelper.addToRequestMap("reasonsList", reasonsList);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_CLOSEOUT_REASON_LIST;
    }

    /**
     * Handles adding a close out reason. If successful, it displays the list of close out reasons.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        String reason = ParamUtil.getString(request, "reason");
        try {
            CloseOutReasonMgr.add(reason, actionHelper.getLoggedOnUser(request));
            SessionMessages.add(request, "closeout-reason-added");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return list(request, response);
    }

    /**
     * Handles performing a soft delete of a single close out reason.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String delete(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                CloseOutReason reason = CloseOutReasonMgr.get(id);
                actionHelper.addToRequestMap("reason", reason);
                return Constants.JSP_CLOSEOUT_REASON_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return list(request, response);
            }

            CloseOutReasonMgr.delete(id);
            SessionMessages.add(request, "closeout-reason-deleted");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        }
        return list(request, response);
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }
}
