package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.List;

@Test
public class JobsTest {
    Job job = new Job();
    JobMgr jobMgr = new JobMgr();
    Transaction tx;

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

    public void shouldFindUppserSupervisor() throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        job = (Job) session.load(Job.class, new Job(new Employee(787812), "1234", "00"));
        tx.commit();
        int pidm = 990871;

        assert jobMgr.isUpperSupervisor(job, pidm) : "failed to find detect upper supervisor";
    }

    public void shouldNotFindUpperSupervisorForTopSupervisor() throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        job = (Job) session.load(Job.class, new Job(new Employee(990871), "1234", "00"));
        tx.commit();
        int pidm = 990871;

        assert !jobMgr.isUpperSupervisor(job, pidm) : "should not have found an upper supervisor";
    }

    public void shouldCorrectlyDetectEmployeeSupervisor() throws Exception {
        assert JobMgr.isSupervisor(990871, null) : "isSupervisor() should count employees correctly";
        assert !JobMgr.isSupervisor(12345, null) : "isSupervisor() should not count inactive employees";
    }


    /**
     * Tests that the jobs view is not empty.
     * Before you run this test method make sure that the beforeMethod in this class is commented out.
     */
    public void shouldHaveJobsInView() throws Exception {
        List<Job> results = jobMgr.list();
        int i = 0;

        // place a breakpoint below if you want to step through the records to make sure
        // we are getting data from the view
        for (Job job : results) {
            assert job != null;
            i++;
            if (i >= 5) {
                break;
            }
        }
        assert results.size() > 0 : "The list of employees should not be empty";
    }


    @Test(groups={"pending"})
    public void shouldOnlyListJobsNotTerminatedByAppointmentType() throws Exception {

    }

    public void listShortNotTerminatedJobsShouldOnlyIncludePidmAndPosNoAndSuffix() throws Exception {
        List<Job> jobs = JobMgr.listShortNotTerminatedJobs(AppointmentType.CLASSIFIED);
        assert jobs.size() != 0 : "Missing jobs from list";
        for (Job job : jobs) {
            assert job.getEmployee().getId() != 0 : "Missing required property";
            assert job.getPositionNumber() != null : "Missing required property";
            assert job.getSuffix() != null : "Missing required property";
            assert !job.getStatus().equals("T");
            assert job.getAppointmentType().equals(AppointmentType.CLASSIFIED);
        }
    }

    public void getJobShouldReturnNullWhenNotFound() throws Exception {
        Job job = JobMgr.getJob(56198, "1234", "22");
        assert job == null : "Job is not in db, and should be null";
    }

    public void getJobShouldReturnJob() throws Exception {
        Job job = JobMgr.getJob(56198, "1234", "01");
        assert job != null : "Job is in db, and should not be null";
        assert job.getEmployee().getId() != 0;
        assert job.getPositionNumber() != null;
        assert job.getSuffix() != null;
    }

    public void getJobsShouldReturnJobs() throws Exception {
        assert JobMgr.getJobs(56198).size() == 2;
        assert JobMgr.getJobs(111).size() == 0;
        assert JobMgr.getJobs(12345).size() == 3;
    }

    public void shouldReturnBusinessCenter() throws Exception {
        assert jobMgr.getBusinessCenter(56198).equals("UABC");
    }

    public void shouldReturnCorrectNewAnnualStartDateIfFirstLasted18Months() throws Exception {
        Calendar cal = Calendar.getInstance();
        Job job = new Job();
        job.setAnnualInd(18);
        job.setEmployee(new Employee(12345));
        job.setPositionNumber("C1234");
        job.setSuffix("00");

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, Calendar.JUNE,
                Calendar.getInstance().get(Calendar.YEAR)-1);

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-4);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, Calendar.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-3);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, Calendar.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-2);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, Calendar.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
    }

    private void assertCorrectNewAnnualStartDateForAnnualInd18(Job job, Calendar cal, int month,
                                                               int year) throws Exception {
        job.setBeginDate(cal.getTime());
        Calendar newStartDate = job.getNewAnnualStartDate();

        assert newStartDate.get(Calendar.YEAR) == year;
        assert newStartDate.get(Calendar.MONTH) == month;
        assert newStartDate.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public void shouldReturnCorrectNewAnnualStartDateForFirstAnnualIfAnnualIndIs18Case2()
            throws Exception {
        Calendar cal = Calendar.getInstance();
        Job job = new Job();
        job.setAnnualInd(18);
        job.setEmployee(new Employee(12345));
        job.setPositionNumber("C1234");
        job.setSuffix("00");

        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 4);
        cal.set(Calendar.YEAR, 2010);

        job.setBeginDate(cal.getTime());
        Calendar newStartDate = job.getNewAnnualStartDate();

        assert newStartDate.get(Calendar.YEAR) == 2011;
        assert newStartDate.get(Calendar.MONTH) == Calendar.JANUARY;
        assert newStartDate.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public void shouldParseJobFromString() {
        assert null == Job.getJobFromString("");
        assert null == Job.getJobFromString("1234");
        assert null == Job.getJobFromString("1234_dfd");
        assert null == Job.getJobFromString("1234_dfd_");

        Job job = Job.getJobFromString("1234_C12345_00");
        assert job.getId() == 1234;
        assert job.getPositionNumber().equals("C12345");
        assert job.getSuffix().equals("00");
    }

    public void shouldReturnNullWhenEmployeeHasNoSupervisorJob() {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        assert null == JobMgr.getSupervisingJob(0);
        assert null == JobMgr.getSupervisingJob(-13435);

        assert null == JobMgr.getSupervisingJob(787812);

        Job supervisingJob = JobMgr.getSupervisingJob(12345);
        assert null != supervisingJob;
        assert supervisingJob.getEmployee().getId() == 12345;
        assert supervisingJob.getPositionNumber().equals("1234");
        assert supervisingJob.getSuffix().equals("00");

        tx.commit();
    }

    public void searchShouldAcceptOsuid() throws Exception {
        List<Job> jobs = JobMgr.search("931421235", null, 0);
        assert jobs.size() == 3;

        jobs = JobMgr.search("931421234", null, 0);
        assert jobs.size() == 1;

        jobs = JobMgr.search("111111111", null, 0);
        assert jobs.size() == 0;
    }

    public void searchByOsuidShouldCheckBCPermissions() throws Exception {
        List<Job> jobs = JobMgr.findByOsuid("12345677", "AABC", 0);
        assert jobs.size() == 2;

        assert jobs.get(0).getEmployee().getId() == 56199;
        assert jobs.get(0).getPositionNumber().equals("1234");

        assert jobs.get(1).getEmployee().getId() == 56199;
        assert jobs.get(1).getPositionNumber().equals("12341");
    }

    //@todo: the test below requires oracle :(
    public void searchByOsuidShouldCheckSupervisorPermissions() throws Exception {}



    public void searchByNameShouldAcceptFirstNameOnly() throws Exception {
        List<Job> jobs = JobMgr.findByName("Joan", null, 0);
        assert jobs.size() == 1;

        jobs = JobMgr.findByName("Joannnnnn", null, 0);
        assert jobs.size() == 0;

        jobs = JobMgr.findByName("Jo", null, 0);
        assert jobs.size() == 2;
    }

    public void searchByNameShouldAcceptLastNameOnly() throws Exception {
        List<Job> jobs = JobMgr.findByName("Cedeno", null, 0);
        assert jobs.size() == 1;

        jobs = JobMgr.findByName("Bond", null, 0);
        assert jobs.size() == 0;

        jobs = JobMgr.findByName("Barlow", null, 0);
        assert jobs.size() == 3;
    }

    public void searchByNameShouldAcceptFirstAndLastName() throws Exception {
        List<Job> jobs = JobMgr.findByName("Joan Lu", null, 0);
        assert jobs.size() == 1;

        jobs = JobMgr.findByName("Lu Joan", null, 0);
        assert jobs.size() == 1;
    }

    public void searchByNameShouldAcceptFirstAndLastNameWithCommaInBetween() throws Exception {
        List<Job> jobs = JobMgr.findByName("Lu, Joan", null, 0);
        assert jobs.size() == 1;
    }

    public void shouldFindOrgCodeAsAdmin() throws Exception {
        assert JobMgr.findOrgCode("abcd", null) == false;
        assert JobMgr.findOrgCode("123456", null) == true;
    }

    public void shouldFindOrgCodeAsBC() throws Exception {
        assert JobMgr.findOrgCode("654321", "UABC") == true;
        assert JobMgr.findOrgCode("654321", "AABC") == false;
    }
}
