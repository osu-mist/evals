package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.Breadcrumb;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.*;

public class ReportMgr {

    public static final String STAGE = "stage";
    public static final String UNIT = "unit";
    public static final String ACTIVE_STATUS_SQL = "appraisals.status not in " +
            "('completed', 'closed', 'archivedCompleted', 'archivedClosed')";
    public static final int WAY_OVERDUE_DAYS = 30;

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
     * @param addBC         Whether the bc should be added to where clause
     * @return
     */
    public static String getChartSQL(String scope, String reportType, boolean sortByCount,
                                     boolean addBC) {
        String col1 = "";
        String select = "";
        String notNull = "";
        String from = " FROM appraisals, PYVPASJ ";
        String where = " WHERE " + ACTIVE_STATUS_SQL + " " +
                "AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix ";
        String group = " GROUP BY ";
        String order = " ORDER BY ";

        if (!scope.equals(ReportsAction.DEFAULT_SCOPE) && addBC) {
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
        notNull = " AND " + col1 + " IS NOT NULL";
        group += col1;
        if (sortByCount) {
            order += "count(*) DESC, " + col1;
        } else {
            order += col1 + ", count(*) DESC";
        }

        if (isWayOverdueReport(reportType)) {
            where += " AND appraisals.overdue > 30";
        } else if (isOverdueReport(reportType)) {
            where += " AND appraisals.overdue > 0";
        }

        return select + where + notNull + group + order;
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
     * @param sortByCount       Whether the data should be sorted by the count first
     * @param directSupervisors A list of jobs of the direct supervisors of the current level
     * @param myTeamAppraisals  A list of appraisals of the employees directly under current
     *                          supervisor
     * @param currentSupervisorJob
     * @param inLeafSupervisor  Whether or not the current supervisor report is lowest level
     * @return
     */
    public static List<Object[]> getChartData(HashMap paramMap,
                                              boolean sortByCount, List<Job> directSupervisors,
                                              ArrayList<Appraisal> myTeamAppraisals,
                                              Job currentSupervisorJob, boolean inLeafSupervisor) {
        Session session = HibernateUtil.getCurrentSession();

        String sqlQuery = "";
        List results;
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = (String) paramMap.get(Constants.BC_NAME);
        boolean addBC = !StringUtils.isEmpty(bcName);

        // Fetch direct supervisors so we can get supervising data
        if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            sqlQuery = getSupervisorChartSQL(report, sortByCount, directSupervisors.size(),
                    inLeafSupervisor);
        } else {
            sqlQuery = getChartSQL(scope, report, sortByCount, addBC);
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
            chartQuery.setParameter("tsOrgCode", scopeValue);

            // only set the bcName if it's not empty. if an admin searches, the bcName is empty
            if (addBC) {
                 chartQuery.setParameter("bcName", bcName);
            }
            results = chartQuery.list();
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            EvalsUtil.setStartWithParameters(directSupervisors, chartQuery);
            results = chartQuery.list();

            // Sometimes, we just want the drill down data and the current supervisor's team
            // shouldn't be included
            if (myTeamAppraisals != null && currentSupervisorJob != null && !inLeafSupervisor) {
                results = addMyTeamToReport(results, report, myTeamAppraisals,
                        currentSupervisorJob);
            }
        } else {
            results = chartQuery.list();
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
     * @param reportType
     * @param sortByCount
     * @param directSupervisorCount
     * @param inLeafSupervisor
     * @return
     */
    public static String getSupervisorChartSQL(String reportType, boolean sortByCount,
                                        int directSupervisorCount, boolean inLeafSupervisor) {
        //@todo: need to handle case when directSupervisors is empty
        String startWith = EvalsUtil.getStartWithClause(directSupervisorCount);
        String where = "WHERE pyvpasj_status = 'A' AND pyvpasj_appointment_type " +
                "in :appointmentTypes ";
        if (!inLeafSupervisor) {
            where += "AND level > 1 ";
        }
        String where2 = " WHERE " + ACTIVE_STATUS_SQL;
        String outerSelect = "SELECT SUM(has_appraisal), ";
        String select = "SELECT " +
                "(case when appraisals.id is not null then 1 else 0 end) as has_appraisal, ";
        String fromPart1 = "FROM (SELECT level, pyvpasj_pidm, pyvpasj_posn, pyvpasj_suff, " +
                "CONNECT_BY_ROOT pyvpasj_pidm as root_supervisor_pidm " +
                "FROM pyvpasj " + where;

        String fromPart2 =
                startWith +
                Constants.CONNECT_BY +
                ") LEFT JOIN appraisals on " +
                "(appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn " +
                "AND appraisals.job_suffix = pyvpasj_suff)";

        String from = fromPart1 + fromPart2;

        String orderBy = " ORDER BY ";
        String col1 = "";

        if (reportType.contains(STAGE)) {
            col1 = "status";
            select += "appraisals.status ";
        } else {
            col1 = "root_supervisor_pidm";
            select += "root_supervisor_pidm ";
        }

        outerSelect += col1;
        String groupBy = "GROUP BY " + col1;
        if (sortByCount) {
            orderBy += "sum(has_appraisal) DESC, " + col1;
        } else {
            orderBy += col1 + ", sum(has_appraisal) DESC";
        }

        if (isWayOverdueReport(reportType)) {
            where2 += " AND appraisals.overdue > 30";
        } else if (isOverdueReport(reportType)) {
            where2 += " AND appraisals.overdue > 0";
        }

        return outerSelect + " from (" + select + from + where2 + ") " + groupBy + orderBy;
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

        if (myTeamAppraisals == null || results.isEmpty()) {
            return newResults;
        }

        newResults.addAll(results);
        List<Appraisal> teamAppraisalTemp = new ArrayList<Appraisal>();

        // Only use the active evaluations
        for (Appraisal appraisal : myTeamAppraisals) {
            // my team appraisals contains jobs that need evaluations created.
            if (appraisal.getId() == 0) {
                continue;
            }

            String status = appraisal.getStatus();
            if (!status.equals(Appraisal.STATUS_COMPLETED) &&
                    !status.equals(Appraisal.STATUS_CLOSED) &&
                    !status.equals(Appraisal.STATUS_ARCHIVED_COMPLETED) &&
                    !status.equals(Appraisal.STATUS_ARCHIVED_CLOSED)) {

                boolean addAppraisal = true;

                // Filter out of the appraisal if needed based on overdue or way overdue days.
                Integer overdue = appraisal.getOverdue();
                if (isOverdueReport(report) && overdue < 1) {
                    addAppraisal = false;
                } else if (isWayOverdueReport(report) && overdue <= WAY_OVERDUE_DAYS) {
                    addAppraisal = false;
                }

                if (!appraisal.getJob().getAppointmentType().equals(AppointmentType.CLASSIFIED)) {
                    addAppraisal = false;
                }

                if (addAppraisal) {
                    teamAppraisalTemp.add(appraisal);
                }

            }
        }

        // don't act the direct employees of the current supervisor if none of them had
        // evaluations.
        if (!teamAppraisalTemp.isEmpty()) {
            if (report.contains(UNIT)) {
                Integer pidm = (Integer) currentSupervisorJob.getEmployee().getId();
                Object[] currentSupervisorLevel = new Object[2];
                currentSupervisorLevel[0] = (Object) teamAppraisalTemp.size();
                currentSupervisorLevel[1] = (Object) pidm;
                newResults.add(0, currentSupervisorLevel);
            } else {
                for (Appraisal appraisal : teamAppraisalTemp) {
                    Object[] newStatusRow = {1, appraisal.getStatus()};
                    newResults.add(newStatusRow);
                }
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
     * @param sortByCount
     * @param directSupervisors
     * @return List of arrays of objects. The objects are strings
     */
    public static List<Object[]> getDrillDownData(HashMap paramMap, boolean sortByCount,
                                                  List<Job> directSupervisors,
                                                  boolean inLeafSupervisor) {
        HashMap copyParamMap = new HashMap();
        copyParamMap.putAll(paramMap);
        copyParamMap.put(ReportsAction.REPORT, ReportsAction.REPORT_DEFAULT);
        return getChartData(copyParamMap, sortByCount, directSupervisors,
                null, null, inLeafSupervisor);
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
     * If the report is overdue by unit or stage
     *
     * @param reportType
     * @return
     */
    public static boolean isOverdueReport(String reportType) {
        return reportType.contains("Overdue");
    }

    /**
     * If the report is way overdue by unit or stage
     * @param reportType
     * @return
     */
    public static boolean isWayOverdueReport(String reportType) {
        return reportType.contains("WayOverdue");
    }

    /**
     * Returns a list of late evaluations. The data returned is a list of object[] since we only needed a few
     * columns from various tables. It was less code/simpler to do it via sql rather than hql.
     *
     * @param bcNames                   List of bc names to get late report data
     * @return List<Object[]>           Late evaluation records
     */
    public static List<Object[]> getLateEvaluations(List<String> bcNames) {
        Session session = HibernateUtil.getCurrentSession();
        List<String> supportedAppointmentTypes = Arrays.asList(
                AppointmentType.CLASSIFIED,
                AppointmentType.CLASSIFIED_IT);

        List<String> ignoredStatus = Arrays.asList(
                Appraisal.STATUS_COMPLETED,
                Appraisal.STATUS_CLOSED,
                Appraisal.STATUS_ARCHIVED_CLOSED,
                Appraisal.STATUS_ARCHIVED_COMPLETED
        );

        return session.getNamedQuery("report.reportLateEvaluations")
                .setParameterList("ignoredStatus", ignoredStatus)
                .setParameterList("bcNames", bcNames)
                .list();
    }
}
