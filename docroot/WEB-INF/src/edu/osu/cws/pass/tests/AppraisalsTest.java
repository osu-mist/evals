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

import java.util.Date;

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
    @BeforeClass
    @BeforeMethod
    public void setUp() {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        try {
            dbunit.seedDatabase();
        } catch (Exception e) {}
    }

    /**
     * This method tests that the Appraisals class can create an appraisal given a Job object.
     *
     * @throws Exception
     */
    @Test(groups = {"unittest"}, dataProvider = "job")
    public void shouldCreateAnAppraisal(Job job) throws Exception {
        assert appraisals.createAppraisal(job) != 0 :
                "Appraisals.createAppraisal should return id of appraisal";
    }

    @Test(groups = {"unittest"},  expectedExceptions = ModelException.class)
    public void appraisalShouldRequireValidJob() throws Exception {
        Job invalidJob = new Job();

        assert appraisals.createAppraisal(invalidJob) != 0 :
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
    public Appraisal loadAppraisalSaveList() throws ModelException {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        int appraisalID =  appraisals.createAppraisal(job);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();

        employee = employees.findByOnid("luf");
        updatedAppraisal.setId(appraisalID);

        updatedAppraisal.setGoalApprover(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);
        updatedAppraisal.setHrApprover(employee);
        updatedAppraisal.setHrApprovedDate(new Date());
        updatedAppraisal.setReviewStatusID("review id");
        updatedAppraisal.setHrComments("hr comments text");
        updatedAppraisal.setEmployeeComments("employee comments");
        updatedAppraisal.setEmployeeSignedDate(new Date());
        updatedAppraisal.setEmailType("submit-goals");
        updatedAppraisal.setEmailCount(5);
        updatedAppraisal.setCloseOutDate(new Date());
        updatedAppraisal.setCloseOutBy(employee);
        updatedAppraisal.setCloseOutReason("close out reason");

        return updatedAppraisal;
    }

    /**
     * This method tests that the updateAppraisal throws an exception when an extra field is being updated that is
     * not allowed.
     *
     * @param modifiedAppraisal
     */
    @Test(groups = {"unitttest"})
    public void updateAppraisalModelData()
            throws ModelException {
        Appraisal modifiedAppraisal = loadAppraisalSaveList();
        appraisals.updateAppraisal(modifiedAppraisal);
        assert modifiedAppraisal.getModifiedDate() != null :
                "Appraisal with just appraisal values failed to save";
    }

    public Appraisal loadAppraisalAssessments() throws ModelException {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        int appraisalID =  appraisals.createAppraisal(job);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();
        employee = employees.findByOnid("luf");

        updatedAppraisal.setGoalApprover(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);

        for (Assessment assessment : updatedAppraisal.getAssessments()) {
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }
        return updatedAppraisal;
    }

    /**
     * Returns an appraisal object with some data and assessment results.
     *
     * @return
     * @throws ModelException
     */
    public Appraisal loadAppraisalGoals() throws ModelException {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, 1);
        tx.commit();

        int appraisalID =  appraisals.createAppraisal(job);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();
        employee = employees.findByOnid("luf");

        updatedAppraisal.setGoalApprover(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalComments("goal comments data");
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
            throws ModelException {
        Appraisal modifiedAppraisal = loadAppraisalGoals();
        appraisals.updateAppraisal(modifiedAppraisal);
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
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
            throws ModelException {

        Appraisal modifiedAppraisal = loadAppraisalAssessments();

        // Editing the goals for the first time
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setGoal("first edit of goal");
        }
        appraisals.setLoggedInUser(modifiedAppraisal.getJob().getEmployeePidm());
        appraisals.updateAppraisal(modifiedAppraisal);
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assert assessment.getGoal() != null :
                    "Appraisal assessments goals failed to save";
            assert assessment.getAssessmentLogs().size() == 1 :
                    "Appraisal assessment goals should have a new log";
        }

        // Editing the goals for the second time
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assessment.setGoal("second edit of goal");
        }
        appraisals.setLoggedInUser(new Employees().findByOnid("luf"));
        appraisals.updateAppraisal(modifiedAppraisal);
        for (Assessment assessment : modifiedAppraisal.getAssessments()) {
            assert assessment.getGoal().equals("second edit of goal") :
                    "Appraisal assessments goals failed to save";
            assert assessment.getAssessmentLogs().size() == 2 :
                    "Appraisal assessment goals should have a new log";
        }

    }
}
