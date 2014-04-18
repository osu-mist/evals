package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Test
public class JobsTest {
    Job job = new Job();
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
        job = (Job) session.load(Job.class, new Job(new Employee(787812), "1234", "00"));
        int pidm = 990871;

        assert JobMgr.isUpperSupervisor(job, pidm) : "failed to find detect upper supervisor";
    }

    public void shouldNotFindUpperSupervisorForTopSupervisor() throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        job = (Job) session.load(Job.class, new Job(new Employee(990871), "1234", "00"));
        int pidm = 990871;

        assert !JobMgr.isUpperSupervisor(job, pidm) : "should not have found an upper supervisor";
    }

    public void shouldCorrectlyDetectEmployeeSupervisor() throws Exception {
        assert JobMgr.isSupervisor(990871, null) : "isSupervisor() should count employees correctly";
        assert !JobMgr.isSupervisor(56199, null) : "isSupervisor() should not count inactive employees";
    }


    @Test(groups={"pending"})
    public void shouldOnlyListJobsNotTerminatedByAppointmentType() throws Exception {

    }

    public void listShortNotTerminatedJobsShouldOnlyIncludePidmAndPosNoAndSuffix() throws Exception {
        ArrayList<String> appointmentTypes = new ArrayList<String>();
        Collections.addAll(appointmentTypes, new String[] {AppointmentType.CLASSIFIED});
        List<Job> jobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        assert jobs.size() != 0 : "Missing jobs from list";
        for (Job job : jobs) {
            assert job.getEmployee().getId() != 0 : "Missing required property";
            assert job.getPositionNumber() != null : "Missing required property";
            assert job.getSuffix() != null : "Missing required property";
            assert !job.getStatus().equals("T");
            assert job.getAppointmentType().equals(AppointmentType.CLASSIFIED);
        }
    }

    public void listShortNotTerminatedJobsShouldFilterByAppointmentType() throws Exception {
        ArrayList<String> appointmentTypes = new ArrayList<String>();
        Collections.addAll(appointmentTypes, new String[] {AppointmentType.CLASSIFIED_IT});

        // Try getting only Classified IT
        List<Job> jobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        assert jobs.size() != 0 : "Missing jobs from list";
        for (Job job : jobs) {
            assert job.getAppointmentType().equals(AppointmentType.CLASSIFIED_IT);
        }

        // Try getting only Classified
        appointmentTypes = new ArrayList<String>();
        Collections.addAll(appointmentTypes, new String[] {AppointmentType.CLASSIFIED});
        jobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        assert jobs.size() != 0 : "Missing jobs from list";
        for (Job job : jobs) {
            assert job.getAppointmentType().equals(AppointmentType.CLASSIFIED);
        }

        // Try getting both appointment types
        appointmentTypes = new ArrayList<String>();
        Collections.addAll(appointmentTypes, new String[]
                {AppointmentType.CLASSIFIED, AppointmentType.CLASSIFIED_IT});
        jobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        assert jobs.size() != 0 : "Missing jobs from list";
        int classifiedJobCount = 0;
        int classifiedITJobCount = 0;
        for (Job job : jobs) {
            if (job.getAppointmentType().equals(AppointmentType.CLASSIFIED)) {
                classifiedJobCount++;
            }
            if (job.getAppointmentType().equals(AppointmentType.CLASSIFIED_IT)) {
                classifiedITJobCount++;
            }
        }

        assert classifiedITJobCount > 0 : "Should have fetched some jobs";
        assert classifiedJobCount > 0 : "Should have fetched some jobs";
    }

    public void listShortNotTerminatedJobsShouldOnlyInclude00SuffixAndNonFutureBeginDate() throws Exception {
        ArrayList<String> appointmentTypes = new ArrayList<String>();
        Collections.addAll(appointmentTypes, new String[] {AppointmentType.CLASSIFIED});

        List<Job> jobs = JobMgr.listShortNotTerminatedJobs(appointmentTypes);
        assert jobs.size() != 0 : "Missing jobs from list";
        for (Job job : jobs) {
            job = JobMgr.getJob(job.getEmployee().getId(), job.getPositionNumber(), job.getSuffix());
            assert job.getSuffix().equals("00") : "Only jobs with suffix '00' will be created";
            assert job.getBeginDate().compareTo(new Date()) <= 0 : "Jobs with future begin date will not be created";
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
        assert JobMgr.getJobs(56198).size() == 1;
        assert JobMgr.getJobs(111).size() == 0;
        assert JobMgr.getJobs(12345).size() == 3;
    }

    public void shouldReturnBusinessCenter() throws Exception {
        assert JobMgr.getBusinessCenter(56198).equals("UABC");
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

        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, DateTimeConstants.JUNE,
                Calendar.getInstance().get(Calendar.YEAR)-1);

        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-4);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, DateTimeConstants.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-3);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, DateTimeConstants.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-2);
        assertCorrectNewAnnualStartDateForAnnualInd18(job, cal, DateTimeConstants.DECEMBER,
                Calendar.getInstance().get(Calendar.YEAR));
    }

    public void shouldCorrectlyDetermineIfUserIsAProfessionalSupervisor() {
        assert JobMgr.isProfessionalSupervisor(56200);
        assert !JobMgr.isProfessionalSupervisor(12345);
    }

    private void assertCorrectNewAnnualStartDateForAnnualInd18(Job job, Calendar cal, int month,
                                                               int year) throws Exception {
        job.setBeginDate(cal.getTime());
        DateTime newStartDate = job.getNewAnnualStartDate();

        assert newStartDate.getYear() == year;
        assert newStartDate.getMonthOfYear() == month;
        assert newStartDate.getDayOfMonth() == 1;
    }

    public void shouldReturnCorrectNewAnnualStartDateForFirstAnnualIfAnnualIndIs18Case2()
            throws Exception {
        Job job = new Job();
        job.setAnnualInd(18);
        job.setEmployee(new Employee(12345));
        job.setPositionNumber("C1234");
        job.setSuffix("00");

        DateTime dateTime = EvalsUtil.getToday().minusYears(1).withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(4);
        job.setBeginDate(dateTime.toDate());
        DateTime newStartDate = job.getNewAnnualStartDate();

        assert newStartDate.getYear() == EvalsUtil.getToday().getYear();
        assert newStartDate.getMonthOfYear() == DateTimeConstants.JANUARY;
        assert newStartDate.getDayOfMonth() == 1;
    }

    public void shouldParseJobFromString() {
        assert null == Job.getJobFromString("");
        assert null == Job.getJobFromString("1234");
        assert null == Job.getJobFromString("1234_dfd");
        assert null == Job.getJobFromString("1234_dfd_");

        Job job = Job.getJobFromString("1234_C12345_00");
        assert job.getEmployee().getId() == 1234;
        assert job.getPositionNumber().equals("C12345");
        assert job.getSuffix().equals("00");
    }

    public void shouldReturnNullWhenEmployeeHasNoSupervisorJob() {
        assert null == JobMgr.getSupervisingJob(0);
        assert null == JobMgr.getSupervisingJob(-13435);

        assert null == JobMgr.getSupervisingJob(787812);

        Job supervisingJob = JobMgr.getSupervisingJob(12345);
        assert null != supervisingJob;
        assert supervisingJob.getEmployee().getId() == 12345;
        assert supervisingJob.getPositionNumber().equals("1234");
        assert supervisingJob.getSuffix().equals("00");
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
        assert jobs.size() == 4;
    }

    public void searchByNameShouldAcceptLastNameOnly() throws Exception {
        List<Job> jobs = JobMgr.findByName("Cedeno", null, 0);
        assert jobs.size() == 3;

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

    public void getSalaryShouldReturnSalaryObjectForITAppointmentTypes() throws Exception {
        Job job = JobMgr.getJob(74589, "85392", "00");
        Salary salary = job.getSalary();
        assert salary.getCurrent() == 2500;
        assert salary.getHigh() == 4000;
        assert salary.getLow() == 2000;
        assert salary.getMidPoint() == 3000;
        assert salary.getSgrpCode().equals("123456");
    }

    public void getSalaryShouldReturnNullForNonITAppointmentTypes() throws Exception {
        Job job = new Job();
        job.setAppointmentType(AppointmentType.CLASSIFIED);
        assert job.getSalary() == null : "We only support Classified IT when getting salary info";
    }

    public void shouldFetchPositionDescription() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Employee employee = new Employee(12345);
        employee.setOsuid("931421235");
        job = (Job) session.load(Job.class, new Job(employee, "1234", "00"));

        PositionDescription pd1 = JobMgr.getPositionDescription(job);
        assert pd1.getId() == 1;
        assert pd1.getPositionTitle().equals("PD1");
        assert pd1.getLeadWorkResponsibilities().size() == 2;

        Employee employee1 = new Employee(12467);
        employee1.setOsuid("931421234");
        job = (Job) session.load(Job.class, new Job(employee1, "1234", "00"));

        JobMgr.getPositionDescription(job);
        PositionDescription pd2 = JobMgr.getPositionDescription(job);
        assert pd2.getId() == 2;
        assert pd2.getPositionTitle().equals("PD2");
        assert pd2.getLeadWorkResponsibilities().size() == 1;
    }
}
