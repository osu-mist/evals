package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.portlet.Constants;
import edu.osu.cws.evals.portlet.ReportsAction;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.evals.util.HibernateUtil;
import edu.osu.cws.util.CWSUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.*;

public class AppraisalMgr {

    // used to sort the list of evaluations displayed during searches
    private static final String LIST_ORDER = " order by job.employee.lastName, " +
            "job.employee.firstName, startDate";

    // Constants that specify appraisal list for report
    private static final String REPORT_LIST_SELECT = "select new edu.osu.cws.evals.models.Appraisal " +
            "(id, job.employee.firstName, job.employee.lastName, startDate, endDate," +
            " status, overdue, job.employee.id, job.positionNumber, job.suffix)" +
            " from edu.osu.cws.evals.models.Appraisal ";
    private static final String REPORT_LIST_WHERE = " where status not in ('completed', 'archived', " +
            "'closed') ";


    /**
     * This method creates an appraisal for the given job by calling the Hibernate
     * class. It returns the id of the created appraisal.
     *
     * @param job   Job for this appraisal
     * @param type: trial, annual, initial
     * @param startDate: (DateTime) starting date of appraisal period.
     * @return appraisal.id
     * @throws Exception
     */
    public static Appraisal createAppraisal(Job job, DateTime startDate, String type)
            throws Exception {
        Appraisal appraisal = new Appraisal();
        GoalVersion goalVersion = new GoalVersion();
        appraisal.addGoalVersion(goalVersion);
        goalVersion.setCreateDate(new Date());
        // the first goal version is automatically approved
        goalVersion.setRequestDecision(true);

        if (!type.equals(Appraisal.TYPE_TRIAL) && !type.equals(Appraisal.TYPE_ANNUAL) &&
                !type.equals(Appraisal.TYPE_INITIAL)) {
            throw new ModelException("Invalid appraisal type : " + type);
        }

        appraisal.setJob(job);
        appraisal.setStartDate(startDate.toDate());
        appraisal.setCreateDate(new Date());
        appraisal.setRating(0);
        appraisal.setStatus(Appraisal.STATUS_GOALS_DUE);

        // In the db, we only store: annual or trial.
        String dbType = type;
        if (type.equals(Appraisal.TYPE_INITIAL)) {
            dbType = Appraisal.TYPE_ANNUAL;
        }
        appraisal.setType(dbType);

        DateTime endDate = job.getEndEvalDate(startDate, type);
        appraisal.setEndDate(CWSUtil.toDate(endDate));

        if (appraisal.validate()) {
            Session session = HibernateUtil.getCurrentSession();

            // Create the assessments & assessment criteria
            List<CriterionArea> criteriaList = CriteriaMgr.list(job.getAppointmentType());
            addAssessmentToGoalVersion(goalVersion, Constants.BLANK_ASSESSMENTS_IN_NEW_EVALUATION,
                    criteriaList);
            session.save(appraisal);
        }

        return appraisal;
    }

    /**
     * Creates a series of new assessments and adds them to a goal version.
     *
     * @param goalVersion
     * @param count
     * @param criteriaList
     */
    public static void addAssessmentToGoalVersion(GoalVersion goalVersion, int count,
                                                  List<CriterionArea> criteriaList) throws Exception {
        for (int i = 1; i <= count; i++) {
            createNewAssessment(goalVersion, i, criteriaList);
        }
    }

    /**
     * Creates assessments and associated objects for a goal version once the goals reactivation
     * is approved. The criteria list associated to the new assessment is the list originally
     * used when the first goal version was created.
     *
     * @param unapprovedGoalsVersion
     * @param appraisal
     * @throws Exception
     */
    public static void addAssessmentForGoalsReactivation(GoalVersion unapprovedGoalsVersion,
                                                         Appraisal appraisal) throws Exception {
        // get the sorted criteria list for the first approved to goal version
        List<CriterionArea> criteriaList = new ArrayList<CriterionArea>();
        GoalVersion goalVersion = appraisal.getApprovedGoalsVersions().get(0);
        if (goalVersion != null && !goalVersion.getAssessments().isEmpty()) {
            Assessment assessment = (Assessment) goalVersion.getAssessments().toArray()[0];
            if (assessment != null && !assessment.getAssessmentCriteria().isEmpty()) {
                for (AssessmentCriteria assessmentCriteria : assessment.getSortedAssessmentCriteria()) {
                    criteriaList.add(assessmentCriteria.getCriteriaArea());
                }
            }
        }

        addAssessmentToGoalVersion(unapprovedGoalsVersion,
                Constants.BLANK_ASSESSMENTS_IN_REACTIVATED_GOALS, criteriaList);


        Session session = HibernateUtil.getCurrentSession();
        session.save(unapprovedGoalsVersion);
    }


    /**
     * This method is called upon completion or closure of an trial appraisal to create the
     * initial appraisal record.
     * It copies the jobpidm, position#, subfix, all the fields related to goals,
     * the assessment records from the trial records.
     * Depends on the times, it either sets the status to goalsApproved, resultsDue
     * or results over due.
     * startDate of the appraisal record is the first day of the month on or after
     * the eval_date or job_start_date.
     * end_date of the appraisal record is the number of month indicated by
     * PYVPASJ.annual_eval_ind.
     *
     * @param  trialAppraisal: this is the newly closed or completed trial appraisal
     * @return the newly created appraisal
     * @throws Exception
     */
    public static Appraisal createInitialAppraisalAfterTrial(Appraisal trialAppraisal)
            throws Exception {
        // copy appraisal & properties
        Appraisal appraisal = Appraisal.createFirstAnnual(trialAppraisal);

        Session session = HibernateUtil.getCurrentSession();
        session.save(appraisal);

        return appraisal;
    }

    /**
     * Updates the appraisal object along with the assessment object. If the goals have been
     * modified, a new record is inserted in the assessments_logs table.
     *
     * @param modifiedAppraisal
     * @param loggedInUser
     * @return
     * @throws ModelException
     */
    public static boolean updateAppraisal(Appraisal modifiedAppraisal, Employee loggedInUser)
            throws ModelException {
        String originalGoalText;
        String updatedGoalTextGoalText;
        GoalLog goalLog;

        // Try to save the data
        Session session = HibernateUtil.getCurrentSession();
        session.saveOrUpdate(modifiedAppraisal);

        // save salary object if present
        if (modifiedAppraisal.getSalary() != null) {
            session.saveOrUpdate(modifiedAppraisal.getSalary());
        }

        for (GoalVersion goalVersion : modifiedAppraisal.getGoalVersions()) {
            for (Assessment assessment : goalVersion.getAssessments()) {
                assessment.setModifiedDate(new Date()); //@todo: need to figure out a better way to set this. It isn't always updated
                session.saveOrUpdate(assessment);  //@todo: joan: Do we need to do this everytime?

                // Create new assessment log if necessary
                originalGoalText = assessment.getLastGoalLog(GoalLog.DEFAULT_GOAL_TYPE).getContent();
                updatedGoalTextGoalText = assessment.getGoal();
                //@todo: use a hash instead of comparing these two long text fields
                if (!originalGoalText.equals(updatedGoalTextGoalText)) {
                    goalLog = new GoalLog();
                    goalLog.setCreateDate(new Date());
                    goalLog.setAuthor(loggedInUser);
                    if (updatedGoalTextGoalText == null || updatedGoalTextGoalText.equals("")) {
                        updatedGoalTextGoalText = "empty";
                    }
                    goalLog.setContent(updatedGoalTextGoalText);
                    assessment.addAssessmentLog(goalLog);
                    session.save(goalLog);
                }

            }
        }
        return true;
    }

    /**
     * Creates the first annual appraisal if needed. The first annual appraisal is created if:
     *  1) The current appraisal is of type: trial
     *  2) The job annual_indicator != 0
     *
     * @param trialAppraisal
     * @throws Exception
     * @return appraisal    The first annual appraisal created, null otherwise
     */
    public static Appraisal createFirstAnnualAppraisal(Appraisal trialAppraisal)
            throws Exception {
        Job job = trialAppraisal.getJob();

        if (!trialAppraisal.getType().equals(Appraisal.TYPE_TRIAL)) {
            return null;
        }
        if (job.getAnnualInd() == 0) {
            return null;
        }
        return createInitialAppraisalAfterTrial(trialAppraisal);
    }

    /**
     * Returns the first trial appraisal for the given job.
     *
     * @param job
     * @return trialAppraisal
     */
    public static Appraisal getTrialAppraisal(Job job) {
        Session session = HibernateUtil.getCurrentSession();

        Appraisal trialAppraisal = (Appraisal) session.getNamedQuery("appraisal.getTrialAppraisal")
                .setInteger("pidm", job.getEmployee().getId())
                .setString("posno", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .uniqueResult();
        return trialAppraisal;
    }

    /**
     * Returns a list of active appraisals for all the jobs that the current pidm holds. If the
     * posno and suffix are provided, the evaluations are specific to the job.
     * The fields that are returned in the appraisal are:
     *      id
     *      Job.jobTitle
     *      startDate
     *      endDate
     *      status
     *      overdue
     *
     * @return
     * @param pidm
     * @param posno
     * @param suffix
     */
    public static ArrayList<Appraisal> getAllMyActiveAppraisals(int pidm, String posno,
                                                                String suffix) {
        Session session = HibernateUtil.getCurrentSession();
        String query = "select new edu.osu.cws.evals.models.Appraisal(id, job.jobTitle, startDate, " +
                "endDate, status, overdue)" +
                " from edu.osu.cws.evals.models.Appraisal where " +
                " job.employee.id = :pidm and status not in ('archived')";

        if (posno != null && suffix != null) {
            query += " and job.positionNumber = :posno and job.suffix = :suffix";
        }

        Query hibQuery = session.createQuery(query).setInteger("pidm", pidm);
        if (posno != null && suffix != null) {
            hibQuery.setString("posno", posno).setString("suffix", suffix);
        }

        ArrayList<Appraisal> result = (ArrayList<Appraisal>) hibQuery.list();

        // Check the status of the appraisal and check to see if it needs to be replaced
        for(Appraisal appraisal : result) {
            appraisal.setRole("employee");
        }
        return result;
    }

    /**
     * Returns a list of appraisals with limited attributes set: id, job title, employee name,
     * job appointment type, start date, end date, status, goalsRequiredModification and
     * employeSignedDate. If the posno and suffix are specific, the team appraisals are
     * specific to that supervising job.
     *
     * @param pidm          Supervisor's pidm.
     * @param onlyActive    Whether or not to include only the active appraisals
     * @param posno         Supervisor's posno
     * @param suffix        Supervisor's suffix
     * @return List of Appraisal that contains the jobs this employee supervises.
     */
    public static ArrayList<Appraisal> getMyTeamsAppraisals(Integer pidm, boolean onlyActive,
                                                 String posno, String suffix) {
        ArrayList<Appraisal> appraisals = new ArrayList<Appraisal>();
        List<Integer> pidms = new ArrayList<Integer>();
        Session session = HibernateUtil.getCurrentSession();

        String query = "select ap.ID, jobs.PYVPASJ_DESC, jobs.PYVPASJ_APPOINTMENT_TYPE, " +
                "ap.START_DATE, ap.END_DATE, ap.STATUS, ap.GOALS_REQUIRED_MOD_DATE, " +
                "ap.EMPLOYEE_SIGNED_DATE, jobs.PYVPASJ_PIDM, ap.OVERDUE " +
                "FROM appraisals ap, PYVPASJ jobs " +
                "WHERE ap.JOB_PIDM=jobs.PYVPASJ_PIDM AND ap.POSITION_NUMBER=jobs.PYVPASJ_POSN " +
                "AND ap.JOB_SUFFIX=jobs.PYVPASJ_SUFF AND jobs.PYVPASJ_SUPERVISOR_PIDM=:pidm ";

        if (!StringUtils.isEmpty(posno) && !StringUtils.isEmpty(suffix)) {
            query += "AND jobs.PYVPASJ_SUPERVISOR_POSN=:posno " +
                    "AND jobs.PYVPASJ_SUPERVISOR_SUFF = :suffix ";
        }

        if (onlyActive) {
            query += "AND status NOT IN ('archived') ";
        }

        Query hibQuery = session.createSQLQuery(query)
                .addScalar("ID", StandardBasicTypes.INTEGER)
                .addScalar("PYVPASJ_DESC", StandardBasicTypes.STRING)
                .addScalar("PYVPASJ_APPOINTMENT_TYPE", StandardBasicTypes.STRING)
                .addScalar("START_DATE", StandardBasicTypes.DATE)
                .addScalar("END_DATE", StandardBasicTypes.DATE)
                .addScalar("STATUS", StandardBasicTypes.STRING)
                .addScalar("GOALS_REQUIRED_MOD_DATE", StandardBasicTypes.DATE)
                .addScalar("EMPLOYEE_SIGNED_DATE", StandardBasicTypes.DATE)
                .addScalar("PYVPASJ_PIDM", StandardBasicTypes.INTEGER)
                .addScalar("OVERDUE", StandardBasicTypes.INTEGER)
                .setInteger("pidm", pidm);
        if (!StringUtils.isEmpty(posno) && !StringUtils.isEmpty(suffix)) {
            hibQuery.setString("posno", posno).setString("suffix", suffix);
        }
        List<Object[]> result =  hibQuery.list();

        if (result.isEmpty()) {
            return appraisals;
        }

        // Build list of appraisals from sql results
        for (Object[] aResult : result) {
           Appraisal appraisal;
            Integer id = (Integer) aResult[0];
            String jobTitle = (String) aResult[1];
            String appointmentType = (String) aResult[2];
            Date startDate = (Date) aResult[3];
            Date endDate = (Date) aResult[4];
            String status = (String) aResult[5];
            Date goalsReqModDate = (Date) aResult[6];
            Date employeeSignDate = (Date) aResult[7];
            Integer employeePidm = (Integer) aResult[8];
            Integer overdue = (Integer) aResult[9];

            appraisal = new Appraisal(id, jobTitle, null, null, appointmentType,
                    startDate, endDate, status, goalsReqModDate, employeeSignDate, employeePidm,
                    overdue);
            appraisals.add(appraisal);
            pidms.add(employeePidm);
        }

        List<Employee> employees = session.getNamedQuery("employee.firstAndLastNameByPidm")
                .setParameterList("ids", pidms).list();
        HashMap<Integer, Employee> employeeHashMap = new HashMap<Integer, Employee>();
        for (Employee employee : employees) {
            employeeHashMap.put(employee.getId(), employee);
        }

        for (Appraisal ap : appraisals) {
            Integer employeePidm = ap.getJob().getEmployee().getId();
            Employee employee = employeeHashMap.get(employeePidm);
            ap.getJob().setEmployee(employee);
        }

        ArrayList<Appraisal> myTeamAppraisals = new ArrayList<Appraisal>();
        if (appraisals != null) {
            for (Appraisal appraisal : appraisals) {
                appraisal.setRole("supervisor");
                myTeamAppraisals.add(appraisal);
            }
        }

        return myTeamAppraisals;
    }

    /**
     * It returns the appraisal that matches the id.
     * It also adds the currentSupervisor to the appraisal object.
     *
     * @param id
     * @return Appraisal
     * @throws Exception
     */
    public static Appraisal getAppraisal(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);

        return appraisal;
    }

    /**
     * Returns an ArrayList of Appraisal which contain data about appraisals pending
     * review. This method is used to display a list of pending reviews in the displayReview
     * actions method. If maxResults > 0, it will limit the number of results.
     *
     * @param businessCenterName
     * @param maxResults
     * @return
     * @throws Exception
     */
    //@todo: Joan: don't do anything with job.endDate, that's not reliable.
    public static ArrayList<Appraisal> getReviews(String businessCenterName, int maxResults) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Query hibernateQuery = session.getNamedQuery("appraisal.getReviews")
                .setString("bc", businessCenterName);
        //@todo: Joan: can we set the maxResults before querying the database?
        if (maxResults > 0) {
            hibernateQuery.setMaxResults(maxResults);
        }
        ArrayList<Appraisal> result =  (ArrayList<Appraisal>) hibernateQuery.list();
        return result;
    }

    /**
     * Returns a count of the pending reviews for a given business center.
     *
     * @param businessCenterName
     * @return
     * @throws Exception
     */
    public static int getReviewCount(String businessCenterName) throws Exception {
        int reviewCount = 0;
        Session session = HibernateUtil.getCurrentSession();
        List results = session.getNamedQuery("appraisal.reviewCount")
                .setString("bcName", businessCenterName)
                .list();
        if (!results.isEmpty()) {
            reviewCount = Integer.parseInt(results.get(0).toString());
        }
        return reviewCount;
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are due for review for a business center.
     */
    public static int getReviewDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, Appraisal.STATUS_REVIEW_DUE);
    }

    /**
     * Returns the count of reviews due for a specific business center based on the status.
     *
     * @param bcName
     * @param status
     * @return
     * @throws Exception
     */
    private static int getReviewCountByStatus(String bcName, String status) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "select count(*) "+
                "from edu.osu.cws.evals.models.Appraisal where job.businessCenterName = :bc " +
                "and status in (:status) and job.endDate is NULL";

        Object countObj = session.createQuery(query)
                .setString("bc", bcName)
                .setString("status", status)
                .list().get(0);

        return Integer.parseInt(countObj.toString());
    }

    /**
     * @param bcName: name of the business center
     * @return  the number of appraisals that are overdue for review for a business center.
     */
    public static int getReviewOvedDueCount(String bcName) throws Exception {
        return getReviewCountByStatus(bcName, Appraisal.STATUS_REVIEW_OVERDUE);
    }

    /**
     * select id from appraisals where status is not completed or closed.
     * @return an array of int containing the appraisalID's of
     * all the appraisal records whose status are not "completed", "closed"
     * or "archived".
     * It now assumes all appraisal records are classified appraisal records.
     * @throws Exception
     */
    public static int[] getOpenIDs() throws Exception {
        int[] ids;
        List result;
        Session session = HibernateUtil.getCurrentSession();
        String query = "select appraisal.id from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where status not in ('completed', 'closed', 'archived')";

        result = session.createQuery(query).list();
        ids = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            ids[i] = (Integer) result.get(i);
        }

        return ids;
    }

    /**
     *
     * @param job
     * @return  true is a trial appraisal exist for the job, false otherwise
     */
    public static boolean trialAppraisalExists(Job job) throws Exception {
        DateTime startDate = job.getTrialStartDate();
        return startDate != null && appraisalExists(job, startDate, Appraisal.TYPE_TRIAL);
    }

    /** select count(*) from appraisals where job.... and startDAte = startDate and type = type
     * @param job: job against which the appraisal was create
     * @param startDate: (DateTime) start date of appraisal period
     * @param type: "trial" or "annual".
     * @return true if an appraisal exist for job and startDate and type, false otherwise
     */
    public static boolean appraisalExists(Job job, DateTime startDate, String type) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        startDate = startDate.minusMonths(6);
        Date beginDate = startDate.toDate();
        Date endDate = startDate.plusMonths(12).toDate();

        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                "and appraisal.startDate >= :beginDate and appraisal.startDate <= :endDate";

        Iterator resultMapIter = session.createQuery(query)
                .setInteger("pidm", job.getEmployee().getId())
                .setString("positionNumber", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .setString("type", type)
                .setDate("beginDate", beginDate)
                .setDate("endDate", endDate)
                .setMaxResults(1)
                .list().iterator();

        int appraisalCount = 0;
        if (resultMapIter.hasNext()) {
            appraisalCount =  Integer.parseInt(resultMapIter.next().toString());
        }
        return (appraisalCount > 0);
    }

    /**  @@todo: Jose to implement this
     * @param job
     * @return true if there is a trial appraisal not in the status of "closed", "completed"
     *          or "archived". false otherwise.
     * @throws Exception
     */
    public static boolean openTrialAppraisalExists(Job job) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        String query = "select count(*) from edu.osu.cws.evals.models.Appraisal appraisal " +
                "where appraisal.job.employee.id = :pidm and appraisal.job.positionNumber = :positionNumber " +
                "and appraisal.job.suffix = :suffix and appraisal.type = :type " +
                "and (appraisal.status != 'closed' AND  appraisal.status != 'completed' AND " +
                "appraisal.status != 'archived')";

        Iterator resultMapIter = session.createQuery(query)
                .setInteger("pidm", job.getEmployee().getId())
                .setString("positionNumber", job.getPositionNumber())
                .setString("suffix", job.getSuffix())
                .setString("type", Appraisal.TYPE_TRIAL)   //@todo: Joan: it's always going to be trial, right?
                .setMaxResults(1)  //@todo: Joan: No need to set maxResuls?
                .list().iterator();

        int appraisalCount = 0;
        if (resultMapIter.hasNext()) {
            appraisalCount =  Integer.parseInt(resultMapIter.next().toString());
        }
        return (appraisalCount > 0);
    }

    /**
     * @param job
     * @param appraisalStartDate    DateTime object
     * @return
     * @throws Exception
     */
    public static boolean AnnualExists(Job job, DateTime appraisalStartDate) throws Exception
    {
        if (appraisalExists(job, appraisalStartDate, Appraisal.TYPE_ANNUAL))
            return true;

        //If we get here, there is no record for the job for appraisalStartDate
        //It's possible that someone added a value for Pyvpasj_eval_date after we created
        //the previous appraisal, so need to check that.
        int thisYear = appraisalStartDate.getYear();
        DateTime startDateBasedOnJobBeginDate = job.getAnnualStartDateBasedOnJobBeginDate(thisYear);

        return !startDateBasedOnJobBeginDate.equals(appraisalStartDate) &&
                appraisalExists(job, startDateBasedOnJobBeginDate, Appraisal.TYPE_ANNUAL);
    }


    /**
     * Updates the appraisal status and originalStatus using the id of the appraisal and hsql query.
     *
     * @param appraisal
     * @throws Exception
     */
    public static void updateAppraisalStatus(Appraisal appraisal) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "update edu.osu.cws.evals.models.Appraisal appraisal " +
                "set status = :status, originalStatus = :origStatus where id = :id";

        session.createQuery(query)
                .setString("status", appraisal.getStatus())
                .setString("origStatus", appraisal.getOriginalStatus())
                .setInteger("id", appraisal.getId())
                .executeUpdate();
    }


    /**
     * First get the jobs that match the search criteria. Then we get the list of appraisals
     * using the jobs in the where clause.
     *
     * @param searchTerm    osu id of or name the employee's appraisals we are searching
     * @param pidm          pidm of the logged in user
     * @param isSupervisor
     * @param bcName
     * @return
     * @throws Exception
     */
    public static List<Appraisal> search(String searchTerm, int pidm, boolean isSupervisor,
                                         String bcName) throws Exception {
        int supervisorPidm = isSupervisor? pidm : 0;
        List<Job> jobs = JobMgr.search(searchTerm, bcName,supervisorPidm);

        String hql = "select new edu.osu.cws.evals.models.Appraisal ( " +
            "id, job.jobTitle, job.positionNumber, startDate, endDate, type, " +
            "job.employee.id, job.employee.lastName, job.employee.firstName, " +
            "evaluationSubmitDate, status, job.businessCenterName, " +
            "job.orgCodeDescription, job.suffix, overdue) from " +
            "edu.osu.cws.evals.models.Appraisal ";

        return getAppraisalsByJobs(jobs, hql, null);
    }

    /**
     * Returns a list of appraisals that are retrieved based on the jobs. These are used by
     * by appraisal.search and report search of employee appraisals.
     *
     * @param jobs
     * @param hql
     * @param conditions
     * @return
     * @throws Exception
     */
    private static List<Appraisal> getAppraisalsByJobs(List<Job> jobs, String hql,
                                                       List<String> conditions)
            throws Exception {
        // The list of jobs contains the pidm, posno and suff that match the search criteria and
        // the permission level of the logged in user.
        if (jobs == null || jobs.isEmpty()) {
            return new ArrayList<Appraisal>();
        }

        Session session = HibernateUtil.getCurrentSession();
        if (conditions == null) {
            conditions = new ArrayList<String>();
        }

        List<String> jobConditions = new ArrayList<String>();
        for (int i = 0; i < jobs.size(); i++) {
            String cond = "(job.employee.id = :pidm" + i + " and job.positionNumber = :posno" + i +
                    " and job.suffix = :suff"+i+")";
            jobConditions.add(cond);
        }

        // add the job pidm, posno and suffix to the hql query to only get the appraisals we need.
        if (!jobConditions.isEmpty()) {
            String jobJoinClause = StringUtils.join(jobConditions, " or ");
            conditions.add(jobJoinClause);
        }

        if (!conditions.isEmpty()) {
            hql += " where " + StringUtils.join(conditions, " and ") + " ";
        }

        hql += LIST_ORDER;
        Query query = session.createQuery(hql);

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            query.setInteger("pidm"+i, job.getEmployee().getId())
                    .setString("posno"+i, job.getPositionNumber())
                    .setString("suff"+i, job.getSuffix());
        }

        List<Appraisal> temp =  (ArrayList<Appraisal>) query.list();

        return addSupervisorToAppraisals(temp);
    }

    /**
     * Add the supervisor to the list of appraisal pojo. This is so that the jsp is able
     * to handle with job's not having supervisors.
     *
     * @param source
     * @return
     * @throws Exception
     */
    private static List<Appraisal> addSupervisorToAppraisals(List<Appraisal> source)
            throws Exception {
        List<Appraisal> destination = new ArrayList<Appraisal>();

        // Add supervisor pojo to the appraisal list
        for (Appraisal appraisal : source) {
            Job job = appraisal.getJob();
            Job tempJob = JobMgr.addSupervisorToJob(job);
            appraisal.setJob(tempJob);

            // Copy the appraisal pojo and fields to handle missing employee/job
            Appraisal nonDbAppraisal = new Appraisal(appraisal);
            destination.add(nonDbAppraisal);
        }

        return destination;
    }


    /**
     * This method is used by ReportsAction to display the list of evaluations for a given set of
     * jobs.
     *
     * @param jobs
     * @return
     */
    public static List<Appraisal> getEmployeeAppraisalList(List<Job> jobs) throws Exception {
        ArrayList<String> conditions = new ArrayList<String>();
        String hql = REPORT_LIST_SELECT;
        conditions.add("status not in ('completed', 'archived', 'closed')");

        return getAppraisalsByJobs(jobs, hql, conditions);
    }


    /**
     * Returns the data needed to generate the google charts and nothing more.
     *
     * @param paramMap
     * @param directSupervisors
     * @param inLeafSupervisor
     * @return
     */
    public static List<Appraisal> getReportListData(HashMap paramMap, List<Job> directSupervisors,
                                                    boolean inLeafSupervisor) throws Exception {
        Session session = HibernateUtil.getCurrentSession();

        List<Appraisal> results = new ArrayList<Appraisal>();
        String scope = (String) paramMap.get(ReportsAction.SCOPE);
        String scopeValue = (String) paramMap.get(ReportsAction.SCOPE_VALUE);
        String report = (String) paramMap.get(ReportsAction.REPORT);
        String bcName = (String) paramMap.get(Constants.BC_NAME);
        boolean addBC = !StringUtils.isEmpty(bcName);

        String hqlQuery = getReportListHQL(scope, report, addBC);
        Query listQuery = session.createQuery(hqlQuery)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);

        if (scope.equals(ReportsAction.SCOPE_BC)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("bcName", scopeValue)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            results = (ArrayList<Appraisal>) listQuery
                    .setParameter("orgPrefix", scopeValue + "%")
                    .setParameter("bcName", bcName)
                    .list();
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            listQuery.setParameter("tsOrgCode", scopeValue);

            // only set the bcName if it's not empty. if an admin searches, the bcName is empty
            if (addBC) {
                listQuery.setParameter("bcName", bcName);
            }

            results = (ArrayList<Appraisal>) listQuery.list();
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            List<Integer> appraisalIds = getAppraisalIdsForSupervisorReport(directSupervisors,
                    inLeafSupervisor);

            if (!appraisalIds.isEmpty()) {
                results = (ArrayList<Appraisal>) listQuery
                        .setParameterList("appraisalIds", appraisalIds)
                        .list();
            }
        } else {
            results = (ArrayList<Appraisal>) listQuery
                    .list();
        }

        return addSupervisorToAppraisals(results);
    }

    /**
     * Returns the hql needed to display the list of evaluation records in the reports.
     * The data is sorted by lastName, firstName
     *
     * @param scope
     * @param reportType
     * @param addBC         Whether the BC should be added to the where clause
     * @return
     */
    public static String getReportListHQL(String scope, String reportType, boolean addBC) {
        String where = REPORT_LIST_WHERE + " and job.appointmentType in :appointmentTypes ";

        //@todo: do we need to do bc filtering when the scope is supervisor?
        if (!scope.equals(ReportsAction.DEFAULT_SCOPE) &&
                !scope.equals(ReportsAction.SCOPE_SUPERVISOR) && addBC) {
            where += " and job.businessCenterName = :bcName";
        }

        if (scope.equals(ReportsAction.SCOPE_ORG_PREFIX)) {
            where += " and job.orgCodeDescription LIKE :orgPrefix";
        } else if (scope.equals(ReportsAction.SCOPE_ORG_CODE)) {
            where += " and job.tsOrgCode = :tsOrgCode";
        } else if (scope.equals(ReportsAction.SCOPE_SUPERVISOR)) {
            where += " and id in (:appraisalIds)";
        }

        if (ReportMgr.isWayOverdueReport(reportType)) {
            where += " and overdue > 30";
        } else if (ReportMgr.isOverdueReport(reportType)) {
            where += " and overdue > 0";
        }

        return REPORT_LIST_SELECT + where + LIST_ORDER;
    }

    /**
     * Returns the list of all the appraisals that match the current supervising chain all the
     * way down to the leaf.
     *
     * @param directSupervisors
     * @param inLeafSupervisor
     * @return
     */
    public static List<Integer> getAppraisalIdsForSupervisorReport(List<Job> directSupervisors,
                                                                   boolean inLeafSupervisor) {
        if (directSupervisors == null) {
            return null;
        }

        Session session = HibernateUtil.getCurrentSession();
        List<Integer> ids = new ArrayList<Integer>();

        String select = "SELECT appraisals.id FROM pyvpasj " +
                "LEFT JOIN appraisals ON (appraisals.job_pidm = pyvpasj_pidm AND " +
                "appraisals.position_number = pyvpasj_posn AND " +
                "appraisals.job_suffix = pyvpasj_suff) ";
        String where = "WHERE pyvpasj_status = 'A' " +
                "AND PYVPASJ_APPOINTMENT_TYPE in (:appointmentTypes) " +
                "and appraisals.status not in ('completed', 'archived', 'closed')";
        String startWith = EvalsUtil.getStartWithClause(directSupervisors.size());

        if (!inLeafSupervisor) {
            where += " AND level > 1 ";
        }
        String sql = select + where + startWith + Constants.CONNECT_BY;

        Query query = session.createSQLQuery(sql)
                .setParameterList("appointmentTypes", ReportsAction.APPOINTMENT_TYPES);
        EvalsUtil.setStartWithParameters(directSupervisors, query);
        List<BigDecimal> result = query.list();

        for (BigDecimal id : result) {
            ids.add(Integer.parseInt(id.toString()));
        }

        return ids;
    }

    /**
     * Saves the overdue value by itself in the appraisal record.
     *
     * @param appraisal
     */
    public static void saveOverdue(Appraisal appraisal) {
        Session session = HibernateUtil.getCurrentSession();
        session.getNamedQuery("appraisal.saveOverdue")
                .setInteger("id", appraisal.getId())
                .setInteger("overdue", appraisal.getOverdue())
                .executeUpdate();
    }

    /**
     * Takes care of creating a new Assessment object with the assessment criterias and sets up
     * the goal version association.
     *
     * @param goalVersion
     * @param sequence
     * @param criterionAreas
     * @return
     */
    public static Assessment createNewAssessment(GoalVersion goalVersion, int sequence,
                                     List<CriterionArea> criterionAreas) {
        Session session = HibernateUtil.getCurrentSession();
        Assessment assessment = new Assessment();
        goalVersion.addAssessment(assessment);
        assessment.setCreateDate(new Date());
        assessment.setModifiedDate(new Date());
        assessment.setSequence(sequence);

        Set<AssessmentCriteria> assessmentCriterias = new HashSet<AssessmentCriteria>();
        for (CriterionArea criterionArea : criterionAreas) {
            AssessmentCriteria assessmentCriteria = new AssessmentCriteria();
            assessment.addAssessmentCriteria(assessmentCriteria);
            assessmentCriteria.setAssessment(assessment);
            assessmentCriteria.setCriteriaArea(criterionArea);
            assessmentCriterias.add(assessmentCriteria);
        }
        assessment.setAssessmentCriteria(assessmentCriterias);

        return assessment;
    }

    /**
     * Deletes any existing salary record for a given appraisal if it exists. Then creates & saves
     * a new salary record for an appraisal.
     *
     * @param appraisal
     * @param configurationMap
     * @return
     */
    public static Salary createOrUpdateSalary(Appraisal appraisal,
                                              Map<String, Configuration> configurationMap) {
        Session session = HibernateUtil.getCurrentSession();
        // delete salary object if it exists
        session.getNamedQuery("salary.deleteSalaryForAppraisal")
                .setInteger("appraisalId", appraisal.getId())
                .executeUpdate();

        // create new salary object
        Job job = appraisal.getJob();
        Salary salary = job.getSalary();
        salary.setAppraisalId(appraisal.getId());

        String aboveOrBelow = "below";
        if (appraisal.getJob().getSalaryCurrent() > appraisal.getJob().getSalaryMidpoint()) {
            aboveOrBelow = "above";
        }

        String increaseRate2Value = configurationMap.get("IT-increase-rate2-" + aboveOrBelow + "-control-value").getValue();
        String increaseRate1MinVal = configurationMap.get("IT-increase-rate1-" + aboveOrBelow + "-control-min-value").getValue();
        String increaseRate1MaxVal= configurationMap.get("IT-increase-rate1-" + aboveOrBelow + "-control-max-value").getValue();

        salary.setTwoIncrease(Double.parseDouble(increaseRate2Value));
        salary.setOneMax(Double.parseDouble(increaseRate1MaxVal));
        salary.setOneMin(Double.parseDouble(increaseRate1MinVal));

        salary.setAppraisalId(appraisal.getId());
        session.save(salary);

        return salary;
    }

    /**
     * Creates a new un approved goal version. It doesn't create the associated assessments
     * since the goal version might not be approved by the supervisor.
     *
     * @param appraisal
     */
    public static void addGoalVersion(Appraisal appraisal) {
        Session session = HibernateUtil.getCurrentSession();
        GoalVersion unApprovedGoalVersion = new GoalVersion();
        unApprovedGoalVersion.setCreateDate(new Date());
        appraisal.addGoalVersion(unApprovedGoalVersion);
        session.save(unApprovedGoalVersion);
    }

    /**
     * Returns the date time when the unapproved goal version of the given appraisal was created.
     * This is needed to figure out when the various goals reactivation pieces are due.
     *
     * @param appraisalID
     * @return
     * @throws Exception
     */
    public static DateTime getPendingRequestGoalVersionCreateDate(int appraisalID) throws Exception {
        Appraisal appraisal = getAppraisal(appraisalID);
        GoalVersion pendingRequestGoalVersion = appraisal.getRequestPendingGoalsVersion();

        if (pendingRequestGoalVersion != null) {
            return new DateTime(pendingRequestGoalVersion.getCreateDate());
        }

        // If we got here it means that the user just submitted the request and thus the goal
        // version hasn't been saved to the db. Thus the create date is now.
        return new DateTime();
    }

    /**
     * Returns the date time when the supervisor approved the goals reactivation request.
     *
     * @param appraisalID
     * @return
     * @throws Exception
     */
    public static DateTime getUnapprovedGoalVersionRequestDecDate(int appraisalID) throws Exception {
        Appraisal appraisal = getAppraisal(appraisalID);
        GoalVersion unapprovedGoalsVersion = appraisal.getUnapprovedGoalsVersion();

        if (unapprovedGoalsVersion != null) {
            return new DateTime(unapprovedGoalsVersion.getRequestDecisionDate());
        }

        return null;
    }
}
