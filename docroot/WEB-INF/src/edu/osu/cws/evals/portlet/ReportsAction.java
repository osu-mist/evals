package edu.osu.cws.evals.portlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.util.Breadcrumb;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.portlet.*;
import java.lang.reflect.Type;
import java.util.*;

public class ReportsAction implements ActionInterface {

    private static Log _log = LogFactoryUtil.getLog(ReportsAction.class);
    public static final String SCOPE = "scope";
    public static final String SCOPE_VALUE = "scopeValue";
    public static final String SEARCH_TERM = "searchTerm";
    public static final String BREADCRUMB_LIST = "breadcrumbList";

    public static final String DEFAULT_SCOPE = "root";
    public static final String DEFAULT_SCOPE_VALUE = "OSU";
    public static final String SCOPE_BC = "bc";
    public static final String SCOPE_ORG_PREFIX = "orgPrefix";
    public static final String SCOPE_ORG_CODE = "orgCode";
    public static final String SCOPE_SUPERVISOR = "supervisor";

    // The various types of reports supported
    public static final String REPORT = "report";
    public static final String REPORT_DEFAULT = "unitBreakdown";
    public static final String REPORT_UNIT_BREAKDOWN = "unitBreakdown";
    public static final String REPORT_UNIT_OVERDUE = "unitOverdue";
    public static final String REPORT_UNIT_WAYOVERDUE = "unitWayOverdue";
    public static final String REPORT_STAGE_BREAKDOWN = "stageBreakdown";
    public static final String REPORT_STAGE_OVERDUE = "stageOverdue";
    public static final String REPORT_STAGE_WAYOVERDUE = "stageWayOverdue";

    public static final String CHART_TYPE_DEFAULT = "pie";
    public static final String CHART_TYPE_BAR = "bar";
    public static final String CHART_TYPE_COLUMN = "column";

    public static String[] APPOINTMENT_TYPES = {"Classified"};
    public static String[] DRILL_DOWN_INDEX = {
            DEFAULT_SCOPE,
            SCOPE_BC,
            SCOPE_ORG_PREFIX,
            SCOPE_ORG_CODE,
            SCOPE_SUPERVISOR
    };
    public static final String BREADCRUMB_INDEX = "breadcrumbIndex";
    public static final String CHART_TYPE = "chartType";

    private List<Breadcrumb> breadcrumbList = new ArrayList<Breadcrumb>();

    private boolean enableByUnitReports = true;

    private ActionHelper actionHelper;
    private HomeAction homeAction;

    /**
     * Map used to store drilldown, and search options used to fetch
     * data.
     * *scope: root (default), bc, orgPrefix, orgCode, employee
     * scopeValue: related to scope: osu, uabc, mum, 123456, 943232
     * breadCrumbIndex: numeric index of the breadcrumb clicked
     * searchTerm: the searchTerm the user entered, either osuid, name or orgCode
     * bcName:  the name of the bcName the report should filter on
     * breadcrumbList: The breadcrumbs that should be used: request, session, or default osu
     */
    private HashMap paramMap = new HashMap();

    private List<Appraisal> listAppraisals;
    private List<Object[]> tableData;
    private List<Job> searchResults = new ArrayList<Job>();

    /**
     * Format:
     * {
     *     // # of evals, displayValue, scopeValue
     *     5, 'UABC', 'UABC' // when scope is bc
     *     5, 'CLA', 'CLA' // when scope is orgPrefix
     *     5, '1234', '1234' // when scope is orgCode
     *     5, 'Bond, James', 'pidm_posno_suffix' // when scope is supervisor
     *
     * }
     */
    private List<Object[]> chartData;

    private List<Object[]> drillDownData;

    /**
     * Holds the job of the current supervisor level in the supervisor report.
     * Set in setParamMap()
     */
    private Job currentSupervisorJob;

    /**
     * Holds list of appraisals of direct employees of current supervisor
     */
    private ArrayList<Appraisal> supervisorTeamAppraisal;

    /**
     * Holds list of appraisals of current supervisor
     */
    private ArrayList<Appraisal> supervisorAppraisals;

    /**
     * Holds list of appraisals of direct employees of current supervisor in classfied IT
     */

    private ArrayList<String[]> supervisorClassifiedITAppraisal;

    private boolean inLeafSupervisorReport = false;

    /**
     * Specifies if the current supervisor report is the report of the logged in user
     */
    private boolean isMyReport = false;

    /**
     * Whether or not the current page we're viewing is the appraisal search. Meaning
     * listing the appraisals for a single employee.
     */
    private boolean isAppraisalSearch = false;

    Gson gson = new Gson();

    private Breadcrumb rootBreadcrumb = new Breadcrumb("OSU", DEFAULT_SCOPE, DEFAULT_SCOPE_VALUE);

    /**
     * Main method used in this class. It responds to the user request when the user is viewing
     * any kind of report.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String report(PortletRequest request, PortletResponse response) throws Exception {
        PortletSession session = request.getPortletSession();

        // parse the various parameters from request and session
        setParamMap(request);

        processReport(request, response);
        setupDataForJSP(request);
        actionHelper.useMaximizedMenu(request);

        // Save session values only if we didn't throw an exception before we got here.
        session.setAttribute(BREADCRUMB_LIST, breadcrumbList);
        session.setAttribute("paramMap", paramMap);

        return Constants.JSP_REPORT;
    }

    /**
     * Main process method that handles calling the search, active report, permission checks and
     * breadcrumbs.
     *
     * @param request
     * @param response
     * @throws Exception
     */
    private void processReport(PortletRequest request, PortletResponse response) throws Exception {
        boolean displaySearchResults = false;
        // if the user searched handle it
        String searchTerm = getSearchTerm();
        if (!searchTerm.equals("")) {
            displaySearchResults = search(searchTerm, request);
        }

        setIsMyReport(request);
        breadcrumbList = getBreadcrumbs();
        // set the bc name using the scope value of the 2nd breadcrumb if applicable
        if (!displaySearchResults && paramMap.get(Constants.BC_NAME) == null) {
                setBcName();
        }

        if (!canViewReport(request)) {
            accessDeniedReset(request);
            displaySearchResults = false;
        }

        if (!displaySearchResults) {
            if (getScope().equals(SCOPE_SUPERVISOR)) {
                rightPaneData(request);
            }

            // Check if we are viewing a supervisor report of a mere employee
            boolean displayAppraisalSearchList = false;
            if (getScope().equals(SCOPE_SUPERVISOR)) {
                String posno = currentSupervisorJob.getPositionNumber();
                int pidm = currentSupervisorJob.getEmployee().getId();
                displayAppraisalSearchList = !JobMgr.isSupervisor(pidm, posno);
            }

            if (displayAppraisalSearchList) { // display appraisal list of single employee
                searchResults = new ArrayList<Job>();
                searchResults.add(currentSupervisorJob); // add single employee as the result
                isAppraisalSearch = activeAppraisalList(request, response);
                if (!isAppraisalSearch) { //employee had no evaluations
                    String prevSearchTerm = ParamUtil.getString(request, "prevSearchTerm");
                    if (!StringUtils.isEmpty(prevSearchTerm)) { // go back to previous search
                        paramMap.put(SEARCH_TERM, prevSearchTerm);
                    } else {
                        // remove the search term so that we can load the previous report
                        paramMap.put(SEARCH_TERM, "");
                        paramMap.put(SCOPE, ParamUtil.getString(request, SCOPE));
                        paramMap.put(SCOPE_VALUE, ParamUtil.getString(request, SCOPE_VALUE));
                        paramMap.put(REPORT, ParamUtil.getString(request, REPORT));
                        List<Breadcrumb> prevCrumbs = jsonToBreadcrumbList(request, "prevCrumbs");
                        paramMap.put(BREADCRUMB_LIST, prevCrumbs);
                        setCurrentSupervisor();
                    }
                    processReport(request, response);
                }
            } else {
                activeReport();
            }
        }
    }

    /**
     * Resets the paramMap values to the default report and sets the message of
     * access denied to the user.
     *
     * @param request
     */
    private void accessDeniedReset(PortletRequest request) {
        paramMap.put(SCOPE, DEFAULT_SCOPE);
        paramMap.put(SCOPE_VALUE, DEFAULT_SCOPE_VALUE);
        breadcrumbList = new ArrayList<Breadcrumb>();
        breadcrumbList.add(rootBreadcrumb);
        paramMap.put(SEARCH_TERM, "");
        paramMap.put(REPORT, REPORT_DEFAULT);
        searchResults.clear();

        actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
    }

    private void setupDataForJSP(PortletRequest request) throws Exception {
        boolean showDrillDownMenu = false;
        String searchTerm = getSearchTerm();
        boolean displaySearchResultsPage = displaySearchResultsPage();

        if (isAppraisalSearch) { // display search result of employee appraisals
            actionHelper.addToRequestMap("listAppraisals", listAppraisals, request);
            actionHelper.addToRequestMap("isAppraisalSearch", isAppraisalSearch, request);
        } else if (!displaySearchResultsPage) { // regular active report
            // chart related data
            actionHelper.addToRequestMap("chartData", chartData, request);
            actionHelper.addToRequestMap("tableData", tableData, request);
            actionHelper.addToRequestMap("drillDownData", drillDownData, request);
            actionHelper.addToRequestMap("listAppraisals", listAppraisals, request);

            // parameter related data
            String scope = getScope();
            String scopeValue = getScopeValue();
            actionHelper.addToRequestMap("scope", scope, request);
            actionHelper.addToRequestMap("scopeValue", scopeValue, request);
            actionHelper.addToRequestMap("report", paramMap.get(REPORT), request);
            actionHelper.addToRequestMap("reportTitle", ReportMgr.getReportTitle(paramMap), request);
            actionHelper.addToRequestMap("reportHeader", ReportMgr.getReportHeader(paramMap), request);
            actionHelper.addToRequestMap("chartType", getChartType(request), request);
            if (scope.equals(SCOPE_ORG_CODE)) {
                enableByUnitReports = false;
            }
            actionHelper.addToRequestMap("enableByUnitReports", enableByUnitReports, request);

            if (scope.equals(SCOPE_SUPERVISOR)) {
                // right pane data: supervisor appraisals and supervisor team and supervisor classified IT
                actionHelper.addToRequestMap("myActiveAppraisals", supervisorAppraisals, request);
                actionHelper.addToRequestMap("myTeamsActiveAppraisals", supervisorTeamAppraisal, request);
                actionHelper.addToRequestMap("myClassifiedITAppraisals", supervisorClassifiedITAppraisal, request);
                for (String str[] : supervisorClassifiedITAppraisal){
                     _log.error("user name: " + str[0]);
                     _log.error("review: " + str[1]);
                }
                actionHelper.addToRequestMap("isMyReport", isMyReport, request);
            }

            // breadcrumb and drill down data
            String nextScope = nextScopeInDrillDown(scope);
            actionHelper.addToRequestMap("nextScope", nextScope, request);

            boolean allowAllDrillDown = false;
            if (actionHelper.isLoggedInUserAdmin(request)) {
                allowAllDrillDown = true;
            }
            actionHelper.addToRequestMap("allowAllDrillDown", allowAllDrillDown, request);

            String reviewerBCName = "";
            if (actionHelper.isLoggedInUserReviewer(request)) {
                int employeeID = actionHelper.getLoggedOnUser(request).getId();
                reviewerBCName = actionHelper.getReviewer(employeeID).getBusinessCenterName();
            }
            actionHelper.addToRequestMap("reviewerBCName", reviewerBCName, request);

            showDrillDownMenu = showDrillDownMenu(allowAllDrillDown, reviewerBCName);

            actionHelper.addToRequestMap("chartDataScopeMap", chartDataScopeMap(), request);

            if (currentSupervisorJob != null) {
                String currentSupervisorName = currentSupervisorJob.getEmployee().getName();
                actionHelper.addToRequestMap("currentSupervisorName", currentSupervisorName, request);
            }
        } else { // displaying search results - multiple jobs
            actionHelper.addToRequestMap("searchResults", searchResults, request);
        }

        actionHelper.addToRequestMap("now", new Date(), request);
        actionHelper.addToRequestMap("searchTerm", searchTerm, request);
        actionHelper.addToRequestMap("breadcrumbList", breadcrumbList,request);
        actionHelper.addToRequestMap("requestBreadcrumbs", getRequestBreadcrumbs(), request);

        actionHelper.addToRequestMap("showDrillDownMenu", showDrillDownMenu, request);
        actionHelper.addToRequestMap("searchView", displaySearchResultsPage, request);

        // Error String messages used by search js
        ResourceBundle resource = (ResourceBundle) actionHelper
                .getPortletContextAttribute("resourceBundle");
        actionHelper.addToRequestMap("searchJsErrorDefault",
                resource.getString("report-search-js-validation-default"), request);
        actionHelper.addToRequestMap("searchJsErrorSupervisor",
                resource.getString("report-search-js-validation-supervisor"), request);

        // My Report data
        showMyReportLink(request);

        // list of breadcrumbs used when the request only needs OSU as the breadcrumbs
        List<Breadcrumb> crumbListWithRootOnly = new ArrayList<Breadcrumb>();
        crumbListWithRootOnly.add(rootBreadcrumb);
        String breadcrumbsWithRootOnly = gson.toJson(crumbListWithRootOnly);
        actionHelper.addToRequestMap("breadcrumbsWithRootOnly", breadcrumbsWithRootOnly, request);
    }

    private String nextScopeInDrillDown(String currentScope) {
        int currentScopeIndex = ArrayUtils.indexOf(DRILL_DOWN_INDEX, currentScope);
        int nextDrillDownScope = currentScopeIndex;
        if (currentScopeIndex < DRILL_DOWN_INDEX.length -1) {
            nextDrillDownScope++;
        }
        return (String) DRILL_DOWN_INDEX[nextDrillDownScope];
    }

    private boolean activeReport() throws Exception {
        List<Job> directEmployees = null;
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) actionHelper.getPortletContextAttribute("configurations");
        Configuration config = configurationMap.get("reportMaxDataForCharts");
        int maxDataPoints = Integer.parseInt(config.getValue());

        if (getScope().equals(ReportsAction.SCOPE_SUPERVISOR)) {
            boolean allowUnitReport = true;
            Job supervisorJob = Job.getJobFromString(getScopeValue());
            if (supervisorJob != null) {
                if (JobMgr.isBottomLevelSupervisor(supervisorJob)) {
                    allowUnitReport = false;
                    inLeafSupervisorReport = true;
                }

                boolean supervisorsOnly = !inLeafSupervisorReport;
                directEmployees = JobMgr.getDirectEmployees(supervisorJob, supervisorsOnly);
            }

            // If the supervisor is the bottom level supervisor, can't use by unit reports
            if (!allowUnitReport) {
                String report = (String) paramMap.get(REPORT);
                if (report.contains(ReportMgr.UNIT)) {
                    paramMap.put(REPORT, REPORT_STAGE_BREAKDOWN);
                }
                enableByUnitReports = false;
            }
        }
        tableData = ReportMgr.getChartData(paramMap, true, directEmployees,
                supervisorTeamAppraisal, currentSupervisorJob, inLeafSupervisorReport);
        chartData = ReportMgr.trimDataPoints(tableData, maxDataPoints);
        setDrillDownData(directEmployees, inLeafSupervisorReport);


        if (shouldListAppraisals()) {
            listAppraisals = AppraisalMgr.getReportListData(paramMap, directEmployees,
                    inLeafSupervisorReport);
        }

        return true;
    }

    /**
     * Fetches from the db the data needed for the right pane for supervisors:
     * my evaluations and myTeam evaluations.
     *
     * @param request
     * @throws Exception
     */
    private void rightPaneData(PortletRequest request) throws Exception {
        int supervisorLevelPidm = currentSupervisorJob.getEmployee().getId();
        String supervisorLevelPosno = currentSupervisorJob.getPositionNumber();
        String supervisorLevelSuffix = currentSupervisorJob.getSuffix();
        supervisorTeamAppraisal = AppraisalMgr.getMyTeamsAppraisals(supervisorLevelPidm,
                true, supervisorLevelPosno, supervisorLevelSuffix);
        supervisorAppraisals = AppraisalMgr.getAllMyActiveAppraisals(supervisorLevelPidm,
                supervisorLevelPosno, supervisorLevelSuffix);
        supervisorClassifiedITAppraisal = AppraisalMgr.getMyClassifiedITAppriasal(supervisorLevelPidm);
    }

    /**
     * Displays just the list of appraisals for a single employee. This is used when the user
     * searches for a single non-supervisor employee and we display the appraisals of the
     * search result employee. If the employee doesn't have any appraisals, we show an error
     * message to the user telling them about this.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    private boolean activeAppraisalList(PortletRequest request, PortletResponse response)
            throws Exception {
        listAppraisals = AppraisalMgr.getEmployeeAppraisalList(searchResults);

        // Check if the user had no evaluation records
        if (listAppraisals == null || listAppraisals.isEmpty()) {
            ResourceBundle resource = (ResourceBundle) actionHelper
                    .getPortletContextAttribute("resourceBundle");

            String errorMsg = resource.getString("report-search-no-results-no-evals");
            actionHelper.addErrorsToRequest(request, errorMsg);
            return false;
        }

        // when showing the evaluation list after clicking on a search result, set the searchTerm
        if (getSearchTerm().equals("")) {
            Breadcrumb lastBreadcrumb = breadcrumbList.get(breadcrumbList.size() - 1);
            paramMap.put(SEARCH_TERM, lastBreadcrumb.getAnchorText());
        }

        return true;
    }

    /**
     *  Set up the drill down array of data. If the user is viewing the default unit report, we
     *  just copy the data and remove the current supervisor since he can't drill into himself. If
     *  we are in another type of report, we do a db query to get the drill down list.
     *
     * @param directSupervisors
     * @param inLeafSupervisor
     */
    private void setDrillDownData(List<Job> directSupervisors, boolean inLeafSupervisor) {
        // The drill down data is the same as the report by unit (overdue may not have all units)
        String report = (String) paramMap.get(REPORT);
        if (report.equals(REPORT_DEFAULT)) {
            drillDownData = new ArrayList<Object[]>();
            drillDownData.addAll(tableData);

            // For the supervisor reports, if the 1st slice is the current supervisor, we remove it
            // from the drill down.
            if (getScope().equals(SCOPE_SUPERVISOR)) {
                Object[] firstRow = drillDownData.get(0);
                if (firstRow.length > 1) {
                    String scopeValue = firstRow[2].toString();
                    if (currentSupervisorJob.getIdKey().equals(scopeValue)) {
                        drillDownData.remove(0);
                    }
                }
            }
        } else {
            drillDownData = ReportMgr.getDrillDownData(paramMap, false, directSupervisors,
                    inLeafSupervisor);
        }
    }

    /**
     * Whether or not we should be listing the evaluation records that match the paramMap
     * search criteria.
     *
     * Right now, we display the data list when:
     * 1) the scope level is orgCode
     * 2) if the # of evaluation records is less than a configuration value
     *
     * @return
     */
    private boolean shouldListAppraisals() {
        if (getScope().equals(SCOPE_ORG_CODE)) {
            return true;
        }

        int numberOfEvalRecords = 0;
        Map<String, Configuration> configurationMap =
                (Map<String, Configuration>) actionHelper.getPortletContextAttribute("configurations");
        Configuration config = configurationMap.get("reportMaxDataForList");
        int maxDataForList = Integer.parseInt(config.getValue());

        for (Object[] row : tableData) {
            numberOfEvalRecords += Integer.parseInt(row[0].toString());
        }

        return numberOfEvalRecords <= maxDataForList;
    }

    /**
     * Handles looking at the breadcrumbs currently stored and by
     * looking at the paramMap, it changes the breadcrumbs and returns
     * them as a list.
	 * There are 3 possibilities:
     *    1. return just the initial one (osu)
     *    2. return startCrumbs  (not changed)
	 *    3. return startCrumbs.add(new Breadcumb(...))
     *    4. if somebody clicks on a previous scope of the breadcrumb, we need to remove
     *    the rest of the scopes down the chain.
     *
     * @return
     */
    private List<Breadcrumb> getBreadcrumbs() {
        List<Breadcrumb> startCrumbs = (List<Breadcrumb>) paramMap.get(BREADCRUMB_LIST);
        List<Breadcrumb> crumbs = new ArrayList<Breadcrumb>();

        // Initial user click to reports
        String scope = getScope();
        String scopeValue = getScopeValue();

        boolean hasMultipleSearchResults = false;
        if (searchResults != null && searchResults.size() > 1) {
            hasMultipleSearchResults = true;
        }
        if (paramMap.isEmpty() || scope.equals(DEFAULT_SCOPE) || hasMultipleSearchResults ||
                startCrumbs == null || startCrumbs.isEmpty()) {
            crumbs.add(rootBreadcrumb);
            return crumbs;
        }

        int breadcrumbIndex = (Integer) paramMap.get("breadcrumbIndex");
        boolean clickedCrumb = breadcrumbIndex != -1 && breadcrumbIndex < startCrumbs.size() - 1;
        if (clickedCrumb) {
            crumbs = startCrumbs.subList(0, breadcrumbIndex+1);
        } else {
            crumbs.addAll(startCrumbs);

            // Figure out if the user selected a different report, within the same scope
            Breadcrumb lastBreadcrumb = startCrumbs.get(startCrumbs.size() - 1);
            String scopeValueOfLastCrumb = lastBreadcrumb.getScopeValue();
            boolean sameScopeValue = scopeValue.equals(scopeValueOfLastCrumb);

            if (!sameScopeValue) {
                String anchorText = scopeValue;
                if (scope.equals(SCOPE_SUPERVISOR)) {
                    anchorText = currentSupervisorJob.getEmployee().getName();
                }

                Breadcrumb crumb = new Breadcrumb(anchorText, scope, scopeValue);
                crumbs.add(crumb);
            }
        }

        return crumbs;
    }

    private boolean displaySearchResultsPage() {
        return searchResults != null && searchResults.size() > 1;
    }

    /**
     * Returns the scope value of the breadcrumbList separated by a space.
     *
     * @return
     */
    private String getRequestBreadcrumbs() {
        return gson.toJson(breadcrumbList);
    }

    private String getScopeValue() {
        return (String) paramMap.get(SCOPE_VALUE);
    }

    private String getScope() {
        return (String) paramMap.get(SCOPE);
    }

    private String getSearchTerm() {
        return (String) paramMap.get(SEARCH_TERM);
    }

    private String getChartType(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        String chartType = (String) session.getAttribute(CHART_TYPE);
        if (chartType == null || chartType.equals("")) {
            chartType = CHART_TYPE_DEFAULT;
        }

        return chartType;
    }

    /**
     * Looks at the values passed in the request, the session values and the
     * default ones. First it tries to use the request, if the values are not
     * found there, it tries session. Last it uses the default value.
     *
     * @param request
     */
    private void setParamMap(PortletRequest request) throws Exception {
        PortletSession session = request.getPortletSession();
        HashMap sessionParam = (HashMap) session.getAttribute("paramMap");
        if (sessionParam == null) {
            sessionParam = new HashMap();
        }

        String requestScope = ParamUtil.getString(request, SCOPE);
        String requestScopeValue = ParamUtil.getString(request, SCOPE_VALUE);
        String sessionScope = (String) sessionParam.get(SCOPE);
        String sessionScopeValue = (String) sessionParam.get(SCOPE_VALUE);
        if (!requestScope.equals("") && !requestScopeValue.equals("")) {
            paramMap.put(SCOPE, requestScope);
            paramMap.put(SCOPE_VALUE, requestScopeValue);
        } else if (sessionScope != null && sessionScopeValue != null) {
            paramMap.put(SCOPE, sessionScope);
            paramMap.put(SCOPE_VALUE, sessionScopeValue);
        } else {
            paramMap.put(SCOPE, DEFAULT_SCOPE);
            paramMap.put(SCOPE_VALUE, DEFAULT_SCOPE_VALUE);
        }

        String requestReport = ParamUtil.getString(request, REPORT);
        String sessionReport = (String) sessionParam.get(REPORT);
        if (!requestReport.equals("") ) {
            paramMap.put(REPORT, requestReport);
        } else if (sessionReport != null) {
            paramMap.put(REPORT, sessionReport);
        } else {
            paramMap.put(REPORT, REPORT_DEFAULT);
        }

        String searchTerm = ParamUtil.getString(request, "searchTerm");
        if(searchTerm != null){
            _log.error("the untrimed searchTerm is: " + searchTerm);
        }
        searchTerm = StringUtils.trim(searchTerm);{
            _log.error("the trimed searchTerm is:" + searchTerm);
        }
        paramMap.put(SEARCH_TERM, searchTerm);

        setOrgCodeReportType();

        // The inex of the breadcrumb the user clicked on
        int breadcrumbIndex = ParamUtil.getInteger(request, BREADCRUMB_INDEX, -1);
        paramMap.put(BREADCRUMB_INDEX, breadcrumbIndex);

        // The list of breadcrumbs
        List<Breadcrumb> reqCrumbsList = jsonToBreadcrumbList(request, "requestBreadcrumbs");
        List<Breadcrumb> sessCrumbs = (List<Breadcrumb>) sessionParam.get(BREADCRUMB_LIST);

        if (reqCrumbsList != null && !reqCrumbsList.isEmpty()) {
            paramMap.put(BREADCRUMB_LIST, reqCrumbsList);
        } else if (sessCrumbs != null && !sessCrumbs.isEmpty()) {
            paramMap.put(BREADCRUMB_LIST, sessCrumbs);
        } else {
            List<Breadcrumb> defaultCrumbs = new ArrayList<Breadcrumb>();
            defaultCrumbs.add(rootBreadcrumb);
            paramMap.put(BREADCRUMB_LIST, defaultCrumbs);
        }

        setCurrentSupervisor();
    }

    /**
     * Converts a string in the request from json to a list of breadcrumbs.
     *
     * @param request       PortletRequest
     * @param fieldName     Name of the parameter in the request that holds the json string
     * @return
     */
    private List<Breadcrumb> jsonToBreadcrumbList(PortletRequest request, String fieldName) {
        String jsonBreadcrumbs = ParamUtil.getString(request, fieldName);
        Type collectionType = new TypeToken<Collection<Breadcrumb>>(){}.getType();
        return gson.fromJson(jsonBreadcrumbs, collectionType);
    }

    /**
     * Sets current supervisor from the scopeValue.
     *
     * @throws Exception
     */
    private void setCurrentSupervisor() throws Exception {
        if (getScope().equals(SCOPE_SUPERVISOR)) {
            Job tempJob = Job.getJobFromString(getScopeValue());
            //@todo: need to handle bad supervising data
            currentSupervisorJob = JobMgr.getJob(tempJob.getEmployee().getId(),
                    tempJob.getPositionNumber(), tempJob.getSuffix());
        }
    }

    private void setOrgCodeReportType() {
        // If the user is about to enter the org code scope and the chart is by Unit, use stage
        String selectedReport = (String) paramMap.get(REPORT);
        String selectedScope = (String) paramMap.get(SCOPE);
        if (selectedScope.equals(SCOPE_ORG_CODE) && selectedReport.contains(ReportMgr.UNIT)) {
            paramMap.put(REPORT,  REPORT_STAGE_BREAKDOWN);
        }
    }

    /**
     * Handles an ajax request made by the user to save the chart type when they change to
     * a different chart type.
     *
     * @param request
     * @param response
     * @return
     */
    public String saveChartType(PortletRequest request, PortletResponse response) {
        String chartType = ParamUtil.getString(request, "chartType", CHART_TYPE_DEFAULT);

        boolean allowedChartType = chartType.equals(CHART_TYPE_BAR)
                || chartType.equals(CHART_TYPE_COLUMN) || chartType.equals(CHART_TYPE_DEFAULT);

        if (allowedChartType)   {
            PortletSession session = request.getPortletSession();

            try {
                session.setAttribute(CHART_TYPE, chartType);
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return "";
        }

        return "success";
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

    /**
     * Checks the permissions of the logged in user to determine if they can view
     * the current report they are requesting.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public boolean canViewReport(PortletRequest request) throws Exception {
        if (actionHelper.isLoggedInUserAdmin(request)) {
            return true;
        }

        if (getScope().equals(DEFAULT_SCOPE) && !displaySearchResultsPage()) {
            return true;
        }

        int employeeID = actionHelper.getLoggedOnUser(request).getId();
        boolean supervisorReport = getScope().equals(SCOPE_SUPERVISOR);
        boolean isReviewer = actionHelper.isLoggedInUserReviewer(request);
        boolean isSupervisor = actionHelper.isLoggedInUserSupervisor(request);
        String searchTerm = getSearchTerm();

        // Search permission checks
        boolean searchingOrgCode = CWSUtil.validateOrgCode(searchTerm);
        if (!searchTerm.equals("")) {
            // bc reviewer and admin are the only ones that can search by org code
            if (searchingOrgCode && isReviewer) {
                return true;
            } else if (!searchingOrgCode && (isReviewer || isSupervisor)) {
                // bc reviewer, admin or supervisor can search by name or osu id
                return true;
            }
        }

        if (isReviewer) {
            String bcName = actionHelper.getReviewer(employeeID).getBusinessCenterName();

            // the bc reviewer can drill down the report if supervisor is in his bc
            if (supervisorReport && currentSupervisorJob.getBusinessCenterName().equals(bcName)) {
                return true;
            }

            // the bc reviewer can drill down to orgPrefix or orgCode if they are in the same bc
            if (breadcrumbList.size() > 1) {
                Breadcrumb bcBreadcrumb = breadcrumbList.get(1);
                if (bcBreadcrumb.getScope().equals(SCOPE_BC) &&
                        bcBreadcrumb.getScopeValue().equals(bcName)) {
                    return true;
                } else {
                    // check if the user loaded an org code direcly into the url
                    boolean allowedOrgCode = JobMgr.findOrgCode(getScopeValue(), bcName);
                    if (allowedOrgCode) {
                        return true;
                    }
                }
            }
        }

        // Check supervisor report to see if it's the current user's report or in her
        // supervising chain
        if (supervisorReport) {
            JobMgr jobMgr = new JobMgr();

            if (isMyReport) {
                return true;
            }

            if (jobMgr.isUpperSupervisor(currentSupervisorJob, employeeID)) {
                return true;
            }
        }
        return false;
    }

    //check if the loggedOnUser a supervisor, if he is, active "My Report" Menu
    private void setIsMyReport(PortletRequest request) throws Exception {
        if (getScope().equals(SCOPE_SUPERVISOR)) {
            int employeeID = actionHelper.getLoggedOnUser(request).getId();
            isMyReport = currentSupervisorJob.getEmployee().getId() == employeeID;
        }
    }

    /**
     * Whether or not the drill down menu should be displayed in the charts.
     *
     * @param allowAllDrillDown
     * @param reviewerBCName
     * @return
     */
    private boolean showDrillDownMenu(boolean allowAllDrillDown, String reviewerBCName) {
        // We don't allow drill down in lowest supervisor level
        if (inLeafSupervisorReport) {
            return false;
        }

        //scope != 'orgCode' && (allowAllDrillDown || reviewerBCName != '')
        if (!getScope().equals(SCOPE_ORG_CODE) &&
                (allowAllDrillDown || !reviewerBCName.equals(""))) {
            return true;
        }

        if (getScope().equals(SCOPE_SUPERVISOR)) {
            return true;
        }

        return false;
    }

    /**
     * Whether or not the my report link should be displayed. It also passes to the jsp
     * the needed values to generate the my report links.
     *
     * @param request
     * @return
     * @throws Exception
     */
    private void showMyReportLink(PortletRequest request) throws Exception {
        boolean showMyReportLink = false;

        if (actionHelper.isLoggedInUserSupervisor(request)) {
            // We make the assumption that a person has only 1 supervising job
            Employee loggedInUser = actionHelper.getLoggedOnUser(request);
            Job supervisorJob = JobMgr.getSupervisingJob(loggedInUser.getId());
            String supervisorJobTitle = supervisorJob.getJobTitle();
            String myReportSupervisorKey = supervisorJob.getEmployee().getId() + "_" +
                    supervisorJob.getPositionNumber() + "_" + supervisorJob.getSuffix();

            showMyReportLink = true;
            actionHelper.addToRequestMap("supervisorJobTitle", supervisorJobTitle, request);
            actionHelper.addToRequestMap("myReportSupervisorKey", myReportSupervisorKey, request);
        }

        if (actionHelper.isLoggedInUserReviewer(request)) {
            String myReportBcName = actionHelper.getBusinessCenterForLoggedInReviewer(request);
            showMyReportLink = true;
            actionHelper.addToRequestMap("myReportBcName", myReportBcName,request);
        }

        if (actionHelper.isLoggedInUserAdmin(request)) {
            showMyReportLink = true;
        }

        actionHelper.addToRequestMap("showMyReportLink", showMyReportLink,request);
    }

    /**
     * Returns a string representation of a map that helps look up the scope value
     * based on the display value of the chart data. This is needed for chart drill
     * down.
     *
     * @return
     */
    private String chartDataScopeMap() {
        String report = (String) paramMap.get(REPORT);
        if (report.contains(ReportMgr.STAGE)) {
            return "{}";
        }

        HashMap<String, String> dataScopeMap = new HashMap<String, String>();
        for (Object[] row : chartData) {
            String displayValue = "";
            String scopeValue = "";
            if(row[1]!= null){
                displayValue = row[1].toString();
                scopeValue = row[1].toString();
            }
            if (row.length == 3) {
                if(row[2] != null){
                    scopeValue = row[2].toString();
                }
            }
            dataScopeMap.put(displayValue, scopeValue);
            }
        return gson.toJson(dataScopeMap);
    }

    /**
     * Returns true, if we have 2 or more search results to display. Otherwise it returns
     * false.
     *
     * @param searchTerm
     * @param request
     * @return
     * @throws Exception
     */
    private boolean search(String searchTerm, PortletRequest request) throws Exception {
        ResourceBundle resource = (ResourceBundle) actionHelper
                .getPortletContextAttribute("resourceBundle");
        String noSearchResult = "report-search-no-results-";
        String searchType = "";
        String bcName = "";
        String userType = "admin"; // used for no results msg
        boolean noSearchResults = false; // the user query didn't match any jobs/employee
        String noSearchResultMsg = ""; // msg to display the user when there were no results
        boolean tooManyResults = false;

        if (actionHelper.isLoggedInUserReviewer(request) &&
                !actionHelper.isLoggedInUserAdmin(request)) {
            bcName = actionHelper.getBusinessCenterForLoggedInReviewer(request);
            userType = "reviewer";
        }
        if (CWSUtil.validateOrgCode(searchTerm)) {
            searchType = "orgCode";
            boolean validOrgCode = JobMgr.findOrgCode(searchTerm, bcName);
            if (validOrgCode) {
                paramMap.put(Constants.BC_NAME, bcName);
                paramMap.put(SCOPE, SCOPE_ORG_CODE);
                paramMap.put(SCOPE_VALUE, searchTerm);
                setOrgCodeReportType();
                return false;
            } else { // no search results found
                noSearchResults = true;
            }
        } else {
            // Define the type of search used for error msg
            searchType = "name";
            if (CWSUtil.validateOsuid(searchTerm)) {
                searchType = "osuid";
            }

            int supervisorPidm = 0;
            if (actionHelper.isLoggedInUserSupervisor(request)) {
                Employee loggedInUser = actionHelper.getLoggedOnUser(request);
                supervisorPidm = loggedInUser.getId();
                userType = "supervisor";
            }

            try {
                // search by name/osuid - also does permission checking
                searchResults = JobMgr.search(searchTerm, bcName, supervisorPidm);
                int numberOfResults = (searchResults == null)? 0 : searchResults.size();

                // display report, list of results or error msg
                switch (numberOfResults) {
                    case 0: // no search results found
                        noSearchResults = true;
                        break;
                    case 1:
                        int pidm = searchResults.get(0).getEmployee().getId();
                        String positionNumber = searchResults.get(0).getPositionNumber();
                        String suffix = searchResults.get(0).getSuffix();
                        currentSupervisorJob = JobMgr.getJob(pidm, positionNumber, suffix);
                        String scopeValue = currentSupervisorJob.getIdKey();

                        paramMap.put(SCOPE, SCOPE_SUPERVISOR);
                        paramMap.put(SCOPE_VALUE, scopeValue.toString());
                        return false;
                    default:
                        return true;
                }

            } catch (ModelException e) {
                tooManyResults = true;
                noSearchResultMsg = e.getMessage();
            }
        }

        if (noSearchResults && noSearchResultMsg.equals("")) {
            if (searchType.equals("orgCode")) {
                noSearchResult += searchType + "-" + userType;
            } else {
                if (actionHelper.isLoggedInUserAdmin(request)) {
                    noSearchResult = "appraisal-search-no-results-admin";
                } else if (actionHelper.isLoggedInUserReviewer(request)) {
                    noSearchResult = "appraisal-search-no-results-reviewer";
                } else {
                    noSearchResult = "appraisal-search-no-results-supervisor";
                }
            }

            noSearchResultMsg = resource.getString(noSearchResult);
        }

        // Set a message if there were no results or too many
        if (noSearchResults || tooManyResults) {
            actionHelper.addErrorsToRequest(request, noSearchResultMsg);
        }

        switchRequestBreadcrumbsWithSession(request);
        return false;
    }

    /**
     * Stores the breadcrumb list from session into paramMap.
     *
     * @param request
     */
    private void switchRequestBreadcrumbsWithSession(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        List<Breadcrumb> sessCrumbs = (List<Breadcrumb>) session.getAttribute(BREADCRUMB_LIST);
        paramMap.put(BREADCRUMB_LIST, sessCrumbs);

    }

    private void setBcName() {
        if (breadcrumbList.size() > 1) {
            Breadcrumb secondBreadcrumb = breadcrumbList.get(1);
            if (secondBreadcrumb.getScope().equals(SCOPE_BC)) {
                paramMap.put(Constants.BC_NAME, secondBreadcrumb.getScopeValue());
            }
        }
    }
}
