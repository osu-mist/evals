package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
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
    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldCreateAnAppraisal(Job job, Configuration configuration) throws Exception {
        assert AppraisalMgr.createAppraisal(job, new DateTime(),  Appraisal.TYPE_ANNUAL).getId() != 0 :
                "AppraisalMgr.createAppraisal should return id of appraisal";
    }

    @Test(groups = {"unittest"},  expectedExceptions = ModelException.class)
    public void appraisalShouldRequireValidJob() throws Exception {
        Job invalidJob = new Job();

        assert AppraisalMgr.createAppraisal(invalidJob, new DateTime(),  Appraisal.TYPE_ANNUAL).getId() != 0 :
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
     * TestNG Dataprovider, returns an array of Configurations to be used in this test class.
     * @return
     */
    @DataProvider(name = "jobAndGoalsDueConfiguration")
    public Object[][] loadJobAndGoalsDueConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setName(Appraisal.STATUS_GOALS_DUE);
        configuration.setValue("30");
        configuration.setReferencePoint("start");
        configuration.setAction("substract");

        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();

        return new Object[][] {
                {job, configuration}
        };
    }

    /**
     * TestNG Dataprovider, returns an array of Configurations to be used in this test class.
     *
     * @return
     */
    @DataProvider(name = "jobAndGoalsDueAndResultsDueConfiguration")
    public Object[][] loadJobAndGoalsDueAndResultsDueConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setName(Appraisal.STATUS_GOALS_DUE);
        configuration.setValue("30");
        configuration.setReferencePoint("start");
        configuration.setAction("substract");

        Configuration configuration2 = new Configuration();
        configuration2.setName(Appraisal.STATUS_GOALS_DUE);
        configuration2.setValue("30");
        configuration2.setReferencePoint("start");
        configuration2.setAction("substract");

        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();

        return new Object[][] {
                {job, configuration, configuration2}
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

        int appraisalID =  AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL)
                .getId();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();

        employee = employeeMgr.findByOnid("luf", null);
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
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        AppraisalMgr.updateAppraisal(modifiedAppraisal, new Employee());
        tx.commit();
        // no exception means success
    }

    public Appraisal loadAppraisalAssessments() throws Exception {
        Session hsession = HibernateUtil.getCurrentSession();
        Transaction tx = hsession.beginTransaction();
        Job job = (Job) hsession.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        tx.commit();

        int appraisalID =  AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL)
                .getId();
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);
        tx.commit();
        employee = employeeMgr.findByOnid("luf", null);

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalsComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);

        for (Assessment assessment : updatedAppraisal.getCurrentGoalVersion().getAssessments()) {
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
        int appraisalID =  AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL)
                .getId();

        // Grab the freshly created appraisal from the db before we start
        // updating the properties.
        employee = employeeMgr.findByOnid("luf", null);
        hsession = HibernateUtil.getCurrentSession();
        tx = hsession.beginTransaction();
        Appraisal updatedAppraisal = (Appraisal) hsession.load(Appraisal.class, appraisalID);

        updatedAppraisal.setEvaluator(employee);
        updatedAppraisal.setGoalApprovedDate(new Date());
        updatedAppraisal.setGoalsComments("goal comments data");
        updatedAppraisal.setResultSubmitDate(new Date());
        updatedAppraisal.setEvaluation("evaluation text");
        updatedAppraisal.setRating(1);

        for (Assessment assessment : updatedAppraisal.getCurrentGoalVersion().getAssessments()) {
            assessment.setEmployeeResult("employee results txt");
            assessment.setSupervisorResult("supervisor results txt");
        }

        AppraisalMgr.updateAppraisal(updatedAppraisal, new Employee());
        tx.commit();


        for (Assessment assessment : updatedAppraisal.getCurrentGoalVersion().getAssessments()) {
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
        for (Assessment assessment : modifiedAppraisal.getCurrentGoalVersion().getAssessments()) {
            assessment.setGoal("first edit of goal");
        }
        //@ todo
        //appraisalMgr.setLoggedInUser(modifiedAppraisal.getJob().getEmployee());
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        AppraisalMgr.updateAppraisal(modifiedAppraisal, new Employee());
        tx.commit();
        for (Assessment assessment : modifiedAppraisal.getCurrentGoalVersion().getAssessments()) {
            assert assessment.getGoal() != null :
                    "Appraisal assessments goals failed to save";
            assert assessment.getGoalLogs().size() == 1 :
                    "Appraisal assessment goals should have a new log";
        }

        // Editing the goals for the second time
        for (Assessment assessment : modifiedAppraisal.getCurrentGoalVersion().getAssessments()) {
            assessment.setGoal("second edit of goal");
        }
        // @ todo
        //appraisalMgr.setLoggedInUser(new EmployeeMgr().findByOnid("luf", null));
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        AppraisalMgr.updateAppraisal(modifiedAppraisal, new Employee());
        tx.commit();
        for (Assessment assessment : modifiedAppraisal.getCurrentGoalVersion().getAssessments()) {
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
        assert myActiveAppraisals.size() == 6 : "Invalid size of active appraisals";
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
        List<Appraisal> teamActiveAppraisals = AppraisalMgr.getMyTeamsAppraisals(pidm, true,
                null, null);
        assert teamActiveAppraisals.size() == 6 : "Invalid size of team active appraisals";
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
        //@ todo
        //assert appraisalMgr.getRole(appraisal, invalidPidm).equals("");
        tx.commit();
    }

    public void shouldDetectEmployeeRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);
        tx.commit();
        // @ todo
        //assert appraisalMgr.getRole(appraisal, 12345).equals("employee");
    }

    public void shouldDetectReviewerRoleInAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);

        //@ todo
        //assert appraisalMgr.getRole(appraisal, 787812).equals("reviewer");
        tx.commit();
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
        Transaction tx = session.beginTransaction();
        Configuration goalsDueConfig = (Configuration) session.load(Configuration.class, 1);
        List<Job> results = (List<Job>) session.createQuery("from edu.osu.cws.evals.models.Job where status = 'A'").list();
        tx.commit();
        for (Job job : results) {
            AppraisalMgr.createAppraisal(job, new Date(), Appraisal.TYPE_ANNUAL, goalsDueConfig);
        }
    }*/

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldSetTheStartDateWhenCreatingAppraisal(Job job, Configuration configuration) throws Exception {
        DateTime today = new DateTime();
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(today);
    }

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration",
            expectedExceptions = {ModelException.class})
    public void shouldOnlyCreateTwoTypesOfAppraisals(Job job, Configuration configuration) throws Exception {
        DateTime today = new DateTime();
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal != null;

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal != null;

        appraisal = AppraisalMgr.createAppraisal(job, today, "invalid type");
        assert appraisal == null;

    }

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldUseStartDatePlusNumberOfMonthsInTrialIndToSetTheEndDateForTrialAppraisal(Job job,
            Configuration configuration) throws Exception {
        DateTime today = new DateTime();
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_TRIAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal.getStartDate().equals(today) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 6 months.";

        job.setTrialInd(9);
        endDate = job.getEndEvalDate(today, Appraisal.TYPE_TRIAL);
        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        assert appraisal.getStartDate().equals(today) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 9 months.";
    }

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueAndResultsDueConfiguration")
    public void shouldUseStartDatePlusNumberOfMonthsInAnnualIndToSetTheEndDateForFirstAnnualAppraisal(Job job,
            Configuration goalsDueConfig, Configuration resultsDueConfig) throws Exception {
        DateTime today = new DateTime();
        job.setAnnualInd(12);
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_INITIAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        appraisal = AppraisalMgr.createInitialAppraisalAfterTrial(appraisal, resultsDueConfig);
        assert appraisal.getStartDate().equals(today) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 12 months.";

        job.setAnnualInd(18);
        endDate = job.getEndEvalDate(today, Appraisal.TYPE_INITIAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_TRIAL);
        appraisal = AppraisalMgr.createInitialAppraisalAfterTrial(appraisal, resultsDueConfig);
        assert appraisal.getStartDate().equals(today) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) : "End date should have been today + 18 months.";
    }

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldUseTwelveMonthsForAllAnnualAppraisalsAfterTheFirstOne(Job job, Configuration configuration)
            throws Exception {
        DateTime today = new DateTime();
        job.setAnnualInd(100);
        DateTime endDate = job.getEndEvalDate(today, Appraisal.TYPE_ANNUAL);

        appraisal = AppraisalMgr.createAppraisal(job, today, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(today) : "Start date should be set correctly.";
        assert appraisal.getEndDate().equals(endDate.toDate()) :
                "End date should have been today + 12 months for annual appraisals.";
    }

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldSetStatusToAppraisalDueIfStartDateIsBeforeNov1st2011(Job job)
            throws Exception{
        String startPointString = "10/29/2011";
        DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_FORMAT_FULL);
        DateTime startPointDate = fmt.parseDateTime(startPointString);

        appraisal = AppraisalMgr.createAppraisal(job, startPointDate, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(startPointDate) : "Start date should be set correctly.";
        assert appraisal.getStatus().equals(Appraisal.STATUS_APPRAISAL_DUE) :
                "appraisal status should have been appraisalDue, instead got - " + appraisal.getStatus();
    }

/*    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldSetStatusToGoalsOverdueIfGoalsAreDueInPast(Job job, Configuration configuration)
            throws Exception {
        //@todo: not sure how to test this
        String startPointString = "11/01/2011";
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        Date startPointDate = fmt.parse(startPointString);

        appraisal = AppraisalMgr.createAppraisal(job, startPointDate, Appraisal.TYPE_ANNUAL, configuration);
        assert appraisal.getStartDate().equals(startPointDate) : "Start date should be set correctly.";
        assert appraisal.getStatus().equals("appraisalDue") :
                "appraisal status should have been appraisalDue, instead got - " + appraisal.getStatus();
    }*/

    @Test(groups = {"unittest"}, dataProvider = "jobAndGoalsDueConfiguration")
    public void shouldSetStatusToGoalsDueIfAppraisalIsAfterNov1st2011AndGoalsAreNotDueInPast(Job job,
            Configuration configuration) throws Exception {
        String startPointString = "11/01/2012";
        DateTimeFormatter fmt = DateTimeFormat.forPattern(Constants.DATE_FORMAT_FULL);
        DateTime startPointDate = fmt.parseDateTime(startPointString);

        appraisal = AppraisalMgr.createAppraisal(job, startPointDate, Appraisal.TYPE_ANNUAL);
        assert appraisal.getStartDate().equals(startPointDate) : "Start date should be set correctly.";
        assert appraisal.getStatus().equals(Appraisal.STATUS_GOALS_DUE) :
                "appraisal status should have been goalsDue, instead got - " + appraisal.getStatus();
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

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.DAY_OF_MONTH, 14);
        Date startDate = cal.getTime();

        assert AppraisalMgr.appraisalExists(job, new DateTime(startDate), "annual");

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
        Transaction tx = session.beginTransaction();
        Appraisal appraisal = (Appraisal) session.load(Appraisal.class, 1);
        tx.commit();
        appraisal.setStatus(Appraisal.STATUS_CLOSED);
        appraisal.setOriginalStatus(Appraisal.STATUS_GOALS_DUE);
        AppraisalMgr.updateAppraisalStatus(appraisal);
    }
}
