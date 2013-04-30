package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.BusinessCenterMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.ReviewerMgr;
import edu.osu.cws.evals.models.*;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ReviewersAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    /**
     * Handles listing the reviewer users. It only performs error checking. The list of
     * reviewers is already set by EvalsPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        actionHelper.refreshContextCache();
        ArrayList<Reviewer> reviewersList = (ArrayList<Reviewer>) actionHelper.getPortletContextAttribute("reviewersList");
        ArrayList<BusinessCenter> businessCenters = (ArrayList<BusinessCenter>) BusinessCenterMgr.list();

        actionHelper.addToRequestMap("isMaster", actionHelper.isLoggedInUserMasterAdmin());
        actionHelper.addToRequestMap("reviewersList", reviewersList);
        actionHelper.addToRequestMap("businessCenters", businessCenters);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_REVIEWER_LIST;
    }

    /**
     * Handles adding a reviewer user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is an reviewer
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        boolean isAdmin = actionHelper.getAdmin() != null;
        boolean isReviewer = actionHelper.getReviewer() != null;

        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");

        // Check whether or not the user is already a reviewer user
        Employee onidUser = EmployeeMgr.findByOnid(onid, null);
        if (isReviewer) {
            actionHelper.addErrorsToRequest(resource.getString("already-reviewer"));
            return list(request, response);
        }

        try {
            ReviewerMgr.add(onid, businessCenterName);
            actionHelper.updateContextTimestamp();
            actionHelper.setAdminPortletData();
            SessionMessages.add(request, "reviewer-added");
        } catch (Exception e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        }

        return list(request, response);
    }

    /**
     * Handles deleting a reviewer user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String delete(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Reviewer reviewer = ReviewerMgr.get(id);
                if (reviewer.getEmployee() != null) {
                    reviewer.getEmployee().getName(); // initialize name due to lazy-loading
                }
                actionHelper.addToRequestMap("reviewer", reviewer);
                return Constants.JSP_REVIEWER_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return list(request, response);
            }

            ReviewerMgr.delete(id);
            actionHelper.updateContextTimestamp();
            actionHelper.setAdminPortletData();
            SessionMessages.add(request, "reviewer-deleted");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        }

        return list(request, response);
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
