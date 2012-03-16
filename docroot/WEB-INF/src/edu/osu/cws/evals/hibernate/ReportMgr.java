package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.Breadcrumb;
import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigDecimal;
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
        String col1 = "";
        String select = "";
        String from = " FROM appraisals, PYVPASJ ";
        String where = " WHERE appraisals.status not in ('completed', 'archived', 'closed') " +
                "AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix ";
        String group = " GROUP BY ";
        String order = " ORDER BY ";

        if (!scope.equals(ReportsAction.DEFAULT_SCOPE)) {
            where += " AND PYVPASJ_BCTR_TITLE = :bcName";
        }
        if (reportType.contains(STAGE)) {
            col1 = "status";
        }

        // assign a where clause depending on scope & the col1 if it is not empty
        if (scope.equals(ReportsAction.DEFAULT_SCOPE)) {
            col1 = col1.equals("")? "PYVPASJ_BCTR_TITLE" : col1;
        } else if (scope.equals(ReportsAction.SCOPE_BC)) {
            col1 = col1.equals("")? "SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)" : col1;
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            col1 = col1.equals("")? "PYVPASJ_ORGN_CODE_TS" : col1;
            where += " AND PYVPASJ_ORGN_DESC LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            col1 = col1.equals("")? "PYVPASJ_SUPERVISOR_PIDM" : col1;
            where += " AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode ";
        }

        select = "SELECT count(*), " + col1 + ", " + col1 + " as col1_copy" + from;
        group += col1;
        if (sortByCount) {
            order += "count(*) DESC, " + col1;
        } else {
            order += col1 + ", count(*) DESC";
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
     * The formatting of the data is:
     * {
     *     [number, 'bcName', 'bcName'],
     *     [number, 'orgPrefix', 'orgPrefix'],
     *     [number, 'orgCode', 'orgCode'],
     *     [number, 'lastName, firstName', 'pidm_posno_suffix], // for supervisors
     * }
     *
     * @param paramMap
     * @param crumbs            Breadcrumbs
     * @param sortByCount       Whether the data should be sorted by the count first
     * @param directSupervisors A list of jobs of the direct supervisors of the current level
     * @param myTeamAppraisals  A list of appraisals of the employees directly under current
     *                          supervisor
     * @return
     */
    public static List<Object[]> getChartData(HashMap paramMap, List<Breadcrumb> crumbs,
                                              boolean sortByCount, List<Job> directSupervisors,
                                              ArrayList<Appraisal> myTeamAppraisals,
                                              Job currentSupervisorJob) {
        Session session = HibernateUtil.getCurrentSession();

        String sqlQuery = "";
        List results = new ArrayList<Object[]>();
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = "";
        if (crumbs.size() > 1) {
            bcName = crumbs.get(1).getScopeValue();
        }

        // Fetch direct supervisors so we can get supervising data
        if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            sqlQuery = getSupervisorChartSQL(scope, report, sortByCount, directSupervisors);
        } else {
            sqlQuery = getChartSQL(scope, report, sortByCount);
        }

        Query chartQuery = session.createSQLQuery(sqlQuery)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);

        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = chartQuery
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = chartQuery
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            results = chartQuery
                    .setParameter("bcName", bcName)
                    .setParameter("tsOrgCode", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            results = chartQuery
                    .list();

            // Sometimes, we just want the drill down data and the current supervisor
            // shouldn't be included
            if (myTeamAppraisals != null && currentSupervisorJob != null) {
                results = addMyTeamToReport(results, report, myTeamAppraisals,
                        currentSupervisorJob);
            }
        } else {
            results = chartQuery
                    .list();
        }

        if (!results.isEmpty()) {
            if (report.contains(STAGE)) {
                results = convertStatusToStage(results);
                results = combineAndSortStages(results);
            }

            // Add a third column to uniquely identify supervisors: pidm_posno_suffix
            if (scope.equals(ReportsAction.SCOPE_SUPERVISOR) && report.contains(UNIT)) {
                results = addSupervisorByUnitData(results, directSupervisors, currentSupervisorJob);
            }
        }

        return results;
    }

    /**
     * Generates the sql needed to fetch the supervisors report.
     *
     * @param scope
     * @param reportType
     * @param sortByCount
     * @param directSupervisors     List<Job> of direct supervisors
     * @return
     */
    public static String getSupervisorChartSQL(String scope, String reportType, boolean sortByCount,
                                        List<Job> directSupervisors) {
        //@todo: need to handle case when directSupervisors is empty
        String outerSelect = "SELECT SUM(has_appraisal), ";
        String select = "SELECT " +
                "(case when appraisals.id is not null then 1 else 0 end) as has_appraisal, ";
        String from = "FROM pyvpasj left join appraisals on " +
                "(appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) ";
        String where = "WHERE pyvpasj_status = 'A' AND pyvpasj_appointment_type " +
                "in :appointmentTypes ";
        String orderBy = " ORDER BY ";
        String col1 = "";

        String startWith = EvalsUtil.getStartWithClause(directSupervisors);

        if (reportType.contains(STAGE)) {
            col1 = "status";
            select += "appraisals.status ";
        } else {
            col1 = "root_supervisor_pidm";
            select += "CONNECT_BY_ROOT pyvpasj_pidm as root_supervisor_pidm ";
        }

        outerSelect += col1;
        String groupBy = "GROUP BY " + col1;
        if (sortByCount) {
            orderBy += "sum(has_appraisal) DESC, " + col1;
        } else {
            orderBy += col1 + ", sum(has_appraisal) DESC";
        }

        if (reportType.contains("WayOverdue")) {
            where += " AND appraisals.overdue > 30";
        } else if (reportType.contains("Overdue")) {
            where += " AND appraisals.overdue > 0";
        }

        return outerSelect + " from (" + select + from + where + startWith +
                Constants.CONNECT_BY + ") " + groupBy + orderBy;
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
     *
     * Input:
     * {
     *     5, 12345, // # of evaluations, supervisor pidm
     *     10, 54334
     *     20, 56751
     * }
     * Output:
     * {
     * // # of evaluations, supervisor name, pidm_posno_suffix
     *     5, 'Doe, Jane', '12345_C1111_00',
     *     10, 'Smith, Joe', '54334_C1234_00',
     *     20, 'Kent, Clark', '54334_C1234_00'
     * }
     *
     * @param results
     * @param directSupervisors
     * @return
     */
    public static List<Object[]> addSupervisorByUnitData(List<Object[]> results,
                                                         List<Job> directSupervisors,
                                                         Job currentSupervisorJob) {
        List<Job> supervisorsInReport = new ArrayList<Job>();
        supervisorsInReport.addAll(directSupervisors);

        // When we are getting the drill down data, we don't need the current supervisor
        if (currentSupervisorJob != null) {
            supervisorsInReport.add(currentSupervisorJob);
        }

        List<Object[]> supervisorData = new ArrayList<Object[]>();
        HashMap<String, Integer> pidmEvalCount = new HashMap<String, Integer>();
        HashMap<Integer, Job> supervisorMap = new HashMap<Integer, Job>();

        for (Job supervisor : supervisorsInReport) {
            supervisorMap.put(supervisor.getEmployee().getId(), supervisor);
        }

        for (Object[] row : results) {
            Integer pidm = Integer.parseInt(row[1].toString());
            int evalCount = Integer.parseInt(row[0].toString());
            Job supervisor = supervisorMap.get(pidm);
            String supervisorName = supervisor.getEmployee().getName();
            String key = pidm + "_" + supervisor.getPositionNumber() +
                    "_" + supervisor.getSuffix();

            Object[] newRow = {evalCount, supervisorName, key};
            supervisorData.add(newRow);
        }

        return supervisorData;
    }

    /**
     * Adds raw information about the current supervisor by unit or by stage report data.
     * In the case of the by unit we add:
     *  ["# of evals", "pidm of direct employee"]
     *
     * In the case of by stage we add:
     *  ["1", "status of evaluation of evaluation of direct employee1"],
     *  ["1", "status of evaluation of evaluation of direct employee2"]
     *
     * The data will be combined and processed by other methods.
     *
     * @param results
     * @param report
     * @param myTeamAppraisals
     * @return
     */
    public static List<Object[]> addMyTeamToReport(List<Object[]> results, String report,
                                                   List<Appraisal> myTeamAppraisals,
                                                   Job currentSupervisorJob) {
        // add all the results to a new list because we cannot modify data that comes from
        // Hibernate
        List<Object[]> newResults = new ArrayList<Object[]>();
        newResults.addAll(results);

        //@todo: what happens if results is empty?
        //@todo: what happens if myTeamAppraisals is empty?
        //@todo: need to filter out data if report is overdue or wayOverdue
        if (report.contains(UNIT)) {
            Integer pidm = (Integer) currentSupervisorJob.getEmployee().getId();
            Object[] currentSupervisorLevel = new Object[2];
            currentSupervisorLevel[0] = (Object) myTeamAppraisals.size();
            currentSupervisorLevel[1] = (Object) pidm;
            newResults.add(0, currentSupervisorLevel);
        } else {
            for (Appraisal appraisal : myTeamAppraisals) {
                Object[] newStatusRow = {1, appraisal.getStatus()};
                newResults.add(newStatusRow);
            }
        }

        return newResults;
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
        if (results.size() <= maxDataPoints || maxDataPoints < 1 || results.isEmpty()) {
            return results;
        }

        List<Object[]> trimmedData = new ArrayList<Object[]>();
        List<Object[]> otherData = new ArrayList<Object[]>();
        trimmedData.addAll(results.subList(0,maxDataPoints-1));
        otherData.addAll(results.subList(maxDataPoints-1, results.size()));

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
     * Returns the data used for the drill down menu.
     *
     * @param paramMap search/request parameters
     * @param crumbs list of breadcrumbs
     * @param sortByCount
     * @param directSupervisors
     * @return List of arrays of objects. The objects are strings
     */
    public static List<Object[]> getDrillDownData(HashMap paramMap, List<Breadcrumb> crumbs,
                                                  boolean sortByCount, List<Job> directSupervisors) {
        HashMap copyParamMap = new HashMap();
        copyParamMap.putAll(paramMap);
        copyParamMap.put(ReportsAction.REPORT, ReportsAction.REPORT_DEFAULT);
        return getChartData(copyParamMap, crumbs, sortByCount, directSupervisors,
                null, null);
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

        //@todo: do we need to do bc filtering when the scope is supervisor?
        if (!scope.equals(ReportsAction.DEFAULT_SCOPE) &&
                !scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            where += " and job.businessCenterName = :bcName";
        }

        if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            where += " and job.orgCodeDescription LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            where += " and job.tsOrgCode = :tsOrgCode";
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            where += " and id in (:appraisalIds)";
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
    public static List<Appraisal> getListData(HashMap paramMap, List<Breadcrumb> crumbs,
                                              List<Job> directSupervisors) {
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
        Query listQuery = session.createQuery(hqlQuery)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);

        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("bcName", bcName)
                    .setParameter("tsOrgCode", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            List<Integer> appraisalIds = getAppraisalIdsForSupervisorReport(directSupervisors);
            results = (ArrayList<Appraisal>) listQuery
                    .setParameterList("appraisalIds", appraisalIds)
                    .list();
        } else {
            results = (ArrayList<Appraisal>) listQuery
                    .list();
        }

        return results;
    }

    /**
     * Returns the list of all the appraisals that match the current supervising chain all the
     * way down to the leaf.
     *
     * @param directSupervisors
     * @return
     */
    public static List<Integer> getAppraisalIdsForSupervisorReport(List<Job> directSupervisors) {
        if (directSupervisors == null) {
            return null;
        }

        Session session = HibernateUtil.getCurrentSession();
        List<Integer> ids = new ArrayList<Integer>();

        String select = "SELECT appraisals.id FROM pyvpasj " +
                "LEFT JOIN appraisals ON (appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) ";
        String where = "WHERE pyvpasj_status = 'A' " +
                "AND PYVPASJ_APPOINTMENT_TYPE in (:appointmentTypes)";
        String startWith = EvalsUtil.getStartWithClause(directSupervisors);

        String sql = select + where + startWith + Constants.CONNECT_BY;
        List<BigDecimal> result = session.createSQLQuery(sql)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES)
                .list();

        for (BigDecimal id : result) {
            ids.add(Integer.parseInt(id.toString()));
        }

        return ids;
    }
}
