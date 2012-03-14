package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.StandardBasicTypes;

import java.util.ArrayList;
import java.util.List;

public class JobMgr {

    /**
     * Traverses up the supervising chain of the given job and if the given pidm matches
     * a supervisor it returns true.
     *
     * @param job   Job to traverse the supervising chain
     * @param pidm  Employee to check whether or not is upper supervisor
     * @return boolean
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public boolean isUpperSupervisor(Job job, int pidm) throws ModelException {
        Job supervisorJob = job.getSupervisor();

        // Iterate over the supervising chain. If the supervisor has no employee associated
        // or if the supervisor pidm doesn't match what we're looking for go up the supervising
        // chain.
        while (supervisorJob != null &&
                (!supervisorJob.getStatus().equals("A")
                        || !supervisorJob.getEmployee().getStatus().equals("A")
                        || supervisorJob.getEmployee().getId() != pidm)) {
            supervisorJob = supervisorJob.getSupervisor();
        }

        return supervisorJob != null;
    }

    /**
     * Determines whether a person has any job which is a supervising job.
     *
     * @param pidm  pidm of employee to check
     * @return isSupervisor
     * @throws Exception
     */
    public boolean isSupervisor(int pidm) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int employeeCount;
        employeeCount = 0;
        List<Object> results = session.getNamedQuery("job.isSupervisor")
                .setInteger("pidm", pidm)
                .list();
        if (!results.isEmpty()) {
            employeeCount = Integer.parseInt(results.get(0).toString());
        }
        return employeeCount > 0;
    }

    /**
     * Retrieves a list of Jobs from the database.
     * @return
     */
    //@todo: Where do you use this method.  This is a very expensive operation.
    public List<Job> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return this.list(session);
    }

    /**
     * Retrieves a list of Jobs from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Job> list(Session session) throws Exception {
        List<Job> result = session.createQuery("from edu.osu.cws.evals.models.Job").list();
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

        jobs = session.createQuery("from edu.osu.cws.evals.models.Job job " +
                "where job.status != 'T' and job.appointmentType = :appointmentType")
                .setString("appointmentType", appointmentType)
                .list();
        return jobs;
    }

    /**
     *
     * @param appointmentType
     * @return
     * @throws Exception
     */
    public static List<Job> listShortNotTerminatedJobs(String appointmentType) throws Exception {
        List<Job> jobs;
        Session session = HibernateUtil.getCurrentSession();

        String query = "select new edu.osu.cws.evals.models.Job(employee.id, positionNumber, suffix, " +
                "status, appointmentType) from edu.osu.cws.evals.models.Job job " +
                "where job.status != 'T' and job.appointmentType = :appointmentType";
        jobs = session.createQuery(query)
                .setString("appointmentType", appointmentType)
                .list();
        return jobs;
    }

    /**
     * Returns the first job corresponding to the primary keys, or null if not found.
     *
     * @param pidm
     * @param posn
     * @param suffix
     * @return
     * @throws Exception
     */
    public static Job getJob(int pidm, String posn, String suffix) throws Exception {
        Job job = null;
        Session session = HibernateUtil.getCurrentSession();

        String query = "from edu.osu.cws.evals.models.Job job " +
                "where job.employee.id = :pidm and job.positionNumber = :positionNumber " +
                "and job.suffix = :suffix";

        List<Job> jobs = session.createQuery(query)
                .setInteger("pidm", pidm)
                .setString("positionNumber", posn)
                .setString("suffix", suffix)
                .list();

        if (!jobs.isEmpty()) {
            job = jobs.get(0);
        }
        return job;
    }

    /**
     * Returns the jobs corresponding to the employee pidm.
     *
     * @param pidm
     * @return
     * @throws Exception
     */
    public static List<Job> getJobs(int pidm) throws Exception {
        List<Job> jobs;
        Session session = HibernateUtil.getCurrentSession();

        String query = "from edu.osu.cws.evals.models.Job job " +
                "where job.employee.id = :pidm";

        jobs = session.createQuery(query)
                .setInteger("pidm", pidm)
                .list();
        return jobs;
    }

    /**
     * Returns the business center name of the first active job that this pidm holds.
     *
     * @param pidm
     * @return
     * @throws Exception
     */
    public static String getBusinessCenter(int pidm) throws Exception {
        String businessCenter = null;
        Session session = HibernateUtil.getCurrentSession();

        String query = "select businessCenterName from edu.osu.cws.evals.models.Job job " +
                "where job.employee.id = :pidm and job.status = 'A'";

        Query hibernateQuery = session.createQuery(query)
                .setInteger("pidm", pidm)
                .setMaxResults(1);

        if (hibernateQuery.iterate().hasNext()) {
            businessCenter = (String) hibernateQuery.iterate().next();
        }

        return businessCenter;
    }

    /**
     * Returns a list of the direct supervisors under an mid-level or upper supervisor.
     *
     * @param supervisorJob
     * @return
     */
    public static List<Job> getDirectSupervisorJobs(Job supervisorJob) {
        Session session = HibernateUtil.getCurrentSession();
        List<Job> results = (List<Job>) session.getNamedQuery("job.directSupervisors")
                .setInteger("id", supervisorJob.getEmployee().getId())
                .setString("posno", supervisorJob.getPositionNumber())
                .setString("suffix", supervisorJob.getSuffix())
                .list();

        return results;
    }
}