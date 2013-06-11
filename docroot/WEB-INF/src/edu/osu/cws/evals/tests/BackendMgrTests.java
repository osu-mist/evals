package edu.osu.cws.evals.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.osu.cws.evals.backend.BackendMgr;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

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
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
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
        Job job = (Job) session.load(Job.class, new Job(new Employee(12467), "1234", "00"));

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
        DateTime beginDate = new DateTime(job.getBeginDate()).withYear(new DateTime().getYear());
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
        AppraisalMgr.createAppraisal(job, new DateTime(),  Appraisal.TYPE_ANNUAL);

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
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
        assert !mgr.sendMail(appraisal);
        tx.commit();
    }

    public void shouldSendMailToEmployee() throws Exception {
        BackendMgr mgr = getMgrInstance();
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Job job = (Job) session.load(Job.class, new Job(new Employee(12345), "1234", "00"));

        // create appraisal for testing
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
        MockMailer mockMailer = (MockMailer) mgr.getMailerInterface();
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
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
        MockMailer mockMailer = (MockMailer) mgr.getMailerInterface();
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
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
        appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        MockMailer mockMailer = (MockMailer) mgr.getMailerInterface();
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
        Appraisal appraisal = AppraisalMgr.createAppraisal(job, new DateTime(), Appraisal.TYPE_ANNUAL);
        appraisal.setStatus(Appraisal.STATUS_APPRAISAL_DUE);
        appraisal.getJob().setSupervisor(null);

        // supervisor should log an error
        Integer errorCalls = mgr.getDataErrorCount();
        assert !mgr.sendMail(appraisal);
        assert mgr.getDataErrorCount() == errorCalls + 1 : "New error should be logged";
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

//    Things to test:
//    getSupervisorEmailList()
//    emailSupervisors()
//    emailReviewers()
//
//    Appraisal.updateOverdue()
//    AppraisalMgr.saveOverdue()
//
//    EmailMgr.getLastEmail()
//    EvalsUtil.anotherEmail


}
