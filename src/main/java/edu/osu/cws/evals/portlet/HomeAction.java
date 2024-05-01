package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class HomeAction implements ActionInterface {
    private ActionHelper actionHelper;

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
        actionHelper.addToRequestMap("homePageNotice", Notices.get("Homepage Notice"));
        String homeJSP = getHomeJSP();
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        boolean isAdmin = actionHelper.getAdmin() != null;
        boolean isReviewer = actionHelper.getReviewer() != null;

        // specify menu type, help links and yellow box to display in home view
        actionHelper.useNormalMenu();
        helpLinks();
        actionHelper.addToRequestMap("alertMsg", true);
        actionHelper.addToRequestMap("isHome", true);

        actionHelper.setupMyAppraisals();
        actionHelper.setupMyTeamActiveAppraisals();
        ArrayList<Appraisal> myAppraisals =
                (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myAppraisals");
        ArrayList<Appraisal> myTeamsActiveAppraisals  =
                (ArrayList<Appraisal>) actionHelper.getFromRequestMap("myTeamsActiveAppraisals");

        boolean hasAppraisals = (myAppraisals != null && !myAppraisals.isEmpty()) ||
                (myTeamsActiveAppraisals != null && !myTeamsActiveAppraisals.isEmpty());

        if (!isAdmin && !isReviewer && !hasAppraisals) {
            actionHelper.addToRequestMap("hasNoEvalsAccess", true);
        }

        actionHelper.addToRequestMap("initiateProfFaculty", displayInitiateEvaluationsButton());

        actionHelper.setRequiredActions();
        if (homeJSP.equals(Constants.JSP_HOME_REVIEWER)) {
            int maxResults = config.getInt("reviewer.home.pending.max");
            ArrayList<Appraisal> appraisals = actionHelper.getReviewsForLoggedInUser(maxResults);
            actionHelper.addToRequestMap("appraisals", appraisals);
        }
        return homeJSP;
    }

    /**
     * Whether or not EvalS should display a button in the home view of the user to let them initiate
     * the professional faculty and ranked faculty evaluations.
     *
     * It checks that the user is a supervisor of professional faculty or ranked_flag =1 and that at least one
     * of these unclassified employees needs an evaluation created. This is handled by calling the
     * JobMgr.isUnclassifiedSupervisor() method.
     *
     * @return
     * @throws Exception
     */
    private boolean displayInitiateEvaluationsButton() throws Exception {
        Map<String, Configuration> configMap = (Map<String, Configuration>) actionHelper.getPortletContextAttribute("configurations");
        if (!EvalsUtil.isProfessionalFacultyEnabled(configMap)) {
            return false;
        }

        List<String> appointmentTypes = new ArrayList<String>();
        appointmentTypes.add(AppointmentType.PROFESSIONAL_FACULTY);

        List<Job> supervisorJob = JobMgr.getSupervisorJobs(actionHelper.getLoggedOnUser());
        // if the person is not even a supervisor, don't display the button
        if (supervisorJob == null || supervisorJob.isEmpty()) {
            return false;
        }

        ArrayList<Job> employeeShortJobs = (ArrayList<Job>) JobMgr.listEmployeesShortJobs(supervisorJob, appointmentTypes);
        ArrayList<Job> jobsWithoutActiveEvaluations = JobMgr.getJobWithoutActiveEvaluations(employeeShortJobs);
        return JobMgr.isUnclassifiedSupervisor(actionHelper.getLoggedOnUser().getId()) &&
                jobsWithoutActiveEvaluations != null && !jobsWithoutActiveEvaluations.isEmpty();
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        PortletSession session = ActionHelper.getSession(request);
        actionHelper.useNormalMenu();
        Employee employee = actionHelper.getLoggedOnUser();
        if(!employee.getLoadJobs()){
            employee.setJobs(EmployeeMgr.findJobs(employee.getId()));
            employee.setLoadJobs(true);
        }
        actionHelper.addToRequestMap("employee", employee);
        session.setAttribute("loggedOnUser", employee);
        return Constants.JSP_MY_INFO;
    }

    /**
     * Set up the attribute in the request object that contains an array of helpful links
     *
     */
    private void helpLinks() {
        PropertiesConfiguration config = actionHelper.getEvalsConfig();
        actionHelper.addToRequestMap("helpLinks", config.getList("helpfulLinks"));
    }

    /**
     * Handles the user clicking on a link to reset the status of the open appraisal to set the status
     * to goals-due or results-due. If reset to goals-due, this function resets the entire appraisal
     * to a newly created state.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String demoResetAppraisal(PortletRequest request, PortletResponse response) throws Exception {
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        if (!actionHelper.isDemo()) {
            actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            return display(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        String status = ParamUtil.getString(request, "status");

        if (id == 0 || status == null || status.equals("")) {
            actionHelper.addErrorsToRequest(resource.getString("appraisal-cannot-reset"));
        }

        try {
            if(status.equals(Appraisal.STATUS_RESULTS_DUE)) {
                Appraisal appraisal = new Appraisal();
                appraisal.setId(id);
                appraisal.setStatus(status);
                appraisal.setOriginalStatus(status);

                AppraisalMgr.updateAppraisalStatus(appraisal);
            }
            else if (status.equals(Appraisal.STATUS_GOALS_DUE)) {
                Appraisal appraisal = AppraisalMgr.getAppraisal(id);
                AppraisalMgr.resetAppraisal(appraisal);
            }
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
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        if (!actionHelper.isDemo()) {
            actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            return display(request, response);
        }

        PortletSession session = ActionHelper.getSession(request);
        String employeeOnid = ParamUtil.getString(request, "employee.onid");
        Employee employee = new Employee();
        try {
            employee = EmployeeMgr.findByOnid(employeeOnid, null);
        } catch (Exception e) {
            _log.error("unexpected exception - " + CWSUtil.stackTraceString(e));
        }
        session.setAttribute("loggedOnUser", employee);
        actionHelper.setLoggedOnUser();
        session.removeAttribute(ActionHelper.ALL_MY_APPRAISALS);
        session.removeAttribute(ActionHelper.MY_TEAMS_ACTIVE_APPRAISALS);
        actionHelper.setUpUserPermission(true);

        return display(request, response);
    }


    /**
     * Returns the value of the home jsp file to render. It also performs
     * the code check to make sure that the logged in user is allowed to
     * view that file. If the user doesn't have access to that view, it
     * returns the default home-jsp.
     *
     * @return
     * @throws Exception
     */
    private String getHomeJSP() throws Exception {
        String homeJsp = Constants.JSP_HOME;
        String currentRole = actionHelper.getCurrentRole();
        ResourceBundle resource = (ResourceBundle) actionHelper.getPortletContextAttribute("resourceBundle");
        boolean isReviewer = actionHelper.getReviewer() != null;
        boolean isAdmin = actionHelper.getAdmin() != null;

        if (currentRole.equals(ActionHelper.ROLE_ADMINISTRATOR)) {
            if (!isAdmin) {
                actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            } else {
                homeJsp = Constants.JSP_HOME_ADMIN;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_REVIEWER)) {
            if (!isReviewer) {
                actionHelper.addErrorsToRequest(resource.getString("access-denied"));
            } else {
                homeJsp = Constants.JSP_HOME_REVIEWER;
            }
        } else if (currentRole.equals(ActionHelper.ROLE_SUPERVISOR)) {
            if (!actionHelper.isLoggedInUserSupervisor()) {
                actionHelper.addErrorsToRequest(resource.getString("access-denied"));
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

    public void setErrorHandler(ErrorHandler errorHandler) {
        // we do nothing in this method.
    }
}
