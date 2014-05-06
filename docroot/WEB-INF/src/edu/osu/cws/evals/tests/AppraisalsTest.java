package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.ConfigurationMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

@Test
public class AppraisalsTest {

    Appraisal appraisal = new Appraisal();
    Employee employee = new Employee();
    Transaction tx;
    Session session;
    Job job;
    
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
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (session.isOpen()) {
            tx.commit();
        }
    }
    /**
     * This method tests that the AppraisalMgr class can create an appraisal given a Job object.
     *
     * @throws Exception
     */
    @Test(groups = {"unittest"})
    public void shouldCreateAnAppraisal() throws Exception {
        assert AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(),  Appraisal.TYPE_ANNUAL).getId() != 0 :
                "AppraisalMgr.createAppraisal should return id of appraisal";
    }

    /**
     * TestNG Dataprovider, returns an array of Jobs to be used in this test class.
     * @return
     */
    @DataProvider(name = "job")
    public Object[][] loadJob() {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx1 = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx1.commit();

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
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        Appraisal appraisal1 = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        int appraisalID =  appraisal1
                .getId();
        hsession = HibernateUtil.getCurrentSession();
        hsession.evict(appraisal1);
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);

        employee = EmployeeMgr.findByOnid("luf", null);
        updatedAppraisal.setId(appraisalID);

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.getUnapprovedGoalsVersion().setGoalsComments("goal comments data");
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

        GoalVersion firstGoalVersion = (GoalVersion) appraisal.getGoalVersions().toArray()[0];
        firstGoalVersion.setRequestDecision(true);
        firstGoalVersion.setGoalsApprovedDate(new Date());

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setGoal("foobar");
        }

        return updatedAppraisal;
    }

    /**
     * This method tests that the updateAppraisal throws an exception when an extra field is being updated that is
     * not allowed.
     *
     * @throws edu.osu.cws.evals.models.ModelException If there is a problem validation data
     */
    @Test(groups = {"unitttest"})
    public void updateAppraisalModelData()
            throws Exception {
        Appraisal modifiedAppraisal = loadAppraisalSaveList();
        Employee loggedInUser = EmployeeMgr.findByOnid("luf", null);
        AppraisalMgr.updateAppraisal(modifiedAppraisal, loggedInUser);
        // no exception means success
    }

    public Appraisal loadAppraisalAssessments() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        Appraisal appraisal =  AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        Integer appraisalID = appraisal.getId();
        hsession.evict(appraisal);
        appraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        employee = EmployeeMgr.findByOnid("luf", null);

        appraisal.setEvaluator(employee);
        appraisal.setGoalApprovedDate(new Date());
        appraisal.getUnapprovedGoalsVersion().setGoalsComments("goal comments data");
        appraisal.setResultSubmitDate(new Date());
        appraisal.setEvaluation("evaluation text");
        appraisal.setRating(1);

        GoalVersion firstGoalVersion = (GoalVersion) appraisal.getGoalVersions().toArray()[0];
        firstGoalVersion.setRequestDecision(true);

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }
        return appraisal;
    }

    @Test(groups = {"unittest"})
    public void shouldUpdateAppraisalWithResults()
            throws Exception {

        // Create the appraisal for this test
        Session hsession = HibernateUtil.getCurrentSession();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        Appraisal appraisal1 = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        int appraisalID =  appraisal1.getId();

        // Grab the freshly created appraisal from the db before we start
        // updating the properties.
        employee = EmployeeMgr.findByOnid("luf", null);

        appraisal1.setEvaluator(employee);
        appraisal1.setGoalApprovedDate(new Date());
        //appraisal1.getUnapprovedGoalsVersion().setGoalsComments("goal comments data");
        appraisal1.setResultSubmitDate(new Date());
        appraisal1.setEvaluation("evaluation text");
        appraisal1.setRating(1);
        GoalVersion firstGoalVersion = (GoalVersion) appraisal.getGoalVersions().toArray()[0];
        firstGoalVersion.setRequestDecision(true);

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setGoal("foobar");
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }

        AppraisalMgr.updateAppraisal(appraisal1, employee);
        tx.commit();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        appraisal1 = (Appraisal) hsession.load(Appraisal.class, appraisalID);

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assert assessment.getEmployeeResult() != null :
                    "Appraisal assessments employee result failed to save";
            assert assessment.getSupervisorResult() != null :
                    "Appraisal assessments supervisor result failed to save";
        }
    }


    @Test(groups = {"unittest"})
    public void shouldResetAppraisal() throws Exception {
        // Create the appraisal for this test
        Session hsession = HibernateUtil.getCurrentSession();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        Appraisal appraisal1 = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        int appraisalID =  appraisal1.getId();

        // Grab the freshly created appraisal from the db before we start
        // updating the properties.
        employee = EmployeeMgr.findByOnid("luf", null);

        appraisal1.setEvaluator(employee);
        appraisal1.setGoalApprovedDate(new Date());
        //appraisal1.getUnapprovedGoalsVersion().setGoalsComments("goal comments data");
        appraisal1.setResultSubmitDate(new Date());
        appraisal1.setEvaluation("evaluation text");
        appraisal1.setRating(1);
        GoalVersion firstGoalVersion = (GoalVersion) appraisal1.getGoalVersions().toArray()[0];
        firstGoalVersion.setRequestDecision(true);

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setGoal("foobar");
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }

        AppraisalMgr.updateAppraisal(appraisal1, employee);

        hsession.save(appraisal1);
        tx.commit();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        appraisal1 = (Appraisal) hsession.load(Appraisal.class, appraisalID);

        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assert assessment.getEmployeeResult() != null :
                    "Appraisal assessments employee result failed to save";
            assert assessment.getSupervisorResult() != null :
                    "Appraisal assessments supervisor result failed to save";
        }

        // Reset appraisal
        AppraisalMgr.resetAppraisal(appraisal1);
        firstGoalVersion = (GoalVersion) appraisal1.getGoalVersions().toArray()[0];

        tx.commit();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();

        assert appraisal1.getGoalVersions().size() == 1 : "Appraisal goal versions failed to reset!";
        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assert assessment.getEmployeeResult() == null :
                    "Appraisal assessments employee result failed to reset!";
            assert assessment.getSupervisorResult() == null :
                    "Appraisal assessments supervisor result failed to reset!";
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
        GoalVersion firstGoalVersion = (GoalVersion) modifiedAppraisal.getGoalVersions().toArray()[0];
        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setGoal("first edit of goal");
        }
        AppraisalMgr.updateAppraisal(modifiedAppraisal, modifiedAppraisal.getJob().getEmployee());
        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assert assessment.getGoal() != null :
                    "Appraisal assessments goals failed to save";
            assert assessment.getGoalLogs().size() == 1 :
                    "Appraisal assessment goals should have a new log";
        }

        // Editing the goals for the second time
        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assessment.setGoal("second edit of goal");
        }
        Employee loggedInUser = EmployeeMgr.findByOnid("luf", null);
        AppraisalMgr.updateAppraisal(modifiedAppraisal, loggedInUser);
        for (Assessment assessment : firstGoalVersion.getAssessments()) {
            assert assessment.getGoal().equals("second edit of goal") :
                    "Appraisal assessments goals failed to save";
            assert assessment.getGoalLogs().size() == 2 :
                    "Appraisal assessment goals should have a new log";
        }
    }

    @Test(groups = "unittest")
    public void shouldFindAllEmployeeActiveAppraisals() throws Exception {
        int pidm = 12345;
        ArrayList<Appraisal> myActiveAppraisals = AppraisalMgr.getAllMyActiveAppraisals(pidm,
                null, null);
        assert myActiveAppraisals.size() == 7 : "Invalid size of active appraisals";
        for (Appraisal ap : myActiveAppraisals) {
            assert ap.getId() != new Integer(0) : "id should be present in list of appraisals";
            assert ap.getJob().getJobTitle() != null : "job title should be present in list of appraisals";
            assert ap.getStartDate() != null : "start date should be present in list of appraisals";
            assert ap.getEndDate() != null : "end date should be present in list of appraisals";
            assert ap.getStatus() != null : "status should be present in list of appraisals";
        }
    }

    @Test(groups = "unittest")
    public void shouldFindAllEmployeeAppraisals() throws Exception {
        int pidm = 12345;
        ArrayList<Appraisal> myAppraisals = AppraisalMgr.getAllMyAppraisals(pidm,
                null, null, false);
        assert myAppraisals.size() == 8 : "Invalid size of appraisals";
        for (Appraisal ap : myAppraisals) {
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
        List<Appraisal> teamActiveAppraisals = AppraisalMgr.getMyTeamsAppraisals(pidm, true,
                null, null);
        assert teamActiveAppraisals.size() == 7 : "Invalid size of team active appraisals";
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

    public void shouldOnlyIncludeReviewDueOrReviewPastDueInAppraisalReviewList() throws Exception {
        for (Appraisal appraisal : AppraisalMgr.getReviews("UABC", -1)) {
            assert appraisal.getStatus().equals(Appraisal.STATUS_REVIEW_DUE)
                    || appraisal.getStatus().equals(Appraisal.STATUS_REVIEW_OVERDUE);
        }
    }

    public void getReviewsShouldIncludeOnlyNeededFields() throws Exception {
        //@todo: a couple of extra fields were added to the reviews: supervisor first/last name and tsOrgCode
        for (Appraisal appraisal : AppraisalMgr.getReviews("UABC", -1)) {
            assert appraisal.getId() != 0 : "Missing appraisalID";
            assert !appraisal.getJob().getEmployee().getName().equals("") : "Missing employeeName";
            assert !appraisal.getJob().getJobTitle().equals("") : "Missing jobTitle";
            assert !appraisal.getStatus().equals("") : "Missing status";
            assert appraisal.getEvaluationSubmitDate() != null : "Missing evaluationSubmitDate";
        }
    }

    public void shouldReturnCorrectReviewCount() throws Exception {
        assert AppraisalMgr.getReviewCount("UABC") == 2;
        assert AppraisalMgr.getReviewCount("foobar") == 0;
    }

    /**
     * This test creates one appraisal for each job in the db.
     *
     * @throws Exception
     */
/*    public void createAppraisals() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Configuration goalsDueConfig = (Configuration) session.load(Configuration.class, 1);
        List<Job> results = (List<Job>) session.createQuery("from edu.osu.cws.evals.models.Job where status = 'A'").list();
        for (Job job : results) {
            AppraisalMgr.createAppraisal(job, new Date(), Appraisal.TYPE_ANNUAL, goalsDueConfig);
        }
    }*/

    @Test(groups = {"unittest"})
    public void shouldSetTheStartDateWhenCreatingAppraisal() throws Exception {
        DateTime today = EvalsUtil.getToday();
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(today.toDate());
    }

    @Test(groups = {"unittest"},
            expectedExceptions = {ModelException.class})
    public void shouldOnlyCreateTwoTypesOfAppraisals() throws Exception {
        DateTime today = EvalsUtil.getToday();
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal != null;

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal != null;

        appraisal = AppraisalMgr.createAppraisal(job, today, "invalid type");
        assert appraisal == null;

    }

    @Test(groups = {"unittest"})
    public void shouldUseStartDatePlusNumberOfMonthsInTrialIndToSetTheEndDateForTrialAppraisal()
            throws Exception {
        DateTime today = EvalsUtil.getToday();
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_TRIAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal.getStartDate().equals(today.toDate()) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 6 months.";

        job.setTrialInd(9);
        endDate = job.getEndEvalDate(today, Appraisal.TYPE_TRIAL);
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal.getStartDate().equals(today.toDate()) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 9 months.";
    }

    @Test(groups = {"unittest"})
    public void shouldUseStartDatePlusNumberOfMonthsInAnnualIndToSetTheEndDateForFirstAnnualAppraisal()
            throws Exception {
        DateTime today = EvalsUtil.getToday();
        job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        job.setAnnualInd(12);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal.getStartDate().equals(today.toDate()) : "Start date should be set correctly.";

        // for some reason I couldn't fetch the job.
        DateTime expectedStartDate = EvalsUtil.getToday().withYear(2010).withTimeAtStartOfDay()
                .withMonthOfYear(DateTimeConstants.FEBRUARY).withDayOfMonth(1);
        DateTime expectedEndDate = expectedStartDate.plusMonths(12).minusDays(1);
        appraisal = AppraisalMgr.createInitialAppraisalAfterTrial(appraisal);
        assert appraisal.getStartDate().equals(expectedStartDate.toDate()) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(expectedEndDate.toDate()) : "End date should have been today + 12 months.";

        job.setAnnualInd(18);
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_INITIAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        appraisal = AppraisalMgr.createInitialAppraisalAfterTrial(appraisal);
        expectedEndDate = expectedStartDate.plusMonths(18).minusDays(1);
        assert appraisal.getStartDate().equals(expectedStartDate.toDate()) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(expectedEndDate.toDate()) : "End date should have been today + 18 months.";
    }

    @Test(groups = {"unittest"})
    public void shouldUseTwelveMonthsForAllAnnualAppraisalsAfterTheFirstOne()
            throws Exception {
        DateTime today = EvalsUtil.getToday();
        job.setAnnualInd(100);
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_ANNUAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(today.toDate()) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) :
                "End date should have been today + 12 months for annual appraisals.";
    }

    @Test(groups = {"unittest"})
    public void shouldSetStatusToGoalsDueOnCreation()
            throws Exception{
        String startPointString = "10/29/2011";
        DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_FORMAT_FULL);
        DateTime startPointDate = fmt.parseDateTime(startPointString);

        appraisal = AppraisalMgr.createAppraisal(job, startPointDate, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(startPointDate.toDate()) : "Start date should be set correctly.";
        assert appraisal.getStatus().equals(Appraisal.STATUS_GOALS_DUE) :
                "appraisal status should have been appraisalDue, instead got - " + appraisal.getStatus();
    }

    @Test(groups = {"unittest"})
    public void shouldSetStatusToGoalsDueIfAppraisalIsAfterNov1st2011AndGoalsAreNotDueInPast()
            throws Exception {
        String startPointString = "11/01/2012";
        DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_FORMAT_FULL);
        DateTime startPointDate = fmt.parseDateTime(startPointString);

        appraisal = AppraisalMgr.createAppraisal(job, startPointDate, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(startPointDate.toDate()) : "Start date should be set correctly.";
        assert appraisal.getStatus().equals(Appraisal.STATUS_GOALS_DUE) :
                "appraisal status should have been goalsDue, instead got - " + appraisal.getStatus();
    }

    public void shouldSaveOverdue() throws Exception {
        appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        assert appraisal.getOverdue() == null;
        appraisal.setOverdue(7);
        AppraisalMgr.saveOverdue(appraisal);
        Appraisal appraisal1 = AppraisalMgr.getAppraisal(appraisal.getId());
        assert appraisal1.getOverdue() == 7;
    }

    public void shouldDeleteExistingSalary() throws Exception {
        appraisal = AppraisalMgr.getAppraisal(8);
        AppraisalMgr.createOrUpdateSalary(appraisal, ConfigurationMgr.mapByName());

        String getExistingSalary = "from edu.osu.cws.evals.models.Salary where id = 1";
        String getSalary = "from edu.osu.cws.evals.models.Salary where appraisalId = :appraisal_id";

        assert session.createQuery(getExistingSalary).list().size() == 0 : "should be deleted";
        Salary salary = (Salary) session.createQuery(getSalary)
                .setInteger("appraisal_id", 8).list().get(0);

        assert salary.getAppraisalId() == 8;
        assert salary.getLow() == 2000;
        assert salary.getMidPoint() == 3000;
        assert salary.getHigh() == 4000;
        assert salary.getCurrent() == 2500;
        assert salary.getSgrpCode().equals("123456");
        assert salary.getTwoIncrease() == 2;
        assert salary.getOneMin() == 6;
        assert salary.getOneMax() == 9;
    }

    public void shouldCreateFirstAnnualAppraisalIfTrialAppraisalIsClosedOrCompletedOrRebuttalDue() {
        //@todo: code is written, I just have to test it out
    }

    public void shouldSetStatusOfFirstAnnualAppraisalToGoalsApprovedOrResultsDueOrResultsOverDue() {
        //@todo: code is written, I just have to test it out
    }

    public void shouldCopyAllTheFieldsFromTrialAppraisalWhenCreatingFirstAnnualAppraisal() {
        //@todo: code is written, I just have to test it out
    }

    @Test(groups={"pending"})
    public void shouldOnlyCreateTrialAppraisalIfAndOnlyIfTrialIndIsSet() {}

    @Test(groups={"pending"})
    public void shouldOnlyCreateAnnualAppraisalIfAndOnlyIfAnnualIndIsSet() {}

    @Test(groups={"pending"})
    public void shouldCorrectlyCountReviewsDueBydBC() throws Exception {
        assert AppraisalMgr.getReviewDueCount("AABC") == 0;
        assert AppraisalMgr.getReviewDueCount("UABC") != 0;    }

    @Test(groups={"pending"})
    public void shouldCorrectlyCountReviewsOverueBydBC() throws Exception {
        assert AppraisalMgr.getReviewOvedDueCount("AABC") == 0;
        assert AppraisalMgr.getReviewOvedDueCount("UABC") != 0;
    }

    @Test(groups={"pending"})
    public void shouldDetectIfAJobHasTrialAppraisal() throws Exception {
        //@todo: test trialAppraisalExists
    }

    @Test(groups={"pending"})
    public void shouldDetectIfAJobHasAppraisalWithMatchingStartDateAndType() throws Exception {
        //@todo: test appraisalExists
    }

    @Test(groups={"pending"})
    public void shouldDetectIfAJobHasAnOpenTrialAppraisal() throws Exception {
        Job job = new Job();
        Employee employee = new Employee();
        employee.setId(12345);
        job.setEmployee(employee);
        job.setPositionNumber("1234");
        job.setSuffix("00");

        DateTime startDateTime = EvalsUtil.getToday().withTimeAtStartOfDay().withDayOfMonth(14)
                .withMonthOfYear(DateTimeConstants.MAY).withYear(2010);

        assert AppraisalMgr.appraisalExists(job, startDateTime, "annual");

        job.setPositionNumber("4444");
        assert AppraisalMgr.openTrialAppraisalExists(job);
    }

    @Test(groups={"pending"})
    public void shouldReturnArrayOfOpenIDs() throws Exception {
        assert AppraisalMgr.getOpenIDs().length != 0;
        //@todo: test openIDs
    }

    @Test(groups={"pending"})
    public void shouldUpdateAppraisalStatusAndOriginalStatus() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);
        appraisal.setStatus(Appraisal.STATUS_CLOSED);
        appraisal.setOriginalStatus(Appraisal.STATUS_GOALS_DUE);
        AppraisalMgr.updateAppraisalStatus(appraisal);
    }

    public void shouldReturnSortedListOfApprovedGoalsVersions() {
        appraisal = new Appraisal();
        GoalVersion goalVersion1 = new GoalVersion();
        goalVersion1.setId(1);
        goalVersion1.setCreateDate(new Date());
        goalVersion1.setGoalsApprovedDate(new Date());

        GoalVersion goalVersion2 = new GoalVersion();
        goalVersion2.setId(2);
        goalVersion2.setCreateDate(EvalsUtil.getToday().minusDays(1).toDate());
        goalVersion2.setGoalsApprovedDate(new Date());

        GoalVersion goalVersion3 = new GoalVersion();
        goalVersion3.setId(3);
        goalVersion3.setCreateDate(EvalsUtil.getToday().minusDays(2).toDate());
        goalVersion3.setGoalsApprovedDate(new Date());

        appraisal.addGoalVersion(goalVersion1);
        appraisal.addGoalVersion(goalVersion2);
        appraisal.addGoalVersion(goalVersion3);

        List<GoalVersion> goalVersions= appraisal.getApprovedGoalsVersions();
        assert goalVersions.get(0).getId() == 3;
        assert goalVersions.get(1).getId() == 2;
        assert goalVersions.get(2).getId() == 1;
    }

    public void shouldReturnOnlyApprovedGoals() {
        appraisal = new Appraisal();
        GoalVersion goalVersion1 = new GoalVersion();
        goalVersion1.setId(1);
        goalVersion1.setCreateDate(new Date());
        goalVersion1.setGoalsApprovedDate(new Date());

        GoalVersion goalVersion2 = new GoalVersion();
        goalVersion2.setId(2);
        goalVersion2.setCreateDate(EvalsUtil.getToday().minusDays(1).toDate());

        appraisal.addGoalVersion(goalVersion1);
        appraisal.addGoalVersion(goalVersion2);

        List<GoalVersion> goalVersions= appraisal.getApprovedGoalsVersions();
        assert goalVersions.size() == 1;
        assert goalVersions.get(0).getId() == 1;
    }

    public void shouldReturnSortedListOfUnapprovedGoalsVersions() {
        appraisal = new Appraisal();
        GoalVersion goalVersion1 = new GoalVersion();
        goalVersion1.setId(1);
        goalVersion1.setCreateDate(new Date());
        goalVersion1.setRequestDecision(true);
        appraisal.addGoalVersion(goalVersion1);

        GoalVersion goalVersion = appraisal.getUnapprovedGoalsVersion();
        assert goalVersion.getId() == 1;
    }

    public void shouldReturnOnlyUnapprovedGoals() {
        appraisal = new Appraisal();
        GoalVersion goalVersion1 = new GoalVersion();
        goalVersion1.setId(1);
        goalVersion1.setCreateDate(new Date());
        goalVersion1.setRequestDecision(false);

        GoalVersion goalVersion2 = new GoalVersion();
        goalVersion2.setId(2);
        goalVersion2.setCreateDate(EvalsUtil.getToday().minusDays(1).toDate());
        goalVersion2.setRequestDecision(true);

        appraisal.addGoalVersion(goalVersion1);
        appraisal.addGoalVersion(goalVersion2);

        GoalVersion goalVersion = appraisal.getUnapprovedGoalsVersion();
        assert goalVersion.getId() == 2;
    }
}
