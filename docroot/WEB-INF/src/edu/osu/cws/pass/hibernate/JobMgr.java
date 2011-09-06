package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class JobMgr {

    /**
     * Given a job, it finds the matching supervisor even if the direct supervising
     * job associated to it is not active. Return null is the job has no supervisor
     * associated to it.
     *
     * @param job
     * @return
     * @throws Exception
     */
    public Job getSupervisor(Job job) throws  Exception {
        Session session = HibernateUtil.getCurrentSession();
        Job supervisorJob = null;
        try {
            supervisorJob = this.getSupervisor(job, session);
        } catch (Exception e){
            session.close();
            throw e;
        }
        return supervisorJob;
    }

    /**
     * Given a job, it finds the matching supervisor even if the direct supervising
     * job associated to it is not active. Return null is the job has no supervisor
     * associated to it.
     *
     * @param job   The job we are looking for a supervisor
     * @param session
     * @return supervisor job
     */
    private Job getSupervisor(Job job, Session session) {
        Job supervisorJob = job.getSupervisor();

        if (supervisorJob == null) {
            return null;
        }

        // Iterate up the supervising chain. If the current supervisor doesn't have an
        // active employee or supervisorJob associated, look at the supervisor higher up
        while (supervisorJob != null && (!supervisorJob.getStatus().equals("A") ||
                !supervisorJob.getEmployee().getStatus().equals("A"))) {
            supervisorJob = supervisorJob.getSupervisor();
        }

        return supervisorJob;
    }

    /**
     * Traverses up the supervising chain of the given job and if the given pidm matches
     * a supervisor it returns true.
     *
     * @param job   Job to traverse the supervising chain
     * @param pidm  Employee to check whether or not is upper supervisor
     * @return boolean
     * @throws edu.osu.cws.pass.models.ModelException
     */
    public boolean isUpperSupervisor(Job job, int pidm) throws ModelException {
        Job supervisorJob = job.getSupervisor();

        // If the current job has no supervisor return false right away
        if (supervisorJob == null) {
            return false;
        }

        // Iterate over the supervising chain. If the supervisor has no employee associated
        // or if the supervisor pidm doesn't match what we're looking for go up the supervising
        // chain.
        while (supervisorJob != null &&
                (!supervisorJob.getStatus().equals("A")
                        || !supervisorJob.getEmployee().getStatus().equals("A")
                        || supervisorJob.getEmployee().getId() != pidm)) {
            supervisorJob = supervisorJob.getSupervisor();
        }

        if (supervisorJob == null || !supervisorJob.getStatus().equals("A")
                || !supervisorJob.getEmployee().getStatus().equals("A")) {
            return false;
        } else if (supervisorJob.getEmployee().getId() == pidm) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether a person has any job which is a supervising job.
     *
     * @param pidm  pidm of employee to check
     * @return isSupervisor
     * @throws Exception
     */
    public boolean isSupervisor(int pidm) throws Exception {
        String query = "select count(*) from edu.osu.cws.pass.models.Job where endDate IS NULL " +
                "AND supervisor.employee.id = :pidm AND employee.status = 'A'";

        Session session = HibernateUtil.getCurrentSession();
        int employeeCount = 0;
        try {
            Transaction tx = session.beginTransaction();
            employeeCount = ((Long) session.createQuery(query).setInteger("pidm", pidm)
                    .iterate().next()).intValue();
            tx.commit();
        } catch (Exception e){
            session.close();
            throw e;
        }
        return employeeCount > 0;
    }

    /**
     * Retrieves a list of Jobs from the database.
     * @return
     */
    public List<Job> list() {
        Session session = HibernateUtil.getCurrentSession();
        List results = new ArrayList();
        try {
            results = this.list(session);

        } catch (Exception e) {
            session.close();
        }

        return results;
    }

    /**
     * Retrieves a list of Jobs from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Job> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Job> result = session.createQuery("from edu.osu.cws.pass.models.Job").list();
        tx.commit();
        return result;
    }

    /**
     * @todo: queries for all the not terminated jobs of a certain appointment type and returns them.
     *
     * @param appointmentType: the type of appointment (classified, classifiedIT, ...)
     * @return a list of not terminated jobs of businessType.
     */
    public static List<Job> listNotTerminatedJobs(String appointmentType) throws Exception {
        List<Job> jobs = new ArrayList<Job>();
        Session session = HibernateUtil.getCurrentSession();

        try {
            Transaction tx = session.beginTransaction();
            jobs = session.createQuery("from edu.osu.cws.pass.models.Job job " +
                "where job.status != 'T' and job.appointmentType = :appointmentType")
            .setString("appointmentType", appointmentType)
            .list();
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return jobs;

    }

    /**
     * @@todo: queries for all the not terminated jobs of a certain appointment type.
     * @@todo: Jobs returns only contain the primary key columns
     * @param appointmentType
     * @return
     * @throws Exception
     */
    public static List<Job> listShortNotTerminatedJobs(String appointmentType)throws Exception
    {
        List<Job> jobs = new ArrayList<Job>();
        return jobs;
    }

    /**
     * @todo: returns the job corresponding to the primary keys, or null if not found.
     * @param pidm
     * @param posn
     * @param suffix
     * @return
     * @throws Exception
     */
    public static Job getJob(int pidm, String posn, String suffix) throws Exception
    {
        return new Job();

    }
}