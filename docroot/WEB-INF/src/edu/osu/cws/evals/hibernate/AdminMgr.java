package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.Admin;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.apache.taglibs.standard.lang.jpath.adapter.StatusIterationContext;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

public class AdminMgr {

    /**
     * Uses list() method to grab a list of admins. Then
     * it creates a map of admins using "pidm" as the key and the
     * admin object as the value.
     *
     * @return ruleMap
     */
    public static HashMap<Integer, Admin> mapByEmployeeId() throws Exception {
        HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();
        for (Admin admin : AdminMgr.list()) {
            admins.put(admin.getEmployee().getId(),  admin);
        }
        return admins;
    }

    /**
     * Grabs a list of admins.
     *
     * @throws Exception
     * @return List<Admin>
     */
    public static List<Admin> list() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List<Admin> admins = session.createQuery("from edu.osu.cws.evals.models.Admin admin " +
                "order by admin.isMaster").list();
        return admins;
    }

    /**
     * Handles deleting an admin user. Checks to make sure pojo exists
     * before trying to delete it.
     *
     * @param id of admin pojo
     * @return success
     * @throws Exception
     */
    public static boolean delete(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Admin admin = (Admin) session.get(Admin.class, id);
        if (admin == null) {
            throw new ModelException("Invalid Admin ID");
        }
        return true;
    }

    /**
     * Retrieves an Admin object from the db
     *
     * @param id
     * @return Admin
     * @throws Exception
     */
    public static Admin get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (Admin) session.get(Admin.class, id);
    }

    /**
     * Retrieves an Admin object from the db using the employee id
     *
     * @param onid
     * @return Admin
     * @throws Exception
     */
    public static Admin findByOnid(String onid) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        String query = "from edu.osu.cws.evals.models.Admin admin where admin.employee.onid = :onid";
        List<Admin> results = (List<Admin>) session.createQuery(query)
                .setString("onid", onid)
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
     * @param isMasterValue
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static boolean add(String onid, String isMasterValue, Employee loggedInUser) throws Exception {
        Employee employee = EmployeeMgr.findByOnid(onid, null);
        boolean isMaster = isMasterValue.equals("1");

        // Check that the employee object is valid
        if (employee.getStatus() == null || !employee.getStatus().equals("A")) {
            throw new ModelException("User not found");
        }

        // Check that the user is not already an admin
        if (findByOnid(onid) != null) {
            throw new ModelException("User is already an admin. To change permissions, delete the user and re-add it.");
        }

        Admin admin = new Admin();
        admin.setCreateDate(new Date());
        admin.setCreator(loggedInUser);
        admin.setEmployee(employee);
        admin.setIsMaster(isMaster);

        Session session = HibernateUtil.getCurrentSession();
        AdminMgr.add(admin, session);

        return true;
    }

    /**
     * Validates the admin pojo and saves it to the db.
     *
     * @param admin
     * @param session
     * @throws Exception
     */
    private static void add(Admin admin, Session session) throws Exception {
        admin.validate();
        session.save(admin);
    }
}
