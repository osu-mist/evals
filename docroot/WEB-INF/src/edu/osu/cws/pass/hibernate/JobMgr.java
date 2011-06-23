package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class JobMgr {

    /**
     * Given a job, it finds the matching supervisor even if the direct supervising
     * job has no employee associated to it.
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
     * job has no employee associated to it.
     *
     * @param job   The job we are looking for a supervisor
     * @param session
     * @return supervisor job
     */
    private Job getSupervisor(Job job, Session session) {
        Job currentNode = job.getSupervisor();

        if (currentNode == null) {
            return null;
        }

        // Iterate up the supervising chain. If the current supervisor doesn't have an
        // employee associated, look at the supervisor higher up
        while (currentNode.getEmployee() == null) {
            currentNode = currentNode.getSupervisor();
        }

        return currentNode;
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
        Job currentNode = job.getSupervisor();

        // If the current job has no supervisor return false right away
        if (currentNode == null) {
            return false;
        }

        // Iterate over the supervising chain. If the supervisor has no employee associated
        // or if the supervisor pidm doesn't match what we're looking for go up the supervising
        // chain.
        while (currentNode != null &&
                (currentNode.getEmployee() == null || currentNode.getEmployee().getId() != pidm)) {
            currentNode = currentNode.getSupervisor();
        }

        if (currentNode == null || currentNode.getEmployee() == null) {
            return false;
        } else if (currentNode.getEmployee().getId() == pidm) {
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

}
