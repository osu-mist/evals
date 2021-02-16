package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.models.Reviewer;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;

public class ReviewerMgr {
    /**
     * Uses mapByEmployeeId(session) method to grab a mapByEmployeeId of reviewers. Then
     * it creates a map of admins using "pidm" as the key and the
     * admin object as the value.
     *
     * @return ruleMap
     */
    public static HashMap<Integer, Reviewer> mapByEmployeeId() throws Exception {
        HashMap<Integer, Reviewer> reviewers = new HashMap<Integer, Reviewer>();
        for (Reviewer reviewer : ReviewerMgr.list()) {
            reviewers.put(reviewer.getEmployee().getId(), reviewer);
        }
        return reviewers;
    }

    /**
     * Grabs a list of reviewers.
     *
     * @throws Exception
     * @return
     */
    public static List<Reviewer> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<Reviewer> results = session.createQuery("from edu.osu.cws.evals.models.Reviewer reviewer " +
                "order by reviewer.businessCenterName").list();
        return results;
    }

    /**
     * Handles deleting a reviewer user. Checks to make sure pojo exists
     * before trying to delete it.
     *
     * @param id of admin pojo
     * @return success
     * @throws Exception
     */
    public static boolean delete(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Reviewer admin = (Reviewer) session.get(Reviewer.class, id);
        if (admin == null) {
            throw new ModelException("Invalid Reviewer ID");
        }

        session.delete(admin);
        return true;
    }

    /**
     * Retrieves a Reviewer object from the db
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static Reviewer get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Reviewer) session.get(Reviewer.class, id);
    }

    /**
     * Retrieves a reviewer object from the db using the employee id
     *
     * @param onid
     * @return
     * @throws Exception
     */
    public static List<Reviewer> findByOnid(String onid) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "from edu.osu.cws.evals.models.Reviewer reviewer where reviewer.employee.onid = :onid " +
                "and reviewer.businessCenterName = :businessCenterName";
        List<Reviewer> results = (List<Reviewer>) session.createQuery(query)
                .setString("onid", onid)
                .list();

        return results;
    }

    /**
     * Retrieves a reviewer object from the db using the employee id and BC
     *
     * @param onid
     * @param businessCenterName
     * @return
     * @throws Exception
     */
    public static Reviewer findByOnidAndBC(String onid, String businessCenterName) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return ReviewerMgr.findByOnidBC(onid, businessCenterName, session);
    }

    /**
     * Retrieves a reviewer object from the db using the employee id and BC
     *
     * @param onid
     * @param businessCenterName
     * @param session
     * @return
     */
    private static Reviewer findByOnidBC(String onid, String businessCenterName, Session session) {
        String query = "from edu.osu.cws.evals.models.Reviewer reviewer " +
                "where reviewer.employee.onid = :onid " +
                "and reviewer.businessCenterName = :businessCenterName";
        List<Reviewer> results = (List<Reviewer>) session.createQuery(query)
                .setString("onid", onid)
                .setString("businessCenterName", businessCenterName)
                .list();
        if (results.size() == 0)  {
            return null;
        }

        return results.get(0);
    }

    /**
     * Adds admin user. It checks to make sure that the user is a valid admin to add and that
     * it doesn't already exist in the admins table.
     *
     * @param onid
     * @param businessCenterName
     * @return
     * @throws Exception
     */
    public static boolean add(String onid, String businessCenterName) throws Exception {
        Employee employee = EmployeeMgr.findByOnid(onid, null);

        // Check that the employee object is valid
        if (employee.getStatus() == null || !employee.getStatus().equals("A")) {
            throw new ModelException("User not found");
        }

        // Check that the user is not already an reviewer for that business center
        if (findByOnidAndBC(onid, businessCenterName) != null) {
            throw new ModelException("User is already a reviewer for this business center.");
        }

        Reviewer reviewer = new Reviewer();
        reviewer.setEmployee(employee);
        reviewer.setBusinessCenterName(businessCenterName);

        Session session = HibernateUtil.getCurrentSession();
        add(reviewer, session);
        return true;
    }

    /**
     * Validates the reviewer pojo and saves it to the db.
     *
     * @param reviewer
     * @param session
     * @throws Exception
     */
    private static void add(Reviewer reviewer, Session session) throws Exception {
        reviewer.validate();
        session.save(reviewer);
    }

    /**
     * Returns a list of Reviewer objects associated to the given business center
     *
     * @param bcName
     * @return
     * @throws Exception
     */
    public static List<Reviewer> getReviewers(String bcName) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "from edu.osu.cws.evals.models.Reviewer where businessCenterName = :bcName";
        List<Reviewer> results = (List<Reviewer>) session.createQuery(query)
                .setString("bcName", bcName)
                .list();
        return results;
    }
}