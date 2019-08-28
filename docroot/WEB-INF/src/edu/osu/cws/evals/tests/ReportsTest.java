package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.ReportMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.portlet.ReportsAction;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Test
public class ReportsTest {

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
        assert combinedSortedData.get(0)[1].equals("results");
        assert combinedSortedData.get(0)[0].equals(40);
        assert combinedSortedData.get(1)[1].equals("goals");
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
}
