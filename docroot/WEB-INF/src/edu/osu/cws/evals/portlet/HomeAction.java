package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Appraisal;
import org.apache.commons.configuration.CompositeConfiguration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.ArrayList;

public class HomeAction implements ActionInterface {
    private ActionHelper actionHelper = new ActionHelper();

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
        int employeeId = actionHelper.getLoggedOnUser(request).getId();
        String homeJSP = getHomeJSP(request);
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        boolean isAdmin = actionHelper.isLoggedInUserAdmin(request);
        boolean isReviewer = actionHelper.isLoggedInUserReviewer(request);

        // specify menu type, help links and yellow box to display in home view
        actionHelper.useNormalMenu(request);
        helpLinks(request);
        actionHelper.addToRequestMap("alertMsg", config.getBoolean("alert.display"));
        actionHelper.addToRequestMap("isHome", true);

        actionHelper.setupMyActiveAppraisals(request, employeeId);
        actionHelper.setupMyTeamActiveAppraisals(request, employeeId);
        ArrayList<Appraisal> myActiveAppraisals = (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myActiveAppraisals");
        ArrayList<Appraisal> myTeamsActiveAppraisals  =
                (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myTeamsActiveAppraisals");

        boolean hasAppraisals = (myActiveAppraisals != null && !myActiveAppraisals.isEmpty()) ||
                (myTeamsActiveAppraisals != null && !myTeamsActiveAppraisals.isEmpty());

        if (!isAdmin && !isReviewer && !hasAppraisals) {
            actionHelper.addToRequestMap("hasNoEvalsAccess", true);
        }

        actionHelper.setRequiredActions(request);
        if (homeJSP.equals(Constants.JSP_HOME_REVIEWER)) {
            int maxResults = config.getInt("reviewer.home.pending.max");
            ArrayList<Appraisal> appraisals = actionHelper.getReviewsForLoggedInUser(request, maxResults);
            actionHelper.addToRequestMap("appraisals", appraisals);
        }
        return homeJSP;
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        actionHelper.useNormalMenu(request);
        actionHelper.addToRequestMap("employee", actionHelper.getLoggedOnUser(request));

        return Constants.JSP_MY_INFO;
    }

    /**
     * Set up the attribute in the request object that contains an array of helpful links
     *
     * @param request
     */
    private void helpLinks(PortletRequest request) {
        CompositeConfiguration config = (CompositeConfiguration) actionHelper.getPortletContextAttribute("environmentProp");
        actionHelper.addToRequestMap("helpLinks", config.getStringArray("helpfulLinks"));
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

        if (currentRole.equals(ActionHelper.ROLE_ADMINISTRATOR)) {
            if (!actionHelper.isLoggedInUserAdmin(request)) {
                actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            } else {
                homeJsp = Constants.JSP_HOME_ADMIN;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_REVIEWER)) {
            if (!actionHelper.isLoggedInUserReviewer(request)) {
                actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            } else {
                homeJsp = Constants.JSP_HOME_REVIEWER;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_SUPERVISOR)) {
            if (!actionHelper.isLoggedInUserSupervisor(request)) {
                actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
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
