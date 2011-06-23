package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.hibernate.AdminMgr;
import org.testng.annotations.*;

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
        HashMap adminsList = adminMgr.list();
        assert adminsList.size() == 2 : "Invalid list of admins";
        assert adminsList.containsKey(12345) : "Invalid admin in list";
        assert adminsList.containsKey(12467) : "Invalid admin in list";

    }
}
