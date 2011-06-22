package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.Appraisals;
import edu.osu.cws.pass.util.Employees;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

@Test
public class AppraisalsTest {

    Appraisal appraisal = new Appraisal();
    Appraisals appraisals = new Appraisals();
    Employees employees = new Employees();
    Employee employee = new Employee();
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
    }

    /**
     * This method tests that the Appraisals class can create an appraisal given a Job object.
     *
     * @throws Exception
     */
    @Test(groups = {"unittest"}, dataProvider = "job")
    public void shouldCreateAnAppraisal(Job job) throws Exception {
        assert appraisals.createAppraisal(job, Appraisal.TYPE_ANNUAL) != 0 :
                "Appraisals.createAppraisal should return id of appraisal";
    }

    @Test(groups = {"unittest"},  expectedExceptions = ModelException.class)
    public void appraisalShouldRequireValidJob() throws Exception {
        Job invalidJob = new Job();

        assert appraisals.createAppraisal(invalidJob, Appraisal.TYPE_ANNUAL) != 0 :
                "Appraisals.createAppraisal should require valid Job";
    }

    /**
     * TestNG Dataprovider, returns an array of Jobs to be used in this test class.
     * @return
     */
    @DataProvider(name = "job")
    public Object[][] loadJob() {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        return new Object[][] {
                {job}
        };
    }

    /**
     * Method that builds an Appraisal with a bunch of data to save
     *
     * @return
     */
    public Appraisal loadAppraisalSaveList() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        int appraisalID =  appraisals.createAppraisal(job, Appraisal.TYPE_ANNUAL);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();

        employee = employees.findByOnid("luf");
        updatedAppraisal.setId(appraisalID);

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalsComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);
        updatedAppraisal.setReviewer(employee);
        updatedAppraisal.setReviewSubmitDate(new Date());
        updatedAppraisal.setReviewStatusID("review id");
        updatedAppraisal.setReview("hr comments text");
        updatedAppraisal.setRebuttal("employee comments");
        updatedAppraisal.setEmployeeSignedDate(new Date());
        updatedAppraisal.setCloseOutDate(new Date());
        updatedAppraisal.setCloseOutBy(employee);
        updatedAppraisal.setCloseOutReason("close out reason");

        return updatedAppraisal;
    }

    /**
     * This method tests that the updateAppraisal throws an exception when an extra field is being updated that is
     * not allowed.
     *
     * @throws edu.osu.cws.pass.models.ModelException If there is a problem validation data
     */
    @Test(groups = {"unitttest"})
    public void updateAppraisalModelData()
            throws Exception {
        Appraisal modifiedAppraisal = loadAppraisalSaveList();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        appraisals.updateAppraisal(modifiedAppraisal);
        tx.commit();
        // no exception means success
    }

    public Appraisal loadAppraisalAssessments() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        int appraisalID =  appraisals.createAppraisal(job, Appraisal.TYPE_ANNUAL);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();
        employee = employees.findByOnid("luf");

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalsComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);

        for (Assessment assessment : updatedAppraisal.getAssessments()) {
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }
        return updatedAppraisal;
    }

    @Test(groups = {"unittest"})
    public void shouldUpdateAppraisalWithResults()
            throws Exception {

        // Create the appraisal for this test
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();
        int appraisalID =  appraisals.createAppraisal(job, Appraisal.TYPE_ANNUAL);

        // Grab the freshly created appraisal from the db before we start
        // updating the properties.
        employee = employees.findByOnid("luf");
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalsComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);

        for (Assessment assessment : updatedAppraisal.getAssessments()) {
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }

        appraisals.updateAppraisal(updatedAppraisal);
        tx.commit();


        for (Assessment assessment : updatedAppraisal.getAssessments()) {
            assert assessment.getEmployeeResult() != null :
                    "Appraisal assessments employee result failed to save";
            assert assessment.getSupervisorResult() != null :
                    "Appraisal assessments supervisor result failed to save";
        }
    }

    @Test(groups = {"unittest"})
    /**
     * Edits an appraisal twice and checks to make sure that the goals are logged.
     */
    public void shouldCreateGoalLog()
            throws Exception {

        Appraisal modifiedAppraisal = loadAppraisalAssessments();

        // Editing the goals for the first time
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setGoal("first edit of goal");
        }
        appraisals.setLoggedInUser(modifiedAppraisal.getJob().getEmployee());
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        appraisals.updateAppraisal(modifiedAppraisal);
        tx.commit();
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assert assessment.getGoal() != null :
                    "Appraisal assessments goals failed to save";
            assert assessment.getGoalLogs().size() == 1 :
                    "Appraisal assessment goals should have a new log";
        }

        // Editing the goals for the second time
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setGoal("second edit of goal");
        }
        appraisals.setLoggedInUser(new Employees().findByOnid("luf"));
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        appraisals.updateAppraisal(modifiedAppraisal);
        tx.commit();
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assert assessment.getGoal().equals("second edit of goal") :
                    "Appraisal assessments goals failed to save";
            assert assessment.getGoalLogs().size() == 2 :
                    "Appraisal assessment goals should have a new log";
        }
    }

    @Test(groups = "unittest")
    public void shouldFindAllEmployeeActiveAppraisals() throws Exception {
        int pidm = 12345;
        ArrayList<HashMap> myActiveAppraisals = appraisals.getAllMyActiveAppraisals(pidm);
        assert myActiveAppraisals.size() == 4 : "Invalid size of active appraisals";
        for (HashMap ap : myActiveAppraisals) {
            assert ap.get("id") != new Integer(0) : "id should be present in list of appraisals";
            assert ap.get("jobTitle") != null : "job title should be present in list of appraisals";
            assert ap.get("startDate") != null : "start date should be present in list of appraisals";
            assert ap.get("endDate") != null : "end date should be present in list of appraisals";
            assert ap.get("status") != null : "status should be present in list of appraisals";
        }
    }

    @Test(groups = "unittest")
    public void shouldFindAllTeamActiveAppraisals() throws Exception {
        int pidm = 12467;
        List<HashMap> teamActiveAppraisals = appraisals.getMyTeamsActiveAppraisals(pidm);
        assert teamActiveAppraisals.size() == 4 : "Invalid size of team active appraisals";
        for (HashMap ap : teamActiveAppraisals) {
            assert ap.get("id") != new Integer(0) :
                    "id should be present in list of team appraisals";
            //@todo: should this be use jobTitle instead? check my notes
            assert ap.get("jobTitle") != null : "" +
                    "job title should be present in list of team appraisals";
            assert ap.get("startDate") != null :
                    "start date should be present in list of team appraisals";
            assert ap.get("endDate") != null :
                    "end date should be present in list of team appraisals";
            assert ap.get("status") != null :
                    "status should be present in list of team appraisals";
            assert ap.get("employeeName") != null :
                    "employee name should be present in list of team appraisals";
            assert ap.get("appointmentType") != null :
                    "appointment type name should be present in list of team appraisals";
        }
    }

    public void shouldReturnEmptyStringWhenPidmHasNoRole() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);

        int invalidPidm = 1111;
        assert appraisals.getRole(appraisal, invalidPidm).equals("");
        tx.commit();
    }

    public void shouldDetectEmployeeRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);
        tx.commit();

        assert appraisals.getRole(appraisal, 12345).equals("employee");
    }

    public void shouldDetectReviewerRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);

        assert appraisals.getRole(appraisal, 787812).equals("reviewer");
        tx.commit();
    }

    public void shouldOnlyIncludeReviewDueOrReviewPastDueInAppraisalReviewList() throws Exception {
        for (HashMap appraisal : appraisals.getReviews("UABC")) {
            assert appraisal.get("status").equals("review-due")
                    || appraisal.get("status").equals("review-past-due");
        }
    }

    public void getReviewsShouldIncludeOnlyNeededFields() throws Exception {
        for (HashMap appraisal : appraisals.getReviews("UABC")) {
            assert appraisal.containsKey("id") : "Missing appraisalID";
            assert appraisal.containsKey("employeeName") : "Missing employeeName";
            assert appraisal.containsKey("jobTitle") : "Missing jobTitle";
            assert appraisal.containsKey("status") : "Missing status";
            assert appraisal.containsKey("evaluationSubmitDate") : "Missing evaluationSubmitDate";
            assert appraisal.keySet().size() == 5 :
                    "Incorrect amount of values returned by getReviews";
        }
    }

    public void shouldReturnCorrectReviewCount() throws Exception {
        assert appraisals.getReviewCount("UABC") == 2;
        assert appraisals.getReviewCount("foobar") == 0;
    }

}
