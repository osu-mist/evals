package edu.osu.cws.pass.hibernate;

import edu.osu.cws.pass.models.Admin;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.models.ModelException;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

public class AdminMgr {

    /**
     * Uses list(session) method to grab a list of admins. Then
     * it creates a map of admins using "pidm"as the key and the
     * admin object as the value.
     *
     * @return ruleMap
     */
    public HashMap<Integer, Admin> mapByEmployeeId() throws Exception {
        HashMap<Integer, Admin> admins = new HashMap<Integer, Admin>();
        Session session = HibernateUtil.getCurrentSession();
        try {
            for (Admin admin : this.list(session)) {
                admins.put(admin.getEmployee().getId(),  admin);
            }
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return admins;
    }

    /**
     * Uses list(session) method to grab a list of admins.
     *
     * @throws Exception
     * @return
     */
    public List<Admin> list() throws Exception {
        List<Admin> admins = new ArrayList<Admin>();
        Session session = HibernateUtil.getCurrentSession();

        try {
            admins = list(session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return admins;
    }

    /**
     * Retrieves a list of PermissionRule from the database.
     *
     * @param session
     * @return
     * @throws Exception
     */
    private List<Admin> list(Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        List<Admin> result = session.createQuery("from edu.osu.cws.pass.models.Admin admin " +
                "order by admin.isMaster, admin.employee.lastName, admin.employee.firstName").list();
        tx.commit();
        return result;
    }

    /**
     * Handles deleting an admin user. Checks to make sure pojo exists
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
     * Handles deleting an admin user. Checks to make sure pojo exists
     * before trying to delete it.
     *
     * @param id of admin pojo
     * @return success
     * @throws Exception
     */
    private void delete(int id, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        Admin admin = (Admin) session.get(Admin.class, id);
        if (admin == null) {
            throw new ModelException("Invalid Admin ID");
        }

        session.delete(admin);
        tx.commit();
    }


    /**
     * Retrieves an Admin object from the db
     *
     * @param id
     * @return
     * @throws Exception
     */
    public Admin get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Admin admin;
        try {
            Transaction tx = session.beginTransaction();
            admin = get(id, session);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return admin;
    }

    /**
     * Retrieves an Admin object from the db
     *
     * @param id
     * @param session
     * @return
     */
    private Admin get(int id, Session session) {
        return (Admin) session.get(Admin.class, id);
    }


    /**
     * Retrieves an Admin object from the db using the employee id
     *
     * @param onid
     * @return
     * @throws Exception
     */
    public Admin findByOnid(String onid) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Admin admin;
        try {
            Transaction tx = session.beginTransaction();
            admin = findByOnid(onid, session);
            tx.commit();
        } catch (Exception e) {
            session.close();
            throw e;
        }

        return admin;
    }

    /**
     * Retrieves an Admin object from the db using the employee id
     *
     * @param onid
     * @param session
     * @return
     */
    private Admin findByOnid(String onid, Session session) {
        String query = "from edu.osu.cws.pass.models.Admin admin where admin.employee.onid = :onid";
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
    public boolean add(String onid, String isMasterValue, Employee loggedInUser) throws Exception {
        EmployeeMgr employeeMgr = new EmployeeMgr();
        Employee employee = employeeMgr.findByOnid(onid);
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
        admin.setScope("");

        Session session = HibernateUtil.getCurrentSession();
        try {
            add(admin, session);
        } catch (Exception e) {
            session.close();
            throw e;
        }
        return true;
    }

    /**
     * Validates the admin pojo and saves it to the db.
     *
     * @param admin
     * @param session
     * @throws Exception
     */
    private void add(Admin admin, Session session) throws Exception {
        Transaction tx = session.beginTransaction();
        admin.validate();
        session.save(admin);
        tx.commit();
    }
}
