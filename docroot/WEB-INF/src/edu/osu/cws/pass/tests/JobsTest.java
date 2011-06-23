package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.JobMgr;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
        assert supervisor.getEmployee().getId() == 56198 : "Incorrect supervisor pidm found";
    }

    public void shouldFindUppserSupervisor() throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        job = (Job) session.load(Job.class, 4);
        tx.commit();
        int pidm = 990871;

        assert jobMgr.isUpperSupervisor(job, pidm) : "failed to find detect upper supervisor";
    }

    public void shouldNotFindUpperSupervisorForTopSupervisor() throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        job = (Job) session.load(Job.class, 7);
        tx.commit();
        int pidm = 990871;

        assert !jobMgr.isUpperSupervisor(job, pidm) : "should not have found an upper supervisor";
    }

    public void shouldCorrectlyDetectEmployeeSupervisor() throws Exception {
        assert jobMgr.isSupervisor(990871) : "isSupervisor() should count employees correctly";
        assert !jobMgr.isSupervisor(12345) : "isSupervisor() should not count inactive employees";
    }
}
