package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AppraisalMgr;
import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

@Test
public class AppraisalsTest {

    Appraisal appraisal = new Appraisal();
    AppraisalMgr appraisalMgr = new AppraisalMgr();
    EmployeeMgr employeeMgr = new EmployeeMgr();
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
     * This method tests that the AppraisalMgr class can create an appraisal given a Job object.
     *
     * @throws Exception
     */
    @Test(groups = {"unittest"}, dataProvider = "job")
    public void shouldCreateAnAppraisal(Job job) throws Exception {
        assert appraisalMgr.createAppraisal(job, Appraisal.TYPE_ANNUAL).getId() != 0 :
                "AppraisalMgr.createAppraisal should return id of appraisal";
    }

    @Test(groups = {"unittest"},  expectedExceptions = ModelException.class)
    public void appraisalShouldRequireValidJob() throws Exception {
        Job invalidJob = new Job();

        assert appraisalMgr.createAppraisal(invalidJob, Appraisal.TYPE_ANNUAL).getId() != 0 :
                "AppraisalMgr.createAppraisal should require valid Job";
    }

    /**
     * TestNG Dataprovider, returns an array of Jobs to be used in this test class.
     * @return
     */
    @DataProvider(name = "job")
    public Object[][] loadJob() {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
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
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();

        int appraisalID =  appraisalMgr.createAppraisal(job, Appraisal.TYPE_ANNUAL).getId();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();

        employee = employeeMgr.findByOnid("luf");
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
        appraisalMgr.updateAppraisal(modifiedAppraisal);
        tx.commit();
        // no exception means success
    }

    public Appraisal loadAppraisalAssessments() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();

        int appraisalID =  appraisalMgr.createAppraisal(job, Appraisal.TYPE_ANNUAL).getId();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();
        employee = employeeMgr.findByOnid("luf");

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
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();
        int appraisalID =  appraisalMgr.createAppraisal(job, Appraisal.TYPE_ANNUAL).getId();

        // Grab the freshly created appraisal from the db before we start
        // updating the properties.
        employee = employeeMgr.findByOnid("luf");
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

        appraisalMgr.updateAppraisal(updatedAppraisal);
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
        appraisalMgr.setLoggedInUser(modifiedAppraisal.getJob().getEmployee());
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        appraisalMgr.updateAppraisal(modifiedAppraisal);
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
        appraisalMgr.setLoggedInUser(new EmployeeMgr().findByOnid("luf"));
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        appraisalMgr.updateAppraisal(modifiedAppraisal);
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
        ArrayList<Appraisal> myActiveAppraisals = appraisalMgr.getAllMyActiveAppraisals(pidm);
        assert myActiveAppraisals.size() == 4 : "Invalid size of active appraisals";
        for (Appraisal ap : myActiveAppraisals) {
            assert ap.getId() != new Integer(0) : "id should be present in list of appraisals";
            assert ap.getJob().getJobTitle() != null : "job title should be present in list of appraisals";
            assert ap.getStartDate() != null : "start date should be present in list of appraisals";
            assert ap.getEndDate() != null : "end date should be present in list of appraisals";
            assert ap.getStatus() != null : "status should be present in list of appraisals";
        }
    }

    @Test(groups = "unittest")
    public void shouldFindAllTeamActiveAppraisals() throws Exception {
        int pidm = 12467;
        List<Appraisal> teamActiveAppraisals = appraisalMgr.getMyTeamsAppraisals(pidm, true);
        assert teamActiveAppraisals.size() == 4 : "Invalid size of team active appraisals";
        for (Appraisal ap : teamActiveAppraisals) {
            assert ap.getId() != 0 :
                    "id should be present in list of team appraisals";
            //@todo: should this be use jobTitle instead? check my notes
            assert ap.getJob().getJobTitle() != null : "" +
                    "job title should be present in list of team appraisals";
            assert ap.getStartDate() != null :
                    "start date should be present in list of team appraisals";
            assert ap.getEndDate() != null :
                    "end date should be present in list of team appraisals";
            assert ap.getStatus() != null :
                    "status should be present in list of team appraisals";
            assert ap.getJob().getEmployee().getFirstName() != null :
                    "employee first name should be present in list of team appraisals";
            assert ap.getJob().getEmployee().getLastName() != null :
                    "employee last name should be present in list of team appraisals";
            assert ap.getJob().getAppointmentType() != null :
                    "appointment type name should be present in list of team appraisals";
        }
    }

    public void shouldReturnEmptyStringWhenPidmHasNoRole() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);

        int invalidPidm = 1111;
        assert appraisalMgr.getRole(appraisal, invalidPidm).equals("");
        tx.commit();
    }

    public void shouldDetectEmployeeRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);
        tx.commit();

        assert appraisalMgr.getRole(appraisal, 12345).equals("employee");
    }

    public void shouldDetectReviewerRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);

        assert appraisalMgr.getRole(appraisal, 787812).equals("reviewer");
        tx.commit();
    }

    public void shouldOnlyIncludeReviewDueOrReviewPastDueInAppraisalReviewList() throws Exception {
        for (Appraisal appraisal : appraisalMgr.getReviews("UABC")) {
            assert appraisal.getStatus().equals("reviewDue")
                    || appraisal.getStatus().equals("reviewOverdue");
        }
    }

    public void getReviewsShouldIncludeOnlyNeededFields() throws Exception {
        //@todo: a couple of extra fields were added to the reviews: supervisor first/last name and tsOrgCode
        for (Appraisal appraisal : appraisalMgr.getReviews("UABC")) {
            assert appraisal.getId() != 0 : "Missing appraisalID";
            assert !appraisal.getJob().getEmployee().getName().equals("") : "Missing employeeName";
            assert !appraisal.getJob().getJobTitle().equals("") : "Missing jobTitle";
            assert !appraisal.getStatus().equals("") : "Missing status";
            assert appraisal.getEvaluationSubmitDate() != null : "Missing evaluationSubmitDate";
        }
    }

    public void shouldReturnCorrectReviewCount() throws Exception {
        assert appraisalMgr.getReviewCount("UABC") == 2;
        assert appraisalMgr.getReviewCount("foobar") == 0;
    }

    /**
     * This test creates one appraisal for each job in the db.
     *
     * @throws Exception
     */
/*    public void createAppraisals() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        List<Job> results = (List<Job>) session.createQuery("from edu.osu.cws.pass.models.Job where status = 'A'").list();
        for (Job job : results) {
            appraisalMgr.createAppraisal(job, Appraisal.TYPE_ANNUAL);
        }
    }*/
}
