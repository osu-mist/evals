package edu.osu.cws.evals.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.osu.cws.evals.backend.BackendMgr;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Test
public class BackendMgrTests {
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
        // prevent session info being set by backend module
        HibernateUtil.setHibernateConfig(HibernateUtil.TEST_CONFIG, "", "");
        HibernateUtil.getCurrentSession();
    }

    public void shouldSaveStatusWhenUpdatingAppraisal() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        assert appraisal.getStatus().equals(Appraisal.STATUS_GOALS_DUE);
        tx.commit();

        // get instance and call updateAppraisal
        BackendMgr mgr = getMgrInstance();
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();
        mgr.updateAppraisal(appraisal, Appraisal.STATUS_COMPLETED);

        // check status, original status and updateCount
        assert appraisal.getStatus().equals(Appraisal.STATUS_COMPLETED);
        assert appraisal.getOriginalStatus().equals(Appraisal.STATUS_GOALS_DUE);
        assert mgr.getUpdateCount() == 1 : "updateCount should have been incremented";
        tx.commit();
    }

    public void shouldReturnCorrectSupervisorSb() {
        BackendMgr mgr = getMgrInstance();
        Employee supervisor = new Employee();
        int key = 12345;
        supervisor.setId(key);

        assert !mgr.getSupervisorEmailMessages().containsKey(key);
        assert mgr.getSupervisorSb(supervisor).length() == 0;
    }

    public void shouldNotCreateTrialIfOneExists() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        DateTime startDate = job.getTrialStartDate();

        AppraisalMgr.createAppraisal(job, startDate,  Appraisal.TYPE_TRIAL);
        assert !mgr.handleTrialCreation(job) : "Trial exists so it shouldn't create one.";
        tx.commit();
    }

    public void shouldCreateTrialIfNoneExists() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "4444", "00"));

        Integer currentCount = getEvaluationCount();
        assert mgr.handleTrialCreation(job) : "No trial was present, it should create one";
        assert currentCount == getEvaluationCount() - 1;
        tx.commit();
    }

    public void shouldNotCreateAnnualWhenAnnualIndIs0() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12467), "1234", "00"));

        Integer currentCount = getEvaluationCount();
        assert !mgr.handleAnnualCreation(job) :
                "Annual Indicator was 0, no annual eval should have been created";
        assert currentCount.equals(getEvaluationCount());
        tx.commit();
    }

    public void shouldNotCreateAnnualIfOpenTrialExists() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        // create an initial trial appraisal
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // need to set evalDate and annual ind. so that the exiting open trial eval is created
        DateTime beginDate = new DateTime(job.getBeginDate()).withYear(EvalsUtil.getToday().getYear())
                .withTimeAtStartOfDay();
        job.setEvalDate(beginDate.toDate());
        job.setAnnualInd(12);
        DateTime startDate = job.getTrialStartDate();
        AppraisalMgr.createAppraisal(job, startDate,  Appraisal.TYPE_TRIAL);

        Integer currentCount = getEvaluationCount();
        assert !mgr.handleAnnualCreation(job);
        assert currentCount.equals(getEvaluationCount());
        tx.commit();
    }

    public void shouldNotCreateAnnualIfOneExists() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        // create an appraisal and set annual indicator
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        job.setAnnualInd(12);
        DateTime initialEvalStartDate = job.getInitialEvalStartDate().withYear(new DateTime().getYear());
        AppraisalMgr.createAppraisal(job, initialEvalStartDate,  Appraisal.TYPE_ANNUAL);

        Integer currentCount = getEvaluationCount();
        assert !mgr.handleAnnualCreation(job) : "It shouldn't create one. One already exists";
        assert currentCount.equals(getEvaluationCount());
        tx.commit();
    }


    public void shouldCreateAnnual() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        // create an appraisal and set annual indicator
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        job.setAnnualInd(12);

        Integer currentCount = getEvaluationCount();
        assert mgr.handleAnnualCreation(job) : "It should create one evaluation";
        assert currentCount == getEvaluationCount() - 1;
        tx.commit();

    }

    public void shouldNotSendMailToInactiveJob() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));
        job.setStatus("T"); // set job as terminated

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        assert !mgr.sendMail(appraisal);
        tx.commit();
    }

    public void shouldSendMailToEmployee() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        MockMailer mockMailer = (MockMailer) mgr.getMailer();
        mockMailer.setSendMailReturnValue(true); // let sendMail return true
        assert mgr.sendMail(appraisal);
        assert mockMailer.getEmailType().getMailTo().equals("employee");
        tx.commit();
    }

    public void shouldLogErrorWhenCantSendMailToEmployee() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // setup test objects
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        MockMailer mockMailer = (MockMailer) mgr.getMailer();
        mockMailer.setSendMailReturnValue(false); // let sendMail return false
        Integer errorCalls = mgr.getDataErrorCount();

        // check that no email was sent and error was logged
        assert !mgr.sendMail(appraisal);
        assert mgr.getDataErrorCount() == errorCalls + 1;
        tx.commit();
    }

    public void shouldUpdateStringBufferWhenSendingMailToSupervisor() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        MockMailer mockMailer = (MockMailer) mgr.getMailer();
        mockMailer.setSendMailReturnValue(true); // let sendMail return true

        // check that there are no mail messages for supervisor to begin with
        Employee employee = appraisal.getJob().getSupervisor().getEmployee();
        assert mgr.getSupervisorSb(employee).toString().equals("");

        assert mgr.sendMail(appraisal);
        // check that supervisor got one message in the queue
        assert !mgr.getSupervisorSb(employee).toString().equals("");

        tx.commit();
    }

    public void shouldLogErrorWhenSendMailToNullSupervisor() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, EvalsUtil.getToday(), Appraisal.TYPE_ANNUAL);
        appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        appraisal.getJob().setSupervisor(null);

        // supervisor should log an error
        Integer errorCalls = mgr.getDataErrorCount();
        assert !mgr.sendMail(appraisal);
        assert mgr.getDataErrorCount() == errorCalls + 1 : "New error should be logged";
        tx.commit();
    }

    public void shouldReturnCorrectSupervisorEmailList() {
        BackendMgr mgr = getMgrInstance();
        Employee supervisor = new Employee();
        supervisor.setId(7);

        List<Email> emailList = mgr.getSupervisorEmailList(supervisor);
        assert emailList.isEmpty();
        emailList.add(new Email(1, Appraisal.STATUS_APPRAISAL_DUE));
        assert !mgr.getSupervisorEmailList(supervisor).isEmpty();
    }

    public void shouldEmailSupervisor() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();

        // add supervisor to the list so that we can try to send mock email
        Employee supervisor = EmployeeMgr.findById(12467, null);
        tx.commit();

        List<Email> emailList = mgr.getSupervisorEmailList(supervisor);
        emailList.add(new Email(1, "email type"));
        // add test message to email buffer
        mgr.getSupervisorEmailMessages().put(12467, new StringBuffer());

        MockMailer mailer = (MockMailer) mgr.getMailer();
        assert mailer.getSupervisorIds().isEmpty() : "No emails should have been sent";
        mgr.emailSupervisors();
        assert mailer.getSupervisorIds().size() == 1 : "Only 1 email should have been sent";
        assert mailer.getSupervisorIds().get(0) == 12467 : "Email sent to wrong supervisor";
    }

    public void shouldSendEmailReviewers() throws Exception {
        BackendMgr mgr = getMgrInstance();

        // We only have 1 evaluation in overdue and another one in overdue status
        MockMailer mailer = (MockMailer) mgr.getMailer();
        assert mailer.getReviewDueCount() == 0;
        assert mailer.getReviewOverdueCount() == 0;
        assert mailer.getSendReviewerCallsCount() == 0;
        mgr.emailReviewers();
        assert mailer.getReviewOverdueCount() == 1;
        assert mailer.getReviewDueCount() == 1;
        assert mailer.getSendReviewerCallsCount() == 1;
    }

    public void shouldTimeGoalsReactivationRequest() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setOriginalStatus(Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED);
        GoalVersion goalVersion = new GoalVersion();
        appraisal.addGoalVersion(goalVersion);
        getMgrInstance().timeOutGoalsReactivation(appraisal);

        assert !goalVersion.getRequestDecision();
        assert goalVersion.getRequestDecisionPidm() == null;
        assert goalVersion.getTimedOutAt().equals(Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED);
    }

    public void shouldSendNonProfFacultyEmailsRightAwayDuringUpdate() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType(AppointmentType.CLASSIFIED);

        assert !BackendMgr.timeToSendFirstStatusEmail(appraisal, new DateTime());

        appraisal.getJob().setAppointmentType(AppointmentType.CLASSIFIED_IT);
        assert !BackendMgr.timeToSendFirstStatusEmail(appraisal, new DateTime());
    }

    public void shouldSendProfFacultyEmailsRightAwayOnceReviewPeriodStarts() throws Exception {
        HashSet<Appraisal> appraisalHashSet = new HashSet<Appraisal>();
        Appraisal appraisal = new Appraisal();
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);
        Date yesterday = new DateTime().minusDays(1).toDate();
        appraisal.setStartDate(yesterday);
        Job job = new Job();
        job.setAppointmentType(AppointmentType.PROFESSIONAL_FACULTY);
        job.setAppraisals(appraisalHashSet);
        appraisal.setJob(job);

        // send email right away when it's not the first evaluation
        assert !BackendMgr.timeToSendFirstStatusEmail(appraisal, new DateTime());
    }

    public void shouldUpdateSalaryWhenAppraisalIsDue() throws Exception {
        Appraisal appraisal = new Appraisal();
        appraisal.setJob(new Job());
        appraisal.getJob().setAppointmentType(AppointmentType.CLASSIFIED_IT);
        DateTime endDate = new DateTime().plusDays(61).withTimeAtStartOfDay();
        appraisal.setEndDate(endDate.toDate());
        appraisal.getSalaries().add(new Salary());

        // get instance and call updateAppraisal
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        BackendMgr mgr = getMgrInstance();
        session = HibernateUtil.getCurrentSession();
        tx = session.beginTransaction();

        assert mgr.shouldUpdateSalaryInfo(appraisal) : "Should refresh since appraisal is due in one day";

        endDate = new DateTime().plusDays(60).withTimeAtStartOfDay();
        appraisal.setEndDate(endDate.toDate());
        assert mgr.shouldUpdateSalaryInfo(appraisal) : "Should refresh since appraisal is due today";

        endDate = new DateTime().plusDays(59).withTimeAtStartOfDay();
        appraisal.setEndDate(endDate.toDate());
        assert !mgr.shouldUpdateSalaryInfo(appraisal) : "Should not refresh since appraisal is due in past";

        endDate = new DateTime().plusDays(60).withTimeAtStartOfDay();
        appraisal.setEndDate(endDate.toDate());
        appraisal.getSalaries().clear(); // remove existing salary association
        Salary salary = new Salary();
        salary.setIncrease(7d); // set increase in salary object
        appraisal.getSalaries().add(salary); // add association back
        assert !mgr.shouldUpdateSalaryInfo(appraisal) : "Should not refresh since salary increase is set";


        tx.commit();
    }

    /**
     * Helper method to return count of evaluations in db
     *
     * @return
     */
    private Integer getEvaluationCount() {
        Session session = HibernateUtil.getCurrentSession();
        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal";
        return Integer.parseInt(session.createQuery(query).list().get(0).toString());
    }

    /**
     * Helper method to return instance of backend mgr.
     *
     * @return
     */
    private BackendMgr getMgrInstance() {
        Injector injector = Guice.createInjector(new MockBackendModule());
        return injector.getInstance(BackendMgr.class);
    }
}
