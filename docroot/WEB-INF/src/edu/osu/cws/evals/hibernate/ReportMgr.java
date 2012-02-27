package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.Breadcrumb;
import org.hibernate.Session;

import java.util.*;

public class ReportMgr {

    public static final String STAGE = "stage";
    public static final String UNIT = "unit";

    /**
     * Performs sql query to fetch appraisals and only the needed fields. In order to
     * optimize sql, depending on the scope a different sql query is used.
     * @todo: if we don't use this method to download csv data, remove it
     *
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

    /**
     * Returns the sql needed to grab the raw appraisal data (only the needed columns) so that
     * the java code can process the data for the charts.
     *
     * @param scope         The drill down scope: root, bc, orgPrefix, orgCode
     * @param reportType    The type of report to fetch data for
     * @param sortByCount   Whether the data should be sorted by the count first
     * @return
     */
    public static String getChartSQL(String scope, String reportType, boolean sortByCount) {
        String select = "";
        String from = " FROM appraisals, PYVPASJ ";
        String where = " WHERE appraisals.status not in ('completed', 'archived', 'closed') " +
                "AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix ";
        String group = " GROUP BY ";
        String order = " ORDER BY ";

        String col1 = "";
        if (scope.equals(ReportsAction.DEFAULT_SCOPE)) {
            col1 = "PYVPASJ_BCTR_TITLE";
        } else if (scope.equals(ReportsAction.SCOPE_BC)) {
            col1 = "SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
            where += " AND PYVPASJ_BCTR_TITLE = :bcName";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            col1 = "PYVPASJ_ORGN_CODE_TS";
            where += " AND PYVPASJ_BCTR_TITLE = :bcName" +
                    " AND PYVPASJ_ORGN_DESC LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            col1 = "PYVPASJ_SUPERVISOR_PIDM";
            where += " AND PYVPASJ_BCTR_TITLE = :bcName" +
                    " AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode ";
        }

        if (reportType.contains(UNIT)) {
            select = "SELECT count(*), " + col1 + from;
            group += col1;
            if (sortByCount) {
                order += "count(*) DESC, " + col1;
            } else {
                order += col1 + ", count(*) DESC";
            }
        } else if (reportType.contains(STAGE)) {
            select = "SELECT count(*), status " + from;
            group += " status";
            if (sortByCount) {
                order += "count(*) DESC, status";
            } else {
                order += "status, count(*) DESC";
            }
        }

        if (reportType.contains("WayOverdue")) {
            where += " AND appraisals.overdue > 30";
        } else if (reportType.contains("Overdue")) {
            where += " AND appraisals.overdue > 0";
        }

        return select + where + group + order;
    }

    /**
     * Returns the data needed to generate the google charts and nothing more.
     *
     *
     * @param paramMap
     * @param crumbs
     * @param sortByCount       Whether the data should be sorted by the count first
     * @return
     */
    public static List<Object[]> getChartData(HashMap paramMap, List<Breadcrumb> crumbs,
                                              boolean sortByCount) {
        Session session = HibernateUtil.getCurrentSession();

        List results;
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = "";
        if (crumbs.size() > 1) {
            bcName = crumbs.get(1).getScopeValue();
        }

        String sqlQuery = getChartSQL(scope, report, sortByCount);
        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = session.createSQLQuery(sqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = session.createSQLQuery(sqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            results = session.createSQLQuery(sqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", bcName)
                    .setParameter("tsOrgCode", scopeValue)
                    .list();
        } else {
            results = session.createSQLQuery(sqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .list();
        }

        if (report.contains(STAGE)) {
            if (!results.isEmpty()) {
                results = convertStatusToStage(results);
                results = combineAndSortStages(results);
            }
        }

        return results;
    }

    /**
     * Combines and sorts stages such as goals(Approval), goals(Due) and goals(Overdue)
     * into one.
     *
     * @param results       List<Object[]> data that we get back from sql select
     * @return
     */
    public static List<Object[]> combineAndSortStages(List<Object[]> results) {
        TreeMap<Integer, List<String>> sortedMap = new TreeMap<Integer, List<String>>();
        ArrayList<Object[]> sortedResults = new ArrayList<Object[]>();
        HashMap<String, Integer> combinedData = new HashMap<String, Integer>();

        // Convert the sql data into an array list so that we can combine the total counts
        for (Object[] row : (List<Object[]>) results) {
            String status = (String) row[1];
            int count = Integer.parseInt(row[0].toString());

            if (combinedData.containsKey(status)) {
                 count = count + combinedData.get(status);
            }
            combinedData.put(status, count);
        }

        // Now the data has been combined, place it in a Tree of String lists to sort it
        for (String status : combinedData.keySet()) {
            int count = (Integer) combinedData.get(status);
            List<String> bar = new ArrayList<String>();
            if (sortedMap.containsKey(count)) {
                bar = sortedMap.get(count);
            }
            bar.add(status);
            sortedMap.put(count, bar);
        }

        // Iterate over the sortedMap and generate the sorted results
        for (Integer count : sortedMap.descendingKeySet()) {
            List<String> values = sortedMap.get(count);
            for (String singleValue : values) {
                Object[] row = {count, singleValue};
                sortedResults.add(row);
            }
        }

        return sortedResults;
    }

    /**
     * Parses the sorted results and limits the data based on the maxDataPoints. All
     * the lower section of data that has a sort order >= maxDataPoints gets summed up
     * and grouped together in a section called "other".
     *
     * @param results
     * @param maxDataPoints
     * @return
     */
    public static List<Object[]> trimDataPoints(List<Object[]> results, int maxDataPoints) {
        if (results.size() <= maxDataPoints || maxDataPoints < 1) {
            return results;
        }

        List<Object[]> trimmedData = results.subList(0,maxDataPoints-1);
        List<Object[]> otherData = results.subList(maxDataPoints-1, results.size());
        int otherCount = 0;
        for (Object[] row : otherData) {
            otherCount += Integer.parseInt(row[0].toString());
        }
        if (otherCount > 0) {
            Object[] groupedRow = {otherCount, "other"};
            trimmedData.add(groupedRow);
        }

        return trimmedData;
    }

    /**
     * Returns the data used for the drill down table.
     *
     * @param paramMap search/request parameters
     * @param crumbs list of breadcrumbs
     * @return List of arrays of objects. The objects are strings
     */
    public static List<Object[]> getDrillDownData(HashMap paramMap, List<Breadcrumb> crumbs, boolean sortByCount) {
        HashMap copyParamMap = new HashMap();
        copyParamMap.putAll(paramMap);
        copyParamMap.put(ReportsAction.REPORT, ReportsAction.REPORT_DEFAULT);
        return getChartData(copyParamMap, crumbs, sortByCount);
    }

    /**
     * Iterates over a list of object arrays and replaces the second element in the array
     * which holds the status with the stage.
     *
     * @param results   List of array of objects (result from hibernate select query)
     * @return  List of arrays of objects. The objects are strings
     */
    private static List<Object[]> convertStatusToStage(List<Object[]> results) {
        for (Object[] row : results) {
            String status = (String) row[1];
            String stage = Appraisal.getStage(status);
            row[1] = stage;
        }

        return results;
    }

    /**
     * Returns the report title key, to be used when displaying the title of the
     * report (visualization chart). The actual text can be found in the
     * Language.properties file.
     *
     * @param paramMap  search/request parameters
     * @return  report title key
     */
    public static String getReportTitle(HashMap paramMap) {
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String scope = (String) paramMap.get(ReportsAction.SCOPE);

        String title = "report-title-" + report;
        if (report.contains(UNIT)) {
            title += scope;
        }

        return title;
    }

    /**
     * Returns the report header key, to be used when displaying the header of the
     * report (visualization chart). The actual text can be found in the
     * Language.properties file. Header examples: BC, OrgCode, Stage
     *
     * @param paramMap  search/request parameters
     * @return  report header key
     */
    public static String getReportHeader(HashMap paramMap) {
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String scope = (String) paramMap.get(ReportsAction.SCOPE);

        String title = "report-header-";
        if (report.contains(UNIT)) {
            title += scope;
        } else {
            title += "stages";
        }

        return title;
    }

    /**
     * Returns the hql needed to display the list of evaluation records in the reports.
     * The data is sorted by lastName, firstName
     *
     * @param scope
     * @param reportType
     * @return
     */
    public static String getListHQL(String scope, String reportType) {
        String select = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                " from edu.osu.cws.evals.models.Appraisal ";
        String where = " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes ";
        String order = " order by job.employee.lastName, job.employee.firstName";

        if (scope.equals(ReportsAction.SCOPE_BC)) {
            where += " and job.businessCenterName = :bcName";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            where += " and job.businessCenterName = :bcName" +
                    " and job.orgCodeDescription LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            where += " and job.businessCenterName = :bcName" +
                    " and job.tsOrgCode = :tsOrgCode";
        }

        if (reportType.contains("WayOverdue")) {
            where += " and overdue > 30";
        } else if (reportType.contains("Overdue")) {
            where += " and overdue > 0";
        }

        return select + where + order;
    }

    /**
     * Returns the data needed to generate the google charts and nothing more.
     *
     * @param paramMap
     * @param crumbs
     * @return
     */
    public static List<Appraisal> getListData(HashMap paramMap, List<Breadcrumb> crumbs) {
        Session session = HibernateUtil.getCurrentSession();

        List<Appraisal> results;
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = "";
        if (crumbs.size() > 1) {
            bcName = crumbs.get(1).getScopeValue();
        }

        String hqlQuery = getListHQL(scope, report);
        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = (ArrayList<Appraisal>) session.createQuery(hqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = (ArrayList<Appraisal>) session.createQuery(hqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            results = (ArrayList<Appraisal>) session.createQuery(hqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .setParameter("bcName", bcName)
                    .setParameter("tsOrgCode", scopeValue)
                    .list();
        } else {
            results = (ArrayList<Appraisal>) session.createQuery(hqlQuery)
                    .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                    .list();
        }

        return results;
    }
}
