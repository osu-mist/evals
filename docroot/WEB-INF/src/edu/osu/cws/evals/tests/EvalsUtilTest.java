package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.EmailMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.models.Email;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Test
public class EvalsUtilTest {

    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldReturnInvalidOverdueForGoalsReactivated() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_REACTIVATED);
        int overdue = EvalsUtil.getOverdue(appraisal, new HashMap<String, Configuration>());
        assert overdue == -999 : "Invalid status should have returned -999 for overdue";
    }

    public void shouldReturnInvalidOverdueForGoalsApproved() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_APPROVED);
        int overdue = EvalsUtil.getOverdue(appraisal, new HashMap<String, Configuration>());
        assert overdue == -999 : "Invalid status should have returned -999 for overdue";
    }

    public void shouldReturnCurrentOverdueWhenCompleted() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_COMPLETED);
        appraisal.setOverdue(5);
        int overdue = EvalsUtil.getOverdue(appraisal, new HashMap<String, Configuration>());
        assert overdue == 5 : "It didn't return the existing overdue value";
    }

    public void shouldReturnCurrentOverdueWhenClosed() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_CLOSED);
        appraisal.setOverdue(5);
        int overdue = EvalsUtil.getOverdue(appraisal, new HashMap<String, Configuration>());
        assert overdue == 5 : "It didn't return the existing overdue value";
    }

    public void shouldUseGoalsConfigForGoalsRequiredModification() throws Exception {
        testGetOverdueWithOnlyGoalsDueInMap(Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION);
    }

    public void shouldUseGoalsConfigForGoalsDue() throws Exception {
        testGetOverdueWithOnlyGoalsDueInMap(Appraisal.STATUS_GOALS_DUE);
    }

    private void testGetOverdueWithOnlyGoalsDueInMap(String status) throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(status);

        HashMap<String, Configuration> configurationMap = new HashMap<String, Configuration>();
        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTime(new Date());
        startDateCal.add(Calendar.DAY_OF_MONTH, -60);
        Date startDate = startDateCal.getTime();

        appraisal.setStartDate(startDate);
        Configuration config = new Configuration();
        config.setName("goalsDue");
        config.setValue("30");
        config.setReferencePoint("start");
        config.setAction("add");
        configurationMap.put("goalsDue", config);
        int overdue = EvalsUtil.getOverdue(appraisal, configurationMap);
        assert overdue == 30 : "The number of overdue days was " + overdue + " instead of 30";
    }

    public void testGetOverdueWithGoalsDueInFuture() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus("goalsDue");

        HashMap<String, Configuration> configurationMap = new HashMap<String, Configuration>();
        DateTime startDate = EvalsUtil.getToday().withDayOfMonth(1);

        appraisal.setStartDate(startDate.toDate());
        Configuration config = new Configuration();
        config.setName("goalsDue");
        config.setValue("30");
        config.setReferencePoint("start");
        config.setAction("add");
        configurationMap.put("goalsDue", config);
        int overdue = EvalsUtil.getOverdue(appraisal, configurationMap);
        double expectedDays = Days.daysBetween(startDate.plusMonths(1), EvalsUtil.getToday()).getDays();
        assert overdue == expectedDays :
                "The number of overdue days was " + overdue + " instead of " + expectedDays;
    }

    public void shouldCalculateOverdueForGoalsApprovalOverdue() throws Exception {
        calculateGoalsApprovalOverdueValue(Appraisal.STATUS_GOALS_APPROVAL_OVERDUE);
    }

    public void shouldCalculateOverdueForGoalsApprovalDue() throws Exception {
        String status = Appraisal.STATUS_GOALS_APPROVAL_DUE;
        calculateGoalsApprovalOverdueValue(status);
    }

    public void shouldCalculateIfAnotherEmailNeedsToBeSent() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Email lastEmail = EmailMgr.getLastEmail(1, "goals-submitted");
        tx.commit();
        Configuration config = new Configuration();
        config.setName("goalsDue");
        config.setValue("30");
        config.setReferencePoint("start");
        config.setAction("add");
        assert EvalsUtil.anotherEmail(lastEmail, config): "it should need to send a new email";

        lastEmail.setSentDate(new Date());
        assert !EvalsUtil.anotherEmail(lastEmail, config): "it should not need to send a new email";
    }

    private void calculateGoalsApprovalOverdueValue(String status) throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(status);

        HashMap<String, Configuration> configurationMap = new HashMap<String, Configuration>();
        DateTime startDate = EvalsUtil.getToday().minusDays(60);


        appraisal.setStartDate(startDate.toDate());
        Configuration config = new Configuration();
        config.setName("goalsApprovalDue");
        config.setValue("45");
        config.setReferencePoint("start");
        config.setAction("add");
        configurationMap.put("goalsApprovalDue", config);
        int overdue = EvalsUtil.getOverdue(appraisal, configurationMap);
        assert overdue == 15 : "The number of overdue days was " + overdue + " instead of 30";
    }
}
