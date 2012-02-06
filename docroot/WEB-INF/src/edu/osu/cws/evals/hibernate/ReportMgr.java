package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.Breadcrumb;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;

public class ReportMgr {
    /**
     * Performs sql query to fetch appraisals and only the needed fields. In order to
     * optimize sql, depending on the scope a different sql query is used.
     * @param paramMap  Parameter map with search/filter options.
     * @param crumbs    Breadcrumb list so that we can refer back to previous filter options.
     * @return
     */
    public static List<Object[]> activeReport(HashMap paramMap, List<Breadcrumb> crumbs) {
        Session session = HibernateUtil.getCurrentSession();

        List results;
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String bcName = "";
        if (crumbs.size() > 1) {
            bcName = crumbs.get(1).getScopeValue();
        }


        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = session.getNamedQuery("report.allActiveBC")
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = session.getNamedQuery("report.allActiveOrgPrefix")
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    //@todo: we need to keep track of the bc name previously selected in session
                    //@todo: we need to keep track of the org prefix for like search
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            results = session.getNamedQuery("report.allActiveOrgCode")
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", bcName)
                    .setParameter("tsOrgCode", scopeValue)
                    .list();
//        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
//            results = session.getNamedQuery("report.allActiveOSU")
//                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
//                    .list();
        } else {
            results = session.getNamedQuery("report.allActiveOSU")
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .list();
        }

        return results;
    }
}
