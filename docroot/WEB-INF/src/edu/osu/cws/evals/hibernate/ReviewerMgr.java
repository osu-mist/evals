package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.models.Reviewer;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
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
    public HashMap<Integer, Reviewer> mapByEmployeeId() throws Exception {
        HashMap<Integer, Reviewer> reviewers = new HashMap<Integer, Reviewer>();
        Session session = HibernateUtil.getCurrentSession();
        try {
            for (Reviewer reviewer : this.list(session)) {
                reviewers.put(reviewer.getEmployee().getId(), reviewer);
            }
        } catch (Exception e){
            session.close();
            throw e;
        }

        return reviewers;
    }

    /**
     * Uses list(session) method to grab a list of admins.
     *
     * @throws Exception
     * @return
     */
    public List<Reviewer> list() throws Exception {
        List<Reviewer> reviewers = new ArrayList<Reviewer>();
        Session session = HibernateUtil.getCurrentSession();

        try {
            reviewers = list(session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return reviewers;
    }

    /**
     * Retrieves a list of Reviewer from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Reviewer> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Reviewer> result = session.createQuery("from edu.osu.cws.evals.models.Reviewer reviewer " +
                "order by reviewer.businessCenterName, reviewer.employee.lastName, reviewer.employee.firstName").list();
        tx.commit();
        return result;
    }


    /**
     * Handles deleting a reviewer user. Checks to make sure pojo exists
     * before trying to delete it.
     *
     * @param id of admin pojo
     * @return success
     * @throws Exception
     */
    public boolean delete(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        try {
            delete(id, session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return true;
    }

    /**
     * Handles deleting a reviewer user. Checks to make sure pojo exists
     * before trying to delete it.
     *
     * @param id of admin pojo
     * @param session
     * @return success
     * @throws Exception
     */
    private void delete(int id, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        Reviewer admin = (Reviewer) session.get(Reviewer.class, id);
        if (admin == null) {
            throw new ModelException("Invalid Reviewer ID");
        }

        session.delete(admin);
        tx.commit();
    }

    /**
     * Retrieves a Reviewer object from the db
     *
     * @param id
     * @return
     * @throws Exception
     */
    public Reviewer get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Reviewer reviewer;
        try {
            Transaction tx = session.beginTransaction();
            reviewer = get(id, session);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return reviewer;
    }

    /**
     * Retrieves a Reviewer object from the db
     *
     * @param id
     * @param session
     * @return
     */
    private Reviewer get(int id, Session session) {
        return (Reviewer) session.get(Reviewer.class, id);
    }

    /**
     * Retrieves a reviewer object from the db using the employee id
     *
     * @param onid
     * @return
     * @throws Exception
     */
    public List<Reviewer> findByOnid(String onid) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        ArrayList<Reviewer> reviewerList;
        try {
            Transaction tx = session.beginTransaction();
            reviewerList = (ArrayList<Reviewer>) findByOnid(onid, session);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return reviewerList;
    }

    /**
     * Retrieves a reviewer object from the db using the employee id
     *
     * @param onid
     * @param session
     * @return
     */
    private List<Reviewer> findByOnid(String onid, Session session) {
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
    public Reviewer findByOnidAndBC(String onid, String businessCenterName) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Reviewer reviewer;
        try {
            Transaction tx = session.beginTransaction();
            reviewer = findByOnidBC(onid, businessCenterName, session);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return reviewer;
    }

    /**
     * Retrieves a reviewer object from the db using the employee id and BC
     *
     * @param onid
     * @param businessCenterName
     * @param session
     * @return
     */
    private Reviewer findByOnidBC(String onid, String businessCenterName, Session session) {
        String query = "from edu.osu.cws.evals.models.Reviewer reviewer where reviewer.employee.onid = :onid " +
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
    public boolean add(String onid, String businessCenterName) throws Exception {
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid(onid, null);

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
        try {
            add(reviewer, session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return true;
    }

    /**
     * Validates the reviewer pojo and saves it to the db.
     *
     * @param reviewer
     * @param session
     * @throws Exception
     */
    private void add(Reviewer reviewer, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        reviewer.validate();
        session.save(reviewer);
        tx.commit();
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
        List<Reviewer> results = new ArrayList<Reviewer>();
        try {
            Transaction tx = session.beginTransaction();
            String query = "from edu.osu.cws.evals.models.Reviewer where businessCenterName = :bcName";
            results = (List<Reviewer>) session.createQuery(query)
                    .setString("bcName", bcName)
                    .list();
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return results;
    }
}