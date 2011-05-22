package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.Job;
import edu.osu.cws.pass.models.ModelException;
import org.hibernate.Session;

public class Jobs {

    public Job getSupervisor(Job job) throws ModelException {
        Session session = HibernateUtil.getCurrentSession();
        return this.getSupervisor(job, session);
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
        while (currentNode.getEmployeePidm() == null) {
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
        while (currentNode.getEmployeePidm() == null ||
                currentNode.getEmployeePidm().getId() != pidm) {
            currentNode = currentNode.getSupervisor();
        }

        if (currentNode == null || currentNode.getEmployeePidm() == null) {
            return false;
        } else if (currentNode.getEmployeePidm().getId() == pidm) {
            return true;
        }

        return false;
    }

}
