package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.*;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.Mailer;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.configuration.CompositeConfiguration;

import javax.portlet.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ActionHelper class used to map user form actions to respective class methods.
 */
public class ActionHelper {
    public static final String ROLE_ADMINISTRATOR = "administrator";
    public static final String ROLE_REVIEWER = "reviewer";
    public static final String ROLE_SUPERVISOR = "supervisor";
    public static final String ROLE_SELF = "self";
    public static final String ALL_MY_ACTIVE_APPRAISALS = "allMyActiveAppraisals";
    public static final String MY_TEAMS_ACTIVE_APPRAISALS = "myTeamsActiveAppraisals";
    private static final String REVIEW_LIST = "reviewList";
    private static final String REVIEW_LIST_MAX_RESULTS = "reviewListMaxResults";
    public static final String REQUEST_MAP = "requestMap";

    private EmployeeMgr employeeMgr = new EmployeeMgr();

    private AdminMgr adminMgr = new AdminMgr();

    private ReviewerMgr reviewerMgr = new ReviewerMgr();

    private ConfigurationMgr configurationMgr = new ConfigurationMgr();

    private PortletContext portletContext;

    private PortletRequest request;

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

    private void setRequestMap() {
        PortletSession session = request.getPortletSession(true);
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
        PortletSession session = request.getPortletSession(true);
        List<Appraisal> allMyActiveAppraisals;

        allMyActiveAppraisals = (ArrayList<Appraisal>) session.getAttribute(ALL_MY_ACTIVE_APPRAISALS);
        if (allMyActiveAppraisals == null) {
            allMyActiveAppraisals = AppraisalMgr.getAllMyActiveAppraisals(loggedOnUser.getId(),
                    null, null);
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
        PortletSession session = request.getPortletSession(true);

        ArrayList<Appraisal> myTeamAppraisals;
        myTeamAppraisals = (ArrayList<Appraisal>) session.getAttribute(MY_TEAMS_ACTIVE_APPRAISALS);
        if (myTeamAppraisals == null) {
            myTeamAppraisals = AppraisalMgr.getMyTeamsAppraisals(loggedOnUser.getId(),
                    true, null, null);
            session.setAttribute(MY_TEAMS_ACTIVE_APPRAISALS, myTeamAppraisals);
        }
        return myTeamAppraisals;
    }

    public void setUpUserPermission(boolean refresh) throws Exception {
        PortletSession session = request.getPortletSession(true);

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
     * EvalsPortlet.portletSetup and by ReviewersAction.add methods.
     *
     * @param
     * @throws Exception
     */
    private void setEvalsReviewers() throws Exception {
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
     * EvalsPortlet.portletSetup and by ConfigurationsAction.edit methods.
     *
     * @param
     * @throws Exception
     */
    private void setEvalsConfiguration() throws Exception {
        portletContext.setAttribute("configurations", configurationMgr.mapByName());
        portletContext.setAttribute("configurationsList", configurationMgr.list());
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

        PortletSession session = request.getPortletSession(true);
        reviewList = (ArrayList<Appraisal>) session.getAttribute(REVIEW_LIST);
        session.setAttribute(REVIEW_LIST_MAX_RESULTS, maxResults);

        if (reviewList == null) { //No data yet, need to get it from the database.
            String businessCenterName = ParamUtil.getString(request, "businessCenterName");

            if (businessCenterName.equals("")) {
                businessCenterName = getReviewer().getBusinessCenterName();
            }
            AppraisalMgr appraisalMgr = new AppraisalMgr();
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
     * Setups up parameters from portletContext needed by AppraisalMgr class.
     *
     * @param appraisalMgr
     */
    public void setAppraisalMgrParameters(AppraisalMgr appraisalMgr) {
        HashMap permissionRules = (HashMap) portletContext.getAttribute("permissionRules");
        HashMap<Integer, Admin> admins = (HashMap<Integer, Admin>) portletContext.getAttribute("admins");
        HashMap<Integer, Reviewer> reviewers = (HashMap<Integer, Reviewer>) portletContext.getAttribute("reviewers");
        HashMap appraisalSteps = (HashMap) portletContext.getAttribute("appraisalSteps");
        Mailer mailer = (Mailer) portletContext.getAttribute("mailer");
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) portletContext.getAttribute("configurations");

        appraisalMgr.setPermissionRules(permissionRules);
        appraisalMgr.setLoggedInUser(loggedOnUser);
        appraisalMgr.setAdmins(admins);
        appraisalMgr.setReviewers(reviewers);
        appraisalMgr.setAppraisalSteps(appraisalSteps);
        appraisalMgr.setMailer(mailer);
        appraisalMgr.setConfigurationMap(configurationMap);

    }

    /**
     * Handles removing an appraisal from the reviewList stored in session. This method is called
     * by the AppraisalsAction.update method after a reviewer submits a review.
     *
     * @param appraisal
     * @throws Exception
     */
    public void removeReviewAppraisalInSession(Appraisal appraisal) throws Exception {
        List<Appraisal> reviewList = getReviewsForLoggedInUser(-1);
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

    /**
     * Checks if the context cache is outdated and refreshes the context cache:
     * admins, reviewers and configuration lists and maps. If the context cache is refreshed, it
     * updates the context cache timestamp in the portlet context.
     *
     * @throws Exception
     */
    public void refreshContextCache() throws Exception {
        Date contextCacheTimestamp = (Date) portletContext.getAttribute(EvalsPortlet.CONTEXT_CACHE_TIMESTAMP);
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
        PortletSession session = request.getPortletSession(true);
        loggedOnUser = (Employee) session.getAttribute("loggedOnUser");

        // if not in session, get it from db
        if (loggedOnUser == null) {
            loggedOnUser = employeeMgr.findByOnid(getLoggedOnUsername(), "employee-with-jobs");
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
    public String getLoggedOnUsername() {
        PortletSession session = request.getPortletSession(true);
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
        if (getAdmin() == null) {
            return false;
        }
        return getAdmin().getIsMaster();
    }

    /**
     * Returns true if the logged in user is a supervisor, false otherwise.
     *
     * @return boolean
     * @throws Exception
     */
    public boolean isLoggedInUserSupervisor() throws Exception {
        PortletSession session = request.getPortletSession(true);
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
        ArrayList<Appraisal> supervisorActions;
        RequiredAction reviewerAction;
        Reviewer reviewer;

        myActiveAppraisals = (ArrayList<Appraisal>) getFromRequestMap("myActiveAppraisals");
        employeeRequiredActions = getAppraisalActions(myActiveAppraisals, "employee");
        addToRequestMap("employeeActions", employeeRequiredActions);

        // add supervisor required actions, if user has team's active appraisals
        if(getFromRequestMap("myTeamsActiveAppraisals") != null){
            supervisorActions = (ArrayList<Appraisal>) getFromRequestMap("myTeamsActiveAppraisals");
            administrativeActions = getAppraisalActions(supervisorActions, "supervisor");
        }

        reviewer = getReviewer();
        if (reviewer != null) {
            String businessCenterName = reviewer.getBusinessCenterName();
            reviewerAction = getReviewerAction(businessCenterName);
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
    public ArrayList<RequiredAction> getAppraisalActions(List<Appraisal> appraisalList,
                                                         String role) throws Exception {
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
                anchorParams.put("action", "display");
                anchorParams.put("controller", "AppraisalsAction");
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
            AppraisalMgr appraisalMgr = new AppraisalMgr();
            reviewCount = appraisalMgr.getReviewCount(businessCenterName);
        }

        RequiredAction requiredAction = new RequiredAction();
        if (reviewCount == 0) {
            return null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
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
    public void setRequestAttributes(RenderRequest request) {
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
    public void removeRequestMap(){
        PortletSession session = request.getPortletSession(true);
        session.removeAttribute(REQUEST_MAP);
    }

    /**
     * Returns the currentRole that the logged in user last selected. It
     * tries to grab it from the request and it stores it in session.
     *
     * @return
     */
    public String getCurrentRole() {
        PortletSession session = request.getPortletSession(true);
        String currentRole = ParamUtil.getString(request, "currentRole");

        if (!currentRole.equals("")) {
            session.setAttribute("currentRole", currentRole);
        } else {
            currentRole = (String) session.getAttribute("currentRole");
            if (currentRole == null || currentRole.equals("")){
                currentRole = ROLE_SELF;
            }
            session.setAttribute("currentRole", currentRole);
        }
        return currentRole;
    }

    /**
     * Whether the current instance of the portlet should enable demo features.
     * The hostname of the demo server is specified in default.properties.
     *
     * @return
     */
    public boolean isDemo() {
        CompositeConfiguration config = (CompositeConfiguration)
                getPortletContextAttribute("environmentProp");
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




