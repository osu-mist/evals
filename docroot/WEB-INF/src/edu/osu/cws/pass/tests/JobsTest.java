package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.JobMgr;
import edu.osu.cws.pass.models.AppointmentType;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

@Test
public class JobsTest {
    Job job = new Job();
    JobMgr jobMgr = new JobMgr();

    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldFindSupervisorIfNoDirectSupervisor() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        job = (Job) session.load(Job.class, new Job(new Employee(12345), "333", "00"));
        tx.commit();

        Job supervisor = jobMgr.getSupervisor(job);
        assert supervisor != null;
        assert supervisor.getEmployee().getId() == 12345 : "Incorrect supervisor pidm found";
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
        assert jobMgr.isSupervisor(990871) : "isSupervisor() should count employees correctly";
        assert !jobMgr.isSupervisor(12345) : "isSupervisor() should not count inactive employees";
    }


    /**
     * Tests that the jobs view is not empty.
     * Before you run this test method make sure that the beforeMethod in this class is commented out.
     */
    public void shouldHaveJobsInView() {
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
}
