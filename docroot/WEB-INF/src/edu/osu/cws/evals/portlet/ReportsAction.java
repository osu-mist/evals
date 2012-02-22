package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.util.Breadcrumb;
import org.apache.commons.lang.ArrayUtils;

import javax.portlet.*;
import java.util.*;

public class ReportsAction implements ActionInterface {
    public static final String SCOPE = "scope";
    public static final String SCOPE_VALUE = "scopeValue";

    public static final String DEFAULT_SCOPE = "root";
    public static final String DEFAULT_SCOPE_VALUE = "osu";
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

    public static String[] APPOINTMENT_TYPES = {"Classified"};
    public static String[] DRILL_DOWN_INDEX = {
            DEFAULT_SCOPE,
            SCOPE_BC,
            SCOPE_ORG_PREFIX,
            SCOPE_ORG_CODE,
            SCOPE_SUPERVISOR
    };
    public static final String BREADCRUMB_INDEX = "breadcrumbIndex";

    private List<Breadcrumb> breadcrumbList = new ArrayList<Breadcrumb>();

    private ActionHelper actionHelper;
    private HomeAction homeAction;

    /**
     * Map used to store drilldown, and search options used to fetch
     * data.
     * *scope: root (default), bc, orgPrefix, orgCode, employee
     * scopeValue: related to scope: osu, uabc, mum, 123456, 943232
     * breadCrumbIndex: numeric index of the breadcrumb clicked
     */
    private HashMap paramMap = new HashMap();

    private List<Appraisal> listAppraisals;
    private List<Object[]> chartData;
    private List<Object[]> drillDownData;

    /**
     * Value displayed when generating the drill down links in data table.
     */
    HashMap<String, Integer> units = new HashMap<String, Integer>();

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
        setParamMap(request);

        String breadcrumbSessKey = "breadcrumbList";
        List<Breadcrumb> sessionBreadcrumbs = (List<Breadcrumb>) session.getAttribute(breadcrumbSessKey);
        breadcrumbList = getBreadcrumbs(sessionBreadcrumbs);

        if (!canViewReport(request)) {
            if (response instanceof ActionResponse) {
                ((ActionResponse) response ).setWindowState(WindowState.NORMAL);
            }
            actionHelper.addErrorsToRequest(request, ActionHelper.ACCESS_DENIED);
            return homeAction.display(request, response);
        }

        String jspFile = activeReport(breadcrumbList);
        setupDataForJSP(request);
        actionHelper.useMaximizedMenu(request);

        // Save session values only if we didn't throw an exception before we got here.
        session.setAttribute(breadcrumbSessKey, breadcrumbList);
        session.setAttribute("paramMap", paramMap);

        return jspFile;
    }

    private void setupDataForJSP(PortletRequest request) throws Exception {
        actionHelper.addToRequestMap("chartData", chartData);
        actionHelper.addToRequestMap("drillDownData", drillDownData);
        actionHelper.addToRequestMap("listAppraisals", listAppraisals);

        String scope = getScope();
        String scopeValue = getScopeValue();
        actionHelper.addToRequestMap("scope", scope);
        actionHelper.addToRequestMap("scopeValue", scopeValue);
        actionHelper.addToRequestMap("report", paramMap.get(REPORT));
        actionHelper.addToRequestMap("reportTitle", ReportMgr.getReportTitle(paramMap));
        actionHelper.addToRequestMap("reportHeader", ReportMgr.getReportHeader(paramMap));

        String nextScope = nextScopeInDrillDown(scope);
        actionHelper.addToRequestMap("nextScope", nextScope);
        actionHelper.addToRequestMap("breadcrumbList", breadcrumbList);

        boolean allowAllDrillDown = false;
        if (actionHelper.isLoggedInUserAdmin(request)) {
            allowAllDrillDown = true;
        }
        actionHelper.addToRequestMap("allowAllDrillDown", allowAllDrillDown);

        String reviewerBCName = "";
        if (actionHelper.isLoggedInUserReviewer(request)) {
            int employeeID = actionHelper.getLoggedOnUser(request).getId();
            reviewerBCName = actionHelper.getReviewer(employeeID).getBusinessCenterName();
        }
        actionHelper.addToRequestMap("reviewerBCName", reviewerBCName);
    }

    private String nextScopeInDrillDown(String currentScope) {
        int currentScopeIndex = ArrayUtils.indexOf(DRILL_DOWN_INDEX, currentScope);
        int nextDrillDownScope = currentScopeIndex;
        if (currentScopeIndex < DRILL_DOWN_INDEX.length -1) {
            nextDrillDownScope++;
        }
        return (String) DRILL_DOWN_INDEX[nextDrillDownScope];
    }

    private String activeReport(List<Breadcrumb> crumbs) {
        chartData = ReportMgr.getChartData(paramMap, crumbs, true);
        drillDownData = ReportMgr.getDrillDownData(paramMap, crumbs, false);
        if (shouldListAppraisals()) {
            listAppraisals = ReportMgr.getListData(paramMap, crumbs);
        }

        return Constants.JSP_REPORT;
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

        for (Object[] row : chartData ) {
            numberOfEvalRecords += Integer.parseInt(row[0].toString());
        }

        return numberOfEvalRecords <= maxDataForList;
    }

    /**
     * Handles looking at the breadcrumbs stored in session and by
     * looking at the paramMap, it changes the breadcrumbs and returns
     * them as a list.
	 * There are 3 possibilities:
     *    1. return just the initial one (osu)
     *    2. return breadCrumbFromSession  (not changed)
	 *    3. return breadCrumbFromSession.add(new Breadcumb(...))
     *    4. if somebody clicks on a previous scope of the breadcrumb, we need to remove
     *    the rest of the scopes down the chain.
     * @param sessionBreadcrumbs
     * @return
     */
    private List<Breadcrumb> getBreadcrumbs(List<Breadcrumb> sessionBreadcrumbs) {
        List<Breadcrumb> crumbs = new ArrayList<Breadcrumb>();
        Breadcrumb rootBreadcrumb = new Breadcrumb("OSU", DEFAULT_SCOPE, DEFAULT_SCOPE_VALUE);

        // Initial user click to reports
        String scope = getScope();
        String scopeValue = getScopeValue();
        if (paramMap.isEmpty() || scope.equals(DEFAULT_SCOPE)) {
            crumbs.add(rootBreadcrumb);
            return crumbs;
        }

        int breadcrumbIndex = (Integer) paramMap.get("breadcrumbIndex");
        boolean clickedCrumb = breadcrumbIndex != -1 && breadcrumbIndex < sessionBreadcrumbs.size() - 1;
        if (clickedCrumb) {
            crumbs = sessionBreadcrumbs.subList(0, breadcrumbIndex+1);
        } else { // User is drilling down the chain
            crumbs.addAll(sessionBreadcrumbs);

            // Figure out if the user selected a different report, within the same scope
            String scopeOfLastCrumb = sessionBreadcrumbs.get(sessionBreadcrumbs.size() - 1).getScope();
            boolean sameScope = scope.equals(scopeOfLastCrumb);
            if (!sameScope) {
                Breadcrumb crumb = new Breadcrumb(scopeValue, scope, scopeValue);
                crumbs.add(crumb);
            }
        }

        return crumbs;
    }

    private String getScopeValue() {
        return (String) paramMap.get(SCOPE_VALUE);
    }

    private String getScope() {
        return (String) paramMap.get(SCOPE);
    }

    /**
     * Looks at the values passed in the request, the session values and the
     * default ones. First it tries to use the request, if the values are not
     * found there, it tries session. Last it uses the default value.
     *
     * @param request
     */
    private void setParamMap(PortletRequest request) {
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


        // If the user is about to enter the org code scope and the chart is by Unit, use stage
        String selectedReport = (String) paramMap.get(REPORT);
        String selectedScope = (String) paramMap.get(SCOPE);
        if (selectedScope.equals(SCOPE_ORG_CODE) && selectedReport.contains(ReportMgr.UNIT)) {
            paramMap.put(REPORT,  REPORT_STAGE_BREAKDOWN);
        }


        int breadcrumbIndex = ParamUtil.getInteger(request, BREADCRUMB_INDEX, -1);
        paramMap.put(BREADCRUMB_INDEX, breadcrumbIndex);
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

        if (getScope().equals(DEFAULT_SCOPE)) {
            return true;
        }

        if (actionHelper.isLoggedInUserReviewer(request)) {
            int employeeID = actionHelper.getLoggedOnUser(request).getId();
            String businessCenterName = actionHelper.getReviewer(employeeID).getBusinessCenterName();
            if (breadcrumbList.size() > 1) {
                Breadcrumb bcBreadcrumb = breadcrumbList.get(1);
                if (bcBreadcrumb.getScopeValue().equals(businessCenterName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
