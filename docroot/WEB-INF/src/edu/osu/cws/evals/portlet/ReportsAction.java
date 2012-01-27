package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.util.Breadcrumb;
import org.apache.commons.lang.ArrayUtils;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
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

    private static Log _log = LogFactoryUtil.getLog(ReportsAction.class);


    /**
     * Map used to store drilldown, and search options used to fetch
     * data.
     * *scope: root (default), bc, orgPrefix, orgCode, employee
     * scopeValue: related to scope: osu, uabc, mum, 123456, 943232
     * breadCrumbIndex: numeric index of the breadcrumb clicked
     */
    private HashMap paramMap = new HashMap();

    private List<Object[]> reportAppraisals;

    /**
     * Value displayed when generating the drill down links in data table.
     */
    HashSet<String> units = new HashSet<String>();


    public String report(PortletRequest request, PortletResponse response) throws Exception {
        setParamMap(request);
        PortletSession session = request.getPortletSession();
        session.setAttribute("paramMap", paramMap);

        String breadcrumbSessKey = "breadcrumbList";
        List<Breadcrumb> sessionBreadcrumbs = (List<Breadcrumb>) session.getAttribute(breadcrumbSessKey);
        breadcrumbList = getBreadcrumbs(sessionBreadcrumbs);
        session.setAttribute(breadcrumbSessKey, breadcrumbList);

        String jspFile = activeReport(breadcrumbList);
        setupDataForJSP();
        actionHelper.useMaximizedMenu(request);

        return jspFile;
    }

    private void setupDataForJSP() {
        for (Object[] row : reportAppraisals) {
            if (row.length != 0 && row[0] != null) {
                units.add(row[0].toString());
            }
//            debug(row);
        }
//        for (String unit : units) {
//            _log.error("unit = " + unit);
//        }
        actionHelper.addToRequestMap("reportAppraisals", reportAppraisals);
        actionHelper.addToRequestMap("units", units);

        String scope = getScope();
        String scopeValue = getScopeValue();
        actionHelper.addToRequestMap("scope", scope);
        actionHelper.addToRequestMap("scopeValue", scopeValue);

        String nextScope = nextScopeInDrillDown(scope);
        actionHelper.addToRequestMap("nextScope", nextScope);
        actionHelper.addToRequestMap("breadcrumbList", breadcrumbList);
    }

    private String nextScopeInDrillDown(String currentScope) {
        int currentScopeIndex = ArrayUtils.indexOf(DRILL_DOWN_INDEX, currentScope);
        int nextDrillDownScope = currentScopeIndex;
        if (currentScopeIndex < DRILL_DOWN_INDEX.length -1) {
            nextDrillDownScope++;
        }
        return (String) DRILL_DOWN_INDEX[nextDrillDownScope];
    }

    private void debug(Object[] row) {
        int i = 0;
        for (Object column : row) {
            if (column != null) {
                _log.error("column [" + i + "] = " + column.toString());
            }
            i++;
        }
    }

    private String activeReport(List<Breadcrumb> crumbs) {
        reportAppraisals = ReportMgr.activeReport(paramMap, crumbs);

        return Constants.JSP_REPORT;
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
            Breadcrumb crumb = new Breadcrumb(scopeValue, scope, scopeValue);
            crumbs.add(crumb);
        }

        return crumbs;
    }

    private String getScopeValue() {
        return (String) paramMap.get(SCOPE_VALUE);
    }

    private String getScope() {
        return (String) paramMap.get(SCOPE);
    }

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

        int breadcrumbIndex = ParamUtil.getInteger(request, BREADCRUMB_INDEX, -1);
        paramMap.put(BREADCRUMB_INDEX, breadcrumbIndex);
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }
}
