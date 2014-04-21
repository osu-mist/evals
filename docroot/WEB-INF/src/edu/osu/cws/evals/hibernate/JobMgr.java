package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.models.PositionDescription;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.*;

public class JobMgr {

    // The search select, from and where clauses below are used by the find by name and id methods
    public static final String SEARCH_JOB_SELECT = "select pyvpase_id, pyvpasj_pidm, " +
            "pyvpasj_posn, pyvpasj_suff, pyvpase_first_name, " +
            "pyvpase_last_name, pyvpasj_desc ";
    public static final String SEARCH_JOB_FROM = "from pyvpasj, pyvpase ";
    public static final String SEARCH_JOB_WHERE = "where pyvpase_pidm = pyvpasj_pidm ";
    public static final String SEARCH_JOB_ORDER_BY = " order by pyvpase_last_name, " +
            "pyvpase_first_name ";
    public static final String SEARCH_TOO_MANY_RESULTS = "Your search returned too many results. " +
            "Pleae refine your search.";

    /**
     * Traverses up the supervising chain of the given job and if the given pidm matches
     * a supervisor it returns true.
     *
     * @param job   Job to traverse the supervising chain
     * @param pidm  Employee to check whether or not is upper supervisor
     * @return boolean
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public static boolean isUpperSupervisor(Job job, int pidm) throws ModelException {
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
     * Determines whether a person or (job if posno is not empty) has any job
     * which is a supervising job.
     *
     * @param pidm  pidm of employee to check
     * @return isSupervisor
     * @throws Exception
     */
    public static boolean isSupervisor(int pidm, String posno) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        int employeeCount;
        employeeCount = 0;

        String sql = "SELECT count(*) AS countNum FROM PYVPASJ WHERE " +
                "PYVPASJ_SUPERVISOR_PIDM = :pidm AND PYVPASJ_STATUS != 'T'";

        // Use the posno if provided
        if (!StringUtils.isEmpty(posno)) {
            sql += " AND PYVPASJ_SUPERVISOR_POSN = :posno";
        }

        Query query = session.createSQLQuery(sql).setInteger("pidm", pidm);
        if (!StringUtils.isEmpty(posno)) {
            query.setString("posno", posno);
        }
        List<Object> results = query.list();

        if (!results.isEmpty()) {
            employeeCount = Integer.parseInt(results.get(0).toString());
        }
        return employeeCount > 0;
    }

    /**
     * Iterates over the jobs that an employee holds and returns the job that is a supervisor
     *
     * @param employee
     * @return
     * @throws Exception
     */
    public static Job getSupervisorJob(Employee employee) throws Exception {
        for (Job job : (Set<Job>) employee.getJobs()) {
            if (isSupervisor(employee.getId(), job.getPositionNumber())) {
                return job;
            }
        }
        return null;
    }

    /**
     * Retrieves a list of Jobs from the database.
     * @return
     */
    //@todo: Where do you use this method.  This is a very expensive operation.
    public List<Job> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return session.createQuery("from edu.osu.cws.evals.models.Job").list();
    }

    /**
     * @todo: queries for all the not terminated jobs of a certain appointment type and returns them.
     *
     * @param appointmentType: the type of appointment (classified, classifiedIT, ...)
     * @return a list of not terminated jobs of businessType.
     */
    public static List<Job> listNotTerminatedJobs(String appointmentType) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        List<Job> jobs = session.createQuery("from edu.osu.cws.evals.models.Job job " +
                "where job.status != 'T' and job.appointmentType = :appointmentType")
                .setString("appointmentType", appointmentType)
                .list();
        return jobs;
    }

    /**
     * Gets a list of jobs that are not terminated (status != T) that match the provided
     * appointment types. The job objects only have a few properties populated: pidm,
     * position number, suffi, job status and appointment type.
     *
     * @param appointmentTypes  ArrayList of different appointment types to fetch jobs for.
     * @return
     * @throws Exception
     */
    public static List<Job> listShortNotTerminatedJobs(ArrayList<String> appointmentTypes) throws Exception {
        List<Job> jobs;
        Session session = HibernateUtil.getCurrentSession();

        String query = "select new edu.osu.cws.evals.models.Job(employee.id, positionNumber, suffix, " +
                "status, appointmentType) from edu.osu.cws.evals.models.Job job " +
                "where job.status != 'T' and job.appointmentType in (:appointmentTypes) " +
                "and job.beginDate <= :currentDate and job.suffix = '00'";

        jobs = session.createQuery(query)
                .setParameterList("appointmentTypes", appointmentTypes)
                .setDate("currentDate", new Date())
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
     * Gets a list of jobs that are not terminated (status != T) that match the provided
     * appointment types. The job objects only have a few properties populated: pidm,
     * position number, suffi, job status and appointment type.
     *
     * @param supervisorJob          The supervisor job to get the list of employees
     * @param appointmentTypes      ArrayList of different appointment types to fetch jobs for.
     * @return
     * @throws Exception
     */
    public static List<Job> listEmployeesShortJobs(Job supervisorJob, List<String> appointmentTypes)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        List<Job> jobs = (List<Job>) session.getNamedQuery("job.directShortJobEmployees")
                .setInteger("supervisorId", supervisorJob.getEmployee().getId())
                .setString("supervisorPosno", supervisorJob.getPositionNumber())
                .setString("supervisorSuffix", supervisorJob.getSuffix())
                .setParameterList("appointmentTypes", appointmentTypes)
                .list();
        return jobs;
    }

    /**
     * Returns the list of from the list that do not have active evaluations.
     *
     * @param jobs
     * @return
     * @throws Exception
     */
    public static ArrayList<Job> getJobWithoutActiveEvaluations(List<Job> jobs) throws Exception {
        if (jobs == null || jobs.isEmpty()) {
            return null;
        }

        Session session = HibernateUtil.getCurrentSession();
        return (ArrayList<Job>) session.getNamedQuery("job.jobsWithoutActiveEvaluations")
                .setParameterList("jobs", jobs)
                .list();
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
                "where job.employee.id = :pidm and job.status != 'T'";

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
     * Returns a list of the direct employees under an mid-level or upper supervisor.
     *
     * @param supervisorJob
     * @param supervisorsOnly       Whether or not we only want direct supervisors only
     * @return  List<Job>
     * @throws Exception
     */
    public static List<Job> getDirectEmployees(Job supervisorJob, boolean supervisorsOnly)
            throws Exception {
        List<Job> directSupervisors = new ArrayList<Job>();
        Session session = HibernateUtil.getCurrentSession();

        List<Job> results = (List<Job>) session.getNamedQuery("job.directSupervisors")
                .setInteger("id", supervisorJob.getEmployee().getId())
                .setString("posno", supervisorJob.getPositionNumber())
                .setString("suffix", supervisorJob.getSuffix())
                .list();

        if (!supervisorsOnly) {
            return results;
        }

        for (Job directEmployeeJob : results) {
            int pidm = directEmployeeJob.getEmployee().getId();
            String posno = directEmployeeJob.getPositionNumber();
            if (isSupervisor(pidm, posno)) {
                directSupervisors.add(directEmployeeJob);
            }
        }

        return directSupervisors;
    }

    /**
     * Checks if the supervisor is the bottom level supervisor.
     *
     * @param supervisor
     * @return
     */
    public static boolean isBottomLevelSupervisor(Job supervisor) {
        Session session = HibernateUtil.getCurrentSession();
        String select = "SELECT count(*) FROM pyvpasj ";
        String where = "WHERE pyvpasj_status = 'A' " +
                "AND PYVPASJ_APPOINTMENT_TYPE in (:appointmentTypes) AND " +
                "level > 2 ";
        List<Job> directSupervisors = new ArrayList<Job>();
        directSupervisors.add(supervisor);

        String startWith = EvalsUtil.getStartWithClause(directSupervisors.size());
        String sql = select + where + startWith + Constants.CONNECT_BY;

        // count the # of supervisors under the current supervisor
        Query query = session.createSQLQuery(sql)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);
        EvalsUtil.setStartWithParameters(directSupervisors, query);
        BigDecimal result = (BigDecimal) query.uniqueResult();

        int supervisorCount = Integer.parseInt(result.toString());
        return supervisorCount < 1;
    }

    /**
     * Checks if the employee with the given pidm is a
     * professional supervisor.
     *
     * @param pidm
     * @return
     */
    public static boolean isProfessionalSupervisor(int pidm) {
        Session session = HibernateUtil.getCurrentSession();
        Query query = session.getNamedQuery("job.isProfessionalSupervisor");
        query.setParameter("pidm", pidm);
        query.setString("apt_type", AppointmentType.PROFESSIONAL_FACULTY);
        int result = Integer.parseInt(query.list().get(0).toString());

        return result > 0;
    }

    /**
     * Returns the first superivsing job that the given Employee pidm holds.
     *
     * @param pidm
     * @return
     */
    public static Job getSupervisingJob(int pidm) {
        // Check for invalid pidm
        if (pidm < 1) {
            return null;
        }

        Session session = HibernateUtil.getCurrentSession();
        List<Job> teamJobs = (List<Job>) session.getNamedQuery("job.firstSupervisorJob")
                .setInteger("id", pidm)
                .list();

        if (teamJobs != null && !teamJobs.isEmpty()) {
            return teamJobs.get(0).getSupervisor();
        }

        return null;
    }


    /**
     * Returns the jobs with a matching osu id in the employees table. The jobs returned
     * contain: employee osuid, pidm, posno, and suffix. It performs an exact search on the osuid.
     *
     * @param searchTerm      osuid of employee
     * @param bcName
     * @param supervisorPidm
     * @return  Employee
     * @throws Exception
     */
    public static List<Job> findByOsuid(String searchTerm, String bcName, int supervisorPidm)
            throws Exception {
        String sql = SEARCH_JOB_SELECT + SEARCH_JOB_FROM + SEARCH_JOB_WHERE +
                "and pyvpase_id = :searchTerm ";

        return findHelper(searchTerm, bcName, supervisorPidm, sql, null, true);
    }

    /**
     * We only allow searching by first name or last name. Names with numbers in
     * them are not considered valid. The jobs returned contain: employee osuid, pidm, posno
     * and suffix. It does a like search with a wild card '%' at the end of the name.
     *
     * @param searchTerm            First/last name or first last names.
     * @param bcName                If the logged in user is reviewer, their BC.
     * @param supervisorPidm        If is not 0, the logged in user is a supervisor
     * @return
     * @throws Exception
     */
    public static List<Job> findByName(String searchTerm, String bcName, int supervisorPidm)
            throws Exception {
        ArrayList<String> conditions = new ArrayList<String>();
        String where = SEARCH_JOB_WHERE;

        searchTerm = StringUtils.trim(searchTerm).toLowerCase();
        searchTerm = StringUtils.remove(searchTerm, ",");

        // if the user entered two words split them up and search by first, last and last, first
        String[] tokens = StringUtils.split(searchTerm, " ");
        if (tokens.length > 1) {
            conditions.add("(lower(pyvpase_first_name) like :token0 " +
                    "and lower(pyvpase_last_name) like :token1)");
            conditions.add("(lower(pyvpase_first_name) like :token1 " +
                    "and lower(pyvpase_last_name) like :token0)");
        } else {
            // we only search by name and first name if only 1 word was entered. otherwise, you get
            // too many results
            conditions.add("(lower(pyvpase_first_name) like :searchTerm or " +
                "lower(pyvpase_last_name) like :searchTerm)");
        }

        where += "and (" + StringUtils.join(conditions, " or ") + ")";
        String sql = SEARCH_JOB_SELECT + SEARCH_JOB_FROM + where ;

        return findHelper(searchTerm, bcName, supervisorPidm, sql, tokens, false);
    }

    /**
     * Helper method that adds the where clause to filter by bcName, supervisor. This method is
     * used by findByName and findByOsuid. It lso sets the parameter for searchTerm osuid or single
     * name and multiple names.
     *
     * @param searchTerm
     * @param bcName
     * @param supervisorPidm
     * @param sql
     * @param tokens
     * @param isNumeric
     * @return
     * @throws Exception
     */
    private static List<Job> findHelper(String searchTerm, String bcName, int supervisorPidm,
                                        String sql, String[] tokens, boolean isNumeric)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        boolean addBC = !StringUtils.isEmpty(bcName);
        boolean addSupervisor = supervisorPidm != 0;
        List<Job> supervisorJobs = null;

        if (addBC) {
            sql += "and pyvpasj_bctr_title = :bcName ";
        }

        if (addSupervisor) {
            supervisorJobs = getJobs(supervisorPidm);
            if (supervisorJobs.isEmpty()) {
                addSupervisor = false;
            } else {
                String startWith = EvalsUtil.getStartWithClause(supervisorJobs.size());
                sql += startWith + Constants.CONNECT_BY;
            }
        }

        sql += SEARCH_JOB_ORDER_BY;
        Query query = session.createSQLQuery(sql);

        if (tokens != null && tokens.length > 1) { // first last name search
            query.setString("token0", tokens[0]+"%");
            query.setString("token1", tokens[1]+"%");
        } else { // either osuid or first/last name (only 1 word)
            if (isNumeric) {
                query.setInteger("searchTerm", Integer.parseInt(searchTerm));
            } else {
                query.setString("searchTerm", searchTerm + "%");
            }
        }

        if (addBC) {
            query.setString("bcName", bcName);
        }
        if (addSupervisor) {
            EvalsUtil.setStartWithParameters(supervisorJobs, query);
        }

        return convertSearchArrayToJobList(query);
    }

    /**
     * Helper method used by findHelper. It takes the hibernate query and executes it. Then
     * it takes the sql results and converts them into a list of jobs. Column mappings:
     *
     *  row[0] = osuid, row[1] = pidm, row[2] = posn, row[3] = suff, row[4] = first name,
     *  row[5] = last name, row[6] = job title
     *
     * @param query
     * @return
     * @throws Exception
     */
    private static List<Job> convertSearchArrayToJobList(Query query) throws Exception {
        List<Object[]> result = query.list();

        // If the search is too brad, stop and let the user refine it
        if (result.size() > Constants.SEARCH_MAX_RESULTS) {
            throw new ModelException(SEARCH_TOO_MANY_RESULTS);
        }

        List<Job> jobs = new ArrayList<Job>();

        for (Object[] row : result) {
            Employee employee = new Employee(Integer.parseInt(row[1].toString()));
            employee.setOsuid(row[0].toString());
            employee.setFirstName(row[4].toString());
            employee.setLastName(row[5].toString());

            Job job = new Job(employee, row[2].toString(), row[3].toString());
            job.setJobTitle(row[6].toString());
            jobs.add(job);
        }
        jobs = addSupervisorToJobs(jobs);
        return jobs;
    }

    /**
     * Add supervisors to the jobs from the convertSearchArrayToJobList method. The original
     * query doesn't add the supervisor information due to performance concerns.
     *
     * @param jobs
     * @return
     * @throws Exception
     */
    private static List<Job> addSupervisorToJobs(List<Job> jobs) throws Exception {
        List<Job> jobsWithSupervisors = new ArrayList<Job>();

        for (Job job : jobs) {
            addSupervisorToJob(job);
            jobsWithSupervisors.add(job);
        }

        return jobsWithSupervisors;
    }

    /**
     * Loads the supervisor pojo for the given job. This helps when we have queries that
     * don't include the supervisor due to performance issues.
     *
     * @param job
     * @throws Exception
     */
    public static Job addSupervisorToJob(Job job) throws Exception {
        try {
            int pidm = job.getEmployee().getId();
            Job dbJob = getJob(pidm, job.getPositionNumber(), job.getSuffix());
            job.setSupervisor(dbJob.getSupervisor());

            // initialize property needed
            if (job.getSupervisor() != null && job.getSupervisor().getEmployee() != null) {
                job.getSupervisor().getEmployee().getName();
                job.getSupervisor().getIdKey();
            }
        } catch (org.hibernate.ObjectNotFoundException e) { // handle missing job/supervisor
            job.setSupervisor(null);
        }

        return job;
    }

    /**
     * When searching by osuid, we require numeric 9 digits. When searching by name, we don't allow
     * numbers along with it. This method relies on findByOsuid and findByName to do the work.
     *
     * @param searchTerm        The osuid or names to search for
     * @param bcName            The bcName if the logged in user is a reviewer
     * @param supervisorPidm    If is not 0, then the logged in user is a supervisor
     * @return                  List of jobs that matched the search criteria.
     * @throws Exception
     */
    public static List<Job> search(String searchTerm, String bcName, Integer supervisorPidm)
            throws Exception {
        List<Job> result = null;
        if (CWSUtil.validateOsuid(searchTerm)) {
            result = findByOsuid(searchTerm, bcName, supervisorPidm);
        } else if (CWSUtil.validateNameSearch(searchTerm)) {
            result = findByName(searchTerm, bcName, supervisorPidm);
        }

        return result;
    }

    /**
     * Find the orgCode in the database. If the bcName is passed in, then we do a permission check
     * to make sure it belongs to that BC.
     *
     * @param orgCode
     * @param bcName
     * @return
     * @throws Exception
     */
    public static boolean findOrgCode(String orgCode, String bcName) throws Exception {
        orgCode = StringUtils.trim(orgCode);
        if (!CWSUtil.validateOrgCode(orgCode)) {
            return false;
        }

        Session session = HibernateUtil.getCurrentSession();
        String hql = "select count(*) from edu.osu.cws.evals.models.Job " +
                "where tsOrgCode = :orgCode ";
        boolean addBC = !StringUtils.isEmpty(bcName);

        // Add bc name as a permission check
        if (addBC) {
            hql += "and businessCenterName = :bcName";
        }

        Query query = session.createQuery(hql).setString("orgCode", orgCode);
        if (addBC) {
            query.setString("bcName", bcName);
        }

        List<Object> results = query.list();
        int orgCodeCount = 0;
        if (!results.isEmpty()) {
            orgCodeCount = Integer.parseInt(results.get(0).toString());
        }

        // If there were many jobs with this orgCode, then it is considered valid
        return orgCodeCount > 0;
    }

    /**
     * Returns the matching position description for the given job. The position description
     * only matches the employee's osu id, and the job's position #.
     *
     * @param job
     * @return
     */
    public static PositionDescription getPositionDescription(Job job) {
        Session session = HibernateUtil.getCurrentSession();
        return (PositionDescription) session.getNamedQuery("positionDescription.getPD")
                .setString("osuid", job.getEmployee().getOsuid())
                .setString("posno", job.getPositionNumber())
                .uniqueResult();
    }
}