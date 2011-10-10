package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsPDF;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.evals.util.Mailer;
import org.apache.commons.configuration.CompositeConfiguration;
import org.hibernate.Session;

import javax.portlet.*;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    public static final String ROLE_ADMINISTRATOR = "administrator";
    public static final String ROLE_REVIEWER = "reviewer";
    public static final String ROLE_SUPERVISOR = "supervisor";
    public static final String ROLE_SELF = "self";
    public static final String ALL_MY_ACTIVE_APPRAISALS = "allMyActiveAppraisals";
    public static final String MY_TEAMS_ACTIVE_APPRAISALS = "myTeamsActiveAppraisals";

    private EmployeeMgr employeeMgr = new EmployeeMgr();

    private JobMgr jobMgr = new JobMgr();

    private AppointmentTypeMgr appointmentTypeMgr = new AppointmentTypeMgr();

    private AdminMgr adminMgr = new AdminMgr();

    private ReviewerMgr reviewerMgr = new ReviewerMgr();

    private ConfigurationMgr configurationMgr = new ConfigurationMgr();

    private PortletContext portletContext;

    private AppraisalMgr appraisalMgr = new AppraisalMgr();

    private static final String ACCESS_DENIED = "You do not have access to perform this action";

    private HashMap<String, Object> requestMap = new HashMap<String, Object>();

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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();
        Employee loggedOnUser = getLoggedOnUser(request);

        // Fetch list of appointment types to use in add form
        requestMap.put("appointmentTypes", new AppointmentTypeMgr().list());

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

        requestMap.put("criterionArea", criterionArea);
        requestMap.put("criterionDetail", criterionDetail);
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
            addErrorsToRequest(request, ACCESS_DENIED);
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

        requestMap.put("criterionArea", criterionArea);
        requestMap.put("criterionDetail", criterionDetail);
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String appointmentType = ParamUtil.getString(request, "appointmentType",
                CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);

        try {
            List<CriterionArea> criterionList = new CriteriaMgr().list(appointmentType);
            for (CriterionArea criteria : criterionList) {
                criteria.getCurrentDetail().toString();
            }
            requestMap.put("criteria", criterionList);
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int criteriaID = ParamUtil.getInteger(request, "id");
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        try {
            Employee loggedOnUser = getLoggedOnUser(request);

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                CriterionArea criterion = criteriaMgrArea.get(criteriaID);
                requestMap.put("criterion", criterion);
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
            addErrorsToRequest(request, ACCESS_DENIED);
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
    //@@todo: Joan: I don't see the need to have several displayHomeView methods.  Let's talk about this.
    public String displayHomeView(PortletRequest request, PortletResponse response) throws Exception {
        PortletSession session = request.getPortletSession(true);
        Employee employee = getLoggedOnUser(request);
        int employeeId = employee.getId();
        setupMyActiveAppraisals(request, employeeId);
        setupMyTeamActiveAppraisals(request, employeeId);

        boolean isAdmin = isLoggedInUserAdmin(request);
        boolean isReviewer = isLoggedInUserReviewer(request);
        ArrayList<Appraisal> myActiveAppraisals = (ArrayList<Appraisal>) requestMap.get("myActiveAppraisals");
        ArrayList<Appraisal> myTeamsActiveAppraisals  =
                (ArrayList<Appraisal>) requestMap.get("myTeamsActiveAppraisals");
        boolean hasAppraisals = (myActiveAppraisals != null && !myActiveAppraisals.isEmpty()) ||
                (myTeamsActiveAppraisals != null && !myTeamsActiveAppraisals.isEmpty());

        if (!isAdmin && !isReviewer && !hasAppraisals) {
            requestMap.put("hasNoEvalsAccess", true);
        }

        helpLinks(request);
        requestMap.put("isHome", true);

        setRequiredActions(request);
        useNormalMenu(request);
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        requestMap.put("alertMsg", config.getBoolean("alert.display"));
        session.setAttribute("currentRole", ROLE_SELF);

        return "home-jsp";
    }

    /**
     * Set up the attribute in the request object that contains an array of helpful links
     *
     * @param request
     */
    private void helpLinks(PortletRequest request) {
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        requestMap.put("helpLinks", config.getStringArray("helpfulLinks"));
    }

    /**
     * Places in the request object the active appraisals of the user. This is used by the notification
     * piece.
     *
     * @param request
     * @param employeeId    Id/Pidm of the currently logged in user
     * @throws Exception
     */
    private void setupMyActiveAppraisals(PortletRequest request, int employeeId) throws Exception {
        PortletSession session = request.getPortletSession(true);
        List<Appraisal> allMyActiveAppraisals;

        allMyActiveAppraisals = (ArrayList<Appraisal>) session.getAttribute(ALL_MY_ACTIVE_APPRAISALS);
        if (allMyActiveAppraisals == null) {
            allMyActiveAppraisals = appraisalMgr.getAllMyActiveAppraisals(employeeId);
            session.setAttribute(ALL_MY_ACTIVE_APPRAISALS, allMyActiveAppraisals);
        }
        requestMap.put("myActiveAppraisals", allMyActiveAppraisals);
    }

    /**
     * Fetches the supervisor's team active appraisal and stores the list in session. Then it places the list
     * in the requestMap so that the view can access it.
     *
     * @param request
     * @param employeeId    Id/Pidm of the currently logged in user
     * @throws Exception
     */
    private void setupMyTeamActiveAppraisals(PortletRequest request, int employeeId) throws Exception {
        PortletSession session = request.getPortletSession(true);
        List<Appraisal> myTeamsActiveAppraisals;

        if (isLoggedInUserSupervisor(request)) {
            myTeamsActiveAppraisals = (ArrayList<Appraisal>)  session.getAttribute(MY_TEAMS_ACTIVE_APPRAISALS);
            if (myTeamsActiveAppraisals == null) {
                myTeamsActiveAppraisals = appraisalMgr.getMyTeamsAppraisals(employeeId, true);
                session.setAttribute(MY_TEAMS_ACTIVE_APPRAISALS, myTeamsActiveAppraisals);
            }
            requestMap.put(MY_TEAMS_ACTIVE_APPRAISALS, myTeamsActiveAppraisals);
        }
    }

    public String displayAdminHomeView(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        PortletSession session = request.getPortletSession(true);
        Employee employee = getLoggedOnUser(request);
        setupMyActiveAppraisals(request, employee.getId());
        setupMyTeamActiveAppraisals(request, employee.getId());
        setRequiredActions(request);
        useNormalMenu(request);
        helpLinks(request);
        requestMap.put("isAdminHome", true);
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        requestMap.put("alertMsg", config.getBoolean("alert.display"));
        session.setAttribute("currentRole", ROLE_ADMINISTRATOR);

        return "admin-home-jsp";
    }

    public String displayReviewerHomeView(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserReviewer(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        PortletSession session = request.getPortletSession(true);
        Employee employee = getLoggedOnUser(request);
        setupMyActiveAppraisals(request, employee.getId());
        setupMyTeamActiveAppraisals(request, employee.getId());
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        int maxResults = config.getInt("reviewer.home.pending.max");

        ArrayList<Appraisal> appraisals = getReviewsForLoggedInUser(request, maxResults);
        requestMap.put("appraisals", appraisals);

        setRequiredActions(request);
        useNormalMenu(request);
        helpLinks(request);
        requestMap.put("isReviewerHome", true);
        requestMap.put("alertMsg", config.getBoolean("alert.display"));
        session.setAttribute("currentRole", ROLE_REVIEWER);

        return "reviewer-home-jsp";
    }

    public String displaySupervisorHomeView(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserSupervisor(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        PortletSession session = request.getPortletSession(true);
        Employee employee = getLoggedOnUser(request);
        setupMyActiveAppraisals(request, employee.getId());
        setupMyTeamActiveAppraisals(request, employee.getId());
        setRequiredActions(request);
        setTeamAppraisalStatus(request);
        useNormalMenu(request);
        helpLinks(request);
        requestMap.put("isSupervisorHome", true);
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        requestMap.put("alertMsg", config.getBoolean("alert.display"));
        session.setAttribute("currentRole", ROLE_SUPERVISOR);

        return "supervisor-home-jsp";
    }

    /**
     * Goes through the team appraisals in the request object, and sets their status accordingly by calling
     * AppraisalMgr.setAppraisalStatus.
     *
     * @param request
     */
    private void setTeamAppraisalStatus(PortletRequest request) {
        if (requestMap.get("myTeamsActiveAppraisals") != null) {
            ArrayList<Appraisal> newTeamAppraisals = new ArrayList<Appraisal>();
            ArrayList<Appraisal> teamAppraisals = (ArrayList<Appraisal>)
                    requestMap.get("myTeamsActiveAppraisals");
            for (Appraisal appraisal : teamAppraisals) {
                appraisal.setRole("supervisor");
                newTeamAppraisals.add(appraisal);
            }

            requestMap.put("myTeamsActiveAppraisals", newTeamAppraisals);
        }
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        useNormalMenu(request);
        requestMap.put("employee", getLoggedOnUser(request));

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
        requestMap.put("isSupervisor", isSupervisor);

        Boolean isReviewer = (Boolean) session.getAttribute("isReviewer");
        if (refresh || isReviewer == null) {
            isReviewer = getReviewer(employeeId) != null;
            session.setAttribute("isReviewer", isReviewer);
        }
        requestMap.put("isReviewer", isReviewer);

        Boolean isMasterAdmin = (Boolean) session.getAttribute("isSuperAdmin");
        if (refresh || isMasterAdmin == null) {
            if (getAdmin(employeeId) != null && getAdmin(employeeId).getIsMaster()) {
                isMasterAdmin = true;
            } else {
                isMasterAdmin = false;
            }
            session.setAttribute("isMasterAdmin", isMasterAdmin);
        }
        requestMap.put("isMasterAdmin", isMasterAdmin);

        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (refresh || isAdmin == null) {
            isAdmin = getAdmin(employeeId) != null;
            session.setAttribute("isAdmin", isAdmin);
        }
        requestMap.put("isAdmin", isAdmin);

        requestMap.put("employee", getLoggedOnUser(request));
    }

    /**
     * Updates the admins List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by Actions.addAdmin methods.
     *
     * @throws Exception
     */
    public void setEvalsAdmins() throws Exception {
        portletContext.setAttribute("admins", adminMgr.mapByEmployeeId());
        List<Admin> admins = adminMgr.list();
        // Call getName on the admins object to initialize the employee name
        for (Admin admin : admins) {
            if (admin.getEmployee() != null) {
                admin.getEmployee().getName();
            }
        }
        portletContext.setAttribute("adminsList", admins);
    }

    /**
     * Updates the reviewers List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by Actions.addReviewer methods.
     *
     * @throws Exception
     */
    public void setEvalsReviewers() throws Exception {
        portletContext.setAttribute("reviewers", reviewerMgr.mapByEmployeeId());
        List<Reviewer> reviewers = reviewerMgr.list();
        // Call getName on the reviewers object to initialize the employee name
        for (Reviewer reviewer : reviewers) {
            if (reviewer.getEmployee() != null) {
                reviewer.getEmployee().getName();
            }
        }
        portletContext.setAttribute("reviewersList", reviewers);
    }

    /**
     * Updates the configuration List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by Actions.editConfiguration methods.
     *
     * @throws Exception
     */
    public void setEvalsConfiguration() throws Exception {
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        ArrayList<Appraisal> appraisals = getReviewsForLoggedInUser(request, -1);
        requestMap.put("appraisals", appraisals);
        requestMap.put("pageTitle", "pending-reviews");
        useMaximizedMenu(request);

        return "review-list-jsp";
    }

    /**
     * Sets a request parameter to tell the jsp to use the normal top menu.
     *
     * @param request
     */
    private void useNormalMenu(PortletRequest request) {
        requestMap.put("menuHome", true);
    }

    /**
     * Sets a request parameter to tell the jsp to use the maximized top menu.
     *
     * @param request
     */
    private void useMaximizedMenu(PortletRequest request) {
        requestMap.put("menuMax", true);
    }

    /**
     * Retrieves the pending reviews for the logged in user.
     *
     * @param request
     * @param maxResults
     * @return
     * @throws Exception
     */
    private ArrayList<Appraisal> getReviewsForLoggedInUser(PortletRequest request, int maxResults) throws Exception {
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");
        if (businessCenterName.equals("")) {
            int employeeID = getLoggedOnUser(request).getId();
            businessCenterName = getReviewer(employeeID).getBusinessCenterName();
        }
        return appraisalMgr.getReviews(businessCenterName, maxResults);
    }

    /**
     * Renders a list of appraisals based on the search criteria.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String searchAppraisals(PortletRequest request, PortletResponse response) throws Exception {
        List<Appraisal> appraisals = new ArrayList<Appraisal>();
        requestMap.put("pageTitle", "search-results");
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");

        boolean isAdmin = isLoggedInUserAdmin(request);
        boolean isReviewer = isLoggedInUserReviewer(request);
        boolean isSupervisor = isLoggedInUserSupervisor(request);

        if (!isAdmin && !isReviewer && !isSupervisor)  {
            addErrorsToRequest(request, ACCESS_DENIED);
            ((ActionResponse) response).setWindowState(WindowState.NORMAL);
            return displayHomeView(request, response);
        }

        int pidm = getLoggedOnUser(request).getId();
        int osuid = ParamUtil.getInteger(request, "osuid");
        if (osuid == 0) {
            addErrorsToRequest(request, "Please enter an employee's OSU ID");
        } else {
            String bcName = "";
            if (isReviewer) {
                bcName = getReviewer(pidm).getBusinessCenterName();
            }
            appraisals = appraisalMgr.search(osuid, pidm, isAdmin, isSupervisor, bcName);

            if (appraisals.isEmpty()) {
                if (isAdmin) {
                    addErrorsToRequest(request, resource.getString("appraisal-search-no-results-admin"));
                } else if (isReviewer) {
                    addErrorsToRequest(request, resource.getString("appraisal-search-no-results-reviewer"));
                } else {
                    addErrorsToRequest(request, resource.getString("appraisal-search-no-results-supervisor"));
                }
            }
        }

        requestMap.put("appraisals", appraisals);
        useMaximizedMenu(request);

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
        Boolean showForm = false;
        Appraisal appraisal;
        PermissionRule permRule;
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        int userId = currentlyLoggedOnUser.getId();

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        setAppraisalMgrParameters(currentlyLoggedOnUser);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);

        //@@todo: Joan: This calls appraisalMgr.getRoleAndSession, which is called again a few lines later.
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String userRole = appraisalMgr.getRoleAndSession(appraisal, userId);
        appraisal.setRole(userRole);
        // Set flag whether or not the html form to update the appraisal needs to be displayed
        if (permRule.getSaveDraft() != null && permRule.getSubmit() != null
                && permRule.getRequireModification() != null) {
            showForm = true;
        }

        setupMyTeamActiveAppraisals(request, userId);
        if (isLoggedInUserReviewer(request)) {
            ArrayList<Appraisal> reviews = getReviewsForLoggedInUser(request, -1);
            requestMap.put("pendingReviews", reviews);
        }

        if (isLoggedInUserReviewer(request) && appraisal.getEmployeeSignedDate() != null) {
            requestMap.put("displayResendNolij", true);
        }

        // Initialze lazy appraisal associations
        appraisal.getJob().toString();
        appraisal.getJob().getCurrentSupervisor();
        appraisal.getJob().getEmployee().toString();
        appraisal.getSortedAssessments().size();
        for (Assessment assessment : appraisal.getAssessments()) {
            assessment.getCriterionDetail().getAreaID();
        }
        // End of initialize lazy appraisal associations

        requestMap.put("appraisal", appraisal);
        requestMap.put("permissionRule", permRule);
        requestMap.put("showForm", showForm);
        useMaximizedMenu(request);

        return "appraisal-jsp";
    }

    /**
     * Setups up parameters from portletContext needed by AppraisalMgr class.
     *
     * @param currentlyLoggedOnUser
     */
    private void setAppraisalMgrParameters(Employee currentlyLoggedOnUser) {
        HashMap permissionRules = (HashMap) portletContext.getAttribute("permissionRules");
        HashMap<Integer, Admin> admins = (HashMap<Integer, Admin>) portletContext.getAttribute("admins");
        HashMap<Integer, Reviewer> reviewers = (HashMap<Integer, Reviewer>) portletContext.getAttribute("reviewers");
        HashMap appraisalSteps = (HashMap) portletContext.getAttribute("appraisalSteps");
        Mailer mailer = (Mailer) portletContext.getAttribute("mailer");
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) portletContext.getAttribute("configurations");

        appraisalMgr.setPermissionRules(permissionRules);
        appraisalMgr.setLoggedInUser(currentlyLoggedOnUser);
        appraisalMgr.setAdmins(admins);
        appraisalMgr.setReviewers(reviewers);
        appraisalMgr.setAppraisalSteps(appraisalSteps);
        appraisalMgr.setMailer(mailer);
        appraisalMgr.setConfigurationMap(configurationMap);

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
        Session session = HibernateUtil.getCurrentSession();
        CompositeConfiguration config;
        int id = ParamUtil.getInteger(request, "id", 0);
        if (id == 0) {
            addErrorsToRequest(request, "We couldn't find your appraisal. If you believe this is an " +
                    "error, please contact your supervisor.");
            return displayHomeView(request, response);
        }

        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        setAppraisalMgrParameters(currentlyLoggedOnUser);
        Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            SessionErrors.add(request, "You do  not have permission to view the appraisal");
            return displayHomeView(request, response);
        }

        try {
            appraisalMgr.processUpdateRequest(request.getParameterMap(), appraisal, permRule);

            String signAppraisal = ParamUtil.getString(request, "sign-appraisal");
            if (signAppraisal != null && !signAppraisal.equals("")) {
                config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
                String nolijDir = config.getString("pdf.nolijDir");
                String env = config.getString("pdf.env");
                createNolijPDF(appraisal, nolijDir, env);
            }

            if (appraisal.getRole().equals("supervisor")) {
                setupMyTeamActiveAppraisals(request, currentlyLoggedOnUser.getId());
            } else if (appraisal.getRole().equals("employee")) {
                setupMyActiveAppraisals(request, currentlyLoggedOnUser.getId());
            }
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
        updateAppraisalInSession(request, appraisal);

        return displayHomeView(request, response);
    }

    /***
     * This method updates the status of the appraisal in myTeam or myStatus to reflect the
     * changes from the updateAppraisal method.
     *
     * @param request
     * @param appraisal
     */
    private void updateAppraisalInSession(PortletRequest request, Appraisal appraisal) {
        List<Appraisal>  appraisals;
        PortletSession pSession = request.getPortletSession();
        if (appraisal.getRole().equals("employee")) {
            appraisals = (List) pSession.getAttribute(ALL_MY_ACTIVE_APPRAISALS);
        } else if (appraisal.getRole().equals(ROLE_SUPERVISOR)) {
            appraisals = (List) pSession.getAttribute(MY_TEAMS_ACTIVE_APPRAISALS);
        } else {
            return;
        }

        for (Appraisal appraisalInSession: appraisals) {
            if (appraisalInSession.getId() == appraisal.getId()) {
                appraisalInSession.setStatus(appraisal.getStatus());
                break;
            }
        }
    }

    /**
     * Allows the end user to download a PDF copy of the appraisal
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String downloadPDF(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        PermissionRule permRule;

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        setAppraisalMgrParameters(currentlyLoggedOnUser);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRoleAndSession(appraisal, userId);
        appraisal.setRole(userRole);

        // 2) Compose a file name
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        String tmpDir = config.getString("pdf.tmpDir");
        String filename = EvalsPDF.getNolijFileName(appraisal, tmpDir, "dev2");

        // 3) Create PDF
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");
        String rootDir = portletContext.getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 4) Read the PDF file and provide to the user as attachment
        if (response instanceof ResourceResponse) {
            String title = appraisal.getJob().getJobTitle().replace(" ", "_");
            String employeeName = appraisal.getJob().getEmployee().getName().replace(" ", "_");
            String downloadFilename = "performance-appraisal-"+ title + "-" +
                     employeeName + "-" + appraisal.getJob().getPositionNumber()
                    + ".pdf";
            ResourceResponse res = (ResourceResponse) response;
            res.setContentType("application/pdf");
            res.addProperty(HttpHeaders.CACHE_CONTROL, "max-age=3600, must-revalidate");
            res.addProperty(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+downloadFilename);

            OutputStream out = res.getPortletOutputStream();
            RandomAccessFile in = new RandomAccessFile(filename, "r");

            byte[] buffer = new byte[4096];
            int len;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            out.flush();
            in.close();
            out.close();

            // 5) Delete the temp PDF file generated
            EvalsPDF.deletePDF(filename);
        }


        return null;
    }

    /**
     * Sends the appraisal to NOLIJ. This is only allowed to reviewers and does not check whether or not
     * the appraisal has been sent to nolij before. It calls createNolijPDF to do the work.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String resendAppraisalToNolij(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        PermissionRule permRule;
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        setAppraisalMgrParameters(currentlyLoggedOnUser);

        // 1) Get the appraisal and permission rule
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRoleAndSession(appraisal, userId);
        appraisal.setRole(userRole);

        requestMap.put("id", appraisal.getId());
        if (!isLoggedInUserReviewer(request)) {
            String errorMsg = resource.getString("appraisal-resend-permission-denied");
            addErrorsToRequest(request, errorMsg);
            return displayAppraisal(request, response);
        }

        // If there is a problem, createNolijPDF will throw an exception
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        String nolijDir = config.getString("pdf.nolijDir");
        String env = config.getString("pdf.env");
        createNolijPDF(appraisal, nolijDir, env);

        SessionMessages.add(request, "appraisal-sent-to-nolij-success");

        return displayAppraisal(request, response);
    }

    private void createNolijPDF(Appraisal appraisal, String dirName, String env) throws Exception {
        // 1) Compose a file name
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        String filename = EvalsPDF.getNolijFileName(appraisal, dirName, env);

        // 2) Grab the permissionRule
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal, true);

        // 2) Create PDF
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");
        String rootDir = portletContext.getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 3) Insert a record into the nolij_copies table
        String onlyFilename = filename.replaceFirst(dirName, "");
        NolijCopies.add(appraisal.getId(), onlyFilename);
    }



    /**
     * Handles listing the admin users. It only performs error checking. The list of
     * admins is already set by EvalsPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     */
    public String listAdmin(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        ArrayList<Admin> adminsList = (ArrayList<Admin>) portletContext.getAttribute("adminsList");
        requestMap.put("isMaster", isLoggedInUserMasterAdmin(request));
        requestMap.put("adminsList", adminsList);
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        AdminMgr adminMgr = new AdminMgr();
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Admin admin = adminMgr.get(id);
                if (admin.getEmployee() != null) { // initialize name due to lazy-loading
                    admin.getEmployee().getName();
                }
                requestMap.put("admin", admin);
                return "admin-delete-jsp";
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listAdmin(request, response);
            }

            adminMgr.delete(id);
            setEvalsAdmins();
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String isMaster = ParamUtil.getString(request, "isAdmin");

        try {
            adminMgr.add(onid,  isMaster, getLoggedOnUser(request));
            setEvalsAdmins();
            SessionMessages.add(request, "admin-added");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return listAdmin(request, response);
    }

    /**
     * Handles listing the reviewer users. It only performs error checking. The list of
     * reviewers is already set by EvalsPortlet.portletSetup, so we don't need to do
     * anything else in this method.
     *
     * @param request
     * @param response
     * @return
     */
    public String listReviewer(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        ArrayList<Reviewer> reviewersList = (ArrayList<Reviewer>) portletContext.getAttribute("reviewersList");
        BusinessCenterMgr businessCenterMgr = new BusinessCenterMgr();
        ArrayList<BusinessCenter> businessCenters = (ArrayList<BusinessCenter>) businessCenterMgr.list();

        requestMap.put("isMaster", isLoggedInUserMasterAdmin(request));
        requestMap.put("reviewersList", reviewersList);
        requestMap.put("businessCenters", businessCenters);
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
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        ReviewerMgr reviewerMgr = new ReviewerMgr();
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                Reviewer reviewer = reviewerMgr.get(id);
                if (reviewer.getEmployee() != null) {
                    reviewer.getEmployee().getName(); // initialize name due to lazy-loading
                }
                requestMap.put("reviewer", reviewer);
                return "reviewer-delete-jsp";
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listReviewer(request, response);
            }

            reviewerMgr.delete(id);
            setEvalsReviewers();
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
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String onid = ParamUtil.getString(request, "onid");
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");

        // Check whether or not the user is already an admin user
        Employee onidUser = employeeMgr.findByOnid(onid, null);
        if (getReviewer(onidUser.getId()) != null) {
            addErrorsToRequest(request, "This user is already a reviewer.");
            return listReviewer(request, response);
        }

        try {
            reviewerMgr.add(onid, businessCenterName);
            setEvalsReviewers();
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        ArrayList<Configuration> configurations = (ArrayList<Configuration>)
                portletContext.getAttribute("configurationsList");
        requestMap.put("configurations", configurations);
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
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        String value = ParamUtil.getString(request, "value");

        if (id != 0) {
            try {
                configurationMgr.edit(id, value);
                setEvalsConfiguration();
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
        requestMap.put("errorMsg", errorMsg);
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
            loggedOnUser = employeeMgr.findByOnid(loggedOnUsername, "employee-with-jobs");

            // Initialize the jobs and supervisor of the jobs so that display employment
            // information has the data it needs.
            Set<Job> jobs = loggedOnUser.getJobs();
            if (jobs != null && !jobs.isEmpty()) {
                for (Job job : jobs) {
                    if (job.getSupervisor() != null && job.getSupervisor().getEmployee() != null) {
                        job.getSupervisor().getEmployee().getName();
                    }
                }
            }
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
        PortletSession session = request.getPortletSession(true);
        String usernameSessionKey = "onidUsername";
        String onidUsername = (String) session.getAttribute(usernameSessionKey);
        if (onidUsername == null || onidUsername.equals("")) {
            Map userInfo = getLoggedOnUserMap(request);

            String screenName = "";
            if (userInfo != null) {
                screenName = (String) userInfo.get("user.name.nickName");
            }
            // If the screenName is numeric it means that we are using the Oracle db and
            // we need to query banner to fetch the onid username
            if (Pattern.matches("[0-9]+", screenName)) {
                CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
                String bannerHostname = config.getString("banner.hostname");
                onidUsername = EmployeeMgr.getOnidUsername(screenName, bannerHostname);
            } else {
                onidUsername = screenName;
            }
            session.setAttribute(usernameSessionKey, onidUsername);
        }
        return onidUsername;
    }

    /**
     * Sets the porletContext field. This method is called by the delegate and portletSetup
     * methods in EvalsPortlet.
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

        return reviewerMap.get(pidm);
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

        return adminMap.get(pidm);
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


        myActiveAppraisals = (ArrayList<Appraisal>) requestMap.get("myActiveAppraisals");
        employeeRequiredActions = getAppraisalActions(myActiveAppraisals, "employee", resource);
        requestMap.put("employeeActions", employeeRequiredActions);

        // add supervisor required actions, if user has team's active appraisals
        if (requestMap.get("myTeamsActiveAppraisals") != null) {
            supervisorActions = (ArrayList<Appraisal>) requestMap.get("myTeamsActiveAppraisals");
            administrativeActions = getAppraisalActions(supervisorActions, "supervisor", resource);
        }

        reviewer = getReviewer(employeeID);
        if (reviewer != null) {
            String businessCenterName = reviewer.getBusinessCenterName();
            reviewerAction = getReviewerAction(businessCenterName, resource);
            if (reviewerAction != null) {
                administrativeActions.add(reviewerAction);
            }
        }
        requestMap.put("administrativeActions", administrativeActions);
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
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public ArrayList<RequiredAction> getAppraisalActions(List<Appraisal> appraisalList,
                                                         String role, ResourceBundle resource) throws Exception {
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
            if (actionRequired != null && !actionRequired.equals("")) {
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

    /**
     * Sets the entries in requestMap in the RenderRequest object and clears the requestMap
     * afterwards.
     *
     * @param request
     */
    public void setRequestAttributes(RenderRequest request) {
        for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        requestMap.clear();

    }

    /**
     *  It also sets the currentRole from session to request.
     *
     * @param request
     */
    public void setHomeURL(RenderRequest request) {
        String homeAction = "displayHomeView";
        PortletSession session = request.getPortletSession(true);
        String currentRole = (String) session.getAttribute("currentRole");

        request.setAttribute("currentRole", currentRole);
        if (currentRole != null) {
            if (currentRole.equals(ROLE_ADMINISTRATOR)) {
                homeAction = "displayAdminHomeView";
            } else if (currentRole.equals(ROLE_REVIEWER)) {
                homeAction = "displayReviewerHomeView";
            } else if (currentRole.equals(ROLE_SUPERVISOR)) {
                homeAction = "displaySupervisorHomeView";
            }
        }
        request.setAttribute("homeAction", homeAction);
    }
}