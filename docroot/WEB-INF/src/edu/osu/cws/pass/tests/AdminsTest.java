package edu.osu.cws.pass.tests;

import edu.osu.cws.pass.models.Admin;
import edu.osu.cws.pass.util.Admins;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.List;

@Test
public class AdminsTest {
    Admins admins = new Admins();

    /**
     * This setup method is run before this class gets executed in order to
     * set the Hibernate environment to TESTING. This will ensure that we use
     * the testing db for tests.
     *
     */
    @BeforeMethod
    public void setUp() throws Exception {
        HibernateUtil.setEnvironment(HibernateUtil.TESTING);
        DBUnit dbunit = new DBUnit();
        dbunit.seedDatabase();
    }

    public void shouldListAdmins() {
        HashMap adminsList = admins.list();
        assert adminsList.size() == 2 : "Invalid list of admins";
        assert adminsList.containsKey(12345) : "Invalid admin in list";
        assert adminsList.containsKey(12467) : "Invalid admin in list";

    }
}
