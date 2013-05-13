package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.hibernate.AdminMgr;
import edu.osu.cws.evals.hibernate.EmployeeMgr;
import edu.osu.cws.evals.models.Admin;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;

@Test
public class AdminsTest {

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldListAdmins() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        HashMap adminsList = AdminMgr.mapByEmployeeId();
        assert adminsList.size() == 3 : "Invalid list of admins";
        assert adminsList.containsKey(12345) : "Invalid admin in list";
        assert adminsList.containsKey(12467) : "Invalid admin in list";
        tx.commit();
    }

    public void shouldListSortedAdminsByIsMaster() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ArrayList<Admin> admins = (ArrayList<Admin>) AdminMgr.list();
        assert admins.get(2).getIsMaster();
        assert admins.get(2).getEmployee().getId() == 8712359 : "Incorrect sorting by name";

        assert admins.get(1).getIsMaster();
        assert admins.get(1).getEmployee().getId() == 12345 : "Incorrect sorting by name";

        assert !admins.get(0).getIsMaster();
        assert admins.get(0).getEmployee().getId() == 12467 : "Incorrect sorting by name";
        tx.commit();
    }

    public void shouldDeleteAdminUser() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        ArrayList<Admin> oldAdminList = (ArrayList<Admin>) AdminMgr.list();
        AdminMgr.delete(1);
        ArrayList<Admin> newAdminList = (ArrayList<Admin>) AdminMgr.list();

        assert oldAdminList.size() == newAdminList.size() + 1;
        tx.commit();
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddAdminEmployeeWhoIsNotActive() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Employee loggedInUser = EmployeeMgr.findByOnid("cedenoj", null);
        AdminMgr.add("testing", "1", loggedInUser);
        tx.commit();
    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddAmindEmployeeWhoIsAlreadyAdmin() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Employee loggedInUser = EmployeeMgr.findByOnid("cedenoj", null);
        AdminMgr.add("cedenoj", "0", loggedInUser);
        tx.commit();
    }

    public void shouldAddAdminUser() throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        Transaction tx = session.beginTransaction();
        Employee loggedInUser = EmployeeMgr.findByOnid("cedenoj", null);
        AdminMgr.add("barlowc", "0", loggedInUser);
        Admin admin = AdminMgr.findByOnid("barlowc");
        assert admin != null : "added admin user should not be null";
        assert !admin.getIsMaster() : "Incorrect admin level";
        assert admin.getEmployee().getOnid().equals("barlowc");

        AdminMgr.delete(admin.getId());

        AdminMgr.add("barlowc", "1", loggedInUser);
        admin = AdminMgr.findByOnid("barlowc");
        assert admin != null : "added admin user should not be null";
        assert admin.getIsMaster() : "Incorrect admin level";
        assert admin.getEmployee().getOnid().equals("barlowc");
        tx.commit();
    }
}
