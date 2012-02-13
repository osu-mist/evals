package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.portlet.ReportsAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class ReportsTest {
//    @BeforeMethod
//    public void setUp() throws Exception {
//        DBUnit dbunit = new DBUnit();
//        dbunit.seedDatabase();
//    }

    public void shouldProduceCorrectSQLForOSULevel() {
        String sql = ReportMgr.getChartSQL("root", "unitBreakdown");
        String expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " GROUP BY PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "unitOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 0 GROUP BY PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "unitWayOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_BCTR_TITLE FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND appraisals.overdue > 30 GROUP BY PYVPASJ_BCTR_TITLE";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageBreakdown");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 0 GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL("root", "stageWayOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND" +
                " PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND appraisals.overdue > 30 GROUP BY  status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForBCLevel() {
        String scope = ReportsAction.SCOPE_BC;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown");
        String expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ " +
                " WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue");
        expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ " +
                " WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND PYVPASJ_BCTR_TITLE = :bcName AND appraisals.overdue > 0 " +
                "GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue");
        expectedSQL = "SELECT count(*), SUBSTR(PYVPASJ_ORGN_DESC, 1, 3) FROM appraisals, PYVPASJ  " +
                "WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND appraisals.overdue > 30 " +
                "GROUP BY SUBSTR(PYVPASJ_ORGN_DESC, 1, 3)";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status not " +
                "in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number AND " +
                "PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName AND " +
                "appraisals.overdue > 0 GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName" +
                " AND appraisals.overdue > 30 GROUP BY  status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForOrgPrefixLevel() {
        String scope = ReportsAction.SCOPE_ORG_PREFIX;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown");
        String expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND" +
                " PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix " +
                "GROUP BY PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND " +
                "appraisals.overdue > 0 GROUP BY PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_ORGN_CODE_TS FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix " +
                "AND appraisals.overdue > 30 GROUP BY PYVPASJ_ORGN_CODE_TS";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status" +
                " not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE" +
                " in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm AND " +
                "PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND" +
                " appraisals.overdue > 0 GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_DESC LIKE :orgPrefix AND " +
                "appraisals.overdue > 30 GROUP BY  status";
        assert sql.equals(expectedSQL);
    }

    public void shouldProduceCorrectSQLForOrgCodeLevel() {
        String scope = ReportsAction.SCOPE_ORG_CODE;
        String sql = ReportMgr.getChartSQL(scope, "unitBreakdown");
        String expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  " +
                "WHERE appraisals.status not in ('completed', 'archived', 'closed') AND " +
                "PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode  " +
                "GROUP BY PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE " +
                "in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 0 GROUP BY PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "unitWayOverdue");
        expectedSQL = "SELECT count(*), PYVPASJ_SUPERVISOR_PIDM FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm " +
                "AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 30 GROUP BY PYVPASJ_SUPERVISOR_PIDM";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageBreakdown");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE " +
                "appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix  " +
                "AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode  GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE appraisals.status " +
                "not in ('completed', 'archived', 'closed') AND PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes " +
                "AND PYVPASJ_PIDM = appraisals.job_pidm AND PYVPASJ_POSN = appraisals.position_number " +
                "AND PYVPASJ_SUFF = appraisals.job_suffix  AND PYVPASJ_BCTR_TITLE = :bcName AND" +
                " PYVPASJ_ORGN_CODE_TS = :tsOrgCode  AND appraisals.overdue > 0 " +
                "GROUP BY  status";
        assert sql.equals(expectedSQL);

        sql = ReportMgr.getChartSQL(scope, "stageWayOverdue");
        expectedSQL = "SELECT count(*), status  FROM appraisals, PYVPASJ  WHERE" +
                " appraisals.status not in ('completed', 'archived', 'closed') AND" +
                " PYVPASJ_APPOINTMENT_TYPE in :appointmentTypes AND PYVPASJ_PIDM = appraisals.job_pidm" +
                " AND PYVPASJ_POSN = appraisals.position_number AND PYVPASJ_SUFF = appraisals.job_suffix " +
                " AND PYVPASJ_BCTR_TITLE = :bcName AND PYVPASJ_ORGN_CODE_TS = :tsOrgCode " +
                " AND appraisals.overdue > 30 GROUP BY  status";
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
}
