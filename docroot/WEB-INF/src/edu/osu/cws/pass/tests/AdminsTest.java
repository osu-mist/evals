package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AdminMgr;
import edu.osu.cws.pass.hibernate.EmployeeMgr;
import edu.osu.cws.pass.models.Admin;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.models.ModelException;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;

@Test
public class AdminsTest {
    AdminMgr adminMgr = new AdminMgr();

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
        HashMap adminsList = adminMgr.mapByEmployeeId();
        assert adminsList.size() == 2 : "Invalid list of admins";
        assert adminsList.containsKey(12345) : "Invalid admin in list";
        assert adminsList.containsKey(12467) : "Invalid admin in list";

    }

    public void shouldListSortedAdminsByIsMasterAndThenByLastName() throws Exception {
        ArrayList<Admin> admins = (ArrayList<Admin>) adminMgr.list();
        assert admins.get(1).getIsMaster();
        assert admins.get(1).getEmployee().getId() == 8712359 : "Incorrect sorting by name";

        assert admins.get(2).getIsMaster();
        assert admins.get(2).getEmployee().getId() == 12345 : "Incorrect sorting by name";

        assert !admins.get(0).getIsMaster();
        assert admins.get(0).getEmployee().getId() == 12467 : "Incorrect sorting by name";
    }

    public void shouldDeleteAdminUser() throws Exception {
        ArrayList<Admin> oldAdminList = (ArrayList<Admin>) adminMgr.list();
        adminMgr.delete(1);
        ArrayList<Admin> newAdminList = (ArrayList<Admin>) adminMgr.list();

        assert oldAdminList.size() == newAdminList.size() + 1;

    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddAdminEmployeeWhoIsNotActive() throws Exception {
        Employee loggedInUser = new EmployeeMgr().findByOnid("cedenoj");
        adminMgr.add("testing", "1", loggedInUser);

    }

    @Test(expectedExceptions = {ModelException.class})
    public void shouldNotAddAmindEmployeeWhoIsAlreadyAdmin() throws Exception {
        Employee loggedInUser = new EmployeeMgr().findByOnid("cedenoj");
        adminMgr.add("cedenoj", "0", loggedInUser);
    }

    public void shouldAddAdminUser() throws Exception {
        Employee loggedInUser = new EmployeeMgr().findByOnid("cedenoj");
        adminMgr.add("barlowc", "0", loggedInUser);
        Admin admin = adminMgr.findByOnid("barlowc");
        assert admin != null : "added admin user should not be null";
        assert !admin.getIsMaster() : "Incorrect admin level";
        assert admin.getEmployee().getOnid().equals("barlowc");

        adminMgr.delete(admin.getId());

        adminMgr.add("barlowc", "1", loggedInUser);
        admin = adminMgr.findByOnid("barlowc");
        assert admin != null : "added admin user should not be null";
        assert admin.getIsMaster() : "Incorrect admin level";
        assert admin.getEmployee().getOnid().equals("barlowc");

    }
}
