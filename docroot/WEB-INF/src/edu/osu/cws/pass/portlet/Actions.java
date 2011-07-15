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

    private ConfigurationMgr configurationMgr = new ConfigurationMgr();

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
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to add criteria");
            return displayHomeView(request, response);
        }

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
        useMaximizedMenu(request);

        return "criteria-add-jsp";
    }

    /**
     * Handles editing of an evaluation criteria. Checks user permission. Then calls CriteriaMgr
     * to handle the editing.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String editCriteria(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to edit criteria");
            return displayHomeView(request, response);
        }

        CriteriaMgr criteriaMgr = new CriteriaMgr();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();
        try {
            int criterionAreaId = ParamUtil.getInteger(request, "criterionAreaId");
            if (request instanceof RenderRequest) {
                criterionArea = criteriaMgr.get(criterionAreaId);
                if (criterionArea != null) {
                    criterionDetail = criterionArea.getCurrentDetail();
                }
            } else {
                Employee loggedOnUser = getLoggedOnUser(request);
                criteriaMgr.edit(request.getParameterMap(), criterionAreaId, loggedOnUser);
                return listCriteria(request, response);
            }
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }

        request.setAttribute("criterionArea", criterionArea);
        request.setAttribute("criterionDetail", criterionDetail);
        useMaximizedMenu(request);

        return "criteria-add-jsp";
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
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to list criteria");
            return displayHomeView(request, response);
        }

        String appointmentType = ParamUtil.getString(request, "appointmentType",
                CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);

        try {
            List<CriterionArea> criterionList = new CriteriaMgr().list(appointmentType);
            request.setAttribute("criteria", criterionList);
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }

        useMaximizedMenu(request);
        return "criteria-list-jsp";
    }

    /**
     * Handles deleting an evaluation criteria. If the request a regular http request, it
     * displays a confirm page. Once the user confirms the deletion, the criteria is deleted,
     * the sequence is updated and the list of criteria is displayed again. If the request is
     * AJAX, we remove the evaluation criteria.
     *
     * @param request
     * @param response
     * @return String   If the request is ajax returns json, otherwise jsp file
     * @throws Exception
     */
    public String deleteCriteria(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to list criteria");
            return displayHomeView(request, response);
        }

        int criteriaID = ParamUtil.getInteger(request, "id");
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        try {
            Employee loggedOnUser = getLoggedOnUser(request);

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                CriterionArea criterion = criteriaMgrArea.get(criteriaID);
                request.setAttribute("criterion", criterion);
                return "criteria-delete-jsp";
            }

            // If user hits cancel, send them to list criteria page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listCriteria(request, response);
            }
            criteriaMgrArea.delete(criteriaID, loggedOnUser);
            SessionMessages.add(request, "criteria-deleted");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
            if (isAJAX(request, response)) {
                return e.getMessage();
            }
        }

        if (isAJAX(request, response)) {
            return "success";
        }

        return listCriteria(request, response);
    }

    /**
     * Specifies whether or not the request is an AJAX request by checking whether or not
     * request and response are instances of ResourceRequest and ResourceResponse.
     *
     * @param request
     * @param response
     * @return
     */
    private boolean isAJAX(PortletRequest request, PortletResponse response) {
        return request instanceof ResourceRequest && response instanceof ResourceResponse;
    }

    /**
     * This method is called via AJAX when the sequence of an evaluation criteria is updated.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String updateCriteriaSequence(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        int sequence = ParamUtil.getInteger(request, "sequence");
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();

        try {
            Employee loggedOnUser = getLoggedOnUser(request);
            criteriaMgrArea.updateSequence(id, sequence);
        } catch (ModelException e) {
            return e.getMessage();
        }

        return "success";
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

        setupActiveAppraisals(request, employeeId);

        setRequiredActions(request);
        useNormalMenu(request);

        return "home-jsp";
    }

    /**
     * Places in the request object the active appraisals of the user. This is used by the notification
     * piece.
     *
     * @param request
     * @param employeeId
     * @throws Exception
     */
    private void setupActiveAppraisals(PortletRequest request, int employeeId) throws Exception {
        ArrayList<Appraisal> allMyActiveAppraisals = appraisalMgr.getAllMyActiveAppraisals(employeeId);
        request.setAttribute("myActiveAppraisals", allMyActiveAppraisals);
        if (isLoggedInUserSupervisor(request)) {
            List<Appraisal> myTeamsActiveAppraisals = appraisalMgr.getMyTeamsAppraisals(employeeId, true);
            request.setAttribute("myTeamsActiveAppraisals", myTeamsActiveAppraisals);
        }
    }

    public String displayAdminHomeView(PortletRequest request, PortletResponse response) throws Exception {
        Employee employee = getLoggedOnUser(request);
        setupActiveAppraisals(request, employee.getId());
        setRequiredActions(request);
        useNormalMenu(request);

        return "admin-home-jsp";
    }

    public String displaySupervisorHomeView(PortletRequest request, PortletResponse response) throws Exception {
        Employee employee = getLoggedOnUser(request);
        setupActiveAppraisals(request, employee.getId());
        setRequiredActions(request);
        useNormalMenu(request);

        return "supervisor-home-jsp";
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        useNormalMenu(request);
        request.setAttribute("employee", getLoggedOnUser(request));

        return "my-information-jsp";
    }

    /**
     * Checks the user permission level and sets up some flags in the session object to store those
     * permissions.
     *
     * @param request
     * @param refresh   Update the user permissions, even if they have already been set
     * @throws Exception
     */
    public void setUpUserPermissionInSession(PortletRequest request, boolean refresh) throws Exception {
        PortletSession session = request.getPortletSession(true);
        Employee employee = getLoggedOnUser(request);
        int employeeId = employee.getId();

        Boolean isSupervisor = (Boolean) session.getAttribute("isSupervisor");
        if (refresh || isSupervisor == null) {
            isSupervisor = jobMgr.isSupervisor(employeeId);
            session.setAttribute("isSupervisor", isSupervisor);
        }
        request.setAttribute("isSupervisor", isSupervisor);

        Boolean isReviewer = (Boolean) session.getAttribute("isReviewer");
        if (refresh || isReviewer == null) {
            isReviewer = getReviewer(employeeId) != null;
            session.setAttribute("isReviewer", isReviewer);
        }
        request.setAttribute("isReviewer", isReviewer);

        Boolean isMasterAdmin = (Boolean) session.getAttribute("isSuperAdmin");
        if (refresh || isMasterAdmin == null) {
            if (getAdmin(employeeId) != null && getAdmin(employeeId).getIsMaster()) {
                isMasterAdmin = true;
            }
            session.setAttribute("isMasterAdmin", isMasterAdmin);
        }
        request.setAttribute("isMasterAdmin", isMasterAdmin);

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        _log.error("isAdmin = "+isAdmin);
        if (refresh || isAdmin == null) {
            _log.error("setting isAdmin again");
            isAdmin = getAdmin(employeeId) != null;
            session.setAttribute("isAdmin", isAdmin);
        }
        request.setAttribute("isAdmin", isAdmin);
        _log.error("isAdmin = "+isAdmin);

    }

    /**
     * Updates the admins List in the portletContext. This method is called by
     * PassPortlet.portletSetup and by Actions.addAdmin methods.
     *
     * @throws Exception
     */
    public void setPassAdmins() throws Exception {
        portletContext.setAttribute("admins", adminMgr.mapByEmployeeId());
        portletContext.setAttribute("adminsList", adminMgr.list());
    }

    /**
     * Updates the reviewers List in the portletContext. This method is called by
     * PassPortlet.portletSetup and by Actions.addReviewer methods.
     *
     * @throws Exception
     */
    public void setPassReviewers() throws Exception {
        portletContext.setAttribute("reviewers", reviewerMgr.mapByEmployeeId());
        portletContext.setAttribute("reviewersList", reviewerMgr.list());
    }

    /**
     * Updates the configuration List in the portletContext. This method is called by
     * PassPortlet.portletSetup and by Actions.editConfiguration methods.
     *
     * @throws Exception
     */
    public void setPassConfiguration() throws Exception {
        portletContext.setAttribute("configurations", configurationMgr.mapByName());
        portletContext.setAttribute("configurationsList", configurationMgr.list());
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
        // Check that the logged in user is admin
        if (!isLoggedInUserReviewer(request)) {
            addErrorsToRequest(request, "You do not have access to review performance evaluation");
            return displayHomeView(request, response);
        }

        ArrayList<Appraisal> reviews = getReviewsForLoggedInUser(request);
        request.setAttribute("reviews", reviews);
        useMaximizedMenu(request);

        return "review-list-jsp";
    }

    /**
     * Sets a request parameter to tell the jsp to use the normal top menu.
     *
     * @param request
     */
    private void useNormalMenu(PortletRequest request) {
        request.setAttribute("menuHome", true);
    }

    /**
     * Sets a request parameter to tell the jsp to use the maximized top menu.
     *
     * @param request
     */
    private void useMaximizedMenu(PortletRequest request) {
        request.setAttribute("menuMax", true);
    }

    private ArrayList<Appraisal> getReviewsForLoggedInUser(PortletRequest request) throws Exception {
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");
        if (businessCenterName.equals("")) {
            int employeeID = getLoggedOnUser(request).getId();
            businessCenterName = getReviewer(employeeID).getBusinessCenterName();
        }
        return appraisalMgr.getReviews(businessCenterName);
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
            addErrorsToRequest(request, "You do not have permission to view the appraisal");
            return displayHomeView(request, response);
        }
        String userRole = appraisalMgr.getRoleAndSession(appraisal, currentlyLoggedOnUser.getId());
        appraisalMgr.setAppraisalStatus(appraisal, userRole);

        // Set flag whether or not the html form to update the appraisal needs to be displayed
        if (permRule.getSaveDraft() != null && permRule.getSubmit() != null
                && permRule.getRequireModification() != null) {
            showForm = true;
        }

        if (isLoggedInUserSupervisor(request)) {
            int userID = currentlyLoggedOnUser.getId();
            List<Appraisal> myTeamsActiveAppraisals = appraisalMgr.getMyTeamsAppraisals(userID, true);
            request.setAttribute("myTeamsAppraisals", myTeamsActiveAppraisals);
        }

        if (isLoggedInUserReviewer(request)) {
            ArrayList<Appraisal> reviews = getReviewsForLoggedInUser(request);
            request.setAttribute("pendingReviews", reviews);
        }

        request.setAttribute("appraisal", appraisal);
        request.setAttribute("permissionRule", permRule);
        request.setAttribute("showForm", showForm);
        useMaximizedMenu(request);

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
            addErrorsToRequest(request, "We couldn't find your appraisal. If you believe this is an " +
                    "error, please contact your supervisor.");
            return displayHomeView(request, response);
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

        // If the user hit the save draft button, we stay in the same view
        if (request.getParameter("save-draft") != null) {
            SessionMessages.add(request, "draft-saved");
            if (response instanceof ActionResponse) {
                ((ActionResponse) response).setWindowState(WindowState.MAXIMIZED);
            }
            return displayAppraisal(request, response);
        }

        return displayHomeView(request, response);
    }

    /**
     * Handles listing the admin users. It only performs error checking. The list of
     * admins is already set by PASSPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     */
    public String listAdmin(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to list admin users");
            return displayHomeView(request, response);
        }

        ArrayList<Admin> adminsList = (ArrayList<Admin>) portletContext.getAttribute("adminsList");
        request.setAttribute("isMaster", isLoggedInUserMasterAdmin(request));
        request.setAttribute("adminsList", adminsList);
        useMaximizedMenu(request);

        return "admin-list-jsp";
    }

    /**
     * Handles deleting the admin user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String deleteAdmin(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserMasterAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        AdminMgr adminMgr = new AdminMgr();
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Admin admin = adminMgr.get(id);
                request.setAttribute("admin", admin);
                return "admin-delete-jsp";
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listAdmin(request, response);
            }

            adminMgr.delete(id);
            setPassAdmins();
            SessionMessages.add(request, "admin-deleted");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }

        return listAdmin(request, response);
    }

    /**
     * Handles adding an admin user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String addAdmin(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserMasterAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String isMaster = ParamUtil.getString(request, "isAdmin");

        try {
            adminMgr.add(onid,  isMaster, getLoggedOnUser(request));
            setPassAdmins();
            SessionMessages.add(request, "admin-added");
        } catch (Exception e) {
            addErrorsToRequest(request, e.getMessage());
        }

        return listAdmin(request, response);
    }

    /**
     * Handles listing the reviewer users. It only performs error checking. The list of
     * reviewers is already set by PASSPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     */
    public String listReviewer(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        ArrayList<Reviewer> reviewersList = (ArrayList<Reviewer>) portletContext.getAttribute("reviewersList");
        BusinessCenterMgr businessCenterMgr = new BusinessCenterMgr();
        ArrayList<BusinessCenter> businessCenters = (ArrayList<BusinessCenter>) businessCenterMgr.list();

        request.setAttribute("isMaster", isLoggedInUserMasterAdmin(request));
        request.setAttribute("reviewersList", reviewersList);
        request.setAttribute("businessCenters", businessCenters);
        useMaximizedMenu(request);

        return "reviewer-list-jsp";
    }

    /**
     * Handles deleting a reviewer user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String deleteReviewer(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserMasterAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        ReviewerMgr reviewerMgr = new ReviewerMgr();
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Reviewer reviewer = reviewerMgr.get(id);
                request.setAttribute("reviewer", reviewer);
                return "reviewer-delete-jsp";
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listReviewer(request, response);
            }

            reviewerMgr.delete(id);
            setPassReviewers();
            SessionMessages.add(request, "reviewer-deleted");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }

        return listReviewer(request, response);
    }

    /**
     * Handles adding an admin user.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String addReviewer(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserMasterAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");

        // Check whether or not the user is already an admin user
        Employee onidUser = employeeMgr.findByOnid(onid);
        if (getReviewer(onidUser.getId()) != null) {
            addErrorsToRequest(request, "This user is already a reviewer.");
            return listReviewer(request, response);
        }

        try {
            reviewerMgr.add(onid, businessCenterName);
            setPassReviewers();
            SessionMessages.add(request, "reviewer-added");
        } catch (Exception e) {
            addErrorsToRequest(request, e.getMessage());
        }

        return listReviewer(request, response);
    }


    /**
     * Handles listing the configuration parameters.
     *
     * @param request
     * @param response
     * @return
     */
    public String listConfiguration(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        ArrayList<Configuration> configurations = (ArrayList<Configuration>)
                portletContext.getAttribute("configurationsList");
        request.setAttribute("configurations", configurations);
        useMaximizedMenu(request);

        return "configuration-list-jsp";
    }

    /**
     * Handles updating a configuration parameter. This method is only called using ajax.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String editConfiguration(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, "You do not have access to perform this action");
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        String value = ParamUtil.getString(request, "value");

        if (id != 0) {
            try {
                configurationMgr.edit(id, value);
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        return "success";
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
     * Reviewer object if not, it returns null.
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
     * Returns true if the logged in user is admin, false otherwise.
     *
     * @param request
     * @return boolean
     * @throws Exception
     */
    private boolean isLoggedInUserAdmin(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);
        return (Boolean) session.getAttribute("isAdmin");
    }

    /**
     * Returns true if the logged in user is a master admin, false otherwise.
     *
     * @param request
     * @return boolean
     * @throws Exception
     */
    private boolean isLoggedInUserMasterAdmin(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);
        return (Boolean) session.getAttribute("isMasterAdmin");
    }

    /**
     * Returns true if the logged in user is reviewer, false otherwise.
     *
     * @param request
     * @return boolean
     * @throws Exception
     */
    private boolean isLoggedInUserReviewer(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);
        return (Boolean) session.getAttribute("isReviewer");
    }

    /**
     * Returns true if the logged in user is a supervisor, false otherwise.
     *
     * @param request
     * @return boolean
     * @throws Exception
     */
    private boolean isLoggedInUserSupervisor(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);
        return (Boolean) session.getAttribute("isSupervisor");
    }

    /**
     * Using the request object, it fetches the list of employee appraisals and supervisor
     * appraisals and finds out if there are any actions required for them. It also checks
     * to see if the user is a reviewer and it gets the action required for the reviewer.
     * It sets two attributes in the request object: employeeActions and administrativeActions.
     *
     * @param request
     * @return ArrayList<RequiredAction>
     * @throws Exception
     */
    private void setRequiredActions(PortletRequest request) throws Exception {
        ArrayList<RequiredAction> employeeRequiredActions;
        ArrayList<RequiredAction> administrativeActions = new ArrayList<RequiredAction>();
        ArrayList<Appraisal> myActiveAppraisals;
        ArrayList<Appraisal> supervisorActions;
        RequiredAction reviewerAction;
        Reviewer reviewer;
        Employee loggedInEmployee = getLoggedOnUser(request);
        int employeeID = loggedInEmployee.getId();
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");


        myActiveAppraisals = (ArrayList<Appraisal>) request.getAttribute("myActiveAppraisals");
        employeeRequiredActions = getAppraisalActions(myActiveAppraisals, "employee", resource);
        request.setAttribute("employeeActions", employeeRequiredActions);

        // add supervisor required actions, if user has team's active appraisals
        if (request.getAttribute("myTeamsActiveAppraisals") != null) {
            supervisorActions = (ArrayList<Appraisal>) request.getAttribute("myTeamsActiveAppraisals");
            administrativeActions = getAppraisalActions(supervisorActions, "supervisor", resource);
        }

        reviewer = getReviewer(employeeID);
        if (isLoggedInUserReviewer(request)) {
            String businessCenterName = reviewer.getBusinessCenterName();
            reviewerAction = getReviewerAction(businessCenterName, resource);
            if (reviewerAction != null) {
                administrativeActions.add(reviewerAction);
            }
        }
        request.setAttribute("administrativeActions", administrativeActions);
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
     * @throws edu.osu.cws.pass.models.ModelException
     */
    public ArrayList<RequiredAction> getAppraisalActions(List<Appraisal> appraisalList,
                                                         String role, ResourceBundle resource) throws ModelException {
        Configuration configuration;
        HashMap permissionRuleMap = (HashMap) portletContext.getAttribute("permissionRules");
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) portletContext.getAttribute("configurations");

        ArrayList<RequiredAction> outList = new ArrayList<RequiredAction>();
        String actionKey = "";
        RequiredAction actionReq;
        HashMap<String, String> anchorParams;

        for (Appraisal appraisal : appraisalList) {
            //get the status, compose the key "status"-"role"
            String appraisalStatus = appraisal.getStatus();
            actionKey = appraisalStatus +"-"+role;

            // Get the appropriate permissionrule object from the permissionRuleMap
            PermissionRule rule = (PermissionRule) permissionRuleMap.get(actionKey);
            String actionRequired = "";
            if (rule != null) {
                actionRequired = rule.getActionRequired();
            }
            if (!actionRequired.equals("")) {
                // compose a requiredAction object and add it to the outList.
                anchorParams = new HashMap<String, String>();
                anchorParams.put("action", "displayAppraisal");
                String appraisalID = Integer.toString(appraisal.getId());
                anchorParams.put("id", appraisalID);
                if (appraisalStatus.equals("goalsRequiredModification") || appraisalStatus.equals("goalsReactivated")) {
                    configuration = configurationMap.get("goalsDue");
                } else {
                    if (appraisalStatus.contains("Overdue")) {
                        appraisalStatus = appraisalStatus.replace("Overdue", "Due");
                    }
                    configuration = configurationMap.get(appraisalStatus);
                }
                if (configuration == null) {
                    throw new ModelException("Could not find configuration object for status - " + appraisalStatus);
                }

                actionReq = new RequiredAction();
                actionReq.setParameters(anchorParams);
                actionReq.setAnchorText(actionRequired, appraisal, resource, configuration);
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
        requiredAction.setAnchorText("action-required-review", reviewCount, resource);
        requiredAction.setParameters(parameters);

        return requiredAction;
    }
}
