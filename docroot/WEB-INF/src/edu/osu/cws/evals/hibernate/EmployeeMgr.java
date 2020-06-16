package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeMgr {

    /**
     * Returns the employee with a matching onid username who is active in the employees table.
     * If no active employee is found a new Employee object is returned.
     *
     * @param username      Onid username
     * @param fetchProfile  if not null, then use it as Hibernate fetch profile
     * @return  Employee
     * @throws Exception
     */
    public static Employee findByOnid(String username, String fetchProfile) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Employee employee;
        if (fetchProfile != null) {
            session.enableFetchProfile(fetchProfile);
        }
        employee = (Employee) session.createQuery(
                "from edu.osu.cws.evals.models.Employee as employee where employee.onid = ? and status = 'A'")
                .setString(0, username).uniqueResult();

        // We need to initialize the jobs collection by hand because hql doesn't support fetch-profile
        if (fetchProfile != null && fetchProfile.equals("employee-with-jobs") && employee != null) {
            employee.getJobs().size();
        }

        //@todo: Why do we want to return an empty object instead of a null?
        return (employee == null)? new Employee(): employee;
    }

    /**
     * Returns the employee with a matching id in the employees table.
     *
     * @param id      Id/Pidm of employee
     * @param fetchProfile  if not null, then use it as Hibernate fetch profile
     * @return  Employee
     * @throws Exception
     */
    public static Employee findById(int id, String fetchProfile) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Employee employee;
        if (fetchProfile != null) {
            session.enableFetchProfile(fetchProfile);
        }
        employee = (Employee) session.get(Employee.class, id);

        // We need to initialize the jobs collection by hand because hql doesn't support fetch-profile
        if (fetchProfile != null && fetchProfile.equals("employee-with-jobs") && employee != null) {
            employee.getJobs().size();
        }
        return employee;
    }

    /**
     * Given a Luminis numeric screen name, it queries the Banner database and retrieves the onid username.
     * If the user is not found, it returns an empty string.
     *
     * @param screeName
     * @param bannerHostname  Hostname of the Banner db
     * @param luminisDbLink   Luminis dblink hostname including "@". Only needed in OSDevl.
     * @return
     */
    public static String getOnidUsername(String screeName, String bannerHostname, String luminisDbLink) {
        Session session = HibernateUtil.getCurrentSession();
        String onidUsername = "";

        String query = "SELECT GOBTPAC_EXTERNAL_USER " +
                "FROM gobtpac_spriden_passusr@" + bannerHostname + ", " +
                "luminis.portal_user_id" + luminisDbLink +
                " WHERE SCREENNAME= :screenName AND spriden_id=PERSON_ID";

        List<String> results = (List<String>) session.createSQLQuery(query)
                .setString("screenName", screeName)
                .list();

        if (!results.isEmpty()) {
            onidUsername = results.get(0);
        }
        return onidUsername;
    }

    /**
     * Returns the Jobs with a matching id in the jobs table.
     *
     * @param pidm      Id/Pidm of employee
     * @return  Employee
     * @throws Exception
     * */
    public static Set<Job> findJobs (int pidm) {
        Session session = HibernateUtil.getCurrentSession();
        Set<Job> jobs = new HashSet<Job>();

        Criteria criteria = session.createCriteria(Job.class); //Create the criteria query
        criteria.add(Restrictions.eq("employee.id", pidm)).add(Restrictions.eq("status","A"));
        List list = criteria.list();
        for(Object obj : list){
            Job job = (Job)obj;
            if (job.getSupervisor() != null && job.getSupervisor().getEmployee() != null) {
                job.getSupervisor();
                job.getSupervisor().getEmployee().getName();
            }
            jobs.add(job);
        }
        return jobs;
    }

    public static void createEmployee (int internalId, String osuId, String lastName, String firstName, String onid, String email) {
      Employee emp = new Employee(internalId, firstName, lastName);
      emp.setOsuid(osuId);
      emp.setOnid(onid);
      emp.setEmail(email);
      emp.setStatus("A");
      Session session = HibernateUtil.getCurrentSession();
      // session.save(emp);
    }

}
