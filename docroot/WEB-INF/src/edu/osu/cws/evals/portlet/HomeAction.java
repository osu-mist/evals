package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.configuration.CompositeConfiguration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class HomeAction implements ActionInterface {
    private ActionHelper actionHelper = new ActionHelper();
    private static Log _log = LogFactoryUtil.getLog(HomeAction.class);

    /**
     * Takes care of grabbing all the information needed to display the home view sections
     * (req. actionHelper, my appraisals, my team, reviews and admins) and sets the information
     * in the request object.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String display(PortletRequest request, PortletResponse response) throws Exception {
        Map Notices = (Map)actionHelper.getPortletContextAttribute("Notices");
        actionHelper.addToRequestMap("homePageNotice", Notices.get("Homepage Notice"), request);
        Employee employee = actionHelper.getLoggedOnUser(request);
        int employeeId = employee.getId();
        String homeJSP = getHomeJSP(request);
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        boolean isAdmin = actionHelper.isLoggedInUserAdmin(request);
        boolean isReviewer = actionHelper.isLoggedInUserReviewer(request);

        // specify menu type, help links and yellow box to display in home view
        actionHelper.useNormalMenu(request);
        helpLinks(request);
        actionHelper.addToRequestMap("alertMsg", true, request);
        actionHelper.addToRequestMap("isHome", true, request);

        actionHelper.setupMyActiveAppraisals(request, employeeId);
        actionHelper.setupMyTeamActiveAppraisals(request, employeeId);
        ArrayList<Appraisal> myActiveAppraisals = (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myActiveAppraisals",request);
        ArrayList<Appraisal> myTeamsActiveAppraisals  =
                (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myTeamsActiveAppraisals",request);

        boolean hasAppraisals = (myActiveAppraisals != null && !myActiveAppraisals.isEmpty()) ||
                (myTeamsActiveAppraisals != null && !myTeamsActiveAppraisals.isEmpty());

        if (!isAdmin && !isReviewer && !hasAppraisals) {
            actionHelper.addToRequestMap("hasNoEvalsAccess", true,request);
        }

        actionHelper.setRequiredActions(request);
        if (homeJSP.equals(Constants.JSP_HOME_REVIEWER)) {
            int maxResults = config.getInt("reviewer.home.pending.max");
            ArrayList<Appraisal> appraisals = actionHelper.getReviewsForLoggedInUser(request, maxResults);
            actionHelper.addToRequestMap("appraisals", appraisals,request);
        }
        return homeJSP;
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        PortletSession session = request.getPortletSession(true);
        actionHelper.useNormalMenu(request);
        Employee employee = actionHelper.getLoggedOnUser(request);
        if(!employee.getLoadJobs()){
            employee.setJobs(EmployeeMgr.findJobs(employee.getId()));
            employee.setLoadJobs(true);
        }
        actionHelper.addToRequestMap("employee", employee,request);
        session.setAttribute("loggedOnUser", employee);
        return Constants.JSP_MY_INFO;
    }

    /**
     * Set up the attribute in the request object that contains an array of helpful links
     *
     * @param request
     */
    private void helpLinks(PortletRequest request) {
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        actionHelper.addToRequestMap("helpLinks", config.getStringArray("helpfulLinks"),request);
    }

    /**
     * Handles the user clicking on a link to reset the status of the open appraisal to set the status
     * to goals-due or results-due.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String demoResetAppraisal(PortletRequest request, PortletResponse response) throws Exception {
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        if (!actionHelper.isDemo()) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        String status = ParamUtil.getString(request, "status");

        if (id == 0 || status == null || status.equals("")) {
            actionHelper.addErrorsToRequest(request, resource.getString("appraisal-cannot-reset"));
        }

        try {
            Appraisal appraisal = new Appraisal();
            appraisal.setId(id);
            appraisal.setStatus(status);
            appraisal.setOriginalStatus(status);

            AppraisalMgr.updateAppraisalStatus(appraisal);
        } catch (Exception e) {
            _log.error("unexpected exception - " + CWSUtil.stackTraceString(e));
        }
        AppraisalsAction appraisalsAction = new AppraisalsAction();
        appraisalsAction.setHomeAction(this);
        appraisalsAction.setActionHelper(actionHelper);

        return appraisalsAction.display(request, response);
    }

    /*
     * Handles switching the logged in user for demo purposes. This can be
     * deleted after the demo. It updates the loggedOnUser attribute in the
     * portletSession which is used by all the actions methods.
     *
     * @param request
     * @param response
     * @param portlet
     * @return
     */
    public String demoSwitchUser(PortletRequest request, PortletResponse response) throws Exception {
        if (!actionHelper.isDemo()) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        PortletSession session = request.getPortletSession(true);
        int employeeID = Integer.parseInt(ParamUtil.getString(request, "employee.id"));
        Employee employee = new Employee();
        try {
            employee = EmployeeMgr.findById(employeeID, "employee-with-jobs");
        } catch (Exception e) {
            _log.error("unexpected exception - " + CWSUtil.stackTraceString(e));
        }
        session.setAttribute("loggedOnUser", employee);
        session.removeAttribute(ActionHelper.ALL_MY_ACTIVE_APPRAISALS);
        session.removeAttribute(ActionHelper.MY_TEAMS_ACTIVE_APPRAISALS);
        actionHelper.setUpUserPermissionInSession(request, true);

        return display(request, response);
    }


    /**
     * Returns the value of the home jsp file to render. It also performs
     * the code check to make sure that the logged in user is allowed to
     * view that file. If the user doesn't have access to that view, it
     * returns the default home-jsp.
     *
     * @param request
     * @return
     * @throws Exception
     */
    private String getHomeJSP(PortletRequest request) throws Exception {
        String homeJsp = Constants.JSP_HOME;
        String currentRole = actionHelper.getCurrentRole(request);
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");

        if (currentRole.equals(ActionHelper.ROLE_ADMINISTRATOR)) {
            if (!actionHelper.isLoggedInUserAdmin(request)) {
                actionHelper.addErrorsToRequest(request, resource.getString("access-denied"));
            } else {
                homeJsp = Constants.JSP_HOME_ADMIN;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_REVIEWER)) {
            if (!actionHelper.isLoggedInUserReviewer(request)) {
                actionHelper.addErrorsToRequest(request, resource.getString("access-denied"));
            } else {
                homeJsp = Constants.JSP_HOME_REVIEWER;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_SUPERVISOR)) {
            if (!actionHelper.isLoggedInUserSupervisor(request)) {
                actionHelper.addErrorsToRequest(request, resource.getString("access-denied"));
            } else {
                homeJsp = Constants.JSP_HOME_SUPERVISOR;
            }
        }

        return homeJsp;
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        // we do nothing in this method.
    }
}
