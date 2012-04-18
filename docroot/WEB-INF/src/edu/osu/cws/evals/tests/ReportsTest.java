package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.portlet.ReportsAction;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Test
public class ReportsTest {
//    @BeforeMethod
//    public void setUp() throws Exception {
//        DBUnit dbunit = new DBUnit();
//        dbunit.seedDatabase();
//    }

    public void shouldProduceCorrectSQLForOSULevel() {
        String sql = ReportMgr.getChartSQL("root", "unitBreakdown", true, true);
        String expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " GROUP BY PYVPASJ_BCTR_TITLE ORDER BY count(*) DESC, PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "unitOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 0 GROUP BY PYVPASJ_BCTR_TITLE ORDER BY count(*) DESC, PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "unitWayOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND appraisals.overdue > 30 GROUP BY PYVPASJ_BCTR_TITLE ORDER BY count(*) DESC, PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageBreakdown", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 0 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageWayOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND" +
                " PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 30 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForBCLevel() {
        String scope = ReportsAction.SCOPE_BC;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown", true, true);
        String expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ " +
                " WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) " +
                "ORDER BY count(*) DESC, SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue", true, true);
        expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ " +
                " WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND PYVPASJ_BCTR_TITLE = :bcName AND appraisals.overdue > 0 " +
                "GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) ORDER BY count(*) DESC, SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue", true, true);
        expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ  " +
                "WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND appraisals.overdue > 30 " +
                "GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) ORDER BY count(*) DESC, SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName GROUP BY status " +
                "ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status not " +
                "in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number AND " +
                "PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName AND " +
                "appraisals.overdue > 0 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName" +
                " AND appraisals.overdue > 30 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForOrgPrefixLevel() {
        String scope = ReportsAction.SCOPE_ORG_PREFIX;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown", true, true);
        String expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND" +
                " PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix " +
                "GROUP BY PYVPASJ_ORGN_CODE_TS ORDER BY count(*) DESC, PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND " +
                "appraisals.overdue > 0 GROUP BY PYVPASJ_ORGN_CODE_TS ORDER BY count(*) DESC, PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix " +
                "AND appraisals.overdue > 30 GROUP BY PYVPASJ_ORGN_CODE_TS ORDER BY count(*) DESC, PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status" +
                " not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE" +
                " in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix GROUP BY status " +
                "ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND" +
                " appraisals.overdue > 0 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND " +
                "appraisals.overdue > 30 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForOrgCodeLevel() {
        String scope = ReportsAction.SCOPE_ORG_CODE;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown", true, true);
        String expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  " +
                "WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode  " +
                "GROUP BY PYVPASJ_SUPERVISOR_PIDM ORDER BY count(*) DESC, PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 0 GROUP BY PYVPASJ_SUPERVISOR_PIDM ORDER BY count(*) DESC, " +
                "PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue", true, true);
        expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 30 GROUP BY PYVPASJ_SUPERVISOR_PIDM ORDER BY count(*) DESC, " +
                "PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode  GROUP BY status " +
                "ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName AND" +
                " PYVPASJ_ORGN_CODE_TS = :tsOrgCode  AND appraisals.overdue > 0 " +
                "GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue", true, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 30 GROUP BY status ORDER BY count(*) DESC, status";
        assert sql.equals(expectedSQL);
    }

    public void shouldReturnResultsStageForGoalsApproved() {
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_APPROVED).equals(Appraisal.STAGE_RESULTS);
    }

    public void shouldReturnGoalsStageForGoalsStatusOtherThanApproved() {
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_APPROVAL_DUE).equals(Appraisal.STAGE_GOALS);
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_APPROVAL_OVERDUE).equals(Appraisal.STAGE_GOALS);
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_DUE).equals(Appraisal.STAGE_GOALS);
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_OVERDUE).equals(Appraisal.STAGE_GOALS);
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_REACTIVATED).equals(Appraisal.STAGE_GOALS);
        assert Appraisal.getStage(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION).equals(Appraisal.STAGE_GOALS);
    }

    public void shouldReturnStatusWithoutDueOverdueForStage() {
        assert Appraisal.getStage(Appraisal.STATUS_RESULTS_DUE).equals(Appraisal.STAGE_RESULTS);
        assert Appraisal.getStage(Appraisal.STATUS_SIGNATURE_DUE).equals(Appraisal.STAGE_SIGNATURE);
        assert Appraisal.getStage(Appraisal.STATUS_RELEASE_OVERDUE).equals(Appraisal.STAGE_RELEASE);
    }

    public void shouldReturnSameReportTitleForAStageBasedReport() {
        HashMap paramMap = new HashMap();
        paramMap.put(ReportsAction.REPORT, "stageOverdue");
        assert ReportMgr.getReportTitle(paramMap).equals("report-title-stageOverdue");

        paramMap.put(ReportsAction.SCOPE_VALUE, ReportsAction.SCOPE_BC);
        assert ReportMgr.getReportTitle(paramMap).equals("report-title-stageOverdue");
    }

    public void shouldReturnDifferentReportTitleForAUnitBasedReport() {
        HashMap paramMap = new HashMap();
        paramMap.put(ReportsAction.REPORT, "unitOverdue");
        paramMap.put(ReportsAction.SCOPE, ReportsAction.SCOPE_BC);
        assert ReportMgr.getReportTitle(paramMap).equals("report-title-unitOverduebc");

        paramMap.put(ReportsAction.SCOPE, ReportsAction.SCOPE_ORG_CODE);
        assert ReportMgr.getReportTitle(paramMap).equals("report-title-unitOverdueorgCode");
    }

    public void shouldProduceSQLForSortedUnitOrStatus() {
        String scope = ReportsAction.SCOPE_ORG_CODE;

        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown", false, true);
        String expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  " +
                "WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode  " +
                "GROUP BY PYVPASJ_SUPERVISOR_PIDM ORDER BY PYVPASJ_SUPERVISOR_PIDM, count(*) DESC";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue", false, true);
        expectedSQL = "SELECT count(*), status FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 30 GROUP BY status ORDER BY status, count(*) DESC";
        assert sql.equals(expectedSQL);
    }

    public void shouldCombineAndSortStagesCorrectly() {
        List<Object[]> mixedData = new ArrayList<Object[]>();
        List<Object[]> combinedSortedData = new ArrayList<Object[]>();

        Object[] row1 = {5, "goals"};
        Object[] row2 = {10, "results"};
        Object[] row3 = {5, "goals"};
        Object[] row4 = {30, "goals"};
        Object[] row5 = {30, "results"};

        mixedData.add(row1);
        mixedData.add(row2);
        mixedData.add(row3);
        mixedData.add(row4);
        mixedData.add(row5);

        combinedSortedData = ReportMgr.combineAndSortStages(mixedData);

        assert combinedSortedData.size() == 2;
        assert combinedSortedData.get(0)[1].equals("goals");
        assert combinedSortedData.get(0)[0].equals(40);
        assert combinedSortedData.get(1)[1].equals("results");
        assert combinedSortedData.get(1)[0].equals(40);

        row1[1] = "results";
        row2[1] = "results";
        row3[1] = "goals";
        row4[1] = "goals";
        row5[1] = "results";

        combinedSortedData = ReportMgr.combineAndSortStages(mixedData);

        assert combinedSortedData.size() == 2;
        assert combinedSortedData.get(0)[1].equals("results");
        assert combinedSortedData.get(0)[0].equals(45);
        assert combinedSortedData.get(1)[1].equals("goals");
        assert combinedSortedData.get(1)[0].equals(35);
    }

    public void shouldNotLimitIfDataIsEqualOrLessThanMaxDataPoints() {
        List<Object[]> mixedData = new ArrayList<Object[]>();
        List<Object[]> combinedSortedData = new ArrayList<Object[]>();

        Object[] row1 = {45, "goals"};
        Object[] row2 = {40, "results"};
        Object[] row3 = {35, "review"};
        Object[] row4 = {30, "appraisal"};
        Object[] row5 = {20, "release"};

        mixedData.add(row1);
        mixedData.add(row2);
        mixedData.add(row3);
        mixedData.add(row4);
        mixedData.add(row5);

        combinedSortedData = ReportMgr.trimDataPoints(mixedData, 5);
        assert combinedSortedData.size() == 5;

        combinedSortedData = ReportMgr.trimDataPoints(mixedData, 0);
        assert combinedSortedData.size() == 5;

        combinedSortedData = ReportMgr.trimDataPoints(mixedData, -1);
        assert combinedSortedData.size() == 5;

        combinedSortedData = ReportMgr.trimDataPoints(mixedData, 4);
        assert combinedSortedData.size() == 4;
        assert combinedSortedData.get(0)[0].equals(45);
        assert combinedSortedData.get(0)[1].equals("goals");
        assert combinedSortedData.get(1)[0].equals(40);
        assert combinedSortedData.get(1)[1].equals("results");
        assert combinedSortedData.get(2)[0].equals(35);
        assert combinedSortedData.get(2)[1].equals("review");
        assert combinedSortedData.get(3)[0].equals(50);
        assert combinedSortedData.get(3)[1].equals("other");

    }

    public void shouldProduceCorrectListHQLForRootLevel() {
        String scope;
        String hql;
        String expectedHQL;
        scope = ReportsAction.DEFAULT_SCOPE;

        hql = AppraisalMgr.getReportListHQL(scope, "unitBreakdown", true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_BREAKDOWN, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_OVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes  and overdue > 0" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_OVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_WAYOVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes  and overdue > 30" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_WAYOVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";
    }

    public void shouldProduceCorrectListHQLForBCLevel() {
        String scope;
        String hql;
        String expectedHQL;
        scope = ReportsAction.SCOPE_BC;

        hql = AppraisalMgr.getReportListHQL(scope, "unitBreakdown", true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_BREAKDOWN, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_OVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and overdue > 0" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_OVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_WAYOVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and overdue > 30" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_WAYOVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";
    }

    public void shouldProduceCorrectListHQLForOrgPrefixLevel() {
        String scope = ReportsAction.SCOPE_ORG_PREFIX;
        String hql;
        String expectedHQL;


        hql = AppraisalMgr.getReportListHQL(scope, "unitBreakdown", true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.orgCodeDescription LIKE :orgPrefix" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_BREAKDOWN, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_OVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.orgCodeDescription LIKE :orgPrefix" +
                " and overdue > 0" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_OVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_WAYOVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.orgCodeDescription LIKE :orgPrefix" +
                " and overdue > 30" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_WAYOVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";
    }

    public void shouldProduceCorrectListHQLForOrgCodeLevel() {
        String scope = ReportsAction.SCOPE_ORG_CODE;
        String hql;
        String expectedHQL;


        hql = AppraisalMgr.getReportListHQL(scope, "unitBreakdown", true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.tsOrgCode = :tsOrgCode" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_BREAKDOWN, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_OVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.tsOrgCode = :tsOrgCode" +
                " and overdue > 0" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_OVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";

        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_UNIT_WAYOVERDUE, true);
        expectedHQL = "select new edu.osu.cws.evals.models.Appraisal (" +
                "id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
                " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
                 " from edu.osu.cws.evals.models.Appraisal " +
                " where status not in ('completed', 'archived', 'closed') " +
                " and job.appointmentType in :appointmentTypes " +
                " and job.businessCenterName = :bcName and job.tsOrgCode = :tsOrgCode" +
                " and overdue > 30" +
                " order by job.employee.lastName, job.employee.firstName";
        assert hql.equals(expectedHQL);
        hql = AppraisalMgr.getReportListHQL(scope, ReportsAction.REPORT_STAGE_WAYOVERDUE, true);
        assert hql.equals(expectedHQL) : "The hql should be the same for report & stage";
    }

    public void shouldProductionCorrectChartSQLForSupervisor() {
        String scope = ReportsAction.SCOPE_SUPERVISOR;
        String sql = "";
        String expectedSQL;

        Job job1 = new Job(new Employee(12345), "C12345", "00");
        Job job2 = new Job(new Employee(12345), "C12345", "01");
        ArrayList<Job> jobList = new ArrayList<Job>();
        jobList.add(job1);
        jobList.add(job2);

        expectedSQL = "SELECT SUM(has_appraisal), root_supervisor_pidm from (SELECT " +
                "(case when appraisals.id is not null then 1 else 0 end) as has_appraisal, "+
                "CONNECT_BY_ROOT pyvpasj_pidm as root_supervisor_pidm FROM pyvpasj " +
                "left join appraisals on (appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) WHERE pyvpasj_status = 'A' AND " +
                "pyvpasj_appointment_type in :appointmentTypes " +
                "START WITH (pyvpasj_pidm = 12345 AND pyvpasj_posn = 'C12345' AND " +
                "pyvpasj_suff = '00') OR (pyvpasj_pidm = 12345 AND pyvpasj_posn = 'C12345' " +
                "AND pyvpasj_suff = '01')CONNECT BY "+
                "pyvpasj_supervisor_pidm = prior pyvpasj_pidm AND "+
                "pyvpasj_supervisor_posn = prior pyvpasj_posn AND "+
                "pyvpasj_supervisor_suff = prior pyvpasj_suff ) GROUP BY root_supervisor_pidm"+
                " ORDER BY sum(has_appraisal) DESC, root_supervisor_pidm";

        sql = ReportMgr.getSupervisorChartSQL(scope, ReportsAction.REPORT_UNIT_BREAKDOWN,
                true, 2, false);
        assert sql.equals(expectedSQL);

        expectedSQL = "SELECT SUM(has_appraisal), status from (SELECT " +
                "(case when appraisals.id is not null then 1 else 0 end) as has_appraisal, "+
                "appraisals.status FROM pyvpasj " +
                "left join appraisals on (appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) WHERE pyvpasj_status = 'A' AND " +
                "pyvpasj_appointment_type in :appointmentTypes " +
                "START WITH (pyvpasj_pidm = 12345 AND pyvpasj_posn = 'C12345' AND " +
                "pyvpasj_suff = '00') OR (pyvpasj_pidm = 12345 AND pyvpasj_posn = 'C12345' " +
                "AND pyvpasj_suff = '01')CONNECT BY "+
                "pyvpasj_supervisor_pidm = prior pyvpasj_pidm AND "+
                "pyvpasj_supervisor_posn = prior pyvpasj_posn AND "+
                "pyvpasj_supervisor_suff = prior pyvpasj_suff ) GROUP BY status"+
                " ORDER BY sum(has_appraisal) DESC, status";

        sql = ReportMgr.getSupervisorChartSQL(scope, ReportsAction.REPORT_STAGE_BREAKDOWN,
                true, 2, false);
        assert sql.equals(expectedSQL);
    }
}
