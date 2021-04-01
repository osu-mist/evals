package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AdminMgr;
import edu.osu.cws.evals.models.Admin;
import edu.osu.cws.evals.models.ModelException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.ArrayList;

public class AdminsAction implements ActionInterface {
    
    private ActionHelper actionHelper;
    
    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    /**
     * Handles listing the admin users. It only performs error checking. The list of
     * admins is already set by EvalsPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        actionHelper.refreshContextCache();
        ArrayList<Admin> adminsList =
                (ArrayList<Admin>) actionHelper.getPortletContextAttribute("adminsList");
        actionHelper.addToRequestMap("isMaster", actionHelper.isLoggedInUserMasterAdmin());
        actionHelper.addToRequestMap("adminsList", adminsList);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_ADMIN_LIST;
    }
        
    /**
     * Handles adding an admin user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserMasterAdmin()) {
            return errorHandler.handleAccessDenied(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String isMaster = ParamUtil.getString(request, "isAdmin");

        try {
            AdminMgr.add(onid, isMaster, actionHelper.getLoggedOnUser());
            actionHelper.updateContextTimestamp();
            actionHelper.setAdminPortletData();
            SessionMessages.add(request, "admin-added");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return list(request, response);        
    }
    
    /**
     * Handles deleting the admin user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String delete(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserMasterAdmin()) {
            return errorHandler.handleAccessDenied(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");

        try {
            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Admin admin = AdminMgr.get(id);
                if (admin.getEmployee() != null) { // initialize name due to lazy-loading
                    admin.getEmployee().getName();
                }
                actionHelper.addToRequestMap("admin", admin);
                return Constants.JSP_ADMIN_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return list(request, response);
            }

            AdminMgr.delete(id);
            actionHelper.updateContextTimestamp();
            actionHelper.setAdminPortletData();
            SessionMessages.add(request, "admin-deleted");
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
