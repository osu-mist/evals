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
import java.sql.Timestamp;
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
    private static final String REVIEW_LIST = "reviewList";
    private static final String REVIEW_LIST_MAX_RESULTS = "reviewListMaxResults";
    public static final String APPRAISAL_NOT_FOUND = "We couldn't find your appraisal. If you believe this is an " +
            "error, please contact your supervisor.";

    private EmployeeMgr employeeMgr = new EmployeeMgr();

    private JobMgr jobMgr = new JobMgr();

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

        return Constants.JSP_CRITERIA_ADD;
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

        return Constants.JSP_CRITERIA_ADD;
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
        return Constants.JSP_CRITERIA_LIST;
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
                return Constants.JSP_CRITERIA_DELETE;
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
    public String displayHomeView(PortletRequest request, PortletResponse response) throws Exception {
        int employeeId = getLoggedOnUser(request).getId();
        String homeJSP = getHomeJSP(request);
        CompositeConfiguration config = (CompositeConfiguration) portletContext.getAttribute("environmentProp");
        boolean isAdmin = isLoggedInUserAdmin(request);
        boolean isReviewer = isLoggedInUserReviewer(request);

        // specify menu type, help links and yellow box to display in home view
        useNormalMenu(request);
        helpLinks(request);
        requestMap.put("alertMsg", config.getBoolean("alert.display"));
        requestMap.put("isHome", true);

        setupMyActiveAppraisals(request, employeeId, null);
        setupMyTeamActiveAppraisals(request, employeeId);
        ArrayList<Appraisal> myActiveAppraisals = (ArrayList<Appraisal>) requestMap.get("myActiveAppraisals");
        ArrayList<Appraisal> myTeamsActiveAppraisals  =
                (ArrayList<Appraisal>) requestMap.get("myTeamsActiveAppraisals");

        boolean hasAppraisals = (myActiveAppraisals != null && !myActiveAppraisals.isEmpty()) ||
                (myTeamsActiveAppraisals != null && !myTeamsActiveAppraisals.isEmpty());

        if (!isAdmin && !isReviewer && !hasAppraisals) {
            requestMap.put("hasNoEvalsAccess", true);
        }

        setRequiredActions(request);
        if (homeJSP.equals(Constants.JSP_HOME_REVIEWER)) {
            int maxResults = config.getInt("reviewer.home.pending.max");
            ArrayList<Appraisal> appraisals = getReviewsForLoggedInUser(request, maxResults);
            requestMap.put("appraisals", appraisals);
        }
        return homeJSP;
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
     * @param firstAnnual   First annual appraisal created after trial appraisal is completed
     * @throws Exception
     */
    private void setupMyActiveAppraisals(PortletRequest request, int employeeId, Appraisal firstAnnual)
            throws Exception {
        List<Appraisal> allMyActiveAppraisals = getMyActiveAppraisals(request, employeeId);
        if (firstAnnual != null) {
            allMyActiveAppraisals.add(firstAnnual);
        }
        requestMap.put("myActiveAppraisals", allMyActiveAppraisals);
    }

    /**
     * Tries to fetch the employee active appraisals from session and if they are null, it grabs them from
     * the db.
     *
     * @param request
     * @param employeeId
     * @return
     * @throws Exception
     */
    private List<Appraisal> getMyActiveAppraisals(PortletRequest request, int employeeId) throws Exception {
        PortletSession session = request.getPortletSession(true);
        List<Appraisal> allMyActiveAppraisals;

        allMyActiveAppraisals = (ArrayList<Appraisal>) session.getAttribute(ALL_MY_ACTIVE_APPRAISALS);
        if (allMyActiveAppraisals == null) {
            allMyActiveAppraisals = appraisalMgr.getAllMyActiveAppraisals(employeeId);
            session.setAttribute(ALL_MY_ACTIVE_APPRAISALS, allMyActiveAppraisals);
        }
        return allMyActiveAppraisals;
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
        if (isLoggedInUserSupervisor(request)) {
            ArrayList<Appraisal> myTeamAppraisals = getMyTeamActiveAppraisals(request, employeeId);
            requestMap.put(MY_TEAMS_ACTIVE_APPRAISALS, myTeamAppraisals);
        }
    }

    /**
     * Tries to fetch the my teams active appraisals from session. If they list is null, it fetches them
     * from the db.
     *
     * @param request       PortletRequest
     * @param employeeId    Id of the logged in user
     * @return              ArrayList<Appraisal>
     * @throws Exception
     */
    private ArrayList<Appraisal> getMyTeamActiveAppraisals(PortletRequest request, int employeeId) throws Exception {
        PortletSession session = request.getPortletSession(true);

        ArrayList<Appraisal> myTeamAppraisals;
        List<Appraisal> dbTeamAppraisals;
        myTeamAppraisals = (ArrayList<Appraisal>) session.getAttribute(MY_TEAMS_ACTIVE_APPRAISALS);
        if (myTeamAppraisals == null) {
            dbTeamAppraisals = appraisalMgr.getMyTeamsAppraisals(employeeId, true);
            myTeamAppraisals = new ArrayList<Appraisal>();

            if (dbTeamAppraisals != null) {
                for (Appraisal appraisal : dbTeamAppraisals) {
                    appraisal.setRole("supervisor");
                    myTeamAppraisals.add(appraisal);
                }
            }
            session.setAttribute(MY_TEAMS_ACTIVE_APPRAISALS, myTeamAppraisals);
        }
        return myTeamAppraisals;
    }

    public String displayMyInformation(PortletRequest request, PortletResponse response) throws Exception {
        useNormalMenu(request);
        requestMap.put("employee", getLoggedOnUser(request));

        return Constants.JSP_MY_INFO;
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
     * @param updateContextTimestamp    Whether or not to update the context timestamp in config_times
     * @throws Exception
     */
    public void setEvalsAdmins(boolean updateContextTimestamp) throws Exception {
        portletContext.setAttribute("admins", adminMgr.mapByEmployeeId());
        List<Admin> admins = adminMgr.list();
        // Call getName on the admins object to initialize the employee name
        for (Admin admin : admins) {
            if (admin.getEmployee() != null) {
                admin.getEmployee().getName();
            }
        }
        portletContext.setAttribute("adminsList", admins);
        if (updateContextTimestamp) {
            updateContextTimestamp();
        }
    }

    /**
     * Updates the reviewers List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by Actions.addReviewer methods.
     *
     * @param updateContextTimestamp    Whether or not to update the context timestamp in config_times
     * @throws Exception
     */
    public void setEvalsReviewers(boolean updateContextTimestamp) throws Exception {
        portletContext.setAttribute("reviewers", reviewerMgr.mapByEmployeeId());
        List<Reviewer> reviewers = reviewerMgr.list();
        // Call getName on the reviewers object to initialize the employee name
        for (Reviewer reviewer : reviewers) {
            if (reviewer.getEmployee() != null) {
                reviewer.getEmployee().getName();
            }
        }
        portletContext.setAttribute("reviewersList", reviewers);
        if (updateContextTimestamp) {
            updateContextTimestamp();
        }
    }

    /**
     * Updates the configuration List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by Actions.editConfiguration methods.
     *
     * @param updateContextTimestamp    Whether or not to update the context timestamp in config_times
     * @throws Exception
     */
    public void setEvalsConfiguration(boolean updateContextTimestamp) throws Exception {
        portletContext.setAttribute("configurations", configurationMgr.mapByName());
        portletContext.setAttribute("configurationsList", configurationMgr.list());
        if (updateContextTimestamp) {
            updateContextTimestamp();
        }
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

        return Constants.JSP_REVIEW_LIST;
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
     * Returns the reviews for the logged on User. It is a wrapper for
     * getReviewsForLoggedInUser(request, maxResults). It basically looks up in session what is
     * the number of maxResults and calls the getReviewsForLoggedInUser method.
     *
     * @param request       PortletRequest object
     * @return              ArrayList<Appraisal>
     * @throws Exception
     */
    private ArrayList<Appraisal> getReviewsForLoggedInUser(PortletRequest request) throws Exception {
        int defaultMaxResults = -1;
        PortletSession session = request.getPortletSession(true);
        Integer maxResults = (Integer) session.getAttribute(REVIEW_LIST_MAX_RESULTS);
        if (maxResults == null) {
            maxResults = defaultMaxResults;
        }

        return getReviewsForLoggedInUser(request, maxResults);
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
        ArrayList<Appraisal> reviewList;
        int toIndex;
        ArrayList<Appraisal> outList = new ArrayList<Appraisal>();

        PortletSession session = request.getPortletSession(true);
        reviewList = (ArrayList<Appraisal>) session.getAttribute(REVIEW_LIST);
        session.setAttribute(REVIEW_LIST_MAX_RESULTS, maxResults);

        if (reviewList == null) { //No data yet, need to get it from the database.
            String businessCenterName = ParamUtil.getString(request, "businessCenterName");

            if (businessCenterName.equals("")) {
                int employeeID = getLoggedOnUser(request).getId();
                businessCenterName = getReviewer(employeeID).getBusinessCenterName();
            }
            reviewList = appraisalMgr.getReviews(businessCenterName, -1);
            session.setAttribute(REVIEW_LIST, reviewList);
        }

        if (maxResults == -1 || reviewList.size() < maxResults) {
            toIndex = reviewList.size();
        } else {
            toIndex = maxResults;
        }

        for (int i = 0; i < toIndex; i++) {
            outList.add(reviewList.get(i));
        }

        return outList;
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

        return Constants.JSP_REVIEW_LIST;
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
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);

        setupMyTeamActiveAppraisals(request, userId);
        if (isLoggedInUserReviewer(request)) {
            ArrayList<Appraisal> reviews = getReviewsForLoggedInUser(request, -1);
            requestMap.put("pendingReviews", reviews);
        }

        if (isLoggedInUserReviewer(request) && appraisal.getEmployeeSignedDate() != null) {
            requestMap.put("displayResendNolij", true);
        }
        if ((isLoggedInUserReviewer(request) || isLoggedInUserAdmin(request)) && appraisal.isOpen()) {
            requestMap.put("displayCloseOutAppraisal", true);
        }
        String status = appraisal.getStatus();
        if ((isLoggedInUserAdmin(request) || isLoggedInUserReviewer(request)) &&
                status.equals(Appraisal.STATUS_GOALS_APPROVED)) {
            requestMap.put("displaySetAppraisalStatus", true);
        }

        // Initialze lazy appraisal associations
        Job job = appraisal.getJob();
        job.toString();
        Job supervisor = job.getSupervisor();
        if (supervisor != null && supervisor.getEmployee() != null) {
            supervisor.getEmployee().toString();
        }
        if (job.getEmployee() != null) {
            job.getEmployee().toString();
        }
        if (appraisal.getCloseOutReason() != null) {
            appraisal.getCloseOutReason().getReason();
        }
        appraisal.getSortedAssessments().size();
        for (Assessment assessment : appraisal.getAssessments()) {
            assessment.getCriterionDetail().getAreaID();
        }
        // End of initialize lazy appraisal associations

        requestMap.put("appraisal", appraisal);
        requestMap.put("permissionRule", permRule);
        useMaximizedMenu(request);

        return Constants.JSP_APPRAISAL;
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
            addErrorsToRequest(request, APPRAISAL_NOT_FOUND);
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


            // Creates the first annual appraisal if needed
            Map<String, Configuration> configurationMap =
                    (Map<String, Configuration>) portletContext.getAttribute("configurations");
            Configuration resultsDueConfig = configurationMap.get(Appraisal.STATUS_RESULTS_DUE);
            String action = "";
            if (signAppraisal != null && !signAppraisal.equals("")) {
                action = "sign-appraisal";
            }
            Appraisal firstAnnual = appraisalMgr.createFirstAnnualAppraisal(appraisal, resultsDueConfig, action);


            if (appraisal.getRole().equals("supervisor")) {
                setupMyTeamActiveAppraisals(request, currentlyLoggedOnUser.getId());
            } else if (appraisal.getRole().equals("employee")) {
                setupMyActiveAppraisals(request, currentlyLoggedOnUser.getId(), firstAnnual);
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


        if (appraisal.getStatus().equals(Appraisal.STATUS_RELEASE_DUE) && isLoggedInUserReviewer(request)) {
            removeReviewAppraisalInSession(request, appraisal);
        } else {
            updateAppraisalInSession(request, appraisal);
        }

        return displayHomeView(request, response);
    }

    /**
     * Handles removing an appraisal from the reviewList stored in session. This method is called
     * by the updateAppraisal method after a reviewer submits a review.
     *
     * @param request
     * @param appraisal
     * @throws Exception
     */
    private void removeReviewAppraisalInSession(PortletRequest request, Appraisal appraisal) throws Exception {
        List<Appraisal> reviewList = getReviewsForLoggedInUser(request);
        List<Appraisal> tempList = new ArrayList<Appraisal>();
        tempList.addAll(reviewList);
        for (Appraisal appraisalInSession: tempList) {
            if (appraisalInSession.getId() == appraisal.getId()) {
                reviewList.remove(appraisalInSession);
                break;
            }
        }

        PortletSession session = request.getPortletSession(true);
        session.setAttribute(REVIEW_LIST, reviewList);
    }

    /***
     * This method updates the status of the appraisal in myTeam or myStatus to reflect the
     * changes from the updateAppraisal method.
     *
     * @param request       PortletRequest
     * @param appraisal     appraisal to update in session
     * @throws Exception
     */
    private void updateAppraisalInSession(PortletRequest request, Appraisal appraisal) throws Exception {
        List<Appraisal>  appraisals;
        Employee loggedOnUser = getLoggedOnUser(request);
        int employeeId = loggedOnUser.getId();
        if (appraisal.getRole().equals("employee")) {
            appraisals = getMyActiveAppraisals(request, employeeId);
        } else if (appraisal.getRole().equals(ROLE_SUPERVISOR)) {
            appraisals = getMyTeamActiveAppraisals(request, employeeId);
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
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRole(appraisal, userId);
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
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int userId = currentlyLoggedOnUser.getId();
        String userRole = appraisalMgr.getRole(appraisal, userId);
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
        PermissionRule permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);

        // 2) Create PDF
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");
        String rootDir = portletContext.getRealPath("/");
        EvalsPDF.createPDF(appraisal, permRule, filename, resource, rootDir);

        // 3) Insert a record into the nolij_copies table
        String onlyFilename = filename.replaceFirst(dirName, "");
        NolijCopies.add(appraisal.getId(), onlyFilename);
    }

    /**
     * Handles setting the status of an appraisal record to results due.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String setStatusToResultsDue(PortletRequest request, PortletResponse response) throws Exception {
        Appraisal appraisal;
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        int userId = currentlyLoggedOnUser.getId();

        int appraisalID = ParamUtil.getInteger(request, "id");
        if (appraisalID == 0) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        setAppraisalMgrParameters(currentlyLoggedOnUser);

        // 1) Get the appraisal and role
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        String userRole = appraisalMgr.getRole(appraisal, userId);
        appraisal.setRole(userRole);
        if (!userRole.equals("admin") && !userRole.equals(ROLE_REVIEWER)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        if (request instanceof ActionRequest && response instanceof ActionResponse) {
            appraisal.setOriginalStatus(appraisal.getStatus());
            appraisal.setStatus(Appraisal.STATUS_RESULTS_DUE);
            AppraisalMgr.updateAppraisalStatus(appraisal);
            SessionMessages.add(request, "appraisal-set-status-success");
            return displayAppraisal(request, response);
        }

        return displayHomeView(request, response);
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

        refreshContextCache();
        ArrayList<Admin> adminsList = (ArrayList<Admin>) portletContext.getAttribute("adminsList");
        requestMap.put("isMaster", isLoggedInUserMasterAdmin(request));
        requestMap.put("adminsList", adminsList);
        useMaximizedMenu(request);

        return Constants.JSP_ADMIN_LIST;
    }

    /**
     * Checks if the context cache is outdated and refreshes the context cache:
     * admins, reviewers and configuration lists and maps. If the context cache is refreshed, it
     * updates the context cache timestamp in the portlet context.
     *
     * @throws Exception
     */
    private void refreshContextCache() throws Exception {
        Date contextCacheTimestamp = (Date) portletContext.getAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP);
        Timestamp contextLastUpdate = ConfigurationMgr.getContextLastUpdate();
        if (contextLastUpdate.after(contextCacheTimestamp)) {
            setEvalsAdmins(false);
            setEvalsReviewers(false);
            setEvalsConfiguration(false);
            portletContext.setAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP, new Date());
        }
    }

    /**
     * Updates the context timestamp in the db and also in the portletContext.
     * @throws Exception
     */
    private void updateContextTimestamp() throws Exception {
        Date currentTimestamp = ConfigurationMgr.updateContextTimestamp();
        portletContext.setAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP, currentTimestamp);
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
                return Constants.JSP_ADMIN_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listAdmin(request, response);
            }

            adminMgr.delete(id);
            setEvalsAdmins(true);
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
            setEvalsAdmins(true);
            SessionMessages.add(request, "admin-added");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return listAdmin(request, response);
    }

    /**
     * Handles listing the close out reasons.
     *
     * @param request
     * @param response
     * @return
     */
    public String listCloseOutReasons(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        ArrayList<CloseOutReason> reasonsList = CloseOutReasonMgr.list(false);
        requestMap.put("reasonsList", reasonsList);
        useMaximizedMenu(request);

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
    public String addCloseOutReason(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        String onid = ParamUtil.getString(request, "reason");
        try {
            CloseOutReasonMgr.add(onid, getLoggedOnUser(request));
            SessionMessages.add(request, "closeout-reason-added");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return listCloseOutReasons(request, response);
    }

    /**
     * Handles performing a soft delete of a single close out reason.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String deleteCloseOutReason(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!isLoggedInUserAdmin(request)) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        try {

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                CloseOutReason reason = CloseOutReasonMgr.get(id);
                requestMap.put("reason", reason);
                return Constants.JSP_CLOSEOUT_REASON_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listCloseOutReasons(request, response);
            }

            CloseOutReasonMgr.delete(id);
            SessionMessages.add(request, "closeout-reason-deleted");
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        }
        useNormalMenu(request);
        return listCloseOutReasons(request, response);
    }

    /**
     * Handles an admin/reviewer closing an appraisal. We only display the form to close it. The
     * logic to handle closing is done by updateAppraisal method.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String closeOutAppraisal(PortletRequest request, PortletResponse response) throws Exception {
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

        // 1) Get the appraisal, permission rule and userRole
        appraisal = appraisalMgr.getAppraisal(appraisalID);
        permRule = appraisalMgr.getAppraisalPermissionRule(appraisal);
        String userRole = appraisalMgr.getRole(appraisal, userId);

        // Check to see if the logged in user has permission to access the appraisal
        boolean isAdminOrReviewer = userRole.equals("admin") || userRole.equals("reviewer");
        if (permRule == null || !isAdminOrReviewer) {
            addErrorsToRequest(request, ACCESS_DENIED);
            return displayHomeView(request, response);
        }

        List<CloseOutReason> reasonList = CloseOutReasonMgr.list(false);
        appraisal.getJob().getEmployee().toString();

        requestMap.put("reasonsList", reasonList);
        requestMap.put("appraisal", appraisal);
        useMaximizedMenu(request);

        return Constants.JSP_APPRAISAL_CLOSEOUT;
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

        refreshContextCache();
        ArrayList<Reviewer> reviewersList = (ArrayList<Reviewer>) portletContext.getAttribute("reviewersList");
        BusinessCenterMgr businessCenterMgr = new BusinessCenterMgr();
        ArrayList<BusinessCenter> businessCenters = (ArrayList<BusinessCenter>) businessCenterMgr.list();

        requestMap.put("isMaster", isLoggedInUserMasterAdmin(request));
        requestMap.put("reviewersList", reviewersList);
        requestMap.put("businessCenters", businessCenters);
        useMaximizedMenu(request);

        return Constants.JSP_REVIEWER_LIST;
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
                return Constants.JSP_REVIEWER_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return listReviewer(request, response);
            }

            reviewerMgr.delete(id);
            setEvalsReviewers(true);
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
            setEvalsReviewers(true);
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

        refreshContextCache();
        ArrayList<Configuration> configurations = (ArrayList<Configuration>)
                portletContext.getAttribute("configurationsList");
        requestMap.put("configurations", configurations);
        useMaximizedMenu(request);

        return Constants.JSP_CONFIGURATION_LIST;
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
                setEvalsConfiguration(true);
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
            Set<Job> jobs = loggedOnUser.getNonTerminatedJobs();
            if (jobs != null && !jobs.isEmpty()) {
                for (Job job : jobs) {
                    if (job.getSupervisor() != null && job.getSupervisor().getEmployee() != null) {
                        job.getSupervisor();
                        job.getSupervisor().getEmployee();
                        job.getSupervisor().getEmployee().getName();
                    }
                }
            }
            session.setAttribute("loggedOnUser", loggedOnUser);
            refreshContextCache();
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
    public String getLoggedOnUsername(PortletRequest request) {
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
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null) {
            setUpUserPermissionInSession(request, false);
            isAdmin = (Boolean) session.getAttribute("isAdmin");
        }
        return isAdmin;
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
        Boolean isMasterAdmin = (Boolean) session.getAttribute("isMasterAdmin");
        if (isMasterAdmin == null) {
            setUpUserPermissionInSession(request, false);
            isMasterAdmin = (Boolean) session.getAttribute("isMasterAdmin");
        }
        return isMasterAdmin;
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
        Boolean isReviewer = (Boolean) session.getAttribute("isReviewer");
        if (isReviewer == null) {
            setUpUserPermissionInSession(request, false);
            isReviewer = (Boolean) session.getAttribute("isReviewer");
        }
        return isReviewer;
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
        Boolean isSupervisor = (Boolean) session.getAttribute("isSupervisor");
        if (isSupervisor == null) {
            setUpUserPermissionInSession(request, false);
            isSupervisor = (Boolean) session.getAttribute("isSupervisor");
        }
        return isSupervisor;
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
            reviewerAction = getReviewerAction(businessCenterName, resource, request);
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
                if (appraisalStatus.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION) ||
                        appraisalStatus.equals(Appraisal.STATUS_GOALS_REACTIVATED)) {
                    configuration = configurationMap.get(Appraisal.STATUS_GOALS_DUE);
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
    private RequiredAction getReviewerAction(String businessCenterName, ResourceBundle resource,
                                             PortletRequest request) throws Exception {
        int reviewCount;
        List<Appraisal> reviewList = getReviewsForLoggedInUser(request);
        if (reviewList != null) {
            reviewCount = reviewList.size();
        } else {
            reviewCount = appraisalMgr.getReviewCount(businessCenterName);
        }

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
        String currentRole = getCurrentRole(request);
        requestMap.put("currentRole", currentRole);

        for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        requestMap.clear();

    }

    /**
     * Returns the currentRole that the logged in user last selected. It
     * tries to grab it from the request and it stores it in session.
     *
     * @param request
     * @return
     */
    public String getCurrentRole(PortletRequest request) {
        PortletSession session = request.getPortletSession(true);
        String currentRole = (String) session.getAttribute("currentRole");

        String roleFromRequest = ParamUtil.getString(request, "currentRole");
        if (!roleFromRequest.equals("")) {
            currentRole = roleFromRequest;
            session.setAttribute("currentRole", currentRole);
        }

        if (currentRole == null || currentRole.equals("")) {
            currentRole = ROLE_SELF;
            session.setAttribute("currentRole", currentRole);
        }

        return currentRole;
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
    public String getHomeJSP(PortletRequest request) throws Exception {
        String homeJsp = Constants.JSP_HOME;
        String currentRole = getCurrentRole(request);

        if (currentRole.equals(ROLE_ADMINISTRATOR)) {
            if (!isLoggedInUserAdmin(request)) {
                addErrorsToRequest(request, ACCESS_DENIED);
            } else {
                homeJsp = Constants.JSP_HOME_ADMIN;
            }
        } else if (currentRole.equals(ROLE_REVIEWER)) {
            if (!isLoggedInUserReviewer(request)) {
                addErrorsToRequest(request, ACCESS_DENIED);
            } else {
                homeJsp = Constants.JSP_HOME_REVIEWER;
            }
        } else if (currentRole.equals(ROLE_SUPERVISOR)) {
            if (!isLoggedInUserSupervisor(request)) {
                addErrorsToRequest(request, ACCESS_DENIED);
            } else {
                homeJsp = Constants.JSP_HOME_SUPERVISOR;
            }
        }

        return homeJsp;
    }
}
