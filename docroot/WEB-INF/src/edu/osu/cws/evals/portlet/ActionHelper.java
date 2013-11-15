package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsLogger;
import edu.osu.cws.util.CWSUtil;
import edu.osu.cws.util.Logger;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ActionHelper class used to map user form actions to respective class methods.
 */
public class ActionHelper {
    public static final String ROLE_EMPLOYEE = "employee";
    public static final String ROLE_ADMINISTRATOR = "admin";
    public static final String ROLE_REVIEWER = "reviewer";
    public static final String ROLE_SUPERVISOR = "supervisor";
    public static final String ROLE_UPPER_SUPERVISOR = "upper-supervisor";
    public static final String ROLE_SELF = "self";
    public static final String ALL_MY_ACTIVE_APPRAISALS = "allMyActiveAppraisals";
    public static final String MY_TEAMS_ACTIVE_APPRAISALS = "myTeamsActiveAppraisals";
    private static final String REVIEW_LIST = "reviewList";
    private static final String REVIEW_LIST_MAX_RESULTS = "reviewListMaxResults";
    public static final String REQUEST_MAP = "requestMap";

    private PortletContext portletContext;

    private PortletRequest request;

    private static Log _log = LogFactoryUtil.getLog(ActionHelper.class);

    private PortletResponse response;

    private Employee loggedOnUser;

    private HashMap<String,Object> requestMap;

    private ResourceBundle bundle;

    public ActionHelper(PortletRequest request, PortletResponse response,
                        PortletContext portletContext) throws Exception {
        this.request = request;
        this.response = response;
        this.portletContext = portletContext;
        this.bundle = (ResourceBundle) portletContext.getAttribute("resourceBundle");
        setRequestMap();
        setLoggedOnUser();
    }

    /**
     * Wrapper method to control access to the portlet session.
     *
     * @return
     * @throws Exception
     */
    PortletSession getSession() throws Exception {
        PortletSession session = request.getPortletSession(true);

        if (!request.isRequestedSessionIdValid()) {
            throw new Exception("Session is invalid.");
        }

        if (!isHttpSessionValid(request)) {
            throw new Exception("HttpSession is invalid.");
        }

        return session;
    }

    /**
     * Wrapper method to control access to the portlet session. This method is a temporary
     * solution since we plan on doing some code refactor to only have this class access the
     * portlet session.
     *
     * @return
     * @throws Exception
     */
    public static PortletSession getSession(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession(true);

        if (!request.isRequestedSessionIdValid()) {
            throw new Exception("Portlet Session is invalid.");
        }

        if (!isHttpSessionValid(request)) {
            throw new Exception("HttpSession is invalid.");
        }

        return session;
    }

    /**
     * Safe method that fetches session attribute. It handles session being invalid.
     *
     * @param key
     * @return
     */
    public Object getSessionAttribute(String key) {
        return getSessionAttribute(request, key, portletContext);
    }

    /**
     * Safe method that fetches session attribute. It handles session being invalid.
     *
     * @param request           PortletRequest
     * @param key               Attribute key from session
     * @return
     */

    public static Object getSessionAttribute(PortletRequest request, String key,
                                             PortletContext portletContext) {
        Object value = null;
        try {
            value = getSession(request).getAttribute(key);
        } catch (Exception e) {
            EvalsLogger logger = (EvalsLogger) portletContext.getAttribute("log");
            try {
                logger.log(Logger.ERROR, "Failed to get session", e);
            } catch (Exception ex) {
                _log.error(ex);
            }
        }

        return value;
    }

    /**
     * Whether or not the http session associated with the portlet session is valid. This is checked
     * by calling getCreationTime on the http session. If the http session is invalid calling
     * getCreationTime throws an exception and we can check using that.
     *
     * @param request
     * @return
     */
    private static boolean isHttpSessionValid(PortletRequest request) {
        try {
            HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(request);
            HttpSession session = servletRequest.getSession(true);
            // if the session is invalid calling getCreationTime will throw illegal state exception
            session.getCreationTime();
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private void setRequestMap() throws Exception {
        PortletSession session = getSession();
        requestMap = (HashMap)session.getAttribute(REQUEST_MAP);
        if (requestMap == null) {
            requestMap = new HashMap<String, Object>();
            session.setAttribute(REQUEST_MAP, requestMap);
        }
    }

    /**
     * Specifies whether or not the request is an AJAX request by checking whether or not
     * request and response are instances of ResourceRequest and ResourceResponse.
     *
     * @return
     */
    public boolean isAJAX() {
        return request instanceof ResourceRequest && response instanceof ResourceResponse;
    }

    /**
     * Places in the request object the active appraisals of the user. This is used by the notification
     * piece.
     *
     * @throws Exception
     */
    public void setupMyActiveAppraisals() throws Exception {
        List<Appraisal> allMyActiveAppraisals = getMyActiveAppraisals();
        addToRequestMap("myActiveAppraisals", allMyActiveAppraisals);
    }

    /**
     * Tries to fetch the employee active appraisals from session and if they are null, it grabs
     * them from the db.
     *
     * @return
     * @throws Exception
     */
    public List<Appraisal> getMyActiveAppraisals() throws Exception {
        PortletSession session = getSession();
        List<Appraisal> allMyActiveAppraisals;

        allMyActiveAppraisals = (ArrayList<Appraisal>) session.getAttribute(ALL_MY_ACTIVE_APPRAISALS);
        if (allMyActiveAppraisals == null) {
            allMyActiveAppraisals =
                    AppraisalMgr.getAllMyActiveAppraisals(loggedOnUser.getId(), null, null);
            session.setAttribute(ALL_MY_ACTIVE_APPRAISALS, allMyActiveAppraisals);
        }
        return allMyActiveAppraisals;
    }

    /**
     * Fetches the supervisor's team active appraisal and stores the list in session. Then it places the list
     * in the requestMap so that the view can access it.
     *
     * @throws Exception
     */
    public void setupMyTeamActiveAppraisals() throws Exception {
        if (isLoggedInUserSupervisor()) {
            ArrayList<Appraisal> myTeamAppraisals = getMyTeamActiveAppraisals();
            addToRequestMap(MY_TEAMS_ACTIVE_APPRAISALS, myTeamAppraisals);
        }
    }

    /**
     * Tries to fetch the my teams active appraisals from session. If they list is null, it fetches them
     * from the db.
     *
     * @return              ArrayList<Appraisal>
     * @throws Exception
     */
    public ArrayList<Appraisal> getMyTeamActiveAppraisals() throws Exception {
        PortletSession session = getSession();

        ArrayList<Appraisal> myTeamAppraisals;
        myTeamAppraisals = (ArrayList<Appraisal>) session.getAttribute(MY_TEAMS_ACTIVE_APPRAISALS);
        if (myTeamAppraisals == null) {
            myTeamAppraisals =
                    AppraisalMgr.getMyTeamsAppraisals(loggedOnUser.getId(), true, null, null);
            session.setAttribute(MY_TEAMS_ACTIVE_APPRAISALS, myTeamAppraisals);
        }
        return myTeamAppraisals;
    }

    public void setUpUserPermission(boolean refresh) throws Exception {
        PortletSession session = getSession();

        Boolean isSupervisor = (Boolean) session.getAttribute("isSupervisor");

        if (refresh || isSupervisor == null) {
            isSupervisor = JobMgr.isSupervisor(loggedOnUser.getId(), null);
            session.setAttribute("isSupervisor", isSupervisor);
        }

        addToRequestMap("isSupervisor", isSupervisor);
        addToRequestMap("isReviewer", getReviewer() != null);
        addToRequestMap("isAdmin", getAdmin() != null);
        addToRequestMap("isMasterAdmin", isLoggedInUserMasterAdmin());
        addToRequestMap("employee", loggedOnUser);
    }


    /**
     * Updates the admins List in the portletContext. This method is called by
     * EvalsPortlet.portletSetup and by AdminsAction.add methods.
     *
     * @param
     * @throws Exception
     */
    private void setEvalsAdmins() throws Exception {
        portletContext.setAttribute("admins", AdminMgr.mapByEmployeeId());
        List<Admin> admins = AdminMgr.list();
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
     * EvalsPortlet.portletSetup and by ReviewersAction.add methods.
     *
     * @param
     * @throws Exception
     */
    private void setEvalsReviewers() throws Exception {
        portletContext.setAttribute("reviewers", ReviewerMgr.mapByEmployeeId());
        List<Reviewer> reviewers = ReviewerMgr.list();
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
     * EvalsPortlet.portletSetup and by ConfigurationsAction.edit methods.
     *
     * @param
     * @throws Exception
     */
    private void setEvalsConfiguration() throws Exception {
        portletContext.setAttribute("configurations", ConfigurationMgr.mapByName());
        portletContext.setAttribute("configurationsList", ConfigurationMgr.list());
    }

    /**
     * Sets a request parameter to tell the jsp to use the normal top menu.
     *
     */
    public void useNormalMenu() {
        addToRequestMap("menuHome", true);
    }

    /**
     * Sets a request parameter to tell the jsp to use the maximized top menu.
     *
     */
    public void useMaximizedMenu() {
        addToRequestMap("menuMax", true);
    }

    /**
     * Retrieves the pending reviews for the logged in user.
     *
     * @param maxResults
     * @return
     * @throws Exception
     */
    public ArrayList<Appraisal> getReviewsForLoggedInUser(int maxResults) throws Exception {
        ArrayList<Appraisal> reviewList;
        int toIndex;
        ArrayList<Appraisal> outList = new ArrayList<Appraisal>();

        PortletSession session = getSession();
        reviewList = (ArrayList<Appraisal>) session.getAttribute(REVIEW_LIST);
        session.setAttribute(REVIEW_LIST_MAX_RESULTS, maxResults);

        if (reviewList == null) { //No data yet, need to get it from the database.
            String businessCenterName = ParamUtil.getString(request, "businessCenterName");

            if (businessCenterName.equals("")) {
                businessCenterName = getReviewer().getBusinessCenterName();
            }
            reviewList = AppraisalMgr.getReviews(businessCenterName, -1);
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
     * Checks if the context cache is outdated and refreshes the context cache:
     * admins, reviewers and configuration lists and maps. If the context cache is refreshed, it
     * updates the context cache timestamp in the portlet context.
     *
     * @throws Exception
     */
    public void refreshContextCache() throws Exception {
        Date contextCacheTimestamp =
                (Date) portletContext.getAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP);
        Timestamp contextLastUpdate = ConfigurationMgr.getContextLastUpdate();
        if (contextCacheTimestamp != null && contextLastUpdate.after(contextCacheTimestamp)) {
            setAdminPortletData();
            portletContext.setAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP, new Date());
        }
    }

    /**
     * Refreshes the context cache:
     * admins, reviewers and configuration lists and maps.
     *
     * @throws Exception
     */
    public void setAdminPortletData() throws Exception {
        setEvalsAdmins();
        setEvalsReviewers();
        setEvalsConfiguration();
        setNotices();
    }

    /**
     * Updates the context timestamp in the db and also in the portletContext.
     * @throws Exception
     */
    public void updateContextTimestamp() throws Exception {
        Date currentTimestamp = ConfigurationMgr.updateContextTimestamp();
        portletContext.setAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP, currentTimestamp);
    }

    /**
     * Takes an string error message and sets in the session.
     *
     * @param errorMsg
     */
    public void addErrorsToRequest(String errorMsg) {
        addToRequestMap("errorMsg", errorMsg);
    }

    /**
     * Returns an Employee object of the currently logged on user. First it looks in
     * the PortletSession if it's not there it fetches the Employee object and stores
     * it there.
     *
     * @return
     * @throws Exception
     */
    public Employee getLoggedOnUser() throws Exception {
        return loggedOnUser;
    }

    /**
     * Gets the ONID username of the employee and fetches the employee object from the db
     * if not found in session. The loggedOnUser object is set as an instance variable.
     * It is also stored in the portlet session.
     *
     * @throws Exception
     */
    private void setLoggedOnUser() throws Exception {
        // try to set it from session
        PortletSession session = getSession();
        loggedOnUser = (Employee) session.getAttribute("loggedOnUser");

        // if not in session, get it from db
        if (loggedOnUser == null) {
            loggedOnUser = EmployeeMgr.findByOnid(getLoggedOnUsername(), "employee-with-jobs");
            loggedOnUser.setLoadJobs(false);
            session.setAttribute("loggedOnUser", loggedOnUser);
            refreshContextCache();
        }
    }

    /**
     * Returns a map with information on the currently logged on user.
     *
     * @return
     */
    private Map getLoggedOnUserMap() {
        return (Map)request.getAttribute(PortletRequest.USER_INFO);
    }

    /**
     * Returns the username of the currently logged on user. If there is no valid username, it
     * returns an empty string.
     *
     * @return username
     */
    public String getLoggedOnUsername() throws Exception {
        PortletSession session = getSession();
        String usernameSessionKey = "onidUsername";
        String onidUsername = (String) session.getAttribute(usernameSessionKey);
        if (onidUsername == null || onidUsername.equals("")) {
            Map userInfo = getLoggedOnUserMap();

            String screenName = "";
            if (userInfo != null) {
                screenName = (String) userInfo.get("user.name.nickName");
            }
            // If the screenName is numeric it means that we are using the Oracle db and
            // we need to query banner to fetch the onid username
            if (Pattern.matches("[0-9]+", screenName)) {
                PropertiesConfiguration config = getEvalsConfig();
                String bannerHostname = config.getString("banner.hostname");
                String luminisHostname = config.getString("luminis.hostname");
                onidUsername = EmployeeMgr.getOnidUsername(screenName, bannerHostname, luminisHostname);
            } else {
                onidUsername = screenName;
            }
            session.setAttribute(usernameSessionKey, onidUsername);
        }
        return onidUsername;
    }

    public PortletContext getPortletContext() {
        return portletContext;
    }

    /**
     * Returns an attribute from the portletContext
     *
     * @param key
     * @return
     */
    public Object getPortletContextAttribute(String key) {
        return portletContext.getAttribute(key);
    }

    /**
     * Returns the PropertiesConfiguration object that holds EvalS environment properties used
     * for configuration.
     *
     * @return PropertiesConfiguration
     */
    public PropertiesConfiguration getEvalsConfig() {
        return (PropertiesConfiguration) portletContext.getAttribute("environmentProp");
    }

    /**
     * Looks up in the reviewers HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer.
     * If yes, then we return the Reviewer object if not, it returns null.
     *
     * @return Reviewer
     */
    public Reviewer getReviewer() {
        HashMap<Integer, Reviewer> reviewerMap =
                (HashMap<Integer, Reviewer>) portletContext.getAttribute("reviewers");

        return reviewerMap.get(loggedOnUser.getId());
    }

    /**
     * Looks up in the admins HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer.
     * If yes, then we return the Admin object if not, it returns false.
     *
     * @return Admin
     */
    public Admin getAdmin() {
        HashMap<Integer, Admin> adminMap =
                (HashMap<Integer, Admin>) portletContext.getAttribute("admins");

        return adminMap.get(loggedOnUser.getId());
    }

    /**
     * Returns true if the logged in user is a master admin, false otherwise.
     *
     * @return boolean
     * @throws Exception
     */
    public boolean isLoggedInUserMasterAdmin() throws Exception {
        return getAdmin() != null && getAdmin().getIsMaster();
    }

    /**
     * Returns true if the logged in user is a supervisor, false otherwise.
     *
     * @return boolean
     * @throws Exception
     */
    public boolean isLoggedInUserSupervisor() throws Exception {
        PortletSession session = getSession();
        Boolean isSupervisor = (Boolean) session.getAttribute("isSupervisor");
        if (isSupervisor == null) {
            setUpUserPermission(false);
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
     * @return ArrayList<RequiredAction>
     * @throws Exception
     */
    public void setRequiredActions() throws Exception {
        ArrayList<RequiredAction> employeeRequiredActions;
        ArrayList<RequiredAction> administrativeActions = new ArrayList<RequiredAction>();
        ArrayList<Appraisal> myActiveAppraisals;
        ArrayList<Appraisal> mySupervisingAppraisals;

        myActiveAppraisals = (ArrayList<Appraisal>) getFromRequestMap("myActiveAppraisals");
        employeeRequiredActions = getAppraisalActions(myActiveAppraisals, "employee");
        addToRequestMap("employeeActions", employeeRequiredActions);

        // add supervisor required actions, if user has team's active appraisals
        if(getFromRequestMap("myTeamsActiveAppraisals") != null){

            mySupervisingAppraisals =
                    (ArrayList<Appraisal>) getFromRequestMap("myTeamsActiveAppraisals");
            administrativeActions =
                    getAppraisalActions(mySupervisingAppraisals, "supervisor");
        }

        Reviewer reviewer = getReviewer();
        if (reviewer != null) {
            String businessCenterName = reviewer.getBusinessCenterName();
            RequiredAction reviewerAction = getReviewerAction(businessCenterName);
            if (reviewerAction != null) {
                administrativeActions.add(reviewerAction);
            }
        }
        addToRequestMap("administrativeActions", administrativeActions);
    }

    /**
     * Returns a list of actions required for the given user and role, based on the
     * list of appraisals passed in. If the user and role have no appraisal actions,
     * it returns an empty ArrayList.
     *
     * @param appraisalList     List of appraisals to check for actions required
     * @param role              Role of the currently logged in user
     * @return  outList
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public ArrayList<RequiredAction> getAppraisalActions(List<Appraisal> appraisalList, String role)
            throws Exception {
        Configuration configuration;
        HashMap permissionRuleMap = (HashMap) portletContext.getAttribute("permissionRules");
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) portletContext.getAttribute("configurations");

        ArrayList<RequiredAction> outList = new ArrayList<RequiredAction>();
        RequiredAction actionReq;
        HashMap<String, String> anchorParams;

        for (Appraisal appraisal : appraisalList) {
            //get the status, compose the key "status"-"role"
            String appraisalStatus = appraisal.getStatus();
            String actionKey = appraisalStatus +"-"+role;
            actionKey = actionKey.replace("Overdue", "Due");

            // Get the appropriate permissionrule object from the permissionRuleMap
            PermissionRule rule = (PermissionRule) permissionRuleMap.get(actionKey);
            String actionRequired = "";
            if (rule != null) {
                actionRequired = rule.getActionRequired();
            }
            if (actionRequired != null && !actionRequired.equals("")) {
                // make sure that the action required is overdue if needed
                if (appraisalStatus.contains("Overdue")) {
                    actionRequired = actionRequired.replace("-due", "-overdue");
                }

                // compose a requiredAction object and add it to the outList.
                anchorParams = new HashMap<String, String>();
                anchorParams.put("action", "display");
                anchorParams.put("controller", "AppraisalsAction");
                String appraisalID = Integer.toString(appraisal.getId());
                anchorParams.put("id", appraisalID);
                if (appraisalStatus.equals(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION)) {
                    configuration = configurationMap.get(Appraisal.STATUS_GOALS_DUE);
                } else {
                    if (appraisalStatus.contains("Overdue")) {
                        appraisalStatus = appraisalStatus.replace("Overdue", "Due");
                    }

                    if (appraisalStatus.equals(Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED) ||
                            appraisalStatus.equals(Appraisal.STATUS_GOALS_REACTIVATED)) {
                        appraisalStatus += "Expiration";
                    }
                    configuration = configurationMap.get(appraisalStatus);
                }

                if (configuration == null) {
                    throw new ModelException(
                            "Could not find configuration object for status - " + appraisalStatus);
                }

                actionReq = new RequiredAction();
                actionReq.setParameters(anchorParams);
                actionReq.setAnchorText(actionRequired, appraisal, bundle, configuration);
                outList.add(actionReq);
            }
        }
        return outList;
    }

    /**
     * Returns the required action for the business center reviewer.
     *
     * @param businessCenterName
     * @return
     * @throws Exception
     */
    private RequiredAction getReviewerAction(String businessCenterName)
            throws Exception {
        int reviewCount;
        List<Appraisal> reviewList = getReviewsForLoggedInUser(-1);
        if (reviewList != null) {
            reviewCount = reviewList.size();
        } else {
            reviewCount = AppraisalMgr.getReviewCount(businessCenterName);
        }

        if (reviewCount == 0) {
            return null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        RequiredAction requiredAction = new RequiredAction();
        parameters.put("action", "reviewList");
        parameters.put("controller", "AppraisalsAction");
        requiredAction.setAnchorText("action-required-review", reviewCount, bundle);
        requiredAction.setParameters(parameters);

        return requiredAction;
    }

    /**
     * Sets the entries in requestMap in the RenderRequest object and clears the requestMap
     * afterwards.
     *
     * @param request
     */
    public void setRequestAttributes(RenderRequest request) throws Exception {
        addToRequestMap("currentRole", getCurrentRole());

        for (Map.Entry<String, Object> entry : requestMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        requestMap.clear();
    }

    /**
     * Add the data into requestMap and store the map into session.
     *
     * @param key
     * @param object
     * @return
     */
    public void addToRequestMap(String key, Object object) {
        requestMap.put(key, object);
    }

    /**
     * remove the requestMap in session
     *
     * @param key
     * @return object from requestMap searching from key
     */
    public Object getFromRequestMap(String key) {
        return requestMap.get(key);
    }

    /**
     * remove the requestMap in session
     *
     * @return
     */
    public void removeRequestMap() throws Exception {
        PortletSession session = getSession();
        session.removeAttribute(REQUEST_MAP);
    }

    /**
     * Returns the currentRole that the logged in user last selected. It
     * tries to grab it from the request and it stores it in session.
     *
     * @return
     */
    public String getCurrentRole() throws Exception{
        PortletSession session = getSession();
        String currentRole = ParamUtil.getString(request, "currentRole");

        if (currentRole.equals("")) {
            currentRole = (String) session.getAttribute("currentRole");
            if (currentRole == null || currentRole.equals("")){
                currentRole = ROLE_SELF;
            }
        }
        session.setAttribute("currentRole", currentRole);
        return currentRole;
    }

    /**
     * Whether the current instance of the portlet should enable demo features.
     * The hostname of the demo server is specified in default.properties.
     *
     * @return
     */
    public boolean isDemo() {
        PropertiesConfiguration config = getEvalsConfig();
        String demoHostname = config.getString("demo.hostname");
        String serverHostname = CWSUtil.getLocalHostname();

        return demoHostname.equals(serverHostname);
    }

    /**
     * Set up the attributes needed for the user switch used by the demo.
     *
     * @throws Exception
     */
    public void setupDemoSwitch() throws Exception {
        if (!isDemo()) {
            return;
        }

        // set Employee  and employees object(s)
        addToRequestMap("employees", EmployeeMgr.list());
        addToRequestMap("employee", loggedOnUser);
    }

    /**
     * fetch the latest notice from notice table and addToRequestMap as yellowBox message
     * @param
     * @return Text of yellowBox message
     * @throws Exception
     */
    private void setNotices() throws Exception {
        Map notices = NoticeMgr.getNotices();
        portletContext.setAttribute("Notices", notices);
    }

}




