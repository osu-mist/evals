package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.hibernate.*;
import edu.osu.cws.pass.models.*;

import javax.portlet.*;
import java.util.*;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    private static Log _log = LogFactoryUtil.getLog(Actions.class);

    private EmployeeMgr employeeMgr = new EmployeeMgr();

    private JobMgr jobMgr = new JobMgr();

    private AppointmentTypeMgr appointmentTypeMgr = new AppointmentTypeMgr();

    private AdminMgr adminMgr = new AdminMgr();

    private ReviewerMgr reviewerMgr = new ReviewerMgr();

    private PortletContext portletContext;

    private AppraisalMgr appraisalMgr = new AppraisalMgr();

    /**
     * Takes the request object and creates POJO objects. Then it calls the respective
     * Hibernate util classes passing the POJOs to handle the saving of data and
     * validation.
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String addCriteria(PortletRequest request, PortletResponse response) throws Exception {
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();
        Employee loggedOnUser = getLoggedOnUser(request);

        // Fetch list of appointment types to use in add form
        request.setAttribute("appointmentTypes", new AppointmentTypeMgr().list());

        // When the criterionAreaId == null means that the user clicks on the Add Criteria
        // link. Otherwise the form was submitted
        String criterionAreaId = ParamUtil.getString(request, "criterionAreaId");
        if (!criterionAreaId.equals("")) {
            String appointmentType = ParamUtil.getString(request, "appointmentTypeID");
            String name = ParamUtil.getString(request, "name");
            String description = ParamUtil.getString(request, "description");

            criterionArea.setName(name);
            criterionArea.setAppointmentType(appointmentType);
            criterionDetail.setDescription(description);

            try {
                if (criteriaMgrArea.add(criterionArea, criterionDetail, loggedOnUser)) {
                    SessionMessages.add(request, "criteria-saved");
                    return listCriteria(request, response);
                }
            } catch (ModelException e) {
                addErrorsToRequest(request, e.getMessage());
            }
        }

        request.setAttribute("criterionArea", criterionArea);
        request.setAttribute("criterionDetail", criterionDetail);

        return "criteria-add-jsp";
    }

    /**
     * Takes the request object, fetches POJO object using hibernate. Sets new fields
     * using setter methods on POJO. Calls hibernate method to save data back to db.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void editCritera(ActionRequest actionRequest, ActionResponse actionResponse) {
//        @todo: takes the request object, fetches the
    }

    /**
     * Takes the request object and passes the employeeType to the hibernate util class.
     * It returns an array of CriterionArea POJO.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String listCriteria(PortletRequest request, PortletResponse response) throws  Exception {
        String appointmentType = ParamUtil.getString(request, "appointmentType",
                CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);

        try {
            List<CriterionArea> criterionList = new CriteriaMgr().list(appointmentType);
            request.setAttribute("criteria", criterionList);
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }

        return "criteria-list-jsp";
    }

    /**
     * Takes the request object and uses the CriterionAreaID to call the hibernate util
     * and have it delete the CriterionArea.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void deleteCriteria(ActionRequest actionRequest, ActionResponse actionResponse) {

    }

    /**
     * This method uses the request object to get a string with the new order. It then calls
     * a method in the hibernate util class to update the sequence of criterion for the given
     * employeeType.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void updateCriteriaSequence(ActionRequest actionRequest, ActionResponse actionResponse) {

    }

    /**
     * Takes care of grabbing all the information needed to display the home view sections
     * (req. actions, my appraisals, my team, reviews and admins) and sets the information
     * in the request object.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String displayHomeView(PortletRequest request, PortletResponse response) throws Exception {
        Employee employee = getLoggedOnUser(request);
        int employeeId = employee.getId();

        ArrayList<HashMap> allMyActiveAppraisals = appraisalMgr.getAllMyActiveAppraisals(employeeId);
        request.setAttribute("myActiveAppraisals", allMyActiveAppraisals);
        if (jobMgr.isSupervisor(employeeId)) {
            List<HashMap> myTeamsActiveAppraisals = appraisalMgr.getMyTeamsActiveAppraisals(employeeId);
            request.setAttribute("myTeamsActiveAppraisals", myTeamsActiveAppraisals);
            request.setAttribute("isSupervisor", true);
        } else {
            request.setAttribute("isSupervisor", false);
        }

        request.setAttribute("reviewer", getReviewer(employeeId));
        request.setAttribute("admin", getAdmin(employeeId));

        request.setAttribute("requiredActions", getRequiredActions(request));


        return "home-jsp";
    }

    /**
     * Updates the admins List in the portletContext. This method is called by
     * PassPortlet.portletSetup and by Actions.addAdmin methods.
     *
     * @throws Exception
     */
    public void setPassAdmins() throws Exception {
        portletContext.setAttribute("admins", adminMgr.list());
    }

    /**
     * Updates the reviewers List in the portletContext. This method is called by
     * PassPortlet.portletSetup and by Actions.addReviewer methods.
     *
     * @throws Exception
     */
    public void setPassReviewers() throws Exception {
        portletContext.setAttribute("reviewers", reviewerMgr.list());
    }


    /**
     * Handles displaying a list of pending reviews for a given business center.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String displayReviewList(PortletRequest request, PortletResponse response) throws Exception {
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");
        ArrayList<HashMap> reviews = appraisalMgr.getReviews(businessCenterName);
        request.setAttribute("reviews", reviews);

        return "review-list-jsp";
    }

    /**
     * Handles displaying the appraisal when a user clicks on it. It loads the appraisal
     * object along with the respective permissionRule.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String displayAppraisal(PortletRequest request, PortletResponse response) throws Exception {
        int appraisalID = ParamUtil.getInteger(request, "id");
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        appraisalMgr.setLoggedInUser(currentlyLoggedOnUser);
        HashMap permissionRules = (HashMap) portletContext.getAttribute("permissionRules");
        appraisalMgr.setPermissionRules(permissionRules);
        Boolean showForm = false;

        Appraisal appraisal = appraisalMgr.getAppraisal(appraisalID);
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            SessionErrors.add(request, "appraisal-permission-denied");
            return "home-jsp";
        }

        // Set flag whether or not the html form to update the appraisal needs to be displayed
        if (permRule.getSaveDraft() != null && permRule.getSubmit() != null
                && permRule.getRequireModification() != null) {
            showForm = true;
        }

        request.setAttribute("appraisal", appraisal);
        request.setAttribute("permissionRule", permRule);
        request.setAttribute("showForm", showForm);

        return "appraisal-jsp";
    }

    /**
     * Handles updating the appraisal form.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String updateAppraisal(PortletRequest request, PortletResponse response) throws Exception {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            _log.error(entry.getKey() + "/" + entry.getValue()[0]);

        }

        int id = ParamUtil.getInteger(request, "id", 0);
        if (id == 0) {
            SessionErrors.add(request, "appraisal-does-not-exist");
            return "home-jsp";
        }

        HashMap permissionRules = (HashMap) portletContext.getAttribute("permissionRules");
        HashMap appraisalSteps = (HashMap) portletContext.getAttribute("appraisalSteps");
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);

        appraisalMgr.setLoggedInUser(currentlyLoggedOnUser);
        appraisalMgr.setPermissionRules(permissionRules);
        appraisalMgr.setAppraisalSteps(appraisalSteps);

        try {
            appraisalMgr.processUpdateRequest(request.getParameterMap(), id);
        } catch (ModelException e) {
            SessionErrors.add(request, e.getMessage());
        }

        return displayHomeView(request, response);
    }


    /**
     * Takes an string error message and sets in the session.
     *
     * @param request
     * @param errorMsg
     */
    private void addErrorsToRequest(PortletRequest request, String errorMsg) {
        request.setAttribute("errorMsg", errorMsg);
    }

    /**
     * Returns an Employee object of the currently logged on user. First it looks in
     * the PortletSession if it's not there it fetches the Employee object and stores
     * it there.
     *
     * @param request   PortletRequest
     * @return
     * @throws Exception
     */
    private Employee getLoggedOnUser(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);
        Employee loggedOnUser = (Employee) session.getAttribute("loggedOnUser");
        if (loggedOnUser == null) {
            String loggedOnUsername = getLoggedOnUsername(request);
            loggedOnUser = employeeMgr.findByOnid(loggedOnUsername);
            session.setAttribute("loggedOnUser", loggedOnUser);
        }

        return loggedOnUser;
    }

    /**
     * Returns a map with information on the currently logged on user.
     *
     * @param request
     * @return
     */
    private Map getLoggedOnUserMap(PortletRequest request) {
        return (Map)request.getAttribute(PortletRequest.USER_INFO);
    }

    /**
     * Returns the username of the currently logged on user. If there is no valid username, it
     * returns an empty string.
     *
     * @param request
     * @return username
     */
    private String getLoggedOnUsername(PortletRequest request) {
        Map userInfo = getLoggedOnUserMap(request);

        return (userInfo == null) ? "" : (String) userInfo.get("user.login.id");
    }

    /**
     * Sets the porletContext field. This method is called by the delegate and portletSetup
     * methods in PASSPortlet.
     *
     * @param portletContext
     */
    public void setPortletContext(PortletContext portletContext) {
        this.portletContext = portletContext;
    }

    /**
     * Takes in a pidm, and looks up in the reviewers HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer. If yes, then we return the
     * Reviewer object if not, it returns false.
     *
     * @param pidm  Pidm of currently logged in user
     * @return Reviewer
     */
    private Reviewer getReviewer(int pidm) {
        HashMap<Integer, Reviewer> reviewerMap =
                (HashMap<Integer, Reviewer>) portletContext.getAttribute("reviewers");

        if (reviewerMap.containsKey(pidm)) {
            return reviewerMap.get(pidm);
        }
        return null;
    }

    /**
     * Takes in a pidm, and looks up in the admins HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer. If yes, then we return the
     * Admin object if not, it returns false.
     *
     * @param pidm
     * @return Admin
     */
    private Admin getAdmin(int pidm) {
        HashMap<Integer, Admin> adminMap =
                (HashMap<Integer, Admin>) portletContext.getAttribute("admins");

        if (adminMap.containsKey(pidm)) {
            return adminMap.get(pidm);
        }
        return null;
    }

    /**
     * Using the request object, it fetches the list of employee appraisals and supervisor
     * appraisals and finds out if there are any actions required for them. It also checks
     * to see if the user is a reviewer and it gets the action required for the reviewer.
     *
     * @param request
     * @return ArrayList<RequiredAction>
     * @throws Exception
     */
    public ArrayList<RequiredAction> getRequiredActions(PortletRequest request) throws Exception {
        ArrayList<RequiredAction> requiredActions = new ArrayList<RequiredAction>();
        ArrayList<RequiredAction> employeeRequiredActions;
        ArrayList<RequiredAction> supervisorRequiredActions;
        ArrayList<HashMap> myActiveAppraisals;
        ArrayList<HashMap> supervisorActions;
        RequiredAction reviewerAction;
        Reviewer reviewer;
        Employee loggedInEmployee = getLoggedOnUser(request);
        int employeeID = loggedInEmployee.getId();
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");


        myActiveAppraisals = (ArrayList<HashMap>) request.getAttribute("myActiveAppraisals");
        employeeRequiredActions = getAppraisalActions(myActiveAppraisals, "employee", resource);
        requiredActions.addAll(employeeRequiredActions);

        // add supervisor required actions, if user has team's active appraisals
        if (request.getAttribute("myTeamsActiveAppraisals") != null) {
            supervisorActions = (ArrayList<HashMap>) request.getAttribute("myTeamsActiveAppraisals");
            supervisorRequiredActions = getAppraisalActions(supervisorActions, "supervisor", resource);
            requiredActions.addAll(supervisorRequiredActions);
        }

        reviewer = getReviewer(employeeID);
        if (reviewer != null) {
            String businessCenterName = reviewer.getBusinessCenterName();
            reviewerAction = getReviewerAction(businessCenterName, resource);
            if (reviewerAction != null) {
                requiredActions.add(reviewerAction);
            }
        }
        return requiredActions;
    }


    /**
     * Returns a list of actions required for the given user and role, based on the
     * list of appraisals passed in. If the user and role have no appraisal actions,
     * it returns an empty ArrayList.
     *
     * @param appraisalList     List of appraisals to check for actions required
     * @param role              Role of the currently logged in user
     * @param resource          Resource bundle to pass in to RequiredAction bean
     * @return  outList
     */
    public ArrayList<RequiredAction> getAppraisalActions(List<HashMap> appraisalList,
                                                         String role, ResourceBundle resource) {
        HashMap permissionRuleMap = (HashMap) portletContext.getAttribute("permissionRules");
        ArrayList<RequiredAction> outList = new ArrayList<RequiredAction>();
        String actionKey = "";
        RequiredAction actionReq;
        HashMap<String, String> anchorParams;

        for (HashMap<String, String> appraisalMap : appraisalList) {
            //get the status, compose the key "status"-"role"
            actionKey = appraisalMap.get("status")+"-"+role;

            // Get the appropriate permissionrule object from the permissionRuleMap
            PermissionRule rule = (PermissionRule) permissionRuleMap.get(actionKey);
            String actionRequired = rule.getActionRequired();
            if (rule != null && actionRequired != null
                    && !actionRequired.equals("")) {
                // compose a requiredAction object and add it to the outList.
                anchorParams = new HashMap<String, String>();
                anchorParams.put("action", "displayAppraisal");
                anchorParams.put("id", appraisalMap.get("id"));

                actionReq = new RequiredAction();
                actionReq.setParameters(anchorParams);
                actionReq.setAnchorText(actionRequired, appraisalMap, resource);
                outList.add(actionReq);
            }
        }
        return outList;
    }

    /**
     * Returns the required action for the business center reviewer.
     *
     * @param businessCenterName
     * @param resource
     * @return
     * @throws Exception
     */
    private RequiredAction getReviewerAction(String businessCenterName, ResourceBundle resource)
            throws Exception {
        int reviewCount = appraisalMgr.getReviewCount(businessCenterName);

        RequiredAction requiredAction = new RequiredAction();
        if (reviewCount == 0) {
            return null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "displayReviewList");
        parameters.put("businessCenterName", businessCenterName);
        requiredAction.setAnchorText("action-required-review", reviewCount, resource);
        requiredAction.setParameters(parameters);

        return requiredAction;
    }
}