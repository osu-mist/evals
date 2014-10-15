package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.PermissionRuleMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.PermissionRule;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test
public class PermissionRulesTest {

    Transaction tx;

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        Session session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        tx.commit();
    }

    @Test(groups = {"unittest"})
    public void shouldListAllPermissionRules() throws Exception {
        HashMap rules = PermissionRuleMgr.list();
        assert rules.containsKey("goalsDue-employee-Default") : "Invalid key in permissions rules";
        assert rules.containsKey("goalsDue-immediate-supervisor-Default") : "Invalid key in permissions rules";
        assert rules.size() == 2 :
        "PermissionRuleMgr.list() should find all permission rules";

    }

    public void shouldReturnDefaultPermissionRule() throws Exception {
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType("Classified IT");

        PermissionRule rule = PermissionRuleMgr.getPermissionRule(rules, appraisal, "employee");
        assert rule.getAppointmentType().equals("Default");
    }

    public void shouldReturnSpecificPermissionRule() throws Exception {
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType("Professional Faculty");

        PermissionRule rule = PermissionRuleMgr.getPermissionRule(rules, appraisal, "employee");
        assert rule.getAppointmentType().equals("Professional Faculty");
    }

    public void shouldReturnNullWhenNoPermRuleIsFound() throws Exception {
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType("Professional Faculty");

        PermissionRule rule = PermissionRuleMgr.getPermissionRule(rules, appraisal, "supervisor");
        assert rule == null;
    }

    public void shouldReturnNullWhenStatusIsNotSet() throws Exception {
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        Appraisal appraisal = new Appraisal();
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType("Professional Faculty");

        PermissionRule rule = PermissionRuleMgr.getPermissionRule(rules, appraisal, "supervisor");
        assert rule == null;
    }

    public void shouldReturnFalseWhenEvaluationIsNull() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation(null);
        // Assertions
        assert !rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnFalseWhenEvaluationIsEmpty() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("");
        // Assertions
        assert !rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnFalseWhenStatusIsAppraisalDue() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("e");
        rule1.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        // Assertions
        assert !rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnFalseWhenStatusIsEmployeeReviewDue() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_EMPLOYEE_REVIEW_DUE);
        // Assertions
        assert !rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsReleaseDue() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("e");
        rule1.setStatus(Appraisal.STATUS_RELEASE_DUE);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsSignatureDue() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_SIGNATURE_DUE);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsClosed() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_CLOSED);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsCompleted() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_COMPLETED);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsArchivedClosed() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_ARCHIVED_CLOSED);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    public void shouldReturnTrueWhenStatusIsArchivedCompleted() {
        // Setup
        HashMap<String, PermissionRule> rules = getTestRuleMap();
        PermissionRule rule1 = rules.get("goalsDue-employee-Default");
        rule1.setEvaluation("v");
        rule1.setStatus(Appraisal.STATUS_ARCHIVED_COMPLETED);
        // Assertions
        assert rule1.getCanViewEvalReleaseSig();
    }

    private HashMap<String, PermissionRule> getTestRuleMap() {
        HashMap<String, PermissionRule> rules = new HashMap<String, PermissionRule>();

        String key1 = "goalsDue-employee-Default";
        PermissionRule rule1 = new PermissionRule();
        rule1.setAppointmentType("Default");
        rules.put(key1, rule1);

        String key2 = "goalsDue-employee-ProfessionalFaculty";
        PermissionRule rule2 = new PermissionRule();
        rule2.setAppointmentType("Professional Faculty");
        rules.put(key2, rule2);

        return rules;
    }
}
